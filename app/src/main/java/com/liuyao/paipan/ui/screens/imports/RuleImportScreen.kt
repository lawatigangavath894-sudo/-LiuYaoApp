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
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

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
            val name = runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
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
                    footer = "支持 txt / md / csv 等文本资料。会先读取原始 bytes 并识别 UTF-8、GB18030、GBK、GB2312 等编码，进入预览后可手动切换编码。",
                ) {
                    item {
                        Text(
                            "导入前必须先预览确认，避免乱码或无关资料直接入库。",
                            style = IOSTextStyles.Body,
                            color = AppTheme.colors.label,
                            modifier = Modifier.padding(Spacing.cardPadding),
                        )
                    }
                }
            }

            state.fileName?.let { fileName ->
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
                                if (state.isParsing) {
                                    "正在解析..."
                                } else {
                                    "已按 ${state.encoding ?: "未知编码"} 解析出 ${state.drafts.size} 条草稿，其中 ${state.reviewCount} 条待确认。"
                                },
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(horizontal = Spacing.rowHorizontal).padding(bottom = Spacing.rowVertical),
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Text(
                        error,
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
                        onClick = { picker.launch(arrayOf("text/plain", "text/markdown", "text/csv", "text/*", "*/*")) },
                    )
                    if (state.drafts.isNotEmpty() || state.rawPreview.isNotBlank()) {
                        IOSSecondaryButton(
                            text = "查看预览(${state.drafts.size})",
                            onClick = onParsed,
                        )
                    }
                }
            }
        }
    }
}
