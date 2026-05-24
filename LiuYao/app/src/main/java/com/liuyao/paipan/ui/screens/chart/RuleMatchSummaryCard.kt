package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.match.ResultBucket
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 断语摘要卡:三类计数 + 冲突提示。
 * 当"支持成"与"支持不成"并存时,明确提示不给唯一结论(原则:冲突要同时展示)。
 */
@Composable
fun RuleMatchSummaryCard(report: MatchReport, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.pageHorizontal)) {
        IOSCard {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    CountItem(ResultBucket.SUPPORT_YES, report.supportYes.size)
                    CountItem(ResultBucket.SUPPORT_NO, report.supportNo.size)
                    CountItem(ResultBucket.NEUTRAL, report.neutral.size)
                }
                if (report.hasConflict) {
                    Text(
                        "卦中同时存在支持成与支持不成的断语,需结合具体条件与轻重权衡,不宜只取单一结论。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                } else if (report.all.isEmpty()) {
                    Text(
                        "暂无命中断语。可在「断语库」补充规则后再看。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                }
            }
        }
    }
}

@Composable
private fun CountItem(bucket: ResultBucket, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(Spacing.sm).clip(CircleShape).background(bucketColor(bucket)))
        Text(
            "${bucket.cn} $count",
            style = IOSTextStyles.Subhead.copy(fontWeight = FontWeight.SemiBold),
            color = AppTheme.colors.label,
            modifier = Modifier.padding(start = Spacing.xs),
        )
    }
}
