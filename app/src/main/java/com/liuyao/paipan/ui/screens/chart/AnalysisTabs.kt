package com.liuyao.paipan.ui.screens.chart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.domain.analysis.AnalysisItem
import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.CaseSimilarityEngine
import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.analysis.MethodAnalysisEngine
import com.liuyao.paipan.domain.analysis.ShenShaAnalysisEngine
import com.liuyao.paipan.domain.analysis.StrengthAnalysisEngine
import com.liuyao.paipan.domain.analysis.XiangAnalysisEngine
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun AnalysisTabs(
    chart: LiuYaoChart?,
    lock: AnalysisLock?,
    report: MatchReport? = null,
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = listOf("神煞", "旺衰", "批注", "案例", "占法", "取象", "断语", "AI", "反馈")

    Column {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
            Text(
                "分析",
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sectionHeaderGap, top = Spacing.sm),
            )
            IOSSegmentedControl(
                options = tabs,
                selectedIndex = selected,
                onSelect = { selected = it },
                scrollable = true,
            )
        }

        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "analysis",
            modifier = Modifier.padding(top = Spacing.sectionGap),
        ) { idx ->
            when (tabs[idx]) {
                "神煞" -> ShenShaContent(chart, lock)
                "旺衰" -> StrengthContent(chart, lock)
                "批注" -> AnnotationContent(lock)
                "案例" -> CaseContent(lock)
                "占法" -> MethodContent(lock)
                "取象" -> XiangContent(chart, lock)
                "断语" -> RuleContent(report, favoriteIds, onToggleFavorite)
                "AI" -> AiContent(lock, report)
                "反馈" -> FeedbackPanel()
            }
        }
    }
}

@Composable
private fun ShenShaContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    if (chart == null || lock == null) {
        ItemContent(emptyList(), "暂无神煞分析：请先完成排盘和分析锁定。")
        return
    }
    ItemContent(
        items = ShenShaAnalysisEngine.analyze(chart, lock),
        empty = "没有与当前占事、主用神或关键爻相关的神煞；不展示神煞大全。",
    )
}

@Composable
private fun StrengthContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    if (chart == null || lock == null) {
        ItemContent(emptyList(), "暂无旺衰分析：未找到关键爻。")
        return
    }
    ItemContent(
        items = StrengthAnalysisEngine.analyze(chart, lock),
        empty = "没有可分析的主用神、世应、动变、伏飞或空破冲合关键爻。",
    )
}

@Composable
private fun AnnotationContent(lock: AnalysisLock?) {
    val items = lock?.let {
        listOf(
            AnalysisItem(
                title = "当前批注上下文",
                body = "${it.category.displayName()} / ${it.mainVariable}；关键爻：${it.keyLineIndexes.joinToString("、").ifBlank { "未定" }}。",
                reason = "批注绑定当前 chartId=${it.chartId} 与 AnalysisLock；完整关键爻/断语绑定编辑后续开放。",
                layer = MatchLayer.SIDE_REFERENCE,
            ),
        )
    }.orEmpty()
    ItemContent(items, "暂无批注上下文。")
}

@Composable
private fun CaseContent(lock: AnalysisLock?) {
    if (lock == null) {
        ItemContent(emptyList(), "暂无案例匹配：请先完成分析锁定。")
    } else {
        ItemContent(CaseSimilarityEngine.placeholder(lock), "暂无同类案例。")
    }
}

@Composable
private fun MethodContent(lock: AnalysisLock?) {
    if (lock == null) {
        ItemContent(emptyList(), "暂无占法资料：请先完成分析锁定。")
    } else {
        ItemContent(
            MethodAnalysisEngine.analyze(lock),
            "未检索到当前占类的占法资料，请导入相关断语资料。",
        )
    }
}

@Composable
private fun XiangContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    if (chart == null || lock == null) {
        ItemContent(emptyList(), "暂无取象：请先完成排盘和分析锁定。")
    } else {
        ItemContent(
            XiangAnalysisEngine.analyze(chart, lock),
            "未找到当前占事相关的关键爻取象；不展示六神、六亲、地支大全。",
        )
    }
}

@Composable
private fun AiContent(lock: AnalysisLock?, report: MatchReport?) {
    val items = lock?.let {
        listOf(
            AnalysisItem(
                title = "AI 解析上下文已锁定",
                body = "Prompt 将包含占事类别、问题、主变量、主用神、世应、关键爻、旺衰摘要、神煞摘要、主结果断语、过程条件断语和最多 10 条资料片段。",
                reason = "断语 ${report?.all?.size ?: 0} 条；资料片段 ${it.knowledgeSnippets.size} 条；不会发送整本书或无关断语。",
                layer = MatchLayer.MAIN_RESULT,
            ),
        )
    }.orEmpty()
    ItemContent(items, "AI 解析需要先生成 AnalysisLock。")
}

@Composable
private fun ItemContent(items: List<AnalysisItem>, empty: String) {
    IOSGroupedSection {
        item {
            Column(Modifier.padding(Spacing.cardPadding)) {
                if (items.isEmpty()) {
                    Text(empty, style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel)
                } else {
                    items.forEachIndexed { index, item ->
                        Text(
                            item.title,
                            style = IOSTextStyles.Subhead,
                            color = AppTheme.colors.label,
                            modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.md),
                        )
                        Text(
                            item.body,
                            style = IOSTextStyles.Footnote,
                            color = AppTheme.colors.secondaryLabel,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "依据：${item.reason}",
                            style = IOSTextStyles.Caption,
                            color = AppTheme.colors.tertiaryLabel,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleContent(
    report: MatchReport?,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
) {
    if (report == null) {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
            Text("正在按分析锁定匹配断语...", style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)) {
        RuleMatchSummaryCard(report)
        RuleLayerSection(MatchLayer.MAIN_RESULT, report.mainResult, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.PROCESS, report.processOrCondition, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.RISK_WARNING, report.riskWarnings, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.SIDE_REFERENCE, report.sideReference, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.INSUFFICIENT_DATA, report.insufficientData, favoriteIds, onToggleFavorite)
    }
}
