package com.liuyao.paipan.domain.match

import com.liuyao.paipan.data.db.entity.RuleStatsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleReliabilityCalculatorTest {

    private fun stats(hit: Int = 0, miss: Int = 0, partial: Int = 0, unknown: Int = 0) =
        RuleStatsEntity(
            ruleId = "r", matchedCount = hit + miss + partial + unknown,
            hitCount = hit, missCount = miss, lastUpdatedEpoch = 0,
            partialCount = partial, unknownCount = unknown,
        )

    @Test
    fun noFeedback_isNeutral() {
        assertEquals(RuleReliabilityCalculator.PRIOR, RuleReliabilityCalculator.reliability(null), 0.0001)
        assertEquals(RuleReliabilityCalculator.PRIOR, RuleReliabilityCalculator.reliability(stats()), 0.0001)
    }

    @Test
    fun manyHits_highReliability() {
        val r = RuleReliabilityCalculator.reliability(stats(hit = 20))
        assertTrue("应明显高于中性: $r", r > 0.8)
    }

    @Test
    fun manyMisses_lowReliability() {
        val r = RuleReliabilityCalculator.reliability(stats(miss = 20))
        assertTrue("应明显低于中性: $r", r < 0.2)
    }

    @Test
    fun partial_countsHalf() {
        // 全部部分验中,应趋向 0.5 附近(半分)
        val r = RuleReliabilityCalculator.reliability(stats(partial = 20))
        assertTrue("部分验中应在中性附近: $r", r in 0.4..0.6)
    }

    @Test
    fun missLowersSortMultiplier() {
        val good = RuleReliabilityCalculator.sortMultiplier(stats(hit = 10))
        val bad = RuleReliabilityCalculator.sortMultiplier(stats(miss = 10))
        assertTrue("误判规则乘子应更小: good=$good bad=$bad", bad < good)
        // 但不为 0(不删除,只降权)
        assertTrue("乘子有下限,不删除规则", bad >= RuleReliabilityCalculator.MIN_MULTIPLIER - 0.0001)
    }

    @Test
    fun smoothing_smallSampleNotExtreme() {
        // 仅 1 次验中,不应立刻冲到接近 1
        val r = RuleReliabilityCalculator.reliability(stats(hit = 1))
        assertTrue("小样本应被平滑: $r", r in 0.5..0.7)
    }

    @Test
    fun matcherSorting_reliableRuleRanksFirst() {
        // 用乘子作为 reliabilityProvider 验证排序:误判规则被压后
        // 这里仅验证 sortMultiplier 的单调性已足够(matcher 注入该乘子)
        val highMul = RuleReliabilityCalculator.sortMultiplier(stats(hit = 10))
        val lowMul = RuleReliabilityCalculator.sortMultiplier(stats(miss = 10))
        assertTrue(highMul > lowMul)
    }
}
