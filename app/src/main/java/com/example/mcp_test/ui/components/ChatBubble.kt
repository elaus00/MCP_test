package com.example.mcp_test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mcp_test.model.ChatMessage

/**
 * 채팅 버블을 표시하는 Composable 함수
 * @param message 표시할 채팅 메시지
 * @param onRetryConnection 서버 재연결 버튼 클릭 리스너
 */
@Composable
fun ChatBubble(
    message: ChatMessage,
    onRetryConnection: () -> Unit = {}
) {
    val isUser = message.isUser
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary 
                          else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary 
                    else MaterialTheme.colorScheme.onSecondaryContainer
    
    // 중요: Alignment.Start/End를 Alignment.CenterStart/CenterEnd로 변경
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        if (message.isLoading) {
            // 로딩 인디케이터 표시
            Column(
                modifier = Modifier
                    .widthIn(max = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "응답 기다리는 중...",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 14.sp
                )
            }
        } else {
            // 일반 메시지 표시
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .padding(12.dp)
            ) {
                // 긴 메시지의 경우 스크롤링 지원
                Box(
                    modifier = Modifier
                        .heightIn(max = 400.dp) // 최대 텍스트 영역 제한
                ) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.3.sp
                    )
                }
                
                // 서버 연결 실패 메시지에 재시도 버튼 추가
                if (!isUser && message.text.contains("MCP 서버 연결 실패")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onRetryConnection() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("재연결")
                    }
                }
            }
        }
    }
}
