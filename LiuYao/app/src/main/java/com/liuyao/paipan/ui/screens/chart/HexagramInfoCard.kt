package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 卦象信息卡:本卦 → 变卦(大字),卦宫·五行,世应,六冲/六合 Badge。
 */
@Composable
fun HexagramInfoCard(model: ChartUiModel) {
    IOSGroupedSection(header = "卦象") {
        item {
            Column(Modifier.padding(Spacing.cardPadding)) {
                // 本卦 → 变卦
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(model.benHex, style = IOSTextStyles.Title2, color = AppTheme.colors.label)
                    Text(
                        "  →  ",
                        style = IOSTextStyles.Title3,
                        color = AppTheme.colors.tertiaryLabel,
                    )
                    Text((model.bianHex ?: "—"), style = IOSTextStyles.Title2, color = AppTheme.colors.accent)
                }

                // 卦宫·五行 + 世应
                Row(
                    Modifier.padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Text(
                        "${model.palace} · ${model.palaceElement}",
                        style = IOSTextStyles.Subhead,
                        color = AppTheme.colors.secondaryLabel,
                    )
                    Text(
                        model.worldResponse,
                        style = IOSTextStyles.Subhead,
                        color = AppTheme.colors.secondaryLabel,
                    )
                }

                // 卦性标识
                if (model.hexNature != "无") {
                    val natureColor = if (model.hexNature == "六合") AppTheme.colors.combine else AppTheme.colors.clash
                    Row(Modifier.padding(top = Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        IOSBadge(
                            model.hexNature,
                            container = natureColor.copy(alpha = 0.12f),
                            content = natureColor,
                        )
                    }
                }
            }
        }
    }
}
