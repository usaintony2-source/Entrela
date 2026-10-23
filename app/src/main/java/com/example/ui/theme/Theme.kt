package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EntrelaColorScheme = darkColorScheme(
    primary = EntrelaPrimary,
    onPrimary = Color.White,
    primaryContainer = EntrelaDarkSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = EntrelaSecondary,
    onSecondary = Color.White,
    secondaryContainer = EntrelaDarkSurfaceVariant,
    onSecondaryContainer = EntrelaTextPrimary,
    tertiary = EntrelaTertiary,
    background = EntrelaDarkBg,
    onBackground = EntrelaTextPrimary,
    surface = EntrelaDarkSurface,
    onSurface = EntrelaTextPrimary,
    surfaceVariant = EntrelaDarkSurfaceVariant,
    onSurfaceVariant = EntrelaTextSecondary,
    outline = EntrelaDarkBorder,
    error = EntrelaHeartRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark mode preferred per requirements
    dynamicColor: Boolean = false, // Keep consistent sleek dark branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EntrelaColorScheme,
        typography = Typography,
        content = content
    )
}
