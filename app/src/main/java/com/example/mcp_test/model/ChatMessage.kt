package com.example.mcp_test.model

/**
 * 채팅 메시지를 나타내는 데이터 클래스
 * @param text 메시지 내용
 * @param isUser 사용자가 보낸 메시지 여부
 * @param isLoading 로딩 중인 메시지 여부
 */
data class ChatMessage(
    val text: String, 
    val isUser: Boolean, 
    val isLoading: Boolean = false
)
