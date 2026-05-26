package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.cases.CaseViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing


/**
 * 案例库列表页。Large Title + 搜索 + 占类胶囊筛选 + grouped 案例卡 + 收藏。
 */
@Composable
fun CasesScreen(
    vm: CaseViewModel,
    onOpenDetail: (String) -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val segLabels = listOf("全部") + FILTER_CATEGORIES.map { it.first }
    val selectedIndex = state.filter?.let { f ->
        FILTER_CATEGORIES.indexOfFirst { it.second == f }.let { if (it >= 0) it + 1 else 0 }
    } ?: 0

    IOSLargeTitleScaffold(title = "案例库") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            // 搜索框
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSCard {
                        BasicTextField(
                            value = state.query,
                            onValueChange = vm::setQuery,
                            singleLine = true,
                            textStyle = IOSTextStyles.Body.merge(TextStyle(color = AppTheme.colors.label)),
                            cursorBrush = SolidColor(AppTheme.colors.accent),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (state.query.isEmpty()) {
                                    Text("搜索占事 / 标题", style = IOSTextStyles.Body, color = AppTheme.colors.tertiaryLabel)
                                }
                                inner()
                            },
                        )
                    }
                }
            }

            // 占类筛选
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSSegmentedControl(
                        options = segLabels,
                        selectedIndex = selectedIndex,
                        onSelect = { idx -> vm.setFilter(if (idx == 0) null else FILTER_CATEGORIES[idx - 1].second) },
                        scrollable = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (state.cases.isEmpty()) {
                item {
                    IOSGroupedSection(footer = "在排盘页点「保存为案例」即可建立案例库。") {
                        item {
                            Text(
                                "还没有案例",
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.tertiaryLabel,
                                modifier = Modifier.padding(Spacing.cardPadding),
                            )
                        }
                    }
                }
            } else {
                item {
                    IOSGroupedSection {
                        state.cases.forEach { case ->
                            item {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickableNoRipple { onOpenDetail(case.id) }
                                        .padding(Spacing.cardPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            case.title.ifBlank { "未命名案例" },
                                            style = IOSTextStyles.Body,
                                            color = AppTheme.colors.label,
                                        )
                                        Row(
                                            Modifier.padding(top = Spacing.xs),
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            case.category?.let { cat ->
                                                IOSBadge(DivinationCategory.cnOf(cat))
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (case.favorite) "★" else "☆",
                                        style = IOSTextStyles.Title3,
                                        color = if (case.favorite) AppTheme.colors.combine else AppTheme.colors.tertiaryLabel,
                                        modifier = Modifier.clickableNoRipple { vm.toggleFavorite(case.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
