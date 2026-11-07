package com.aviatorpredictorbot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AviatorColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE9ED),
    onPrimaryContainer = TextPrimary,
    secondary = PurpleMultiplier,
    onSecondary = Color.White,
    secondaryContainer = TrainBlueDark,
    onSecondaryContainer = TextPrimary,
    tertiary = PinkMultiplier,
    onTertiary = Color.White,
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
    /** Dark system bars (used while the splash screen is showing). */
    darkSystemBars: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = if (darkSystemBars) ChipBlack else LightBackground
            window.statusBarColor = barColor.toArgb()
            window.navigationBarColor = barColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkSystemBars
            controller.isAppearanceLightNavigationBars = !darkSystemBars
        }
    }

    MaterialTheme(
        colorScheme = AviatorColorScheme,
        typography = Typography,
        content = content
    )
}
