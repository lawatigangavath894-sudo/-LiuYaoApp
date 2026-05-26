package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.analysis.AiChartPromptBuilder
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.screens.chart.ChartAnalysisViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AiChatScreen(
    chartId: String?,
    currentChart: LiuYaoChart? = null,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val chartForPrompt = currentChart?.takeIf { chartId.isNullOrBlank() || it.id == chartId }
    val analysisVm: ChartAnalysisViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val analysisState by analysisVm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(chartForPrompt?.id) {
        val chart = chartForPrompt
        if (chart != null) {
            analysisVm.analyze(chart, chart.category ?: DivinationCategory.OTHER)
        }
    }

    val prompt = remember(chartForPrompt, analysisState.analysisLock, analysisState.report) {
        val chart = chartForPrompt
        val lock = analysisState.analysisLock
        if (chart != null && lock != null) AiChartPromptBuilder.build(chart, lock, analysisState.report) else null
    }
    var showPrompt by remember(chartId, prompt) { mutableStateOf(false) }
    var message by remember(chartId) {
        mutableStateOf(
            if (chartId.isNullOrBlank()) {
                "请先在设置中配置 AI 模型。"
            } else {
                "已接收排盘 chartId：$chartId，正在生成资料检索 Prompt。"
            },
        )
    }

    IOSDetailScaffold(title = if (chartId.isNullOrBlank()) "AI 对话" else "AI 解析", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "状态") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                when {
                                    chartId != null && chartForPrompt == null -> "未找到当前排盘 chartId：$chartId，请从排盘页重新进入。"
                                    prompt != null -> "已生成当前排盘、分析锁定与刘昌明资料片段的 AI 解析 Prompt。"
                                    else -> message
                                },
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.secondaryLabel,
                            )
                            if (prompt != null) {
                                IOSSecondaryButton(
                                    text = if (showPrompt) "收起 Prompt" else "展开 Prompt 预览",
                                    onClick = { showPrompt = !showPrompt },
                                    filled = false,
                                )
                                if (showPrompt) {
                                    Text(prompt, style = IOSTextStyles.Footnote, color = AppTheme.colors.label)
                                }
                            }
                        }
                    }
                }
            }
            item {
                IOSGroupedSection(header = "操作") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton("去配置 AI 模型", onClick = onOpenSettings)
                            IOSSecondaryButton("新建对话", onClick = { message = "已新建空对话。发送与历史保存将在后续版本开放。" }, filled = false)
                            IOSSecondaryButton("清空上下文", onClick = { message = "上下文已清空。" }, filled = false)
                        }
                    }
                }
            }
        }
    }
}
