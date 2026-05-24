package com.liuyao.paipan.ui.screens

import com.liuyao.paipan.domain.model.yaoPositionName
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.chart.ChartAction
import com.liuyao.paipan.ui.screens.chart.ChartViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Radius
import com.liuyao.paipan.ui.theme.Spacing

/**
 * 起卦页(本轮:手动摆爻 + 正时)。
 *
 * 用户手动设置六爻阴阳与动爻,点「起此卦」以"当前时间"为起卦时刻,
 * 通过 [ChartViewModel.dispatch] 下发 [ChartAction.ManualCast],排盘由引擎完成。
 * 本页不含任何术数逻辑。
 *
 * @param onCasted 起卦完成后跳转排盘页
 */
@Composable
fun CastScreen(
    vm: ChartViewModel,
    onBack: () -> Unit,
    onCasted: () -> Unit,
) {
    var question by remember { mutableStateOf("") }
    // 手动六爻状态:index0=初爻。yang/moving。默认六阳(乾为天)。
    val yang = remember { mutableStateListOf(true, true, true, true, true, true) }
    val moving = remember { mutableStateListOf(false, false, false, false, false, false) }

    IOSDetailScaffold(title = "起卦", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            // 占事(本轮用默认标题占位)
            item {
                IOSGroupedSection(
                    header = "占事",
                    footer = "本轮占事用默认标题;文本输入框将在后续接入。",
                ) {
                    item {
                        Text(
                            question.ifBlank { "手动起卦" },
                            style = IOSTextStyles.Body,
                            color = AppTheme.colors.label,
                            modifier = Modifier.padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                        )
                    }
                }
            }

            // 起卦方式说明:正时
            item {
                IOSGroupedSection(header = "起卦方式") {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("正时", style = IOSTextStyles.Body, color = AppTheme.colors.label)
                            Text(
                                "以当前时间为起卦时刻",
                                style = IOSTextStyles.Footnote,
                                color = AppTheme.colors.secondaryLabel,
                            )
                        }
                    }
                }
            }

            // 手动摆爻
            item {
                IOSGroupedSection(
                    header = "手动摆爻(上爻 → 初爻)",
                    footer = "点左侧切阴阳,点「动」标记动爻。",
                ) {
                    (5 downTo 0).forEach { i ->
                        item { ManualYaoRow(i, yang[i], moving[i], { yang[i] = !yang[i] }, { moving[i] = !moving[i] }) }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton(
                        text = "起此卦",
                        onClick = {
                            vm.dispatch(ChartAction.ManualCast(question, yang.toList(), moving.toList()))
                            onCasted()
                        },
                    )
                }
            }
        }
    }
}

/** 手动单爻行:左切阴阳、右标动爻 */
@Composable
private fun ManualYaoRow(
    index: Int,
    yang: Boolean,
    moving: Boolean,
    onToggleYang: () -> Unit,
    onToggleMoving: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            yaoPositionName(index + 1),
            style = IOSTextStyles.Caption,
            color = AppTheme.colors.tertiaryLabel,
            modifier = Modifier.width(32.dp),
        )
        Box(
            Modifier.width(80.dp).clickableNoRipple(onToggleYang),
            contentAlignment = Alignment.CenterStart,
        ) {
            YaoSymbol(yang)
        }
        Text(if (yang) "阳" else "阴", style = IOSTextStyles.Body, color = AppTheme.colors.label)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .clip(RoundedCornerShape(Radius.badge))
                .background(
                    if (moving) AppTheme.colors.moving.copy(alpha = 0.14f) else AppTheme.colors.separator.copy(alpha = 0.4f),
                )
                .clickableNoRipple(onToggleMoving)
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        ) {
            Text(
                "动",
                style = IOSTextStyles.CaptionEmphasized,
                color = if (moving) AppTheme.colors.moving else AppTheme.colors.tertiaryLabel,
                fontWeight = if (moving) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun YaoSymbol(yang: Boolean) {
    Row(Modifier.width(64.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (yang) {
            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(2.dp)).background(AppTheme.colors.label.copy(alpha = 0.75f)))
        } else {
            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(2.dp)).background(AppTheme.colors.label.copy(alpha = 0.75f)))
            Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(2.dp)).background(AppTheme.colors.label.copy(alpha = 0.75f)))
        }
    }
}
