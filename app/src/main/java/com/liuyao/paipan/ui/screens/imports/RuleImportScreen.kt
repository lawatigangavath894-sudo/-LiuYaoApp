package com.liuyao.paipan.ui.screens.imports

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 导入入口页(iOS Form 风)。选择本地 txt / markdown 文件 → 解析 → 跳预览。
 *
 * 文件选择器示例:使用 [ActivityResultContracts.OpenDocument],
 * MIME 限定文本类型;拿到 [Uri] 后交给 [RuleImportViewModel.loadFile] 读取解析。
 */
@Composable
fun RuleImportScreen(
    vm: RuleImportViewModel,
    onBack: () -> Unit,
    onParsed: () -> Unit,
) {
    BackHandler { onBack() }
    val state by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            // 取文件名
            val name = runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
                }
            }.getOrNull()
            vm.loadFile(uri, name)
        }
    }

    IOSDetailScaffold(title = "导入断语", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(
                    header = "说明",
                    footer = "支持 .txt 与 .md。导入后会按标题/空行/编号/项目符号切段,并尝试识别占类与类神;识别不到的字段会标为「待人工确认」,可在预览页修改后再入库。",
                ) {
                    item {
                        Text(
                            "半自动导入:先切段、再人工校对,避免资料直接乱入库。",
                            style = IOSTextStyles.Body,
                            color = AppTheme.colors.label,
                            modifier = Modifier.padding(Spacing.cardPadding),
                        )
                    }
                }
            }

            val fileName = state.fileName
            if (fileName != null) {
                item {
                    IOSGroupedSection(header = "已选择") {
                        item {
                            Text(
                                fileName,
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.label,
                                modifier = Modifier.padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                            )
                        }
                        item {
                            Text(
                                if (state.isParsing) "正在解析…" else "已解析出 ${state.drafts.size} 条草稿,其中 ${state.reviewCount} 条待确认。",
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(horizontal = Spacing.rowHorizontal).padding(bottom = Spacing.rowVertical),
                            )
                        }
                    }
                }
            }

            state.error?.let { err ->
                item {
                    Text(
                        err,
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.clash,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    IOSPrimaryButton(
                        text = "选择文件",
                        onClick = { picker.launch(arrayOf("text/plain", "text/markdown", "text/*", "*/*")) },
                    )
                    if (state.drafts.isNotEmpty()) {
                        com.liuyao.paipan.ui.components.IOSSecondaryButton(
                            text = "查看预览(${state.drafts.size})",
                            onClick = onParsed,
                        )
                    }
                }
            }
        }
    }
}
