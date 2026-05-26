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
                Text(
                    "分层：主结果 ${report.mainResult.size}，过程/条件 ${report.processOrCondition.size}，风险 ${report.riskWarnings.size}，旁参考 ${report.sideReference.size}",
                    style = IOSTextStyles.Footnote,
                    color = AppTheme.colors.secondaryLabel,
                    modifier = Modifier.padding(top = Spacing.md),
                )
                if (report.hasConflict) {
                    Text(
                        "卦中同时存在支持成与支持不成的断语，需要结合关键爻、旺衰与资料片段分轻重，不宜只取单条断语。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                } else if (report.all.isEmpty()) {
                    Text(
                        "暂无命中断语。可在断语库导入资料后，系统会按当前占事类别、主变量、用神和关键爻重新匹配。",
                        style = IOSTextStyles.Footnote,
                        color = AppTheme.colors.tertiaryLabel,
                        modifier = Modifier.padding(top = Spacing.sm),
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
