package com.liuyao.paipan.domain.match

import com.liuyao.paipan.data.db.entity.RuleStatsEntity

/**
 * 规则可靠度计算。
 *
 * 由历史反馈统计得出 reliability ∈ [0,1]:
 *  - 验中(hit)计正面,误判(miss)计负面,部分验中(partial)计半个正面;
 *  - 无法判断(unknown)不计入有效反馈,仅记录;
 *  - 采用贝叶斯平滑:反馈样本少时向中性先验 [PRIOR](0.5)收敛,
 *    避免一两次反馈造成剧烈摆动;样本越多越接近真实命中率。
 *
 * 设计取舍(对应需求):
 *  - 不删除任何规则(本计算只产出 0..1,不做删除);
 *  - 只降低误判规则的排序权重:reliability 低 → 排序乘子小 → 靠后;
 *    但不会把规则过滤掉。
 *
 * 纯函数,独立可测。
 */
object RuleReliabilityCalculator {

    /** 中性先验 */
    const val PRIOR = 0.5
    /** 先验等效样本量(平滑强度) */
    const val PRIOR_WEIGHT = 4.0

    /**
     * @return reliability ∈ [0,1];无任何反馈时返回 [PRIOR]。
     */
    fun reliability(stats: RuleStatsEntity?): Double {
        if (stats == null) return PRIOR
        val hit = stats.hitCount.coerceAtLeast(0)
        val miss = stats.missCount.coerceAtLeast(0)
        val partial = stats.partialCount.coerceAtLeast(0)

        // 有效反馈:hit 计 1 分,partial 计 0.5 分,miss 计 0 分
        val positive = hit + 0.5 * partial
        val effective = (hit + miss + partial).toDouble()
        if (effective <= 0.0) return PRIOR

        // 贝叶斯平滑:(positive + prior*priorWeight) / (effective + priorWeight)
        val smoothed = (positive + PRIOR * PRIOR_WEIGHT) / (effective + PRIOR_WEIGHT)
        return smoothed.coerceIn(0.0, 1.0)
    }

    /**
     * 排序乘子 ∈ [MIN_MULTIPLIER, 1.0]。
     * reliability 高 → 接近 1(不削弱);低 → 趋近下限(显著靠后)。
     * 误判多的规则因 reliability 低而被压低排序,但仍保留(不删除)。
     */
    fun sortMultiplier(stats: RuleStatsEntity?): Double {
        val r = reliability(stats)
        // 线性映射 [0,1] → [MIN_MULTIPLIER,1]
        return MIN_MULTIPLIER + (1.0 - MIN_MULTIPLIER) * r
    }

    const val MIN_MULTIPLIER = 0.4

    /** 人类可读的表现摘要 */
    fun summary(stats: RuleStatsEntity?): String {
        if (stats == null || (stats.hitCount + stats.missCount + stats.partialCount + stats.unknownCount) == 0) {
            return "暂无反馈记录"
        }
        val r = (reliability(stats) * 100).toInt()
        return "可靠度 $r% · 验中${stats.hitCount} 误判${stats.missCount} 部分${stats.partialCount}"
    }
}
