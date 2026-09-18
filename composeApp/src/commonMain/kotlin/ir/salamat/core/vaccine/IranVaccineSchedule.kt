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
    val descriptionEn: String,
    val routeFa: String = "",
    val routeEn: String = "",
    val sideEffectsFa: String = "",
    val sideEffectsEn: String = ""
)

object IranVaccineSchedule {

    val ALL_VACCINES: List<VaccineDefinition> = listOf(
        // At Birth (0 months)
        VaccineDefinition(
            code = "BCG",
            nameFa = "ب ث ژ (سل)",
            nameEn = "BCG (Tuberculosis)",
            targetAgeMonths = 0,
            descriptionFa = "واکسن ب ث ژ نوزاد را در برابر انواع شدید و کشنده بیماری سل به‌ویژه مننژیت سلی محافظت می‌کند. تزریق این واکسن در بدو تولد و قبل از ترخیص از بیمارستان ضروری است.",
            descriptionEn = "Protects against severe complications of Tuberculosis, including tuberculous meningitis. Administered at birth before hospital discharge.",
            routeFa = "تزریق داخل جلدی (بازوی چپ)",
            routeEn = "Intradermal injection (Upper left arm)",
            sideEffectsFa = "ایجاد جوش قرمز و برآمدگی پوستی پس از چند هفته کاملاً طبیعی بوده و نشانه ایجاد ایمنی است.",
            sideEffectsEn = "A small red papule and mild ulceration healing into a scar is normal and indicates immunity."
        ),
        VaccineDefinition(
            code = "HEPB_0",
            nameFa = "هپاتیت ب (نوبت اول)",
            nameEn = "Hepatitis B (Dose 1)",
            targetAgeMonths = 0,
            descriptionFa = "واکسن هپاتیت B نوزاد را در برابر عفونت خطرناک ویروسی کبد، هپاتیت مزمن و نارسایی کبد محافظت می‌کند. تزریق در ۲۴ ساعت اول زندگی بیشترین اثربخشی را دارد.",
            descriptionEn = "Protects against Hepatitis B virus liver infection and chronic liver damage. Crucial within the first 24 hours of life.",
            routeFa = "تزریق عضلانی (ران)",
            routeEn = "Intramuscular injection (Anterolateral thigh)",
            sideEffectsFa = "درد و قرمزی خفیف در محل تزریق و تب خفیف گذرا.",
            sideEffectsEn = "Mild tenderness or redness at the injection site; occasional low-grade fever."
        ),
        VaccineDefinition(
            code = "OPV_0",
            nameFa = "قطره فلج اطفال (نوبت بدو تولد)",
            nameEn = "Oral Polio - OPV (Birth)",
            targetAgeMonths = 0,
            descriptionFa = "قطره خوراکی پولیو (OPV) جهت ایجاد ایمنی اولیه مخاطی در روده و پیشگیری از فلج غیرقابل بازگشت اندام‌ها.",
            descriptionEn = "Live attenuated oral polio drops establishing early intestinal mucosal immunity against poliovirus.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "عارضه خاصی ندارد. در صورت استفراغ تا ۱۰ دقیقه بعد از مصرف، نوبت باید تکرار شود.",
            sideEffectsEn = "No notable side effects. Re-administer if infant regurgitates within 10 minutes."
        ),

        // 2 Months
        VaccineDefinition(
            code = "PENTA_1",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۱)",
            nameEn = "Pentavalent (Dose 1)",
            targetAgeMonths = 2,
            descriptionFa = "واکسن ترکیبی ۵ در ۱ شامل دیفتری، کزاز، سیاه‌سرفه، هپاتیت ب و هموفیلوس آنفلوانزا نوع b (عامل شایع مننژیت و ذات‌الریه باکتریایی در کودکان).",
            descriptionEn = "5-in-1 combination vaccine protecting against Diphtheria, Tetanus, Pertussis, Hepatitis B, and Hib pneumonia/meningitis.",
            routeFa = "تزریق عضلانی (ران چپ)",
            routeEn = "Intramuscular injection (Left thigh)",
            sideEffectsFa = "تب، درد، تورم موضعی و بی‌قراری تا ۴۸ ساعت. استفاده از استامینوفن طبق دستور پزشک و کمپرس سرد در ۲۴ ساعت اول توصیه می‌شود.",
            sideEffectsEn = "Mild to moderate fever and fussiness for 24-48 hours. Pediatric acetaminophen and cool compress recommended."
        ),
        VaccineDefinition(
            code = "OPV_1",
            nameFa = "قطره فلج اطفال (نوبت ۱)",
            nameEn = "Oral Polio - OPV (Dose 1)",
            targetAgeMonths = 2,
            descriptionFa = "نوبت اول دوره شیرخوارگی قطره خوراکی فلج اطفال در ۲ ماهگی برای تقویت ایمنی گوارشی علیه ویروس فلج اطفال.",
            descriptionEn = "First scheduled infant oral polio drops dose reinforcing mucosal protection.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "معمولاً بدون هرگونه عارضه جانبی.",
            sideEffectsEn = "Generally without any adverse side effects."
        ),

