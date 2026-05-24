package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.model.ElementRelation
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.YaoLine
import com.liuyao.paipan.domain.rule.RuleCondition

/**
 * 条件求值器:判断单个 [RuleCondition] 是否在当前 [LiuYaoChart] 下成立。
 *
 * 第一版支持 17 种条件(见 README/需求);其余条件([RuleCondition.AtPosition]、
 * [RuleCondition.HiddenSpiritPresent]、[RuleCondition.FlyingSuppressesHidden])
 * 返回 [Outcome.unsupported],由匹配器决定如何对待(本版按"不计入命中也不计失败"处理)。
 *
 * 求值不依赖 UI / DB,纯函数,独立可测。
 */
class RuleConditionEvaluator(
    private val chart: LiuYaoChart,
    private val usefulGod: UsefulGod,
) {

    /**
     * @param matched   是否命中
     * @param lines     牵涉爻位(1..6)
     * @param supported 该条件类型本版是否支持(不支持时 matched 无意义)
     */
    data class Outcome(
        val matched: Boolean,
        val lines: List<Int>,
        val supported: Boolean = true,
    ) {
        companion object {
            val unsupported = Outcome(matched = false, lines = emptyList(), supported = false)
            fun hit(lines: List<Int>) = Outcome(true, lines)
            fun miss() = Outcome(false, emptyList())
        }
    }

    /** 当前用神对应的爻(以世为用→世爻;某六亲→该六亲诸爻;不上卦→空) */
    private fun usefulGodLines(): List<YaoLine> = when (val u = usefulGod) {
        is UsefulGod.WorldSelf -> listOf(chart.worldLine)
        is UsefulGod.Kin -> chart.linesOf(u.kin)
    }

    fun evaluate(condition: RuleCondition): Outcome = when (condition) {

        // 1. 六亲出现
        is RuleCondition.KinPresent -> {
            val ls = chart.linesOf(condition.kin)
            if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
        }

        // 2. 六亲不现
        is RuleCondition.KinAbsent -> {
            val ls = chart.linesOf(condition.kin)
            if (ls.isEmpty()) Outcome.hit(emptyList()) else Outcome.miss()
        }

        // 3. 用神发动
        RuleCondition.UseGodMoving -> {
            val ls = usefulGodLines().filter { it.isMoving }
            if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
        }

        // 4. 用神空亡
        RuleCondition.UseGodVoid -> usefulGodStatusHit { it.status.isVoid }

        // 5. 用神月破
        RuleCondition.UseGodMonthBroken -> usefulGodStatusHit { it.status.isMonthBroken }

        // 6. 用神日冲
        RuleCondition.UseGodDayClashed -> usefulGodStatusHit { it.status.isDayClashed }

        // 7. 用神得月建
        RuleCondition.UseGodSupportedByMonth -> usefulGodStatusHit { it.status.isSupportedByMonth }

        // 8. 用神得日辰
        RuleCondition.UseGodSupportedByDay -> usefulGodStatusHit { it.status.isSupportedByDay }

        // 9. 世爻空亡
        RuleCondition.WorldVoid ->
            if (chart.worldLine.status.isVoid) Outcome.hit(listOf(chart.worldLineIndex)) else Outcome.miss()

        // 10. 应爻空亡
        RuleCondition.ResponseVoid ->
            if (chart.responseLine.status.isVoid) Outcome.hit(listOf(chart.responseLineIndex)) else Outcome.miss()

        // 11. 世应相生(任一方向相生即算)
        RuleCondition.WorldResponseGenerate -> {
            val w = chart.worldLine.element
            val r = chart.responseLine.element
            val rel = w.relationTo(r)
            val gen = rel == ElementRelation.GENERATES || rel == ElementRelation.GENERATED_BY
            if (gen) Outcome.hit(listOf(chart.worldLineIndex, chart.responseLineIndex)) else Outcome.miss()
        }

        // 12. 世应相克(任一方向相克即算)
        RuleCondition.WorldResponseRestrain -> {
            val w = chart.worldLine.element
            val r = chart.responseLine.element
            val rel = w.relationTo(r)
            val res = rel == ElementRelation.RESTRAINS || rel == ElementRelation.RESTRAINED_BY
            if (res) Outcome.hit(listOf(chart.worldLineIndex, chart.responseLineIndex)) else Outcome.miss()
        }

        // 13. 动爻化回头生(变爻生本爻)
        RuleCondition.BackGenerate -> backRelationHit(generate = true)

        // 14. 动爻化回头克(变爻克本爻)
        RuleCondition.BackRestrain -> backRelationHit(generate = false)

        // 15. 六神出现(可指定落在某目标上)
        is RuleCondition.SixGodPresent -> {
            val target = condition.onTarget
            val candidates = if (target == null) chart.lines else targetLines(target)
            val ls = candidates.filter { it.sixGod == condition.sixGod }
            if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
        }

        // 16. 地支冲
        is RuleCondition.BranchClash -> {
            if (condition.a != null && condition.b != null) {
                val present = chart.lines.filter { it.branch == condition.a || it.branch == condition.b }
                val ok = present.map { it.branch }.toSet().containsAll(listOf(condition.a, condition.b))
                if (ok) Outcome.hit(present.map { it.index }) else Outcome.miss()
            } else {
                // 未指定具体地支:任一爻逢冲(日冲/月破皆视为冲)
                val ls = chart.lines.filter { it.status.isDayClashed || it.status.isClashed || it.status.isMonthBroken }
                if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
            }
        }

        // 17. 地支合
        is RuleCondition.BranchCombine -> {
            if (condition.a != null && condition.b != null) {
                val present = chart.lines.filter { it.branch == condition.a || it.branch == condition.b }
                val ok = present.map { it.branch }.toSet().containsAll(listOf(condition.a, condition.b))
                if (ok) Outcome.hit(present.map { it.index }) else Outcome.miss()
            } else {
                val ls = chart.lines.filter { it.status.isCombined }
                if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
            }
        }

        // —— 本版未支持的条件 ——
        is RuleCondition.AtPosition,
        is RuleCondition.HiddenSpiritPresent,
        is RuleCondition.FlyingSuppressesHidden,
        -> Outcome.unsupported
    }

    // ───────── 辅助 ─────────

    private inline fun usefulGodStatusHit(pred: (YaoLine) -> Boolean): Outcome {
        val ls = usefulGodLines().filter(pred)
        return if (ls.isNotEmpty()) Outcome.hit(ls.map { it.index }) else Outcome.miss()
    }

    /** 动爻与其变爻的回头生/克 */
    private fun backRelationHit(generate: Boolean): Outcome {
        val hits = chart.movingLines.mapNotNull { line ->
            val changed = line.changedLine ?: return@mapNotNull null
            val rel = line.element.relationTo(changed.element) // 本爻 → 变爻
            val ok = if (generate) {
                rel == ElementRelation.GENERATED_BY  // 变爻生本爻 = 本爻被变爻所生
            } else {
                rel == ElementRelation.RESTRAINED_BY // 变爻克本爻 = 本爻被变爻所克
            }
            if (ok) line.index else null
        }
        return if (hits.isNotEmpty()) Outcome.hit(hits) else Outcome.miss()
    }

    private fun targetLines(target: com.liuyao.paipan.domain.rule.RuleTarget): List<YaoLine> =
        when (target.type) {
            com.liuyao.paipan.domain.rule.RuleTarget.Type.WORLD -> listOf(chart.worldLine)
            com.liuyao.paipan.domain.rule.RuleTarget.Type.RESPONSE -> listOf(chart.responseLine)
            com.liuyao.paipan.domain.rule.RuleTarget.Type.SPECIFIC_KIN ->
                target.kin?.let { chart.linesOf(it) } ?: emptyList()
            com.liuyao.paipan.domain.rule.RuleTarget.Type.SPECIFIC_POSITION ->
                target.position?.let { p -> chart.lines.filter { it.index == p } } ?: emptyList()
            com.liuyao.paipan.domain.rule.RuleTarget.Type.USE_GOD -> usefulGodLines()
            com.liuyao.paipan.domain.rule.RuleTarget.Type.WHOLE_CHART -> chart.lines
        }
}
