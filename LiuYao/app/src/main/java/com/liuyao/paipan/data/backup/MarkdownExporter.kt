package com.liuyao.paipan.data.backup

import com.liuyao.paipan.domain.model.yaoPositionName
import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.data.db.entity.ChartEntity
import com.liuyao.paipan.data.db.entity.ChartLineEntity
import com.liuyao.paipan.domain.model.CaseVerdict
import com.liuyao.paipan.domain.model.DivinationCategory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Markdown 导出。把案例 / 排盘转成可读的 .md 文本,便于分享与归档。
 * 纯函数,独立可测。
 */
object MarkdownExporter {

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    private fun time(epoch: Long): String =
        if (epoch <= 0) "—" else runCatching { fmt.format(Instant.ofEpochSecond(epoch)) }.getOrDefault("—")

    /** 单个案例 → Markdown */
    fun caseToMarkdown(
        case: CaseEntity,
        feedback: CaseFeedbackEntity?,
        chart: ChartEntity?,
        chartLines: List<ChartLineEntity> = emptyList(),
        hitRuleTexts: List<String> = emptyList(),
    ): String = buildString {
        appendLine("# ${case.title}")
        appendLine()
        appendLine("## 占事")
        appendLine("- 标题:${case.title}")
        case.category?.let { appendLine("- 占类:${categoryCn(it)}") }
        if (case.question.isNotBlank()) appendLine("- 占事:${case.question}")
        appendLine("- 起卦时间:${time(case.castEpoch)}")
        appendLine("- 保存时间:${time(case.createdEpoch)}")
        appendLine()

        if (chart != null) {
            appendLine("## 原盘")
            appendLine(chartToMarkdown(chart, chartLines, withHeader = false))
            appendLine()
        }

        if (hitRuleTexts.isNotEmpty()) {
            appendLine("## 当时命中的断语")
            hitRuleTexts.forEach { appendLine("- $it") }
            appendLine()
        }

        if (feedback != null) {
            appendLine("## 反馈")
            appendLine("- 结果类型:${CaseVerdict.fromName(feedback.verdict).cn}")
            appendLine("- 最终结果:${feedback.actualResult.ifBlank { "—" }}")
            appendLine("- 反馈时间:${time(feedback.feedbackEpoch)}")
            if (feedback.note.isNotBlank()) appendLine("- 备注:${feedback.note}")
            if (feedback.hitRuleIdsCsv.isNotBlank()) appendLine("- 验中规则:${feedback.hitRuleIdsCsv}")
            if (feedback.missRuleIdsCsv.isNotBlank()) appendLine("- 误判规则:${feedback.missRuleIdsCsv}")
        }
    }

    /** 排盘 → Markdown */
    fun chartToMarkdown(
        chart: ChartEntity,
        lines: List<ChartLineEntity>,
        withHeader: Boolean = true,
    ): String = buildString {
        if (withHeader) {
            appendLine("# ${chart.question.ifBlank { "排盘" }}")
            appendLine()
        }
        appendLine("- 本卦:${chart.originalHexName}")
        appendLine("- 变卦:${if (chart.changedLower != null) "有变卦" else "静卦"}")
        appendLine("- 卦宫:${chart.palace}")
        appendLine("- 四柱:${chart.yearGanZhi} ${chart.monthGanZhi} ${chart.dayGanZhi} ${chart.hourGanZhi}")
        appendLine("- 旬空:${chart.xunKong}")
        appendLine("- 世应:世${chart.worldLineIndex}爻 / 应${chart.responseLineIndex}爻")
        if (lines.isNotEmpty()) {
            appendLine()
            appendLine("### 六爻(上 → 初)")
            lines.sortedByDescending { it.position }.forEach { l ->
                val marks = buildList {
                    if (l.isWorld) add("世")
                    if (l.isResponse) add("应")
                    if (l.isMoving) add("动")
                }.joinToString("")
                appendLine("- ${yaoPositionName(l.position)}爻 ${l.sixGod} ${l.sixKin} ${l.naJiaBranch}${if (marks.isNotEmpty()) " [$marks]" else ""}")
            }
        }
    }

    private fun categoryCn(name: String): String =
        runCatching { DivinationCategory.valueOf(name).cn }.getOrDefault(name)
}
