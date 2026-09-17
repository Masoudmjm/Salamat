package ir.salamat.ui.screens.growth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrowthEvaluationTest {

    @Test
    fun testBmiCalculationAccurate() {
        // Height: 180cm, Weight: 75kg -> BMI = 75 / (1.8^2) = 75 / 3.24 = 23.148 -> 23.1
        val bmi = BmiCalculator.calculateBmi(75.0, 180.0)
        assertEquals(23.1, bmi)

        // Height: 165cm, Weight: 62kg -> BMI = 62 / (1.65^2) = 62 / 2.7225 = 22.77 -> 22.8
        val bmi2 = BmiCalculator.calculateBmi(62.0, 165.0)
        assertEquals(22.8, bmi2)
    }

    @Test
    fun testBmiCalculationEdgeZeroOrNegative() {
        assertEquals(0.0, BmiCalculator.calculateBmi(0.0, 170.0))
        assertEquals(0.0, BmiCalculator.calculateBmi(70.0, 0.0))
        assertEquals(0.0, BmiCalculator.calculateBmi(-10.0, 170.0))
        assertEquals(0.0, BmiCalculator.calculateBmi(70.0, -170.0))
    }

    @Test
    fun testBmiCategoryBoundaries() {
        // Underweight: < 18.5
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.getBmiCategory(16.0))
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.getBmiCategory(18.4))

        // Normal: 18.5 <= BMI < 25.0
        assertEquals(BmiCategory.NORMAL, BmiCalculator.getBmiCategory(18.5))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.getBmiCategory(22.0))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.getBmiCategory(24.9))

        // Overweight: 25.0 <= BMI < 30.0
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.getBmiCategory(25.0))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.getBmiCategory(27.5))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.getBmiCategory(29.9))

        // Obese: >= 30.0
        assertEquals(BmiCategory.OBESE, BmiCalculator.getBmiCategory(30.0))
        assertEquals(BmiCategory.OBESE, BmiCalculator.getBmiCategory(36.2))
    }

    @Test
    fun testIdealWeightRangeCalculation() {
        // Height: 170cm (1.7m, h^2 = 2.89)
        // Min: 18.5 * 2.89 = 53.465 -> 53.5 kg
        // Max: 24.9 * 2.89 = 71.961 -> 72.0 kg
        val (minKg, maxKg) = BmiCalculator.calculateIdealWeightRange(170.0)
        assertEquals(53.5, minKg)
        assertEquals(72.0, maxKg)

        // Invalid height
        val (zeroMin, zeroMax) = BmiCalculator.calculateIdealWeightRange(0.0)
        assertEquals(0.0, zeroMin)
        assertEquals(0.0, zeroMax)
    }

    @Test
    fun testCategoryLocalizationAndColors() {
        for (category in BmiCategory.entries) {
            assertTrue(category.titleFa().isNotBlank())
            assertTrue(category.titleEn().isNotBlank())
            assertTrue(category.recommendationFa().isNotBlank())
            assertTrue(category.recommendationEn().isNotBlank())
            assertTrue(category.colorHex() > 0)
        }
    }
}
