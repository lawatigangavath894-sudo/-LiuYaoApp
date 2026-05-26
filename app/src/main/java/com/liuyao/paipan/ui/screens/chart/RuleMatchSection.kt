package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.match.ResultBucket
import com.liuyao.paipan.domain.match.RuleMatchResult
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 一个分类区块:标题(色点 + 名称 + 计数)+ 该类断语卡列表。
 * 列表为空时整块不渲染。
 */
@Composable
fun RuleMatchSection(
    bucket: ResultBucket,
    results: List<RuleMatchResult>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) return
    val dotColor = bucketColor(bucket)
    Column(modifier.padding(horizontal = Spacing.pageHorizontal)) {
        Row(
            Modifier.padding(start = Spacing.xs, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(Spacing.sm).clip(CircleShape).background(dotColor))
            Text(
                "${bucket.cn} · ${results.size}",
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            results.forEach { r ->
                MatchResultCard(
                    result = r,
                    favorited = r.rule.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(r.rule.id) },
                )
            }
        }
    }
}

@Composable
fun bucketColor(bucket: ResultBucket): Color = when (bucket) {
    ResultBucket.SUPPORT_YES -> AppTheme.colors.world
    ResultBucket.SUPPORT_NO -> AppTheme.colors.clash
    ResultBucket.NEUTRAL -> AppTheme.colors.secondaryLabel
}

@Composable
fun RuleLayerSection(
    layer: MatchLayer,
    results: List<RuleMatchResult>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) return
    val dotColor = when (layer) {
        MatchLayer.MAIN_RESULT -> AppTheme.colors.world
        MatchLayer.PROCESS -> AppTheme.colors.combine
        MatchLayer.CONDITION -> AppTheme.colors.accent
        MatchLayer.SIDE_REFERENCE -> AppTheme.colors.secondaryLabel
    }
    Column(modifier.padding(horizontal = Spacing.pageHorizontal)) {
        Row(
            Modifier.padding(start = Spacing.xs, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(Spacing.sm).clip(CircleShape).background(dotColor))
            Text(
                "${layer.title} · ${results.size}",
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            results.forEach { r ->
                MatchResultCard(
                    result = r,
                    favorited = r.rule.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(r.rule.id) },
                )
            }
        }
    }
}
