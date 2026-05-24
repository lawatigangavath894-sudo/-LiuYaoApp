package com.liuyao.paipan.ui.screens.imports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 导入预览页(grouped cards)。逐条草稿可展开编辑;底部「确认导入」写入 Room。
 */
@Composable
fun ImportPreviewScreen(
    vm: RuleImportViewModel,
    onBack: () -> Unit,
    onImported: () -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    IOSDetailScaffold(title = "导入预览", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSCard {
                        Column {
                            Text(
                                "共 ${state.drafts.size} 条草稿",
                                style = IOSTextStyles.Headline,
                                color = AppTheme.colors.label,
                            )
                            Text(
                                if (state.reviewCount > 0) {
                                    "其中 ${state.reviewCount} 条仍有「待确认」字段,建议先校对再导入。"
                                } else {
                                    "全部字段已识别,可直接导入。"
                                },
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(top = Spacing.xs),
                            )
                        }
                    }
                }
            }

            items(state.drafts, key = { it.id }) { draft ->
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    DraftRuleEditor(
                        draft = draft,
                        onChange = { vm.updateDraft(it) },
                        onRemove = { vm.removeDraft(draft.id) },
                    )
                }
            }

            if (state.drafts.isEmpty()) {
                item {
                    Text(
                        "没有可导入的草稿。",
                        style = IOSTextStyles.Body,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton(
                        text = "确认导入" + if (state.drafts.isNotEmpty()) "(${state.drafts.size})" else "",
                        enabled = state.drafts.isNotEmpty(),
                        onClick = { vm.confirmImport(onDone = onImported) },
                    )
                }
            }
        }
    }
}
