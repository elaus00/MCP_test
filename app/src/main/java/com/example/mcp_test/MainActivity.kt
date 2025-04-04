package com.example.mcp_test

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mcp_test.service.MCPClientService
import com.example.mcp_test.ui.components.ChatScreen
import com.example.mcp_test.ui.theme.MCP_testTheme
import com.example.mcp_test.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    // ViewModel 인스턴스 생성
    private val viewModel: ChatViewModel by viewModels()
    
    // MCP 클라이언트 서비스
    private lateinit var mcpClientService: MCPClientService
    
    // 에뮬레이터를 사용하는 경우에는 10.0.2.2를 사용해야 합니다
    // 실제 기기에서 테스트할 때는 로컬 네트워크 IP 주소를 사용하세요
    private val serverUrl = "http://10.0.2.2:3001" // 에뮬레이터용 로컬호스트 연결

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // MCP 클라이언트 서비스 초기화
        mcpClientService = MCPClientService()
        
        setContent {
            MCP_testTheme {
                ChatScreen(
                    viewModel = viewModel,
                    onSendMessage = { message ->
                        sendMessage(message)
                    },
                    onRetryConnection = {
                        // 서버 연결 재시도 기능 추가
                        connectToMcpServer()
                    }
                )
            }
        }
        
        // 서버 연결 시도
        connectToMcpServer()
    }
    
    /**
     * MCP 서버에 연결합니다.
     */
    private fun connectToMcpServer() {
        // 서버 연결 시도 메시지 추가
        viewModel.addMessage("서버에 연결 중입니다...", false)
        
        lifecycleScope.launch(Dispatchers.IO) {
            mcpClientService.connectToServer(
                serverUrl = serverUrl,
                onSuccess = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "MCP 서버에 연결되었습니다", Toast.LENGTH_SHORT).show()
                        viewModel.addMessage("서버에 연결되었습니다! 질문을 입력해보세요.", false)
                    }
                },
                onError = { exception -> 
                    lifecycleScope.launch(Dispatchers.Main) {
                        val errorMsg = "MCP 서버 연결 실패: ${exception.message}"
                        Toast.makeText(
                            this@MainActivity,
                            errorMsg,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.addMessage(errorMsg, false)
                        
                        // 연결 실패 시 추가 안내 메시지
                        viewModel.addMessage(
                            "서버가 실행 중인지 확인해보세요. 서버 URL이 '$serverUrl'인지 확인하세요.", 
                            false
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 메시지를 MCP 서버로 전송합니다.
     */
    private fun sendMessage(message: String) {
        // 로딩 메시지 추가
        viewModel.addLoadingMessage()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = mcpClientService.processQuery(message)
                withContext(Dispatchers.Main) {
                    // 로딩 메시지 제거
                    viewModel.removeLoadingMessage()
                    // 응답을 추가
                    viewModel.addMessage(response, false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 로딩 메시지 제거
                    viewModel.removeLoadingMessage()
                    // 오류 메시지 추가
                    viewModel.addMessage("오류 발생: ${e.message}", false)
                    Toast.makeText(
                        this@MainActivity,
                        "메시지 처리 실패: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 클라이언트 리소스 해제
        mcpClientService.close()
    }
}
