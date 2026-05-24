package com.liuyao.paipan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** 语义颜色的 CompositionLocal;通过 [AppTheme.colors] 读取 */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

private fun lightScheme(c: AppColors) = lightColorScheme(
    primary = c.accent,
    onPrimary = Color.White,
    background = c.systemBackground,
    onBackground = c.label,
    surface = c.card,
    onSurface = c.label,
    surfaceVariant = c.systemBackground,
    onSurfaceVariant = c.secondaryLabel,
    outlineVariant = c.separator,
)

private fun darkScheme(c: AppColors) = darkColorScheme(
    primary = c.accent,
    onPrimary = Color.White,
    background = c.systemBackground,
    onBackground = c.label,
    surface = c.card,
    onSurface = c.label,
    surfaceVariant = c.systemBackground,
    onSurfaceVariant = c.secondaryLabel,
    outlineVariant = c.separator,
)

/**
 * App 主题。同时下发:
 *  - MaterialTheme(colorScheme + AppM3Typography)
 *  - LocalAppColors(语义颜色 token)
 *
 * 业务/组件层取色优先用 `AppTheme.colors.xxx`,文字样式用 `IOSTextStyles.xxx`
 * 或 `MaterialTheme.typography.xxx`。
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = if (darkTheme) darkScheme(appColors) else lightScheme(appColors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppM3Typography,
            content = content,
        )
    }
}

/** 便捷访问器:AppTheme.colors / AppTheme.isDark */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalAppColors.current.isDark
}
