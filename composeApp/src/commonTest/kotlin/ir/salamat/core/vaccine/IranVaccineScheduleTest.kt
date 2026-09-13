package ir.salamat.core.vaccine

import ir.salamat.core.model.VaccineStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IranVaccineScheduleTest {

    @Test
    fun testVaccineListContainsCoreVaccines() {
        assertEquals(16, IranVaccineSchedule.ALL_VACCINES.size)
        assertNotNull(IranVaccineSchedule.getByCode("BCG"))
        assertNotNull(IranVaccineSchedule.getByCode("PENTA_1"))
        assertNotNull(IranVaccineSchedule.getByCode("MMR_1"))
    }

    @Test
    fun testDueDateCalculation() {
        val birthDate = LocalDate(2024, 1, 15)
        val birthVaccineDueDate = IranVaccineSchedule.calculateDueDate(birthDate, 0)
        assertEquals(birthDate, birthVaccineDueDate)

        val twoMonthDueDate = IranVaccineSchedule.calculateDueDate(birthDate, 2)
        assertEquals(LocalDate(2024, 3, 15), twoMonthDueDate)

        val oneYearDueDate = IranVaccineSchedule.calculateDueDate(birthDate, 12)
        assertEquals(LocalDate(2025, 1, 15), oneYearDueDate)
    }

    @Test
    fun testStatusComputation() {
        val birthDate = LocalDate(2024, 1, 1)

        // 1. Completed
        val completedStatus = IranVaccineSchedule.computeStatus(
            targetAgeMonths = 2,
            birthDate = birthDate,
            administeredDate = LocalDate(2024, 3, 1),
            currentDate = LocalDate(2024, 3, 2)
        )
        assertEquals(VaccineStatus.COMPLETED, completedStatus)

        // 2. Upcoming (well before due date)
        val upcomingStatus = IranVaccineSchedule.computeStatus(
            targetAgeMonths = 2, // Due: 2024-03-01
            birthDate = birthDate,
            administeredDate = null,
            currentDate = LocalDate(2024, 1, 15)
        )
        assertEquals(VaccineStatus.UPCOMING, upcomingStatus)

        // 3. Due soon (within 14 days of due date)
        val dueStatus = IranVaccineSchedule.computeStatus(
            targetAgeMonths = 2, // Due: 2024-03-01
            birthDate = birthDate,
            administeredDate = null,
            currentDate = LocalDate(2024, 2, 20)
        )
        assertEquals(VaccineStatus.DUE, dueStatus)

        // 4. Overdue (more than 30 days past due date)
        val overdueStatus = IranVaccineSchedule.computeStatus(
            targetAgeMonths = 2, // Due: 2024-03-01, Overdue after 2024-03-31
            birthDate = birthDate,
            administeredDate = null,
            currentDate = LocalDate(2024, 4, 10)
        )
        assertEquals(VaccineStatus.OVERDUE, overdueStatus)
    }
}
