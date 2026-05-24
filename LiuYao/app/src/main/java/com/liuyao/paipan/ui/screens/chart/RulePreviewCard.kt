package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.liuyao.paipan.data.RulePreview
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 断语预览卡。视觉接近 iOS Notes/Reminders 卡片:
 * 顶部来源 + 占类 + 分数;中部原始断语(强调)与白话解释;底部命中条件 + 展开按钮。
 */
@Composable
fun RulePreviewCard(rule: RulePreview, onExpand: () -> Unit = {}) {
    IOSCard {
        Column {
            // 来源 + 占类 + 分数
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    rule.source,
                    style = IOSTextStyles.Caption,
                    color = AppTheme.colors.secondaryLabel,
                    modifier = Modifier.weight(1f),
                )
                ScoreBadge(rule.score)
            }
            Row(Modifier.padding(top = Spacing.xs), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                IOSBadge(rule.category)
            }

            // 原始断语
            Text(
                rule.original,
                style = IOSTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.label,
                modifier = Modifier.padding(top = Spacing.md),
            )
            // 白话
            Text(
                rule.plain,
                style = IOSTextStyles.Subhead,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(top = Spacing.xs),
            )

            // 命中条件
            Row(
                Modifier.padding(top = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("命中条件", style = IOSTextStyles.Caption, color = AppTheme.colors.tertiaryLabel)
                Text(
                    rule.condition,
                    style = IOSTextStyles.Footnote,
                    color = AppTheme.colors.secondaryLabel,
                    modifier = Modifier.padding(start = Spacing.sm),
                )
            }

            // 展开
            IOSSecondaryButton(
                "展开详情",
                onClick = onExpand,
                filled = false,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val color = when {
        score >= 80 -> AppTheme.colors.world
        score >= 60 -> AppTheme.colors.combine
        else -> AppTheme.colors.secondaryLabel
    }
    IOSBadge("匹配 $score", container = color.copy(alpha = 0.12f), content = color)
}
