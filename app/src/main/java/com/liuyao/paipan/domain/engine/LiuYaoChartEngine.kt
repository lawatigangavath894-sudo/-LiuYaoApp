package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.ChangedLine
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.YaoLine

/**
 * 六爻排盘引擎(第一版)。
 *
 * 管线:
 *  时间 → 四柱 / 旬空(CalendarCalculator)
 *  六爻 → 本卦 / 变卦(HexagramFactory / ChangedHexagramCalculator)
 *  本卦 → 卦宫 / 世应 / 纳甲 / 六亲(各 Calculator)
 *  日干 → 六神(SixGod.sequenceByDayStem)
 *  缺六亲 → 伏神 / 飞神(HiddenFlyingSpiritCalculator)
 *  每爻 → 爻状态(ChartStatusCalculator)
 *  组装 → LiuYaoChart
 *
 * 全程无 UI / 数据库 / 断语依赖,纯函数式,便于测试与扩展。
 */
object LiuYaoChartEngine {

    fun build(input: ChartInput): ChartBuildResult {
        val warnings = mutableListOf<String>()

        // ── 1. 四柱 / 旬空 ──
        val pillars = CalendarCalculator.fourPillars(input.dateTime)
        val xunKong = CalendarCalculator.xunKong(pillars.day)
        val monthBranch = pillars.month.branch
        val dayBranch = pillars.day.branch
        val dayStem = pillars.day.stem

        // ── 2. 本卦 / 变卦 ──
        val original = HexagramFactory.fromLines(input.lines)
        val changed = ChangedHexagramCalculator.changedOf(original, input.movingPositions)

        // ── 3. 卦宫 / 世应 / 纳甲 / 六亲 ──
        val palace = PalaceCalculator.palaceOf(original)
        val wr = WorldResponseCalculator.of(original)
        val naJia: List<GanZhi> = NaJiaCalculator.naJiaOf(original)
        val sixKins: List<SixKin> = SixKinCalculator.sixKinOf(palace.element, naJia)

        // ── 4. 六神(初爻→上爻) ──
        val sixGods: List<SixGod> = SixGod.sequenceByDayStem(dayStem)

        // ── 5. 伏神 / 飞神 ──
        val hidden = HiddenFlyingSpiritCalculator.compute(original, sixKins, naJia)

        // ── 6. 变卦纳甲 / 六亲(变爻六亲仍以本宫五行论) ──
        val changedNaJia: List<GanZhi>? = changed?.let { NaJiaCalculator.naJiaOf(it) }
        val changedSixKins: List<SixKin>? = changedNaJia?.let {
            SixKinCalculator.sixKinOf(palace.element, it)
        }

        // ── 7. 逐爻装配 ──
        val lines = (0 until 6).map { i ->
            val position = i + 1
            val isMoving = position in input.movingPositions
            val status = ChartStatusCalculator.statusOf(
                naJia = naJia[i],
                monthBranch = monthBranch,
                dayBranch = dayBranch,
                xunKong = xunKong,
            )
            val changedLine = if (isMoving && changed != null && changedNaJia != null && changedSixKins != null) {
                ChangedLine(
                    yinYang = changed.lines[i],
                    sixKin = changedSixKins[i],
                    naJia = changedNaJia[i],
                )
            } else null

            val h = hidden[position]

            YaoLine(
                index = position,
                yinYang = input.lines[i],
                isMoving = isMoving,
                sixGod = sixGods[i],
                sixKin = sixKins[i],
                naJia = naJia[i],
                element = naJia[i].branchElement,
                isWorld = position == wr.worldPosition,
                isResponse = position == wr.responsePosition,
                hiddenSpirit = h?.hiddenSpirit,
                flyingSpirit = h?.flyingSpirit,
                changedLine = changedLine,
                status = status,
            )
        }

        // ── 8. 组装 LiuYaoChart ──
        val chart = LiuYaoChart(
            id = input.id ?: generateId(input),
            question = input.question,
            category = input.category,
            dateTime = input.dateTime,
            yearGanZhi = pillars.year,
            monthGanZhi = pillars.month,
            dayGanZhi = pillars.day,
            hourGanZhi = pillars.hour,
            xunKong = xunKong,
            originalHexagram = original,
            changedHexagram = changed,
            palace = palace,
            isSixClash = original.isSixClash,
            isSixCombine = original.isSixCombine,
            lines = lines,
            worldLineIndex = wr.worldPosition,
            responseLineIndex = wr.responsePosition,
            method = input.method,
            notes = emptyList(),
        )

        // 历法精度提示(临近节气/立春)
        warnings += "月柱/年柱基于低精度节气公式,临界日可能有 ±1 日误差(见 CalendarCalculator TODO)。"

        return ChartBuildResult(chart = chart, warnings = warnings)
    }

    private fun generateId(input: ChartInput): String =
        "chart-${input.dateTime.year}${"%02d".format(input.dateTime.monthValue)}" +
            "${"%02d".format(input.dateTime.dayOfMonth)}-${input.dateTime.hour}${input.dateTime.minute}"
}
