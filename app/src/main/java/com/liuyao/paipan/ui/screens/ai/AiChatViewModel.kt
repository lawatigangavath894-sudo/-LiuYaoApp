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
    private val autoSentChartIds = mutableSetOf<String>()

    private val _ui = MutableStateFlow(AiChatUiState(provider = store.loadProvider(), messages = store.loadMessages()))
    val ui = _ui.asStateFlow()

    fun reloadProvider() {
        val provider = store.loadProvider()
        _ui.update { it.copy(provider = provider, message = null) }
    }

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

    fun showModelSelectTodo() {
        _ui.update { it.copy(message = "模型选择将在后续版本开放。") }
    }

    fun sendInput() {
        val input = _ui.value.input.trim()
        if (input.isBlank()) {
            _ui.update { it.copy(message = "请输入内容。") }
            return
        }
        sendText(input)
    }

    fun retryLastMessage() {
        val lastUser = _ui.value.messages.lastOrNull { it.role == "user" }?.content
        if (lastUser.isNullOrBlank()) {
            _ui.update { it.copy(message = "没有可重试的消息。") }
            return
        }
        val withoutLastError = _ui.value.messages.dropLastWhile { it.role == "error" }
        store.saveMessages(withoutLastError)
        _ui.update { it.copy(messages = withoutLastError) }
        sendText(lastUser, appendUserMessage = false)
    }

    fun autoAnalyzeChart(chartId: String?, prompt: String?) {
        if (chartId.isNullOrBlank() || prompt.isNullOrBlank()) return
        if (chartId in autoSentChartIds) return
        val provider = store.loadProvider()
        val invalid = client.validate(provider)
        if (invalid != null) {
            _ui.update { it.copy(provider = provider, message = "请先在设置中配置 AI 模型。") }
            return
        }
        autoSentChartIds += chartId
        newConversation()
        sendText(prompt)
    }

    fun sendText(text: String, appendUserMessage: Boolean = true) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            _ui.update { it.copy(message = "请输入内容。") }
            return
        }
        if (_ui.value.isSending) return

        val provider = store.loadProvider()
        val invalid = client.validate(provider)
        if (invalid != null) {
            _ui.update { it.copy(provider = provider, message = "请先在设置中配置 AI 模型。$invalid") }
            return
        }

        val user = AiChatMessage(role = "user", content = trimmed)
        val pending = AiChatMessage(role = "assistant", content = "正在生成...", status = "sending")
        val existing = if (appendUserMessage) _ui.value.messages + user else _ui.value.messages
        val base = existing + pending
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
