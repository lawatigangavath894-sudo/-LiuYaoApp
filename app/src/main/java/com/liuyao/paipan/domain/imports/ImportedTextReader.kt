package com.liuyao.paipan.domain.imports

import java.nio.charset.Charset

/**
 * 导入文本读取器:从原始 bytes 检测编码并解码为字符串。
 *
 * 修复点:绝不直接用默认 UTF-8 readText(),否则 GBK/GB2312 中文乱码。
 * 流程:BOM 检测 → 无 BOM 时多编码评分择优 → 清理 BOM/控制字符/重复空行。
 * 纯逻辑(只依赖 JVM Charset),便于测试;Uri/InputStream 由调用方提供 bytes。
 */
object ImportedTextReader {

    /** 文件大小上限 5MB */
    const val MAX_BYTES = 5 * 1024 * 1024

    data class ReadResult(
        val text: String,
        val encoding: String,
        val warning: String?, // 非空 = 编码可能不准确等提示
    )

    sealed interface ReadOutcome {
        data class Success(val result: ReadResult) : ReadOutcome
        data class Failure(val message: String) : ReadOutcome
    }

    fun read(bytes: ByteArray?): ReadOutcome {
        if (bytes == null) return ReadOutcome.Failure("无法读取文件")
        if (bytes.isEmpty()) return ReadOutcome.Failure("文件为空")
        if (bytes.size > MAX_BYTES) return ReadOutcome.Failure("文件较大,建议拆分后导入。")

        // 1) BOM 检测
        bomCharset(bytes)?.let { (cs, name, skip) ->
            val text = String(bytes, skip, bytes.size - skip, cs)
            return ReadOutcome.Success(ReadResult(clean(text), name, null))
        }

        // 2) 无 BOM:候选编码评分择优
        val candidates = buildList {
            add("UTF-8")
            add("GBK")
            add("GB2312")
            if (charsetAvailable("Big5")) add("Big5")
            add(Charset.defaultCharset().name())
        }.distinct()

        var best: Pair<String, Double>? = null
        var bestText = ""
        for (name in candidates) {
            val cs = runCatching { Charset.forName(name) }.getOrNull() ?: continue
            val decoded = String(bytes, cs)
            val score = score(decoded)
            if (best == null || score > best!!.second) {
                best = name to score
                bestText = decoded
            }
        }

        if (best == null) {
            // 兜底:系统默认
            val decoded = String(bytes, Charset.defaultCharset())
            return ReadOutcome.Success(ReadResult(clean(decoded), Charset.defaultCharset().name(), "编码可能不准确,请检查预览内容是否正常。"))
        }

        val (encName, bestScore) = best!!
        // 评分偏低(仍有较多替换符/不可见字符)→ 提示
        val warning = if (bestScore < SCORE_WARN_THRESHOLD) "编码可能不准确,请检查预览内容是否正常。" else null
        return ReadOutcome.Success(ReadResult(clean(bestText), encName, warning))
    }

    private fun charsetAvailable(name: String): Boolean = runCatching { Charset.isSupported(name) }.getOrDefault(false)

    /** 返回 (Charset, 显示名, 跳过的BOM字节数) */
    private fun bomCharset(b: ByteArray): Triple<Charset, String, Int>? {
        return when {
            b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte() ->
                Triple(Charsets.UTF_8, "UTF-8 (BOM)", 3)
            b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte() ->
                Triple(Charsets.UTF_16LE, "UTF-16LE", 2)
            b.size >= 2 && b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte() ->
                Triple(Charsets.UTF_16BE, "UTF-16BE", 2)
            else -> null
        }
    }

    private const val SCORE_WARN_THRESHOLD = 0.85

    /**
     * 解码质量评分 ∈ [0,1]。综合:替换符越少越好、中文/可见字符占比越高越好、控制字符越少越好。
     */
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
        // 中文占比作为加分(中文资料应有较高中文比例)
        val chineseBonus = (chinese / n).coerceAtMost(0.5)
        return (visibleRatio + chineseBonus - replacePenalty * 3 - controlPenalty * 2).coerceIn(0.0, 1.0)
    }

    /** 清理:去 BOM 残留、剥离异常控制字符、合并 3+ 连续空行为 1 个,保留换行结构 */
    private fun clean(raw: String): String {
        var t = raw.removePrefix("\uFEFF")
        // 去除除 \n \r \t 外的控制字符
        t = t.filter { it == '\n' || it == '\r' || it == '\t' || !it.isISOControl() }
        // 统一换行,合并多空行
        t = t.replace("\r\n", "\n").replace("\r", "\n")
        t = Regex("\\n{3,}").replace(t, "\n\n")
        return t.trim()
    }
}
