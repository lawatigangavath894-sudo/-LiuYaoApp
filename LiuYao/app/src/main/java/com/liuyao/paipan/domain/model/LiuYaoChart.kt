package com.liuyao.paipan.domain.model

import java.time.LocalDateTime

/**
 * 17. 起卦方式 DivinationMethod
 * 用 sealed 以便不同方式携带各自的起卦原始输入(扩展点)。
 */
sealed class DivinationMethod(val cn: String) {
    /** 正时(以起卦时间四柱) */
    data object SolarTime : DivinationMethod("正时")

    /** 铜钱摇卦:六次,每次记 2/3 个背面(背=阴面),可由此推老阴少阳等 */
    data class Coin(val tosses: List<Int>) : DivinationMethod("铜钱")

    /** 手动指定六爻老少阴阳 */
    data class Manual(val raw: String) : DivinationMethod("手动")

    /** 数字起卦:上数、下数、动爻数 */
    data class Number(val upper: Int, val lower: Int, val moving: Int) : DivinationMethod("数字")
}

/**
 * 占类 DivinationCategory —— 用于断语匹配的事项分类。
 * 设为枚举但保留 [OTHER] 兜底,后续可平滑扩充。
 */
enum class DivinationCategory(val cn: String) {
    CAREER("事业求职"),
    WEALTH("财运"),
    MARRIAGE("婚姻感情"),
    HEALTH("疾病健康"),
    STUDY("学业考试"),
    FAME("求名文书"),
    LOST("寻物失物"),
    TRAVEL("出行"),
    LAWSUIT("官非诉讼"),
    PREGNANCY("孕产"),
    HOUSE("房宅"),
    COOPERATION("合作"),
    FORTUNE("运势"),
    OTHER("其他");

    companion object {
        /** 安全解析:非法/脏数据回退到 OTHER,避免崩溃 */
        fun fromName(name: String?): DivinationCategory =
            entries.firstOrNull { it.name == name } ?: OTHER

        /** 安全解析为中文名(供 UI 直接展示) */
        fun cnOf(name: String?): String = fromName(name).cn
    }
}

/**
 * 18. 占事信息 ChartQuestion —— 结构化占事(可选使用)。
 * [LiuYaoChart.question] 保留纯文本以贴合题目签名;需要结构化时用本类承载。
 */
data class ChartQuestion(
    val text: String,
    val category: DivinationCategory? = null,
    val askerNote: String? = null,
)

/**
 * 12. 排盘总模型 LiuYaoChart。
 *
 * 字段严格对应题目要求;[lines] 约定 index0 = 初爻。
 * 世/应以"爻序 1..6"记录于 [worldLineIndex]/[responseLineIndex]。
 */
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
    val worldLineIndex: Int,    // 1..6
    val responseLineIndex: Int, // 1..6
    val method: DivinationMethod,
    val notes: List<String>,
) {
    /** 世爻(按 1..6 取 index 对应爻;lines 以 index0=初爻存储) */
    val worldLine: YaoLine get() = lines.first { it.index == worldLineIndex }
    val responseLine: YaoLine get() = lines.first { it.index == responseLineIndex }

    /** 动爻列表 */
    val movingLines: List<YaoLine> get() = lines.filter { it.isMoving }

    /** 月令地支(便于旺衰/关系计算) */
    val monthBranch: EarthlyBranch get() = monthGanZhi.branch
    val dayBranch: EarthlyBranch get() = dayGanZhi.branch

    /** 按某六亲取爻(可能多现) */
    fun linesOf(kin: SixKin): List<YaoLine> = lines.filter { it.sixKin == kin }
}
