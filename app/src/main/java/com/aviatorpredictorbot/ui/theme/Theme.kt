package com.aviatorpredictorbot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AviatorColorScheme = darkColorScheme(
    primary = CrimsonLight,
    onPrimary = DeepDarkBackground,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = TextPrimary,
    secondary = PurpleMultiplier,
    onSecondary = TextPrimary,
    secondaryContainer = TrainBlueDark,
    onSecondaryContainer = TextPrimary,
    tertiary = PinkMultiplier,
    onTertiary = DeepDarkBackground,
    background = DeepDarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CrimsonBorder,
    outlineVariant = CardBorderRed
)

@Composable
fun AviatorPredictorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepDarkBackground.toArgb()
            window.navigationBarColor = DeepDarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = AviatorColorScheme,
        typography = Typography,
        content = content
    )
}
