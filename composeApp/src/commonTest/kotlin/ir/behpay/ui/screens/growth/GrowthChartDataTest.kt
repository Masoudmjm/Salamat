package ir.behpay.ui.screens.growth

import ir.behpay.core.model.GrowthRecord
import ir.behpay.ui.screens.growth.chart.GrowthChartDataHelper
import ir.behpay.ui.screens.growth.chart.TrendDirection
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GrowthChartDataTest {

    private val r1 = GrowthRecord(
        id = "r1",
        profileId = "p1",
        date = LocalDate(2024, 3, 15),
        weightKg = 11.2,
        heightCm = 82.0,
        headCircumferenceCm = 46.5,
        bmi = 16.7,
        notes = "10 months",
        createdAt = 100L
    )

    private val r2 = GrowthRecord(
        id = "r2",
        profileId = "p1",
        date = LocalDate(2023, 11, 5),
        weightKg = 8.6,
        heightCm = 71.0,
        headCircumferenceCm = 43.5,
        bmi = 17.1,
        notes = "6 months",
        createdAt = 50L
    )

    private val r3 = GrowthRecord(
        id = "r3",
        profileId = "p1",
        date = LocalDate(2024, 1, 10),
        weightKg = 9.8,
        heightCm = 76.0,
        headCircumferenceCm = 45.0,
        bmi = 17.0,
        notes = "8 months",
        createdAt = 80L
    )

    @Test
    fun testExtractPointsChronologicalOrder() {
        // Records passed in mixed order: r1 (March 2024), r2 (Nov 2023), r3 (Jan 2024)
        val records = listOf(r1, r2, r3)
        val points = GrowthChartDataHelper.extractPoints(records, GrowthChartMetric.WEIGHT, isPersian = true)

        assertEquals(3, points.size)
        // First point should be r2 (Nov 2023)
        assertEquals(LocalDate(2023, 11, 5), points[0].date)
        assertEquals(8.6f, points[0].value)

        // Second point should be r3 (Jan 2024)
        assertEquals(LocalDate(2024, 1, 10), points[1].date)
        assertEquals(9.8f, points[1].value)

        // Third point should be r1 (March 2024)
        assertEquals(LocalDate(2024, 3, 15), points[2].date)
        assertEquals(11.2f, points[2].value)
    }

    @Test
    fun testHeadCircumferenceExcludesNulls() {
        val rWithoutHead = GrowthRecord(
            id = "r4",
            profileId = "p1",
            date = LocalDate(2024, 5, 1),
            weightKg = 11.8,
            heightCm = 84.0,
            headCircumferenceCm = null,
            bmi = 16.7,
            notes = null,
            createdAt = 120L
        )

        val records = listOf(r2, rWithoutHead)
        val weightPoints = GrowthChartDataHelper.extractPoints(records, GrowthChartMetric.WEIGHT, isPersian = true)
        val headPoints = GrowthChartDataHelper.extractPoints(records, GrowthChartMetric.HEAD_CIRCUMFERENCE, isPersian = true)

        assertEquals(2, weightPoints.size)
        assertEquals(1, headPoints.size)
        assertEquals(43.5f, headPoints[0].value)
    }

    @Test
    fun testDeltaSummaryCalculation() {
        val records = listOf(r2, r1) // 8.6kg -> 11.2kg (+2.6kg)
        val points = GrowthChartDataHelper.extractPoints(records, GrowthChartMetric.WEIGHT, isPersian = true)
        val delta = GrowthChartDataHelper.calculateDeltaSummary(points, GrowthChartMetric.WEIGHT, isPersian = true)

        assertNotNull(delta)
        assertEquals(TrendDirection.INCREASING, delta.direction)
        assertEquals(2.6f, delta.delta)
        assertTrue(delta.formattedDelta.contains("+") || delta.formattedDelta.contains("۲.۶"))
        assertTrue(delta.insightText.isNotBlank())
    }

    @Test
    fun testDeltaSummarySinglePointReturnsNull() {
        val records = listOf(r1)
        val points = GrowthChartDataHelper.extractPoints(records, GrowthChartMetric.WEIGHT, isPersian = true)
        val delta = GrowthChartDataHelper.calculateDeltaSummary(points, GrowthChartMetric.WEIGHT, isPersian = true)

        assertNull(delta)
    }

    @Test
    fun testAvailableMetricsChildVsAdult() {
        val childRecords = listOf(r1, r2)
        val childMetrics = GrowthChartDataHelper.getAvailableMetrics(childRecords, isChild = true)
        assertEquals(4, childMetrics.size)
        assertTrue(childMetrics.contains(GrowthChartMetric.HEAD_CIRCUMFERENCE))

        // Adult should never show head circumference even if recorded
        val adultMetrics = GrowthChartDataHelper.getAvailableMetrics(childRecords, isChild = false)
        assertEquals(3, adultMetrics.size)
        assertTrue(!adultMetrics.contains(GrowthChartMetric.HEAD_CIRCUMFERENCE))
    }
}
