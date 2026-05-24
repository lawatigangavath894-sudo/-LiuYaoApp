package com.liuyao.paipan.domain.rule

import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin

/**
 * 3. 规则条件 RuleCondition —— 断语命中的结构化条件单元。
 *
 * 采用 sealed interface:每种条件是一个具体类型,类型安全、可被未来的匹配器穷举,
 * 也便于序列化(后续 Room 阶段会讨论 JSON vs 拆表)。
 *
 * 覆盖题目要求的 20 种条件(序号对应):
 *  1 六亲出现  2 六亲不现  3 用神发动  4 用神空亡  5 用神月破  6 用神日冲
 *  7 用神得月建  8 用神得日辰  9 世爻空亡  10 应爻空亡  11 世应相生  12 世应相克
 *  13 动爻化回头生  14 动爻化回头克  15 六神出现  16 地支冲  17 地支合
 *  18 爻位指定  19 伏神出现  20 飞神压伏神
 *
 * 每个条件提供 [cn] 人类可读描述,便于展示与调试。
 */
sealed interface RuleCondition {
    val cn: String

    // 1. 某六亲出现(上卦)
    data class KinPresent(val kin: SixKin) : RuleCondition {
        override val cn get() = "${kin.cn}出现"
    }

    // 2. 某六亲不现(不上卦)
    data class KinAbsent(val kin: SixKin) : RuleCondition {
        override val cn get() = "${kin.cn}不上卦"
    }

    // 3. 用神发动
    data object UseGodMoving : RuleCondition {
        override val cn get() = "用神发动"
    }

    // 4. 用神空亡
    data object UseGodVoid : RuleCondition {
        override val cn get() = "用神旬空"
    }

    // 5. 用神月破
    data object UseGodMonthBroken : RuleCondition {
        override val cn get() = "用神月破"
    }

    // 6. 用神日冲
    data object UseGodDayClashed : RuleCondition {
        override val cn get() = "用神逢日冲"
    }

    // 7. 用神得月建(月生扶)
    data object UseGodSupportedByMonth : RuleCondition {
        override val cn get() = "用神得月建生扶"
    }

    // 8. 用神得日辰(日生扶)
    data object UseGodSupportedByDay : RuleCondition {
        override val cn get() = "用神得日辰生扶"
    }

    // 9. 世爻空亡
    data object WorldVoid : RuleCondition {
        override val cn get() = "世爻旬空"
    }

    // 10. 应爻空亡
    data object ResponseVoid : RuleCondition {
        override val cn get() = "应爻旬空"
    }

    // 11. 世应相生
    data object WorldResponseGenerate : RuleCondition {
        override val cn get() = "世应相生"
    }

    // 12. 世应相克
    data object WorldResponseRestrain : RuleCondition {
        override val cn get() = "世应相克"
    }

    // 13. 动爻化回头生
    data object BackGenerate : RuleCondition {
        override val cn get() = "动爻化回头生"
    }

    // 14. 动爻化回头克
    data object BackRestrain : RuleCondition {
        override val cn get() = "动爻化回头克"
    }

    // 15. 某六神出现(可指定落在哪类目标上,target 可空表示只要出现)
    data class SixGodPresent(
        val sixGod: SixGod,
        val onTarget: RuleTarget? = null,
    ) : RuleCondition {
        override val cn get() = buildString {
            append(sixGod.cn)
            if (onTarget != null) append("临").append(onTarget.cn) else append("出现")
        }
    }

    // 16. 地支相冲(两地支;任一为空表示"用神逢冲"由匹配器结合用神判断)
    data class BranchClash(
        val a: EarthlyBranch? = null,
        val b: EarthlyBranch? = null,
    ) : RuleCondition {
        override val cn get() = if (a != null && b != null) "${a.cn}${b.cn}相冲" else "逢冲"
    }

    // 17. 地支相合
    data class BranchCombine(
        val a: EarthlyBranch? = null,
        val b: EarthlyBranch? = null,
    ) : RuleCondition {
        override val cn get() = if (a != null && b != null) "${a.cn}${b.cn}相合" else "逢合"
    }

    // 18. 爻位指定(某一爻位满足某子条件;position 1..6)
    data class AtPosition(
        val position: Int,
        val inner: RuleCondition? = null,
    ) : RuleCondition {
        override val cn get() = "第${position}爻" + (inner?.let { "(${it.cn})" } ?: "")
    }

    // 19. 伏神出现(可指定伏神为某六亲)
    data class HiddenSpiritPresent(val kin: SixKin? = null) : RuleCondition {
        override val cn get() = (kin?.cn ?: "") + "伏神出现"
    }

    // 20. 飞神压伏神(飞神克伏神;可指定伏神六亲)
    data class FlyingSuppressesHidden(val hiddenKin: SixKin? = null) : RuleCondition {
        override val cn get() = "飞神克制" + (hiddenKin?.cn ?: "") + "伏神"
    }
}
