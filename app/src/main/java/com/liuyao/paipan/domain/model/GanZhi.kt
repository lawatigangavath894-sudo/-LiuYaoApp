package com.liuyao.paipan.domain.model

/**
 * 10. 干支 GanZhi —— 天干 + 地支 的组合。
 *
 * 用于:四柱(年月日时)、纳甲(每爻配干支)。
 * 这里只承载结构与确定性属性,不做"由公历推干支"的历法计算(那属于排盘引擎层)。
 */
data class GanZhi(
    val stem: HeavenlyStem,
    val branch: EarthlyBranch,
) {
    /** 显示用:如 "丙申" */
    val cn: String get() = "${stem.cn}${branch.cn}"

    /** 地支五行(六爻断事常以地支五行为准) */
    val branchElement: FiveElement get() = branch.element

    /** 天干五行 */
    val stemElement: FiveElement get() = stem.element

    /**
     * 60 甲子序号(0..59);若该天干地支组合不构成合法甲子(阴阳不配)返回 -1。
     * 合法甲子要求天干与地支序号同奇偶。
     */
    val sexagenaryIndex: Int
        get() {
            val s = stem.ordinal
            val b = branch.ordinal
            if ((s % 2) != (b % 2)) return -1
            // 求满足 i≡s (mod10) 且 i≡b (mod12) 的 0..59
            for (i in 0 until 60) if (i % 10 == s && i % 12 == b) return i
            return -1
        }

    override fun toString(): String = cn

    companion object {
        fun fromCn(s: String): GanZhi {
            require(s.length == 2) { "干支须为两字: $s" }
            return GanZhi(HeavenlyStem.fromCn(s[0].toString()), EarthlyBranch.fromCn(s[1].toString()))
        }

        /** 由 60 甲子序号构造 */
        fun fromIndex(i: Int): GanZhi {
            val n = ((i % 60) + 60) % 60
            return GanZhi(HeavenlyStem.fromIndex(n % 10), EarthlyBranch.fromIndex(n % 12))
        }
    }
}
