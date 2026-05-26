package com.liuyao.paipan.domain.imports

import java.nio.charset.Charset

object ImportedTextReader {
    const val MAX_BYTES = 5 * 1024 * 1024

    data class ReadResult(
        val text: String,
        val encoding: String,
        val warning: String?,
    )

    sealed interface ReadOutcome {
        data class Success(val result: ReadResult) : ReadOutcome
        data class Failure(val message: String) : ReadOutcome
    }

    fun read(bytes: ByteArray?): ReadOutcome {
        if (bytes == null) return ReadOutcome.Failure("无法读取文件")
        if (bytes.isEmpty()) return ReadOutcome.Failure("文件为空")
        if (bytes.size > MAX_BYTES) return ReadOutcome.Failure("文件较大，建议拆分后导入。")

        bomCharset(bytes)?.let { (cs, name, skip) ->
            val text = String(bytes, skip, bytes.size - skip, cs)
            return ReadOutcome.Success(ReadResult(clean(text), name, null))
        }

        val candidates = buildList {
            add("UTF-8")
            add("GBK")
            add("GB2312")
            if (charsetAvailable("Big5")) add("Big5")
            add(Charset.defaultCharset().name())
        }.distinct()

        var bestName: String? = null
        var bestScore = Double.NEGATIVE_INFINITY
        var bestText = ""
        for (name in candidates) {
            val cs = runCatching { Charset.forName(name) }.getOrNull() ?: continue
            val decoded = String(bytes, cs)
            val score = score(decoded)
            if (score > bestScore) {
                bestName = name
                bestScore = score
                bestText = decoded
            }
        }

        val encName = bestName ?: Charset.defaultCharset().name()
        val finalText = if (bestName == null) String(bytes, Charset.defaultCharset()) else bestText
        val warning = if (bestScore < SCORE_WARN_THRESHOLD) "编码可能不准确，请检查预览内容是否正常。" else null
        return ReadOutcome.Success(ReadResult(clean(finalText), encName, warning))
    }

    private fun charsetAvailable(name: String): Boolean =
        runCatching { Charset.isSupported(name) }.getOrDefault(false)

    private fun bomCharset(b: ByteArray): Triple<Charset, String, Int>? = when {
        b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte() ->
            Triple(Charsets.UTF_8, "UTF-8 (BOM)", 3)
        b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte() ->
            Triple(Charsets.UTF_16LE, "UTF-16LE", 2)
        b.size >= 2 && b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte() ->
            Triple(Charsets.UTF_16BE, "UTF-16BE", 2)
        else -> null
    }

    private const val SCORE_WARN_THRESHOLD = 0.85

    private fun score(text: String): Double {
        if (text.isEmpty()) return 0.0
        var replacement = 0
        var chinese = 0
        var visible = 0
        var control = 0
        for (ch in text) {
            when {
                ch == '\uFFFD' -> replacement++
                ch in '\u4E00'..'\u9FFF' -> { chinese++; visible++ }
                ch == '\n' || ch == '\r' || ch == '\t' -> visible++
                ch.isISOControl() -> control++
                ch.code in 0x20..0x7E -> visible++
                !ch.isISOControl() -> visible++
            }
        }
        val n = text.length.toDouble()
        val replacePenalty = replacement / n
        val controlPenalty = control / n
        val visibleRatio = visible / n
        val chineseBonus = (chinese / n).coerceAtMost(0.5)
        return (visibleRatio + chineseBonus - replacePenalty * 3 - controlPenalty * 2).coerceIn(0.0, 1.0)
    }

    private fun clean(raw: String): String {
        var t = raw.removePrefix("\uFEFF")
        t = t.filter { it == '\n' || it == '\r' || it == '\t' || !it.isISOControl() }
        t = t.replace("\r\n", "\n").replace("\r", "\n")
        t = Regex("\\n{3,}").replace(t, "\n\n")
        return t.trim()
    }
}
