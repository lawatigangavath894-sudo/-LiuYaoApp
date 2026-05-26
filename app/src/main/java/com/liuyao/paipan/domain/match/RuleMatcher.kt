package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RuleTarget

/**
 * 断语匹配器(第一版)。
 *
 * 输入:当前 [LiuYaoChart]、当前占类 [DivinationCategory]、规则列表。
 * 输出:[MatchReport](已分三类:支持成/支持不成/中性,各自按分数+权重降序)。
 *
 * 匹配原则(对应需求):
 *  1) 占类必须匹配:rule.category 必须等于入参占类(或规则为 OTHER 通配);
 *  2) 类神必须匹配:规则 target 与该占类用神一致(USE_GOD 视为天然匹配);
 *  3) matchConditions 逐条判断;
 *  4) excludeConditions 命中则排除;
 *  5) 条件越具体分越高(见 MatchScoreCalculator);
 *  6) 权重越高排序越靠前;
 *  7) 支持成/支持不成并存不强行合并(分桶输出);
 *  8) 三类输出。
 *
 * 纯逻辑,无 UI / DB 依赖,独立可测。
 */
object RuleMatcher {

    fun match(
        chart: LiuYaoChart,
        category: DivinationCategory,
        rules: List<DivinationRule>,
        /**
         * 规则可靠度提供器:ruleId → 排序乘子(0..1)。
         * 默认全 1.0(不依赖历史,保持纯函数可测)。
         * 调用方(ViewModel)可注入由 [RuleReliabilityCalculator.sortMultiplier] 算出的值,
         * 使误判多的规则排序靠后(但不删除)。
         */
        reliabilityProvider: (String) -> Double = { 1.0 },
        lock: AnalysisLock? = null,
    ): MatchReport {
        val usefulGod = lock?.primaryUsefulGod?.let { UsefulGod.Kin(it) } ?: UsefulGodResolver.primary(category)
        val evaluator = RuleConditionEvaluator(chart, usefulGod)

        val results = rules
            .filter { categoryMatches(it, category) }       // 原则 1
            .filter { targetMatches(it, category, lock) }          // 原则 2
            .filter { lock == null || withinLockScope(it, lock) }
            .map { rule -> enrich(evaluateRule(rule, evaluator), lock) }
            .filter { it.matched }                           // 仅保留命中(未命中/被排除不进结果)

        // 排序键:基础键 × 可靠度乘子。reliability 低者排序靠后,但仍保留。
        val keyOf: (RuleMatchResult) -> Double = { r ->
            MatchScoreCalculator.sortKey(r).toDouble() * reliabilityProvider(r.rule.id)
        }

        val yes = results.filter { it.bucket == ResultBucket.SUPPORT_YES }.sortedByDescending(keyOf)
        val no = results.filter { it.bucket == ResultBucket.SUPPORT_NO }.sortedByDescending(keyOf)
        val neutral = results.filter { it.bucket == ResultBucket.NEUTRAL }.sortedByDescending(keyOf)

        return MatchReport(supportYes = yes, supportNo = no, neutral = neutral)
    }

