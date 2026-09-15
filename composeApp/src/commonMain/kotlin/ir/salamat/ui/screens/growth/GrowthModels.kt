package ir.salamat.ui.screens.growth

import ir.salamat.core.model.GrowthRecord
import ir.salamat.core.model.Profile
import kotlin.math.roundToInt

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE;

    fun titleFa(): String = when (this) {
        UNDERWEIGHT -> "کم‌وزن"
        NORMAL -> "وزن طبیعی و سلامت"
        OVERWEIGHT -> "دارای اضافه‌وزن"
        OBESE -> "چاقی"
    }

    fun titleEn(): String = when (this) {
        UNDERWEIGHT -> "Underweight"
        NORMAL -> "Normal Weight"
        OVERWEIGHT -> "Overweight"
        OBESE -> "Obese"
    }

    fun colorHex(): Long = when (this) {
        UNDERWEIGHT -> 0xFF0288D1 // Light Blue
        NORMAL -> 0xFF2E7D32      // Green
        OVERWEIGHT -> 0xFFED6C02  // Amber / Orange
        OBESE -> 0xFFD32F2F       // Red
    }

    fun recommendationFa(): String = when (this) {
        UNDERWEIGHT -> "شاخص توده بدنی پایین‌تر از حد استاندارد است. مشورت با کارشناس تغذیه توصیه می‌شود."
        NORMAL -> "شاخص توده بدنی در محدوده ایده‌آل و سلامت قرار دارد. سبک زندگی و تغذیه سالم را حفظ کنید."
        OVERWEIGHT -> "شاخص توده بدنی بالاتر از حد نرمال است. افزایش فعالیت بدنی و اصلاح رژیم غذایی پیشنهاد می‌شود."
        OBESE -> "شاخص توده بدنی در محدوده چاقی است. ارزیابی تخصصی توسط پزشک و برنامه کاهش وزن ضروری است."
    }

    fun recommendationEn(): String = when (this) {
        UNDERWEIGHT -> "BMI is below normal. A consultation with a nutritionist is recommended."
        NORMAL -> "BMI is within the ideal healthy range. Keep maintaining a balanced lifestyle."
        OVERWEIGHT -> "BMI indicates overweight. Increasing daily activity and balanced diet are advised."
        OBESE -> "BMI indicates obesity. Professional medical guidance and weight management are recommended."
    }
}

object BmiCalculator {

    /**
     * Calculates Body Mass Index: weight (kg) / (height (m) ^ 2)
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0 || weightKg <= 0) return 0.0
        val heightM = heightCm / 100.0
        val rawBmi = weightKg / (heightM * heightM)
        return (rawBmi * 10.0).roundToInt() / 10.0
    }

    /**
     * Determines standard adult BMI category.
     */
    fun getBmiCategory(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 25.0 -> BmiCategory.NORMAL
        bmi < 30.0 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    /**
     * Calculates healthy weight range (BMI 18.5 to 24.9) for a given height in cm.
     */
    fun calculateIdealWeightRange(heightCm: Double): Pair<Double, Double> {
        if (heightCm <= 0) return Pair(0.0, 0.0)
        val heightM = heightCm / 100.0
        val hSq = heightM * heightM
        val minKg = ((18.5 * hSq) * 10.0).roundToInt() / 10.0
        val maxKg = ((24.9 * hSq) * 10.0).roundToInt() / 10.0
        return Pair(minKg, maxKg)
    }
}

data class GrowthUiState(
    val profile: Profile? = null,
    val records: List<GrowthRecord> = emptyList(),
    val latestRecord: GrowthRecord? = null,
    val bmiCategory: BmiCategory? = null,
    val idealWeightRange: Pair<Double, Double>? = null,
    val isAddDialogOpen: Boolean = false,
    val isLoading: Boolean = true
)
