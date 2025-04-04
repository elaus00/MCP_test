package com.example.mcp_test.service

import com.example.mcp_test.MCPClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MCP 클라이언트 서비스 클래스
 * MCPClient 인스턴스를 관리하고 상태 콜백을 제공합니다.
 */
class MCPClientService {
    private val client = MCPClient()
    
    /**
     * 지정된 URL로 MCP 서버에 연결합니다.
     * @param serverUrl MCP 서버 URL
     * @param onSuccess 연결 성공 시 호출될 콜백
     * @param onError 연결 실패 시 호출될 콜백
     */
    suspend fun connectToServer(
        serverUrl: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            client.connectToRemoteServer(serverUrl)
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    /**
     * 쿼리를 MCP 서버에 전송하고 응답을 받습니다.
     * @param query 전송할 쿼리 문자열
     * @return 서버 응답
     */
    suspend fun processQuery(query: String): String {
        return client.processQuery(query)
    }
    
    /**
     * MCP 클라이언트 리소스를 해제합니다.
     */
    fun close() {
        CoroutineScope(Dispatchers.IO).launch {
            client.close()
        }
    }
}
