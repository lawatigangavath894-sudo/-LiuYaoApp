package com.liuyao.paipan.ui.screens.imports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 导入预览页。展示文件名/编码/原文预览/拆分条目;支持全选与逐条勾选;确认仅导入选中条目。
 */
@Composable
fun ImportPreviewScreen(
    vm: RuleImportViewModel,
    onBack: () -> Unit,
    onImported: () -> Unit,
) {
    BackHandler { onBack() }
    val state by vm.ui.collectAsStateWithLifecycle()
    val result = state.importResult

    IOSDetailScaffold(title = "导入预览", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            // 文件信息 + 编码 badge
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(state.fileName ?: "未选择文件", style = IOSTextStyles.Headline, color = AppTheme.colors.label, modifier = Modifier.weight(1f))
                                state.encoding?.let { IOSBadge(it, container = AppTheme.colors.accentSoft, content = AppTheme.colors.accent) }
                            }
                            Text(
                                "共 ${state.drafts.size} 条 · 已选 ${state.selectedCount} 条 · 待确认 ${state.reviewCount} 条",
                                style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(top = Spacing.xs),
                            )
                            state.encodingWarning?.let {
                                Text(it, style = IOSTextStyles.Footnote, color = AppTheme.colors.moving, modifier = Modifier.padding(top = Spacing.xs))
                            }
                        }
                    }
                }
            }

            // 原文预览(可滚动)
            if (state.rawPreview.isNotBlank()) {
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Column {
                                Text("原文预览", style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
                                Text(
                                    state.rawPreview,
                                    style = IOSTextStyles.Caption, color = AppTheme.colors.secondaryLabel,
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState()).padding(top = Spacing.xs),
                                )
                            }
                        }
                    }
                }
            }

            // 全选 / 全不选
            if (state.drafts.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.pageHorizontal), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        IOSSecondaryButton("全选", onClick = { vm.selectAll(true) }, filled = false, modifier = Modifier.weight(1f))
                        IOSSecondaryButton("全不选", onClick = { vm.selectAll(false) }, filled = false, modifier = Modifier.weight(1f))
                    }
                }
            }

            // 条目列表
            items(state.drafts, key = { it.id }) { draft ->
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    Row(verticalAlignment = Alignment.Top) {
                        // 勾选框
                        Text(
                            if (draft.selectedForImport) "☑" else "☐",
                            style = IOSTextStyles.Body,
                            color = if (draft.selectedForImport) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel,
                            modifier = Modifier.padding(end = Spacing.sm, top = Spacing.sm).clickableNoRipple { vm.toggleSelected(draft.id) },
                        )
                        Column(Modifier.weight(1f)) {
                            // 状态徽标
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), modifier = Modifier.padding(bottom = Spacing.xs)) {
                                val (c, t) = when (draft.importStatus) {
                                    "可能乱码" -> AppTheme.colors.moving to "可能乱码"
                                    "待确认" -> AppTheme.colors.tertiaryLabel to "待确认"
                                    else -> AppTheme.colors.accent to "可导入"
                                }
                                IOSBadge(t, container = c.copy(alpha = 0.18f), content = c)
                            }
                            DraftRuleEditor(
                                draft = draft,
                                onChange = { vm.updateDraft(it) },
                                onRemove = { vm.removeDraft(draft.id) },
                            )
                        }
                    }
                }
            }

            if (state.drafts.isEmpty()) {
                item {
                    Text("没有可导入的草稿。", style = IOSTextStyles.Body, color = AppTheme.colors.tertiaryLabel, modifier = Modifier.padding(horizontal = Spacing.pageHorizontal))
                }
            }

            // 导入完成统计
            result?.let { r ->
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Text(
                                "成功导入 ${r.success} 条 · 跳过 ${r.skipped} 条 · 失败 ${r.failed} 条",
                                style = IOSTextStyles.Body, color = AppTheme.colors.label,
                            )
                        }
                    }
                }
            }

            // 底部按钮
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    IOSPrimaryButton(
                        text = "确认导入(${state.selectedCount})",
                        enabled = state.selectedCount > 0 && result == null,
                        onClick = { vm.confirmImport(onDone = onImported) },
                    )
                    IOSSecondaryButton("取消", onClick = onBack, filled = false)
                }
            }
        }
    }
}
