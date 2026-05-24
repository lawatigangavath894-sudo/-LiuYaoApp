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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.rules.RulesViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/** 本轮筛选用的占类(题目指定 10 项),映射到 DivinationCategory */

/**
 * 断语库列表页。Large Title + 占类胶囊筛选 + grouped 列表 + 新增入口。
 * 仅读 [RulesViewModel] 状态,持久化经 Room。
 */
@Composable
fun RulesScreen(
    vm: RulesViewModel,
    onOpenDetail: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    // segmented 选项:全部 + 10 占类
    val segLabels = listOf("全部") + FILTER_CATEGORIES.map { it.first }
    val selectedIndex = state.filter?.let { f ->
        FILTER_CATEGORIES.indexOfFirst { it.second == f }.let { if (it >= 0) it + 1 else 0 }
    } ?: 0

    IOSLargeTitleScaffold(
        title = "断语库",
        trailing = { IOSSecondaryButton("新增", onClick = onAdd, filled = false) },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSSegmentedControl(
                        options = segLabels,
                        selectedIndex = selectedIndex,
                        onSelect = { idx ->
                            vm.setFilter(if (idx == 0) null else FILTER_CATEGORIES[idx - 1].second)
                        },
                        scrollable = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val rules = state.visibleRules
            if (rules.isEmpty()) {
                item {
                    IOSGroupedSection(footer = "点右上「新增」录入断语。") {
                        item {
                            Text(
                                "暂无断语",
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
                        rules.forEach { rule ->
                            item {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickableNoRipple { onOpenDetail(rule.id) }
                                        .padding(Spacing.cardPadding),
                                ) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            rule.originalText.take(20).ifBlank { "(无原文)" },
                                            style = IOSTextStyles.Body,
                                            color = AppTheme.colors.label,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IOSBadge(rule.category.cn)
                                    }
                                    Text(
                                        rule.plainExplanation.take(40),
                                        style = IOSTextStyles.Footnote,
                                        color = AppTheme.colors.secondaryLabel,
                                        modifier = Modifier.padding(top = Spacing.xs),
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
