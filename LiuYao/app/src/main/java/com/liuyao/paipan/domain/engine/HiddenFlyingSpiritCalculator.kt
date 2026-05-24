package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.FlyingSpirit
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.Hexagram
import com.liuyao.paipan.domain.model.HiddenSpirit
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.SixKin

/**
 * 10 & 11. 伏神 / 飞神。
 *
 * 规则:某卦六亲不全时,缺失的六亲"伏"在卦中。传统取法以本宫首卦(纯卦)
 * 的纳甲与六亲为参照:在纯卦中找到该缺失六亲所在的爻位,把它作为伏神,
 * 伏于本卦同位置之爻(该本卦爻即为"飞神")。
 *
 * 返回:爻位(1..6)→ (伏神, 飞神)。仅含确有伏神的爻位。
 *
 * TODO: 个别流派对"伏神是否以首卦为准/是否取月卦身"有差异,本版采用最通行的
 *       "本宫首卦伏神"法;后续可扩展策略。
 */
object HiddenFlyingSpiritCalculator {

    data class Hidden(val hiddenSpirit: HiddenSpirit, val flyingSpirit: FlyingSpirit)

    /**
     * @param hexagram      本卦
     * @param presentKins   本卦六爻六亲(index0=初爻),用于判断缺哪些六亲
     * @param naJia         本卦六爻纳甲(index0=初爻),用于构造飞神
     */
    fun compute(
        hexagram: Hexagram,
        presentKins: List<SixKin>,
        naJia: List<GanZhi>,
    ): Map<Int, Hidden> {
        val palace: Palace = PalaceCalculator.palaceOf(hexagram)
        val present = presentKins.toSet()
        val missing = SixKin.entries.toSet() - present
        if (missing.isEmpty()) return emptyMap()

        // 本宫首卦(纯卦):内外卦皆为本宫卦
        val pure = Hexagram(palace.pureTrigram, palace.pureTrigram)
        val pureNaJia = NaJiaCalculator.naJiaOf(pure)
        val pureKins = SixKinCalculator.sixKinOf(palace.element, pureNaJia)

        val result = LinkedHashMap<Int, Hidden>()
        for (kin in missing) {
            // 在纯卦中找该六亲首次出现的爻位
            val idx = pureKins.indexOfFirst { it == kin }
            if (idx < 0) continue // 理论上不会发生
            val position = idx + 1
            val hidden = HiddenSpirit(sixKin = kin, naJia = pureNaJia[idx])
            // 飞神 = 本卦同位置之爻
            val flying = FlyingSpirit(sixKin = presentKins[idx], naJia = naJia[idx])
            result[position] = Hidden(hidden, flying)
        }
        return result
    }
}
