package com.liuyao.paipan.ui.screens.cases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.liuyao.paipan.domain.model.CaseVerdict
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Radius
import com.liuyao.paipan.ui.theme.Spacing

/** 反馈提交数据 */
data class FeedbackInput(
    val verdict: CaseVerdict,
    val actualResult: String,
    val note: String,
    val hitRuleIds: List<String>,
    val missRuleIds: List<String>,
)

/**
 * 案例反馈表单(iOS Form 风,置于 bottom sheet)。
 *
 * @param hitRuleOptions 当时命中的断语 (id, 原文) 供勾选"验中/误判"。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaseFeedbackPanel(
    hitRuleOptions: List<Pair<String, String>>,
    initial: FeedbackInput? = null,
    onSubmit: (FeedbackInput) -> Unit,
) {
    val verdicts = CaseVerdict.entries.toList()
    var verdictIdx by remember { mutableStateOf(verdicts.indexOf(initial?.verdict ?: CaseVerdict.UNKNOWN)) }
    var actual by remember { mutableStateOf(initial?.actualResult ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    val hitSet = remember { mutableStateOf(initial?.hitRuleIds?.toSet() ?: emptySet()) }
    val missSet = remember { mutableStateOf(initial?.missRuleIds?.toSet() ?: emptySet()) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionLabel("结果类型")
        IOSSegmentedControl(
            options = verdicts.map { it.cn },
            selectedIndex = verdictIdx.coerceAtLeast(0),
            onSelect = { verdictIdx = it },
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("最终结果")
        FormField(actual, { actual = it }, "如:面试通过,已入职")

        SectionLabel("备注")
        FormField(note, { note = it }, "补充说明(可空)", minLines = 2)

        if (hitRuleOptions.isNotEmpty()) {
            SectionLabel("验中的断语(点选)")
            ChipMultiSelect(hitRuleOptions, hitSet.value, AppTheme.colors.world) { id ->
                hitSet.value = hitSet.value.toggle(id)
                if (id in hitSet.value) missSet.value = missSet.value - id
            }
            SectionLabel("误判的断语(点选)")
            ChipMultiSelect(hitRuleOptions, missSet.value, AppTheme.colors.clash) { id ->
                missSet.value = missSet.value.toggle(id)
                if (id in missSet.value) hitSet.value = hitSet.value - id
            }
        }

        IOSPrimaryButton(
            text = "保存反馈",
            onClick = {
                onSubmit(
                    FeedbackInput(
                        verdict = verdicts[verdictIdx.coerceAtLeast(0)],
                        actualResult = actual.trim(),
                        note = note.trim(),
                        hitRuleIds = hitSet.value.toList(),
                        missRuleIds = missSet.value.toList(),
                    ),
                )
            },
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

private fun Set<String>.toggle(x: String): Set<String> = if (x in this) this - x else this + x

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
}

@Composable
private fun FormField(value: String, onChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.small))
            .background(AppTheme.colors.systemBackground)
            .padding(Spacing.md),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = minLines == 1,
            minLines = minLines,
            textStyle = IOSTextStyles.Body.merge(TextStyle(color = AppTheme.colors.label)),
            cursorBrush = SolidColor(AppTheme.colors.accent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = IOSTextStyles.Body, color = AppTheme.colors.tertiaryLabel)
                }
                inner()
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipMultiSelect(
    options: List<Pair<String, String>>,
    selected: Set<String>,
    accent: androidx.compose.ui.graphics.Color,
    onToggle: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        options.forEach { (id, text) ->
            val on = id in selected
            IOSBadge(
                text = text.take(12),
                container = if (on) accent.copy(alpha = 0.16f) else AppTheme.colors.separator.copy(alpha = 0.3f),
                content = if (on) accent else AppTheme.colors.secondaryLabel,
                modifier = Modifier.clickableNoRipple { onToggle(id) },
            )
        }
    }
}
