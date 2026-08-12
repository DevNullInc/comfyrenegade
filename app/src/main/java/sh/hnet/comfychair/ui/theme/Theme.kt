package sh.hnet.comfychair.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import sh.hnet.comfychair.storage.AppSettings

// Custom dark color scheme using the palette from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme()

@Composable
fun ComfyChairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    // Override status bar appearance (null = auto based on theme, true = light icons, false = dark icons)
    forceDarkStatusBar: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Read the persisted font family key and build typography.
    // Falls back to default if the selected font cannot be resolved.
    val fontFamilyKey = AppSettings.getFontFamily(context)
    val typography = remember(fontFamilyKey) {
        try {
            val fontFamily = resolveFontFamily(fontFamilyKey)
            createTypography(fontFamily)
        } catch (_: Exception) {
            AppTypography
        }
    }

    // Update status bar icon colors based on theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status bar = dark icons (for light theme)
            // Dark status bar = light icons (for dark theme)
            // forceDarkStatusBar overrides automatic behavior
            insetsController.isAppearanceLightStatusBars = when (forceDarkStatusBar) {
                true -> false  // Force light icons (dark status bar)
                false -> true  // Force dark icons (light status bar)
                null -> !darkTheme  // Auto based on theme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

