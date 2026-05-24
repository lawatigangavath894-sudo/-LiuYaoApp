package com.liuyao.paipan.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.prefs.CastMethodPref
import com.liuyao.paipan.data.prefs.ChartDisplayPrefs
import com.liuyao.paipan.data.prefs.DarkMode
import com.liuyao.paipan.data.prefs.DataStoreManager
import com.liuyao.paipan.data.prefs.RulesDisplayPrefs
import com.liuyao.paipan.data.prefs.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val store = DataStoreManager(app)

    val prefs: StateFlow<UserPreferences> = store.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setDarkMode(mode: DarkMode) = viewModelScope.launch { store.setDarkMode(mode) }
    fun setCastMethod(m: CastMethodPref) = viewModelScope.launch { store.setCastMethod(m) }
    fun setChartDisplay(p: ChartDisplayPrefs) = viewModelScope.launch { store.setChartDisplay(p) }
    fun setRulesDisplay(p: RulesDisplayPrefs) = viewModelScope.launch { store.setRulesDisplay(p) }

    /** 数据管理:清空偏好缓存(不动 Room 业务数据) */
    fun clearPrefsCache(onDone: () -> Unit = {}) = viewModelScope.launch {
        store.clearAll()
        onDone()
    }
}
