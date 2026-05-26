package com.liuyao.paipan.domain.analysis

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
        val fallback = fallbackUsefulGod(category, question)
        val primary = usefulFromKnowledge ?: fallback.primary
        val secondary = fallback.secondary
            .filter { it != primary }
            .distinct()
            .take(3)
        val moving = chart.movingLines.map { it.index }.sorted()
        val hidden = chart.lines.filter { it.hiddenSpirit != null }.map { it.index }.sorted()
        val flying = chart.lines.filter { it.flyingSpirit != null }.map { it.index }.sorted()
        val keyLines = buildKeyLines(chart, primary, secondary, moving, hidden, flying)
        val fromKnowledge = usefulFromKnowledge != null
        val focusKeywords = buildFocusKeywords(category, mainVariable, primary, secondary, chart)
        val usedFallback = !fromKnowledge

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
            movingLineIndexes = moving,
            hiddenSpiritLineIndexes = hidden,
            flyingSpiritLineIndexes = flying,
            focusKeywords = focusKeywords,
            knowledgeSnippets = snippets.take(10),
            analysisDirection = directionText(category, mainVariable, primary),
            lockReason = if (fromKnowledge) {
                "依据本地刘昌明资料片段中出现的占类、用神与术语锁定；再结合当前排盘的世应、动爻、伏神/飞神和相关六亲定位关键爻。"
            } else {
                "未检索到明确资料规则，当前使用内置兜底取用规则；该结果只作为基础锁定，建议导入资料后复核。"
            },
            uncertainReason = when {
                snippets.isEmpty() -> "未检索到刘昌明资料片段。"
                !fromKnowledge -> "资料中未出现明确的“以某六亲为用神”规则。"
                primary == null -> "用神无法从资料或兜底规则中锁定。"
                keyLines.isEmpty() -> "当前排盘中未找到可用关键爻。"
                else -> null
            },
            excludedScopes = listOf("无关占类", "全量神煞大全", "全库泛匹配断语", "未命中的资料片段"),
            usedFallback = usedFallback,
            analysisScope = if (snippets.isEmpty() || primary == null) AnalysisScope.INSUFFICIENT_DATA else AnalysisScope.MAIN_RESULT,
        )
    }

    private data class UsefulFallback(val primary: SixKin?, val secondary: List<SixKin>)

    private fun fallbackUsefulGod(category: DivinationCategory, question: String): UsefulFallback = when (category) {
        DivinationCategory.MARRIAGE -> when {
            question.contains("男问") || question.contains("女友") || question.contains("妻") || question.contains("女方") ->
                UsefulFallback(SixKin.WEALTH, listOf(SixKin.OFFICIAL))
            question.contains("女问") || question.contains("男友") || question.contains("夫") || question.contains("男方") ->
                UsefulFallback(SixKin.OFFICIAL, listOf(SixKin.WEALTH))
            else -> UsefulFallback(SixKin.OFFICIAL, listOf(SixKin.WEALTH))
        }
        DivinationCategory.WEALTH -> UsefulFallback(SixKin.WEALTH, listOf(SixKin.OFFSPRING, SixKin.SIBLING, SixKin.OFFICIAL))
        DivinationCategory.STUDY, DivinationCategory.FAME -> UsefulFallback(SixKin.PARENT, listOf(SixKin.OFFICIAL, SixKin.OFFSPRING))
        DivinationCategory.CAREER -> UsefulFallback(SixKin.OFFICIAL, listOf(SixKin.PARENT))
        DivinationCategory.LAWSUIT -> UsefulFallback(SixKin.OFFICIAL, listOf(SixKin.PARENT, SixKin.SIBLING))
        DivinationCategory.HEALTH -> UsefulFallback(SixKin.OFFICIAL, listOf(SixKin.OFFSPRING, SixKin.PARENT))
        DivinationCategory.LOST -> when {
            listOf("证", "文书", "合同", "票", "卡").any { question.contains(it) } ->
                UsefulFallback(SixKin.PARENT, listOf(SixKin.WEALTH))
            listOf("宠物", "孩子", "子女").any { question.contains(it) } ->
                UsefulFallback(SixKin.OFFSPRING, listOf(SixKin.WEALTH))
            else -> UsefulFallback(SixKin.WEALTH, listOf(SixKin.PARENT, SixKin.OFFSPRING))
        }
        DivinationCategory.TRAVEL -> UsefulFallback(null, listOf(SixKin.PARENT, SixKin.OFFICIAL))
        DivinationCategory.PREGNANCY -> UsefulFallback(SixKin.OFFSPRING, listOf(SixKin.PARENT, SixKin.OFFICIAL))
        DivinationCategory.HOUSE -> UsefulFallback(SixKin.PARENT, listOf(SixKin.WEALTH, SixKin.OFFICIAL))
        DivinationCategory.COOPERATION -> UsefulFallback(SixKin.WEALTH, listOf(SixKin.OFFICIAL, SixKin.SIBLING))
        DivinationCategory.FORTUNE, DivinationCategory.OTHER -> UsefulFallback(null, emptyList())
    }

    private fun inferUsefulGod(snippets: List<KnowledgeSnippet>): SixKin? {
        val text = snippets.take(10).joinToString("\n") { it.originalText }
        if (!text.contains("用神") && !text.contains("取用")) return null
        val explicit = Regex("(以|取|看).{0,8}(父母|兄弟|官鬼|妻财|子孙).{0,8}(为|作)?用神")
            .find(text)
            ?.value
        val target = explicit ?: text
        return SixKin.entries.firstOrNull { target.contains(it.displayName()) }
    }

    private fun buildKeyLines(
        chart: LiuYaoChart,
        primary: SixKin?,
        secondary: List<SixKin>,
        moving: List<Int>,
        hidden: List<Int>,
        flying: List<Int>,
    ): List<Int> = buildList {
        add(chart.worldLineIndex)
        add(chart.responseLineIndex)
        addAll(moving)
        if (primary != null) addAll(chart.lines.filter { it.sixKin == primary }.map { it.index })
        secondary.forEach { kin -> addAll(chart.lines.filter { it.sixKin == kin }.map { it.index }) }
        addAll(hidden)
        addAll(flying)
    }.filter { it in 1..6 }.distinct().sorted()

    private fun buildFocusKeywords(
        category: DivinationCategory,
        mainVariable: String,
        primary: SixKin?,
        secondary: List<SixKin>,
        chart: LiuYaoChart,
    ): List<String> = buildList {
        add(category.displayName())
        addAll(QuestionFocusResolver.resultKeywords(mainVariable))
        primary?.let { add(it.displayName()) }
        secondary.forEach { add(it.displayName()) }
        addAll(listOf("世爻", "应爻", "动爻", "伏神", "飞神", "用神"))
        chart.originalHexagram.name.takeIf { it.isNotBlank() }?.let { add(it) }
        chart.changedHexagram?.name?.takeIf { it.isNotBlank() }?.let { add(it) }
    }.distinct()

    private fun directionText(category: DivinationCategory, mainVariable: String, primary: SixKin?): String =
        "围绕“${category.displayName()} / $mainVariable”判断，以${primary?.displayName() ?: "世应与关键爻"}为主，结合世应、动爻、旺衰、空破冲合和资料片段分层输出。"
}
