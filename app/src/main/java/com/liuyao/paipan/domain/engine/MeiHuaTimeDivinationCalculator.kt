package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.DivinationMethod
import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.YinYang
import java.time.LocalDateTime

data class MeiHuaTimeResult(
    val upperNumber: Int,
    val lowerNumber: Int,
    val movingLine: Int,
    val upperTrigram: EightTrigram,
    val lowerTrigram: EightTrigram,
    val lines: List<YinYang>,
    val moving: List<Boolean>,
)

object MeiHuaTimeDivinationCalculator {
    /**
     * 梅花易数时间起卦(标准规则,正时与择时共用)。
     *
     * 不直接使用公历 year/month/day/hour 数值,而是换算:
     *  - 年支数:子=1…亥=12(由地支序 +1)
     *  - 农历月数:正月=1…十二月=12(闰月按本月数字)
     *  - 农历日数:初一=1…三十=30
     *  - 时辰数:子时=1…亥时=12(每两小时一支,子时含 23:00–00:59)
     *
     * 上卦 =(年支数+农历月+农历日) % 8,余 0 取 8
     * 下卦 =(年支数+农历月+农历日+时辰数) % 8,余 0 取 8
     * 动爻 =(年支数+农历月+农历日+时辰数) % 6,余 0 取 6
     * 八卦数:1乾 2兑 3离 4震 5巽 6坎 7艮 8坤;动爻自下而上 1初…6上。
     */
    fun calculate(dateTime: LocalDateTime): MeiHuaTimeResult {
        // 真实农历信息(经 6tail lunar)。年支数、农历月、农历日、时辰数。
        val lunar = CalendarConversionService.lunarInfoOf(dateTime)
        val yearBranchNumber = CalendarConversionService.yearBranchNumber(dateTime) // 子=1..亥=12
        val lunarMonth = lunar.lunarMonth   // 闰月按本月数字
        val lunarDay = lunar.lunarDay
        val hourNumber = CalendarConversionService.hourBranchNumber(dateTime) // 子=1..亥=12

        val dateSum = yearBranchNumber + lunarMonth + lunarDay
        val fullSum = dateSum + hourNumber

        val upperNumber = normalizedRemainder(dateSum, 8)
        val lowerNumber = normalizedRemainder(fullSum, 8)
        val movingLine = normalizedRemainder(fullSum, 6)
        val upper = trigramOf(upperNumber)
        val lower = trigramOf(lowerNumber)
        return MeiHuaTimeResult(
            upperNumber = upperNumber,
            lowerNumber = lowerNumber,
            movingLine = movingLine,
            upperTrigram = upper,
            lowerTrigram = lower,
            lines = lower.lines + upper.lines,
            moving = (1..6).map { it == movingLine },
        )
    }

    fun chartInput(
        dateTime: LocalDateTime,
        question: String,
        category: DivinationCategory,
        method: DivinationMethod,
    ): ChartInput {
        val result = calculate(dateTime)
        return ChartInput(
            dateTime = dateTime,
            lines = result.lines,
            moving = result.moving,
            question = question,
            method = method,
            category = category,
        )
    }

    private fun normalizedRemainder(value: Int, divisor: Int): Int {
        val remainder = value % divisor
        return if (remainder == 0) divisor else remainder
    }

    private fun trigramOf(number: Int): EightTrigram = when (number) {
        1 -> EightTrigram.QIAN
        2 -> EightTrigram.DUI
        3 -> EightTrigram.LI
        4 -> EightTrigram.ZHEN
        5 -> EightTrigram.XUN
        6 -> EightTrigram.KAN
        7 -> EightTrigram.GEN
        8 -> EightTrigram.KUN
        else -> error("Invalid MeiHua trigram number: $number")
    }
}
