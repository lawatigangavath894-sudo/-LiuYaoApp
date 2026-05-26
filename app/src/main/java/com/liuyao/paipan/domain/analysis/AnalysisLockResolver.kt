package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.StrengthLevel

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
        val secondary = fallback.secondary.filter { it != primary }.distinct().take(4)
        val usefulLines = chart.lines
            .filter { it.sixKin == primary || it.sixKin in secondary }
            .map { it.index }
            .sorted()
        val moving = chart.movingLines.map { it.index }.sorted()
        val changed = chart.lines.filter { it.changedLine != null }.map { it.index }.sorted()
        val hidden = chart.lines.filter { it.hiddenSpirit != null }.map { it.index }.sorted()
        val flying = chart.lines.filter { it.flyingSpirit != null }.map { it.index }.sorted()
        val voids = chart.lines.filter { it.status.isVoid }.map { it.index }.sorted()
        val monthBroken = chart.lines.filter { it.status.isMonthBroken }.map { it.index }.sorted()
        val dayClashed = chart.lines.filter { it.status.isDayClashed }.map { it.index }.sorted()
        val combined = chart.lines.filter { it.status.isCombined }.map { it.index }.sorted()
        val clashed = chart.lines.filter { it.status.isClashed }.map { it.index }.sorted()
        val strong = chart.lines
            .filter { it.status.strength in listOf(StrengthLevel.PROSPEROUS, StrengthLevel.STRONG) }
            .map { it.index }
            .sorted()
        val weak = chart.lines
            .filter { it.status.strength in listOf(StrengthLevel.TRAPPED, StrengthLevel.DEAD) }
            .map { it.index }
            .sorted()
        val keyLines = buildKeyLines(
            chart = chart,
            usefulLines = usefulLines,
            moving = moving,
            changed = changed,
            hidden = hidden,
            flying = flying,
            voids = voids,
            monthBroken = monthBroken,
            dayClashed = dayClashed,
        )
        val relatedGods = chart.lines
            .filter { it.index in keyLines }
            .map { it.sixGod }
            .distinct()
        val shenSha = relatedShenSha(category, relatedGods)
        val fromKnowledge = usefulFromKnowledge != null
        val warnings = buildWarnings(snippets, primary, keyLines, mainVariable)

        return AnalysisLock(
            chartId = chart.id,
            question = question,
            category = category,
            mainVariable = mainVariable,
            primaryUsefulGod = primary,
            secondaryUsefulGods = secondary,
            worldLineIndex = chart.worldLineIndex,
            responseLineIndex = chart.responseLineIndex,
            usefulGodLineIndexes = usefulLines,
            keyLineIndexes = keyLines,
            movingLineIndexes = moving,
            changedLineIndexes = changed,
            hiddenSpiritLineIndexes = hidden,
            flyingSpiritLineIndexes = flying,
            voidLineIndexes = voids,
            monthBrokenLineIndexes = monthBroken,
            dayClashedLineIndexes = dayClashed,
            combinedLineIndexes = combined,
            clashedLineIndexes = clashed,
            strongLineIndexes = strong,
            weakLineIndexes = weak,
            relatedSixGods = relatedGods,
            relatedShenSha = shenSha,
            focusKeywords = buildFocusKeywords(category, question, mainVariable, primary, secondary, chart, shenSha),
            knowledgeSnippets = snippets.take(10),
            analysisWarnings = warnings,
            analysisDirection = directionText(category, mainVariable, primary),
            lockReason = if (fromKnowledge) {
                "先按占事类别和问题检索本地资料，资料片段中出现用神取法或相关六亲，因此以资料为优先依据；再结合世应、动变、伏飞、空破冲合锁定关键爻。"
            } else {
                "未检索到明确的资料用神取法，当前使用内置基础规则兜底；所有分析会标记为基础锁定，建议导入刘昌明资料后复核。"
            },
            uncertainReason = warnings.firstOrNull(),
            excludedScopes = listOf("无关占类", "全量神煞大全", "全库泛匹配断语", "未命中的资料片段"),
            usedFallback = !fromKnowledge,
            analysisScope = if (snippets.isEmpty() || primary == null) AnalysisScope.INSUFFICIENT_DATA else AnalysisScope.MAIN_RESULT,
        )
    }

    private data class UsefulFallback(val primary: SixKin?, val secondary: List<SixKin>)

    private fun fallbackUsefulGod(category: DivinationCategory, question: String): UsefulFallback = when (category) {
        DivinationCategory.MARRIAGE -> when {
            listOf("男问", "女友", "妻", "女方").any { question.contains(it) } ->
                UsefulFallback(SixKin.WEALTH, listOf(SixKin.OFFICIAL))
            listOf("女问", "男友", "夫", "男方").any { question.contains(it) } ->
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
        if (!listOf("用神", "取用", "为用", "为主").any { text.contains(it) }) return null
        val windows = Regex(".{0,16}(父母|兄弟|官鬼|妻财|子孙).{0,16}(用神|取用|为用|为主)")
            .findAll(text)
            .map { it.value }
            .joinToString("\n")
            .ifBlank { text }
        return SixKin.entries.firstOrNull { windows.contains(it.displayName()) }
    }

    private fun buildKeyLines(
        chart: LiuYaoChart,
        usefulLines: List<Int>,
        moving: List<Int>,
        changed: List<Int>,
        hidden: List<Int>,
        flying: List<Int>,
        voids: List<Int>,
        monthBroken: List<Int>,
        dayClashed: List<Int>,
    ): List<Int> = buildList {
        add(chart.worldLineIndex)
        add(chart.responseLineIndex)
        addAll(usefulLines)
        addAll(moving)
        addAll(changed)
        addAll(hidden)
        addAll(flying)
        addAll(voids.filter { it in usefulLines || it in moving })
        addAll(monthBroken.filter { it in usefulLines || it in moving })
        addAll(dayClashed.filter { it in usefulLines || it in moving })
    }.filter { it in 1..6 }.distinct().sorted()

    private fun relatedShenSha(category: DivinationCategory, gods: List<SixGod>): List<String> {
        val preferred = when (category) {
            DivinationCategory.HEALTH, DivinationCategory.PREGNANCY -> listOf(SixGod.WHITE_TIGER, SixGod.BLACK_TORTOISE)
            DivinationCategory.STUDY, DivinationCategory.FAME -> listOf(SixGod.VERMILION_BIRD, SixGod.AZURE_DRAGON)
            DivinationCategory.WEALTH -> listOf(SixGod.AZURE_DRAGON, SixGod.HOOK_EARTH)
            DivinationCategory.LOST -> listOf(SixGod.BLACK_TORTOISE, SixGod.HOOK_EARTH)
            DivinationCategory.LAWSUIT -> listOf(SixGod.WHITE_TIGER, SixGod.VERMILION_BIRD)
            else -> gods
        }
        return (preferred + gods).distinct().map { it.displayName() }
    }

    private fun buildWarnings(
        snippets: List<KnowledgeSnippet>,
        primary: SixKin?,
        keyLines: List<Int>,
        mainVariable: String,
    ): List<String> = buildList {
        if (snippets.isEmpty()) add("未检索到本地刘昌明资料片段。")
        if (primary == null) add("用神无法从资料或基础规则中明确锁定。")
        if (keyLines.isEmpty()) add("当前排盘未找到可用于重点分析的关键爻。")
        if (mainVariable == QuestionFocusResolver.UNCLEAR) add("占事问题不够明确，建议补充成败、吉凶、去留、得失、安危或应期。")
    }

    private fun buildFocusKeywords(
        category: DivinationCategory,
        question: String,
        mainVariable: String,
        primary: SixKin?,
        secondary: List<SixKin>,
        chart: LiuYaoChart,
        shenSha: List<String>,
    ): List<String> = buildList {
        add(category.displayName())
        addAll(categoryAliases(category))
        addAll(QuestionFocusResolver.resultKeywords(mainVariable))
        addAll(question.split(Regex("""[\s，。！？、；：,.!?]+""")).filter { it.length >= 2 })
        primary?.let { add(it.displayName()) }
        secondary.forEach { add(it.displayName()) }
        addAll(listOf("世爻", "应爻", "动爻", "变爻", "伏神", "飞神", "用神", "旬空", "空亡", "月破", "日冲", "合", "冲", "刑", "害"))
        addAll(shenSha)
        chart.originalHexagram.name.takeIf { it.isNotBlank() }?.let { add(it) }
        chart.changedHexagram?.name?.takeIf { it.isNotBlank() }?.let { add(it) }
    }.map { it.trim() }.filter { it.isNotBlank() }.distinct()

    private fun categoryAliases(category: DivinationCategory): List<String> = when (category) {
        DivinationCategory.MARRIAGE -> listOf("婚姻", "感情", "官鬼", "妻财", "世应", "六合", "冲克")
        DivinationCategory.WEALTH -> listOf("财运", "求财", "妻财", "子孙", "兄弟", "劫财")
        DivinationCategory.STUDY, DivinationCategory.FAME -> listOf("考试", "学业", "父母", "官鬼", "录取", "成绩", "文书")
        DivinationCategory.CAREER -> listOf("工作", "求职", "官鬼", "父母", "入职", "offer", "单位")
        DivinationCategory.HEALTH -> listOf("疾病", "健康", "官鬼", "子孙", "白虎", "玄武", "世爻")
        DivinationCategory.LOST -> listOf("失物", "寻物", "妻财", "父母", "子孙", "伏神", "飞神", "空亡")
        DivinationCategory.TRAVEL -> listOf("行人", "出行", "应爻", "父母", "消息", "驿马", "冲合")
        DivinationCategory.LAWSUIT -> listOf("官事", "官司", "诉讼", "官鬼", "父母", "证据", "世应")
        DivinationCategory.PREGNANCY -> listOf("孕产", "怀孕", "子孙", "父母", "白虎", "胎")
        DivinationCategory.HOUSE -> listOf("房宅", "父母", "妻财", "官鬼", "交易", "隐患")
        DivinationCategory.COOPERATION -> listOf("合作", "应爻", "妻财", "兄弟", "官鬼", "分利")
        DivinationCategory.FORTUNE -> listOf("运势", "世爻", "用神", "吉凶")
        DivinationCategory.OTHER -> listOf("用神", "世爻", "应爻")
    }

    private fun directionText(category: DivinationCategory, mainVariable: String, primary: SixKin?): String =
        "围绕“${category.displayName()} / $mainVariable”判断，以${primary?.displayName() ?: "世应与关键爻"}为主，结合世应、动变、旺衰、空破冲合、神煞和资料片段分层输出。"
}
