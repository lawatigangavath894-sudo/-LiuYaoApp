package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.HeavenlyStem
import com.liuyao.paipan.domain.model.Hexagram

/**
 * 7. 纳甲规则。
 *
 * 为一卦的六爻各配干支:
 *  - 初、二、三爻(内卦)用内卦纳甲天干 + 内卦地支;
 *  - 四、五、上爻(外卦)用外卦纳甲天干 + 外卦地支。
 * 返回列表 index0 = 初爻。
 */
object NaJiaCalculator {

    /** 返回六爻纳甲,index0=初爻 */
    fun naJiaOf(hexagram: Hexagram): List<GanZhi> =
        naJiaOf(hexagram.lowerTrigram, hexagram.upperTrigram)

    fun naJiaOf(inner: EightTrigram, outer: EightTrigram): List<GanZhi> {
        val innerStem: HeavenlyStem = Trigram.innerStem.getValue(inner)
        val outerStem: HeavenlyStem = Trigram.outerStem.getValue(outer)
        val innerBranches: List<EarthlyBranch> = Trigram.innerBranches.getValue(inner)
        val outerBranches: List<EarthlyBranch> = Trigram.outerBranches.getValue(outer)

        val result = ArrayList<GanZhi>(6)
        // 内卦三爻
        for (i in 0 until 3) result.add(GanZhi(innerStem, innerBranches[i]))
        // 外卦三爻
        for (i in 0 until 3) result.add(GanZhi(outerStem, outerBranches[i]))
        return result
    }

    /** 取某一爻(1..6)的纳甲 */
    fun naJiaAt(hexagram: Hexagram, position: Int): GanZhi {
        require(position in 1..6) { "爻位须 1..6" }
        return naJiaOf(hexagram)[position - 1]
    }
}
