package ir.salamat.core.datetime

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class JalaliDateTest {

    @Test
    fun testNowruz1403Conversion() {
        val gregorian = LocalDate(2024, 3, 20)
        val jalali = gregorian.toJalali()
        assertEquals(1403, jalali.year)
        assertEquals(1, jalali.month)
        assertEquals(1, jalali.day)

        val backToGregorian = jalali.toLocalDate()
        assertEquals(gregorian, backToGregorian)
    }

    @Test
    fun testSummerDateConversion() {
        val gregorian = LocalDate(2024, 9, 11)
        val jalali = gregorian.toJalali()
        assertEquals(1403, jalali.year)
        assertEquals(6, jalali.month)
        assertEquals(21, jalali.day)

        val backToGregorian = jalali.toLocalDate()
        assertEquals(gregorian, backToGregorian)
    }

    @Test
    fun testFormattingAndPersianDigits() {
        val jalali = JalaliDate(1403, 6, 21)
        assertEquals("1403/06/21", jalali.formatNumeric(persianDigits = false))
        assertEquals("۱۴۰۳/۰۶/۲۱", jalali.formatNumeric(persianDigits = true))
        assertEquals("۲۱ شهریور ۱۴۰۳", jalali.formatPersian())
        assertEquals("21 Shahrivar 1403", jalali.formatEnglish())
    }

    @Test
    fun testDigitConversionHelpers() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", "1234567890".toPersianDigits())
        assertEquals("1234567890", "۱۲۳۴۵۶۷۸۹۰".toWesternDigits())
    }
}
