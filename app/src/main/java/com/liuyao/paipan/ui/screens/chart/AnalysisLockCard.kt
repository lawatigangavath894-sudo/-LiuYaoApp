package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AnalysisLockCard(lock: AnalysisLock?, message: String?) {
    var editHint by remember(lock?.chartId) { mutableStateOf(false) }
    IOSGroupedSection(
        header = "分析锁定",
        footer = message ?: lock?.uncertainReason,
    ) {
        item {
            Column(
                Modifier.padding(Spacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (lock == null) {
                    Text("正在锁定占类、用神与资料片段...", style = IOSTextStyles.Subhead, color = AppTheme.colors.secondaryLabel)
                    return@Column
                }
                LockRow("占事类别", lock.category.displayName())
                LockRow("占事问题", lock.question)
                LockRow("主变量", lock.mainVariable)
                LockRow("主用神", lock.primaryUsefulGod?.displayName() ?: "未锁定")
                LockRow("辅助用神", lock.secondaryUsefulGods.joinToString("、") { it.displayName() }.ifBlank { "无" })
                LockRow("世爻", lock.worldLineIndex?.toString() ?: "未定")
                LockRow("应爻", lock.responseLineIndex?.toString() ?: "未定")
                LockRow("关键爻", lock.keyLineIndexes.joinToString("、").ifBlank { "未定" })
                LockRow("资料来源", lock.knowledgeSnippets.firstOrNull()?.sourceName ?: "未检索到明确资料")
                Text(lock.lockReason, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
                IOSSecondaryButton(
                    text = "修改",
                    onClick = { editHint = true },
                    filled = false,
                )
                if (editHint) {
                    Text(
                        "分析锁定编辑将在后续版本开放。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = IOSTextStyles.Subhead, color = AppTheme.colors.secondaryLabel)
        Text(
            value,
            style = IOSTextStyles.Subhead,
            color = AppTheme.colors.label,
            modifier = Modifier.weight(1f).padding(start = Spacing.md),
        )
    }
}
