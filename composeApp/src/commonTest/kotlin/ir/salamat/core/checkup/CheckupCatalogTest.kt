package ir.salamat.core.checkup

import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CheckupCatalogTest {

    @Test
    fun testCatalogHasCoreAdultScreenings() {
        assertNotNull(CheckupCatalog.findByKey("blood_pressure"))
        assertNotNull(CheckupCatalog.findByKey("annual_blood_test"))
        assertNotNull(CheckupCatalog.findByKey("dental_exam"))
        assertNotNull(CheckupCatalog.findByKey("eye_exam"))
        assertNotNull(CheckupCatalog.findByKey("colorectal_screening"))
        assertNotNull(CheckupCatalog.findByKey("cervical_screening"))
        assertNotNull(CheckupCatalog.findByKey("breast_screening"))
        assertNotNull(CheckupCatalog.findByKey("bone_density"))
        assertNotNull(CheckupCatalog.findByKey("general_physician"))
    }

    @Test
    fun testAgeCalculation() {
        val birthDate = LocalDate(1990, 5, 15)
        // Before birthday
        assertEquals(33, CheckupCatalog.calculateAgeYears(birthDate, LocalDate(2024, 5, 10)))
        // On birthday
        assertEquals(34, CheckupCatalog.calculateAgeYears(birthDate, LocalDate(2024, 5, 15)))
        // After birthday
        assertEquals(34, CheckupCatalog.calculateAgeYears(birthDate, LocalDate(2024, 6, 1)))
    }

    @Test
    fun testEligibleCheckupsForYoungAdultMale() {
        val profile = Profile(
            id = "m1",
            name = "علی",
            birthDate = LocalDate(2000, 1, 1),
            gender = Gender.MALE,
            type = ProfileType.ADULT,
            avatarColor = 0,
            createdAt = 0
        )
        val today = LocalDate(2024, 1, 1) // Age 24
        val eligible = CheckupCatalog.getEligibleCheckups(profile, today)
        val keys = eligible.map { it.key }

        // Must include: blood_pressure, dental, eye, general_physician
        assertTrue(keys.contains("blood_pressure"))
        assertTrue(keys.contains("dental_exam"))
        assertTrue(keys.contains("eye_exam"))
        assertTrue(keys.contains("general_physician"))

        // Should NOT include 30+ (annual_blood_test), 50+ (colorectal), 60+ (bone_density)
        assertFalse(keys.contains("annual_blood_test"))
        assertFalse(keys.contains("colorectal_screening"))
        assertFalse(keys.contains("bone_density"))

        // Male should NOT include female-only screenings
        assertFalse(keys.contains("breast_screening"))
        assertFalse(keys.contains("cervical_screening"))
    }

    @Test
    fun testEligibleCheckupsForMiddleAgedFemale() {
        val profile = Profile(
            id = "f1",
            name = "مریم",
            birthDate = LocalDate(1980, 1, 1),
            gender = Gender.FEMALE,
            type = ProfileType.ADULT,
            avatarColor = 0,
            createdAt = 0
        )
        val today = LocalDate(2024, 1, 1) // Age 44
        val eligible = CheckupCatalog.getEligibleCheckups(profile, today)
        val keys = eligible.map { it.key }

        // Must include:
        assertTrue(keys.contains("blood_pressure"))
        assertTrue(keys.contains("annual_blood_test")) // 30+
        assertTrue(keys.contains("dental_exam"))
        assertTrue(keys.contains("eye_exam"))
        assertTrue(keys.contains("cervical_screening")) // Female 30-59
        assertTrue(keys.contains("breast_screening"))   // Female 40-69
        assertTrue(keys.contains("general_physician"))

        // Should NOT include age 50+ colorectal yet
        assertFalse(keys.contains("colorectal_screening"))
        assertFalse(keys.contains("bone_density"))
    }

    @Test
    fun testEligibleCheckupsForSenior() {
        val profile = Profile(
            id = "s1",
            name = "پدربزرگ",
            birthDate = LocalDate(1960, 1, 1),
            gender = Gender.MALE,
            type = ProfileType.ADULT,
            avatarColor = 0,
            createdAt = 0
        )
        val today = LocalDate(2024, 1, 1) // Age 64
        val eligible = CheckupCatalog.getEligibleCheckups(profile, today)
        val keys = eligible.map { it.key }

        assertTrue(keys.contains("colorectal_screening")) // Age 50-70
        assertTrue(keys.contains("bone_density"))          // Age 60+
    }
}
