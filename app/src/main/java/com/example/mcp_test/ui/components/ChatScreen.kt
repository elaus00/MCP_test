package com.example.mcp_test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mcp_test.model.ChatMessage
import com.example.mcp_test.ui.theme.MCP_testTheme
import com.example.mcp_test.viewmodel.ChatViewModel

/**
 * 채팅 화면 UI 컴포넌트
 * @param viewModel 채팅 ViewModel
 * @param onSendMessage 메시지 전송 콜백
 * @param onRetryConnection 서버 재연결 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onSendMessage: (String) -> Unit,
    onRetryConnection: () -> Unit = {}
) {
    val messages = viewModel.messages
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    
    // 메시지 추가될 때마다 스크롤 최하단으로 이동
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCP 채팅") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("메시지를 입력하세요") },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            // 키보드 액션으로 메시지 전송 추가
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (userInput.isNotBlank()) {
                                        viewModel.addMessage(userInput, true)
                                        onSendMessage(userInput)
                                        userInput = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        )
                        IconButton(
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    viewModel.addMessage(userInput, true)
                                    onSendMessage(userInput)
                                    userInput = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty()) {
                // 채팅 메시지가 없을 때 안내 표시
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MCP 테스트 채팅",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onRetryConnection() }) {
                            Text("서버에 연결하기")
                        }
                    }
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(messages) { message ->
                        ChatBubble(message = message, onRetryConnection = onRetryConnection)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MCP_testTheme {
        // 프리뷰용 ViewModel 생성
        val previewViewModel = ChatViewModel().apply {
            addMessage("안녕하세요!", true)
            addMessage("반갑습니다. 무엇을 도와드릴까요?", false)
            addLoadingMessage()
        }
        
        ChatScreen(
            viewModel = previewViewModel, 
            onSendMessage = {}, 
            onRetryConnection = {}
        )
    }
}
