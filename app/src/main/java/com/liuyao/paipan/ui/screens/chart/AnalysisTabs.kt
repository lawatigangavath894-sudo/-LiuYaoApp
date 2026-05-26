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
import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.YaoLine
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
    val tabs = listOf("神煞", "旺衰", "批注", "案例", "占法", "取象", "断语", "反馈")

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
                "神煞" -> ShenShaContent(chart, lock)
                "旺衰" -> StrengthContent(chart, lock)
                "批注" -> AnnotationContent(lock)
                "案例" -> CaseContent(lock)
                "占法" -> MethodContent(lock)
                "取象" -> ImageContent(chart, lock)
                "断语" -> RuleContent(report, favoriteIds, onToggleFavorite)
                "反馈" -> FeedbackPanel()
            }
        }
    }
}

@Composable
private fun ShenShaContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    val lines = lockedLines(chart, lock)
    BulletContent(
        if (lines.isEmpty()) listOf("暂无可分析神煞：请先完成排盘。") else lines.map { line ->
            "${positionName(line.index)}：${line.sixGod.displayName()}，与${keyReason(line, lock)}相关。"
        },
    )
}

@Composable
private fun StrengthContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    val lines = lockedLines(chart, lock)
    BulletContent(
        if (lines.isEmpty()) listOf("暂无旺衰分析：未找到关键爻。") else lines.map { line ->
            val flags = buildList {
                add("旺衰${line.status.strength.displayName()}")
                if (line.status.isVoid) add("旬空")
                if (line.status.isMonthBroken) add("月破")
                if (line.status.isDayClashed) add("日冲")
                if (line.status.isCombined) add("合")
                if (line.status.isClashed) add("冲")
                if (line.status.isPunished) add("刑")
                if (line.status.isHarmed) add("害")
            }.joinToString("、")
            "${positionName(line.index)} ${line.sixKin.displayName()}：$flags。关键原因：${keyReason(line, lock)}。"
        },
    )
}

@Composable
private fun AnnotationContent(lock: AnalysisLock?) {
    BulletContent(
        listOf(
            lock?.lockReason ?: "分析锁定生成中。",
            "批注将绑定当前占事、主变量与关键爻；完整编辑将在后续版本开放。",
        ),
    )
}

@Composable
private fun CaseContent(lock: AnalysisLock?) {
    BulletContent(
        listOf(
            "仅检索同类占事、相同主变量、相近用神结构的案例。",
            if (lock == null) "当前暂无锁定结果。" else "暂无同类案例：${lock.category.displayName()} / ${lock.mainVariable}。",
        ),
    )
}

@Composable
private fun MethodContent(lock: AnalysisLock?) {
    val snippets = lock?.knowledgeSnippets.orEmpty()
    BulletContent(
        if (snippets.isEmpty()) {
            listOf("资料不足：未导入或未命中刘昌明资料，暂不展示泛化占法。")
        } else {
            snippets.take(3).map { "${it.sourceName}：${it.originalText.take(90)}" }
        },
    )
}

@Composable
private fun ImageContent(chart: LiuYaoChart?, lock: AnalysisLock?) {
    val lines = lockedLines(chart, lock)
    BulletContent(
        if (lines.isEmpty()) listOf("资料不足：未找到关键爻，暂不展示泛化取象。") else lines.map { line ->
            val hidden = line.hiddenSpirit?.let { "，伏神${it.sixKin.displayName()}${it.naJia.stem.displayName()}${it.naJia.branch.displayName()}" }.orEmpty()
            val flying = line.flyingSpirit?.let { "，飞神${it.sixKin.displayName()}${it.naJia.stem.displayName()}${it.naJia.branch.displayName()}" }.orEmpty()
            "${positionName(line.index)}：${line.sixKin.displayName()}、${line.sixGod.displayName()}、${line.naJia.stem.displayName()}${line.naJia.branch.displayName()}${hidden}${flying}。"
        },
    )
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
private fun RuleContent(
    report: MatchReport?,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
) {
    if (report == null) {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
            Text("正在分析断语...", style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)) {
        RuleMatchSummaryCard(report)
        RuleLayerSection(MatchLayer.MAIN_RESULT, report.mainResult, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.PROCESS, report.processOrCondition, favoriteIds, onToggleFavorite)
        RuleLayerSection(MatchLayer.SIDE_REFERENCE, report.sideReference, favoriteIds, onToggleFavorite)
    }
}

private fun lockedLines(chart: LiuYaoChart?, lock: AnalysisLock?): List<YaoLine> {
    if (chart == null) return emptyList()
    val indexes = lock?.keyLineIndexes.orEmpty().ifEmpty {
        listOf(chart.worldLineIndex, chart.responseLineIndex) + chart.movingLines.map { it.index }
    }.distinct()
    return chart.lines.filter { it.index in indexes }
}

private fun keyReason(line: YaoLine, lock: AnalysisLock?): String = when {
    lock == null -> "当前排盘"
    line.index == lock.worldLineIndex -> "世爻本人"
    line.index == lock.responseLineIndex -> "应爻对方"
    line.index in lock.movingLineIndexes -> "动爻变化"
    line.sixKin == lock.primaryUsefulGod -> "主用神${line.sixKin.displayName()}"
    line.sixKin in lock.secondaryUsefulGods -> "辅助用神${line.sixKin.displayName()}"
    line.index in lock.hiddenSpiritLineIndexes -> "伏神"
    line.index in lock.flyingSpiritLineIndexes -> "飞神"
    else -> "分析锁定"
}

private fun positionName(index: Int): String = when (index) {
    6 -> "上爻"
    5 -> "五爻"
    4 -> "四爻"
    3 -> "三爻"
    2 -> "二爻"
    else -> "初爻"
}
