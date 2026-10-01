package ir.behpay.ui.screens.growth.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.behpay.core.model.GrowthRecord
import ir.behpay.ui.screens.growth.GrowthChartMetric

@Composable
fun GrowthChartCard(
    records: List<GrowthRecord>,
    isChild: Boolean,
    idealWeightRange: Pair<Double, Double>?,
    isPersian: Boolean,
    modifier: Modifier = Modifier
) {
    val availableMetrics = remember(records, isChild) {
        GrowthChartDataHelper.getAvailableMetrics(records, isChild)
    }

    var selectedMetric by remember(availableMetrics) {
        mutableStateOf(availableMetrics.firstOrNull() ?: GrowthChartMetric.WEIGHT)
    }

    // Fallback if current metric is not available in updated list
    if (selectedMetric !in availableMetrics && availableMetrics.isNotEmpty()) {
        selectedMetric = availableMetrics.first()
    }

    val points = remember(records, selectedMetric, isPersian) {
        GrowthChartDataHelper.extractPoints(records, selectedMetric, isPersian)
    }

    val deltaSummary = remember(points, selectedMetric, isPersian) {
        GrowthChartDataHelper.calculateDeltaSummary(points, selectedMetric, isPersian)
    }

    var selectedPointIndex by remember(points, selectedMetric) {
        mutableStateOf<Int?>(null)
    }

    val metricColor = when (selectedMetric) {
        GrowthChartMetric.WEIGHT -> Color(0xFF2E7D32)            // Forest Green
        GrowthChartMetric.HEIGHT -> Color(0xFF0288D1)            // Ocean Blue
        GrowthChartMetric.HEAD_CIRCUMFERENCE -> Color(0xFF7B1FA2) // Deep Purple
        GrowthChartMetric.BMI -> Color(0xFFE65100)               // Burnt Amber
    }

    val healthyRange = when (selectedMetric) {
        GrowthChartMetric.WEIGHT -> idealWeightRange?.let {
            if (it.first > 0 && it.second > 0) Pair(it.first.toFloat(), it.second.toFloat()) else null
        }
        GrowthChartMetric.BMI -> Pair(18.5f, 24.9f)
        else -> null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title & Latest Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPersian) "روند پایش رشد" else "Growth Trend Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPersian) "تغییرات پارامترهای رشد در طول زمان" else "Growth parameters over time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (points.isNotEmpty()) {
                    val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.last()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = metricColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = activePoint.displayValue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = metricColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (metric in availableMetrics) {
                    val isSelected = metric == selectedMetric
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMetric = metric
                            selectedPointIndex = null
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = metric.icon())
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersian) metric.titleFa() else metric.titleEn(),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = metricColor.copy(alpha = 0.15f),
                            selectedLabelColor = metricColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Viewport or Guidance Fallback
            if (points.size >= 2) {
                GrowthCanvasPlot(
                    points = points,
                    metric = selectedMetric,
                    accentColor = metricColor,
                    healthyRange = healthyRange,
                    isPersian = isPersian,
                    selectedIndex = selectedPointIndex,
                    onSelectIndex = { selectedPointIndex = it }
                )

                // Growth Insight Banner
                if (deltaSummary != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GrowthInsightBanner(
                        deltaSummary = deltaSummary,
                        metricColor = metricColor,
                        isPersian = isPersian
                    )
                }
            } else if (points.size == 1) {
                GrowthCanvasPlot(
                    points = points,
                    metric = selectedMetric,
                    accentColor = metricColor,
                    healthyRange = healthyRange,
                    isPersian = isPersian,
                    selectedIndex = 0,
                    onSelectIndex = {}
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian)
                            "با ثبت اندازه‌گیری‌های بعدی، خط روند و تحلیل تغییرات در اینجا محاسبه می‌شود."
                        else
                            "Log more measurements to calculate growth velocity and trend curves.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Empty state for this specific metric
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPersian) "رکوردی برای این شاخص ثبت نشده است" else "No records for this metric",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthInsightBanner(
    deltaSummary: GrowthDeltaSummary,
    metricColor: Color,
    isPersian: Boolean,
    modifier: Modifier = Modifier
) {
    val indicatorSymbol = when (deltaSummary.direction) {
        TrendDirection.INCREASING -> "↗"
        TrendDirection.DECREASING -> "↘"
        TrendDirection.STABLE -> "→"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(metricColor.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = metricColor.copy(alpha = 0.20f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = indicatorSymbol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = metricColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = deltaSummary.insightText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
