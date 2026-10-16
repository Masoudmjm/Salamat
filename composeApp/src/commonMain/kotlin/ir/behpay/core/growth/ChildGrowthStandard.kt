package ir.behpay.core.growth

import ir.behpay.core.model.Gender
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ChildGrowthStatus {
    UNDERWEIGHT_FOR_HEIGHT,
    APPROPRIATE,
    OVERWEIGHT_FOR_HEIGHT;

    fun titleFa(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "کم‌وزن نسبت به قد (زیر منحنی کارت رشد)"
        APPROPRIATE -> "تناسب قد و وزن مطلوب و مناسب"
        OVERWEIGHT_FOR_HEIGHT -> "وزن بالاتر از منحنی نسبت به قد"
    }

    fun titleEn(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "Underweight for Height (Below Growth Curve)"
        APPROPRIATE -> "Appropriate Weight-for-Height (Normal Growth)"
        OVERWEIGHT_FOR_HEIGHT -> "Above Standard Weight-for-Height"
    }

    fun shortLabelFa(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "کمبود وزن نسبت به قد"
        APPROPRIATE -> "تناسب قد و وزن مناسب"
        OVERWEIGHT_FOR_HEIGHT -> "بالاتر از منحنی قد"
    }

    fun shortLabelEn(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "Underweight for Height"
        APPROPRIATE -> "Appropriate Proportion"
        OVERWEIGHT_FOR_HEIGHT -> "Above Standard"
    }

    fun colorHex(): Long = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> 0xFF0288D1 // Light Blue
        APPROPRIATE -> 0xFF2E7D32            // Green (Green zone of vaccination card)
        OVERWEIGHT_FOR_HEIGHT -> 0xFFED6C02  // Orange
    }

    fun recommendationFa(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "نسبت وزن به قد کودک کمتر از محدوده استاندارد کارت پایش رشد است. بررسی الگوی تغذیه، شیردهی و مشورت با مراقب سلامت یا پزشک کودکان در مرکز بهداشت توصیه می‌شود."
        APPROPRIATE -> "نسبت قد و وزن کودک کاملاً متناسب و در محدوده سبز کارت پایش رشد وزارت بهداشت قرار دارد. تداوم تغذیه سالم، خواب منظم و مراجعات دوره‌ای واکسیناسیون توصیه می‌شود."
        OVERWEIGHT_FOR_HEIGHT -> "وزن کودک نسبت به قد او بالاتر از صدک استاندارد کارت رشد است. پایش مستمر در نوبت‌های بعدی واکسیناسیون و مشورت با پزشک کودکان جهت تنظیم برنامه تغذیه پیشنهاد می‌شود."
    }

    fun recommendationEn(): String = when (this) {
        UNDERWEIGHT_FOR_HEIGHT -> "Weight-for-height is below the standard growth curve of the child health card. Nutritional review and pediatrician consultation are recommended."
        APPROPRIATE -> "Weight and height ratio is within the optimal green zone of the official child growth monitoring chart. Continue standard feeding and routine checkups."
        OVERWEIGHT_FOR_HEIGHT -> "Weight is above the standard percentile curve for this height. Regular monitoring at vaccination visits and pediatrician consultation are recommended."
    }
}

data class ChildGrowthEvaluation(
    val status: ChildGrowthStatus,
    val percentileEstimate: Int,
    val idealWeightRange: Pair<Double, Double>,
    val medianWeightKg: Double
)

/**
 * Child growth monitoring standards based on the official Iranian Vaccination & Child Health Card
 * (کارت پایش رشد و واکسیناسیون کودک - وزارت بهداشت، درمان و آموزش پزشکی)
 * adhering to WHO Child Growth Standards for weight-for-length/height (birth to 6 years).
 */
object ChildGrowthStandard {

    private data class GrowthAnchor(
        val heightCm: Double,
        val p3: Double,
        val p50: Double,
        val p97: Double
    )

    private val BOYS_TABLE: List<GrowthAnchor> = listOf(
        GrowthAnchor(45.0, 2.0, 2.4, 3.0),
        GrowthAnchor(50.0, 2.9, 3.4, 4.0),
        GrowthAnchor(55.0, 4.0, 4.6, 5.4),
        GrowthAnchor(60.0, 5.2, 5.9, 6.8),
        GrowthAnchor(65.0, 6.4, 7.2, 8.2),
        GrowthAnchor(70.0, 7.5, 8.4, 9.6),
        GrowthAnchor(75.0, 8.5, 9.5, 10.8),
        GrowthAnchor(80.0, 9.5, 10.6, 12.1),
        GrowthAnchor(85.0, 10.5, 11.8, 13.5),
        GrowthAnchor(90.0, 11.6, 13.0, 14.9),
        GrowthAnchor(95.0, 12.7, 14.3, 16.5),
        GrowthAnchor(100.0, 13.9, 15.7, 18.2),
        GrowthAnchor(105.0, 15.1, 17.2, 20.0),
        GrowthAnchor(110.0, 16.4, 18.8, 22.0),
        GrowthAnchor(115.0, 17.8, 20.5, 24.2),
        GrowthAnchor(120.0, 19.3, 22.4, 26.6)
    )

