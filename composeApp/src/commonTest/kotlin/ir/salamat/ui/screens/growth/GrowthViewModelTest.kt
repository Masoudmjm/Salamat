package ir.salamat.ui.screens.growth

import app.cash.turbine.test
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.test.FakeGrowthRepository
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
class GrowthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val profileRepo = FakeProfileRepository()
    private val growthRepo = FakeGrowthRepository()

    private val sampleChild = Profile(
        id = "child_1",
        name = "آرتین رضایی",
        birthDate = LocalDate(2024, 1, 1),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
        createdAt = 100L
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
    fun testInitialUiStateEmptyRecords() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = GrowthViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            growthRepository = growthRepo
        )

        viewModel.uiState.test {
            // Initial or loaded state
            val state = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertFalse(state.isLoading)
            assertEquals("آرتین رضایی", state.profile?.name)
            assertTrue(state.records.isEmpty())
            assertNull(state.latestRecord)
            assertNull(state.bmiCategory)
        }
    }

    @Test
    fun testAddRecordUpdatesStateAndCalculatesBmi() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = GrowthViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            growthRepository = growthRepo
        )

        viewModel.uiState.test {
            val initial = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertTrue(initial.records.isEmpty())

            // Add first measurement: Weight 10kg, Height 80cm
            viewModel.addRecord(
                date = LocalDate(2024, 6, 1),
                weightKg = 10.0,
                heightCm = 80.0,
                headCircumferenceCm = 45.0,
                notes = "۶ ماهگی"
            )

            val updated = awaitItem()
            assertEquals(1, updated.records.size)
            val latest = updated.latestRecord
            assertNotNull(latest)
            assertEquals(10.0, latest.weightKg)
            assertEquals(80.0, latest.heightCm)
            assertEquals(45.0, latest.headCircumferenceCm)
            // BMI = 10 / (0.8^2) = 10 / 0.64 = 15.625 -> 15.6
            assertEquals(15.6, latest.bmi)
            assertEquals(BmiCategory.UNDERWEIGHT, updated.bmiCategory)
        }
    }

    @Test
    fun testDeleteRecordRemovesFromState() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = GrowthViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            growthRepository = growthRepo
        )

        viewModel.uiState.test {
            awaitItem().let { if (it.isLoading) awaitItem() else it }

            viewModel.addRecord(
                date = LocalDate(2024, 6, 1),
                weightKg = 10.0,
                heightCm = 80.0,
                headCircumferenceCm = null,
                notes = null
            )
            val stateWithRecord = awaitItem()
            val recordId = stateWithRecord.records.first().id

            viewModel.deleteRecord(recordId)
            val stateAfterDelete = awaitItem()
            assertTrue(stateAfterDelete.records.isEmpty())
            assertNull(stateAfterDelete.latestRecord)
        }
    }

    @Test
    fun testToggleAddDialogState() = runTest {
        profileRepo.saveProfile(sampleChild)

        val viewModel = GrowthViewModel(
            profileId = sampleChild.id,
            profileRepository = profileRepo,
            growthRepository = growthRepo
        )

        viewModel.uiState.test {
            val initial = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertFalse(initial.isAddDialogOpen)

            viewModel.setAddDialogOpen(true)
            val openState = awaitItem()
            assertTrue(openState.isAddDialogOpen)

            viewModel.setAddDialogOpen(false)
            val closedState = awaitItem()
            assertFalse(closedState.isAddDialogOpen)
        }
    }
}
