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
import com.liuyao.paipan.domain.imports.ImportedTextReader
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

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
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSCard {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    state.fileName ?: "未选择文件",
                                    style = IOSTextStyles.Headline,
                                    color = AppTheme.colors.label,
                                    modifier = Modifier.weight(1f),
                                )
                                state.encoding?.let {
                                    IOSBadge(it, container = AppTheme.colors.accentSoft, content = AppTheme.colors.accent)
                                }
                            }
                            Text(
                                "共 ${state.drafts.size} 条 · 已选 ${state.selectedCount} 条 · 待确认 ${state.reviewCount} 条",
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                            )
                            Text(
                                "当前识别编码：${state.encoding ?: "未知"}，自动推荐：${state.autoEncoding ?: "未知"}",
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                            )
                            state.encodingWarning?.let {
                                Text(
                                    it,
                                    style = IOSTextStyles.Footnote,
                                    color = AppTheme.colors.moving,
                                )
                            }
                        }
                    }
                }
            }

            item {
                EncodingSelector(
                    options = state.encodingOptions,
                    selected = state.encoding,
                    autoEncoding = state.autoEncoding,
                    candidates = state.candidateResults.associateBy { it.encoding },
                    onSelect = vm::selectEncoding,
                )
            }

            if (state.rawPreview.isNotBlank()) {
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Column {
                                Text("原文预览", style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
                                Text(
                                    state.rawPreview,
                                    style = IOSTextStyles.Caption,
                                    color = AppTheme.colors.secondaryLabel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .verticalScroll(rememberScrollState())
                                        .padding(top = Spacing.xs),
                                )
                            }
                        }
                    }
                }
            }

            if (state.drafts.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Spacing.pageHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        IOSSecondaryButton("全选", onClick = { vm.selectAll(true) }, filled = false, modifier = Modifier.weight(1f))
                        IOSSecondaryButton("全不选", onClick = { vm.selectAll(false) }, filled = false, modifier = Modifier.weight(1f))
                    }
                }
            }

            items(state.drafts, key = { it.id }) { draft ->
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            if (draft.selectedForImport) "☑" else "☐",
                            style = IOSTextStyles.Body,
                            color = if (draft.selectedForImport) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel,
                            modifier = Modifier.padding(end = Spacing.sm, top = Spacing.sm).clickableNoRipple {
                                vm.toggleSelected(draft.id)
                            },
                        )
                        Column(Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), modifier = Modifier.padding(bottom = Spacing.xs)) {
                                val (color, title) = when (draft.importStatus) {
                                    "可能乱码" -> AppTheme.colors.moving to "可能乱码"
                                    "待确认" -> AppTheme.colors.tertiaryLabel to "待确认"
                                    else -> AppTheme.colors.accent to "可导入"
                                }
                                IOSBadge(title, container = color.copy(alpha = 0.18f), content = color)
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
                    Text(
                        if (state.isParsing) "正在解析..." else "没有可导入的草稿。",
                        style = IOSTextStyles.Body,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }

            result?.let { importResult ->
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Text(
                                "成功导入 ${importResult.success} 条 · 跳过 ${importResult.skipped} 条 · 失败 ${importResult.failed} 条",
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.label,
                            )
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    IOSPrimaryButton(
                        text = "确认导入(${state.selectedCount})",
                        enabled = state.selectedCount > 0 && result == null && !state.isPossiblyGarbled,
                        onClick = { vm.confirmImport(onDone = onImported) },
                    )
                    if (state.isPossiblyGarbled) {
                        Text(
                            "当前预览可能仍存在乱码，请切换编码或取消有风险条目后再导入。",
                            style = IOSTextStyles.Footnote,
                            color = AppTheme.colors.moving,
                        )
                    }
                    IOSSecondaryButton("取消导入", onClick = onBack, filled = false)
                }
            }
        }
    }
}

@Composable
private fun EncodingSelector(
    options: List<String>,
    selected: String?,
    autoEncoding: String?,
    candidates: Map<String, ImportedTextReader.DecodedCandidate>,
    onSelect: (String) -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
        IOSCard {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("编码选择", style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
                val names = options.ifEmpty { listOf(ImportedTextReader.AUTO) + ImportedTextReader.supportedEncodingNames() }
                names.forEach { name ->
                    val label = if (name == ImportedTextReader.AUTO) {
                        "自动${autoEncoding?.let { "（$it）" }.orEmpty()}"
                    } else {
                        val score = candidates[name]?.score
                        if (score != null) "$name · $score" else name
                    }
                    IOSSecondaryButton(
                        text = if (name == selected || (name == ImportedTextReader.AUTO && selected == autoEncoding)) "✓ $label" else label,
                        onClick = { onSelect(name) },
                        filled = name == selected || (name == ImportedTextReader.AUTO && selected == autoEncoding),
                    )
                }
            }
        }
    }
}
