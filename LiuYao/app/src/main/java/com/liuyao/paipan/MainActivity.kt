package com.liuyao.paipan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.liuyao.paipan.data.prefs.DarkMode
import com.liuyao.paipan.data.prefs.DataStoreManager
import com.liuyao.paipan.data.prefs.UserPreferences
import com.liuyao.paipan.nav.AppNavigation
import com.liuyao.paipan.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val store = remember { DataStoreManager(applicationContext) }
            val prefs by store.preferences.collectAsState(initial = UserPreferences())
            val dark = when (prefs.darkMode) {
                DarkMode.SYSTEM -> isSystemInDarkTheme()
                DarkMode.LIGHT -> false
                DarkMode.DARK -> true
            }
            AppTheme(darkTheme = dark) {
                AppNavigation()
            }
        }
    }
}
