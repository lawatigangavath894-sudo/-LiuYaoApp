package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.mockChart
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RulePolarity
import com.liuyao.paipan.domain.rule.RuleTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 匹配器第一版测试。
 * 卦:mockChart() = 乾为天初爻动,巳月丙申日;占类 FAME(求名,用神=父母)。
 * 父母爻:三爻辰土(应,旬空)、上爻戌土(世,得月生)。
 */
class RuleMatcherTest {

    private val chart = mockChart()

    private fun rule(
        id: String,
        polarity: RulePolarity,
        match: List<RuleCondition>,
        exclude: List<RuleCondition> = emptyList(),
        target: RuleTarget = RuleTarget.UseGod,
        category: DivinationCategory = DivinationCategory.FAME,
        weight: Double = 0.7,
    ) = DivinationRule(
        id = id, sourceId = "s", sourceName = "测试",
        category = category, target = target,
        originalText = "原文$id", plainExplanation = "白话$id",
        conditionText = "", positiveMeaning = null, negativeMeaning = null,
        matchConditions = match, excludeConditions = exclude,
        polarity = polarity, priority = 50, confidenceWeight = weight, tags = emptyList(),
    )

    @Test
    fun threeBuckets_andConflict() {
        val rules = listOf(
            rule("A", RulePolarity.AUSPICIOUS, listOf(RuleCondition.KinPresent(SixKin.PARENT))),
            rule("B", RulePolarity.INAUSPICIOUS, listOf(RuleCondition.UseGodVoid)),
            rule("C", RulePolarity.NEUTRAL, listOf(RuleCondition.ResponseVoid, RuleCondition.UseGodVoid)),
        )
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, rules)

        assertEquals(1, report.supportYes.size)
        assertEquals("A", report.supportYes.first().rule.id)
        assertEquals(1, report.supportNo.size)
        assertEquals("B", report.supportNo.first().rule.id)
        assertEquals(1, report.neutral.size)
        assertEquals("C", report.neutral.first().rule.id)
        // 支持成与支持不成并存,不合并
        assertTrue(report.hasConflict)
    }

    @Test
    fun excludeCondition_removesRule() {
        val r = rule(
            "D", RulePolarity.AUSPICIOUS,
            match = listOf(RuleCondition.KinPresent(SixKin.PARENT)),
            exclude = listOf(RuleCondition.UseGodVoid), // 用神(父母辰土)旬空 → 排除
        )
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(r))
        // 被排除,不进任何桶
        assertTrue(report.all.none { it.rule.id == "D" })
    }

    @Test
    fun categoryMismatch_filteredOut() {
        // 规则属 WEALTH,当前占类 FAME → 不匹配
        val r = rule("E", RulePolarity.AUSPICIOUS, listOf(RuleCondition.KinPresent(SixKin.PARENT)), category = DivinationCategory.WEALTH)
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(r))
        assertTrue(report.all.isEmpty())
    }

    @Test
    fun targetKinMismatch_filteredOut() {
        // FAME 用神=父母;规则指定类神为妻财 → 类神不匹配
        val r = rule(
            "F", RulePolarity.AUSPICIOUS,
            listOf(RuleCondition.KinPresent(SixKin.PARENT)),
            target = RuleTarget.kin(SixKin.WEALTH),
        )
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(r))
        assertTrue(report.all.isEmpty())
    }

    @Test
    fun result_fieldsPopulated() {
        val r = rule("G", RulePolarity.INAUSPICIOUS, listOf(RuleCondition.UseGodVoid))
        val res = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(r)).supportNo.first()
        assertTrue(res.matched)
        assertTrue(res.score in 1..100)
        assertEquals(RulePolarity.INAUSPICIOUS, res.polarity)
        assertTrue(res.matchedConditions.contains(RuleCondition.UseGodVoid))
        assertTrue(res.excludedByConditions.isEmpty())
        // 用神父母旬空者为三爻辰土 → relatedLines 含 3
        assertTrue(res.relatedLineIndexes.contains(3))
        assertTrue(res.explanation.isNotBlank())
    }

    @Test
    fun higherWeightRanksFirst() {
        val low = rule("L", RulePolarity.AUSPICIOUS, listOf(RuleCondition.KinPresent(SixKin.PARENT)), weight = 0.5)
        val high = rule("H", RulePolarity.AUSPICIOUS, listOf(RuleCondition.KinPresent(SixKin.PARENT)), weight = 0.95)
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(low, high))
        // 权重高者排前(原则 6)
        assertEquals("H", report.supportYes.first().rule.id)
    }

    @Test
    fun partialMatch_notMatched() {
        // 两条 match,其一不成立(用神得月建:父母上爻戌得月生成立;但用日冲——父母无被日冲者)
        val r = rule(
            "P", RulePolarity.AUSPICIOUS,
            listOf(RuleCondition.UseGodSupportedByMonth, RuleCondition.UseGodDayClashed),
        )
        val report = RuleMatcher.match(chart, DivinationCategory.FAME, listOf(r))
        // 有一条未命中 → 整体不命中
        assertTrue(report.all.isEmpty())
    }
}
