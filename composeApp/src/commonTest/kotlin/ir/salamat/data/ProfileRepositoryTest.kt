package ir.salamat.data

import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.Gender
import ir.salamat.core.model.GrowthRecord
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.test.FakeCheckupRepository
import ir.salamat.test.FakeGrowthRepository
import ir.salamat.test.FakeProfileRepository
import ir.salamat.test.FakeVaccineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileRepositoryTest {

    private val profileRepo = FakeProfileRepository()
    private val vaccineRepo = FakeVaccineRepository()
    private val growthRepo = FakeGrowthRepository()
    private val checkupRepo = FakeCheckupRepository()

    init {
        // Wire cascading deletion callback to emulate ProfileRepositoryImpl cascading logic
        profileRepo.onDeleteProfileCallback = { profileId ->
            vaccineRepo.deleteVaccinesForProfile(profileId)
            growthRepo.deleteGrowthRecordsForProfile(profileId)
            checkupRepo.deleteCheckupsForProfile(profileId)
        }
    }

    @Test
    fun testSaveAndRetrieveProfiles() = runTest {
        val child = Profile(
            id = "c1",
            name = "آرتین",
            birthDate = LocalDate(2024, 1, 1),
            gender = Gender.MALE,
            type = ProfileType.CHILD,
            avatarColor = 0xFF0A686D.toInt(),
            createdAt = 1000L
        )
        val adult = Profile(
            id = "a1",
            name = "مریم",
            birthDate = LocalDate(1988, 4, 15),
            gender = Gender.FEMALE,
            type = ProfileType.ADULT,
            avatarColor = 0xFF8E24AA.toInt(),
            createdAt = 2000L
        )

        profileRepo.saveProfile(child)
        profileRepo.saveProfile(adult)

        val all = profileRepo.getAllProfiles().first()
        assertEquals(2, all.size)

        val retrievedChild = profileRepo.getProfileById("c1").first()
        assertNotNull(retrievedChild)
        assertEquals("آرتین", retrievedChild.name)
        assertEquals(ProfileType.CHILD, retrievedChild.type)
    }

    @Test
    fun testUpdateProfileFields() = runTest {
        val initial = Profile(
            id = "p1",
            name = "علی",
            birthDate = LocalDate(1995, 2, 10),
            gender = Gender.MALE,
            type = ProfileType.ADULT,
            avatarColor = 0xFF123456.toInt(),
            createdAt = 100L
        )
        profileRepo.saveProfile(initial)

        val updated = initial.copy(
            name = "علی رضایی",
            birthDate = LocalDate(1995, 2, 12),
            avatarColor = 0xFF654321.toInt()
        )
        profileRepo.saveProfile(updated)

        val retrieved = profileRepo.getProfileById("p1").first()
        assertNotNull(retrieved)
        assertEquals("علی رضایی", retrieved.name)
        assertEquals(LocalDate(1995, 2, 12), retrieved.birthDate)
        assertEquals(0xFF654321.toInt(), retrieved.avatarColor)
    }

    @Test
    fun testCascadingDeletionCleansChildRecords() = runTest {
        val targetProfileId = "user_delete_target"
        val otherProfileId = "user_keep"

        // 1. Create target and other profiles
        profileRepo.saveProfile(
            Profile(
                id = targetProfileId,
                name = "هدف حذف",
                birthDate = LocalDate(2024, 1, 1),
                gender = Gender.FEMALE,
                type = ProfileType.CHILD,
                avatarColor = 0,
                createdAt = 10L
            )
        )
        profileRepo.saveProfile(
            Profile(
                id = otherProfileId,
                name = "کاربر ماندگار",
                birthDate = LocalDate(2020, 1, 1),
                gender = Gender.MALE,
                type = ProfileType.CHILD,
                avatarColor = 0,
                createdAt = 20L
            )
        )

        // 2. Attach vaccines, growth records, and checkup reminders to target
        vaccineRepo.initializeVaccinesForProfile(targetProfileId, LocalDate(2024, 1, 1), LocalDate(2024, 6, 1))
        vaccineRepo.initializeVaccinesForProfile(otherProfileId, LocalDate(2020, 1, 1), LocalDate(2024, 6, 1))

        growthRepo.addGrowthRecord(
            GrowthRecord(
                id = "g_target",
                profileId = targetProfileId,
                date = LocalDate(2024, 2, 1),
                weightKg = 5.2,
                heightCm = 58.0,
                headCircumferenceCm = 38.0,
                bmi = 15.5,
                notes = "Check",
                createdAt = 1L
            )
        )
        growthRepo.addGrowthRecord(
            GrowthRecord(
                id = "g_other",
                profileId = otherProfileId,
                date = LocalDate(2024, 2, 1),
                weightKg = 15.0,
                heightCm = 95.0,
                headCircumferenceCm = null,
                bmi = 16.6,
                notes = "Keep",
                createdAt = 2L
            )
        )

        checkupRepo.addCheckupReminder(
            CheckupReminder(
                id = "chk_target",
                profileId = targetProfileId,
                titleKey = "chk_target",
                intervalMonths = 6,
                nextDueDate = LocalDate(2024, 8, 1),
                lastCompletedDate = null,
                notes = null,
                createdAt = 1L
            )
        )
        checkupRepo.addCheckupReminder(
            CheckupReminder(
                id = "chk_other",
                profileId = otherProfileId,
                titleKey = "chk_other",
                intervalMonths = 12,
                nextDueDate = LocalDate(2024, 12, 1),
                lastCompletedDate = null,
                notes = null,
                createdAt = 2L
            )
        )

        // Verify target records exist before delete
        assertTrue(vaccineRepo.getVaccinesForProfile(targetProfileId).first().isNotEmpty())
        assertEquals(1, growthRepo.getGrowthRecordsForProfile(targetProfileId).first().size)
        assertEquals(1, checkupRepo.getCheckupsForProfile(targetProfileId).first().size)

        // 3. Execute cascading deletion
        profileRepo.deleteProfile(targetProfileId)

        // 4. Verify target profile and its dependent child records are completely wiped
        assertNull(profileRepo.getProfileById(targetProfileId).first())
        assertEquals(0, vaccineRepo.getVaccinesForProfile(targetProfileId).first().size)
        assertEquals(0, growthRepo.getGrowthRecordsForProfile(targetProfileId).first().size)
        assertEquals(0, checkupRepo.getCheckupsForProfile(targetProfileId).first().size)

        // 5. Verify other profile's records remain 100% intact
        assertNotNull(profileRepo.getProfileById(otherProfileId).first())
        assertEquals(16, vaccineRepo.getVaccinesForProfile(otherProfileId).first().size)
        assertEquals(1, growthRepo.getGrowthRecordsForProfile(otherProfileId).first().size)
        assertEquals(1, checkupRepo.getCheckupsForProfile(otherProfileId).first().size)
    }
}
