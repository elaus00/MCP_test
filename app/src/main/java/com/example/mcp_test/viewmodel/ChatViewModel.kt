package com.example.mcp_test.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.mcp_test.model.ChatMessage

/**
 * 채팅 화면의 상태를 관리하는 ViewModel
 */
class ChatViewModel : ViewModel() {
    // 채팅 메시지 목록 (상태)
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    /**
     * 새 메시지 추가
     * @param text 메시지 내용
     * @param isUser 사용자 메시지 여부
     */
    fun addMessage(text: String, isUser: Boolean) {
        _messages.add(ChatMessage(text, isUser))
    }

    /**
     * 로딩 메시지 추가
     */
    fun addLoadingMessage() {
        _messages.add(ChatMessage("", false, true))
    }

    /**
     * 로딩 메시지 제거
     */
    fun removeLoadingMessage() {
        _messages.removeAll { it.isLoading }
    }

    /**
     * 모든 메시지 삭제
     */
    fun clearMessages() {
        _messages.clear()
    }
}
