package ir.salamat.core.checkup

import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import kotlinx.datetime.LocalDate

/**
 * Predefined catalog entry for standard preventive health screening.
 * Based on Iranian Ministry of Health guidelines (IraPEN & Primary Healthcare / مراقبت‌های ادغام‌یافته میانسالان و سالمندان).
 */
data class CheckupDefinition(
    val key: String,
    val nameFa: String,
    val nameEn: String,
    val descriptionFa: String,
    val descriptionEn: String,
    val defaultIntervalMonths: Int,
    val minAgeYears: Int = 18,
    val maxAgeYears: Int = 120,
    val eligibleGender: Gender? = null // null means both male and female
)

object CheckupCatalog {

    val ALL_CHECKUPS: List<CheckupDefinition> = listOf(
        CheckupDefinition(
            key = "blood_pressure",
            nameFa = "سنجش فشار خون",
            nameEn = "Blood Pressure Screening",
            descriptionFa = "غربالگری پرفشاری خون و ارزیابی سلامت قلب و عروق.",
            descriptionEn = "Screening for hypertension and cardiovascular risk.",
            defaultIntervalMonths = 6,
            minAgeYears = 18
        ),
        CheckupDefinition(
            key = "annual_blood_test",
            nameFa = "آزمایش قند ناشتا و چربی خون",
            nameEn = "Fasting Glucose & Lipid Panel",
            descriptionFa = "غربالگری دیابت نوع ۲، کلسترول و تری‌گلیسرید بر اساس بسته ایراپن (IraPEN).",
            descriptionEn = "Type 2 diabetes, cholesterol and triglyceride screening under IraPEN protocol.",
            defaultIntervalMonths = 12,
            minAgeYears = 30
        ),
        CheckupDefinition(
            key = "dental_exam",
            nameFa = "معاینه دندان‌پزشکی و سلامت دهان",
            nameEn = "Dental & Oral Health Exam",
            descriptionFa = "بررسی پوسیدگی دندان، سلامت لثه و جرم‌گیری سالانه.",
            descriptionEn = "Dental caries check, periodontal evaluation and annual prophylaxis.",
            defaultIntervalMonths = 12,
            minAgeYears = 18
        ),
        CheckupDefinition(
            key = "eye_exam",
            nameFa = "معاینه چشم و بینایی‌سنجی",
            nameEn = "Vision & Eye Checkup",
            descriptionFa = "سنجش نمره بینایی، فشار چشم و پیشگیری از آب‌سیاه (گلوکوم).",
            descriptionEn = "Visual acuity check, intraocular pressure and glaucoma screening.",
            defaultIntervalMonths = 24,
            minAgeYears = 18
        ),
        CheckupDefinition(
            key = "colorectal_screening",
            nameFa = "غربالگری سرطان روده بزرگ (تست فیت)",
            nameEn = "Colorectal Screening (FIT)",
            descriptionFa = "آزمایش ایمنوشیمیایی خون مخفی مدفوع (FIT) برای بزرگسالان سنین ۵۰ تا ۷۰ سال.",
            descriptionEn = "Fecal immunochemical test (FIT) for adults aged 50–70.",
            defaultIntervalMonths = 24,
            minAgeYears = 50,
            maxAgeYears = 70
        ),
        CheckupDefinition(
            key = "cervical_screening",
            nameFa = "غربالگری دهانه رحم (پاپ اسمیر / HPV)",
            nameEn = "Cervical Screening (Pap Smear)",
            descriptionFa = "پیشگیری و تشخیص زودهنگام تغییرات سلولی دهانه رحم برای بانوان سنین ۳۰ تا ۵۹ سال.",
            descriptionEn = "Cervical cytology and HPV screening for women aged 30–59.",
            defaultIntervalMonths = 36,
            minAgeYears = 30,
            maxAgeYears = 59,
            eligibleGender = Gender.FEMALE
        ),
        CheckupDefinition(
            key = "breast_screening",
            nameFa = "غربالگری سرطان پستان (ماموگرافی)",
            nameEn = "Breast Screening (Mammography)",
            descriptionFa = "ماموگرافی دوره‌ای و معاینه بالینی پستان برای بانوان بالای ۴۰ سال.",
            descriptionEn = "Biannual mammography and clinical breast examination for women aged 40+.",
            defaultIntervalMonths = 24,
            minAgeYears = 40,
            maxAgeYears = 69,
            eligibleGender = Gender.FEMALE
        ),
        CheckupDefinition(
            key = "bone_density",
            nameFa = "سنجش تراکم استخوان (DEXA)",
            nameEn = "Bone Mineral Density (DEXA)",
            descriptionFa = "ارزیابی پوکی استخوان و پیشگیری از شکستگی در دوران سالمندی.",
            descriptionEn = "Osteoporosis evaluation and fracture risk assessment for seniors.",
            defaultIntervalMonths = 24,
            minAgeYears = 60
        ),
        CheckupDefinition(
            key = "general_physician",
            nameFa = "معاینه عمومی و پزشک خانواده",
            nameEn = "General Physician Checkup",
            descriptionFa = "بررسی بالینی جامع سالانه، مرور داروها و پایش شاخص‌های عمومی سلامت.",
            descriptionEn = "Annual comprehensive wellness exam and medication review.",
            defaultIntervalMonths = 12,
            minAgeYears = 18
        )
    )

    fun findByKey(key: String): CheckupDefinition? = ALL_CHECKUPS.firstOrNull { it.key == key }

    fun calculateAgeYears(birthDate: LocalDate, currentDate: LocalDate): Int {
        val hadBirthdayThisYear = (currentDate.monthNumber > birthDate.monthNumber) ||
                (currentDate.monthNumber == birthDate.monthNumber && currentDate.dayOfMonth >= birthDate.dayOfMonth)
        val diff = currentDate.year - birthDate.year
        return if (hadBirthdayThisYear) diff else diff - 1
    }

    fun getEligibleCheckups(profile: Profile, currentDate: LocalDate): List<CheckupDefinition> {
        val age = calculateAgeYears(profile.birthDate, currentDate)
        return ALL_CHECKUPS.filter { item ->
            age >= item.minAgeYears &&
            age <= item.maxAgeYears &&
            (item.eligibleGender == null || item.eligibleGender == profile.gender)
        }
    }
}
