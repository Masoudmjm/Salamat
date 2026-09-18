package ir.salamat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.salamat.core.ui.image.decodeBase64ToBitmap

/**
 * Universal Profile Avatar Component.
 * Displays the member's photo if present and valid; otherwise falls back gracefully
 * to a styled circular monogram with their theme avatar color and name initial.
 */
@Composable
fun ProfileAvatar(
    name: String,
    avatarColor: Int,
    avatarPhoto: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    border: BorderStroke? = null
) {
    val bitmap = remember(avatarPhoto) {
        if (!avatarPhoto.isNullOrBlank()) decodeBase64ToBitmap(avatarPhoto) else null
    }

    val baseModifier = modifier
        .size(size)
        .let { if (border != null) it.border(border, shape) else it }
        .clip(shape)

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    } else {
        Box(
            modifier = baseModifier.background(Color(avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            val initial = name.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
            val fontSize = (size.value * 0.44f).coerceAtLeast(10f).sp
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}
