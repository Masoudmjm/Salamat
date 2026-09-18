package ir.salamat.core.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.browser.document
import org.jetbrains.skia.Image
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
actual fun rememberImagePicker(
    onImagePicked: (String?) -> Unit
): () -> Unit {
    return {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val result = reader.result as? String
                    onImagePicked(result)
                }
                reader.readAsDataURL(file)
            } else {
                onImagePicked(null)
            }
        }
        input.click()
    }
}

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return try {
        val clean = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
        val bytes = Base64.decode(clean)
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
