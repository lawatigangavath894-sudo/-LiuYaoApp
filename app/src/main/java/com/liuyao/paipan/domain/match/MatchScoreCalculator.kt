package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition

/**
 * 匹配打分。
 *
 * 设计:
 *  - 每个命中条件给"具体度分":越具体(限定到具体六亲/具体地支/落在具体目标)的条件分越高;
 *  - 命中条件的具体度之和 → 归一到 0..100 的基础分;
 *  - 再乘以规则的 [DivinationRule.confidenceWeight](0..1)得最终分;
 *  - 排序键:最终分为主;权重作为次级,保证"权重越高越靠前"(原则 6)。
 *
 * 纯函数,独立可测。
 */
object MatchScoreCalculator {

    /** 单条件的"具体度"权重 */
    fun specificity(c: RuleCondition): Int = when (c) {
        // 明确限定到具体六亲/具体地支,信息量大
        is RuleCondition.KinPresent -> 3
        is RuleCondition.KinAbsent -> 3
        is RuleCondition.SixGodPresent -> if (c.onTarget != null) 4 else 2
        is RuleCondition.BranchClash -> if (c.a != null && c.b != null) 4 else 2
        is RuleCondition.BranchCombine -> if (c.a != null && c.b != null) 4 else 2
        // 用神相关:中等具体
        RuleCondition.UseGodMoving,
        RuleCondition.UseGodVoid,
        RuleCondition.UseGodMonthBroken,
        RuleCondition.UseGodDayClashed,
        RuleCondition.UseGodSupportedByMonth,
        RuleCondition.UseGodSupportedByDay,
        -> 3
        // 世应相关
        RuleCondition.WorldVoid,
        RuleCondition.ResponseVoid,
        RuleCondition.WorldResponseGenerate,
        RuleCondition.WorldResponseRestrain,
        -> 2
        // 回头生克:依赖动爻与变爻,较具体
        RuleCondition.BackGenerate,
        RuleCondition.BackRestrain,
        -> 3
        // 本版未支持
        else -> 0
    }

    /**
     * @param matchedConditions 命中的条件
     * @param totalConditions   规则 match 条件总数(用于归一)
     * @param weight            规则置信权重 0..1
     * @return 0..100 的最终分
     */
    fun score(
        matchedConditions: List<RuleCondition>,
        totalConditions: Int,
        weight: Double,
    ): Int {
        if (totalConditions <= 0) return 0
        val gained = matchedConditions.sumOf { specificity(it) }
        // 满分基准:假设每条都按其具体度满额命中
        val maxByCount = (totalConditions * MAX_SPECIFICITY).coerceAtLeast(1)
        val base = (gained.toDouble() / maxByCount).coerceIn(0.0, 1.0)
        // 命中比例也纳入:全部命中给满,部分命中按比例
        val hitRatio = matchedConditions.size.toDouble() / totalConditions
        val combined = 0.5 * base + 0.5 * hitRatio
        return (combined * weight * 100).toInt().coerceIn(0, 100)
    }

    /** 排序键:分数 * 1000 + 权重档,保证分数优先、权重次级 */
    fun sortKey(result: RuleMatchResult): Int =
        result.score * 1000 + (result.rule.confidenceWeight * 100).toInt()

    private const val MAX_SPECIFICITY = 4
}
