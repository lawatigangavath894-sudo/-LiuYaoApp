package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RuleTarget

object RuleMatcher {
    fun match(
        chart: LiuYaoChart,
        category: DivinationCategory,
        rules: List<DivinationRule>,
        reliabilityProvider: (String) -> Double = { 1.0 },
        lock: AnalysisLock? = null,
    ): MatchReport {
        val usefulGod = lock?.primaryUsefulGod?.let { UsefulGod.Kin(it) } ?: UsefulGodResolver.primary(category)
        val evaluator = RuleConditionEvaluator(chart, usefulGod)
        val scoped = rules
            .filter { categoryMatches(it, category) }
            .filter { targetMatches(it, category, lock) }
            .map { rule -> enrich(evaluateRule(rule, evaluator), lock, chart) }
            .filter { it.matched && it.excludeReason == null }
            .filter { it.score >= (it.rule.matchHint?.minScoreToShow ?: 0) }

        val keyOf: (RuleMatchResult) -> Double = { r ->
            (r.score * 1000 + layerWeight(r.matchLayer)).toDouble() * reliabilityProvider(r.rule.id)
        }

        val ordered = scoped.sortedByDescending(keyOf).take(60)
        return MatchReport(
            supportYes = ordered.filter { it.bucket == ResultBucket.SUPPORT_YES },
            supportNo = ordered.filter { it.bucket == ResultBucket.SUPPORT_NO },
            neutral = ordered.filter { it.bucket == ResultBucket.NEUTRAL },
        )
    }

    fun evaluateRule(rule: DivinationRule, evaluator: RuleConditionEvaluator): RuleMatchResult {
        val excludedBy = rule.excludeConditions.filter { c ->
            val o = evaluator.evaluate(c)
            o.supported && o.matched
        }
        val matched = mutableListOf<RuleCondition>()
        val failed = mutableListOf<RuleCondition>()
        val relatedLines = linkedSetOf<Int>()

        for (condition in rule.matchConditions) {
            val outcome = evaluator.evaluate(condition)
            if (!outcome.supported) continue
            if (outcome.matched) {
                matched += condition
                relatedLines += outcome.lines
            } else {
                failed += condition
            }
        }

        val supportedCount = matched.size + failed.size
        val requireAll = rule.matchHint?.requireAllMatch ?: true
        val isMatched = excludedBy.isEmpty() &&
            supportedCount > 0 &&
            if (requireAll) failed.isEmpty() else matched.isNotEmpty()

        val score = if (isMatched) {
            MatchScoreCalculator.score(matched, supportedCount, rule.confidenceWeight)
        } else {
            0
        }
        val explanation = MatchExplanationBuilder.build(
            rule = rule,
            matched = isMatched,
            matchedConditions = matched,
            failedConditions = failed,
            excluded = excludedBy,
        )

        return RuleMatchResult(
            rule = rule,
            matched = isMatched,
            score = score,
            polarity = rule.polarity,
            matchedConditions = matched,
            failedConditions = failed,
            excludedByConditions = excludedBy,
            explanation = explanation,
            relatedLineIndexes = relatedLines.sorted(),
        )
    }

    private fun categoryMatches(rule: DivinationRule, category: DivinationCategory): Boolean =
        rule.category == category || rule.category == DivinationCategory.OTHER

    private fun targetMatches(rule: DivinationRule, category: DivinationCategory, lock: AnalysisLock?): Boolean {
        if (lock == null) return targetMatches(rule, category)
        val target = rule.target
        val primary = lock.primaryUsefulGod
        val secondary = lock.secondaryUsefulGods.toSet()
        return when (target.type) {
            RuleTarget.Type.USE_GOD,
            RuleTarget.Type.WORLD,
            RuleTarget.Type.RESPONSE,
            RuleTarget.Type.WHOLE_CHART,
            -> true
            RuleTarget.Type.SPECIFIC_POSITION -> target.position in lock.keyLineIndexes
            RuleTarget.Type.SPECIFIC_KIN -> target.kin == primary || target.kin in secondary
        }
    }

