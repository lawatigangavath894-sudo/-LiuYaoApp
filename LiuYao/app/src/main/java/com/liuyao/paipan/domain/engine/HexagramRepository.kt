package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.Hexagram
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.YinYang

/**
 * 六十四卦索引仓库。
 *
 * 通过"八宫卦序"算法一次性展开 64 卦,记录每卦的:
 *  - 所属卦宫 [Palace]
 *  - 世爻位 [worldPosition](1..6)
 *  - 应爻位 [responsePosition](1..6)
 *
 * 卦由 (内卦, 外卦) 唯一确定,作为查询键。
 * 数据由八宫纯卦逐爻翻转生成,已对照标准盘核验全部 64 卦。
 */
object HexagramRepository {

    data class HexInfo(
        val palace: Palace,
        val worldPosition: Int,
        val responsePosition: Int,
    )

    private val table: Map<Pair<EightTrigram, EightTrigram>, HexInfo> = build()

    /** 查询某卦的卦宫/世应。 */
    fun lookup(inner: EightTrigram, outer: EightTrigram): HexInfo =
        table[inner to outer] ?: error("未登记的卦: 内${inner.cn}外${outer.cn}")

    fun lookup(hexagram: Hexagram): HexInfo =
        lookup(hexagram.lowerTrigram, hexagram.upperTrigram)

    /** 全部 64 卦信息(用于测试/校验)。 */
    fun all(): Map<Pair<EightTrigram, EightTrigram>, HexInfo> = table

    private fun build(): Map<Pair<EightTrigram, EightTrigram>, HexInfo> = buildMap {
        // 八宫的纯卦(本宫卦)
        val pureOrder = listOf(
            EightTrigram.QIAN, EightTrigram.DUI, EightTrigram.LI, EightTrigram.ZHEN,
            EightTrigram.XUN, EightTrigram.KAN, EightTrigram.GEN, EightTrigram.KUN,
        )
        for (pure in pureOrder) {
            val palace = Palace.ofTrigram(pure)
            val pureLines = pure.lines + pure.lines // 六爻 = 内外皆纯卦
            for (state in Trigram.palaceStates) {
                val lines = pureLines.toMutableList()
                for (pos in state.flips) {
                    lines[pos - 1] = lines[pos - 1].flip()
                }
                val inner = EightTrigram.fromLines(lines.subList(0, 3))
                val outer = EightTrigram.fromLines(lines.subList(3, 6))
                val world = state.worldPosition
                val response = if (world <= 3) world + 3 else world - 3
                // 八宫展开保证 (inner,outer) 唯一,不会覆盖
                put(inner to outer, HexInfo(palace, world, response))
            }
        }
    }

    private fun YinYang.flip(): YinYang = if (this == YinYang.YANG) YinYang.YIN else YinYang.YANG
}
