package com.example.mcp_test

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.anthropic.models.messages.Model
import com.anthropic.models.messages.Tool
import com.anthropic.models.messages.ToolUnion
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.mcpSse
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

class MCPClient : AutoCloseable {
    private val anthropic = AnthropicOkHttpClient.builder()
        .apiKey(BuildConfig.ANTHROPIC_API_KEY) // BuildConfig에서 API 키 가져오기
        .build()

    private val mcp: Client =
        Client(clientInfo = Implementation(name = "mcp-client-cli", version = "1.0.0"))
    private lateinit var tools: List<ToolUnion>
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(SSE)
//        install(Logging) {
//            level = LogLevel.ALL
//        }
    }

    override fun close() {
        runBlocking {
            mcp.close()
            anthropic.close()
            httpClient.close()
        }
    }

    // 로컬 서버 스크립트 방식 (기존 방식)
    suspend fun connectToServer(serverScriptPath: String) {
        try {
            val command = buildList {
                when (serverScriptPath.substringAfterLast(".")) {
                    "js" -> add("node")
                    "py" -> add(
                        if (System.getProperty("os.name").lowercase()
                                .contains("win")
                        ) "python" else "python3"
                    )

                    "jar" -> addAll(listOf("java", "-jar"))
                    else -> throw IllegalArgumentException("Server script must be a .js, .py or .jar file")
                }
                add(serverScriptPath)
            }

            val process = ProcessBuilder(command).start()
            val transport = StdioClientTransport(
                input = process.inputStream.asSource().buffered(),
                output = process.outputStream.asSink().buffered()
            )

            mcp.connect(transport)
            loadTools()
        } catch (e: Exception) {
            println("Failed to connect to MCP server: $e")
            throw e
        }
    }

    // 새로운 SSE 기반 원격 서버 연결 방식
    suspend fun connectToRemoteServer(serverUrl: String) {
        try {
            // Ktor 클라이언트로 SSE 기반 MCP 클라이언트 생성 및 연결
            val mcpClient = httpClient.mcpSse(
                urlString = serverUrl,
                reconnectionTime = 5.seconds
            )

            // mcp 참조 업데이트
            this.mcp.close() // 기존 클라이언트 닫기
            this.javaClass.getDeclaredField("mcp").apply {
                isAccessible = true
                set(this@MCPClient, mcpClient)
            }

            loadTools()

            println("Connected to remote MCP server at $serverUrl with tools: ${
                tools.joinToString(", ") { it.tool().get().name() }
            }")
        } catch (e: Exception) {
            println("Failed to connect to remote MCP server: $e")
            throw e
        }
    }

    // 도구 목록 로드 공통 함수
    private suspend fun loadTools() {
        val toolsResult = mcp.listTools()
        tools = toolsResult?.tools?.map { tool ->
            ToolUnion.ofTool(
                Tool.builder()
                    .name(tool.name)
                    .description(tool.description ?: "")
                    .inputSchema(
                        Tool.InputSchema.builder()
                            .type(JsonValue.from(tool.inputSchema.type))
                            .properties(tool.inputSchema.properties.toJsonValue())
                            .putAdditionalProperty(
                                "required",
                                JsonValue.from(tool.inputSchema.required)
                            )
                            .build()
                    )
                    .build()
            )
        } ?: emptyList()
    }

    private val messageParamsBuilder: MessageCreateParams.Builder = MessageCreateParams.builder()
        .model(Model.CLAUDE_3_5_SONNET_20241022)
        .maxTokens(1024)

    suspend fun processQuery(query: String): String {
        val messages = mutableListOf(
            MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(query)
                .build()
        )

        val response = anthropic.messages().create(
            messageParamsBuilder
                .messages(messages)
                .tools(tools)
                .build()
        )

        val finalText = mutableListOf<String>()
        response.content().forEach { content ->
            when {
                content.isText() -> finalText.add(content.text().getOrNull()?.text() ?: "")

                content.isToolUse() -> {
                    val toolName = content.toolUse().get().name()
                    val toolArgs =
                        content.toolUse().get()._input().convert(object : TypeReference<Map<String, JsonValue>>() {})

                    val result = mcp.callTool(
                        name = toolName,
                        arguments = toolArgs ?: emptyMap()
                    )
                    finalText.add("[Calling tool $toolName with args $toolArgs]")

                    messages.add(
                        MessageParam.builder()
                            .role(MessageParam.Role.USER)
                            .content(
                                """
                                "type": "tool_result",
                                "tool_name": $toolName,
                                "result": ${result?.content?.joinToString("\n") { (it as TextContent).text ?: "" }}
                            """.trimIndent()
                            )
                            .build()
                    )

                    val aiResponse = anthropic.messages().create(
                        messageParamsBuilder
                            .messages(messages)
                            .build()
                    )

                    finalText.add(aiResponse.content().first().text().getOrNull()?.text() ?: "")
                }
            }
        }

        return finalText.joinToString("\n", prefix = "", postfix = "")
    }

    suspend fun chatLoop() {
        println("\nMCP Client Started!")
        println("Type your queries or 'quit' to exit.")

        while (true) {
            print("\nQuery: ")
            val message = readLine() ?: break
            if (message.lowercase() == "quit") break
            val response = processQuery(message)
            println("\n$response")
        }
    }
}

private fun JsonObject.toJsonValue(): JsonValue {
    val mapper = ObjectMapper()
    val node = mapper.readTree(this.toString())
    return JsonValue.fromJsonNode(node)
}