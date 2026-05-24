package com.liuyao.paipan.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liuyao.paipan.ui.screens.CasesScreen
import com.liuyao.paipan.ui.screens.CaseDetailScreen
import com.liuyao.paipan.ui.screens.cases.CaseViewModel
import com.liuyao.paipan.ui.screens.backup.BackupScreen
import com.liuyao.paipan.ui.screens.backup.RestoreScreen
import com.liuyao.paipan.ui.screens.backup.BackupViewModel
import com.liuyao.paipan.ui.screens.CastScreen
import com.liuyao.paipan.ui.screens.ChartScreen
import com.liuyao.paipan.ui.screens.HomeScreen
import com.liuyao.paipan.ui.screens.RulesScreen
import com.liuyao.paipan.ui.screens.RuleDetailScreen
import com.liuyao.paipan.ui.screens.RuleEditScreen
import com.liuyao.paipan.ui.screens.SettingsScreen
import com.liuyao.paipan.ui.screens.imports.RuleImportScreen
import com.liuyao.paipan.ui.screens.imports.ImportPreviewScreen
import com.liuyao.paipan.ui.screens.imports.RuleImportViewModel
import com.liuyao.paipan.ui.theme.AppTheme

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Route.Home.route, "首页", Icons.Filled.Home),
    Tab(Route.Rules.route, "断语", Icons.Filled.MenuBook),
    Tab(Route.Cases.route, "案例", Icons.Filled.Inbox),
    Tab(Route.Settings.route, "设置", Icons.Filled.Settings),
)

@Composable
fun AppNavigation(nav: NavHostController = rememberNavController()) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = current in Route.tabRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (showBar) IosTabBar(current) { route -> nav.switchTab(route) } },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Route.Home.route) {
                HomeScreen(
                    onCast = { nav.navigate(Route.Cast.route) },
                    onOpenChart = { nav.navigate(Route.Chart.route) },
                )
            }
            composable(Route.Rules.route) {
                val rulesVm: com.liuyao.paipan.ui.screens.rules.RulesViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Rules.route),
                    )
                RulesScreen(
                    vm = rulesVm,
                    onOpenDetail = { id -> nav.navigate(Route.RuleDetail.create(id)) },
                    onAdd = { nav.navigate(Route.RuleEdit.create(null)) },
                )
            }
            composable(Route.RuleDetail.route) { entry ->
                val rulesVm: com.liuyao.paipan.ui.screens.rules.RulesViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Rules.route),
                    )
                val id = entry.arguments?.getString("ruleId").orEmpty()
                RuleDetailScreen(
                    vm = rulesVm,
                    ruleId = id,
                    onBack = { nav.popBackStack() },
                    onEdit = { rid -> nav.navigate(Route.RuleEdit.create(rid)) },
                )
            }
            composable(
                Route.RuleEdit.route,
                arguments = listOf(navArgument("ruleId") { type = NavType.StringType; defaultValue = "" }),
            ) { entry ->
                val rulesVm: com.liuyao.paipan.ui.screens.rules.RulesViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Rules.route),
                    )
                val id = entry.arguments?.getString("ruleId")?.takeIf { it.isNotEmpty() }
                RuleEditScreen(
                    vm = rulesVm,
                    ruleId = id,
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.popBackStack() },
                )
            }
            composable(Route.Cases.route) {
                val caseVm: CaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Cases.route),
                )
                CasesScreen(
                    vm = caseVm,
                    onOpenDetail = { id -> nav.navigate(Route.CaseDetail.create(id)) },
                )
            }
            composable(
                Route.CaseDetail.route,
                arguments = listOf(navArgument("caseId") { type = NavType.StringType }),
            ) { entry ->
                val caseVm: CaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Cases.route),
                )
                val id = entry.arguments?.getString("caseId").orEmpty()
                CaseDetailScreen(vm = caseVm, caseId = id, onBack = { nav.popBackStack() })
            }
            composable(Route.Settings.route) {
                val settingsVm: com.liuyao.paipan.ui.screens.settings.SettingsViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                SettingsScreen(
                    vm = settingsVm,
                    onImport = { nav.navigate(Route.RuleImport.route) },
                    onBackup = { nav.navigate(Route.Backup.route) },
                )
            }
            composable(Route.Backup.route) {
                val backupVm: BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Backup.route),
                )
                BackupScreen(
                    vm = backupVm,
                    onBack = { nav.popBackStack() },
                    onOpenRestore = { nav.navigate(Route.Restore.route) },
                )
            }
            composable(Route.Restore.route) {
                val backupVm: BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Backup.route),
                )
                RestoreScreen(vm = backupVm, onBack = { nav.popBackStack() })
            }
            composable(Route.RuleImport.route) {
                val importVm: RuleImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Settings.route),
                )
                RuleImportScreen(
                    vm = importVm,
                    onBack = { nav.popBackStack() },
                    onParsed = { nav.navigate(Route.ImportPreview.route) },
                )
            }
            composable(Route.ImportPreview.route) {
                val importVm: RuleImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Settings.route),
                )
                ImportPreviewScreen(
                    vm = importVm,
                    onBack = { nav.popBackStack() },
                    onImported = {
                        // 导入完成,回到设置页
                        nav.popBackStack(Route.Settings.route, inclusive = false)
                    },
                )
            }

            composable(Route.Cast.route) {
                val chartVm: com.liuyao.paipan.ui.screens.chart.ChartViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Home.route),
                    )
                CastScreen(
                    vm = chartVm,
                    onBack = { nav.popBackStack() },
                    onCasted = {
                        nav.navigate(Route.Chart.route) {
                            popUpTo(Route.Cast.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Chart.route) {
                val chartVm: com.liuyao.paipan.ui.screens.chart.ChartViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Home.route),
                    )
                ChartScreen(vm = chartVm, onBack = { nav.popBackStack() })
            }
        }
    }
}

/** 一级 Tab 间切换的标准模式:保存/恢复状态,避免堆栈累积 */
private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun IosTabBar(current: String?, onSelect: (String) -> Unit) {
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .navigationBarsPadding()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = current == tab.route
                val tint = if (selected) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(tab.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(tab.icon, contentDescription = tab.label, tint = tint)
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = tint,
                    )
                }
            }
        }
    }
}
