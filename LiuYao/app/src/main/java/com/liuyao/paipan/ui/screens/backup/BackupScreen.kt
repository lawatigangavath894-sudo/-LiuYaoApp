package com.liuyao.paipan.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
 * 数据导出页(iOS Form)。选导出类型 → 预览 → 保存到文件(SAF)。
 */
@Composable
fun BackupScreen(
    vm: BackupViewModel,
    onBack: () -> Unit,
    onOpenRestore: () -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()

    // 保存文件:CreateDocument。拿到 Uri 后把预览内容写入。
    val preview = state.preview
    val saver = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri: Uri? ->
        if (uri != null && preview != null) vm.writeToUri(uri, preview.content)
    }

    IOSDetailScaffold(title = "导出与备份", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "导出为 JSON", footer = "JSON 可用于备份与跨设备恢复。") {
                    item { IOSListRow("导出断语库", showChevron = true, onClick = { vm.previewRules() }) }
                    item { IOSListRow("导出案例库", showChevron = true, onClick = { vm.previewCases() }) }
                }
            }

            item {
                IOSGroupedSection(header = "恢复") {
                    item { IOSListRow("从 JSON 恢复…", showChevron = true, onClick = onOpenRestore) }
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

            // 预览
            if (preview != null) {
                item {
                    Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                        IOSCard {
                            Column {
                                Text("导出预览", style = IOSTextStyles.Headline, color = AppTheme.colors.label)
                                Text(
                                    "共 ${preview.itemCount} 条 · 约 ${preview.byteSize} 字节 · ${preview.suggestedFileName}",
                                    style = IOSTextStyles.Footnote,
                                    color = AppTheme.colors.secondaryLabel,
                                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm),
                                )
                                Text(
                                    preview.content.take(800) + if (preview.content.length > 800) "\n…" else "",
                                    style = IOSTextStyles.Caption,
                                    color = AppTheme.colors.secondaryLabel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp)
                                        .verticalScroll(rememberScrollState()),
                                )
                            }
                        }
                    }
                }
                item {
                    Column(
                        Modifier.padding(horizontal = Spacing.pageHorizontal),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        IOSPrimaryButton("保存到文件", onClick = { saver.launch(preview.suggestedFileName) })
                        IOSSecondaryButton("取消预览", onClick = { vm.clearPreview() }, filled = false)
                    }
                }
            }
        }
    }
}
