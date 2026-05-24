package com.liuyao.paipan.data

import androidx.compose.ui.graphics.Color

// ——— Mock 模型(本阶段仅用于展示,后续阶段由引擎/Room 取代) ———

data class RecentCast(val id: String, val question: String, val hex: String, val time: String)
data class RuleItem(val id: String, val title: String, val category: String, val summary: String)
data class CaseItem(val id: String, val question: String, val hex: String, val verdict: String, val date: String)

/** 排盘页用:一行爻 */
data class YaoRow(
    val liuShen: String,
    val liuQin: String,
    val branch: String,
    val element: String,
    val marker: String?,    // "世" / "应" / null
    val moving: Boolean,
    val changedTo: String?, // 变爻六亲地支,如 "父母丑土"
    val badges: List<String>,
    val empty: Boolean,     // 旬空
)

object MockData {

    const val todayGanZhi = "丙午年 · 癸巳月 · 丙申日 · 乙未时"
    const val todayXunKong = "旬空 辰巳"

    val recentCasts = listOf(
        RecentCast("c1", "能否拿到这家公司 offer", "天水讼 → 风泽中孚", "今天 14:09"),
        RecentCast("c2", "明年财运如何", "火山旅 → 火风鼎", "昨天 21:30"),
        RecentCast("c3", "合同能否顺利签订", "乾为天 → 天风姤", "5 月 18 日"),
    )

    val rules = listOf(
        RuleItem("r1", "官鬼伏而被克", "求职", "用神伏藏又遭日月克冲,谋事多不成,或久拖生变。"),
        RuleItem("r2", "妻财持世逢日生", "财运", "财爻临世得日辰生扶,财来就我,主进财顺遂。"),
        RuleItem("r3", "父母化兄弟带刑", "文书", "文书动而化兄弟,又见相刑,通知多旁落他人或反复。"),
        RuleItem("r4", "子孙旺动克官", "官非", "子孙当令发动克制官鬼,官灾消解,亦不利谋官。"),
    )

    val cases = listOf(
        CaseItem("k1", "面试能否通过", "天水讼 → 风泽中孚", "待验证", "5 月 22 日"),
        CaseItem("k2", "丢失钱包能否找回", "山地剥 → 山水蒙", "准", "5 月 10 日"),
        CaseItem("k3", "投资项目是否可行", "泽天夬 → 泽风大过", "部分", "4 月 28 日"),
    )

    // —— 排盘页 mock(乾为天 → 天风姤,巳月丙申日) ——
    const val chartQuestion = "求测:能否拿到 offer"
    const val chartTime = "公历 2026-05-22 14:09 · 农历四月初六"
    const val chartGanZhi = "丙午年 癸巳月 丙申日 乙未时"
    const val chartXunKong = "辰巳"
    const val chartHexLine = "乾为天 → 天风姤"
    const val chartPalace = "乾宫 · 金 · 六冲"

    // 由上而下(上爻 → 初爻),便于直接渲染
    val chartYao = listOf(
        YaoRow("青龙", "父母", "戌", "土", "世", false, null, emptyList(), false),
        YaoRow("玄武", "兄弟", "申", "金", null, false, null, emptyList(), false),
        YaoRow("白虎", "官鬼", "午", "火", null, false, null, emptyList(), false),
        YaoRow("螣蛇", "父母", "辰", "土", "应", false, null, listOf("旬空"), true),
        YaoRow("勾陈", "妻财", "寅", "木", null, false, null, emptyList(), false),
        YaoRow("朱雀", "子孙", "子", "水", null, true, "父母丑土", listOf("回头生"), false),
    )

    val analysisTabs = listOf("神煞", "旺衰", "批注", "案例", "占法", "取象", "断语", "反馈")

    fun verdictColor(v: String): Pair<Color, Color> = when (v) {
        "准" -> Color(0x1A2E7D32) to Color(0xFF2E7D32)
        "部分" -> Color(0x1AB26A00) to Color(0xFFB26A00)
        "不准" -> Color(0x1AB44A3A) to Color(0xFFB44A3A)
        else -> Color(0x143C3C43) to Color(0xFF6B6B70)
    }
}
