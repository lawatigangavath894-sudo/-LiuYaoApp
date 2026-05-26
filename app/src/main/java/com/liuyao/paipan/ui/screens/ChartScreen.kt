package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSDetailScaffold
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

/**
 * 鎺掔洏椤点€傛暟鎹潵鑷?[ChartViewModel] 鐨勭姸鎬?鐢?[LiuYaoChartEngine] 鎺掔洏鍚庣粡
 * [ChartUiMapper] 鎶曞奖涓哄睍绀烘ā鍨嬨€俇I 浠呮覆鏌?涓嶅惈浠讳綍鎺掔洏閫昏緫銆?
 *
 * 缁撴瀯(鑷笂鑰屼笅):Large Title 鍗犱簨 鈫?鏃堕棿鍗?鈫?鍗﹁薄鍗?鈫?鍏埢鐩?鈫?鍒嗘瀽鍖恒€?
 */
@Composable
fun ChartScreen(
    vm: ChartViewModel,
    onBack: () -> Unit,
    onAiAnalyze: (String) -> Unit = {},
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val chart = state.chart

    val analysisVm: ChartAnalysisViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val analysisState by analysisVm.ui.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(chart?.id) {
        if (chart != null) {
            analysisVm.analyze(chart, chart.category ?: com.liuyao.paipan.domain.model.DivinationCategory.OTHER)
        }
    }

    val title = chart?.question ?: "鎺掔洏"
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
                item { HexagramPlate(ui) }
                item {
                    AnalysisTabs(
                        report = analysisState.report,
                        favoriteIds = analysisState.favoriteRuleIds,
                        onToggleFavorite = analysisVm::toggleFavorite,
                    )
                }
                item {
                    val caseVm: com.liuyao.paipan.ui.screens.cases.CaseViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel()
                    var saved by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    androidx.compose.foundation.layout.Column(
                        Modifier.padding(horizontal = Spacing.pageHorizontal),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        com.liuyao.paipan.ui.components.IOSPrimaryButton(
                            text = "AI 瑙ｆ瀽",
                            onClick = { onAiAnalyze(chart.id) },
                        )
                        com.liuyao.paipan.ui.components.IOSPrimaryButton(
                            text = if (saved) "已保存为案例 ✓" else "保存为案例",
                            enabled = !saved,
                            onClick = {
                                caseVm.saveCurrentChartAsCase(chart, analysisState.report) { saved = true }
                            },
                        )
                    }
                }
            }
        }
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
