package com.liuyao.paipan.ui.screens.chart

import com.liuyao.paipan.data.YaoLineData
import com.liuyao.paipan.data.YaoTag
import com.liuyao.paipan.domain.model.LineStatus
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.StrengthLevel
import com.liuyao.paipan.domain.model.YaoLine
import java.time.format.DateTimeFormatter

/**
 * 排盘页 UI 展示模型。把领域 [LiuYaoChart] 投影为视图友好的字段,
 * 使 ChartScreen / 各卡片 / YaoLineRow 只与展示数据耦合。
 */
data class ChartUiModel(
    val question: String,
    val gregorian: String,
    val ganZhiYear: String,
    val ganZhiMonth: String,
    val ganZhiDay: String,
    val ganZhiHour: String,
    val xunKong: String,
    val castMethod: String,
    val benHex: String,
    val bianHex: String?,
    val palace: String,
    val palaceElement: String,
    val hexNature: String,          // 六冲/六合/无
    val worldResponse: String,
    val yaoLines: List<YaoLineData>, // 由上爻(index5)→初爻(index0) 排列,便于自上而下渲染
)

object ChartUiMapper {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm")

    fun toUiModel(chart: LiuYaoChart): ChartUiModel {
        // 领域 lines 为 index0=初爻;UI 自上而下需要 上爻→初爻
        val topToBottom = chart.lines.sortedByDescending { it.index }

        return ChartUiModel(
            question = chart.question,
            gregorian = "公历 ${chart.dateTime.format(dateFmt)}",
            ganZhiYear = chart.yearGanZhi.cn,
            ganZhiMonth = chart.monthGanZhi.cn,
            ganZhiDay = chart.dayGanZhi.cn,
            ganZhiHour = chart.hourGanZhi.cn,
            xunKong = chart.xunKong.joinToString("、") { it.cn },
            castMethod = chart.method.cn,
            benHex = chart.originalHexagram.name,
            bianHex = chart.changedHexagram?.name,
            palace = chart.palace.cn,
            palaceElement = chart.palace.element.cn,
            hexNature = when {
                chart.isSixClash -> "六冲"
                chart.isSixCombine -> "六合"
                else -> "无"
            },
            worldResponse = "世${posCn(chart.worldLineIndex)}爻 · 应${posCn(chart.responseLineIndex)}爻",
            yaoLines = topToBottom.map { it.toUi() },
        )
    }

    private fun YaoLine.toUi(): YaoLineData = YaoLineData(
        position = index,
        liuShen = sixGod.cn,
        liuQin = sixKin.cn,
        ganZhi = naJia.cn,
        branch = naJia.branch.cn,
        element = element.cn,
        yang = yinYang == com.liuyao.paipan.domain.model.YinYang.YANG,
        isWorld = isWorld,
        isResponse = isResponse,
        moving = isMoving,
        changedLiuQin = changedLine?.sixKin?.cn,
        changedGanZhi = changedLine?.naJia?.cn,
        fuShen = hiddenSpirit?.let { "${it.sixKin.cn}${it.naJia.branch.cn}${it.element.cn}" },
        feiShen = flyingSpirit?.let { "${it.sixKin.cn}${it.naJia.branch.cn}${it.element.cn}" },
        prosperity = status.strength.cn,
        relations = status.toTags(),
    )

    private fun LineStatus.toTags(): List<YaoTag> = buildList {
        if (isVoid) add(YaoTag.EMPTY)
        if (isMonthBroken) add(YaoTag.BREAK)
        if (isDayClashed) add(YaoTag.DAY_CLASH)
        if (isCombined) add(YaoTag.COMBINE)
        if (isClashed && !isDayClashed) add(YaoTag.CLASH)
        if (isPunished) add(YaoTag.PUNISH)
        if (isHarmed) add(YaoTag.HARM)
        // 伏神标签由 fuShen 字段单独成行展示,这里不重复加 HIDDEN
    }

    private fun posCn(pos: Int): String = when (pos) {
        1 -> "初"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; 6 -> "上"; else -> pos.toString()
    }
}
