package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.rule.RuleTarget
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSFormPickerRow
import com.liuyao.paipan.ui.components.IOSFormTextField
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.screens.rules.RuleFormState
import com.liuyao.paipan.ui.screens.rules.RulesViewModel
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 断语编辑页(新增 / 编辑共用)。iOS Form 风:分组卡 + 表单行 + 底部大圆角保存。
 * 仅通过 [RulesViewModel.save] 写入 Room。
 *
 * @param ruleId 为 null 时新增;非空时编辑该规则。
 */
@Composable
fun RuleEditScreen(
    vm: RulesViewModel,
    ruleId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val existing = ruleId?.let { id -> state.rules.firstOrNull { it.id == id } }

    var form by remember(ruleId) {
        mutableStateOf(existing?.let { RuleFormState.from(it) } ?: RuleFormState())
    }

    IOSDetailScaffold(
        title = if (form.isEditing) "编辑断语" else "新增断语",
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "来源与分类") {
                    item {
                        IOSFormTextField(
                            value = form.sourceName,
                            onValueChange = { form = form.copy(sourceName = it) },
                            placeholder = "如:刘昌明《象断六爻》",
                            label = "来源",
                        )
                    }
                    item {
                        IOSFormPickerRow(
                            label = "占类",
                            options = DivinationCategory.entries.toList(),
                            selected = form.category,
                            optionLabel = { it.cn },
                            onSelect = { form = form.copy(category = it) },
                        )
                    }
                    item {
                        IOSFormPickerRow(
                            label = "类神",
                            options = RuleTarget.Type.entries.toList(),
                            selected = form.targetType,
                            optionLabel = { it.cn },
                            onSelect = { form = form.copy(targetType = it) },
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "内容") {
                    item {
                        IOSFormTextField(
                            value = form.originalText,
                            onValueChange = { form = form.copy(originalText = it) },
                            placeholder = "录入断语原文",
                            label = "原文",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                    item {
                        IOSFormTextField(
                            value = form.plainExplanation,
                            onValueChange = { form = form.copy(plainExplanation = it) },
                            placeholder = "用白话解释这条断语",
                            label = "白话解释",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                    item {
                        IOSFormTextField(
                            value = form.conditionText,
                            onValueChange = { form = form.copy(conditionText = it) },
                            placeholder = "什么卦象下适用",
                            label = "适用条件",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                    item {
                        IOSFormTextField(
                            value = form.excludeText,
                            onValueChange = { form = form.copy(excludeText = it) },
                            placeholder = "什么情况下不适用(可空)",
                            label = "不适用条件",
                            singleLine = false,
                            minLines = 1,
                        )
                    }
                    item {
                        IOSFormTextField(
                            value = form.tagsText,
                            onValueChange = { form = form.copy(tagsText = it) },
                            placeholder = "用逗号分隔,如:用神空亡, 应期",
                            label = "标签",
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "属性") {
                    item {
                        IOSFormPickerRow(
                            label = "优先级",
                            options = listOf(10, 25, 50, 75, 100),
                            selected = nearestOf(form.priority, listOf(10, 25, 50, 75, 100)),
                            optionLabel = { it.toString() },
                            onSelect = { form = form.copy(priority = it) },
                        )
                    }
                    item {
                        IOSFormPickerRow(
                            label = "权重",
                            options = listOf(0.3, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0),
                            selected = nearestOfD(form.confidenceWeight, listOf(0.3, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)),
                            optionLabel = { it.toString() },
                            onSelect = { form = form.copy(confidenceWeight = it) },
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton(
                        text = if (form.isEditing) "保存修改" else "保存断语",
                        enabled = form.canSave,
                        onClick = { vm.save(form, onDone = onSaved) },
                    )
                }
            }
        }
    }
}

private fun nearestOf(v: Int, options: List<Int>): Int =
    options.minByOrNull { kotlin.math.abs(it - v) } ?: options.first()

private fun nearestOfD(v: Double, options: List<Double>): Double =
    options.minByOrNull { kotlin.math.abs(it - v) } ?: options.first()
