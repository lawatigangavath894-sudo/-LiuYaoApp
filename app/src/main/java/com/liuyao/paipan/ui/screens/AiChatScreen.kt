package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.liuyao.paipan.ui.theme.Radius
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
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) chatVm.reloadProvider()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(chartForPrompt?.id) {
        chatVm.reloadProvider()
        chartForPrompt?.let { chart ->
            analysisVm.analyze(chart, chart.category ?: DivinationCategory.OTHER)
        }
    }

    val prompt = remember(chartForPrompt, analysisState.analysisLock, analysisState.report) {
        val chart = chartForPrompt
        val lock = analysisState.analysisLock
        if (chart != null && lock != null) AiChartPromptBuilder.build(chart, lock, analysisState.report) else null
    }
    LaunchedEffect(chartId, prompt, chatState.provider.baseUrl, chatState.provider.apiKey, chatState.provider.modelName) {
        chatVm.autoAnalyzeChart(chartId, prompt)
    }

    val providerConfigured = chatState.provider.baseUrl.isNotBlank() &&
        chatState.provider.apiKey.isNotBlank() &&
        chatState.provider.modelName.isNotBlank()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    IOSDetailScaffold(
        title = if (chartId.isNullOrBlank()) "AI 对话" else "AI 解析",
        onBack = onBack,
        trailing = {
            AiChatMenu(
                expanded = menuOpen,
                onExpandedChange = { menuOpen = it },
                onNew = {
                    chatVm.newConversation()
                    menuOpen = false
                },
                onClear = {
                    confirmClear = true
                    menuOpen = false
                },
                onDelete = {
                    confirmDelete = true
                    menuOpen = false
                },
                onSelectModel = {
                    chatVm.showModelSelectTodo()
                    menuOpen = false
                },
                onSettings = {
                    menuOpen = false
                    onOpenSettings()
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                ModelBadge(
                    modelName = chatState.provider.modelName.ifBlank { "未配置模型" },
                    isFromChart = !chartId.isNullOrBlank(),
                    category = chartForPrompt?.category,
                )
            }

            if (!providerConfigured) {
                item {
                    IOSGroupedSection {
                        item {
                            Column(
                                Modifier.padding(Spacing.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                Text("请先在设置中配置 AI 模型", style = IOSTextStyles.Body, color = AppTheme.colors.label)
                                IOSPrimaryButton("去设置", onClick = onOpenSettings)
                            }
                        }
                    }
                }
            }

            if (chartId != null && chartForPrompt == null) {
                item {
                    Text(
                        "未找到当前排盘，请从排盘页重新进入 AI 解析。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }

            if (chatState.messages.isEmpty()) {
                item {
                    Text(
                        if (providerConfigured) "暂无消息，可以开始提问。" else "配置完成后即可开始对话。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            } else {
                items(chatState.messages, key = { it.id }) { msg ->
                    MessageBubble(
                        role = msg.role,
                        content = if (chartId != null && msg.role == "user" && msg.content == prompt) {
                            "已发送当前排盘与断语资料"
                        } else {
                            msg.content
                        },
                        fullContent = msg.content,
                        onRetry = chatVm::retryLastMessage,
                    )
                }
            }

            item {
                IOSGroupedSection {
                    item {
                        IOSFormTextField(
                            value = chatState.input,
                            onValueChange = { if (providerConfigured) chatVm.setInput(it) },
                            placeholder = if (providerConfigured) "输入问题，继续追问当前排盘或普通对话" else "请先配置 AI 模型",
                            minLines = 2,
                            singleLine = false,
                        )
                    }
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSPrimaryButton(
                                text = if (chatState.isSending) "发送中..." else "发送",
                                enabled = providerConfigured && !chatState.isSending && chatState.input.isNotBlank(),
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

    if (confirmClear) {
        ConfirmDialog(
            title = "清空上下文",
            text = "确认清空当前对话消息？Provider 和历史配置不会删除。",
            confirmText = "清空",
            onConfirm = {
                chatVm.clearContext()
                confirmClear = false
            },
            onDismiss = { confirmClear = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "删除对话",
            text = "确认删除当前对话？删除后会保留一个空白新对话。",
            confirmText = "删除",
            destructive = true,
            onConfirm = {
                chatVm.deleteConversation()
                confirmDelete = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun AiChatMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onNew: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onSelectModel: () -> Unit,
    onSettings: () -> Unit,
) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = AppTheme.colors.accent)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(AppTheme.colors.card, RoundedCornerShape(Radius.card)),
        ) {
            DropdownMenuItem(text = { Text("新建对话") }, onClick = onNew)
            DropdownMenuItem(text = { Text("选择模型") }, onClick = onSelectModel)
            DropdownMenuItem(text = { Text("AI 设置") }, onClick = onSettings)
            DropdownMenuItem(text = { Text("清空上下文") }, onClick = onClear)
            DropdownMenuItem(
                text = { Text("删除对话", color = AppTheme.colors.clash) },
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ModelBadge(modelName: String, isFromChart: Boolean, category: DivinationCategory?) {
    Row(
        Modifier
            .padding(horizontal = Spacing.pageHorizontal)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Badge(modelName)
        if (isFromChart) Badge("来自排盘")
        category?.let { Badge(it.cn) }
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        Modifier
            .background(AppTheme.colors.accentSoft, RoundedCornerShape(Radius.badge))
            .padding(horizontal = Spacing.md, vertical = 5.dp),
    ) {
        Text(text, style = IOSTextStyles.CaptionEmphasized, color = AppTheme.colors.accent)
    }
}

@Composable
private fun MessageBubble(role: String, content: String, fullContent: String, onRetry: () -> Unit) {
    val isUser = role == "user"
    val clipboard = LocalClipboardManager.current
    val label = when (role) {
        "user" -> "我"
        "assistant" -> "AI"
        "error" -> "错误"
        else -> role
    }
    var expanded by remember(fullContent) { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(horizontal = Spacing.pageHorizontal)) {
        IOSGroupedSection(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.86f else 1f),
            header = label,
        ) {
            item {
                Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        if (expanded) fullContent else content,
                        style = IOSTextStyles.Body,
                        color = if (role == "error") AppTheme.colors.clash else AppTheme.colors.label,
                    )
                    if (content != fullContent) {
                        IOSSecondaryButton(
                            text = if (expanded) "收起 Prompt" else "展开 Prompt",
                            onClick = { expanded = !expanded },
                            filled = false,
                        )
                    }
                    when (role) {
                        "assistant" -> IOSSecondaryButton(
                            text = "复制回复",
                            onClick = { clipboard.setText(AnnotatedString(fullContent)) },
                            filled = false,
                        )
                        "error" -> IOSSecondaryButton(
                            text = "重试",
                            onClick = onRetry,
                            filled = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (destructive) AppTheme.colors.clash else AppTheme.colors.accent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppTheme.colors.secondaryLabel)
            }
        },
        containerColor = AppTheme.colors.card,
        titleContentColor = AppTheme.colors.label,
        textContentColor = AppTheme.colors.secondaryLabel,
    )
}
