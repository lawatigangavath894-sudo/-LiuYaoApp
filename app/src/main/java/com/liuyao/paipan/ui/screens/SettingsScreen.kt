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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuyao.paipan.data.prefs.CastMethodPref
import com.liuyao.paipan.data.prefs.DarkMode
import com.liuyao.paipan.ui.components.IOSBottomSheet
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSLargeTitleScaffold
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSToggleRow
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.settings.SettingsViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onImport: () -> Unit = {},
    onBackup: () -> Unit = {},
    onAiSettings: () -> Unit = {},
) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<SettingsSheet?>(null) }

    IOSLargeTitleScaffold(title = "设置") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            item {
                IOSGroupedSection(header = "外观") {
                    item {
                        IOSListRow(
                            title = "深色模式",
                            value = prefs.darkMode.cn,
                            showChevron = true,
                            onClick = { sheet = SettingsSheet.DARK_MODE },
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "起卦") {
                    item {
                        IOSListRow(
                            title = "默认起卦方式",
                            value = prefs.defaultCastMethod.cn,
                            showChevron = true,
                            onClick = { sheet = SettingsSheet.CAST_METHOD },
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "AI 大模型") {
                    item { IOSListRow("AI 服务配置", value = "未配置", showChevron = true, onClick = onAiSettings) }
                    item { IOSListRow("默认模型", value = "未配置", showChevron = true, onClick = onAiSettings) }
                    item { IOSListRow("连接测试", value = "去配置", showChevron = true, onClick = onAiSettings) }
                }
            }

            item {
                val c = prefs.chartDisplay
                IOSGroupedSection(header = "排盘显示") {
                    item { IOSToggleRow("显示伏神", c.showHidden, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showHidden = checked)) }) }
                    item { IOSToggleRow("显示飞神", c.showFlying, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showFlying = checked)) }) }
                    item { IOSToggleRow("显示神煞", c.showShenSha, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showShenSha = checked)) }) }
                    item { IOSToggleRow("显示旺衰", c.showStrength, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showStrength = checked)) }) }
                    item { IOSToggleRow("显示空亡", c.showVoid, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showVoid = checked)) }) }
                    item { IOSToggleRow("五行颜色", c.showElementColor, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showElementColor = checked)) }) }
                    item { IOSToggleRow("详细 Badge", c.showDetailBadge, onCheckedChange = { checked -> vm.setChartDisplay(c.copy(showDetailBadge = checked)) }) }
                }
            }

            item {
                val r = prefs.rulesDisplay
                IOSGroupedSection(header = "断语显示") {
                    item { IOSToggleRow("只显示命中", r.onlyMatched, onCheckedChange = { checked -> vm.setRulesDisplay(r.copy(onlyMatched = checked)) }) }
                    item { IOSToggleRow("显示未命中原因", r.showFailedReason, onCheckedChange = { checked -> vm.setRulesDisplay(r.copy(showFailedReason = checked)) }) }
                    item { IOSToggleRow("显示冲突断语", r.showConflict, onCheckedChange = { checked -> vm.setRulesDisplay(r.copy(showConflict = checked)) }) }
                    item { IOSToggleRow("按权重排序", r.sortByWeight, onCheckedChange = { checked -> vm.setRulesDisplay(r.copy(sortByWeight = checked)) }) }
                    item {
                        IOSListRow(
                            title = "按来源筛选",
                            value = r.sourceFilter.ifBlank { "全部" },
                            showChevron = true,
                            onClick = { sheet = SettingsSheet.SOURCE_FILTER },
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(
                    header = "数据管理",
                    footer = "导出与备份将生成文件；清空缓存只重置偏好设置，不影响案例与断语。",
                ) {
                    item { IOSListRow("导入断语资料", value = "txt / md", showChevron = true, onClick = onImport) }
                    item { IOSListRow("导出 / 备份", value = "JSON / Markdown", showChevron = true, onClick = onBackup) }
                    item { IOSListRow("从备份恢复", showChevron = true, onClick = onBackup) }
                    item { IOSListRow("清空缓存", showChevron = true, onClick = { sheet = SettingsSheet.CLEAR_CACHE }) }
                }
            }

            item {
                Text(
                    "六爻排盘 · 0.1.0",
                    style = IOSTextStyles.Footnote,
                    color = AppTheme.colors.tertiaryLabel,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }
        }
    }

    when (sheet) {
        SettingsSheet.DARK_MODE -> IOSBottomSheet(onDismiss = { sheet = null }, title = "深色模式") {
            Column {
                DarkMode.entries.forEach { mode ->
                    OptionRow(mode.cn, selected = mode == prefs.darkMode) {
                        vm.setDarkMode(mode)
                        sheet = null
                    }
                }
            }
        }

        SettingsSheet.CAST_METHOD -> IOSBottomSheet(onDismiss = { sheet = null }, title = "默认起卦方式") {
            Column {
                CastMethodPref.entries.forEach { method ->
                    OptionRow(method.cn, selected = method == prefs.defaultCastMethod) {
                        vm.setCastMethod(method)
                        sheet = null
                    }
                }
            }
        }

        SettingsSheet.SOURCE_FILTER -> IOSBottomSheet(onDismiss = { sheet = null }, title = "按来源筛选") {
            Column {
                val options = listOf(
                    "" to "全部",
                    "刘昌明" to "刘昌明",
                    "手动录入" to "手动录入",
                    "导入" to "导入",
                )
                options.forEach { (value, label) ->
                    OptionRow(label, selected = value == prefs.rulesDisplay.sourceFilter) {
                        vm.setRulesDisplay(prefs.rulesDisplay.copy(sourceFilter = value))
                        sheet = null
                    }
                }
            }
        }

        SettingsSheet.CLEAR_CACHE -> IOSBottomSheet(onDismiss = { sheet = null }, title = "清空缓存") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "将把所有偏好设置恢复默认。案例与断语数据不受影响。",
                    style = IOSTextStyles.Subhead,
                    color = AppTheme.colors.secondaryLabel,
                )
                IOSPrimaryButton(
                    text = "确认清空",
                    onClick = {
                        vm.clearPrefsCache()
                        sheet = null
                    },
                )
            }
        }

        null -> Unit
    }
}

private enum class SettingsSheet { DARK_MODE, CAST_METHOD, SOURCE_FILTER, CLEAR_CACHE }

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        if (selected) {
            Text("✓", style = IOSTextStyles.Body, color = AppTheme.colors.accent)
        }
    }
}
