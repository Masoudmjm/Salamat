package ir.salamat.ui.screens.growth.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.salamat.core.datetime.toPersianDigits
import ir.salamat.ui.screens.growth.GrowthChartMetric
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun GrowthCanvasPlot(
    points: List<GrowthChartPoint>,
    metric: GrowthChartMetric,
    accentColor: Color,
    healthyRange: Pair<Float, Float>?,
    isPersian: Boolean,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSelectIndex: (Int?) -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(points, metric) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val healthyZoneColor = Color(0xFF2E7D32).copy(alpha = 0.10f)
    val healthyLineColor = Color(0xFF2E7D32).copy(alpha = 0.40f)

    val labelTextStyle = remember(onSurfaceVariantColor) {
        TextStyle(
            color = onSurfaceVariantColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }

    val tooltipTitleStyle = remember(accentColor) {
        TextStyle(
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }

    val tooltipSubStyle = remember(onSurfaceVariantColor) {
        TextStyle(
            color = onSurfaceVariantColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Normal
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .padding(vertical = 8.dp)
                .pointerInput(points) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (points.size >= 2) {
                                val chartWidth = size.width - 90.dp.toPx()
                                val startX = 50.dp.toPx()
                                val stepX = chartWidth / (points.size - 1)
                                val tappedIdx = points.indices.minByOrNull { i ->
                                    val px = startX + i * stepX
                                    abs(px - offset.x)
                                }
                                onSelectIndex(if (tappedIdx == selectedIndex) null else tappedIdx)
                            } else if (points.size == 1) {
                                onSelectIndex(if (selectedIndex == 0) null else 0)
                            }
                        }
                    )
                }
        ) {
            if (points.isEmpty()) return@Canvas

            val leftPadding = 50.dp.toPx()
            val rightPadding = 24.dp.toPx()
            val topPadding = 32.dp.toPx()
            val bottomPadding = 36.dp.toPx()

            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding

            if (points.size == 1) {
                drawSinglePointPlot(
                    point = points.first(),
                    centerX = size.width / 2f,
                    centerY = size.height / 2f,
                    accentColor = accentColor,
                    surfaceColor = surfaceColor,
                    textMeasurer = textMeasurer,
                    tooltipTitleStyle = tooltipTitleStyle,
                    tooltipSubStyle = tooltipSubStyle
                )
                return@Canvas
            }

            // Calculate min and max bounds for Y axis
            val pointValues = points.map { it.value }
            var minY = pointValues.minOrNull() ?: 0f
            var maxY = pointValues.maxOrNull() ?: 100f

            if (healthyRange != null) {
                minY = min(minY, healthyRange.first)
                maxY = max(maxY, healthyRange.second)
            }

            val yPadding = max((maxY - minY) * 0.18f, 1f)
            val effectiveMinY = minY - yPadding
            val effectiveMaxY = maxY + yPadding
            val ySpan = max(effectiveMaxY - effectiveMinY, 0.001f)

            fun valueToY(v: Float): Float {
                val ratio = (v - effectiveMinY) / ySpan
                return topPadding + (1f - ratio) * chartHeight
            }

            // 1. Draw Healthy Zone Band if provided
            if (healthyRange != null && healthyRange.first > 0 && healthyRange.second > 0) {
                val bandTopY = valueToY(healthyRange.second)
                val bandBottomY = valueToY(healthyRange.first)
                val bandHeight = bandBottomY - bandTopY
                if (bandHeight > 0) {
                    drawRoundRect(
                        color = healthyZoneColor,
                        topLeft = Offset(leftPadding, bandTopY),
                        size = Size(chartWidth, bandHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    // Dashed lines for bounds
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = healthyLineColor,
                        start = Offset(leftPadding, bandTopY),
                        end = Offset(leftPadding + chartWidth, bandTopY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashPathEffect
                    )
                    drawLine(
                        color = healthyLineColor,
                        start = Offset(leftPadding, bandBottomY),
                        end = Offset(leftPadding + chartWidth, bandBottomY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashPathEffect
                    )
                }
            }

            // 2. Draw 4 Horizontal Grid Lines & Y-Axis Labels
            val gridCount = 4
            for (g in 0 until gridCount) {
                val ratio = g.toFloat() / (gridCount - 1)
                val gridVal = effectiveMinY + ratio * (effectiveMaxY - effectiveMinY)
                val gy = topPadding + (1f - ratio) * chartHeight

                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, gy),
                    end = Offset(leftPadding + chartWidth, gy),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                val labelNum = ((gridVal * 10f).roundToInt() / 10f).toString()
                val labelStr = if (isPersian) labelNum.toPersianDigits() else labelNum
                val measured = textMeasurer.measure(text = labelStr, style = labelTextStyle)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(leftPadding - measured.size.width - 6.dp.toPx(), gy - measured.size.height / 2f)
                )
            }

            // 3. Map Points to Canvas Coordinates
            val stepX = chartWidth / (points.size - 1)
            val animatedPoints = points.indices.map { i ->
                val px = leftPadding + i * stepX
                val targetPy = valueToY(points[i].value)
                val baseY = topPadding + chartHeight
                val currentPy = baseY - (baseY - targetPy) * progressAnim.value
                Offset(px, currentPy)
            }

            // 4. Draw Smooth Spline (Cubic Bézier)
            val strokePath = Path()
            val fillPath = Path()

            strokePath.moveTo(animatedPoints.first().x, animatedPoints.first().y)
            fillPath.moveTo(animatedPoints.first().x, topPadding + chartHeight)
            fillPath.lineTo(animatedPoints.first().x, animatedPoints.first().y)

            for (i in 0 until animatedPoints.size - 1) {
                val p0 = animatedPoints[i]
                val p1 = animatedPoints[i + 1]
                val midX = (p0.x + p1.x) / 2f

                strokePath.cubicTo(
                    midX, p0.y,
                    midX, p1.y,
                    p1.x, p1.y
                )
                fillPath.cubicTo(
                    midX, p0.y,
                    midX, p1.y,
                    p1.x, p1.y
                )
            }

            fillPath.lineTo(animatedPoints.last().x, topPadding + chartHeight)
            fillPath.close()

            // Draw Area Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.28f),
                        accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = topPadding,
                    endY = topPadding + chartHeight
                )
            )

            // Draw Curve Stroke
            drawPath(
                path = strokePath,
                color = accentColor,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Draw X-Axis Date Labels & Measurement Dots
            for (i in points.indices) {
                val pos = animatedPoints[i]
                val dateLabel = points[i].dateFormatted
                val measuredDate = textMeasurer.measure(text = dateLabel, style = labelTextStyle)

                drawText(
                    textLayoutResult = measuredDate,
                    topLeft = Offset(
                        pos.x - measuredDate.size.width / 2f,
                        topPadding + chartHeight + 8.dp.toPx()
                    )
                )

                // Outer circle ring
                drawCircle(
                    color = surfaceColor,
                    radius = 4.5.dp.toPx(),
                    center = pos,
                    style = Fill
                )
                drawCircle(
                    color = accentColor,
                    radius = 4.5.dp.toPx(),
                    center = pos,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Inner center dot
                drawCircle(
                    color = accentColor,
                    radius = 2.dp.toPx(),
                    center = pos,
                    style = Fill
                )
            }

            // 6. Interactive Scrubber & Floating Tooltip
            val activeIdx = selectedIndex ?: (points.size - 1)
            if (activeIdx in points.indices) {
                val activePoint = points[activeIdx]
                val activePos = animatedPoints[activeIdx]

                // Vertical dashed scrubber line
                drawLine(
                    color = accentColor.copy(alpha = 0.6f),
                    start = Offset(activePos.x, topPadding),
                    end = Offset(activePos.x, topPadding + chartHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )

                // Highlight circle ring
                drawCircle(
                    color = accentColor.copy(alpha = 0.22f),
                    radius = 11.dp.toPx(),
                    center = activePos
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.5.dp.toPx(),
                    center = activePos
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.5.dp.toPx(),
                    center = activePos
                )

                // Render Floating Tooltip
                drawTooltip(
                    point = activePoint,
                    anchor = activePos,
                    chartWidth = size.width,
                    chartHeight = size.height,
                    surfaceColor = surfaceColor,
                    accentColor = accentColor,
                    textMeasurer = textMeasurer,
                    tooltipTitleStyle = tooltipTitleStyle,
                    tooltipSubStyle = tooltipSubStyle
                )
            }
        }
    }
}

private fun DrawScope.drawSinglePointPlot(
    point: GrowthChartPoint,
    centerX: Float,
    centerY: Float,
    accentColor: Color,
    surfaceColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    tooltipTitleStyle: TextStyle,
    tooltipSubStyle: TextStyle
) {
    // Large pulsating marker
    drawCircle(
        color = accentColor.copy(alpha = 0.18f),
        radius = 26.dp.toPx(),
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = surfaceColor,
        radius = 9.dp.toPx(),
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = accentColor,
        radius = 9.dp.toPx(),
        center = Offset(centerX, centerY),
        style = Stroke(width = 3.dp.toPx())
    )
    drawCircle(
        color = accentColor,
        radius = 4.dp.toPx(),
        center = Offset(centerX, centerY)
    )

    val titleMeas = textMeasurer.measure(text = point.displayValue, style = tooltipTitleStyle)
    val subMeas = textMeasurer.measure(text = point.dateFormatted, style = tooltipSubStyle)

    val textY = centerY + 32.dp.toPx()
    drawText(
        textLayoutResult = titleMeas,
        topLeft = Offset(centerX - titleMeas.size.width / 2f, textY)
    )
    drawText(
        textLayoutResult = subMeas,
        topLeft = Offset(centerX - subMeas.size.width / 2f, textY + titleMeas.size.height + 2.dp.toPx())
    )
}

private fun DrawScope.drawTooltip(
    point: GrowthChartPoint,
    anchor: Offset,
    chartWidth: Float,
    chartHeight: Float,
    surfaceColor: Color,
    accentColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    tooltipTitleStyle: TextStyle,
    tooltipSubStyle: TextStyle
) {
    val titleMeas = textMeasurer.measure(text = point.displayValue, style = tooltipTitleStyle)
    val subMeas = textMeasurer.measure(text = point.dateFormatted, style = tooltipSubStyle)

    val contentW = max(titleMeas.size.width, subMeas.size.width).toFloat()
    val boxPaddingX = 10.dp.toPx()
    val boxPaddingY = 6.dp.toPx()
    val boxWidth = contentW + boxPaddingX * 2
    val boxHeight = titleMeas.size.height + subMeas.size.height + boxPaddingY * 2 + 2.dp.toPx()

    // Determine X placement (prevent overflow left/right)
    var boxLeft = anchor.x - boxWidth / 2f
    if (boxLeft < 12.dp.toPx()) boxLeft = 12.dp.toPx()
    if (boxLeft + boxWidth > chartWidth - 12.dp.toPx()) {
        boxLeft = chartWidth - 12.dp.toPx() - boxWidth
    }

    // Determine Y placement (above point, or below if too close to top)
    var boxTop = anchor.y - boxHeight - 12.dp.toPx()
    if (boxTop < 6.dp.toPx()) {
        boxTop = anchor.y + 14.dp.toPx()
    }

    // Tooltip Container Background & Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(boxLeft + 1.dp.toPx(), boxTop + 2.dp.toPx()),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
        color = surfaceColor,
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
        color = accentColor.copy(alpha = 0.35f),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    // Draw Tooltip Text
    val titleX = boxLeft + (boxWidth - titleMeas.size.width) / 2f
    val titleY = boxTop + boxPaddingY
    drawText(
        textLayoutResult = titleMeas,
        topLeft = Offset(titleX, titleY)
    )

    val subX = boxLeft + (boxWidth - subMeas.size.width) / 2f
    val subY = titleY + titleMeas.size.height + 2.dp.toPx()
    drawText(
        textLayoutResult = subMeas,
        topLeft = Offset(subX, subY)
    )
}
