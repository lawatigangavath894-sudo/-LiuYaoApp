package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.SixKin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 卦法基础算法单元测试。
 *
 * 验收标准:给六个爻,可知本卦、变卦、卦宫、世应、纳甲、六亲。
 * 下列测试用 [HexagramCalcTest.fullPipeline_qianToGou] 完整覆盖该链路。
 *
 * 基准数据来自八宫六十四卦标准盘(已离线核对)。
 */
class HexagramCalcTest {

    /** 验收主用例:乾为天(初爻动)→ 天风姤 全链路。 */
    @Test
    fun fullPipeline_qianToGou() {
        // 输入:六爻阴阳,index0=初爻。乾为天 = 六阳。
        val lines = List(6) { true }
        val original = HexagramFactory.fromBooleans(lines)

        // 本卦
        assertEquals("乾为天", original.name)
        assertEquals(EightTrigram.QIAN, original.lowerTrigram)
        assertEquals(EightTrigram.QIAN, original.upperTrigram)

        // 卦宫
        assertEquals(Palace.QIAN_PALACE, PalaceCalculator.palaceOf(original))

        // 世应
        val wr = WorldResponseCalculator.of(original)
        assertEquals(6, wr.worldPosition)
        assertEquals(3, wr.responsePosition)

        // 纳甲(index0=初爻):甲子 甲寅 甲辰 壬午 壬申 壬戌
        val naJia = NaJiaCalculator.naJiaOf(original).map { it.cn }
        assertEquals(listOf("甲子", "甲寅", "甲辰", "壬午", "壬申", "壬戌"), naJia)

        // 六亲(乾宫金):初子孙 二妻财 三父母 四官鬼 五兄弟 上父母
        val kin = SixKinCalculator.sixKinOf(original)
        assertEquals(
            listOf(
                SixKin.OFFSPRING, SixKin.WEALTH, SixKin.PARENT,
                SixKin.OFFICIAL, SixKin.SIBLING, SixKin.PARENT,
            ),
            kin,
        )

        // 六冲卦
        assertTrue(HexagramRelationCalculator.isSixClash(original))

        // 变卦:初爻动 → 天风姤
        val changed = ChangedHexagramCalculator.changedOf(original, setOf(1))
        assertEquals("天风姤", changed?.name)
        // 变卦纳甲初爻应为 辛丑
        val changedNaJia = NaJiaCalculator.naJiaOf(changed!!).map { it.cn }
        assertEquals("辛丑", changedNaJia[0])
    }

    /** 火山旅(内艮外离):离宫,世1应4,纳甲与六亲核对。 */
    @Test
    fun huoShanLv_palaceWorldNaJia() {
        val hex = HexagramFactory.fromLines(
            // 内艮:阴阴阳;外离:阳阴阳
            listOf(false, false, true, true, false, true)
                .map { if (it) com.liuyao.paipan.domain.model.YinYang.YANG else com.liuyao.paipan.domain.model.YinYang.YIN },
        )
        assertEquals("火山旅", hex.name)
        assertEquals(Palace.LI_PALACE, PalaceCalculator.palaceOf(hex))
        val wr = WorldResponseCalculator.of(hex)
        assertEquals(1, wr.worldPosition)
        assertEquals(4, wr.responsePosition)
        // 纳甲:丙辰 丙午 丙申 己酉 己未 己巳
        assertEquals(
            listOf("丙辰", "丙午", "丙申", "己酉", "己未", "己巳"),
            NaJiaCalculator.naJiaOf(hex).map { it.cn },
        )
    }

    /** 静卦无变卦。 */
    @Test
    fun noMovingLine_givesNullChanged() {
        val original = HexagramFactory.fromBooleans(List(6) { true })
        assertNull(ChangedHexagramCalculator.changedOf(original, emptySet()))
        assertNull(ChangedHexagramCalculator.changedOf(original, List(6) { false }))
    }

    /** 六合卦判断:天地否 为六合。 */
    @Test
    fun sixCombine_recognized() {
        // 天地否:内坤(三阴)外乾(三阳)
        val pi = HexagramFactory.fromBooleans(listOf(false, false, false, true, true, true))
        assertEquals("天地否", pi.name)
        assertTrue(HexagramRelationCalculator.isSixCombine(pi))
    }

    /** 仓库完整性:八宫共登记 64 卦,且世位均在 1..6。 */
    @Test
    fun repository_has64Hexagrams() {
        val all = HexagramRepository.all()
        assertEquals(64, all.size)
        all.values.forEach {
            assertTrue(it.worldPosition in 1..6)
            assertTrue(it.responsePosition in 1..6)
            assertEquals(3, kotlin.math.abs(it.worldPosition - it.responsePosition))
        }
    }

    /** 六亲规则点测:乾宫(金),官鬼应为火。 */
    @Test
    fun sixKin_rule_spotCheck() {
        // 金:克我者为官鬼。克金者为火 → 官鬼=火
        val original = HexagramFactory.fromBooleans(List(6) { true })
        val naJia = NaJiaCalculator.naJiaOf(original)
        val kin = SixKinCalculator.sixKinOf(original)
        val officialIndex = kin.indexOfFirst { it == SixKin.OFFICIAL }
        assertEquals("火", naJia[officialIndex].branchElement.cn)
    }
}
