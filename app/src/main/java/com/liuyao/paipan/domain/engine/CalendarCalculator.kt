package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.HeavenlyStem
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.floor
import kotlin.math.sin

/**
 * 历法计算:公历 → 年月日时四柱干支,以及旬空。
 *
 * 精度与边界说明(本轮第一版):
 *  - 日柱:以儒略日连续推算,锚点 2026-05-22 = 丙申(60 甲子序 32),精确可靠。
 *  - 月柱:以"节"分月,用低精度太阳黄经公式定节气(误差约 0.01°),
 *    临近节气交接的当天可能有 ±1 日误差。TODO: 接入高精度节气表/天文算法。
 *  - 年柱:以"立春"换年。本轮用太阳黄经判断是否已过立春(315°),
 *    再决定年干支。TODO: 与权威万年历逐年校验。
 *  - 时柱:子时以 23:00 起;本轮按 23:00–01:00 为子时的"晚子时"简化,
 *    未做"早晚子时分日"。TODO: 处理晚子时是否进位日柱的流派差异。
 *  - 真太阳时:本轮不做经度/均时差校正,按输入本地钟表时间。TODO。
 */
object CalendarCalculator {

    data class FourPillars(
        val year: GanZhi,
        val month: GanZhi,
        val day: GanZhi,
        val hour: GanZhi,
    )

    /** 计算四柱 */
    fun fourPillars(dateTime: LocalDateTime): FourPillars {
        val date = dateTime.toLocalDate()
        val dayIdx = dayIndex(date)
        val jdNoon = julianDay(date) + 0.5
        val monthBranch = monthBranch(jdNoon)
        val yearIdx = yearIndex(dateTime, jdNoon)

        val yearGz = GanZhi.fromIndex(yearIdx)
        val monthGz = monthGanZhi(yearIdx % 10, monthBranch)
        val dayGz = GanZhi.fromIndex(dayIdx)
        val hourBranch = hourBranch(dateTime.hour)
        val hourGz = hourGanZhi(dayIdx % 10, hourBranch)

        return FourPillars(yearGz, monthGz, dayGz, hourGz)
    }

    /** 旬空:本旬(以日柱起)未占用的两个地支 */
    fun xunKong(dayGanZhi: GanZhi): List<EarthlyBranch> {
        val dayIdx = dayGanZhi.sexagenaryIndex
        require(dayIdx >= 0) { "非法日柱干支: ${dayGanZhi.cn}" }
        val head = dayIdx - dayIdx % 10           // 旬首(甲)的序号
        val used = (0 until 10).map { (head + it) % 12 }.toSet()
        return (0 until 12).filter { it !in used }.map { EarthlyBranch.fromIndex(it) }
    }

    // ───────────────────────── 历法内部实现 ─────────────────────────

    /** 儒略日(整数,正午为基准用 +0.5) */
    fun julianDay(date: LocalDate): Long {
        val y = date.year; val m = date.monthValue; val d = date.dayOfMonth
        val a = (14 - m) / 12
        val yy = y + 4800 - a
        val mm = m + 12 * a - 3
        return d + (153L * mm + 2) / 5 + 365L * yy + yy / 4 - yy / 100 + yy / 400 - 32045
    }

    /** 日柱 60 甲子序号(0..59)。锚点 2026-05-22 = 丙申(32)。 */
    fun dayIndex(date: LocalDate): Int {
        val anchor = julianDay(LocalDate.of(2026, 5, 22))
        val diff = julianDay(date) - anchor
        return (((32 + diff) % 60 + 60) % 60).toInt()
    }

    /** 太阳视黄经(度),低精度公式 */
    fun sunLongitude(jd: Double): Double {
        val n = jd - 2451545.0
        val l = (280.460 + 0.9856474 * n) % 360
        val g = Math.toRadians((357.528 + 0.9856003 * n) % 360)
        val lam = l + 1.915 * sin(g) + 0.020 * sin(2 * g)
        return ((lam % 360) + 360) % 360
    }

    /** 月支:以"节"定月,立春(黄经315°)起寅月,每 30° 一节 */
    fun monthBranch(jdNoon: Double): EarthlyBranch {
        val lam = sunLongitude(jdNoon)
        val idx = floor(((lam - 315 + 360) % 360) / 30.0).toInt() // 0=寅
        return EarthlyBranch.fromIndex((idx + 2) % 12)            // 寅=index2
    }

    /** 年柱序号:以立春换年。立春前仍属上一年。 */
    fun yearIndex(dateTime: LocalDateTime, jdNoon: Double): Int {
        val lam = sunLongitude(jdNoon)
        // 黄经在 [315,360)∪[0,315) 的判断:立春点为 315°。
        // 公历 1-2 月若尚未过立春(太阳黄经 < 315 且 > 某阈),归前一年。
        var y = dateTime.year
        val month = dateTime.monthValue
        // 简化:1 月、2 月上旬通常未过立春 → 前一年。用黄经辅助判断。
        val beforeLiChun = (month <= 2 && lam < 315.0 && lam > 270.0)
        if (beforeLiChun) y -= 1
        // 公元年 → 干支序号:公元 4 年为甲子(序0)
        return ((y - 4) % 60 + 60) % 60
    }

    /** 五虎遁:由年干 + 月支 → 月柱 */
    fun monthGanZhi(yearStem: Int, monthBranch: EarthlyBranch): GanZhi {
        // 甲己之年丙作首… 寅月之干:
        val firstStemOfYin = intArrayOf(2, 4, 6, 8, 0)[yearStem % 5] // 丙戊庚壬甲
        val steps = (monthBranch.ordinal - 2 + 12) % 12             // 距寅月的月数
        val stem = (firstStemOfYin + steps) % 10
        return GanZhi(HeavenlyStem.fromIndex(stem), monthBranch)
    }

    /** 五鼠遁:由日干 + 时支 → 时柱 */
    fun hourGanZhi(dayStem: Int, hourBranch: EarthlyBranch): GanZhi {
        val firstStemOfZi = intArrayOf(0, 2, 4, 6, 8)[dayStem % 5] // 甲丙戊庚壬
        val stem = (firstStemOfZi + hourBranch.ordinal) % 10
        return GanZhi(HeavenlyStem.fromIndex(stem), hourBranch)
    }

    /** 时支:子时 23–1,丑 1–3 … 每两小时一支 */
    fun hourBranch(hour: Int): EarthlyBranch = EarthlyBranch.fromIndex(((hour + 1) / 2) % 12)
}
