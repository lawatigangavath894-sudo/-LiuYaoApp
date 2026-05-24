package com.liuyao.paipan.data.backup

import com.liuyao.paipan.data.db.LiuYaoRepository

/** 导出预览:内容文本 + 摘要 */
data class ExportPreview(
    val content: String,
    val itemCount: Int,
    val suggestedFileName: String,
) {
    val byteSize: Int get() = content.toByteArray(Charsets.UTF_8).size
}

/**
 * 导出管理:从 [LiuYaoRepository] 取数据,生成可导出的文本(JSON / Markdown)。
 * 不负责写文件(交由 UI 通过 SAF 写出),只产出内容与预览,便于先预览再保存。
 */
class ExportManager(private val repo: LiuYaoRepository) {

    /** 断语库 → JSON */
    suspend fun exportRulesJson(): ExportPreview {
        val rules = repo.loadAllRules()
        val json = JsonSchema.rulesToJson(rules)
        return ExportPreview(json, rules.size, "rules-${stamp()}.json")
    }

    /** 案例库 → JSON(含反馈) */
    suspend fun exportCasesJson(): ExportPreview {
        val cases = repo.allCases()
        val withFb = cases.map { c -> c to repo.feedbackOf(c.id) }
        val json = JsonSchema.casesToJson(withFb)
        return ExportPreview(json, cases.size, "cases-${stamp()}.json")
    }

    /** 单个案例 → Markdown */
    suspend fun exportCaseMarkdown(caseId: String): ExportPreview? {
        val case = repo.caseById(caseId) ?: return null
        val fb = repo.feedbackOf(caseId)
        val chart = repo.loadChartEntity(case.chartId)
        val lines = if (chart != null) repo.loadChartLines(case.chartId) else emptyList()
        val hitTexts = case.hitRuleIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .mapNotNull { repo.loadRule(it)?.originalText }
        val md = MarkdownExporter.caseToMarkdown(case, fb, chart, lines, hitTexts)
        return ExportPreview(md, 1, "case-${case.id}.md")
    }

    /** 排盘 → Markdown */
    suspend fun exportChartMarkdown(chartId: String): ExportPreview? {
        val chart = repo.loadChartEntity(chartId) ?: return null
        val lines = repo.loadChartLines(chartId)
        val md = MarkdownExporter.chartToMarkdown(chart, lines)
        return ExportPreview(md, 1, "chart-$chartId.md")
    }

    private fun stamp(): String {
        val t = java.time.LocalDateTime.now()
        return "%04d%02d%02d".format(t.year, t.monthValue, t.dayOfMonth)
    }
}
