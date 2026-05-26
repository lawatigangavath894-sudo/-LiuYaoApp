package com.liuyao.paipan.domain.model

import java.time.LocalDateTime

sealed class DivinationMethod(val cn: String) {
    data object SolarTime : DivinationMethod("正时")
    data object SelectedTime : DivinationMethod("选择时间")
    data class Coin(val tosses: List<Int>) : DivinationMethod("铜钱")
    data class Manual(val raw: String) : DivinationMethod("手动")
}

enum class DivinationCategory(val cn: String) {
    CAREER("工作求职"),
    WEALTH("财运"),
    MARRIAGE("婚姻感情"),
    HEALTH("疾病健康"),
    STUDY("学业考试"),
    FAME("求名文书"),
    LOST("寻物失物"),
    TRAVEL("行人出行"),
    LAWSUIT("官非诉讼"),
    PREGNANCY("孕产"),
    HOUSE("房宅"),
    COOPERATION("合作"),
    FORTUNE("运势"),
    OTHER("其他");

    companion object {
        fun fromName(name: String?): DivinationCategory =
            entries.firstOrNull { it.name == name } ?: OTHER

        fun cnOf(name: String?): String = fromName(name).cn
    }
}

data class ChartQuestion(
    val text: String,
    val category: DivinationCategory? = null,
    val askerNote: String? = null,
)

data class LiuYaoChart(
    val id: String,
    val question: String,
    val category: DivinationCategory?,
    val dateTime: LocalDateTime,
    val yearGanZhi: GanZhi,
    val monthGanZhi: GanZhi,
    val dayGanZhi: GanZhi,
    val hourGanZhi: GanZhi,
    val xunKong: List<EarthlyBranch>,
    val originalHexagram: Hexagram,
    val changedHexagram: Hexagram?,
    val palace: Palace,
    val isSixClash: Boolean,
    val isSixCombine: Boolean,
    val lines: List<YaoLine>,
    val worldLineIndex: Int,
    val responseLineIndex: Int,
    val method: DivinationMethod,
    val notes: List<String>,
) {
    val worldLine: YaoLine get() = lines.firstOrNull { it.index == worldLineIndex } ?: lines.first()
    val responseLine: YaoLine get() = lines.firstOrNull { it.index == responseLineIndex } ?: lines.first()
    val movingLines: List<YaoLine> get() = lines.filter { it.isMoving }
    val monthBranch: EarthlyBranch get() = monthGanZhi.branch
    val dayBranch: EarthlyBranch get() = dayGanZhi.branch

    fun linesOf(kin: SixKin): List<YaoLine> = lines.filter { it.sixKin == kin }
}
