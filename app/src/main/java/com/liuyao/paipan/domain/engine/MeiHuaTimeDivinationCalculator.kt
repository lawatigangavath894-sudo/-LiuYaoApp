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
    fun calculate(dateTime: LocalDateTime): MeiHuaTimeResult {
        val upperNumber = normalizedRemainder(
            dateTime.year + dateTime.monthValue + dateTime.dayOfMonth,
            8,
        )
        val lowerSeed = dateTime.year + dateTime.monthValue + dateTime.dayOfMonth + dateTime.hour
        val lowerNumber = normalizedRemainder(lowerSeed, 8)
        val movingLine = normalizedRemainder(lowerSeed, 6)
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
