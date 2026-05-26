package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.analysis.AiChartPromptBuilder
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSFormTextField
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.screens.ai.AiChatViewModel
import com.liuyao.paipan.ui.screens.chart.ChartAnalysisViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AiChatScreen(
    chartId: String?,
    currentChart: LiuYaoChart? = null,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val chatVm: AiChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val chatState by chatVm.ui.collectAsStateWithLifecycle()
    val chartForPrompt = currentChart?.takeIf { chartId.isNullOrBlank() || it.id == chartId }
    val analysisVm: ChartAnalysisViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val analysisState by analysisVm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(chartForPrompt?.id) {
        chatVm.reloadProvider()
        val chart = chartForPrompt
        if (chart != null) {
            analysisVm.analyze(chart, chart.category ?: DivinationCategory.OTHER)
        }
    }

    val prompt = remember(chartForPrompt, analysisState.analysisLock, analysisState.report) {
        val chart = chartForPrompt
        val lock = analysisState.analysisLock
        if (chart != null && lock != null) AiChartPromptBuilder.build(chart, lock, analysisState.report) else null
    }
    var showPrompt by remember(chartId, prompt) { mutableStateOf(false) }
    var message by remember(chartId) {
        mutableStateOf(
            if (chartId.isNullOrBlank()) {
                "请先在设置中配置 AI 模型。"
            } else {
                "已接收排盘 chartId：$chartId，正在生成资料检索 Prompt。"
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
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                when {
                                    chartId != null && chartForPrompt == null -> "未找到当前排盘 chartId：$chartId，请从排盘页重新进入。"
                                    prompt != null -> "已生成当前排盘、分析锁定与刘昌明资料片段的 AI 解析 Prompt。"
                                    else -> message
                                },
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.secondaryLabel,
                            )
                            if (prompt != null) {
                                IOSSecondaryButton(
                                    text = if (showPrompt) "收起 Prompt" else "展开 Prompt 预览",
                                    onClick = { showPrompt = !showPrompt },
                                    filled = false,
                                )
                                if (showPrompt) {
                                    Text(prompt, style = IOSTextStyles.Footnote, color = AppTheme.colors.label)
                                }
                                IOSSecondaryButton(
                                    text = "发送排盘 Prompt",
                                    onClick = { chatVm.sendText(prompt) },
                                    filled = false,
                                )
                            }
                        }
                    }
                }
            }
            item {
                IOSGroupedSection(header = "当前模型") {
                    item {
                        Text(
                            chatState.provider.modelName.ifBlank { "请先在设置中配置 AI 模型" },
                            style = IOSTextStyles.Body,
                            color = AppTheme.colors.secondaryLabel,
                            modifier = Modifier.padding(Spacing.cardPadding),
                        )
                    }
                }
            }
            if (chatState.messages.isNotEmpty()) {
                items(chatState.messages, key = { it.id }) { msg ->
                    MessageBubble(msg.role, msg.content)
                }
            }
            item {
                IOSGroupedSection(header = "操作") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton("去配置 AI 模型", onClick = onOpenSettings)
                            IOSSecondaryButton("新建对话", onClick = { chatVm.newConversation() }, filled = false)
                            IOSSecondaryButton("清空上下文", onClick = { chatVm.clearContext() }, filled = false)
                            IOSSecondaryButton("删除对话", onClick = { chatVm.deleteConversation() }, filled = false)
                        }
                    }
                }
            }
            item {
                IOSGroupedSection(header = "发送消息") {
                    item {
                        IOSFormTextField(
                            value = chatState.input,
                            onValueChange = { chatVm.setInput(it) },
                            placeholder = "输入问题，继续追问当前排盘或普通对话",
                            minLines = 2,
                            singleLine = false,
                        )
                    }
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton(
                                text = if (chatState.isSending) "发送中..." else "发送",
                                enabled = !chatState.isSending,
                                onClick = { chatVm.sendInput() },
                            )
                        }
                    }
                }
            }
            chatState.message?.let { msg ->
                item {
                    Text(
                        msg,
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(role: String, content: String) {
    val isUser = role == "user"
    val clipboard = LocalClipboardManager.current
    val label = when (role) {
        "user" -> "我"
        "assistant" -> "AI"
        "error" -> "错误"
        else -> role
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = Spacing.pageHorizontal)) {
        IOSGroupedSection(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.86f else 1f),
            header = label,
        ) {
            item {
                Text(
                    content,
                    style = IOSTextStyles.Body,
                    color = if (role == "error") AppTheme.colors.clash else AppTheme.colors.label,
                    modifier = Modifier.padding(Spacing.cardPadding),
                )
                if (role == "assistant") {
                    IOSSecondaryButton(
                        text = "复制回复",
                        onClick = { clipboard.setText(AnnotatedString(content)) },
                        modifier = Modifier.padding(horizontal = Spacing.cardPadding, vertical = Spacing.sm),
                        filled = false,
                    )
                }
            }
        }
    }
}
