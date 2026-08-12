package sh.hnet.comfychair.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import sh.hnet.comfychair.R

/**
 * OpenDyslexic font family, loaded from bundled OTF resources.
 * Provides Regular, Bold, Italic, and BoldItalic variants.
 */
val OpenDyslexicFontFamily = FontFamily(
    Font(R.font.open_dyslexic_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.open_dyslexic_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.open_dyslexic_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.open_dyslexic_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/**
 * Mapping of font family identifier strings to their FontFamily objects.
 * The key is stored in AppSettings for persistence.
 */
val AvailableFontFamilies: List<Pair<String, FontFamily>> = listOf(
    "default" to FontFamily.Default,
    "open_dyslexic" to OpenDyslexicFontFamily,
    "serif" to FontFamily.Serif,
    "sans_serif" to FontFamily.SansSerif,
    "monospace" to FontFamily.Monospace,
    "cursive" to FontFamily.Cursive
)

/**
 * Resolves a persisted font family key to its FontFamily.
 * Falls back to [FontFamily.Default] if the key is unknown or the font fails to load.
 */
fun resolveFontFamily(key: String): FontFamily {
    return try {
        AvailableFontFamilies.firstOrNull { it.first == key }?.second ?: FontFamily.Default
    } catch (_: Exception) {
        FontFamily.Default
    }
}

/**
 * Creates a Material 3 [Typography] instance using the given [fontFamily].
 * All text styles use the provided font family while preserving standard M3 sizing.
 */
fun createTypography(fontFamily: FontFamily = FontFamily.Default): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Default app typography using the system default font family.
 * Referenced by [ComfyChairTheme] as the default value.
 */
val AppTypography = createTypography()

