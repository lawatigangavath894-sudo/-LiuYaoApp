package com.liuyao.paipan.ui.screens.chart

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
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.match.RuleMatchResult
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun MatchResultCard(
    result: RuleMatchResult,
    favorited: Boolean,
    onToggleFavorite: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IOSCard {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    result.rule.originalText,
                    style = IOSTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.label,
                    modifier = Modifier.weight(1f).clickableNoRipple { expanded = !expanded },
                )
                Text(
                    text = if (favorited) "★" else "☆",
                    style = IOSTextStyles.Title3,
                    color = if (favorited) AppTheme.colors.combine else AppTheme.colors.tertiaryLabel,
                    modifier = Modifier.padding(start = Spacing.sm).clickableNoRipple(onToggleFavorite),
                )
            }

            Row(
                Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IOSBadge(result.rule.category.displayName())
                IOSBadge(result.matchLayer.title)
                IOSBadge("置信${result.confidenceLevel}")
                ScorePill(result.score)
            }

            if (result.relatedLineIndexes.isNotEmpty()) {
                Text(
                    "关联爻位：${result.relatedLineIndexes.joinToString("、")}",
                    style = IOSTextStyles.Caption,
                    color = AppTheme.colors.secondaryLabel,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }

            Text(
                result.rule.plainExplanation,
                style = IOSTextStyles.Subhead,
                color = AppTheme.colors.secondaryLabel,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                modifier = Modifier.padding(top = Spacing.sm).clickableNoRipple { expanded = !expanded },
            )

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = Spacing.sm)) {
                    ConditionBlock("命中条件", result.matchedConditions, AppTheme.colors.world)
                    if (result.failedConditions.isNotEmpty()) {
                        ConditionBlock("未命中条件", result.failedConditions, AppTheme.colors.tertiaryLabel)
                    }
                    if (result.excludedByConditions.isNotEmpty()) {
                        ConditionBlock("排除条件", result.excludedByConditions, AppTheme.colors.clash)
                    }
                    if (result.relevanceReason.isNotBlank()) {
                        Text(
                            "关联依据：${result.relevanceReason}",
                            style = IOSTextStyles.Caption,
                            color = AppTheme.colors.secondaryLabel,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                    if (result.lockReason.isNotBlank()) {
                        Text(
                            "分层理由：${result.lockReason}",
                            style = IOSTextStyles.Caption,
                            color = AppTheme.colors.secondaryLabel,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                    if (result.conflictReason != null) {
                        Text(
                            "冲突提示：${result.conflictReason}",
                            style = IOSTextStyles.Caption,
                            color = AppTheme.colors.accent,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                    Text(
                        "来源 · ${result.rule.sourceName}",
                        style = IOSTextStyles.Caption,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }

            Text(
                if (expanded) "收起" else "展开详情",
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.accent,
                modifier = Modifier.padding(top = Spacing.sm).clickableNoRipple { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun ConditionBlock(label: String, conditions: List<RuleCondition>, accent: androidx.compose.ui.graphics.Color) {
    if (conditions.isEmpty()) return
    Column(Modifier.padding(bottom = Spacing.sm)) {
        Text(label, style = IOSTextStyles.Caption, color = AppTheme.colors.tertiaryLabel)
        Text(
            conditions.joinToString("、") { it.cn },
            style = IOSTextStyles.Footnote,
            color = accent,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ScorePill(score: Int) {
    val color = when {
        score >= 80 -> AppTheme.colors.world
        score >= 60 -> AppTheme.colors.combine
        else -> AppTheme.colors.secondaryLabel
    }
    IOSBadge("匹配 $score", container = color.copy(alpha = 0.12f), content = color)
}
