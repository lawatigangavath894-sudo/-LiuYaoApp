package com.liuyao.paipan.domain.imports

import java.nio.charset.Charset

object ImportedTextReader {
    const val MAX_BYTES = 20 * 1024 * 1024
    const val AUTO = "AUTO"

    data class DecodedCandidate(
        val encoding: String,
        val text: String,
        val score: Int,
        val warning: String?,
    )

    data class ReadResult(
        val text: String,
        val encoding: String,
        val warning: String?,
        val candidates: List<DecodedCandidate>,
    )

    sealed interface ReadOutcome {
        data class Success(val result: ReadResult) : ReadOutcome
        data class Failure(val message: String) : ReadOutcome
    }

    fun supportedEncodingNames(): List<String> = buildList {
        add("UTF-8")
        add("UTF-8 BOM")
        add("UTF-16LE")
        add("UTF-16BE")
        add("GB18030")
        add("GBK")
        add("GB2312")
        if (charsetAvailable("Big5")) add("Big5")
    }.distinct()

    fun read(bytes: ByteArray?): ReadOutcome {
        if (bytes == null) return ReadOutcome.Failure("无法读取文件")
        if (bytes.isEmpty()) return ReadOutcome.Failure("文件为空")
        if (bytes.size > MAX_BYTES) return ReadOutcome.Failure("文件过大，建议拆分后导入。")

        val candidates = decodeCandidates(bytes)
        val best = candidates.maxByOrNull { it.score }
            ?: return ReadOutcome.Failure("无法识别文件编码")
        return ReadOutcome.Success(
            ReadResult(
                text = best.text,
                encoding = best.encoding,
                warning = best.warning,
                candidates = candidates,
            ),
        )
    }

    fun decode(bytes: ByteArray, encoding: String): DecodedCandidate {
        val (charsetName, skip) = when (encoding) {
            "UTF-8 BOM" -> "UTF-8" to utf8BomSkip(bytes)
            "UTF-16LE" -> "UTF-16LE" to utf16LeBomSkip(bytes)
            "UTF-16BE" -> "UTF-16BE" to utf16BeBomSkip(bytes)
            else -> encoding to 0
        }
        val charset = runCatching { Charset.forName(charsetName) }.getOrElse { Charsets.UTF_8 }
        val raw = runCatching { String(bytes, skip, bytes.size - skip, charset) }
            .getOrElse { String(bytes, Charsets.UTF_8) }
        val cleaned = clean(raw)
        val score = scoreDecodedText(cleaned)
        return DecodedCandidate(
            encoding = encoding,
            text = cleaned,
            score = score,
            warning = if (score < SCORE_WARN_THRESHOLD) "编码可能不准确，请切换编码检查预览。" else null,
        )
    }

    private fun decodeCandidates(bytes: ByteArray): List<DecodedCandidate> {
        val bomCandidate = bomEncoding(bytes)?.let { decode(bytes, it) }
        val candidates = supportedEncodingNames()
            .map { decode(bytes, it) }
            .let { list -> if (bomCandidate == null) list else listOf(bomCandidate) + list }
            .distinctBy { it.encoding }
            .sortedByDescending { it.score }
        return candidates
    }

    fun scoreDecodedText(text: String): Int {
        if (text.isBlank()) return 0
        var replacement = 0
        var chinese = 0
        var visible = 0
        var control = 0
        var mojibake = 0
        for (ch in text) {
            when {
                ch == '\uFFFD' -> replacement++
                ch in '\u4E00'..'\u9FFF' -> {
                    chinese++
                    visible++
                }
                ch == '\n' || ch == '\r' || ch == '\t' -> visible++
                ch.isISOControl() -> control++
                ch.code in 0x20..0x7E -> visible++
                !ch.isISOControl() -> visible++
            }
            if (ch == 'Ã' || ch == 'Â' || ch == '�') mojibake++
        }
        val badWords = listOf("锟斤拷", "���", "Ã", "Â", "¤", "¶", "¼", "½", "闁", "鐨", "鍙", "绛", "涓")
        mojibake += badWords.sumOf { word -> Regex.escape(word).toRegex().findAll(text).count() * 3 }

        val n = text.length.coerceAtLeast(1).toDouble()
        val chineseRatio = chinese / n
        val visibleRatio = visible / n
        val replacementRatio = replacement / n
        val controlRatio = control / n
        val mojibakeRatio = mojibake / n
        val commonChineseHits = listOf("卦", "爻", "世", "应", "财", "官", "父母", "兄弟", "子孙", "断", "占")
            .count { text.contains(it) }

        return (
            visibleRatio * 250 +
                chineseRatio * 500 +
                commonChineseHits * 20 -
                replacementRatio * 900 -
                controlRatio * 500 -
                mojibakeRatio * 1200
            ).toInt().coerceAtLeast(0)
    }

    private fun bomEncoding(bytes: ByteArray): String? = when {
        utf8BomSkip(bytes) > 0 -> "UTF-8 BOM"
        utf16LeBomSkip(bytes) > 0 -> "UTF-16LE"
        utf16BeBomSkip(bytes) > 0 -> "UTF-16BE"
        else -> null
    }

    private fun utf8BomSkip(bytes: ByteArray): Int =
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) 3 else 0

    private fun utf16LeBomSkip(bytes: ByteArray): Int =
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) 2 else 0

    private fun utf16BeBomSkip(bytes: ByteArray): Int =
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) 2 else 0

    private fun charsetAvailable(name: String): Boolean =
        runCatching { Charset.isSupported(name) }.getOrDefault(false)

    private const val SCORE_WARN_THRESHOLD = 170

    private fun clean(raw: String): String {
        var text = raw.removePrefix("\uFEFF")
        text = text.filter { it == '\n' || it == '\r' || it == '\t' || !it.isISOControl() }
        text = text.replace("\r\n", "\n").replace("\r", "\n")
        text = Regex("[\\u0000\\u200B\\u200C\\u200D]").replace(text, "")
        text = Regex("\\n{4,}").replace(text, "\n\n")
        return text.trim()
    }
}
