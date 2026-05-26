package com.liuyao.paipan.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.ui.components.IOSBadge
import com.liuyao.paipan.ui.components.IOSCard
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.IOSSegmentedControl
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import androidx.compose.material3.Text

// ════════════════════════════════════════════════════════════════════
// 组件画廊:逐个展示设计系统组件
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ComponentGallery() {
    var segIndex by remember { mutableStateOf(1) }
    var tabIndex by remember { mutableStateOf(0) }
    val analysisTabs = listOf("神煞", "旺衰", "批注", "案例", "占法", "取象", "断语", "反馈")

    IOSLargeTitleScaffold(
        title = "设计系统",
        trailing = { IOSSecondaryButton("完成", onClick = {}, filled = false) },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            // 文本层级
            item {
                IOSGroupedSection(header = "字体层级") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding)) {
                            Text("Large Title", style = IOSTextStyles.LargeTitle, color = AppTheme.colors.label)
                            Text("Title 1", style = IOSTextStyles.Title1, color = AppTheme.colors.label)
                            Text("Title 2", style = IOSTextStyles.Title2, color = AppTheme.colors.label)
                            Text("Headline", style = IOSTextStyles.Headline, color = AppTheme.colors.label)
                            Text("Body — 正文示例文字", style = IOSTextStyles.Body, color = AppTheme.colors.label)
                            Text("Footnote — 脚注示例", style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
                        }
                    }
                }
            }
            // 分段控件
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    Text("分段控件(等分)", style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(bottom = Spacing.sm))
                    IOSSegmentedControl(
                        options = listOf("正时", "选择时间", "手动"),
                        selectedIndex = segIndex, onSelect = { segIndex = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("分段控件(横向滚动)", style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel,
                        modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.sm))
                    IOSSegmentedControl(
                        options = analysisTabs, selectedIndex = tabIndex, onSelect = { tabIndex = it },
                        scrollable = true,
                    )
                }
            }
            // Badge 全家福(状态色)
            item {
                IOSGroupedSection(header = "状态 Badge") {
                    item {
                        Row(
                            Modifier.padding(Spacing.cardPadding),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            IOSBadge("世", container = AppTheme.colors.world.copy(alpha = 0.12f), content = AppTheme.colors.world)
                            IOSBadge("应", container = AppTheme.colors.response.copy(alpha = 0.12f), content = AppTheme.colors.response)
                            IOSBadge("动", container = AppTheme.colors.moving.copy(alpha = 0.12f), content = AppTheme.colors.moving)
                            IOSBadge("空", container = AppTheme.colors.empty.copy(alpha = 0.14f), content = AppTheme.colors.empty)
                            IOSBadge("破", container = AppTheme.colors.breakState.copy(alpha = 0.12f), content = AppTheme.colors.breakState)
                            IOSBadge("合", container = AppTheme.colors.combine.copy(alpha = 0.12f), content = AppTheme.colors.combine)
                            IOSBadge("冲", container = AppTheme.colors.clash.copy(alpha = 0.12f), content = AppTheme.colors.clash)
                        }
                    }
                }
            }
            // 列表行
            item {
                IOSGroupedSection(header = "列表行") {
                    item { IOSListRow("普通行", value = "右值", showChevron = true, onClick = {}) }
                    item { IOSListRow("带 Badge", badge = "新", showChevron = true, onClick = {}) }
                    item { IOSListRow("仅标题", showChevron = false) }
                }
            }
            // 卡片 + 按钮
            item {
                Column(
                    Modifier.padding(horizontal = Spacing.pageHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    IOSCard {
                        Column {
                            Text("通用卡片 IOSCard", style = IOSTextStyles.Headline, color = AppTheme.colors.label)
                            Text("白底、柔和圆角、无重阴影。", style = IOSTextStyles.Subhead, color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(top = Spacing.xs))
                        }
                    }
                    IOSPrimaryButton("主操作按钮", onClick = {})
                    IOSSecondaryButton("次操作按钮", onClick = {})
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 示例页拼装:模拟一个"卦象信息"页(纯静态,无业务)
// ════════════════════════════════════════════════════════════════════

@Composable
private fun SampleAssembledScreen() {
    IOSLargeTitleScaffold(title = "求测:能否拿到 offer") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            item {
                IOSGroupedSection(header = "时间") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding)) {
                            Text("公历 2026-05-22 14:09 · 农历四月初六", style = IOSTextStyles.Subhead, color = AppTheme.colors.secondaryLabel)
                            Text("丙午年 癸巳月 丙申日 乙未时", style = IOSTextStyles.Body, color = AppTheme.colors.label,
                                modifier = Modifier.padding(top = Spacing.xs))
                            Row(Modifier.padding(top = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                IOSBadge("旬空 辰巳", container = AppTheme.colors.empty.copy(alpha = 0.14f), content = AppTheme.colors.empty)
                                IOSBadge("六冲", container = AppTheme.colors.clash.copy(alpha = 0.12f), content = AppTheme.colors.clash)
                            }
                        }
                    }
                }
            }
            item {
                IOSGroupedSection(header = "卦象") {
                    item {
                        Column(Modifier.padding(Spacing.cardPadding)) {
                            Text("乾为天 → 天风姤", style = IOSTextStyles.Title3, color = AppTheme.colors.accent)
                            Text("乾宫 · 金", style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel,
                                modifier = Modifier.padding(top = Spacing.xs))
                        }
                    }
                }
            }
            item {
                IOSGroupedSection(header = "六爻盘(示意)") {
                    item { SampleYaoRow("青龙", "父母 戌土", "世", false) }
                    item { SampleYaoRow("白虎", "官鬼 午火", null, false) }
                    item { SampleYaoRow("螣蛇", "父母 辰土", "应", false, empty = true) }
                    item { SampleYaoRow("朱雀", "子孙 子水", null, true) }
                }
            }
            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton("保存为案例", onClick = {})
                }
            }
        }
    }
}

