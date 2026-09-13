package ir.salamat.core.datetime

import kotlinx.datetime.LocalDate

/**
 * Pure Kotlin representation of a Solar Hijri (Jalali) date.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<JalaliDate> {

    init {
        require(month in 1..12) { "Month must be between 1 and 12, was $month" }
        require(day in 1..31) { "Day must be between 1 and 31, was $day" }
    }

    override operator fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    /**
     * Formats as YYYY/MM/DD with 2-digit padding.
     */
    fun formatNumeric(persianDigits: Boolean = false): String {
        val y = year.toString()
        val m = month.toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        val text = "$y/$m/$d"
        return if (persianDigits) text.toPersianDigits() else text
    }

    /**
     * Formats with Persian month name, e.g. "۲۱ شهریور ۱۴۰۳"
     */
    fun formatPersian(): String {
        val monthName = PERSIAN_MONTHS.getOrElse(month - 1) { "" }
        return "${day.toString().toPersianDigits()} $monthName ${year.toString().toPersianDigits()}"
    }

    /**
     * Formats with English month name, e.g. "21 Shahrivar 1403"
     */
    fun formatEnglish(): String {
        val monthName = ENGLISH_JALALI_MONTHS.getOrElse(month - 1) { "" }
        return "$day $monthName $year"
    }

    companion object {
        val PERSIAN_MONTHS = listOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        val ENGLISH_JALALI_MONTHS = listOf(
            "Farvardin", "Ordibehesht", "Khordad",
            "Tir", "Mordad", "Shahrivar",
            "Mehr", "Aban", "Azar",
            "Dey", "Bahman", "Esfand"
        )
    }
}

/**
 * Converts Western digits to Persian digits.
 */
fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder()
    for (char in this) {
        if (char in '0'..'9') {
            builder.append(persianDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

/**
 * Converts Persian digits to Western digits.
 */
fun String.toWesternDigits(): String {
    val builder = StringBuilder()
    for (char in this) {
        when (char) {
            in '۰'..'۹' -> builder.append(char - '۰')
            in '٠'..'٩' -> builder.append(char - '٠') // Arabic-indic
            else -> builder.append(char)
        }
    }
    return builder.toString()
}

/**
 * Converts a Gregorian LocalDate to JalaliDate using Julian Day Number algorithm.
 */
fun LocalDate.toJalali(): JalaliDate {
    val gy = this.year
    val gm = this.monthNumber
    val gd = this.dayOfMonth

    val gDNo = if (gm > 2) {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val leap = if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 1 else 0
        gDaysInMonth[gm - 1] + gd + leap
    } else {
        if (gm == 1) gd else gd + 31
    }

    val jy: Int
    val jm: Int
    val jd: Int

    var gy2 = gy - 1600
    var gm2 = gm - 1
    var gd2 = gd - 1

    var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

    val gDays = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    for (i in 0 until gm2) {
        gDayNo += gDays[i + 1]
    }
    if (gm2 > 1 && ((gy2 % 4 == 0 && gy2 % 100 != 0) || (gy2 % 400 == 0))) {
        gDayNo++
    }
    gDayNo += gd2

    var jDayNo = gDayNo - 79

    val jNp = jDayNo / 12053
    jDayNo %= 12053

    var jyCalculated = 979 + 33 * jNp + 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jyCalculated += (jDayNo - 1) / 365
        jDayNo = (jDayNo - 1) % 365
    }

    val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    var jmCalculated = 0
    while (jmCalculated < 11 && jDayNo >= jDaysInMonth[jmCalculated]) {
        jDayNo -= jDaysInMonth[jmCalculated]
        jmCalculated++
    }

    jy = jyCalculated
    jm = jmCalculated + 1
    jd = jDayNo + 1

    return JalaliDate(jy, jm, jd)
}

/**
 * Converts a JalaliDate back to a Gregorian LocalDate.
 */
fun JalaliDate.toLocalDate(): LocalDate {
    val jy = this.year - 979
    val jm = this.month - 1
    val jd = this.day - 1

    var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
    val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    for (i in 0 until jm) {
        jDayNo += jDaysInMonth[i]
    }
    jDayNo += jd

    var gDayNo = jDayNo + 79

    var gy = 1600 + 400 * (gDayNo / 146097)
    gDayNo %= 146097

    var leap = true
    if (gDayNo >= 36525) {
        gDayNo--
        gy += 100 * (gDayNo / 36524)
        gDayNo %= 36524

        if (gDayNo >= 365) {
            gDayNo++
        } else {
            leap = false
        }
    }

    gy += 4 * (gDayNo / 1461)
    gDayNo %= 1461

    if (gDayNo >= 366) {
        leap = false
        gDayNo--
        gy += gDayNo / 365
        gDayNo %= 365
    }

    val gDays = intArrayOf(
        0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    )
    var gm = 0
    while (gm < 12 && gDayNo >= gDays[gm + 1]) {
        gDayNo -= gDays[gm + 1]
        gm++
    }

    val gd = gDayNo + 1
    return LocalDate(gy, gm + 1, gd)
}
