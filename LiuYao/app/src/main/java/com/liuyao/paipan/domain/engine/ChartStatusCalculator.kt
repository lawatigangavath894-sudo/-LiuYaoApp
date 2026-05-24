package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.ElementRelation
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.LineStatus
import com.liuyao.paipan.domain.model.StrengthLevel

/**
 * 12. 爻状态计算。
 *
 * 针对每个爻,结合月令(月支)、日辰(日支)与旬空,给出:
 *  旬空、月破、日冲、月生、日生、(日)合、(爻间)冲、刑、害,以及旺衰等级。
 *
 * 说明:
 *  - "冲/合/刑/害"在严格断卦中既看与日月之关系,也看爻与爻之间关系。
 *    本版以"与日辰"的关系为主轴(最常用),爻间关系作为可选输入预留。
 *    TODO: 完整的爻间两两刑冲合害、三合局/三会局、暗动判定等后续扩展。
 */
object ChartStatusCalculator {

    /**
     * @param branch       本爻地支
     * @param monthBranch  月支(月令)
     * @param dayBranch    日支(日辰)
     * @param xunKong      旬空地支
     */
    fun statusOf(
        branch: EarthlyBranch,
        monthBranch: EarthlyBranch,
        dayBranch: EarthlyBranch,
        xunKong: List<EarthlyBranch>,
    ): LineStatus {
        val element = branch.element

        val isVoid = branch in xunKong
        val isMonthBroken = monthBranch.clashWith() == branch     // 月破:被月支所冲
        val isDayClashed = dayBranch.clashWith() == branch        // 日冲:被日辰所冲

        // 生扶:日/月五行生本爻,或与本爻比和(同气相助)
        val isSupportedByMonth = supports(monthBranch, branch)
        val isSupportedByDay = supports(dayBranch, branch)

        // 与日辰的合
        val isCombined = dayBranch.combineWith() == branch
        // 与日辰的冲(等同 isDayClashed,这里 isClashed 表示"逢冲"的总标志)
        val isClashed = isDayClashed
        // 与日辰的刑、害
        val isPunished = EarthlyBranch.isPunish(branch, dayBranch) && branch != dayBranch
        val isHarmed = EarthlyBranch.isHarm(branch, dayBranch)

        val strength = StrengthLevel.byMonth(monthBranch.element, element)

        return LineStatus(
            isVoid = isVoid,
            isMonthBroken = isMonthBroken,
            isDayClashed = isDayClashed,
            isCombined = isCombined,
            isClashed = isClashed,
            isPunished = isPunished,
            isHarmed = isHarmed,
            isSupportedByMonth = isSupportedByMonth,
            isSupportedByDay = isSupportedByDay,
            strength = strength,
        )
    }

    /** 便捷重载:直接用爻纳甲 */
    fun statusOf(
        naJia: GanZhi,
        monthBranch: EarthlyBranch,
        dayBranch: EarthlyBranch,
        xunKong: List<EarthlyBranch>,
    ): LineStatus = statusOf(naJia.branch, monthBranch, dayBranch, xunKong)

    /** source 是否生扶 target(生我或比和) */
    private fun supports(source: EarthlyBranch, target: EarthlyBranch): Boolean {
        return when (target.element.relationTo(source.element)) {
            ElementRelation.GENERATED_BY -> true // 源生本爻
            ElementRelation.SAME -> true         // 同气比和
            else -> false
        }
    }
}