    private fun targetMatches(rule: DivinationRule, category: DivinationCategory): Boolean {
        val godKin = UsefulGodResolver.primaryKin(category)
        return when (rule.target.type) {
            RuleTarget.Type.USE_GOD,
            RuleTarget.Type.WORLD,
            RuleTarget.Type.RESPONSE,
            RuleTarget.Type.WHOLE_CHART,
            RuleTarget.Type.SPECIFIC_POSITION,
            -> true
            RuleTarget.Type.SPECIFIC_KIN -> godKin != null && rule.target.kin == godKin
        }
    }

    private fun enrich(result: RuleMatchResult, lock: AnalysisLock?, chart: LiuYaoChart): RuleMatchResult {
        if (lock == null || !result.matched) return result
        val text = listOf(
            result.rule.originalText,
            result.rule.plainExplanation,
            result.rule.conditionText,
            result.rule.tags.joinToString(" "),
            result.rule.target.kin?.displayName().orEmpty(),
        ).joinToString(" ")
        val scoring = scoreAgainstLock(result, lock, text)
        val layer = classifyLayer(result, lock, text, scoring)
        val sourceIds = lock.knowledgeSnippets
            .filter { snippet -> snippet.matchedKeywords.any { text.contains(it) } }
            .map { it.id }
            .take(3)
        val relatedLine = (result.relatedLineIndexes + targetLine(result, lock)).firstOrNull { it in lock.keyLineIndexes }
        return result.copy(
            score = (result.score + scoring.bonus).coerceIn(0, 100),
            matchLayer = layer,
            lockReason = lockReason(layer, scoring),
            sourceSnippetIds = sourceIds,
            sourceOriginalText = lock.knowledgeSnippets.firstOrNull { it.id in sourceIds }?.originalText,
            relatedUsefulGod = lock.primaryUsefulGod,
            relatedLineIndex = relatedLine,
            relatedLineIndexes = (result.relatedLineIndexes + targetLine(result, lock)).filterNotNull().distinct().sorted(),
            relevanceReason = scoring.reasons.joinToString("；"),
            conflictReason = conflictReason(result, chart, lock),
            confidenceLevel = confidenceLevel(result.score + scoring.bonus),
        )
    }

    private data class LockScore(val bonus: Int, val reasons: List<String>)

    private fun scoreAgainstLock(result: RuleMatchResult, lock: AnalysisLock, text: String): LockScore {
        var bonus = 0
        val reasons = mutableListOf<String>()
        if (result.rule.category == lock.category) {
            bonus += 12
            reasons += "占类匹配"
        }
        if (QuestionFocusResolverCompat.anyKeyword(text, lock.focusKeywords)) {
            bonus += 14
            reasons += "命中占事关键词"
        }
        lock.primaryUsefulGod?.let {
            if (text.contains(it.displayName()) || result.rule.target.kin == it) {
                bonus += 16
                reasons += "命中主用神"
            }
        }
        if (result.relatedLineIndexes.any { it in lock.keyLineIndexes }) {
            bonus += 12
            reasons += "命中关键爻"
        }
        if (result.relatedLineIndexes.any { it == lock.worldLineIndex || it == lock.responseLineIndex }) {
            bonus += 8
            reasons += "命中世应"
        }
        if (result.relatedLineIndexes.any { it in lock.movingLineIndexes || it in lock.changedLineIndexes }) {
            bonus += 8
            reasons += "命中动变"
        }
        if (listOf("空", "旬空", "月破", "日冲", "合", "冲", "刑", "害", "旺", "衰").any { text.contains(it) }) {
            bonus += 8
            reasons += "命中状态条件"
        }
        if (lock.relatedShenSha.any { text.contains(it) }) {
            bonus += 6
            reasons += "命中相关神煞"
        }
        if (lock.knowledgeSnippets.any { snippet -> snippet.matchedKeywords.any { text.contains(it) } }) {
            bonus += 10
            reasons += "命中资料片段关键词"
        }
        return LockScore(bonus.coerceAtMost(55), reasons.ifEmpty { listOf("仅与当前占类弱相关") })
    }