        // 4 Months
        VaccineDefinition(
            code = "PENTA_2",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۲)",
            nameEn = "Pentavalent (Dose 2)",
            targetAgeMonths = 4,
            descriptionFa = "نوبت دوم واکسن پنج‌گانه در ۴ ماهگی برای تثبیت پادتن‌های ایمنی در برابر دیفتری، کزاز، سیاه‌سرفه، هپاتیت ب و هموفیلوس.",
            descriptionEn = "Second Pentavalent dose consolidating antibody levels against the 5 childhood diseases.",
            routeFa = "تزریق عضلانی (ران چپ)",
            routeEn = "Intramuscular injection (Left thigh)",
            sideEffectsFa = "تب و درد خفیف در محل تزریق که ظرف ۲۴ تا ۴۸ ساعت برطرف می‌شود.",
            sideEffectsEn = "Low-grade fever and local tenderness subsiding within 24-48 hours."
        ),
        VaccineDefinition(
            code = "OPV_2",
            nameFa = "قطره فلج اطفال (نوبت ۲)",
            nameEn = "Oral Polio - OPV (Dose 2)",
            targetAgeMonths = 4,
            descriptionFa = "دوز دوم شیرخوارگی قطره خوراکی ضد فلج اطفال همزمان با واکسن‌های تزریقی ۴ ماهگی.",
            descriptionEn = "Second regular infantile dose of oral polio drops administered at 4 months.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "بدون عارضه جانبی.",
            sideEffectsEn = "No adverse effects."
        ),
        VaccineDefinition(
            code = "IPV_1",
            nameFa = "فلج اطفال تزریقی (IPV نوبت ۱)",
            nameEn = "Inactivated Polio - IPV (Dose 1)",
            targetAgeMonths = 4,
            descriptionFa = "واکسن تزریقی غیرفعال و کشته‌شده فلج اطفال (IPV). این واکسن پادتن‌های در گردش خون را در کنار قطره خوراکی به حداکثر می‌رساند.",
            descriptionEn = "Injectable inactivated polio vaccine (IPV) maximizing systemic circulating blood immunity alongside oral drops.",
            routeFa = "تزریق عضلانی (ران راست)",
            routeEn = "Intramuscular injection (Right thigh)",
            sideEffectsFa = "قرمزی یا حساسیت مختصر در موضع تزریق.",
            sideEffectsEn = "Mild local redness or tenderness at injection site."
        ),

        // 6 Months
        VaccineDefinition(
            code = "PENTA_3",
            nameFa = "واکسن پنج‌گانه (پنتاوالان نوبت ۳)",
            nameEn = "Pentavalent (Dose 3)",
            targetAgeMonths = 6,
            descriptionFa = "سومین و آخرین نوبت واکسن پنج‌گانه مرحله شیرخوارگی در ۶ ماهگی، که ایمنی پایه کودک را تا ۱۸ ماهگی کامل می‌نماید.",
            descriptionEn = "Third dose completing primary infant immunization series against the 5 diseases until 18 months.",
            routeFa = "تزریق عضلانی (ران چپ)",
            routeEn = "Intramuscular injection (Left thigh)",
            sideEffectsFa = "تب و بی‌قراری گذرا که با مراقبت و قطره استامینوفن تسکین می‌یابد.",
            sideEffectsEn = "Mild fever and irritability manageable with standard pediatric care."
        ),
        VaccineDefinition(
            code = "OPV_3",
            nameFa = "قطره فلج اطفال (نوبت ۳)",
            nameEn = "Oral Polio - OPV (Dose 3)",
            targetAgeMonths = 6,
            descriptionFa = "نوبت سوم قطره خوراکی فلج اطفال در ۶ ماهگی جهت تکمیل چرخه مصونیت در دوره شیرخوارگی.",
            descriptionEn = "Third oral polio dose at 6 months finalizing primary infantile gut defense.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "بدون عارضه.",
            sideEffectsEn = "No adverse effects."
        ),

        // 12 Months
        VaccineDefinition(
            code = "MMR_1",
            nameFa = "سرخک، سرخجه، اوریون (MMR نوبت ۱)",
            nameEn = "MMR (Dose 1)",
            targetAgeMonths = 12,
            descriptionFa = "واکسن ترکیبی زنده ضعیف‌شده در یک‌سالگی جهت پیشگیری قطعی از سه بیماری مسری سرخک، سرخجه و اوریون و عوارض خطرناک ریوی و مغزی آنها.",
            descriptionEn = "Live attenuated vaccine protecting against Measles, Mumps, and Rubella and their serious complications at 1 year.",
            routeFa = "تزریق زیرجلدی (بازو)",
            routeEn = "Subcutaneous injection (Upper arm)",
            sideEffectsFa = "تب خفیف یا دانه‌های پوستی ریز ممکن است با تأخیر ۵ تا ۱۲ روز بعد از تزریق بروز کند که طبیعی و خودمحدودشونده است.",
            sideEffectsEn = "Delayed mild fever or transient rash may occur 5 to 12 days after injection and resolves spontaneously."
        ),

        // 18 Months
        VaccineDefinition(
            code = "MMR_2",
            nameFa = "سرخک، سرخجه، اوریون (MMR نوبت ۲)",
            nameEn = "MMR (Dose 2)",
            targetAgeMonths = 18,
            descriptionFa = "دوز یادآور واکسن MMR در ۱۸ ماهگی جهت تضمین ایمنی طولانی‌مدت (بیش از ۹۷٪ مصونیت پایدار مادام‌العمر).",
            descriptionEn = "Booster dose of MMR at 18 months providing over 97% long-lasting lifetime immunity.",
            routeFa = "تزریق زیرجلدی (بازو)",
            routeEn = "Subcutaneous injection (Upper arm)",
            sideEffectsFa = "به ندرت ممکن است تب خفیفی چند روز پس از تزریق ایجاد شود.",
            sideEffectsEn = "Occasional low fever a few days after vaccination."
        ),
        VaccineDefinition(
            code = "DTP_1",
            nameFa = "ثلاث (دیفتری، کزاز، سیاه‌سرفه - یادآور ۱)",
            nameEn = "DTP Booster (Dose 1)",
            targetAgeMonths = 18,
            descriptionFa = "اولین نوبت یادآور واکسن سه‌گانه ثلاث در ۱۸ ماهگی برای تجدید پادتن‌های علیه سم دیفتری، کزاز و باکتری سیاه‌سرفه.",
            descriptionEn = "First DTP booster at 18 months elevating protective antibodies against diphtheria, tetanus, and pertussis.",
            routeFa = "تزریق عضلانی (ران یا بازو)",
            routeEn = "Intramuscular injection (Thigh or arm)",
            sideEffectsFa = "تب، تورم و درد موضعی که با استامینوفن و کمپرس کنترل می‌شود.",
            sideEffectsEn = "Fever and local soreness managed with paracetamol and compresses."
        ),
        VaccineDefinition(
            code = "OPV_4",
            nameFa = "قطره فلج اطفال (یادآور ۱)",
            nameEn = "Oral Polio - OPV (Booster 1)",
            targetAgeMonths = 18,
            descriptionFa = "دوز یادآور اول قطره فلج اطفال در ۱۸ ماهگی جهت تجدید مصونیت روده و جامعه در سن نوپایی کودک.",
            descriptionEn = "First oral polio booster dose at 18 months sustaining strong intestinal barrier protection.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "بدون عارضه خاص.",
            sideEffectsEn = "No notable side effects."
        ),

        // 6 Years (Pre-school)
        VaccineDefinition(
            code = "DTP_2",
            nameFa = "ثلاث (یادآور دوم قبل از مدرسه)",
            nameEn = "DTP Booster (Dose 2)",
            targetAgeMonths = 72,
            descriptionFa = "دوز یادآور دوم واکسن سه‌گانه ثلاث در ۶ سالگی قبل از ورود به دبستان جهت ایمنی دانش‌آموز در محیط‌های جمعی مدرسه.",
            descriptionEn = "Second DTP booster at 6 years before elementary school entry, ensuring solid protection in classrooms.",
            routeFa = "تزریق عضلانی (عضله بازو)",
            routeEn = "Intramuscular injection (Deltoid arm muscle)",
            sideEffectsFa = "درد و کوفتگی در دست تزریق‌شده و گاهی تب خفیف.",
            sideEffectsEn = "Localized arm soreness and occasional low-grade fever."
        ),
        VaccineDefinition(
            code = "OPV_5",
            nameFa = "قطره فلج اطفال (یادآور ۲)",
            nameEn = "Oral Polio - OPV (Booster 2)",
            targetAgeMonths = 72,
            descriptionFa = "آخرین دوز یادآور قطره فلج اطفال در ۶ سالگی قبل از ورود به مدرسه برای حفظ ریشه‌کنی دائمی فلج اطفال.",
            descriptionEn = "Final routine oral polio booster drops at 6 years before primary school entry.",
            routeFa = "قطره خوراکی (۲ قطره)",
            routeEn = "Oral drops (2 drops)",
            sideEffectsFa = "بدون هرگونه عارضه.",
            sideEffectsEn = "No side effects."
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
