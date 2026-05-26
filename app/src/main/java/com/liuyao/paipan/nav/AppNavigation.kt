package com.liuyao.paipan.nav

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liuyao.paipan.ui.screens.AiChatScreen
import com.liuyao.paipan.ui.screens.AiSettingsScreen
import com.liuyao.paipan.ui.screens.CaseDetailScreen
import com.liuyao.paipan.ui.screens.CasesScreen
import com.liuyao.paipan.ui.screens.CastScreen
import com.liuyao.paipan.ui.screens.ChartScreen
import com.liuyao.paipan.ui.screens.HomeScreen
import com.liuyao.paipan.ui.screens.RuleDetailScreen
import com.liuyao.paipan.ui.screens.RuleEditScreen
import com.liuyao.paipan.ui.screens.RulesScreen
import com.liuyao.paipan.ui.screens.SettingsScreen
import com.liuyao.paipan.ui.screens.backup.BackupScreen
import com.liuyao.paipan.ui.screens.backup.BackupViewModel
import com.liuyao.paipan.ui.screens.backup.RestoreScreen
import com.liuyao.paipan.ui.screens.cases.CaseViewModel
import com.liuyao.paipan.ui.screens.imports.ImportPreviewScreen
import com.liuyao.paipan.ui.screens.imports.RuleImportScreen
import com.liuyao.paipan.ui.screens.imports.RuleImportViewModel
import com.liuyao.paipan.ui.theme.AppTheme

private data class Tab(val route: String, val label: String)

private val tabs = listOf(
    Tab(Route.Home.route, "首页"),
    Tab(Route.AiChat.route, "AI 对话"),
    Tab(Route.Rules.route, "断语"),
    Tab(Route.Cases.route, "案例"),
    Tab(Route.Settings.route, "设置"),
)

