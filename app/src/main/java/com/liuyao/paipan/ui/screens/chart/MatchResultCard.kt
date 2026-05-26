package com.liuyao.paipan.ui.screens.chart

import com.liuyao.paipan.domain.model.yaoPositionName
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
import com.liuyao.paipan.domain.match.RuleMatchResult
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 单条断语匹配结果卡。收起态精简(原文 + 占类 + 分数 + 收藏);
 * 点击展开显示白话、命中/未命中/排除条件、相关爻位、来源。
 */
@Composable
fun MatchResultCard(
    result: RuleMatchResult,
    favorited: Boolean,
    onToggleFavorite: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IOSCard {
        Column {
            // 头部:原文 + 收藏
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

            // 占类 + 分数
            Row(
                Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IOSBadge(result.rule.category.cn)
                ScorePill(result.score)
                if (result.relatedLineIndexes.isNotEmpty()) {
                    Text(
                        "爻位 " + result.relatedLineIndexes.joinToString(",") { yaoPositionName(it) },
                        style = IOSTextStyles.Caption,
                        color = AppTheme.colors.secondaryLabel,
                    )
                }
            }

            // 白话(收起态也给一行,克制)
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
                    Text(
                        "来源 · ${result.rule.sourceName}",
                        style = IOSTextStyles.Caption,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }

            // 展开/收起提示
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
    val c = when {
        score >= 80 -> AppTheme.colors.world
        score >= 60 -> AppTheme.colors.combine
        else -> AppTheme.colors.secondaryLabel
    }
    IOSBadge("匹配 $score", container = c.copy(alpha = 0.12f), content = c)
}
