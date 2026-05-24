package com.liuyao.paipan.ui.screens.imports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.liuyao.paipan.domain.imports.DraftRule
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.rule.RuleTarget
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSFormPickerRow
import com.liuyao.paipan.ui.components.IOSFormTextField
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/** 类神可选项(对应需求的 8 个类神关键词) */
private val TARGET_OPTIONS: List<Pair<String, RuleTarget>> = listOf(
    "官鬼" to RuleTarget.kin(com.liuyao.paipan.domain.model.SixKin.OFFICIAL),
    "父母" to RuleTarget.kin(com.liuyao.paipan.domain.model.SixKin.PARENT),
    "妻财" to RuleTarget.kin(com.liuyao.paipan.domain.model.SixKin.WEALTH),
    "子孙" to RuleTarget.kin(com.liuyao.paipan.domain.model.SixKin.OFFSPRING),
    "兄弟" to RuleTarget.kin(com.liuyao.paipan.domain.model.SixKin.SIBLING),
    "世爻" to RuleTarget.World,
    "应爻" to RuleTarget.Response,
    "用神" to RuleTarget.UseGod,
)

/**
 * 单条草稿编辑卡。收起态显示原文与「待确认」徽标;展开后可改占类/类神/白话/条件/标签。
 */
@Composable
fun DraftRuleEditor(
    draft: DraftRule,
    onChange: (DraftRule) -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(draft.needsReview) }

    IOSCard {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    draft.originalText,
                    style = IOSTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.label,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    modifier = Modifier.weight(1f).clickableNoRipple { expanded = !expanded },
                )
            }

            // 状态徽标
            Row(
                Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (draft.category != null) {
                    IOSBadge(draft.category.cn)
                }
                if (draft.target != null) {
                    IOSBadge(
                        draft.target.cn,
                        container = AppTheme.colors.world.copy(alpha = 0.12f),
                        content = AppTheme.colors.world,
                    )
                }
                if (draft.needsReview) {
                    IOSBadge(
                        "待确认:${draft.pendingFields.joinToString("/")}",
                        container = AppTheme.colors.clash.copy(alpha = 0.12f),
                        content = AppTheme.colors.clash,
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = Spacing.sm)) {
                    IOSFormPickerRow(
                        label = "占类",
                        options = DivinationCategory.entries.toList(),
                        selected = draft.category ?: DivinationCategory.OTHER,
                        optionLabel = { it.cn },
                        onSelect = { onChange(draft.copy(category = it)) },
                    )
                    IOSFormPickerRow(
                        label = "类神",
                        options = TARGET_OPTIONS,
                        selected = TARGET_OPTIONS.firstOrNull { it.second == draft.target } ?: TARGET_OPTIONS.last(),
                        optionLabel = { it.first },
                        onSelect = { onChange(draft.copy(target = it.second)) },
                    )
                    IOSFormTextField(
                        value = draft.plainExplanation,
                        onValueChange = { onChange(draft.copy(plainExplanation = it)) },
                        placeholder = "补充白话解释",
                        label = "白话解释",
                        singleLine = false,
                        minLines = 2,
                    )
                    IOSFormTextField(
                        value = draft.conditionText,
                        onValueChange = { onChange(draft.copy(conditionText = it)) },
                        placeholder = "适用条件(可空)",
                        label = "适用条件",
                        singleLine = false,
                    )
                    IOSFormTextField(
                        value = draft.tagsText,
                        onValueChange = { onChange(draft.copy(tagsText = it)) },
                        placeholder = "逗号分隔",
                        label = "标签",
                    )
                    IOSSecondaryButton("删除此条", onClick = onRemove, filled = false)
                }
            }

            Text(
                if (expanded) "收起" else "展开编辑",
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.accent,
                modifier = Modifier.padding(top = Spacing.sm).clickableNoRipple { expanded = !expanded },
            )
        }
    }
}
