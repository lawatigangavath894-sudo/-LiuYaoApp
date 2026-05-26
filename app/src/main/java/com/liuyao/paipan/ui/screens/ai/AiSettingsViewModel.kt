package com.liuyao.paipan.ui.screens.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.ai.AiConfigStore
import com.liuyao.paipan.data.ai.AiProviderConfig
import com.liuyao.paipan.data.ai.OpenAiCompatibleClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiSettingsUiState(
    val config: AiProviderConfig = AiProviderConfig(),
    val showApiKey: Boolean = false,
    val isTesting: Boolean = false,
    val message: String? = null,
)

class AiSettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AiConfigStore(app.applicationContext)
    private val client = OpenAiCompatibleClient()
    private val _ui = MutableStateFlow(AiSettingsUiState(config = store.loadProvider()))
    val ui = _ui.asStateFlow()

    fun update(config: AiProviderConfig) = _ui.update { it.copy(config = config, message = null) }
    fun toggleApiKey() = _ui.update { it.copy(showApiKey = !it.showApiKey) }

    fun save() {
        val config = _ui.value.config
        val invalid = client.validate(config)
        if (invalid != null) {
            _ui.update { it.copy(message = invalid) }
            return
        }
        store.saveProvider(config)
        _ui.update { it.copy(message = "AI 配置已保存。") }
    }

    fun deleteProvider() {
        store.clearProvider()
        _ui.update { AiSettingsUiState(message = "AI 配置已删除。") }
    }

    fun testConnection() {
        val config = _ui.value.config
        val invalid = client.validate(config)
        if (invalid != null) {
            _ui.update { it.copy(message = invalid) }
            return
        }
        _ui.update { it.copy(isTesting = true, message = "正在测试连接...") }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { client.test(config) }
            _ui.update { it.copy(isTesting = false, message = result.getOrElse { e -> e.message ?: "网络连接失败。" }) }
        }
    }
}
