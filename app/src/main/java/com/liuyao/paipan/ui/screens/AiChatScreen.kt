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
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AiChatScreen(
    chartId: String?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var message by remember(chartId) {
        mutableStateOf(
            if (chartId.isNullOrBlank()) {
                "请先在设置中配置 AI 模型。"
            } else {
                "已接收排盘 chartId：$chartId。AI 解析 Prompt 生成与发送将在后续版本开放。"
            },
        )
    }

    IOSDetailScaffold(title = if (chartId.isNullOrBlank()) "AI 对话" else "AI 解析", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "状态") {
                    item {
                        Text(
                            message,
                            style = IOSTextStyles.Body,
                            color = AppTheme.colors.secondaryLabel,
                            modifier = Modifier.padding(Spacing.cardPadding),
                        )
                    }
                }
            }
            item {
                IOSGroupedSection(header = "操作") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton("去配置 AI 模型", onClick = onOpenSettings)
                            IOSSecondaryButton("新建对话", onClick = { message = "已新建空对话。发送与历史保存将在后续版本开放。" }, filled = false)
                            IOSSecondaryButton("清空上下文", onClick = { message = "上下文已清空。" }, filled = false)
                        }
                    }
                }
            }
        }
    }
}

