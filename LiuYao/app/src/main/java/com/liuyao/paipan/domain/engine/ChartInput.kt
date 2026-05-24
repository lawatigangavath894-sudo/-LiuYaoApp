package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.DivinationMethod
import com.liuyao.paipan.domain.model.YinYang
import java.time.LocalDateTime

/**
 * 排盘引擎输入。
 *
 * 约定:[lines] 与 [moving] 均为长度 6,index0 = 初爻(最下),index5 = 上爻。
 *
 * @param dateTime    起卦时间(用于推四柱)
 * @param lines       六爻阴阳(true 由 [YinYang] 表达)
 * @param moving      六爻动爻状态,true=动爻(老阴/老阳)
 * @param question    占事标题
 * @param method      起卦方式
 * @param category    占类(可空,用于后续断语匹配;本轮仅透传)
 * @param id          可选自定义 id;为空时引擎生成
 */
data class ChartInput(
    val dateTime: LocalDateTime,
    val lines: List<YinYang>,
    val moving: List<Boolean>,
    val question: String,
    val method: DivinationMethod,
    val category: DivinationCategory? = null,
    val id: String? = null,
) {
    init {
        require(lines.size == 6) { "lines 必须为 6 爻,当前 ${lines.size}" }
        require(moving.size == 6) { "moving 必须为 6 位,当前 ${moving.size}" }
    }

    /** 动爻位集合(1..6) */
    val movingPositions: Set<Int>
        get() = moving.mapIndexedNotNull { i, m -> if (m) i + 1 else null }.toSet()

    companion object {
        /** 便捷构造:用布尔表示阴阳(true=阳) */
        fun fromBooleans(
            dateTime: LocalDateTime,
            yangFlags: List<Boolean>,
            moving: List<Boolean>,
            question: String,
            method: DivinationMethod,
            category: DivinationCategory? = null,
            id: String? = null,
        ): ChartInput = ChartInput(
            dateTime = dateTime,
            lines = yangFlags.map { if (it) YinYang.YANG else YinYang.YIN },
            moving = moving,
            question = question,
            method = method,
            category = category,
            id = id,
        )
    }
}
