package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.HeavenlyStem

/**
 * 八卦在排盘中的"算法属性表"。
 *
 * 复用 [EightTrigram] 枚举本身(定义在 domain/model),这里只补充
 * 排盘所需、模型层未承载的规则数据:纳甲天干、纳甲地支、八宫卦序。
 *
 * 纳甲口诀依据(京房纳甲):
 *  乾纳甲壬、坤纳乙癸、艮纳丙、兑纳丁、坎纳戊、离纳己、震纳庚、巽纳辛。
 *  其中乾(内甲外壬)、坤(内乙外癸)内外异干,其余六卦内外同干。
 *  地支:阳卦顺行、阴卦逆行,起支依各卦,排成内三、外三。
 *
 * 全部数据已对照八宫六十四卦标准盘核验。
 */
object Trigram {

    /** 内卦(下三爻)纳甲天干 */
    val innerStem: Map<EightTrigram, HeavenlyStem> = mapOf(
        EightTrigram.QIAN to HeavenlyStem.JIA,
        EightTrigram.KAN to HeavenlyStem.WU,
        EightTrigram.GEN to HeavenlyStem.BING,
        EightTrigram.ZHEN to HeavenlyStem.GENG,
        EightTrigram.XUN to HeavenlyStem.XIN,
        EightTrigram.LI to HeavenlyStem.JI,
        EightTrigram.KUN to HeavenlyStem.YI,
        EightTrigram.DUI to HeavenlyStem.DING,
    )

    /** 外卦(上三爻)纳甲天干。乾/坤内外异干。 */
    val outerStem: Map<EightTrigram, HeavenlyStem> = mapOf(
        EightTrigram.QIAN to HeavenlyStem.REN,
        EightTrigram.KAN to HeavenlyStem.WU,
        EightTrigram.GEN to HeavenlyStem.BING,
        EightTrigram.ZHEN to HeavenlyStem.GENG,
        EightTrigram.XUN to HeavenlyStem.XIN,
        EightTrigram.LI to HeavenlyStem.JI,
        EightTrigram.KUN to HeavenlyStem.GUI,
        EightTrigram.DUI to HeavenlyStem.DING,
    )

    /** 内卦三爻地支(index0=初爻,自下而上) */
    val innerBranches: Map<EightTrigram, List<EarthlyBranch>> = mapOf(
        EightTrigram.QIAN to br("子寅辰"),
        EightTrigram.KAN to br("寅辰午"),
        EightTrigram.GEN to br("辰午申"),
        EightTrigram.ZHEN to br("子寅辰"),
        EightTrigram.XUN to br("丑亥酉"),
        EightTrigram.LI to br("卯丑亥"),
        EightTrigram.KUN to br("未巳卯"),
        EightTrigram.DUI to br("巳卯丑"),
    )

    /** 外卦三爻地支(index0=四爻,自下而上) */
    val outerBranches: Map<EightTrigram, List<EarthlyBranch>> = mapOf(
        EightTrigram.QIAN to br("午申戌"),
        EightTrigram.KAN to br("申戌子"),
        EightTrigram.GEN to br("戌子寅"),
        EightTrigram.ZHEN to br("午申戌"),
        EightTrigram.XUN to br("未巳卯"),
        EightTrigram.LI to br("酉未巳"),
        EightTrigram.KUN to br("丑亥酉"),
        EightTrigram.DUI to br("亥酉未"),
    )

    /**
     * 八宫卦序的"翻爻集合 + 世位"。索引 0..7 对应:
     *  本宫卦、一世、二世、三世、四世、五世、游魂、归魂。
     * 翻爻集合中的数字为爻位(1=初爻 … 6=上爻),表示相对纯卦翻转的爻。
     */
    data class PalaceState(val flips: Set<Int>, val worldPosition: Int)

    val palaceStates: List<PalaceState> = listOf(
        PalaceState(emptySet(), 6),            // 本宫(纯卦)
        PalaceState(setOf(1), 1),              // 一世
        PalaceState(setOf(1, 2), 2),           // 二世
        PalaceState(setOf(1, 2, 3), 3),        // 三世
        PalaceState(setOf(1, 2, 3, 4), 4),     // 四世
        PalaceState(setOf(1, 2, 3, 4, 5), 5),  // 五世
        PalaceState(setOf(1, 2, 3, 5), 4),     // 游魂(五世退四,即翻第四爻回来)
        PalaceState(setOf(5), 3),              // 归魂(内卦复位)
    )

    private fun br(s: String): List<EarthlyBranch> = s.map { EarthlyBranch.fromCn(it.toString()) }
}
