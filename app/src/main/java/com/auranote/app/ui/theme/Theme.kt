package com.auranote.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun AuraNoteTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNavy.toArgb()
            window.navigationBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
