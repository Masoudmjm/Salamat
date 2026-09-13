package ir.salamat.core.vaccine

import ir.salamat.core.model.VaccineStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class VaccineDefinition(
    val code: String,
    val nameFa: String,
    val nameEn: String,
    val targetAgeMonths: Int,
    val descriptionFa: String,
    val descriptionEn: String
)

object IranVaccineSchedule {

    val ALL_VACCINES: List<VaccineDefinition> = listOf(
        // At Birth (0 months)
        VaccineDefinition(
            code = "BCG",
            nameFa = "ب ث ژ (سل)",
            nameEn = "BCG (Tuberculosis)",
            targetAgeMonths = 0,
            descriptionFa = "واکسن حفاظت در برابر بیماری سل در بدو تولد تزریق می‌شود.",
            descriptionEn = "Protection against Tuberculosis given at birth."
        ),
        VaccineDefinition(
            code = "HEPB_0",
            nameFa = "هپاتیت ب (نوبت اول)",
            nameEn = "Hepatitis B (Dose 1)",
            targetAgeMonths = 0,
            descriptionFa = "واکسن پیشگیری از عفونت کبد هپاتیت ب در بدو تولد.",
            descriptionEn = "First dose of Hepatitis B prevention at birth."
        ),
        VaccineDefinition(
            code = "OPV_0",
            nameFa = "قطره فلج اطفال (نوبت بدو تولد)",
            nameEn = "Oral Polio - OPV (Birth)",
            targetAgeMonths = 0,
            descriptionFa = "قطره خوراکی جهت ایجاد ایمنی اولیه در برابر فلج اطفال.",
            descriptionEn = "Oral polio vaccine drops given at birth."
        ),

        // 2 Months
        VaccineDefinition(
            code = "PENTA_1",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۱)",
            nameEn = "Pentavalent (Dose 1)",
            targetAgeMonths = 2,
            descriptionFa = "شامل دیفتری، کزاز، سیاه‌سرفه، هپاتیت ب و هموفیلوس آنفلوانزا ب.",
            descriptionEn = "Combines DTP, Hepatitis B, and Hib."
        ),
        VaccineDefinition(
            code = "OPV_1",
            nameFa = "قطره فلج اطفال (نوبت ۱)",
            nameEn = "Oral Polio - OPV (Dose 1)",
            targetAgeMonths = 2,
            descriptionFa = "نوبت دوم قطره خوراکی فلج اطفال در ۲ ماهگی.",
            descriptionEn = "Second oral polio dose at 2 months."
        ),

