package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liuyao.paipan.data.MockData
import com.liuyao.paipan.data.RecentCast
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val todayGanZhi: String = "",
    val xunKong: String = "",
    val recent: List<RecentCast> = emptyList(),
)

class HomeViewModel : ViewModel() {
    private val _ui = MutableStateFlow(
        HomeUiState(MockData.todayGanZhi, MockData.todayXunKong, MockData.recentCasts),
    )
    val ui = _ui.asStateFlow()
}

@Composable
fun HomeScreen(
    onCast: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenCases: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenChart: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    IOSLargeTitleScaffold(title = "六爻") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = androidx.compose.ui.Modifier.padding(bottom = Spacing.xxl),
        ) {
            item {
                IOSGroupedSection(header = "今日") {
                    item {
                        Column(androidx.compose.ui.Modifier.padding(Spacing.cardPadding)) {
                            Text(state.todayGanZhi, style = IOSTextStyles.Headline, color = AppTheme.colors.label)
                            Text(
                                state.xunKong,
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                                modifier = androidx.compose.ui.Modifier.padding(top = Spacing.xs),
                            )
                        }
                    }
                }
            }

            item {
                IOSGroupedSection(header = "核心入口") {
                    item { IOSListRow("起卦", value = "正时 / 选择时间 / 手动", showChevron = true, onClick = onCast) }
                    item { IOSListRow("AI 对话", value = "暂未配置", showChevron = true, onClick = onOpenAi) }
                    item { IOSListRow("断语库", showChevron = true, onClick = onOpenRules) }
                    item { IOSListRow("案例库", showChevron = true, onClick = onOpenCases) }
                    item { IOSListRow("设置", showChevron = true, onClick = onOpenSettings) }
                }
            }

            item {
                IOSGroupedSection(header = "最近") {
                    if (state.recent.isEmpty()) {
                        item {
                            Text(
                                "暂无最近排盘",
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.tertiaryLabel,
                                modifier = androidx.compose.ui.Modifier.padding(Spacing.cardPadding),
                            )
                        }
                    } else {
                        state.recent.forEach { cast ->
                            item {
                                Column(
                                    androidx.compose.ui.Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.cardPadding)
                                        .then(androidx.compose.ui.Modifier),
                                ) {
                                    IOSListRow(
                                        title = cast.question,
                                        value = cast.hex,
                                        showChevron = true,
                                        onClick = onOpenChart,
                                    )
                                    Row(
                                        androidx.compose.ui.Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(cast.time, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