    /**
     * 对单条规则求值,产出 [RuleMatchResult](无论是否命中都会构造,便于调试;
     * 是否纳入最终报告由 [match] 过滤)。
     */
    fun evaluateRule(rule: DivinationRule, evaluator: RuleConditionEvaluator): RuleMatchResult {
        // 排除条件:任一命中即整体排除
        val excludedBy = rule.excludeConditions.filter { c ->
            val o = evaluator.evaluate(c)
            o.supported && o.matched
        }

        val matched = mutableListOf<RuleCondition>()
        val failed = mutableListOf<RuleCondition>()
        val relatedLines = linkedSetOf<Int>()

        for (c in rule.matchConditions) {
            val o = evaluator.evaluate(c)
            if (!o.supported) continue // 本版未支持的条件:跳过(不计命中也不计失败)
            if (o.matched) {
                matched += c
                relatedLines += o.lines
            } else {
                failed += c
            }
        }

        // 命中判定:无排除命中,且(有 match 条件时)全部支持的 match 条件均命中
        val supportedCount = matched.size + failed.size
        val isMatched = excludedBy.isEmpty() &&
            supportedCount > 0 &&
            failed.isEmpty()

        val score = if (isMatched) {
            MatchScoreCalculator.score(matched, supportedCount, rule.confidenceWeight)
        } else 0

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

    // ───────── 原则 1 / 2 ─────────

    private fun categoryMatches(rule: DivinationRule, category: DivinationCategory): Boolean =
        rule.category == category || rule.category == DivinationCategory.OTHER

    /**
     * 类神匹配:
     *  - 规则 target = USE_GOD:天然匹配(规则就是针对该占类用神写的);
     *  - 规则 target = 指定六亲:须等于该占类用神六亲;
     *  - 规则 target = 世/应/全局:不依赖具体用神六亲,放行;
     *  - 以世为用的占类:USE_GOD / WORLD / 全局放行,指定六亲则不匹配。
     */
    private fun targetMatches(rule: DivinationRule, category: DivinationCategory): Boolean {
        val t = rule.target
        val godKin = UsefulGodResolver.primaryKin(category) // 以世为用时为 null
        return when (t.type) {
            RuleTarget.Type.USE_GOD -> true
            RuleTarget.Type.WORLD -> true
            RuleTarget.Type.RESPONSE -> true
            RuleTarget.Type.WHOLE_CHART -> true
            RuleTarget.Type.SPECIFIC_POSITION -> true
            RuleTarget.Type.SPECIFIC_KIN -> godKin != null && t.kin == godKin
        }
    }

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

    private fun withinLockScope(rule: DivinationRule, lock: AnalysisLock): Boolean {
        val haystack = listOf(
            rule.originalText,
            rule.plainExplanation,
            rule.conditionText,
            rule.tags.joinToString(" "),
            rule.target.kin?.displayName().orEmpty(),
        ).joinToString(" ")
        if (lock.focusKeywords.any { haystack.contains(it) }) return true
        if (lock.primaryUsefulGod != null && haystack.contains(lock.primaryUsefulGod.displayName())) return true
        if (rule.target.type == RuleTarget.Type.USE_GOD || rule.target.type == RuleTarget.Type.WORLD) return true
        return rule.category == lock.category
    }

    private fun enrich(result: RuleMatchResult, lock: AnalysisLock?): RuleMatchResult {
        if (lock == null) return result
        val text = listOf(result.rule.originalText, result.rule.plainExplanation, result.rule.conditionText).joinToString(" ")
        val layer = classifyLayer(result, lock, text)
        val sourceIds = lock.knowledgeSnippets
            .filter { snippet -> snippet.matchedKeywords.any { text.contains(it) } }
            .map { it.id }
            .take(3)
        return result.copy(
            matchLayer = layer,
            lockReason = when (layer) {
                MatchLayer.MAIN_RESULT -> "命中占类、主变量或主用神，属于主结果判断。"
                MatchLayer.PROCESS -> "命中动爻、世应或关键爻，属于过程条件。"
                MatchLayer.CONDITION -> "命中空亡、月破、日冲、生克合冲等状态条件。"
                MatchLayer.SIDE_REFERENCE -> "与当前锁定结果有关，但不是主判断依据。"
            },
            sourceSnippetIds = sourceIds,
            sourceOriginalText = lock.knowledgeSnippets.firstOrNull { it.id in sourceIds }?.originalText,
            relatedUsefulGod = lock.primaryUsefulGod,
            relatedLineIndex = result.relatedLineIndexes.firstOrNull { it in lock.keyLineIndexes },
        )
    }

    private fun classifyLayer(result: RuleMatchResult, lock: AnalysisLock, text: String): MatchLayer {
        val primaryName = lock.primaryUsefulGod?.displayName()
        val mainHit = lock.focusKeywords.any { text.contains(it) } ||
            (primaryName != null && text.contains(primaryName)) ||
            result.rule.target.kin == lock.primaryUsefulGod
        if (mainHit) return MatchLayer.MAIN_RESULT
        if (result.relatedLineIndexes.any { it in lock.keyLineIndexes } ||
            text.contains("动爻") || text.contains("世") || text.contains("应")
        ) return MatchLayer.PROCESS
        if (listOf("空", "旬空", "月破", "日冲", "合", "冲", "刑", "害", "生", "克").any { text.contains(it) }) {
            return MatchLayer.CONDITION
        }
        return MatchLayer.SIDE_REFERENCE
    }
}

