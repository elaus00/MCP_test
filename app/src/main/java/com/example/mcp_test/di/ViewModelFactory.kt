package com.example.mcp_test.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcp_test.viewmodel.ChatViewModel

/**
 * ViewModel 팩토리 클래스
 * 필요한 종속성을 ViewModel에 주입하기 위한 팩토리
 */
class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
