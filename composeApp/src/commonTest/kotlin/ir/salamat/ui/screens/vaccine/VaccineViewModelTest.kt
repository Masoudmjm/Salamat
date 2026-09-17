package ir.salamat.ui.screens.vaccine

import app.cash.turbine.test
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineStatus
import ir.salamat.test.FakeProfileRepository
import ir.salamat.test.FakeVaccineRepository
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
class VaccineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val profileRepo = FakeProfileRepository()
    private val vaccineRepo = FakeVaccineRepository()

    private val sampleChild = Profile(
        id = "child_vac_1",
        name = "آرتین رضایی",
        birthDate = LocalDate(2024, 1, 1),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
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
    fun testAutoInitializeVaccinesForChild() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = VaccineViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            vaccineRepository = vaccineRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.totalCount == 0) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals("آرتین رضایی", state.profile?.name)
            // Iran national childhood schedule has 16 doses
            assertEquals(16, state.totalCount)
            assertEquals(0, state.completedCount)
            assertEquals(0f, state.progress)
            assertTrue(state.milestones.isNotEmpty())
        }
    }

    @Test
    fun testMarkAdministeredAndRevertToPending() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = VaccineViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            vaccineRepository = vaccineRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.totalCount == 0) {
                state = awaitItem()
            }

            val targetVaccine = state.milestones.first().vaccines.first()
            val administeredDate = LocalDate(2024, 1, 2)

            // 1. Mark administered
            viewModel.markAdministered(
                recordId = targetVaccine.record.id,
                administeredDate = administeredDate,
                notes = "تزریق در بدو تولد بدون عارضه"
            )

            var updated = awaitItem()
            while (updated.completedCount == 0) {
                updated = awaitItem()
            }

            assertEquals(1, updated.completedCount)
            // 1 / 16 = 0.0625
            assertTrue(updated.progress > 0.06f)

            val administeredCard = updated.milestones.first().vaccines.find { it.record.id == targetVaccine.record.id }
            assertNotNull(administeredCard)
            assertEquals(VaccineStatus.COMPLETED, administeredCard.status)
            assertEquals(administeredDate, administeredCard.administeredDate)

            // 2. Mark back to pending
            viewModel.markPending(targetVaccine.record.id)

            var reverted = awaitItem()
            while (reverted.completedCount != 0) {
                reverted = awaitItem()
            }

            assertEquals(0, reverted.completedCount)
            assertEquals(0f, reverted.progress)
            val revertedCard = reverted.milestones.first().vaccines.find { it.record.id == targetVaccine.record.id }
            assertNotNull(revertedCard)
            assertNull(revertedCard.administeredDate)
        }
    }

    @Test
    fun testFilterVaccineMilestones() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = VaccineViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            vaccineRepository = vaccineRepo
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.totalCount == 0) {
                state = awaitItem()
            }

            assertEquals(VaccineFilter.ALL, state.selectedFilter)

            viewModel.setFilter(VaccineFilter.COMPLETED)
            var filtered = awaitItem()
            while (filtered.selectedFilter != VaccineFilter.COMPLETED) {
                filtered = awaitItem()
            }
            assertEquals(VaccineFilter.COMPLETED, filtered.selectedFilter)
        }
    }
}
