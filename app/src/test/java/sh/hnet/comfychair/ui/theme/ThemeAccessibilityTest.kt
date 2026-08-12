package sh.hnet.comfychair.ui.theme

import org.junit.Test
import org.junit.Assert.assertTrue
import kotlin.math.max
import kotlin.math.min

/**
 * Verifies that the custom dark theme palette meets WCAG AA contrast requirements.
 *
 * WCAG AA requires a contrast ratio of at least 4.5:1 for normal text
 * and 3.0:1 for large text (18sp+).
 */
class ThemeAccessibilityTest {

    /**
     * Calculate the relative luminance of a color.
     * See https://www.w3.org/TR/WCAG20/#relativeluminancedef
     *
     * @param colorInt ARGB color value (0xAARRGGBB)
     */
    private fun relativeLuminance(colorInt: Long): Double {
        val r = ((colorInt shr 16) and 0xFF) / 255.0
        val g = ((colorInt shr 8) and 0xFF) / 255.0
        val b = (colorInt and 0xFF) / 255.0

        val rLinear = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gLinear = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bLinear = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        return 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
    }

    /**
     * Calculate WCAG contrast ratio between two colors.
     * See https://www.w3.org/TR/WCAG20/#contrast-ratiodef
     *
     * @return Contrast ratio in range [1, 21]
     */
    private fun contrastRatio(color1: Long, color2: Long): Double {
        val lum1 = relativeLuminance(color1)
        val lum2 = relativeLuminance(color2)
        val lighter = max(lum1, lum2)
        val darker = min(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    // Custom dark palette color values (must match Color.kt)
    private val darkPrimary = 0xFF0B3C5DL    // DarkPrimary
    private val darkSecondary = 0xFFE1AA8DL   // DarkSecondary
    private val darkBackground = 0xFF121212L  // DarkBackground
    private val darkSurface = 0xFF1E1E1EL     // DarkSurface
    private val onPrimary = 0xFFFFFFFFL       // White
    private val onSecondary = 0xFF000000L      // Black
    private val onBackground = 0xFFFFFFFFL    // White
    private val onSurface = 0xFFFFFFFFL       // White

    /**
     * Verify that onPrimary text on primary background meets WCAG AA (≥ 4.5:1).
     */
    @Test
    fun `onPrimary on DarkPrimary meets WCAG AA contrast`() {
        val ratio = contrastRatio(onPrimary, darkPrimary)
        assertTrue(
            "onPrimary (#FFFFFF) on DarkPrimary (#0B3C5D) contrast ratio is ${"%.2f".format(ratio)}:1, " +
                    "expected ≥ 4.5:1 for WCAG AA",
            ratio >= 4.5
        )
    }

    /**
     * Verify that onSecondary text on secondary background meets WCAG AA (≥ 4.5:1).
     */
    @Test
    fun `onSecondary on DarkSecondary meets WCAG AA contrast`() {
        val ratio = contrastRatio(onSecondary, darkSecondary)
        assertTrue(
            "onSecondary (#000000) on DarkSecondary (#E1AA8D) contrast ratio is ${"%.2f".format(ratio)}:1, " +
                    "expected ≥ 4.5:1 for WCAG AA",
            ratio >= 4.5
        )
    }

    /**
     * Verify that onBackground text on background meets WCAG AA (≥ 4.5:1).
     */
    @Test
    fun `onBackground on DarkBackground meets WCAG AA contrast`() {
        val ratio = contrastRatio(onBackground, darkBackground)
        assertTrue(
            "onBackground (#FFFFFF) on DarkBackground (#121212) contrast ratio is ${"%.2f".format(ratio)}:1, " +
                    "expected ≥ 4.5:1 for WCAG AA",
            ratio >= 4.5
        )
    }

    /**
     * Verify that onSurface text on surface meets WCAG AA (≥ 4.5:1).
     */
    @Test
    fun `onSurface on DarkSurface meets WCAG AA contrast`() {
        val ratio = contrastRatio(onSurface, darkSurface)
        assertTrue(
            "onSurface (#FFFFFF) on DarkSurface (#1E1E1E) contrast ratio is ${"%.2f".format(ratio)}:1, " +
                    "expected ≥ 4.5:1 for WCAG AA",
            ratio >= 4.5
        )
    }

    /**
     * Verify that DarkSecondary on DarkBackground has at least large-text contrast (≥ 3.0:1).
     * Secondary is often used for accent elements which may be large.
     */
    @Test
    fun `DarkSecondary on DarkBackground meets large text contrast`() {
        val ratio = contrastRatio(darkSecondary, darkBackground)
        assertTrue(
            "DarkSecondary (#E1AA8D) on DarkBackground (#121212) contrast ratio is ${"%.2f".format(ratio)}:1, " +
                    "expected ≥ 3.0:1 for WCAG AA large text",
            ratio >= 3.0
        )
    }
}
