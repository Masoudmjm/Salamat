package ir.behpay.core.growth

import ir.behpay.core.model.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChildGrowthStandardTest {

    @Test
    fun testAppropriateGrowthEvaluationForBoys() {
        // Artin: 80cm, 10.0kg boy -> Expected appropriate range: 9.5kg - 12.1kg
        val evaluation = ChildGrowthStandard.evaluateWeightForHeight(
            weightKg = 10.0,
            heightCm = 80.0,
            gender = Gender.MALE
        )
        assertEquals(ChildGrowthStatus.APPROPRIATE, evaluation.status)
        assertEquals(Pair(9.5, 12.1), evaluation.idealWeightRange)
        assertEquals(10.6, evaluation.medianWeightKg)
        assertTrue(evaluation.percentileEstimate in 15..40)
    }

    @Test
    fun testAppropriateGrowthEvaluationForGirls() {
        // Sara: 82cm, 11.2kg girl -> Interpolated appropriate range ~9.5kg - 12.3kg
        val evaluation = ChildGrowthStandard.evaluateWeightForHeight(
            weightKg = 11.2,
            heightCm = 82.0,
            gender = Gender.FEMALE
        )
        assertEquals(ChildGrowthStatus.APPROPRIATE, evaluation.status)
        assertTrue(evaluation.idealWeightRange.first in 9.3..9.7)
        assertTrue(evaluation.idealWeightRange.second in 12.0..12.5)
        assertTrue(evaluation.percentileEstimate in 50..80)
    }

    @Test
    fun testUnderweightForHeightBoundary() {
        // Boy at 70cm: p3 = 7.5kg, Median = 8.4kg, p97 = 9.6kg
        // Weight 6.8kg -> Below p3 (Underweight on growth card)
        val evaluation = ChildGrowthStandard.evaluateWeightForHeight(
            weightKg = 6.8,
            heightCm = 70.0,
            gender = Gender.MALE
        )
        assertEquals(ChildGrowthStatus.UNDERWEIGHT_FOR_HEIGHT, evaluation.status)
        assertTrue(evaluation.percentileEstimate <= 3)
    }

    @Test
    fun testOverweightForHeightBoundary() {
        // Girl at 75cm: p3 = 8.1kg, Median = 9.1kg, p97 = 10.4kg
        // Weight 11.5kg -> Above p97 (Overweight curve)
        val evaluation = ChildGrowthStandard.evaluateWeightForHeight(
            weightKg = 11.5,
            heightCm = 75.0,
            gender = Gender.FEMALE
        )
        assertEquals(ChildGrowthStatus.OVERWEIGHT_FOR_HEIGHT, evaluation.status)
        assertTrue(evaluation.percentileEstimate >= 97)
    }

    @Test
    fun testInvalidOrZeroMeasurementsEdgeCases() {
        val zeroWeight = ChildGrowthStandard.evaluateWeightForHeight(0.0, 75.0, Gender.MALE)
        assertEquals(ChildGrowthStatus.APPROPRIATE, zeroWeight.status)
        assertEquals(Pair(0.0, 0.0), zeroWeight.idealWeightRange)

        val zeroHeight = ChildGrowthStandard.evaluateWeightForHeight(8.0, 0.0, Gender.FEMALE)
        assertEquals(ChildGrowthStatus.APPROPRIATE, zeroHeight.status)
        assertEquals(Pair(0.0, 0.0), zeroHeight.idealWeightRange)

        val negativeVal = ChildGrowthStandard.evaluateWeightForHeight(-5.0, -50.0, Gender.MALE)
        assertEquals(ChildGrowthStatus.APPROPRIATE, negativeVal.status)
    }

    @Test
    fun testHealthyWeightRangeAndMedianHelpers() {
        val boyRange = ChildGrowthStandard.getHealthyWeightRange(80.0, Gender.MALE)
        assertEquals(Pair(9.5, 12.1), boyRange)
        val boyMedian = ChildGrowthStandard.getMedianWeight(80.0, Gender.MALE)
        assertEquals(10.6, boyMedian)

        val girlRange = ChildGrowthStandard.getHealthyWeightRange(80.0, Gender.FEMALE)
        assertEquals(Pair(9.1, 11.7), girlRange)
        val girlMedian = ChildGrowthStandard.getMedianWeight(80.0, Gender.FEMALE)
        assertEquals(10.2, girlMedian)
    }

    @Test
    fun testLocalizationAndStyling() {
        for (status in ChildGrowthStatus.entries) {
            assertTrue(status.titleFa().isNotBlank())
            assertTrue(status.titleEn().isNotBlank())
            assertTrue(status.shortLabelFa().isNotBlank())
            assertTrue(status.shortLabelEn().isNotBlank())
            assertTrue(status.recommendationFa().isNotBlank())
            assertTrue(status.recommendationEn().isNotBlank())
            assertTrue(status.colorHex() > 0)
        }
    }
}
