package ir.salamat.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = CoralSecondary,
    onSecondary = SurfaceLight,
    secondaryContainer = CoralContainer,
    onSecondaryContainer = OnCoralContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

/**
 * Main application theme with dynamic RTL / LTR layout direction support.
 */
@Composable
fun SalamatTheme(
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = salamatTypography(),
            content = content
        )
    }
}
