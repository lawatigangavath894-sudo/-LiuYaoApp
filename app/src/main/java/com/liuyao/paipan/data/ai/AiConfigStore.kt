package com.liuyao.paipan.data.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AiProviderConfig(
    val id: String = "default",
    val providerName: String = "OpenAI-compatible",
    val providerType: String = "OPENAI_COMPATIBLE",
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = "",
    val isDefault: Boolean = true,
    val enableStreaming: Boolean = false,
    val temperature: Double = 0.7,
    val maxTokens: Int = 1200,
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val status: String = "completed",
    val createdAt: Long = System.currentTimeMillis(),
)

class AiConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)

    fun loadProvider(): AiProviderConfig = AiProviderConfig(
        id = prefs.getString("id", "default") ?: "default",
        providerName = prefs.getString("providerName", "OpenAI-compatible") ?: "OpenAI-compatible",
        baseUrl = prefs.getString("baseUrl", "") ?: "",
        apiKey = prefs.getString("apiKey", "") ?: "",
        modelName = prefs.getString("modelName", "") ?: "",
        enableStreaming = prefs.getBoolean("enableStreaming", false),
        temperature = prefs.getFloat("temperature", 0.7f).toDouble(),
        maxTokens = prefs.getInt("maxTokens", 1200),
    )

    fun saveProvider(config: AiProviderConfig) {
        // TODO: API Key currently uses private SharedPreferences. Migrate to Keystore/EncryptedDataStore before release.
        prefs.edit()
            .putString("id", config.id)
            .putString("providerName", config.providerName)
            .putString("baseUrl", config.baseUrl.trim())
            .putString("apiKey", config.apiKey.trim())
            .putString("modelName", config.modelName.trim())
            .putBoolean("enableStreaming", config.enableStreaming)
            .putFloat("temperature", config.temperature.toFloat())
            .putInt("maxTokens", config.maxTokens)
            .apply()
    }

    fun clearProvider() {
        prefs.edit().clear().apply()
    }

    fun loadMessages(): List<AiChatMessage> {
        val raw = prefs.getString("messages", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                AiChatMessage(
                    id = o.optString("id"),
                    role = o.optString("role"),
                    content = o.optString("content"),
                    status = o.optString("status", "completed"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveMessages(messages: List<AiChatMessage>) {
        val arr = JSONArray()
        messages.takeLast(80).forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("role", it.role)
                    .put("content", it.content)
                    .put("status", it.status)
                    .put("createdAt", it.createdAt),
            )
        }
        prefs.edit().putString("messages", arr.toString()).apply()
    }
}
