package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.match.UsefulGod
import com.liuyao.paipan.domain.match.UsefulGodResolver
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixKin

object AnalysisLockResolver {
    fun resolve(
        chart: LiuYaoChart,
        category: DivinationCategory,
        snippets: List<KnowledgeSnippet>,
    ): AnalysisLock {
        val question = chart.question.ifBlank { "未命名占事" }
        val mainVariable = QuestionFocusResolver.resolve(question)
        val usefulFromKnowledge = inferUsefulGod(snippets)
        val fallback = UsefulGodResolver.primary(category)
        val primary = usefulFromKnowledge ?: (fallback as? UsefulGod.Kin)?.kin
        val secondary = UsefulGodResolver.candidates(category)
            .filter { it != primary }
            .take(2)
        val keyLines = buildKeyLines(chart, primary)
        val fromKnowledge = usefulFromKnowledge != null
        val focusKeywords = buildList {
            add(category.displayName())
            addAll(QuestionFocusResolver.resultKeywords(mainVariable))
            primary?.let { add(it.displayName()) }
            secondary.forEach { add(it.displayName()) }
            add("世爻")
            add("应爻")
            if (chart.movingLines.isNotEmpty()) add("动爻")
        }.distinct()

        return AnalysisLock(
            chartId = chart.id,
            question = question,
            category = category,
            mainVariable = mainVariable,
            primaryUsefulGod = primary,
            secondaryUsefulGods = secondary,
            worldLineIndex = chart.worldLineIndex,
            responseLineIndex = chart.responseLineIndex,
            keyLineIndexes = keyLines,
            focusKeywords = focusKeywords,
            knowledgeSnippets = snippets.take(10),
            lockReason = if (fromKnowledge) {
                "依据刘昌明资料片段中出现的占类、用神与术语锁定；再结合当前排盘的世应、动爻和相关六亲定位关键爻。"
            } else {
                "资料中未检索到明确用神取法，当前使用内置兜底规则，并仅作为临时锁定。"
            },
            uncertainReason = when {
                snippets.isEmpty() -> "未检索到刘昌明资料片段。"
                !fromKnowledge -> "资料中未出现明确的“以某六亲为用神”类规则。"
                primary == null -> "用神无法从资料或兜底规则中锁定。"
                else -> null
            },
            analysisScope = if (snippets.isEmpty() || primary == null) AnalysisScope.INSUFFICIENT_DATA else AnalysisScope.MAIN_RESULT,
        )
    }

    private fun inferUsefulGod(snippets: List<KnowledgeSnippet>): SixKin? {
        val text = snippets.take(5).joinToString("\n") { it.originalText }
        val candidates = SixKin.entries.map { kin -> kin to kin.displayName() }
        val explicit = Regex("""(以|取|用|看).{0,8}(父母|兄弟|官鬼|妻财|子孙).{0,8}(为|作)?.{0,4}(用神|用)""")
            .find(text)
            ?.value
        return candidates.firstOrNull { (_, name) -> explicit?.contains(name) == true }?.first
            ?: candidates.firstOrNull { (_, name) -> text.contains(name) && text.contains("用神") }?.first
    }

    private fun buildKeyLines(chart: LiuYaoChart, primary: SixKin?): List<Int> = buildList {
        add(chart.worldLineIndex)
        add(chart.responseLineIndex)
        chart.movingLines.forEach { add(it.index) }
        if (primary != null) {
            chart.lines.filter { it.sixKin == primary }.forEach { add(it.index) }
        }
    }.distinct().sorted()
}
