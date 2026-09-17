package ir.salamat.core.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class JalaliDateLeapYearTest {

    @Test
    fun testNowruzAcrossMultipleYears() {
        // 1399 (Leap year)
        val nowruz1399 = LocalDate(2020, 3, 20).toJalali()
        assertEquals(JalaliDate(1399, 1, 1), nowruz1399)
        assertEquals(LocalDate(2020, 3, 20), nowruz1399.toLocalDate())

        // 1400 (Standard year)
        val nowruz1400 = LocalDate(2021, 3, 21).toJalali()
        assertEquals(JalaliDate(1400, 1, 1), nowruz1400)
        assertEquals(LocalDate(2021, 3, 21), nowruz1400.toLocalDate())

        // 1401 (Standard year)
        val nowruz1401 = LocalDate(2022, 3, 21).toJalali()
        assertEquals(JalaliDate(1401, 1, 1), nowruz1401)
        assertEquals(LocalDate(2022, 3, 21), nowruz1401.toLocalDate())

        // 1402 (Standard year)
        val nowruz1402 = LocalDate(2023, 3, 21).toJalali()
        assertEquals(JalaliDate(1402, 1, 1), nowruz1402)
        assertEquals(LocalDate(2023, 3, 21), nowruz1402.toLocalDate())

        // 1403 (Leap year)
        val nowruz1403 = LocalDate(2024, 3, 20).toJalali()
        assertEquals(JalaliDate(1403, 1, 1), nowruz1403)
        assertEquals(LocalDate(2024, 3, 20), nowruz1403.toLocalDate())
    }

    @Test
    fun testEsfandMonthEndInStandardAndLeapYears() {
        // 1402 is standard: Esfand 29 is March 19, 2024
        val esfand29_1402 = LocalDate(2024, 3, 19).toJalali()
        assertEquals(1402, esfand29_1402.year)
        assertEquals(12, esfand29_1402.month)
        assertEquals(29, esfand29_1402.day)
        assertEquals(LocalDate(2024, 3, 19), esfand29_1402.toLocalDate())

        // Next day is Nowruz 1403
        val nextDay = LocalDate(2024, 3, 20).toJalali()
        assertEquals(JalaliDate(1403, 1, 1), nextDay)

        // 1403 is a leap year: Esfand has 30 days. Esfand 30 is March 20, 2025
        val esfand30_1403 = LocalDate(2025, 3, 20).toJalali()
        assertEquals(1403, esfand30_1403.year)
        assertEquals(12, esfand30_1403.month)
        assertEquals(30, esfand30_1403.day)
        assertEquals(LocalDate(2025, 3, 20), esfand30_1403.toLocalDate())
    }

    @Test
    fun testMonthLengthTransitionsInFirstAndSecondHalves() {
        // Month 1 to 6 have 31 days
        // Shahrivar 31, 1403 -> Sept 21, 2024
        val shahrivar31 = LocalDate(2024, 9, 21).toJalali()
        assertEquals(JalaliDate(1403, 6, 31), shahrivar31)

        // Mehr 1, 1403 -> Sept 22, 2024
        val mehr1 = LocalDate(2024, 9, 22).toJalali()
        assertEquals(JalaliDate(1403, 7, 1), mehr1)

        // Months 7 to 11 have 30 days
        // Bahman 30, 1403 -> Feb 18, 2025
        val bahman30 = LocalDate(2025, 2, 18).toJalali()
        assertEquals(JalaliDate(1403, 11, 30), bahman30)

        // Esfand 1, 1403 -> Feb 19, 2025
        val esfand1 = LocalDate(2025, 2, 19).toJalali()
        assertEquals(JalaliDate(1403, 12, 1), esfand1)
    }

    @Test
    fun testFullYearRoundTripConversion() {
        // Test bidirectional round trip for all 366 days of 2024
        var current = LocalDate(2024, 1, 1)
        for (i in 0 until 366) {
            val jalali = current.toJalali()
            val back = jalali.toLocalDate()
            assertEquals(current, back, "Failed round-trip for date $current -> $jalali -> $back")
            current = current.plus(1, DateTimeUnit.DAY)
        }
    }
}
