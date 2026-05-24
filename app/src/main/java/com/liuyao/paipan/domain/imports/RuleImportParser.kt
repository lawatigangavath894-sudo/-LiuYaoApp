package com.liuyao.paipan.domain.imports

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.RuleTarget

/**
 * 断语文本解析器(第一版,半自动)。
 *
 * 职责:把 txt / markdown 文本切成断语段,并对每段做关键词识别,生成 [DraftRule]。
 * 不追求完美:识别不到的字段留空(由 UI 标"待人工确认")。纯函数,独立可测。
 *
 * 切段规则(任一即为新段边界):
 *  - markdown 标题行(# / ## ...);
 *  - 空行分隔的段落;
 *  - 有序编号(1. / 1、/ (1) / 一、 等);
 *  - 项目符号(- / * / ・ / •)。
 */
object RuleImportParser {

    /** 占类关键词 → 枚举 */
    private val CATEGORY_KEYWORDS: List<Pair<String, DivinationCategory>> = listOf(
        "婚姻" to DivinationCategory.MARRIAGE,
        "感情" to DivinationCategory.MARRIAGE,
        "财运" to DivinationCategory.WEALTH,
        "求财" to DivinationCategory.WEALTH,
        "考试" to DivinationCategory.STUDY,
        "学业" to DivinationCategory.STUDY,
        "工作" to DivinationCategory.CAREER,
        "求职" to DivinationCategory.CAREER,
        "事业" to DivinationCategory.CAREER,
        "疾病" to DivinationCategory.HEALTH,
        "病" to DivinationCategory.HEALTH,
        "寻物" to DivinationCategory.LOST,
        "失物" to DivinationCategory.LOST,
        "孕产" to DivinationCategory.PREGNANCY,
        "怀孕" to DivinationCategory.PREGNANCY,
        "官司" to DivinationCategory.LAWSUIT,
        "诉讼" to DivinationCategory.LAWSUIT,
        "房宅" to DivinationCategory.HOUSE,
        "买房" to DivinationCategory.HOUSE,
        "合作" to DivinationCategory.COOPERATION,
    )

    /** 类神关键词 → RuleTarget */
    private val TARGET_KEYWORDS: List<Pair<String, RuleTarget>> = listOf(
        "官鬼" to RuleTarget.kin(SixKin.OFFICIAL),
        "父母" to RuleTarget.kin(SixKin.PARENT),
        "妻财" to RuleTarget.kin(SixKin.WEALTH),
        "子孙" to RuleTarget.kin(SixKin.OFFSPRING),
        "兄弟" to RuleTarget.kin(SixKin.SIBLING),
        "世爻" to RuleTarget.World,
        "应爻" to RuleTarget.Response,
        "用神" to RuleTarget.UseGod,
    )

    /** 解析全文为草稿列表 */
    fun parse(text: String): List<DraftRule> =
        splitSegments(text).map { seg -> toDraft(seg) }

    /** 切段 */
    fun splitSegments(text: String): List<String> {
        val segments = mutableListOf<String>()
        val current = StringBuilder()

        fun flush() {
            val s = current.toString().trim()
            if (s.isNotBlank()) segments.add(s)
            current.clear()
        }

        text.lines().forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.isBlank() -> flush()
                isHeading(line) -> { flush(); current.append(cleanMarkdown(line)) }
                isListItem(line) -> { flush(); current.append(cleanListMarker(line)) }
                isNumbered(line) -> { flush(); current.append(cleanNumber(line)) }
                else -> {
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(cleanMarkdown(line))
                }
            }
        }
        flush()
        return segments.filter { it.length >= 2 } // 丢弃过短噪声
    }

    private fun toDraft(segment: String): DraftRule {
        val category = CATEGORY_KEYWORDS.firstOrNull { segment.contains(it.first) }?.second
        val target = TARGET_KEYWORDS.firstOrNull { segment.contains(it.first) }?.second
        // 标签:把命中的占类/类神关键词作为初始标签
        val tags = buildList {
            CATEGORY_KEYWORDS.firstOrNull { segment.contains(it.first) }?.let { add(it.first) }
            TARGET_KEYWORDS.firstOrNull { segment.contains(it.first) }?.let { add(it.first) }
        }
        return DraftRule(
            originalText = segment,
            category = category,
            target = target,
            tagsText = tags.joinToString(", "),
        )
    }

    // ───────── 行类型判断 ─────────

    private fun isHeading(line: String): Boolean = line.trimStart().startsWith("#")

    private fun isListItem(line: String): Boolean {
        val t = line.trimStart()
        return t.startsWith("- ") || t.startsWith("* ") || t.startsWith("• ") ||
            t.startsWith("・") || t.startsWith("· ")
    }

    private val NUMBERED = Regex("""^\s*([0-9]+[.、)]|\([0-9]+\)|[一二三四五六七八九十]+、).*""")
    private fun isNumbered(line: String): Boolean = NUMBERED.matches(line)

    // ───────── 清理标记 ─────────

    private fun cleanMarkdown(line: String): String =
        line.trim().trimStart('#', ' ').replace(Regex("""\*\*|__|`"""), "").trim()

    private fun cleanListMarker(line: String): String =
        line.trimStart().removePrefix("- ").removePrefix("* ")
            .removePrefix("• ").removePrefix("・").removePrefix("· ").trim()
            .let { cleanMarkdown(it) }

    private fun cleanNumber(line: String): String =
        line.trimStart().replaceFirst(Regex("""^([0-9]+[.、)]|\([0-9]+\)|[一二三四五六七八九十]+、)\s*"""), "")
            .let { cleanMarkdown(it) }
}
