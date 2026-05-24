package com.liuyao.paipan.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.data.backup.ImportMode
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 数据恢复页(iOS Form)。选 JSON → 预览 → 选覆盖/合并 → 二次确认 → 恢复。
 * 覆盖为危险操作,需明确二次确认。
 */
@Composable
fun RestoreScreen(
    vm: BackupViewModel,
    onBack: () -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(ImportMode.MERGE) }
    var confirmReplace by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? -> if (uri != null) vm.loadImportFile(uri) }

    IOSDetailScaffold(title = "从 JSON 恢复", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "选择备份文件", footer = "支持导出的断语库 / 案例库 JSON。") {
                    item {
                        IOSListRow(
                            "选择文件",
                            value = state.importPreview?.let { if (it.valid) "已选择" else "无效" },
                            showChevron = true,
                            onClick = { picker.launch(arrayOf("application/json", "application/octet-stream", "*/*")) },
                        )
                    }
                }
            }

            val p = state.importPreview
            if (p != null) {
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Column {
                                Text("解析结果", style = IOSTextStyles.Headline, color = AppTheme.colors.label)
                                Text(
                                    p.message,
                                    style = IOSTextStyles.Subhead,
                                    color = if (p.valid) AppTheme.colors.secondaryLabel else AppTheme.colors.clash,
                                    modifier = Modifier.padding(top = Spacing.xs),
                                )
                            }
                        }
                    }
                }

                if (p.valid) {
                    // 模式选择
                    item {
                        IOSGroupedSection(header = "导入方式") {
                            item {
                                IOSListRow(
                                    ImportMode.MERGE.cn,
                                    value = if (mode == ImportMode.MERGE) "✓" else null,
                                    onClick = { mode = ImportMode.MERGE; confirmReplace = false },
                                )
                            }
                            item {
                                IOSListRow(
                                    ImportMode.REPLACE.cn,
                                    value = if (mode == ImportMode.REPLACE) "✓" else null,
                                    onClick = { mode = ImportMode.REPLACE },
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            Modifier.padding(horizontal = Spacing.pageHorizontal),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            if (mode == ImportMode.REPLACE && !confirmReplace) {
                                Text(
                                    "覆盖会先清空现有同类数据,且不可撤销。",
                                    style = IOSTextStyles.Footnote,
                                    color = AppTheme.colors.clash,
                                )
                                IOSSecondaryButton("我已了解,准备覆盖", onClick = { confirmReplace = true })
                            } else {
                                IOSPrimaryButton(
                                    text = if (mode == ImportMode.REPLACE) "确认覆盖并恢复" else "合并恢复",
                                    onClick = { vm.confirmRestore(mode) },
                                )
                            }
                        }
                    }
                }
            }

            state.message?.let { msg ->
                item {
                    Text(
                        msg,
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.accent,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }
        }
    }
}
