package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.rule.MockRules
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.RulePolarity

/**
 * 示例匹配报告,供 UI 预览与测试(无需真实跑匹配器/DB)。
 */
object MockMatchResults {

    private val rules = MockRules.rules

    val report: MatchReport by lazy {
        // 取 MockRules 里几条,手工归入三类,体现"冲突并存"
        val yes = rules.filter { it.polarity == RulePolarity.AUSPICIOUS }.map {
            RuleMatchResult(
                rule = it,
                matched = true,
                score = 82,
                polarity = it.polarity,
                matchedConditions = listOf(RuleCondition.KinPresent(SixKin.WEALTH), RuleCondition.UseGodSupportedByDay),
                failedConditions = emptyList(),
                excludedByConditions = emptyList(),
                explanation = "命中:妻财出现、用神得日辰生扶。${it.plainExplanation}",
                relatedLineIndexes = listOf(2, 5),
            )
        }
        val no = rules.filter { it.polarity == RulePolarity.INAUSPICIOUS }.map {
            RuleMatchResult(
                rule = it,
                matched = true,
                score = 74,
                polarity = it.polarity,
                matchedConditions = listOf(RuleCondition.UseGodMoving, RuleCondition.BackRestrain),
                failedConditions = listOf(RuleCondition.UseGodVoid),
                excludedByConditions = emptyList(),
                explanation = "命中:用神发动、动爻化回头克。${it.plainExplanation}",
                relatedLineIndexes = listOf(1),
            )
        }
        val neutral = rules.filter { it.polarity == RulePolarity.NEUTRAL || it.polarity == RulePolarity.MIXED }.map {
            RuleMatchResult(
                rule = it,
                matched = true,
                score = 68,
                polarity = it.polarity,
                matchedConditions = listOf(RuleCondition.ResponseVoid, RuleCondition.UseGodVoid),
                failedConditions = emptyList(),
                excludedByConditions = emptyList(),
                explanation = "命中:用神临应且旬空,事在两可。${it.plainExplanation}",
                relatedLineIndexes = listOf(3),
            )
        }
        MatchReport(supportYes = yes, supportNo = no, neutral = neutral)
    }
}
