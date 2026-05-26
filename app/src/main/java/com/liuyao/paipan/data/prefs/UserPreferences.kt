package com.liuyao.paipan.data.prefs

data class UserPreferences(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val defaultCastMethod: CastMethodPref = CastMethodPref.SOLAR_TIME,
    val chartDisplay: ChartDisplayPrefs = ChartDisplayPrefs(),
    val rulesDisplay: RulesDisplayPrefs = RulesDisplayPrefs(),
)

enum class DarkMode(val cn: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}

enum class CastMethodPref(val cn: String) {
    SOLAR_TIME("正时起卦"),
    MANUAL_TIME("选择时间起卦"),
    MANUAL_COIN("手动起卦"),
}

data class ChartDisplayPrefs(
    val showHidden: Boolean = true,
    val showFlying: Boolean = true,
    val showShenSha: Boolean = true,
    val showStrength: Boolean = true,
    val showVoid: Boolean = true,
    val showElementColor: Boolean = true,
    val showDetailBadge: Boolean = true,
)

data class RulesDisplayPrefs(
    val onlyMatched: Boolean = true,
    val showFailedReason: Boolean = false,
    val showConflict: Boolean = true,
    val sortByWeight: Boolean = true,
    val sourceFilter: String = "",
)