        // 4 Months
        VaccineDefinition(
            code = "PENTA_2",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۲)",
            nameEn = "Pentavalent (Dose 2)",
            targetAgeMonths = 4,
            descriptionFa = "دوز دوم پنج‌گانه برای تقویت ایمنی کودک.",
            descriptionEn = "Second dose of Pentavalent."
        ),
        VaccineDefinition(
            code = "OPV_2",
            nameFa = "قطره فلج اطفال (نوبت ۲)",
            nameEn = "Oral Polio - OPV (Dose 2)",
            targetAgeMonths = 4,
            descriptionFa = "نوبت سوم قطره خوراکی فلج اطفال.",
            descriptionEn = "Third oral polio dose."
        ),
        VaccineDefinition(
            code = "IPV_1",
            nameFa = "فلج اطفال تزریقی (IPV نوبت ۱)",
            nameEn = "Inactivated Polio - IPV (Dose 1)",
            targetAgeMonths = 4,
            descriptionFa = "واکسن تزریقی غیرفعال فلج اطفال در ۴ ماهگی.",
            descriptionEn = "Injectable polio vaccine at 4 months."
        ),

        // 6 Months
        VaccineDefinition(
            code = "PENTA_3",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۳)",
            nameEn = "Pentavalent (Dose 3)",
            targetAgeMonths = 6,
            descriptionFa = "دوز سوم واکسن پنج‌گانه در پایان شش ماهگی.",
            descriptionEn = "Third dose of Pentavalent."
        ),
        VaccineDefinition(
            code = "OPV_3",
            nameFa = "قطره فلج اطفال (نوبت ۳)",
            nameEn = "Oral Polio - OPV (Dose 3)",
            targetAgeMonths = 6,
            descriptionFa = "نوبت چهارم قطره خوراکی فلج اطفال در ۶ ماهگی.",
            descriptionEn = "Oral polio dose at 6 months."
        ),

        // 12 Months
        VaccineDefinition(
            code = "MMR_1",
            nameFa = "سرخک، سرخجه، اوریون (MMR نوبت ۱)",
            nameEn = "MMR (Dose 1)",
            targetAgeMonths = 12,
            descriptionFa = "واکسن سه‌گانه سرخک، اوریون و سرخجه در یک سالگی.",
            descriptionEn = "Protection against Measles, Mumps, and Rubella at 1 year."
        ),

        // 18 Months
        VaccineDefinition(
            code = "MMR_2",
            nameFa = "سرخک، سرخجه، اوریون (MMR نوبت ۲)",
            nameEn = "MMR (Dose 2)",
            targetAgeMonths = 18,
            descriptionFa = "دوز یادآور سرخک، سرخجه و اوریون در ۱۸ ماهگی.",
            descriptionEn = "MMR booster dose at 18 months."
        ),
        VaccineDefinition(
            code = "DTP_1",
            nameFa = "ثلاث (دیفتری، کزاز، سیاه‌سرفه - یادآور ۱)",
            nameEn = "DTP Booster (Dose 1)",
            targetAgeMonths = 18,
            descriptionFa = "اولین دوز یادآور واکسن ثلاث در ۱۸ ماهگی.",
            descriptionEn = "First DTP booster at 18 months."
        ),
        VaccineDefinition(
            code = "OPV_4",
            nameFa = "قطره فلج اطفال (یادآور ۱)",
            nameEn = "Oral Polio - OPV (Booster 1)",
            targetAgeMonths = 18,
            descriptionFa = "دوز یادآور اول قطره فلج اطفال در ۱۸ ماهگی.",
            descriptionEn = "First oral polio booster at 18 months."
        ),

        // 6 Years (Pre-school)
        VaccineDefinition(
            code = "DTP_2",
            nameFa = "ثلاث (یادآور دوم قبل از مدرسه)",
            nameEn = "DTP Booster (Dose 2)",
            targetAgeMonths = 72,
            descriptionFa = "دوز یادآور دوم واکسن ثلاث قبل از ورود به مدرسه در ۶ سالگی.",
            descriptionEn = "Second DTP booster before entering school at 6 years."
        ),
        VaccineDefinition(
            code = "OPV_5",
            nameFa = "قطره فلج اطفال (یادآور ۲)",
            nameEn = "Oral Polio - OPV (Booster 2)",
            targetAgeMonths = 72,
            descriptionFa = "دوز یادآور دوم قطره فلج اطفال در ۶ سالگی.",
            descriptionEn = "Second oral polio booster at 6 years."
        )
    )

    fun getByCode(code: String): VaccineDefinition? {
        return ALL_VACCINES.firstOrNull { it.code == code }
    }

    /**
     * Calculates the estimated due date based on the child's birth date.
     */
    fun calculateDueDate(birthDate: LocalDate, targetAgeMonths: Int): LocalDate {
        return if (targetAgeMonths == 0) {
            birthDate
        } else {
            birthDate.plus(targetAgeMonths, DateTimeUnit.MONTH)
        }
    }

    /**
     * Determines status given dates.
     */
    fun computeStatus(
        targetAgeMonths: Int,
        birthDate: LocalDate,
        administeredDate: LocalDate?,
        currentDate: LocalDate
    ): VaccineStatus {
        if (administeredDate != null) return VaccineStatus.COMPLETED

        val dueDate = calculateDueDate(birthDate, targetAgeMonths)
        val overdueThreshold = dueDate.plus(30, DateTimeUnit.DAY)
        val dueSoonThreshold = dueDate.plus(-14, DateTimeUnit.DAY)

        return when {
            currentDate > overdueThreshold -> VaccineStatus.OVERDUE
            currentDate >= dueSoonThreshold -> VaccineStatus.DUE
            else -> VaccineStatus.UPCOMING
        }
    }
}
