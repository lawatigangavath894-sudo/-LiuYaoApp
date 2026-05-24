package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSToggleRow
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.settings.SettingsViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Spacing
import androidx.compose.material3.Text

/**
 * 设置页(iOS Settings grouped list)。
 * 深色模式 / 起卦方式用 bottom sheet 选择;显示设置用开关行;数据管理用导航行。
 * 偏好经 DataStore 持久化([SettingsViewModel])。
 */
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onImport: () -> Unit = {},
    onBackup: () -> Unit = {},
) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<SettingsSheet?>(null) }

    com.liuyao.paipan.ui.components.IOSLargeTitleScaffold(title = "设置") { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            // 1. 外观
            item {
                IOSGroupedSection(header = "外观") {
                    item {
                        IOSListRow("深色模式", value = prefs.darkMode.cn, showChevron = true,
                            onClick = { sheet = SettingsSheet.DARK_MODE })
                    }
                }
            }

            // 2. 起卦
            item {
                IOSGroupedSection(header = "起卦") {
                    item {
                        IOSListRow("默认起卦方式", value = prefs.defaultCastMethod.cn, showChevron = true,
                            onClick = { sheet = SettingsSheet.CAST_METHOD })
                    }
                }
            }

            // 3. 排盘显示
            item {
                val c = prefs.chartDisplay
                IOSGroupedSection(header = "排盘显示") {
                    item { IOSToggleRow("显示伏神", c.showHidden) { vm.setChartDisplay(c.copy(showHidden = it)) } }
                    item { IOSToggleRow("显示飞神", c.showFlying) { vm.setChartDisplay(c.copy(showFlying = it)) } }
                    item { IOSToggleRow("显示神煞", c.showShenSha) { vm.setChartDisplay(c.copy(showShenSha = it)) } }
                    item { IOSToggleRow("显示旺衰", c.showStrength) { vm.setChartDisplay(c.copy(showStrength = it)) } }
                    item { IOSToggleRow("显示空亡", c.showVoid) { vm.setChartDisplay(c.copy(showVoid = it)) } }
                    item { IOSToggleRow("五行颜色", c.showElementColor) { vm.setChartDisplay(c.copy(showElementColor = it)) } }
                    item { IOSToggleRow("详细 Badge", c.showDetailBadge) { vm.setChartDisplay(c.copy(showDetailBadge = it)) } }
                }
            }

            // 4. 断语显示
            item {
                val r = prefs.rulesDisplay
                IOSGroupedSection(header = "断语显示") {
                    item { IOSToggleRow("只显示命中", r.onlyMatched) { vm.setRulesDisplay(r.copy(onlyMatched = it)) } }
                    item { IOSToggleRow("显示未命中原因", r.showFailedReason) { vm.setRulesDisplay(r.copy(showFailedReason = it)) } }
                    item { IOSToggleRow("显示冲突断语", r.showConflict) { vm.setRulesDisplay(r.copy(showConflict = it)) } }
                    item { IOSToggleRow("按权重排序", r.sortByWeight) { vm.setRulesDisplay(r.copy(sortByWeight = it)) } }
                    item {
                        IOSListRow("按来源筛选", value = r.sourceFilter.ifBlank { "全部" }, showChevron = true,
                            onClick = { sheet = SettingsSheet.SOURCE_FILTER })
                    }
                }
            }

            // 5. 数据管理
            item {
                IOSGroupedSection(header = "数据管理", footer = "导出与备份将生成文件;清空缓存仅重置偏好设置,不影响案例与断语。") {
                    item { IOSListRow("导入断语资料", value = "txt / md", showChevron = true, onClick = onImport) }
                    item { IOSListRow("导出 / 备份", value = "JSON · Markdown", showChevron = true, onClick = onBackup) }
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

    // ───── bottom sheets ─────
    when (sheet) {
        SettingsSheet.DARK_MODE -> IOSBottomSheet(onDismiss = { sheet = null }, title = "深色模式") {
            Column {
                DarkMode.entries.forEach { m ->
                    OptionRow(m.cn, selected = m == prefs.darkMode) { vm.setDarkMode(m); sheet = null }
                }
            }
        }
        SettingsSheet.CAST_METHOD -> IOSBottomSheet(onDismiss = { sheet = null }, title = "默认起卦方式") {
            Column {
                CastMethodPref.entries.forEach { m ->
                    OptionRow(m.cn, selected = m == prefs.defaultCastMethod) { vm.setCastMethod(m); sheet = null }
                }
            }
        }
        SettingsSheet.SOURCE_FILTER -> IOSBottomSheet(onDismiss = { sheet = null }, title = "按来源筛选") {
            Column {
                val options = listOf("" to "全部", "刘昌明" to "刘昌明", "手动录入" to "手动录入", "导入" to "导入")
                options.forEach { (value, label) ->
                    OptionRow(label, selected = value == prefs.rulesDisplay.sourceFilter) {
                        vm.setRulesDisplay(prefs.rulesDisplay.copy(sourceFilter = value)); sheet = null
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
                com.liuyao.paipan.ui.components.IOSPrimaryButton(
                    text = "确认清空",
                    onClick = { vm.clearPrefsCache(); sheet = null },
                )
            }
        }
        null -> Unit
    }
}

private enum class SettingsSheet { DARK_MODE, CAST_METHOD, SOURCE_FILTER, CLEAR_CACHE }

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().clickableNoRipple(onClick).padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        if (selected) {
            Text("✓", style = IOSTextStyles.Body, color = AppTheme.colors.accent)
        }
    }
}
