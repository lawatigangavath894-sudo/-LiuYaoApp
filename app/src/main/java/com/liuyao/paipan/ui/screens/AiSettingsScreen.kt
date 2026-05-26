package com.liuyao.paipan.ui.screens

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
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AiSettingsScreen(onBack: () -> Unit) {
    var message by remember { mutableStateOf("AI 配置持久化与真实连接测试暂未完成，将在后续版本开放。") }

    IOSDetailScaffold(title = "AI 大模型设置", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "Provider") {
                    item { IOSListRow("Base URL", value = "未配置") }
                    item { IOSListRow("API Key", value = "未配置") }
                    item { IOSListRow("模型名", value = "未配置") }
                    item { IOSListRow("默认模型", value = "未配置") }
                }
            }
            item {
                IOSGroupedSection(header = "操作") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton("新增 Provider", onClick = { message = "新增 Provider 功能暂未完成，将在后续版本开放。" })
                            IOSSecondaryButton("测试连接", onClick = { message = "请先配置 Base URL、API Key 和模型名后再测试。真实请求将在后续版本开放。" }, filled = false)
                            IOSSecondaryButton("保存配置", onClick = { message = "保存配置功能暂未完成，将在后续版本开放。" }, filled = false)
                        }
                    }
                }
            }
            item {
                Text(
                    message,
                    style = IOSTextStyles.Footnote,
                    color = AppTheme.colors.secondaryLabel,
                    modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                )
            }
        }
    }
}