    private fun classifyLayer(result: RuleMatchResult, lock: AnalysisLock, text: String, scoring: LockScore): MatchLayer {
        if (lock.analysisScope == com.liuyao.paipan.domain.analysis.AnalysisScope.INSUFFICIENT_DATA && scoring.bonus < 20) {
            return MatchLayer.INSUFFICIENT_DATA
        }
        val riskWords = listOf("凶", "病", "险", "破", "空", "伤", "灾", "官非", "白虎", "玄武", "不成", "不过", "不录取")
        if (riskWords.any { text.contains(it) } && scoring.bonus >= 20) return MatchLayer.RISK_WARNING
        val mainHit = scoring.reasons.any { it in listOf("占类匹配", "命中占事关键词", "命中主用神") } &&
            scoring.bonus >= 28
        if (mainHit) return MatchLayer.MAIN_RESULT
        if (result.relatedLineIndexes.any { it in lock.movingLineIndexes || it in lock.changedLineIndexes } ||
            listOf("动爻", "变爻", "世", "应").any { text.contains(it) }
        ) return MatchLayer.PROCESS
        if (listOf("空", "旬空", "月破", "日冲", "合", "冲", "刑", "害", "生", "克", "旺", "衰").any { text.contains(it) }) {
            return MatchLayer.CONDITION
        }
        return MatchLayer.SIDE_REFERENCE
    }

    private fun targetLine(result: RuleMatchResult, lock: AnalysisLock): Int? = when (result.rule.target.type) {
        RuleTarget.Type.WORLD -> lock.worldLineIndex
        RuleTarget.Type.RESPONSE -> lock.responseLineIndex
        RuleTarget.Type.SPECIFIC_POSITION -> result.rule.target.position
        RuleTarget.Type.USE_GOD, RuleTarget.Type.SPECIFIC_KIN -> lock.usefulGodLineIndexes.firstOrNull()
        RuleTarget.Type.WHOLE_CHART -> null
    }

    private fun lockReason(layer: MatchLayer, scoring: LockScore): String = when (layer) {
        MatchLayer.MAIN_RESULT -> "命中占类、主变量、主用神或关键爻，纳入主结果判断。"
        MatchLayer.PROCESS -> "命中世应、动爻、变爻或关键爻，纳入过程条件。"
        MatchLayer.CONDITION -> "命中空亡、月破、日冲、生克合冲刑害或旺衰条件。"
        MatchLayer.RISK_WARNING -> "命中当前占事相关风险词或不利状态，作为风险提示。"
        MatchLayer.SIDE_REFERENCE -> "与当前锁定结果有关，但不作为主判断。"
        MatchLayer.INSUFFICIENT_DATA -> "当前资料不足，只作为待复核参考。"
    } + " 依据：${scoring.reasons.joinToString("、")}"

    private fun conflictReason(result: RuleMatchResult, chart: LiuYaoChart, lock: AnalysisLock): String? {
        if (result.bucket != ResultBucket.NEUTRAL) return null
        if (chart.isSixClash && chart.isSixCombine) return "卦象同时出现冲合信息，需结合关键爻轻重。"
        return if (lock.analysisWarnings.isNotEmpty()) "资料或锁定条件不足，结论置信度需下调。" else null
    }

    private fun confidenceLevel(score: Int): String = when {
        score >= 80 -> "高"
        score >= 55 -> "中"
        else -> "低"
    }

    private fun layerWeight(layer: MatchLayer): Int = when (layer) {
        MatchLayer.MAIN_RESULT -> 500
        MatchLayer.RISK_WARNING -> 420
        MatchLayer.PROCESS -> 350
        MatchLayer.CONDITION -> 280
        MatchLayer.SIDE_REFERENCE -> 120
        MatchLayer.INSUFFICIENT_DATA -> 20
    }
}

private object QuestionFocusResolverCompat {
    fun anyKeyword(text: String, keywords: List<String>): Boolean =
        keywords.any { it.isNotBlank() && text.contains(it) }
}
