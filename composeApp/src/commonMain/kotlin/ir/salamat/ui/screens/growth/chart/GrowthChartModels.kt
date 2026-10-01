package ir.salamat.ui.screens.growth.chart

import ir.salamat.core.datetime.toJalali
import ir.salamat.core.datetime.toPersianDigits
import ir.salamat.core.model.GrowthRecord
import ir.salamat.ui.screens.growth.GrowthChartMetric
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

data class GrowthChartPoint(
    val date: LocalDate,
    val value: Float,
    val displayValue: String,
    val dateFormatted: String
)

enum class TrendDirection {
    INCREASING,
    STABLE,
    DECREASING
}

data class GrowthDeltaSummary(
    val startValue: Float,
    val latestValue: Float,
    val delta: Float,
    val formattedDelta: String,
    val direction: TrendDirection,
    val durationText: String,
    val insightText: String
)

object GrowthChartDataHelper {

    /**
     * Extracts chronological chart points for the specified metric.
     */
    fun extractPoints(
        records: List<GrowthRecord>,
        metric: GrowthChartMetric,
        isPersian: Boolean
    ): List<GrowthChartPoint> {
        val chronological = records.sortedBy { it.date }
        val points = mutableListOf<GrowthChartPoint>()

        for (record in chronological) {
            val rawValue: Double? = when (metric) {
                GrowthChartMetric.WEIGHT -> record.weightKg
                GrowthChartMetric.HEIGHT -> record.heightCm
                GrowthChartMetric.HEAD_CIRCUMFERENCE -> record.headCircumferenceCm
                GrowthChartMetric.BMI -> record.bmi
            }

            if (rawValue != null && rawValue > 0) {
                val floatVal = (round1Decimal(rawValue)).toFloat()
                val jalali = record.date.toJalali()
                val dateStr = if (isPersian) {
                    "${jalali.day.toString().toPersianDigits()} ${jalali.formatPersian().split(" ").getOrNull(1) ?: ""}"
                } else {
                    "${record.date.month.name.take(3)} ${record.date.day}"
                }

                val unit = if (isPersian) metric.unitFa() else metric.unitEn()
                val displayVal = if (isPersian) {
                    "${floatVal.toString().toPersianDigits()} $unit".trim()
                } else {
                    "$floatVal $unit".trim()
                }

                points.add(
                    GrowthChartPoint(
                        date = record.date,
                        value = floatVal,
                        displayValue = displayVal,
                        dateFormatted = dateStr
                    )
                )
            }
        }
        return points
    }

    /**
     * Calculates growth delta and health insight from first to latest data point.
     */
    fun calculateDeltaSummary(
        points: List<GrowthChartPoint>,
        metric: GrowthChartMetric,
        isPersian: Boolean
    ): GrowthDeltaSummary? {
        if (points.size < 2) return null

        val first = points.first()
        val latest = points.last()
        val delta = round1Decimal((latest.value - first.value).toDouble()).toFloat()

        val direction = when {
            delta > 0.1f -> TrendDirection.INCREASING
            delta < -0.1f -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }

        val daysBetween = calculateDaysBetween(first.date, latest.date)
        val durationText = formatDuration(daysBetween, isPersian)

        val unit = if (isPersian) metric.unitFa() else metric.unitEn()
        val absDeltaStr = abs(delta).toString().let { if (isPersian) it.toPersianDigits() else it }
        val prefix = if (delta > 0) "+" else if (delta < 0) "-" else ""
        val formattedDelta = "$prefix$absDeltaStr $unit".trim()

        val insight = if (isPersian) {
            when (direction) {
                TrendDirection.INCREASING -> "رشد صعودی در $durationText ($formattedDelta)"
                TrendDirection.DECREASING -> "کاهش در $durationText ($formattedDelta)"
                TrendDirection.STABLE -> "وضعیت باثبات در $durationText"
            }
        } else {
            when (direction) {
                TrendDirection.INCREASING -> "Upward trend over $durationText ($formattedDelta)"
                TrendDirection.DECREASING -> "Downward change over $durationText ($formattedDelta)"
                TrendDirection.STABLE -> "Stable over $durationText"
            }
        }

        return GrowthDeltaSummary(
            startValue = first.value,
            latestValue = latest.value,
            delta = delta,
            formattedDelta = formattedDelta,
            direction = direction,
            durationText = durationText,
            insightText = insight
        )
    }

    /**
     * Checks if records contain valid head circumference data.
     */
    fun hasHeadCircumference(records: List<GrowthRecord>): Boolean {
        return records.any { it.headCircumferenceCm != null && it.headCircumferenceCm > 0 }
    }

    /**
     * Determines which metrics to display in the switcher.
     */
    fun getAvailableMetrics(records: List<GrowthRecord>, isChild: Boolean): List<GrowthChartMetric> {
        return if (isChild && hasHeadCircumference(records)) {
            listOf(
                GrowthChartMetric.WEIGHT,
                GrowthChartMetric.HEIGHT,
                GrowthChartMetric.HEAD_CIRCUMFERENCE,
                GrowthChartMetric.BMI
            )
        } else {
            listOf(
                GrowthChartMetric.WEIGHT,
                GrowthChartMetric.HEIGHT,
                GrowthChartMetric.BMI
            )
        }
    }

    private fun round1Decimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }

    private fun calculateDaysBetween(from: LocalDate, to: LocalDate): Int {
        val days = (to.toEpochDays() - from.toEpochDays()).toInt()
        return maxOf(days, 1)
    }

    private fun formatDuration(days: Int, isPersian: Boolean): String {
        return when {
            days < 30 -> {
                val num = days.toString().let { if (isPersian) it.toPersianDigits() else it }
                if (isPersian) "$num روز" else "$days days"
            }
            days < 365 -> {
                val months = maxOf(1, days / 30)
                val num = months.toString().let { if (isPersian) it.toPersianDigits() else it }
                if (isPersian) "$num ماه" else "$months months"
            }
            else -> {
                val years = round1Decimal(days / 365.0)
                val num = years.toString().let { if (isPersian) it.toPersianDigits() else it }
                if (isPersian) "$num سال" else "$years years"
            }
        }
    }
}
