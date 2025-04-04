// TestMcpConnection.kt 파일 생성
import com.example.mcp_test.MCPClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val serverUrl = "http://localhost:3001"
    println("Starting MCP connection test to $serverUrl")

    try {
        val client = MCPClient()
        client.use {
            // 서버에 연결
            println("Connecting to server...")
            client.connectToRemoteServer(serverUrl)

            // 간단한 쿼리 전송
            println("\nSending test query...")
            val response = client.processQuery("Hello, can you list the available tools? 한글로 답해줘")
            println("\nResponse received:\n$response")

            println("\nTest completed successfully!")
        }
    } catch (e: Exception) {
        println("\nTest failed: ${e.message}")
        e.printStackTrace()
    }
}