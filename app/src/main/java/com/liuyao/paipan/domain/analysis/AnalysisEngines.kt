package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.YaoLine

data class AnalysisItem(
    val title: String,
    val body: String,
    val reason: String,
    val layer: MatchLayer = MatchLayer.SIDE_REFERENCE,
)

object ShenShaAnalysisEngine {
    fun analyze(chart: LiuYaoChart, lock: AnalysisLock): List<AnalysisItem> =
        chart.lines
            .filter { it.index in lock.keyLineIndexes && it.sixGod in lock.relatedSixGods }
            .map { line ->
                AnalysisItem(
                    title = "${linePositionName(line.index)} ${line.sixGod.displayName()}",
                    body = shenShaMeaning(lock.category, line.sixGod),
                    reason = "关联${lineReason(line, lock)}，不展示无关爻位神煞。",
                    layer = if (line.sixGod.displayName() in lock.relatedShenSha) MatchLayer.RISK_WARNING else MatchLayer.SIDE_REFERENCE,
                )
            }

    private fun shenShaMeaning(category: DivinationCategory, god: SixGod): String = when (god) {
        SixGod.WHITE_TIGER -> if (category == DivinationCategory.HEALTH || category == DivinationCategory.PREGNANCY) {
            "白虎与病伤、血光、生产风险相关，本占作为风险信号观察。"
        } else {
            "白虎仅在关键爻上作为压力、冲突或损伤的旁参考。"
        }
        SixGod.BLACK_TORTOISE -> "玄武在当前关键爻上提示隐情、拖延、失物或暗处信息。"
        SixGod.VERMILION_BIRD -> "朱雀在关键爻上提示消息、文书、考试表达或口舌信息。"
        SixGod.AZURE_DRAGON -> "青龙在关键爻上提示顺遂、喜庆、财喜或有利助缘。"
        SixGod.HOOK_EARTH -> "勾陈在关键爻上提示阻滞、牵连、房宅土地或迟缓。"
        SixGod.SOARING_SNAKE -> "腾蛇在关键爻上提示虚惊、反复、疑虑或纠缠。"
    }
}

object StrengthAnalysisEngine {
    fun analyze(chart: LiuYaoChart, lock: AnalysisLock): List<AnalysisItem> =
        chart.lines
            .filter { it.index in lock.keyLineIndexes }
            .map { line ->
                val status = buildList {
                    add("旺衰${line.status.strength.displayName()}")
                    if (line.status.isSupportedByMonth) add("得月扶")
                    if (line.status.isSupportedByDay) add("得日扶")
                    if (line.status.isVoid) add("旬空")
                    if (line.status.isMonthBroken) add("月破")
                    if (line.status.isDayClashed) add("日冲")
                    if (line.status.isCombined) add("合")
                    if (line.status.isClashed) add("冲")
                    if (line.status.isPunished) add("刑")
                    if (line.status.isHarmed) add("害")
                    if (line.isMoving) add("动爻")
                    if (line.changedLine != null) add("变爻")
                }.joinToString("、")
                AnalysisItem(
                    title = "${linePositionName(line.index)} ${line.sixKin.displayName()} ${line.naJia.displayName()}",
                    body = status,
                    reason = "此爻为${lineReason(line, lock)}，与“${lock.mainVariable}”直接相关。",
                    layer = MatchLayer.CONDITION,
                )
            }
}

object MethodAnalysisEngine {
    fun analyze(lock: AnalysisLock): List<AnalysisItem> =
        lock.knowledgeSnippets
            .filter { snippet ->
                val text = snippet.originalText
                lock.focusKeywords.any { text.contains(it) } &&
                    listOf("占法", "取用", "用神", "断", "占").any { text.contains(it) }
            }
            .take(6)
            .map { snippet ->
                AnalysisItem(
                    title = snippet.sectionTitle ?: snippet.sourceName,
                    body = snippet.originalText.take(180),
                    reason = "匹配关键词：${snippet.matchedKeywords.joinToString("、")}",
                    layer = MatchLayer.PROCESS,
                )
            }

}

object XiangAnalysisEngine {
    fun analyze(chart: LiuYaoChart, lock: AnalysisLock): List<AnalysisItem> =
        chart.lines
            .filter { it.index in lock.keyLineIndexes }
            .map { line ->
                val extra = buildList {
                    line.hiddenSpirit?.let { add("伏神${it.sixKin.displayName()}${it.naJia.displayName()}") }
                    line.flyingSpirit?.let { add("飞神${it.sixKin.displayName()}${it.naJia.displayName()}") }
                    line.changedLine?.let { add("变出${it.sixKin.displayName()}${it.naJia.displayName()}") }
                }.joinToString("；").ifBlank { "无伏飞或变爻补充" }
                AnalysisItem(
                    title = "${linePositionName(line.index)}取象",
                    body = "${line.sixKin.displayName()}、${line.sixGod.displayName()}、${line.naJia.displayName()}，$extra。",
                    reason = "取象来自${lineReason(line, lock)}，只解释当前占事相关爻。",
                    layer = if (line.index in lock.usefulGodLineIndexes) MatchLayer.MAIN_RESULT else MatchLayer.SIDE_REFERENCE,
                )
            }
}

object CaseSimilarityEngine {
    fun placeholder(lock: AnalysisLock): List<AnalysisItem> =
        listOf(
            AnalysisItem(
                title = "暂无同类案例",
                body = "当前仅保留同类占事、相同主变量、相近用神结构的案例入口；未找到相关案例时不展示全量案例。",
                reason = "${lock.category.displayName()} / ${lock.mainVariable}",
                layer = MatchLayer.INSUFFICIENT_DATA,
            ),
        )
}

fun lineReason(line: YaoLine, lock: AnalysisLock): String = when {
    line.index == lock.worldLineIndex -> "世爻（本人/自身状态）"
    line.index == lock.responseLineIndex -> "应爻（对方/外部对象）"
    line.index in lock.usefulGodLineIndexes && line.sixKin == lock.primaryUsefulGod -> "主用神${line.sixKin.displayName()}"
    line.index in lock.usefulGodLineIndexes -> "辅助用神${line.sixKin.displayName()}"
    line.index in lock.movingLineIndexes -> "动爻"
    line.index in lock.hiddenSpiritLineIndexes -> "伏神相关爻"
    line.index in lock.flyingSpiritLineIndexes -> "飞神相关爻"
    line.index in lock.voidLineIndexes -> "空亡关键爻"
    line.index in lock.monthBrokenLineIndexes -> "月破关键爻"
    line.index in lock.dayClashedLineIndexes -> "日冲关键爻"
    else -> "分析锁定关键爻"
}