    private val GIRLS_TABLE: List<GrowthAnchor> = listOf(
        GrowthAnchor(45.0, 2.0, 2.4, 2.9),
        GrowthAnchor(50.0, 2.8, 3.3, 3.8),
        GrowthAnchor(55.0, 3.8, 4.4, 5.1),
        GrowthAnchor(60.0, 4.9, 5.6, 6.4),
        GrowthAnchor(65.0, 6.0, 6.8, 7.8),
        GrowthAnchor(70.0, 7.1, 8.0, 9.2),
        GrowthAnchor(75.0, 8.1, 9.1, 10.4),
        GrowthAnchor(80.0, 9.1, 10.2, 11.7),
        GrowthAnchor(85.0, 10.1, 11.4, 13.1),
        GrowthAnchor(90.0, 11.2, 12.6, 14.6),
        GrowthAnchor(95.0, 12.3, 14.0, 16.2),
        GrowthAnchor(100.0, 13.5, 15.4, 18.0),
        GrowthAnchor(105.0, 14.8, 17.0, 19.9),
        GrowthAnchor(110.0, 16.2, 18.7, 22.0),
        GrowthAnchor(115.0, 17.7, 20.5, 24.4),
        GrowthAnchor(120.0, 19.3, 22.5, 26.9)
    )

    private fun interpolatePercentiles(heightCm: Double, gender: Gender): Triple<Double, Double, Double> {
        val table = if (gender == Gender.FEMALE) GIRLS_TABLE else BOYS_TABLE

        if (heightCm <= table.first().heightCm) {
            val first = table.first()
            val ratio = max(0.5, heightCm / first.heightCm)
            return Triple(first.p3 * ratio, first.p50 * ratio, first.p97 * ratio)
        }

        if (heightCm >= table.last().heightCm) {
            val last = table.last()
            val ratio = min(1.5, heightCm / last.heightCm)
            return Triple(last.p3 * ratio, last.p50 * ratio, last.p97 * ratio)
        }

        for (i in 0 until table.size - 1) {
            val a = table[i]
            val b = table[i + 1]
            if (heightCm in a.heightCm..b.heightCm) {
                val t = (heightCm - a.heightCm) / (b.heightCm - a.heightCm)
                val p3 = a.p3 + t * (b.p3 - a.p3)
                val p50 = a.p50 + t * (b.p50 - a.p50)
                val p97 = a.p97 + t * (b.p97 - a.p97)
                return Triple(p3, p50, p97)
            }
        }

        val last = table.last()
        return Triple(last.p3, last.p50, last.p97)
    }

    /**
     * Evaluates child weight-for-height proportionality according to the official growth card curves.
     */
    fun evaluateWeightForHeight(
        weightKg: Double,
        heightCm: Double,
        gender: Gender
    ): ChildGrowthEvaluation {
        if (weightKg <= 0.0 || heightCm <= 0.0) {
            return ChildGrowthEvaluation(
                status = ChildGrowthStatus.APPROPRIATE,
                percentileEstimate = 50,
                idealWeightRange = Pair(0.0, 0.0),
                medianWeightKg = 0.0
            )
        }

        val (p3, p50, p97) = interpolatePercentiles(heightCm, gender)

        val status = when {
            weightKg < p3 -> ChildGrowthStatus.UNDERWEIGHT_FOR_HEIGHT
            weightKg <= p97 -> ChildGrowthStatus.APPROPRIATE
            else -> ChildGrowthStatus.OVERWEIGHT_FOR_HEIGHT
        }

        val percentile = when {
            weightKg <= p3 -> max(1, ((weightKg / p3) * 3.0).roundToInt())
            weightKg <= p50 -> (3.0 + ((weightKg - p3) / (p50 - p3)) * 47.0).roundToInt()
            weightKg <= p97 -> (50.0 + ((weightKg - p50) / (p97 - p50)) * 47.0).roundToInt()
            else -> min(99, (97.0 + ((weightKg - p97) / (p97 * 0.15)) * 2.0).roundToInt())
        }

        val minKg = (p3 * 10.0).roundToInt() / 10.0
        val maxKg = (p97 * 10.0).roundToInt() / 10.0
        val medianKg = (p50 * 10.0).roundToInt() / 10.0

        return ChildGrowthEvaluation(
            status = status,
            percentileEstimate = percentile,
            idealWeightRange = Pair(minKg, maxKg),
            medianWeightKg = medianKg
        )
    }

    /**
     * Healthy weight range (3rd to 97th percentile of the growth monitoring chart) for a child of given height.
     */
    fun getHealthyWeightRange(heightCm: Double, gender: Gender): Pair<Double, Double> {
        if (heightCm <= 0.0) return Pair(0.0, 0.0)
        val (p3, _, p97) = interpolatePercentiles(heightCm, gender)
        val minKg = (p3 * 10.0).roundToInt() / 10.0
        val maxKg = (p97 * 10.0).roundToInt() / 10.0
        return Pair(minKg, maxKg)
    }

    /**
     * Ideal median weight (50th percentile) on the growth chart.
     */
    fun getMedianWeight(heightCm: Double, gender: Gender): Double {
        if (heightCm <= 0.0) return 0.0
        val (_, p50, _) = interpolatePercentiles(heightCm, gender)
        return (p50 * 10.0).roundToInt() / 10.0
    }
}
