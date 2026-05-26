package com.liuyao.paipan.data.ai

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OpenAiCompatibleClient {
    fun validate(config: AiProviderConfig): String? = when {
        config.baseUrl.isBlank() -> "请填写 Base URL。"
        config.apiKey.isBlank() -> "请填写 API Key。"
        config.modelName.isBlank() -> "请填写模型名。"
        else -> null
    }

    fun test(config: AiProviderConfig): Result<String> {
        val invalid = validate(config)
        if (invalid != null) return Result.failure(IllegalArgumentException(invalid))
        return chat(config, listOf(AiChatMessage(role = "user", content = "ping")), maxTokensOverride = 8)
            .map { "连接成功：${config.modelName}" }
    }

    fun chat(
        config: AiProviderConfig,
        messages: List<AiChatMessage>,
        maxTokensOverride: Int? = null,
    ): Result<String> = runCatching {
        validate(config)?.let { throw IllegalArgumentException(it) }
        val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        val body = JSONObject()
            .put("model", config.modelName)
            .put("temperature", config.temperature)
            .put("max_tokens", maxTokensOverride ?: config.maxTokens)
            .put("stream", false)
            .put(
                "messages",
                JSONArray().also { arr ->
                    messages.filter { it.role == "user" || it.role == "assistant" || it.role == "system" }
                        .takeLast(20)
                        .forEach { msg ->
                            arr.put(JSONObject().put("role", msg.role).put("content", msg.content))
                        }
                },
            )
            .toString()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() }.orEmpty()
        if (code !in 200..299) throw IllegalStateException(classifyError(code, response))
        val json = JSONObject(response)
        json.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("服务商返回为空，模型名称可能不正确。")
    }

    private fun classifyError(code: Int, body: String): String {
        val lower = body.lowercase()
        return when (code) {
            400 -> if ("model" in lower) "模型名称可能不正确。" else "请求参数无效，请检查模型名和 Base URL。"
            401, 403 -> "API Key 无效、权限不足或余额不可用。"
            404 -> "Base URL 或模型接口路径不正确。"
            429 -> "服务商返回限流，请稍后再试。"
            in 500..599 -> "服务商服务器错误，请稍后再试。"
            else -> "网络请求失败($code)：${body.take(160)}"
        }
    }
}
