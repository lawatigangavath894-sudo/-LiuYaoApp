package com.liuyao.paipan.ui.screens.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.data.YaoLineData
import com.liuyao.paipan.data.YaoTag
import com.liuyao.paipan.data.wuXingOf
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import com.liuyao.paipan.ui.theme.tagColors
import com.liuyao.paipan.ui.theme.wuXingColor

/**
 * 单爻行。布局(由左到右):
 *  六神 | 爻象(阴阳横线 + 动点) | 六亲·纳甲(五行着色) | 变爻 | 旺衰 | 关系/世应/动 Badge 群
 * 伏神(若有)以第二行小字缩进显示。
 */
@Composable
fun YaoLineRow(data: YaoLineData) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.rowHorizontal, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 六神
            Text(
                data.liuShen,
                style = IOSTextStyles.Caption,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.width(32.dp),
            )

            Spacer(Modifier.width(Spacing.sm))

            // 爻象:阴阳线
            YaoSymbol(yang = data.yang, moving = data.moving)

            Spacer(Modifier.width(Spacing.md))

            // 六亲 + 纳甲,五行着色的地支
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(120.dp)) {
                Text(
                    data.liuQin,
                    style = IOSTextStyles.Body.copy(
                        fontWeight = if (data.moving) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (data.moving) AppTheme.colors.moving else AppTheme.colors.label,
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    data.ganZhi,
                    style = IOSTextStyles.Subhead,
                    color = wuXingColor(wuXingOf(data.branch)),
                )
            }

            // 变爻
            if (data.changedLiuQin != null && data.changedGanZhi != null) {
                Text(
                    "→ ${data.changedLiuQin}${data.changedGanZhi}",
                    style = IOSTextStyles.Footnote,
                    color = AppTheme.colors.secondaryLabel,
                )
            }

            Spacer(Modifier.weight(1f))

            // 旺衰
            Text(
                data.prosperity,
                style = IOSTextStyles.Caption,
                color = AppTheme.colors.tertiaryLabel,
                modifier = Modifier.padding(end = Spacing.sm),
            )

            // Badge 群:世/应/动 + 关系
            YaoBadges(data)
        }

        // 伏神行(缩进)
        if (data.fuShen != null) {
            Row(
                Modifier.padding(start = 40.dp, top = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("伏 ", style = IOSTextStyles.Caption, color = AppTheme.colors.tertiaryLabel)
                Text(data.fuShen, style = IOSTextStyles.Caption, color = AppTheme.colors.secondaryLabel)
                if (data.feiShen != null) {
                    Text(
                        "  飞 ${data.feiShen}",
                        style = IOSTextStyles.Caption,
                        color = AppTheme.colors.tertiaryLabel,
                    )
                }
            }
        }
    }
}

/** 阴阳爻象:阳为整条,阴为断开两段;动爻在右侧加一个朱砂小圆点。 */
@Composable
private fun YaoSymbol(yang: Boolean, moving: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(34.dp)
                .height(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (yang) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(Spacing.xxs))
                        .background(AppTheme.colors.label.copy(alpha = 0.75f)),
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(Spacing.xxs))
                            .background(AppTheme.colors.label.copy(alpha = 0.75f)),
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(Spacing.xxs))
                            .background(AppTheme.colors.label.copy(alpha = 0.75f)),
                    )
                }
            }
        }
        Spacer(Modifier.width(Spacing.xs))
        Box(
            Modifier.size(Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            if (moving) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(Spacing.badge))
                        .background(AppTheme.colors.moving),
                )
            }
        }
    }
}

@Composable
private fun YaoBadges(data: YaoLineData) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        if (data.isWorld) {
            IOSBadge("世", container = AppTheme.colors.world.copy(alpha = 0.12f), content = AppTheme.colors.world)
        }
        if (data.isResponse) {
            IOSBadge("应", container = AppTheme.colors.response.copy(alpha = 0.12f), content = AppTheme.colors.response)
        }
        if (data.moving) {
            IOSBadge("动", container = AppTheme.colors.moving.copy(alpha = 0.12f), content = AppTheme.colors.moving)
        }
        data.relations.forEach { tag ->
            if (tag != YaoTag.HIDDEN) { // 伏单独成行,不在此重复
                val (bg, fg) = tagColors(tag)
                IOSBadge(tag.label, container = bg, content = fg)
            }
        }
    }
}
