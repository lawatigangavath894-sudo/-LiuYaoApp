package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 时间信息卡:公历 / 农历 / 四柱干支 / 旬空 / 起卦方式。
 * 用分组卡而非表格;四柱用四个并排的干支小列,旬空与起卦方式用脚标行。
 */
@Composable
fun TimeInfoCard(model: ChartUiModel) {
    IOSGroupedSection(header = "时间") {
        item {
            Column(Modifier.padding(Spacing.cardPadding)) {
                Text(model.gregorian, style = IOSTextStyles.Body, color = AppTheme.colors.label)

                // 四柱干支:四列等距
                Row(
                    Modifier.padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    GanZhiPillar("年", model.ganZhiYear, Modifier.weight(1f))
                    GanZhiPillar("月", model.ganZhiMonth, Modifier.weight(1f))
                    GanZhiPillar("日", model.ganZhiDay, Modifier.weight(1f))
                    GanZhiPillar("时", model.ganZhiHour, Modifier.weight(1f))
                }

                // 旬空 + 起卦方式
                Row(
                    Modifier.padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabeledInline("旬空", model.xunKong)
                    LabeledInline("起卦", model.castMethod)
                }
            }
        }
    }
}

@Composable
private fun GanZhiPillar(label: String, ganZhi: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.md))
            .background(AppTheme.colors.systemBackground)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = IOSTextStyles.Caption, color = AppTheme.colors.tertiaryLabel)
        Text(
            ganZhi,
            style = IOSTextStyles.Headline,
            color = AppTheme.colors.label,
            modifier = Modifier.padding(top = Spacing.xxs),
        )
    }
}

@Composable
private fun LabeledInline(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
        Text(
            value,
            style = IOSTextStyles.Footnote,
            color = AppTheme.colors.secondaryLabel,
            modifier = Modifier.padding(start = Spacing.xs),
        )
    }
}
