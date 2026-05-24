package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.DivinationMethod
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.StrengthLevel
import com.liuyao.paipan.domain.model.YinYang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * 排盘引擎第一版测试。
 *
 * 验收标准:输入六个爻,能输出一个完整结构化排盘。
 * [fullChart_qianMovingFirst] 覆盖四柱/旬空/本卦/变卦/卦宫/世应/纳甲/六亲/六神/爻状态全字段。
 */
class LiuYaoChartEngineTest {

    @Test
    fun fullChart_qianMovingFirst() {
        val result = LiuYaoChartEngine.build(MockChartInput.qianMovingFirst)
        val chart = result.chart

        // 四柱:丙午 癸巳 丙申 乙未
        assertEquals("丙午", chart.yearGanZhi.cn)
        assertEquals("癸巳", chart.monthGanZhi.cn)
        assertEquals("丙申", chart.dayGanZhi.cn)
        assertEquals("乙未", chart.hourGanZhi.cn)

        // 旬空:辰、巳
        assertEquals(listOf("辰", "巳"), chart.xunKong.map { it.cn })

        // 本卦 / 变卦 / 卦宫 / 世应
        assertEquals("乾为天", chart.originalHexagram.name)
        assertEquals("天风姤", chart.changedHexagram?.name)
        assertEquals(Palace.QIAN_PALACE, chart.palace)
        assertEquals(6, chart.worldLineIndex)
        assertEquals(3, chart.responseLineIndex)
        assertTrue(chart.isSixClash)

        // lines 完整 6 爻,index0=初爻
        assertEquals(6, chart.lines.size)
        val initial = chart.lines[0]
        assertEquals(1, initial.index)
        assertEquals("甲子", initial.naJia.cn)
        assertEquals(SixKin.OFFSPRING, initial.sixKin)
        assertEquals(SixGod.VERMILION_BIRD, initial.sixGod) // 丙日初爻起朱雀
        assertTrue(initial.isMoving)
        assertEquals(YinYang.YANG, initial.yinYang)

        // 初爻动 → 变爻六亲(子水变丑土,本宫金:土生金=父母)
        assertNotNull(initial.changedLine)
        assertEquals("辛丑", initial.changedLine?.naJia?.cn)
        assertEquals(SixKin.PARENT, initial.changedLine?.sixKin)

        // 世爻(上爻)父母戌土
        val world = chart.worldLine
        assertEquals(6, world.index)
        assertEquals(SixKin.PARENT, world.sixKin)
        assertEquals("壬戌", world.naJia.cn)

        // 六神序(初→上):朱雀 勾陈 螣蛇 白虎 玄武 青龙
        assertEquals(
            listOf(
                SixGod.VERMILION_BIRD, SixGod.HOOK_EARTH, SixGod.SOARING_SNAKE,
                SixGod.WHITE_TIGER, SixGod.BLACK_TORTOISE, SixGod.AZURE_DRAGON,
            ),
            chart.lines.map { it.sixGod },
        )

        // 爻状态点验:
        // 二爻寅木被日辰申冲 → 日冲
        assertTrue(chart.lines[1].status.isDayClashed)
        // 三爻辰土旬空
        assertTrue(chart.lines[2].status.isVoid)
        // 五爻申金:月令巳火克金 → 死
        assertEquals(StrengthLevel.DEAD, chart.lines[4].status.strength)
        // 四爻午火当令 → 旺
        assertEquals(StrengthLevel.PROSPEROUS, chart.lines[3].status.strength)
        // 全卦无亥,无月破
        assertTrue(chart.lines.none { it.status.isMonthBroken })

        // 乾为天六亲俱全 → 无伏神
        assertTrue(chart.lines.all { it.hiddenSpirit == null })
    }

    @Test
    fun hiddenSpirit_inGou() {
        // 天风姤(内巽外乾):乾宫缺妻财,妻财伏于二爻
        val input = ChartInput.fromBooleans(
            dateTime = LocalDateTime.of(2026, 5, 22, 14, 9),
            yangFlags = listOf(false, true, true, true, true, true), // 内巽(阴阳阳)外乾(阳阳阳)
            moving = List(6) { false },
            question = "测伏神",
            method = DivinationMethod.SolarTime,
        )
        val chart = LiuYaoChartEngine.build(input).chart
        assertEquals("天风姤", chart.originalHexagram.name)

        // 应有且仅有妻财为伏神
        val hiddenLines = chart.lines.filter { it.hiddenSpirit != null }
        assertEquals(1, hiddenLines.size)
        val h = hiddenLines.first()
        assertEquals(SixKin.WEALTH, h.hiddenSpirit?.sixKin)
        assertEquals(2, h.index)              // 伏于二爻
        assertNotNull(h.flyingSpirit)         // 飞神为本卦二爻(子孙亥水)
        assertEquals(SixKin.OFFSPRING, h.flyingSpirit?.sixKin)
    }

    @Test
    fun staticHexagram_noChanged() {
        val chart = LiuYaoChartEngine.build(MockChartInput.jiJiStatic).chart
        assertEquals("水火既济", chart.originalHexagram.name)
        assertNull(chart.changedHexagram)
        assertFalse(chart.lines.any { it.isMoving })
        assertTrue(chart.lines.all { it.changedLine == null })
    }

    @Test
    fun result_carriesWarnings() {
        val result = LiuYaoChartEngine.build(MockChartInput.qianMovingFirst)
        // 第一版历法精度提示应存在
        assertTrue(result.warnings.isNotEmpty())
    }
}
