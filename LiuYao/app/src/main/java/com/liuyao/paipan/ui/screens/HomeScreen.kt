package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liuyao.paipan.data.MockData
import com.liuyao.paipan.data.RecentCast
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// —— MVVM:UiState + ViewModel(mock 数据源) ——
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
    onOpenChart: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    IOSLargeTitleScaffold(title = "六爻") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            // 今日干支
            item {
                IOSGroupedSection(header = "今日") {
                    item {
                        Column(Modifier.padding(16.dp)) {
                            Text(state.todayGanZhi, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                state.xunKong,
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
            // 起卦主操作
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onCast),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, tint = AppTheme.colors.accent)
                        Text(
                            "起卦",
                            style = MaterialTheme.typography.titleLarge,
                            color = AppTheme.colors.accent,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
            // 最近卦
            item {
                IOSGroupedSection(header = "最近") {
                    state.recent.forEach { c ->
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onOpenChart)
                                    .padding(16.dp),
                            ) {
                                Text(c.question, style = MaterialTheme.typography.bodyLarge)
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(c.hex, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.accent)
                                    Text(c.time, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.secondaryLabel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
