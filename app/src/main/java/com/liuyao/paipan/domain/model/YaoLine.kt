package com.liuyao.paipan.domain.model

/**
 * 14. 伏神 HiddenSpirit —— 本宫缺失之六亲,伏于某飞爻之下。
 * 伏神自身带六亲、纳甲与五行;其所伏的飞爻信息见 [FlyingSpirit]。
 */
data class HiddenSpirit(
    val sixKin: SixKin,
    val naJia: GanZhi,
) {
    val element: FiveElement get() = naJia.branchElement
}

/**
 * 15. 飞神 FlyingSpirit —— 压在伏神之上的本卦爻(供"飞伏生克"分析)。
 */
data class FlyingSpirit(
    val sixKin: SixKin,
    val naJia: GanZhi,
) {
    val element: FiveElement get() = naJia.branchElement
}

/**
 * 16. 变爻 ChangedLine —— 动爻变出之爻。
 * 六亲仍以本卦卦宫五行论(回头生克的判断基于本爻与变爻五行)。
 */
data class ChangedLine(
    val yinYang: YinYang,
    val sixKin: SixKin,
    val naJia: GanZhi,
) {
    val element: FiveElement get() = naJia.branchElement
}

/**
 * 13. 爻状态 LineStatus —— 该爻在本卦时空下的状态集合 + 旺衰。
 * 字段对应题目要求,均为布尔标志,便于断语规则直接消费。
 */
data class LineStatus(
    val isVoid: Boolean = false,           // 旬空
    val isMonthBroken: Boolean = false,    // 月破
    val isDayClashed: Boolean = false,     // 日冲(暗动/冲实视引擎判定)
    val isCombined: Boolean = false,       // 逢合(日合或爻间合)
    val isClashed: Boolean = false,        // 逢冲(爻间)
    val isPunished: Boolean = false,       // 逢刑
    val isHarmed: Boolean = false,         // 逢害
    val isSupportedByMonth: Boolean = false, // 得月生扶
    val isSupportedByDay: Boolean = false,   // 得日生扶
    val strength: StrengthLevel = StrengthLevel.RESTING,
)

/**
 * 11. 爻 YaoLine —— 排盘的最小单元。
 *
 * @param index 1..6,1=初爻(最下),6=上爻。
 * @param naJia 纳甲:该爻所配干支(地支五行即 [element])。
 */
data class YaoLine(
    val index: Int,
    val yinYang: YinYang,
    val isMoving: Boolean,
    val sixGod: SixGod,
    val sixKin: SixKin,
    val naJia: GanZhi,
    val element: FiveElement,
    val isWorld: Boolean,
    val isResponse: Boolean,
    val hiddenSpirit: HiddenSpirit? = null,
    val flyingSpirit: FlyingSpirit? = null,
    val changedLine: ChangedLine? = null,
    val status: LineStatus = LineStatus(),
) {
    /** 该爻地支 */
    val branch: EarthlyBranch get() = naJia.branch

    /** 是否带伏神 */
    val hasHidden: Boolean get() = hiddenSpirit != null
}
