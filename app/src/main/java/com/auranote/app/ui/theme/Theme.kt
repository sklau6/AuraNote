package com.auranote.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** User-selectable theme modes stored in [AppPreferences]. */
enum class AppTheme { DARK, LIGHT, SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = TextPrimary,
    primaryContainer = NavyCard,
    onPrimaryContainer = PurpleLight,
    secondary = IndigoPrimary,
    onSecondary = TextPrimary,
    secondaryContainer = NavyElevated,
    onSecondaryContainer = IndigoLight,
    tertiary = PinkAccent,
    onTertiary = TextPrimary,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = NavyElevated,
    error = RedAccent,
    onError = TextPrimary,
    errorContainer = Color(0xFF3B1219),
    onErrorContainer = Color(0xFFFFB4AB),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = PurplePrimary,
    surfaceTint = PurplePrimary,
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = IndigoPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = PinkAccent,
    onTertiary = Color.White,
    background = Color(0xFFF8F7FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEEECF4),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = RedAccent,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFCFBCFF),
    surfaceTint = PurplePrimary,
    scrim = Color(0xFF000000)
)

@Composable
fun AuraNoteTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemDark
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgColor = if (useDark) DeepNavy.toArgb() else Color(0xFFF8F7FF).toArgb()
            window.statusBarColor = bgColor
            window.navigationBarColor = bgColor
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDark
                isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
