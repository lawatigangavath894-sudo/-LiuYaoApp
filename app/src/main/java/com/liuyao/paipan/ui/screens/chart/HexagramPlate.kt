package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Sizes
import com.liuyao.paipan.ui.theme.Spacing
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text

/**
 * 六爻盘:IOSCard 包裹,自上爻而下排列各 YaoLineRow,行间内缩发丝线。
 * 数据来自 [ChartUiModel.yaoLines](已按上爻→初爻排列)。
 */
@Composable
fun HexagramPlate(model: ChartUiModel, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.pageHorizontal)) {
        Text(
            "六爻盘".uppercase(),
            style = IOSTextStyles.Footnote,
            color = AppTheme.colors.secondaryLabel,
            modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sectionHeaderGap, top = Spacing.sm),
        )
        IOSCard(contentPadding = PaddingValues(vertical = Spacing.xs)) {
            Column {
                model.yaoLines.forEachIndexed { index, line ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = Sizes.hairline,
                            color = AppTheme.colors.separator,
                            modifier = Modifier.padding(start = Spacing.rowHorizontal),
                        )
                    }
                    YaoLineRow(line)
                }
            }
        }
    }
}
