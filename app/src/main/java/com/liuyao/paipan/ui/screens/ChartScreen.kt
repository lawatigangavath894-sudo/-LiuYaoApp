package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liuyao.paipan.data.ai.AiConfigStore
import com.liuyao.paipan.data.ai.OpenAiCompatibleClient
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.screens.chart.AnalysisLockCard
import com.liuyao.paipan.ui.screens.chart.AnalysisTabs
import com.liuyao.paipan.ui.screens.chart.ChartAnalysisViewModel
import com.liuyao.paipan.ui.screens.chart.ChartUiMapper
import com.liuyao.paipan.ui.screens.chart.ChartViewModel
import com.liuyao.paipan.ui.screens.chart.HexagramInfoCard
import com.liuyao.paipan.ui.screens.chart.HexagramPlate
import com.liuyao.paipan.ui.screens.chart.TimeInfoCard
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun ChartScreen(
    vm: ChartViewModel,
    onBack: () -> Unit,
    onAiAnalyze: (String) -> Unit = {},
    onOpenAiSettings: () -> Unit = {},
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val chart = state.chart
    val context = LocalContext.current

    val analysisVm: ChartAnalysisViewModel = viewModel()
    val analysisState by analysisVm.ui.collectAsStateWithLifecycle()
    var showAiConfigPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(chart?.id) {
        chart?.let {
            analysisVm.analyze(it, it.category ?: DivinationCategory.OTHER)
        }
    }

    val title = chart?.question ?: "排盘"
    IOSDetailScaffold(title = title, onBack = onBack) { padding ->
        if (chart == null) {
            EmptyChart(padding)
        } else {
            val ui = ChartUiMapper.toUiModel(chart)
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
                modifier = Modifier.padding(bottom = Spacing.xxxl),
            ) {
                item { TimeInfoCard(ui) }
                item { HexagramInfoCard(ui) }
                item { AnalysisLockCard(analysisState.analysisLock, analysisState.knowledgeMessage) }
                item { HexagramPlate(ui) }
                item {
                    AnalysisTabs(
                        chart = chart,
                        lock = analysisState.analysisLock,
                        report = analysisState.report,
                        favoriteIds = analysisState.favoriteRuleIds,
                        onToggleFavorite = analysisVm::toggleFavorite,
                    )
                }
                item {
                    val caseVm: com.liuyao.paipan.ui.screens.cases.CaseViewModel = viewModel()
                    var saved by remember(chart.id) { mutableStateOf(false) }
                    var aiOpening by remember(chart.id) { mutableStateOf(false) }
                    Column(
                        Modifier.padding(horizontal = Spacing.pageHorizontal),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        IOSPrimaryButton(
                            text = if (aiOpening) "正在打开 AI..." else "AI 解析",
                            enabled = !aiOpening,
                            onClick = {
                                if (!aiOpening) {
                                    val provider = AiConfigStore(context).loadProvider()
                                    val invalid = OpenAiCompatibleClient().validate(provider)
                                    if (invalid != null) {
                                        showAiConfigPrompt = true
                                    } else {
                                        aiOpening = true
                                        onAiAnalyze(chart.id)
                                    }
                                }
                            },
                        )
                        IOSPrimaryButton(
                            text = if (saved) "已保存为案例 ✓" else "保存为案例",
                            enabled = !saved,
                            onClick = {
                                if (!saved) {
                                    caseVm.saveCurrentChartAsCase(chart, analysisState.report) { saved = true }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAiConfigPrompt) {
        AlertDialog(
            onDismissRequest = { showAiConfigPrompt = false },
            title = { Text("请先配置 AI 模型") },
            text = { Text("当前没有可用的默认 AI Provider，请先在设置中填写 Base URL、API Key 和模型名。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAiConfigPrompt = false
                        onOpenAiSettings()
                    },
                ) {
                    Text("去设置", color = AppTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiConfigPrompt = false }) {
                    Text("取消", color = AppTheme.colors.secondaryLabel)
                }
            },
            containerColor = AppTheme.colors.card,
            titleContentColor = AppTheme.colors.label,
            textContentColor = AppTheme.colors.secondaryLabel,
        )
    }
}

@Composable
private fun EmptyChart(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        Modifier.fillMaxSize().padding(padding).padding(Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "还没有排盘\n请先从首页起卦",
            style = IOSTextStyles.Body,
            color = AppTheme.colors.secondaryLabel,
            textAlign = TextAlign.Center,
        )
    }
}