@Composable
fun AppNavigation(nav: NavHostController = rememberNavController()) {
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val activeTab = currentRoute.asBottomTabRoute()
    val showBar = activeTab in Route.tabRoutes

    if (showBar && activeTab != Route.Home.route) {
        BackHandler {
            nav.navigateToBottomTab(Route.Home.route)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                IosTabBar(activeTab) { route -> nav.navigateToBottomTab(route) }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Route.Home.route) {
                HomeScreen(
                    onCast = { nav.navigateSingleTop(Route.Cast.route) },
                    onOpenAi = { nav.navigateToBottomTab(Route.AiChat.route) },
                    onOpenRules = { nav.navigateToBottomTab(Route.Rules.route) },
                    onOpenCases = { nav.navigateToBottomTab(Route.Cases.route) },
                    onOpenSettings = { nav.navigateToBottomTab(Route.Settings.route) },
                    onOpenChart = { nav.navigateSingleTop(Route.Chart.route) },
                )
            }
            composable(
                Route.AiChat.pattern,
                arguments = listOf(navArgument("chartId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }),
            ) { entry ->
                val chartVm: com.liuyao.paipan.ui.screens.chart.ChartViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Home.route),
                    )
                val chartState by chartVm.ui.collectAsStateWithLifecycle()
                AiChatScreen(
                    chartId = entry.arguments?.getString("chartId")?.takeIf { it.isNotBlank() },
                    currentChart = chartState.chart,
                    onBack = { nav.safeBack(Route.Home.route) },
                    onOpenSettings = { nav.navigateSingleTop(Route.AiSettings.route) },
                )
            }
            composable(Route.AiSettings.route) {
                AiSettingsScreen(onBack = { nav.safeBack(Route.AiChat.route) })
            }
            composable(Route.Rules.route) {
                val rulesVm: com.liuyao.paipan.ui.screens.rules.RulesViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = nav.getBackStackEntry(Route.Rules.route),
                    )
                RulesScreen(
                    vm = rulesVm,
                    onOpenDetail = { id -> nav.navigateSingleTop(Route.RuleDetail.create(id)) },
                    onAdd = { nav.navigateSingleTop(Route.RuleEdit.create(null)) },
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
                    onBack = { nav.safeBack(Route.Rules.route) },
                    onEdit = { rid -> nav.navigateSingleTop(Route.RuleEdit.create(rid)) },
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
                    onBack = { nav.safeBack(Route.Rules.route) },
                    onSaved = { nav.safeBack(Route.Rules.route) },
                )
            }
            composable(Route.Cases.route) {
                val caseVm: CaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Cases.route),
                )
                CasesScreen(
                    vm = caseVm,
                    onOpenDetail = { id -> nav.navigateSingleTop(Route.CaseDetail.create(id)) },
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
                CaseDetailScreen(vm = caseVm, caseId = id, onBack = { nav.safeBack(Route.Cases.route) })
            }
            composable(Route.Settings.route) {
                val settingsVm: com.liuyao.paipan.ui.screens.settings.SettingsViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                SettingsScreen(
                    vm = settingsVm,
                    onImport = { nav.navigateSingleTop(Route.RuleImport.route) },
                    onBackup = { nav.navigateSingleTop(Route.Backup.route) },
                    onAiSettings = { nav.navigateSingleTop(Route.AiSettings.route) },
                )
            }
            composable(Route.Backup.route) {
                val backupVm: BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Backup.route),
                )
                BackupScreen(
                    vm = backupVm,
                    onBack = { nav.safeBack(Route.Settings.route) },
                    onOpenRestore = { nav.navigateSingleTop(Route.Restore.route) },
                )
            }
            composable(Route.Restore.route) {
                val backupVm: BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Backup.route),
                )
                RestoreScreen(vm = backupVm, onBack = { nav.safeBack(Route.Backup.route) })
            }
            composable(Route.RuleImport.route) {
                val importVm: RuleImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Settings.route),
                )
                RuleImportScreen(
                    vm = importVm,
                    onBack = { nav.safeBack(Route.Settings.route) },
                    onParsed = { nav.navigateSingleTop(Route.ImportPreview.route) },
                )
            }
            composable(Route.ImportPreview.route) {
                val importVm: RuleImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = nav.getBackStackEntry(Route.Settings.route),
                )
                ImportPreviewScreen(
                    vm = importVm,
                    onBack = { nav.safeBack(Route.RuleImport.route) },
                    onImported = {
                        if (!nav.popBackStack(Route.Settings.route, inclusive = false)) {
                            nav.navigateToBottomTab(Route.Settings.route)
                        }
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
                    onBack = { nav.safeBack(Route.Home.route) },
                    onCasted = {
                        nav.navigate(Route.Chart.route) {
                            launchSingleTop = true
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
                ChartScreen(
                    vm = chartVm,
                    onBack = { nav.safeBack(Route.Home.route) },
                    onAiAnalyze = { chartId -> nav.navigateSingleTop(Route.AiChat.create(chartId)) },
                    onOpenAiSettings = { nav.navigateSingleTop(Route.AiSettings.route) },
                )
            }
        }
    }
}

private fun NavHostController.navigateToBottomTab(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute.asBottomTabRoute() == route) return
    runCatching {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
        }
    }.onFailure { error ->
        Log.e("AppNavigation", "Bottom tab navigation failed: from=$currentRoute to=$route", error)
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute == route) return
    runCatching {
        navigate(route) {
            launchSingleTop = true
        }
    }.onFailure { error ->
        Log.e("AppNavigation", "Navigation failed: from=$currentRoute to=$route", error)
    }
}

private fun NavHostController.safeBack(fallback: String = Route.Settings.route) {
    if (!popBackStack()) {
        navigateSingleTop(fallback)
    }
}

private fun String?.asBottomTabRoute(): String? = when (this) {
    Route.Home.route -> Route.Home.route
    Route.AiChat.route, Route.AiChat.pattern -> Route.AiChat.route
    Route.Rules.route -> Route.Rules.route
    Route.Cases.route -> Route.Cases.route
    Route.Settings.route -> Route.Settings.route
    else -> null
}

@Composable
private fun IosTabBar(activeTab: String?, onSelect: (String) -> Unit) {
    var lastClickAt by remember { mutableLongStateOf(0L) }
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
                val selected = activeTab == tab.route
                val tint = if (selected) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            val now = System.currentTimeMillis()
                            if (!selected && now - lastClickAt > 300L) {
                                lastClickAt = now
                                onSelect(tab.route)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = tint,
                    )
                }
            }
        }
    }
}
