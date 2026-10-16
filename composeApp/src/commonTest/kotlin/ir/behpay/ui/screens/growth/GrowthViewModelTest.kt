package ir.behpay.ui.screens.growth

import app.cash.turbine.test
import ir.behpay.core.growth.ChildGrowthStatus
import ir.behpay.core.model.Gender
import ir.behpay.core.model.Profile
import ir.behpay.core.model.ProfileType
import ir.behpay.test.FakeGrowthRepository
import ir.behpay.test.FakeProfileRepository
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
    fun testAddRecordUpdatesStateForChildAndEvaluatesGrowthCard() = runTest {
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
            assertEquals(15.6, latest.bmi)
            // Adult BMI category MUST NOT be applied to babies/children
            assertNull(updated.bmiCategory)
            // Evaluated by child growth monitoring curves
            assertNotNull(updated.childEvaluation)
            assertEquals(ChildGrowthStatus.APPROPRIATE, updated.childEvaluation?.status)
            assertEquals(Pair(9.5, 12.1), updated.idealWeightRange)
        }
    }

    @Test
    fun testAddRecordUpdatesStateForAdultAndCalculatesBmi() = runTest {
        val sampleAdult = Profile(
            id = "adult_1",
            name = "علی رضایی",
            birthDate = LocalDate(1990, 5, 1),
            gender = Gender.MALE,
            type = ProfileType.ADULT,
            avatarColor = 0xFF0A686D.toInt(),
            createdAt = 100L
        )
        profileRepo.saveProfile(sampleAdult)

        val viewModel = GrowthViewModel(
            profileId = sampleAdult.id,
            profileRepository = profileRepo,
            growthRepository = growthRepo
        )

        viewModel.uiState.test {
            val initial = awaitItem().let { if (it.isLoading) awaitItem() else it }
            assertTrue(initial.records.isEmpty())

            // Height 180cm, Weight 75kg -> BMI 23.1 (Normal)
            viewModel.addRecord(
                date = LocalDate(2024, 6, 1),
                weightKg = 75.0,
                heightCm = 180.0,
                headCircumferenceCm = null,
                notes = "چک‌آپ سالانه"
            )

            val updated = awaitItem()
            val latest = updated.latestRecord
            assertNotNull(latest)
            assertEquals(23.1, latest.bmi)
            assertEquals(BmiCategory.NORMAL, updated.bmiCategory)
            assertNull(updated.childEvaluation)
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