@Composable
private fun SampleYaoRow(liushen: String, body: String, marker: String?, moving: Boolean, empty: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.rowHorizontal, vertical = 11.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(liushen, style = IOSTextStyles.Caption, color = AppTheme.colors.secondaryLabel,
            modifier = Modifier.padding(end = Spacing.md))
        Text(
            body,
            style = IOSTextStyles.Body,
            color = if (moving) AppTheme.colors.moving else AppTheme.colors.label,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        if (empty) IOSBadge("空", container = AppTheme.colors.empty.copy(alpha = 0.14f), content = AppTheme.colors.empty)
        if (moving) IOSBadge("动", container = AppTheme.colors.moving.copy(alpha = 0.12f), content = AppTheme.colors.moving,
            modifier = Modifier.padding(start = Spacing.sm))
        marker?.let {
            val c = if (it == "世") AppTheme.colors.world else AppTheme.colors.response
            IOSBadge(it, container = c.copy(alpha = 0.12f), content = c, modifier = Modifier.padding(start = Spacing.sm))
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// @Preview 入口(浅色 / 深色 / 示例页)
// ════════════════════════════════════════════════════════════════════

@Preview(name = "组件画廊 · 浅色", showBackground = true, heightDp = 1400)
@Composable
private fun GalleryLightPreview() {
    AppTheme(darkTheme = false) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.systemBackground)) { ComponentGallery() }
    }
}

@Preview(name = "组件画廊 · 深色", showBackground = true, heightDp = 1400)
@Composable
private fun GalleryDarkPreview() {
    AppTheme(darkTheme = true) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.systemBackground)) { ComponentGallery() }
    }
}

@Preview(name = "示例页拼装 · 浅色", showBackground = true, heightDp = 1000)
@Composable
private fun SampleLightPreview() {
    AppTheme(darkTheme = false) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.systemBackground)) { SampleAssembledScreen() }
    }
}

@Preview(name = "示例页拼装 · 深色", showBackground = true, heightDp = 1000)
@Composable
private fun SampleDarkPreview() {
    AppTheme(darkTheme = true) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.systemBackground)) { SampleAssembledScreen() }
    }
}
