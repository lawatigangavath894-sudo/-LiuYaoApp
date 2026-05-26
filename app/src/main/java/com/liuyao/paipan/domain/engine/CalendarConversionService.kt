package com.liuyao.paipan.domain.engine

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object CalendarConversionService {
    val ZONE: ZoneId = ZoneId.of("Asia/Shanghai")

    data class LunarInfo(
        val lunarYear: Int,
        val lunarMonth: Int,
        val isLeapMonth: Boolean,
        val lunarDay: Int,
        val yearGanZhi: String,
        val monthInChinese: String,
        val dayInChinese: String,
        val timeZhiIndex: Int,
    )

    fun lunarInfoOf(dt: LocalDateTime): LunarInfo {
        val solar = Solar.fromYmdHms(dt.year, dt.monthValue, dt.dayOfMonth, dt.hour, dt.minute, 0)
        val lunar = solar.lunar
        val rawMonth = lunar.month
        return LunarInfo(
            lunarYear = lunar.year,
            lunarMonth = kotlin.math.abs(rawMonth),
            isLeapMonth = rawMonth < 0,
            lunarDay = lunar.day,
            yearGanZhi = lunar.yearInGanZhi,
            monthInChinese = lunar.monthInChinese,
            dayInChinese = lunar.dayInChinese,
            timeZhiIndex = zhiIndex(lunar.timeZhi),
        )
    }

    fun lunarToSolar(year: Int, month: Int, isLeap: Boolean, day: Int, hour: Int, minute: Int): LocalDateTime {
        val m = if (isLeap) -month else month
        val lunar = Lunar.fromYmdHms(year, m, day, hour, minute, 0)
        val s = lunar.solar
        return LocalDateTime.of(s.year, s.month, s.day, s.hour, s.minute)
    }

    fun leapMonthOf(lunarYear: Int): Int =
        callLunarUtilInt("getLeapMonth", lunarYear) ?: 0

    fun lunarMonthDays(lunarYear: Int, month: Int, isLeap: Boolean): Int {
        val m = if (isLeap) -month else month
        return callLunarUtilInt("getDaysOfMonth", lunarYear, m)
            ?: calculateMonthDaysBySolarDiff(lunarYear, m)
    }

    fun yearBranchNumber(dt: LocalDateTime): Int {
        val lunar = Solar.fromYmdHms(dt.year, dt.monthValue, dt.dayOfMonth, dt.hour, dt.minute, 0).lunar
        return zhiIndex(lunar.yearZhi) + 1
    }

    fun hourBranchNumber(dt: LocalDateTime): Int = ((dt.hour + 1) / 2) % 12 + 1

    private fun callLunarUtilInt(methodName: String, vararg args: Int): Int? =
        runCatching {
            val parameterTypes = Array(args.size) { Int::class.javaPrimitiveType }
            val method = com.nlf.calendar.util.LunarUtil::class.java.getMethod(methodName, *parameterTypes)
            method.invoke(null, *args.toTypedArray()) as? Int
        }.getOrNull()

    private fun calculateMonthDaysBySolarDiff(lunarYear: Int, lunarMonth: Int): Int {
        val current = Lunar.fromYmdHms(lunarYear, lunarMonth, 1, 0, 0, 0).solar
        val absMonth = kotlin.math.abs(lunarMonth)
        val nextYear = if (absMonth >= 12) lunarYear + 1 else lunarYear
        val nextMonth = if (absMonth >= 12) 1 else absMonth + 1
        val next = Lunar.fromYmdHms(nextYear, nextMonth, 1, 0, 0, 0).solar
        val currentDate = LocalDate.of(current.year, current.month, current.day)
        val nextDate = LocalDate.of(next.year, next.month, next.day)
        return ChronoUnit.DAYS.between(currentDate, nextDate).toInt().coerceIn(29, 30)
    }

    private val ZHI = listOf(
        "\u5b50",
        "\u4e11",
        "\u5bc5",
        "\u536f",
        "\u8fb0",
        "\u5df3",
        "\u5348",
        "\u672a",
        "\u7533",
        "\u9149",
        "\u620c",
        "\u4ea5",
    )

    private fun zhiIndex(zhi: String): Int = ZHI.indexOf(zhi).coerceAtLeast(0)
}
