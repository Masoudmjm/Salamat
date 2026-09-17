package ir.salamat.ui.screens.checkup

import app.cash.turbine.test
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.test.FakeCheckupRepository
import ir.salamat.test.FakeProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val profileRepo = FakeProfileRepository()
    private val checkupRepo = FakeCheckupRepository()

    private val sampleAdult = Profile(
        id = "adult_1",
        name = "مریم حسینی",
        birthDate = LocalDate(1985, 5, 20),
        gender = Gender.FEMALE,
        type = ProfileType.ADULT,
        avatarColor = 0xFF8E24AA.toInt(),
        createdAt = 10L
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAutoInitializeStandardCheckupsForAdult() = runTest {
        profileRepo.saveProfile(sampleAdult)

        val viewModel = CheckupViewModel(
            profileId = sampleAdult.id,
            checkupRepository = checkupRepo,
            profileRepository = profileRepo
        )

        viewModel.uiState.test {
            // Initial loading or loaded with checkups
            var state = awaitItem()
            while (state.isLoading || state.checkups.isEmpty()) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals("مریم حسینی", state.profile?.name)
            // Eligible checkups for female age ~39 should include blood pressure, annual blood test, dental, etc.
            assertTrue(state.checkups.isNotEmpty())
        }
    }

    @Test
    fun testAddCustomCheckup() = runTest {
        profileRepo.saveProfile(sampleAdult)

        val viewModel = CheckupViewModel(
            profileId = sampleAdult.id,
            checkupRepository = checkupRepo,
            profileRepository = profileRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.checkups.isEmpty()) {
                state = awaitItem()
            }
            val initialCount = state.checkups.size

            viewModel.addCheckup(
                titleKey = "custom_eye_exam",
                intervalMonths = 12,
                nextDueDate = LocalDate(2025, 1, 1),
                notes = "بررسی نمره عینک"
            )

            var updated = awaitItem()
            while (updated.checkups.size == initialCount) {
                updated = awaitItem()
            }

            assertEquals(initialCount + 1, updated.checkups.size)
            val custom = updated.checkups.find { it.titleKey == "custom_eye_exam" }
            assertNotNull(custom)
            assertEquals(12, custom.intervalMonths)
            assertEquals("بررسی نمره عینک", custom.notes)
        }
    }

    @Test
    fun testRecordCheckupCompleted() = runTest {
        profileRepo.saveProfile(sampleAdult)

        val viewModel = CheckupViewModel(
            profileId = sampleAdult.id,
            checkupRepository = checkupRepo,
            profileRepository = profileRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.checkups.isEmpty()) {
                state = awaitItem()
            }

            val target = state.checkups.first()
            val completedDate = LocalDate(2024, 6, 1)
            val nextDueDate = LocalDate(2024, 12, 1)

            viewModel.recordCheckupCompleted(
                id = target.id,
                completedDate = completedDate,
                nextDueDate = nextDueDate,
                notes = "نتایج کاملاً نرمال"
            )

            var updated = awaitItem()
            while (updated.checkups.find { it.id == target.id }?.lastCompletedDate == null) {
                updated = awaitItem()
            }

            val updatedCheckup = updated.checkups.find { it.id == target.id }
            assertNotNull(updatedCheckup)
            assertEquals(completedDate, updatedCheckup.lastCompletedDate)
            assertEquals(nextDueDate, updatedCheckup.nextDueDate)
            assertEquals("نتایج کاملاً نرمال", updatedCheckup.notes)
        }
    }

    @Test
    fun testFilterCheckups() = runTest {
        profileRepo.saveProfile(sampleAdult)

        val viewModel = CheckupViewModel(
            profileId = sampleAdult.id,
            checkupRepository = checkupRepo,
            profileRepository = profileRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.checkups.isEmpty()) {
                state = awaitItem()
            }

            assertEquals(CheckupFilter.ALL, state.selectedFilter)

            viewModel.setFilter(CheckupFilter.COMPLETED)
            var filtered = awaitItem()
            while (filtered.selectedFilter != CheckupFilter.COMPLETED) {
                filtered = awaitItem()
            }
            assertEquals(CheckupFilter.COMPLETED, filtered.selectedFilter)
        }
    }

    @Test
    fun testDeleteCheckup() = runTest {
        profileRepo.saveProfile(sampleAdult)

        val viewModel = CheckupViewModel(
            profileId = sampleAdult.id,
            checkupRepository = checkupRepo,
            profileRepository = profileRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.checkups.isEmpty()) {
                state = awaitItem()
            }
            val countBefore = state.checkups.size
            val toDelete = state.checkups.first()

            viewModel.deleteCheckup(toDelete.id)

            var stateAfter = awaitItem()
            while (stateAfter.checkups.size == countBefore) {
                stateAfter = awaitItem()
            }

            assertEquals(countBefore - 1, stateAfter.checkups.size)
            assertNull(stateAfter.checkups.find { it.id == toDelete.id })
        }
    }
}
