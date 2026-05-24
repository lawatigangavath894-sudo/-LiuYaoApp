package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.FiveElement
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.Hexagram
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.SixKin

/**
 * 8. 六亲计算。
 *
 * 以"卦宫五行"为我,各爻"纳甲地支五行"为他,按生克比和定六亲:
 *  生我者父母、克我者官鬼、我克者妻财、我生者子孙、比和者兄弟。
 * (规则本身封装在 [SixKin.of])
 *
 * 返回列表 index0 = 初爻。
 */
object SixKinCalculator {

    fun sixKinOf(hexagram: Hexagram): List<SixKin> {
        val palace = PalaceCalculator.palaceOf(hexagram)
        val naJia = NaJiaCalculator.naJiaOf(hexagram)
        return sixKinOf(palace.element, naJia)
    }

    /** 由卦宫五行 + 六爻纳甲列表,逐爻定六亲 */
    fun sixKinOf(palaceElement: FiveElement, naJia: List<GanZhi>): List<SixKin> =
        naJia.map { SixKin.of(palaceElement, it.branchElement) }

    fun sixKinOf(inner: EightTrigram, outer: EightTrigram): List<SixKin> {
        val palace: Palace = PalaceCalculator.palaceOf(inner, outer)
        val naJia = NaJiaCalculator.naJiaOf(inner, outer)
        return sixKinOf(palace.element, naJia)
    }
}
