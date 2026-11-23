package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val BeityLightColorScheme = lightColorScheme(
    primary = BeityNavy,
    onPrimary = BeityWhite,
    primaryContainer = BeityNavyLight,
    onPrimaryContainer = BeityWhite,
    secondary = BeityGold,
    onSecondary = BeityNavyDark,
    secondaryContainer = BeityGoldContainer,
    onSecondaryContainer = BeityNavy,
    tertiary = BeityGoldDark,
    onTertiary = BeityWhite,
    background = BeityOffWhite,
    onBackground = BeityTextDark,
    surface = BeityWhite,
    onSurface = BeityTextDark,
    surfaceVariant = BeitySurfaceVariantColor(),
    onSurfaceVariant = BeityTextMuted,
    outline = BeityOutlineLight,
    outlineVariant = BeityOutlineLight,
    error = BeityError,
    onError = BeityWhite,
    errorContainer = BeityErrorContainer,
    onErrorContainer = BeityError
)

private fun BeitySurfaceVariantColor() = androidx.compose.ui.graphics.Color(0xFFEEF1F6)

private val BeityDarkColorScheme = darkColorScheme(
    primary = BeityGold,
    onPrimary = BeityNavyDark,
    primaryContainer = BeityNavyLight,
    onPrimaryContainer = BeityGoldLight,
    secondary = BeityGoldLight,
    onSecondary = BeityNavyDark,
    secondaryContainer = BeityNavySurface,
    onSecondaryContainer = BeityGold,
    tertiary = BeityGold,
    onTertiary = BeityNavyDark,
    background = BeityNavyDark,
    onBackground = BeityTextLight,
    surface = BeityNavy,
    onSurface = BeityTextLight,
    surfaceVariant = BeityNavySurface,
    onSurfaceVariant = BeityTextMutedDark,
    outline = BeityOutlineDark,
    outlineVariant = BeityOutlineDark,
    error = ColorErrorDark(),
    onError = BeityNavyDark,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF531818),
    onErrorContainer = ColorErrorDark()
)

private fun ColorErrorDark() = androidx.compose.ui.graphics.Color(0xFFFF8A8A)

val BeityShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BeityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BeityDarkColorScheme else BeityLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BeityTypography,
        shapes = BeityShapes,
        content = content
    )
}
