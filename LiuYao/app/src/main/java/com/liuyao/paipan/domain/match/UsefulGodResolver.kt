package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin

/**
 * 用神 [UsefulGod]:要么是某个六亲,要么"以世为用"(出行/综合运势等无固定六亲用神)。
 */
sealed interface UsefulGod {
    data class Kin(val kin: SixKin) : UsefulGod
    data object WorldSelf : UsefulGod
}

/**
 * 用神判定:由占类确定该卦的用神。
 *
 * 六爻"取用"随事而定。第一版给出最通行的对应,取**主用神**;
 * [candidates] 暴露候选六亲(供后续更精细取用,如婚占男测取财、女测取官)。
 */
object UsefulGodResolver {

    fun primary(category: DivinationCategory): UsefulGod = when (category) {
        DivinationCategory.CAREER -> UsefulGod.Kin(SixKin.OFFICIAL)
        DivinationCategory.WEALTH -> UsefulGod.Kin(SixKin.WEALTH)
        DivinationCategory.MARRIAGE -> UsefulGod.Kin(SixKin.OFFICIAL)
        DivinationCategory.HEALTH -> UsefulGod.Kin(SixKin.OFFICIAL)
        DivinationCategory.STUDY -> UsefulGod.Kin(SixKin.PARENT)
        DivinationCategory.FAME -> UsefulGod.Kin(SixKin.PARENT)
        DivinationCategory.LOST -> UsefulGod.Kin(SixKin.WEALTH)
        DivinationCategory.TRAVEL -> UsefulGod.WorldSelf
        DivinationCategory.LAWSUIT -> UsefulGod.Kin(SixKin.OFFICIAL)
        DivinationCategory.PREGNANCY -> UsefulGod.Kin(SixKin.OFFSPRING)
        DivinationCategory.HOUSE -> UsefulGod.Kin(SixKin.PARENT)
        DivinationCategory.COOPERATION -> UsefulGod.Kin(SixKin.WEALTH)
        DivinationCategory.FORTUNE -> UsefulGod.WorldSelf
        DivinationCategory.OTHER -> UsefulGod.WorldSelf
    }

    /** 候选用神六亲(含主次) */
    fun candidates(category: DivinationCategory): List<SixKin> = when (category) {
        DivinationCategory.MARRIAGE -> listOf(SixKin.OFFICIAL, SixKin.WEALTH)
        DivinationCategory.CAREER -> listOf(SixKin.OFFICIAL, SixKin.PARENT)
        else -> when (val u = primary(category)) {
            is UsefulGod.Kin -> listOf(u.kin)
            UsefulGod.WorldSelf -> emptyList()
        }
    }

    /** 主用神对应的六亲(以世为用时返回 null) */
    fun primaryKin(category: DivinationCategory): SixKin? =
        (primary(category) as? UsefulGod.Kin)?.kin
}
