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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.screens.rules.RulesViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 断语详情页。展示全部字段;提供编辑、删除入口。
 */
@Composable
fun RuleDetailScreen(
    vm: RulesViewModel,
    ruleId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val rule = state.rules.firstOrNull { it.id == ruleId }
    var confirmDelete by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(ruleId) { vm.loadStats(ruleId) }

    IOSDetailScaffold(
        title = "断语详情",
        onBack = onBack,
        trailing = { IOSSecondaryButton("编辑", onClick = { onEdit(ruleId) }, filled = false) },
    ) { padding ->
        if (rule == null) {
            Column(Modifier.padding(padding).padding(Spacing.xl)) {
                Text("断语不存在或已删除", style = IOSTextStyles.Body, color = AppTheme.colors.secondaryLabel)
            }
            return@IOSDetailScaffold
        }

        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "来源与分类") {
                    item { LabeledRow("来源", rule.sourceName) }
                    item { LabeledRow("占类", rule.category.cn) }
                    item { LabeledRow("类神", rule.target.cn) }
                }
            }
            item {
                IOSGroupedSection(header = "原文") {
                    item { ParagraphRow(rule.originalText) }
                }
            }
            item {
                IOSGroupedSection(header = "白话解释") {
                    item { ParagraphRow(rule.plainExplanation) }
                }
            }
            item {
                IOSGroupedSection(header = "适用条件") {
                    item { ParagraphRow(rule.conditionText.ifBlank { "—" }) }
                }
            }
            item {
                IOSGroupedSection(header = "不适用条件") {
                    item { ParagraphRow(rule.negativeMeaning ?: "—") }
                }
            }
            item {
                IOSGroupedSection(header = "属性") {
                    item { LabeledRow("吉凶", rule.polarity.cn) }
                    item { LabeledRow("优先级", rule.priority.toString()) }
                    item { LabeledRow("权重", rule.confidenceWeight.toString()) }
                }
            }
            item {
                val stats = state.statsById[ruleId]
                IOSGroupedSection(
                    header = "历史表现",
                    footer = "可靠度由历史反馈计算,误判越多排序越靠后;系统不会自动删除任何规则。",
                ) {
                    if (stats == null) {
                        item { LabeledRow("反馈记录", "暂无") }
                    } else {
                        val rel = (com.liuyao.paipan.domain.match.RuleReliabilityCalculator.reliability(stats) * 100).toInt()
                        item { LabeledRow("可靠度", "$rel%") }
                        item { LabeledRow("使用次数", stats.matchedCount.toString()) }
                        item { LabeledRow("验中", stats.hitCount.toString()) }
                        item { LabeledRow("误判", stats.missCount.toString()) }
                        item { LabeledRow("部分验中", stats.partialCount.toString()) }
                        item { LabeledRow("无法判断", stats.unknownCount.toString()) }
                        stats.lastVerdict?.let { v ->
                            item { LabeledRow("最近反馈", com.liuyao.paipan.domain.model.CaseVerdict.fromName(v).cn) }
                        }
                    }
                }
            }
            if (rule.tags.isNotEmpty()) {
                item {
                    IOSGroupedSection(header = "标签") {
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(Spacing.cardPadding),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                rule.tags.forEach { IOSBadge(it) }
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (!confirmDelete) {
                        IOSSecondaryButton("删除断语", onClick = { confirmDelete = true })
                    } else {
                        Text(
                            "确认删除?此操作不可撤销。",
                            style = IOSTextStyles.Footnote,
                            color = AppTheme.colors.moving,
                            modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.xs),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            IOSSecondaryButton("取消", onClick = { confirmDelete = false }, modifier = Modifier.weight(1f))
                            IOSPrimaryButton("确认删除", onClick = { vm.delete(ruleId, onDone = onBack) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        Text(value, style = IOSTextStyles.Body, color = AppTheme.colors.secondaryLabel)
    }
}

@Composable
private fun ParagraphRow(text: String) {
    Text(
        text,
        style = IOSTextStyles.Body,
        color = AppTheme.colors.label,
        modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
    )
}
