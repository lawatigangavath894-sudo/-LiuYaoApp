package com.liuyao.paipan.domain.engine

import com.nlf.calendar.Solar
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 四柱反查服务:由(年柱/月柱/日柱/时柱)反查匹配的真实公历时间候选。
 *
 * 规则交给 6tail lunar 的"精确"四柱(立春定年、节气定月、五鼠遁时柱),不自造。
 * 反查在给定年份范围内按日扫描;同一天匹配的时柱对应一个时辰候选。
 * 有界:范围默认当前年 ±100,候选上限 [MAX_RESULTS]。
 */
object FourPillarsReverseLookupService {

    const val MAX_RESULTS = 100

    data class Pillars(
        val year: String? = null,   // 如 "丙午";null = 不约束该柱
        val month: String? = null,
        val day: String? = null,
        val time: String? = null,
    ) {
        val isComplete: Boolean get() = year != null && month != null && day != null && time != null
        val specifiedCount: Int get() = listOf(year, month, day, time).count { it != null }
    }

    data class Candidate(
        val dateTime: LocalDateTime,
        val solarText: String,
        val lunarText: String,
        val pillarsText: String,
        val timeText: String,
    )

    data class LookupResult(
        val candidates: List<Candidate>,
        val truncated: Boolean,
        val message: String?,
    )

    /** 时辰地支 → 代表小时(子时取 0 点这一侧,够起卦用) */
    private val ZHI_HOUR = intArrayOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22)

    fun lookup(
        pillars: Pillars,
        startYear: Int,
        endYear: Int,
    ): LookupResult {
        if (pillars.specifiedCount == 0) {
            return LookupResult(emptyList(), false, "请至少选择一柱后再反查。")
        }
        val results = ArrayList<Candidate>()
        var truncated = false

        var date = LocalDate.of(startYear.coerceAtLeast(1), 1, 1)
        val end = LocalDate.of(endYear.coerceAtMost(9999), 12, 31)

        loop@ while (!date.isAfter(end)) {
            // 用正午判断当日年/月/日柱(避免子时跨日干扰日柱判断)
            val noon = Solar.fromYmdHms(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0).lunar
            val yOk = pillars.year == null || pillars.year == noon.yearInGanZhiExact
            val dOk = pillars.day == null || pillars.day == noon.dayInGanZhiExact
            val mOk = pillars.month == null || pillars.month == noon.monthInGanZhiExact
            if (yOk && mOk && dOk) {
                // 对每个时辰判断时柱
                for (zhiIdx in 0..11) {
                    val hour = ZHI_HOUR[zhiIdx]
                    val ln = Solar.fromYmdHms(date.year, date.monthValue, date.dayOfMonth, hour, 0, 0).lunar
                    if (pillars.time == null || pillars.time == ln.timeInGanZhi) {
                        results.add(toCandidate(date, hour, ln))
                        if (results.size >= MAX_RESULTS) { truncated = true; break@loop }
                    }
                }
            }
            date = date.plusDays(1)
        }

        val msg = when {
            results.isEmpty() -> "未找到匹配时间,请扩大年份范围或检查四柱。"
            truncated -> "候选较多,仅显示前 $MAX_RESULTS 条,请缩小范围或补充四柱。"
            !pillars.isComplete -> "条件不足,候选较多,建议补全四柱。"
            else -> null
        }
        return LookupResult(results, truncated, msg)
    }

    private fun toCandidate(date: LocalDate, hour: Int, lunar: com.nlf.calendar.Lunar): Candidate {
        val dt = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, hour, 0)
        val pillarsText = "${lunar.yearInGanZhiExact} ${lunar.monthInGanZhiExact} ${lunar.dayInGanZhiExact} ${lunar.timeInGanZhi}"
        return Candidate(
            dateTime = dt,
            solarText = "%04d-%02d-%02d %02d:%02d".format(date.year, date.monthValue, date.dayOfMonth, hour, 0),
            lunarText = "${lunar.yearInGanZhi}年 ${lunar.monthInChinese}月${lunar.dayInChinese}",
            pillarsText = pillarsText,
            timeText = "${lunar.timeZhi}时",
        )
    }
}
