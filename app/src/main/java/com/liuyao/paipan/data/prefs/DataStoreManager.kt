package com.liuyao.paipan.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 应用级单例 DataStore */
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * 用户偏好读写管理。
 *
 * 读:[preferences] 暴露 [Flow]<[UserPreferences]>,UI 直接 collect。
 * 写:各 update* 方法为 suspend,在 ViewModel 协程中调用。
 *
 * 示例:
 * ```
 * // 读
 * val prefs by manager.preferences.collectAsStateWithLifecycle(UserPreferences())
 * // 写
 * scope.launch { manager.setDarkMode(DarkMode.DARK) }
 * ```
 */
class DataStoreManager(private val context: Context) {

    private object Keys {
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val CAST_METHOD = stringPreferencesKey("cast_method")
        // 排盘显示
        val SHOW_HIDDEN = booleanPreferencesKey("show_hidden")
        val SHOW_FLYING = booleanPreferencesKey("show_flying")
        val SHOW_SHENSHA = booleanPreferencesKey("show_shensha")
        val SHOW_STRENGTH = booleanPreferencesKey("show_strength")
        val SHOW_VOID = booleanPreferencesKey("show_void")
        val SHOW_ELEMENT_COLOR = booleanPreferencesKey("show_element_color")
        val SHOW_DETAIL_BADGE = booleanPreferencesKey("show_detail_badge")
        // 断语显示
        val ONLY_MATCHED = booleanPreferencesKey("only_matched")
        val SHOW_FAILED = booleanPreferencesKey("show_failed_reason")
        val SHOW_CONFLICT = booleanPreferencesKey("show_conflict")
        val SORT_BY_WEIGHT = booleanPreferencesKey("sort_by_weight")
        val SOURCE_FILTER = stringPreferencesKey("source_filter")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { it.toUserPreferences() }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val def = UserPreferences()
        val cd = def.chartDisplay
        val rd = def.rulesDisplay
        return UserPreferences(
            darkMode = this[Keys.DARK_MODE]?.let { runCatching { DarkMode.valueOf(it) }.getOrNull() } ?: def.darkMode,
            defaultCastMethod = this[Keys.CAST_METHOD]
                ?.takeUnless { it == "NUMBER" || it == "DIGITAL" || it == "NUMERIC" }
                ?.let { runCatching { CastMethodPref.valueOf(it) }.getOrNull() }
                ?: def.defaultCastMethod,
            chartDisplay = ChartDisplayPrefs(
                showHidden = this[Keys.SHOW_HIDDEN] ?: cd.showHidden,
                showFlying = this[Keys.SHOW_FLYING] ?: cd.showFlying,
                showShenSha = this[Keys.SHOW_SHENSHA] ?: cd.showShenSha,
                showStrength = this[Keys.SHOW_STRENGTH] ?: cd.showStrength,
                showVoid = this[Keys.SHOW_VOID] ?: cd.showVoid,
                showElementColor = this[Keys.SHOW_ELEMENT_COLOR] ?: cd.showElementColor,
                showDetailBadge = this[Keys.SHOW_DETAIL_BADGE] ?: cd.showDetailBadge,
            ),
            rulesDisplay = RulesDisplayPrefs(
                onlyMatched = this[Keys.ONLY_MATCHED] ?: rd.onlyMatched,
                showFailedReason = this[Keys.SHOW_FAILED] ?: rd.showFailedReason,
                showConflict = this[Keys.SHOW_CONFLICT] ?: rd.showConflict,
                sortByWeight = this[Keys.SORT_BY_WEIGHT] ?: rd.sortByWeight,
                sourceFilter = this[Keys.SOURCE_FILTER] ?: rd.sourceFilter,
            ),
        )
    }

    suspend fun setDarkMode(mode: DarkMode) = context.dataStore.edit { it[Keys.DARK_MODE] = mode.name }
    suspend fun setCastMethod(m: CastMethodPref) = context.dataStore.edit { it[Keys.CAST_METHOD] = m.name }

    suspend fun setChartDisplay(p: ChartDisplayPrefs) = context.dataStore.edit {
        it[Keys.SHOW_HIDDEN] = p.showHidden
        it[Keys.SHOW_FLYING] = p.showFlying
        it[Keys.SHOW_SHENSHA] = p.showShenSha
        it[Keys.SHOW_STRENGTH] = p.showStrength
        it[Keys.SHOW_VOID] = p.showVoid
        it[Keys.SHOW_ELEMENT_COLOR] = p.showElementColor
        it[Keys.SHOW_DETAIL_BADGE] = p.showDetailBadge
    }

    suspend fun setRulesDisplay(p: RulesDisplayPrefs) = context.dataStore.edit {
        it[Keys.ONLY_MATCHED] = p.onlyMatched
        it[Keys.SHOW_FAILED] = p.showFailedReason
        it[Keys.SHOW_CONFLICT] = p.showConflict
        it[Keys.SORT_BY_WEIGHT] = p.sortByWeight
        it[Keys.SOURCE_FILTER] = p.sourceFilter
    }

    suspend fun clearAll() = context.dataStore.edit { it.clear() }
}
