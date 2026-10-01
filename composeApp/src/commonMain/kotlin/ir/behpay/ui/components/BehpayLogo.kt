package ir.behpay.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.behpay.core.ui.theme.BehpayTheme

private const val SHIELD_PATH_DATA =
    "M54,20 C39,20 27,25 27,25 C27,25 25,53 35,69 C41,79 49,85 54,88 C59,85 67,79 73,69 C83,53 81,25 81,25 C81,25 69,20 54,20 Z " +
    "M54,79.5 C49.5,76.5 43.5,71 39,63 C32.5,51.5 34,31.5 34,31.5 C34,31.5 43,27.5 54,27.5 C65,27.5 74,31.5 74,31.5 C74,31.5 75.5,51.5 69,63 C64.5,71 58.5,76.5 54,79.5 Z"

private const val HEART_PATH_DATA =
    "M54,68 C54,68 38,57.5 38,46.5 C38,40.5 42.8,36 48.2,36 C51.4,36 53.2,37.8 54,39.8 C54.8,37.8 56.6,36 59.8,36 C65.2,36 70,40.5 70,46.5 C70,57.5 54,68 54,68 Z"

private const val SPROUT_STEM_PATH_DATA =
    "M53,68.5 C52.5,58 55.5,50 61.5,45 C57.5,50.5 55.5,58 55.8,67.5 Z"

private const val LITTLE_LEAF_PATH_DATA =
    "M55.2,55.5 C50,55.5 46.8,52.5 46.2,47.8 C50.8,47.8 54.2,50.5 55.2,55.5 Z"

@Composable
fun BehpayLogo(
    size: Dp = 80.dp,
    shape: Shape = RoundedCornerShape(percent = 26),
    shadowElevation: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = shape,
        shadowElevation = shadowElevation,
        color = Color.Transparent
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            // 1. Deep Teal diagonal gradient background (#138A91 -> #0A686D -> #06464A)
            drawRect(
                brush = Brush.linearGradient(
                    0.0f to Color(0xFF138A91),
                    0.65f to Color(0xFF0A686D),
                    1.0f to Color(0xFF06464A),
                    start = Offset.Zero,
                    end = Offset(canvasWidth, canvasHeight)
                )
            )

            // Scale from 108x108 icon coordinate space to actual canvas size
            val scaleX = canvasWidth / 108f
            val scaleY = canvasHeight / 108f

            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                // Apply 0.82x inner scale around center (54, 54) for comfortable vertical padding
                scale(scaleX = 0.82f, scaleY = 0.82f, pivot = Offset(54f, 54f)) {
                    val shieldPath: Path = PathParser().parsePathString(SHIELD_PATH_DATA).toPath().apply {
                        fillType = PathFillType.EvenOdd
                    }
                    val heartPath: Path = PathParser().parsePathString(HEART_PATH_DATA).toPath()
                    val sproutStemPath: Path = PathParser().parsePathString(SPROUT_STEM_PATH_DATA).toPath()
                    val littleLeafPath: Path = PathParser().parsePathString(LITTLE_LEAF_PATH_DATA).toPath()

                    // 2. White Protective Shield
                    drawPath(path = shieldPath, color = Color.White)

                    // 3. Full Warm Coral Heart
                    drawPath(path = heartPath, color = Color(0xFFE76F51))

                    // 4. Curved Sprout Stem + Little Side Leaf
                    drawPath(path = sproutStemPath, color = Color(0xFF0A686D))
                    drawPath(path = littleLeafPath, color = Color(0xFF0A686D))
                }
            }
        }
    }
}

@Preview
@Composable
private fun BehpayLogoPreview() {
    BehpayTheme {
        BehpayLogo(size = 96.dp)
    }
}
