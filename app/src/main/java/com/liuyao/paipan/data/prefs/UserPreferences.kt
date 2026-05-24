package com.liuyao.paipan.data.prefs

/**
 * 用户偏好(不可变快照)。由 [DataStoreManager] 读写。
 * 纯数据,无 Android 依赖,便于测试与在 Compose 中作为状态。
 */
data class UserPreferences(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val defaultCastMethod: CastMethodPref = CastMethodPref.SOLAR_TIME,
    val chartDisplay: ChartDisplayPrefs = ChartDisplayPrefs(),
    val rulesDisplay: RulesDisplayPrefs = RulesDisplayPrefs(),
)

/** 深色模式 */
enum class DarkMode(val cn: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}

/** 起卦默认方式 */
enum class CastMethodPref(val cn: String) {
    SOLAR_TIME("当前时间"),
    MANUAL_TIME("手动时间"),
    MANUAL_COIN("手动摇卦"),
    NUMBER("数字起卦"),
}

/** 排盘显示设置(7 项开关) */
data class ChartDisplayPrefs(
    val showHidden: Boolean = true,    // 伏神
    val showFlying: Boolean = true,    // 飞神
    val showShenSha: Boolean = true,   // 神煞
    val showStrength: Boolean = true,  // 旺衰
    val showVoid: Boolean = true,      // 空亡
    val showElementColor: Boolean = true, // 五行颜色
    val showDetailBadge: Boolean = true,  // 详细 Badge
)

/** 断语显示设置(5 项) */
data class RulesDisplayPrefs(
    val onlyMatched: Boolean = true,       // 只显示命中
    val showFailedReason: Boolean = false, // 显示未命中原因
    val showConflict: Boolean = true,      // 显示冲突断语
    val sortByWeight: Boolean = true,      // 按权重排序
    val sourceFilter: String = "",         // 按来源筛选(空=全部)
)
