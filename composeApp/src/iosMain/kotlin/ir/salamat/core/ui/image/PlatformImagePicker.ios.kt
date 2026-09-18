package ir.salamat.core.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberImagePicker(
    onImagePicked: (String?) -> Unit
): () -> Unit {
    return {
        onImagePicked(null)
    }
}

actual fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return null
}
