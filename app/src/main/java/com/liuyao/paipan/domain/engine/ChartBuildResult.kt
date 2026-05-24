package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.LiuYaoChart

/**
 * 引擎构建结果。
 *
 * 第一版以"必成功"为主(输入已在 [ChartInput] 校验),但保留 [warnings]
 * 用于携带历法精度等非致命提示(如临近节气交接日的潜在 ±1 日误差)。
 */
data class ChartBuildResult(
    val chart: LiuYaoChart,
    val warnings: List<String> = emptyList(),
)
