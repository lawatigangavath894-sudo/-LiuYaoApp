package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSFormTextField
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.screens.ai.AiSettingsViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AiSettingsScreen(onBack: () -> Unit) {
    val vm: AiSettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by vm.ui.collectAsStateWithLifecycle()
    val config = state.config

    IOSDetailScaffold(title = "AI 大模型设置", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "Provider") {
                    item {
                        IOSFormTextField(
                            label = "供应商名称",
                            value = config.providerName,
                            onValueChange = { vm.update(config.copy(providerName = it)) },
                            placeholder = "OpenAI-compatible",
                        )
                    }
                    item {
                        IOSFormTextField(
                            label = "Base URL",
                            value = config.baseUrl,
                            onValueChange = { vm.update(config.copy(baseUrl = it)) },
                            placeholder = "https://api.openai.com/v1",
                        )
                    }
                    item {
                        IOSFormTextField(
                            label = "API Key",
                            value = config.apiKey,
                            onValueChange = { vm.update(config.copy(apiKey = it)) },
                            placeholder = "由用户自行填写",
                            visualTransformation = if (state.showApiKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        )
                    }
                    item {
                        IOSFormTextField(
                            label = "模型名",
                            value = config.modelName,
                            onValueChange = { vm.update(config.copy(modelName = it)) },
                            placeholder = "gpt-4o-mini / deepseek-chat / 自定义模型",
                        )
                    }
                }
            }
            item {
                IOSGroupedSection(header = "操作", footer = "API Key 当前使用应用私有偏好保存；TODO：发布前迁移到 Keystore / EncryptedDataStore。") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            IOSSecondaryButton(
                                text = if (state.showApiKey) "隐藏 API Key" else "显示 API Key",
                                onClick = { vm.toggleApiKey() },
                                filled = false,
                            )
                            IOSPrimaryButton("保存配置", onClick = { vm.save() })
                            IOSSecondaryButton(
                                text = if (state.isTesting) "测试中..." else "测试连接",
                                onClick = { vm.testConnection() },
                                filled = false,
                            )
                            IOSSecondaryButton("删除 Provider", onClick = { vm.deleteProvider() }, filled = false)
                        }
                    }
                }
            }
            state.message?.let { msg ->
                item {
                    Text(
                        msg,
                        style = IOSTextStyles.Footnote,
                        color = if (msg.contains("成功") || msg.contains("已保存")) AppTheme.colors.accent else AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(horizontal = Spacing.pageHorizontal),
                    )
                }
            }
        }
    }
}
