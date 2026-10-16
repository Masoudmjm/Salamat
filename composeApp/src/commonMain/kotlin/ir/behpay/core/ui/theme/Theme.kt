package ir.behpay.core.ui.theme

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
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

/**
 * Standard colors for OutlinedTextField across Behpay screens and dialogs.
 * Ensures crisp border visibility, proper contrast, and clear editable affordance.
 */
@Composable
fun behpayTextFieldColors(
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    focusedContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline,
    focusedBorderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
): androidx.compose.material3.TextFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = containerColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    cursorColor = MaterialTheme.colorScheme.primary
)

/**
 * Main application theme with dynamic RTL / LTR layout direction support.
 */
@Composable
fun BehpayTheme(
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = behpayTypography(),
            content = content
        )
    }
}
