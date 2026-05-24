package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.domain.model.CaseVerdict
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSBottomSheet
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.screens.cases.CaseFeedbackPanel
import com.liuyao.paipan.ui.screens.cases.CaseViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 案例详情页(分组卡)。显示占事/起卦时间/原盘摘要/当时命中断语/反馈/验中/误判,
 * 底部「添加反馈」用 bottom sheet。
 */
@Composable
fun CaseDetailScreen(
    vm: CaseViewModel,
    caseId: String,
    onBack: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(caseId) { vm.openDetail(caseId) }
    val detail by vm.detail.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }

    IOSDetailScaffold(title = "案例详情", onBack = onBack) { padding ->
        val d = detail
        if (d == null || d.case.id != caseId) {
            Column(Modifier.padding(padding).padding(Spacing.xl)) {
                Text("加载中…", style = IOSTextStyles.Body, color = AppTheme.colors.secondaryLabel)
            }
            return@IOSDetailScaffold
        }

        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxxl),
        ) {
            item {
                IOSGroupedSection(header = "占事") {
                    item { LabeledRow("标题", d.case.title) }
                    d.case.category?.let { cat ->
                        item { LabeledRow("占类", com.liuyao.paipan.domain.model.DivinationCategory.cnOf(cat)) }
                    }
                    item { LabeledRow("起卦时间", formatEpoch(d.case.castEpoch)) }
                    item { LabeledRow("保存时间", formatEpoch(d.case.createdEpoch)) }
                }
            }

            d.chart?.let { c ->
                item {
                    IOSGroupedSection(header = "原盘") {
                        item { LabeledRow("本卦", c.originalHexName) }
                        item { LabeledRow("变卦", if (c.changedLower != null) "有变卦" else "静卦") }
                        item { LabeledRow("卦宫", c.palace) }
                        item {
                            LabeledRow(
                                "四柱",
                                "${c.yearGanZhi} ${c.monthGanZhi} ${c.dayGanZhi} ${c.hourGanZhi}",
                            )
                        }
                    }
                }
            }

            item {
                IOSGroupedSection(header = "当时命中的断语") {
                    if (d.hitRules.isEmpty()) {
                        item {
                            Text(
                                "无记录",
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.tertiaryLabel,
                                modifier = Modifier.padding(Spacing.cardPadding),
                            )
                        }
                    } else {
                        d.hitRules.forEach { (id, text) ->
                            item {
                                Text(
                                    text,
                                    style = IOSTextStyles.Subhead,
                                    color = AppTheme.colors.label,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                                )
                            }
                        }
                    }
                }
            }

            // 反馈
            val fb = d.feedback
            item {
                IOSGroupedSection(header = "用户反馈") {
                    if (fb == null) {
                        item {
                            Text(
                                "尚未填写反馈",
                                style = IOSTextStyles.Body,
                                color = AppTheme.colors.tertiaryLabel,
                                modifier = Modifier.padding(Spacing.cardPadding),
                            )
                        }
                    } else {
                        item { LabeledRow("结果类型", CaseVerdict.fromName(fb.verdict).cn) }
                        item { LabeledRow("最终结果", fb.actualResult.ifBlank { "—" }) }
                        item { LabeledRow("反馈时间", formatEpoch(fb.feedbackEpoch)) }
                        if (fb.note.isNotBlank()) {
                            item { ParagraphRow("备注", fb.note) }
                        }
                    }
                }
            }

            if (fb != null) {
                item {
                    IOSGroupedSection(header = "规则校验") {
                        item { RuleIdsRow("验中", fb.hitRuleIdsCsv, AppTheme.colors.world, d.hitRules) }
                        item { RuleIdsRow("误判", fb.missRuleIdsCsv, AppTheme.colors.clash, d.hitRules) }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton(
                        text = if (fb == null) "添加反馈" else "修改反馈",
                        onClick = { showSheet = true },
                    )
                }
            }
        }
    }

    if (showSheet) {
        IOSBottomSheet(onDismiss = { showSheet = false }, title = "案例反馈") {
            val d = detail
            CaseFeedbackPanel(
                hitRuleOptions = d?.hitRules ?: emptyList(),
                onSubmit = { input ->
                    vm.submitFeedback(
                        caseId = caseId,
                        verdict = input.verdict,
                        actualResult = input.actualResult,
                        note = input.note,
                        hitRuleIds = input.hitRuleIds,
                        missRuleIds = input.missRuleIds,
                        onDone = { showSheet = false },
                    )
                },
            )
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        Text(value, style = IOSTextStyles.Body, color = AppTheme.colors.secondaryLabel)
    }
}

@Composable
private fun ParagraphRow(label: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical)) {
        Text(label, style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
        Text(text, style = IOSTextStyles.Body, color = AppTheme.colors.label, modifier = Modifier.padding(top = Spacing.xxs))
    }
}

@Composable
private fun RuleIdsRow(label: String, csv: String, accent: androidx.compose.ui.graphics.Color, hitRules: List<Pair<String, String>>) {
    val ids = csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical)) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        if (ids.isEmpty()) {
            Text("无", style = IOSTextStyles.Subhead, color = AppTheme.colors.tertiaryLabel, modifier = Modifier.padding(top = Spacing.xxs))
        } else {
            ids.forEach { id ->
                val text = hitRules.firstOrNull { it.first == id }?.second ?: id
                Text("· $text", style = IOSTextStyles.Subhead, color = accent, modifier = Modifier.padding(top = Spacing.xxs))
            }
        }
    }
}

private fun formatEpoch(epoch: Long): String {
    if (epoch <= 0) return "—"
    return runCatching {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochSecond(epoch))
    }.getOrDefault("—")
}
