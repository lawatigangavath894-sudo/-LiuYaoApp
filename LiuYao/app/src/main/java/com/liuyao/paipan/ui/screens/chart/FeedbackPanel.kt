package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.liuyao.paipan.data.ChartMockData
import com.liuyao.paipan.data.FeedbackData
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import androidx.compose.material3.Text

/**
 * 反馈面板。视觉接近 iOS 表单:分组行(最终结果/反馈时间/备注/验中/误判)+ 底部保存按钮。
 * 本轮为静态展示,所有行不接真实编辑逻辑。
 */
@Composable
fun FeedbackPanel(data: FeedbackData = ChartMockData.feedback) {
    Column {
        IOSGroupedSection(header = "反馈") {
            item { IOSListRow("最终结果", value = data.result, showChevron = true, onClick = {}) }
            item { IOSListRow("反馈时间", value = data.time, showChevron = true, onClick = {}) }
            item {
                Column(Modifier.padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical)) {
                    Text("备注", style = IOSTextStyles.Body, color = AppTheme.colors.label)
                    Text(
                        data.note.ifBlank { "—" },
                        style = IOSTextStyles.Subhead,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(top = Spacing.xxs),
                    )
                }
            }
        }

        IOSGroupedSection(
            header = "断语校验",
            footer = "保存后将用于后续断语权重修正(后续阶段)。",
            modifier = Modifier.padding(top = Spacing.sectionGap),
        ) {
            item {
                RuleCheckRow("验中断语", data.hitRules, AppTheme.colors.world)
            }
            item {
                RuleCheckRow("误判断语", data.missRules, AppTheme.colors.clash)
            }
        }

        Column(Modifier.padding(horizontal = Spacing.pageHorizontal, vertical = Spacing.sectionGap)) {
            IOSPrimaryButton("保存反馈", onClick = {})
        }
    }
}

@Composable
private fun RuleCheckRow(title: String, rules: List<String>, accent: androidx.compose.ui.graphics.Color) {
    Column(Modifier.padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical)) {
        Text(title, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        if (rules.isEmpty()) {
            Text(
                "尚未标记",
                style = IOSTextStyles.Subhead,
                color = AppTheme.colors.tertiaryLabel,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        } else {
            rules.forEach {
                Text(
                    "· $it",
                    style = IOSTextStyles.Subhead,
                    color = accent,
                    modifier = Modifier.padding(top = Spacing.xxs),
                )
            }
        }
    }
}
