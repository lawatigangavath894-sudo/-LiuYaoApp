package com.liuyao.paipan.ui.screens.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.ai.AiChatMessage
import com.liuyao.paipan.data.ai.AiConfigStore
import com.liuyao.paipan.data.ai.AiProviderConfig
import com.liuyao.paipan.data.ai.OpenAiCompatibleClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiChatUiState(
    val provider: AiProviderConfig = AiProviderConfig(),
    val messages: List<AiChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val message: String? = null,
)

class AiChatViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AiConfigStore(app.applicationContext)
    private val client = OpenAiCompatibleClient()
    private val _ui = MutableStateFlow(AiChatUiState(provider = store.loadProvider(), messages = store.loadMessages()))
    val ui = _ui.asStateFlow()

    fun reloadProvider() = _ui.update { it.copy(provider = store.loadProvider()) }
    fun setInput(value: String) = _ui.update { it.copy(input = value, message = null) }

    fun newConversation() {
        store.saveMessages(emptyList())
        _ui.update { it.copy(messages = emptyList(), input = "", message = "已新建对话。") }
    }

    fun clearContext() {
        store.saveMessages(emptyList())
        _ui.update { it.copy(messages = emptyList(), message = "上下文已清空。") }
    }

    fun deleteConversation() {
        store.saveMessages(emptyList())
        _ui.update { it.copy(messages = emptyList(), input = "", message = "当前对话已删除。") }
    }

    fun sendInput() {
        val input = _ui.value.input.trim()
        if (input.isBlank()) {
            _ui.update { it.copy(message = "请输入内容后再发送。") }
            return
        }
        sendText(input)
    }

    fun sendText(text: String) {
        val provider = store.loadProvider()
        val invalid = client.validate(provider)
        if (invalid != null) {
            _ui.update { it.copy(provider = provider, message = "请先在设置中配置 AI 模型。$invalid") }
            return
        }
        val user = AiChatMessage(role = "user", content = text)
        val pending = AiChatMessage(role = "assistant", content = "正在生成...", status = "sending")
        val base = _ui.value.messages + user + pending
        _ui.update { it.copy(provider = provider, messages = base, input = "", isSending = true, message = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                client.chat(provider, base.filter { it.status != "sending" })
            }
            val finalMessages = if (result.isSuccess) {
                base.dropLast(1) + AiChatMessage(role = "assistant", content = result.getOrThrow())
            } else {
                base.dropLast(1) + AiChatMessage(
                    role = "error",
                    content = result.exceptionOrNull()?.message ?: "网络连接失败。",
                    status = "failed",
                )
            }
            store.saveMessages(finalMessages)
            _ui.update { it.copy(messages = finalMessages, isSending = false) }
        }
    }
}
