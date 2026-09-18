package ir.salamat.core.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Multiplatform launcher for picking an image from the user's gallery / device.
 * Returns a callback to trigger the system picker, and invokes [onImagePicked] with
 * a compressed Base64 data URL string upon success, or null if cancelled.
 */
@Composable
expect fun rememberImagePicker(
    onImagePicked: (String?) -> Unit
): () -> Unit

/**
 * Decodes a Base64-encoded image string into a Compose [ImageBitmap].
 * Returns null if the string is invalid or decoding fails.
 */
expect fun decodeBase64ToBitmap(base64Str: String): ImageBitmap?
