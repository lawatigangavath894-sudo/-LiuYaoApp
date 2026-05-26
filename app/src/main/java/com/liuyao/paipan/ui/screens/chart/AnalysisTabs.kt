package com.liuyao.paipan.ui.screens.chart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.data.ChartMockData
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 分析区:顶部胶囊分段(横向滚动),下方按所选 tab 渲染内容。
 * 神煞/旺衰/批注/案例/占法/取象 用 grouped card 文本;断语用卡片列表;反馈用表单面板。
 */
@Composable
fun AnalysisTabs(
    report: com.liuyao.paipan.domain.match.MatchReport? = null,
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = ChartMockData.analysisTabs

    Column {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
            Text(
                "分析".uppercase(),
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sectionHeaderGap, top = Spacing.sm),
            )
            IOSSegmentedControl(
                options = tabs,
                selectedIndex = selected,
                onSelect = { selected = it },
                scrollable = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "analysis",
            modifier = Modifier.padding(top = Spacing.sectionGap),
        ) { idx ->
            when (tabs[idx]) {
                "神煞" -> ShenShaContent()
                "旺衰" -> BulletContent(ChartMockData.prosperityNotes)
                "批注" -> AnnotationContent()
                "案例" -> CaseContent()
                "占法" -> BulletContent(ChartMockData.zhanFa)
                "取象" -> BulletContent(ChartMockData.quXiang)
                "断语" -> RuleContent(report, favoriteIds, onToggleFavorite)
                "反馈" -> FeedbackPanel()
                else -> BulletContent(emptyList())
            }
        }
    }
}

@Composable
private fun ShenShaContent() {
    IOSGroupedSection {
        ChartMockData.shenSha.forEach { (name, value) ->
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(name, style = IOSTextStyles.Body, color = AppTheme.colors.label)
                    Text(value, style = IOSTextStyles.Body, color = AppTheme.colors.secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun BulletContent(items: List<String>) {
    IOSGroupedSection {
        item {
            Column(Modifier.padding(Spacing.cardPadding)) {
                if (items.isEmpty()) {
                    Text("暂无内容", style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel)
                } else {
                    items.forEachIndexed { i, s ->
                        Text(
                            "· $s",
                            style = IOSTextStyles.Subhead,
                            color = AppTheme.colors.label,
                            modifier = Modifier.padding(top = if (i == 0) 0.dp else Spacing.sm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnotationContent() {
    IOSGroupedSection {
        item {
            Column(Modifier.padding(Spacing.cardPadding)) {
                Text(
                    ChartMockData.annotation,
                    style = IOSTextStyles.Body,
                    color = AppTheme.colors.label,
                )
            }
        }
    }
}

@Composable
private fun CaseContent() {
    IOSGroupedSection(footer = "关联案例点击可跳转(后续阶段)。") {
        ChartMockData.relatedCases.forEach { (title, date) ->
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(title, style = IOSTextStyles.Body, color = AppTheme.colors.label, modifier = Modifier.weight(1f))
                    Text(date, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun RuleContent(
    report: com.liuyao.paipan.domain.match.MatchReport?,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
) {
    if (report == null) {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
            Text("正在分析断语…", style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)) {
        RuleMatchSummaryCard(report)
        RuleLayerSection(
            layer = com.liuyao.paipan.domain.analysis.MatchLayer.MAIN_RESULT,
            results = report.mainResult,
            favoriteIds = favoriteIds,
            onToggleFavorite = onToggleFavorite,
        )
        RuleLayerSection(
            layer = com.liuyao.paipan.domain.analysis.MatchLayer.PROCESS,
            results = report.processOrCondition,
            favoriteIds = favoriteIds,
            onToggleFavorite = onToggleFavorite,
        )
        RuleLayerSection(
            layer = com.liuyao.paipan.domain.analysis.MatchLayer.SIDE_REFERENCE,
            results = report.sideReference,
            favoriteIds = favoriteIds,
            onToggleFavorite = onToggleFavorite,
        )
    }
}
