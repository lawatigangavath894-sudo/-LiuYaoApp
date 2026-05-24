package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.DivinationMethod
import java.time.LocalDateTime

/**
 * 引擎输入示例,供测试与 UI 联调。
 * 不参与生产逻辑。
 */
object MockChartInput {

    /** 乾为天,初爻动 → 天风姤;巳月丙申日(2026-05-22 14:09)。 */
    val qianMovingFirst: ChartInput = ChartInput.fromBooleans(
        dateTime = LocalDateTime.of(2026, 5, 22, 14, 9),
        yangFlags = List(6) { true },                  // 六阳 = 乾为天
        moving = listOf(true, false, false, false, false, false), // 初爻动
        question = "文章投此期刊能否录用",
        method = DivinationMethod.SolarTime,
        category = DivinationCategory.FAME,
    )

    /** 静卦示例:水火既济(无动爻)。内离外坎。 */
    val jiJiStatic: ChartInput = ChartInput.fromBooleans(
        dateTime = LocalDateTime.of(2026, 5, 22, 14, 9),
        // 内离 阳阴阳;外坎 阴阳阴
        yangFlags = listOf(true, false, true, false, true, false),
        moving = List(6) { false },
        question = "近期事务是否平稳",
        method = DivinationMethod.SolarTime,
        category = DivinationCategory.FORTUNE,
    )
}
