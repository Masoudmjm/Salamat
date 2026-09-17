package ir.salamat.ui

import app.cash.turbine.test
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.notification.FakeNotificationScheduler
import ir.salamat.core.notification.NotificationPreferences
import ir.salamat.core.notification.ReminderSyncEngine
import ir.salamat.test.FakeCheckupRepository
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
class AppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val profileRepo = FakeProfileRepository()
    private val vaccineRepo = FakeVaccineRepository()
    private val checkupRepo = FakeCheckupRepository()
    private val notificationScheduler = FakeNotificationScheduler()
    private val reminderSyncEngine = ReminderSyncEngine(notificationScheduler)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        profileRepo.onDeleteProfileCallback = { profileId ->
            vaccineRepo.deleteVaccinesForProfile(profileId)
            checkupRepo.deleteCheckupsForProfile(profileId)
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AppViewModel(
        profileRepository = profileRepo,
        vaccineRepository = vaccineRepo,
        checkupRepository = checkupRepo,
        notificationScheduler = notificationScheduler,
        reminderSyncEngine = reminderSyncEngine
    )

    @Test
    fun testInitialStateEmptyProfiles() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertTrue(state.profiles.isEmpty())
            assertNull(state.activeProfile)
            assertEquals(0, state.overdueCount)
            assertEquals(0, state.dueSoonCount)
            assertTrue(state.isPersian)
        }
    }

    @Test
    fun testAddChildProfileInitializesVaccinesAndSetsActive() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            var saved = false
            viewModel.addProfile(
                name = "آرتین",
                birthDate = LocalDate(2024, 1, 1),
                gender = Gender.MALE,
                type = ProfileType.CHILD,
                avatarColor = 0xFF0A686D.toInt(),
                onSuccess = { saved = true }
            )

            var updated = awaitItem()
            while (updated.profiles.isEmpty()) {
                updated = awaitItem()
            }

            assertTrue(saved)
            assertEquals(1, updated.profiles.size)
            assertEquals("آرتین", updated.activeProfile?.name)
            assertEquals(ProfileType.CHILD, updated.activeProfile?.type)
        }
    }

    @Test
    fun testActiveProfileSelectionAndFallbackOnDeletion() = runTest {
        val child = Profile(
            id = "c1",
            name = "کودک ۱",
            birthDate = LocalDate(2024, 1, 1),
            gender = Gender.MALE,
            type = ProfileType.CHILD,
            avatarColor = 1,
            createdAt = 10L
        )
        val adult = Profile(
            id = "a1",
            name = "بزرگسال ۱",
            birthDate = LocalDate(1990, 1, 1),
            gender = Gender.FEMALE,
            type = ProfileType.ADULT,
            avatarColor = 2,
            createdAt = 20L
        )
        profileRepo.saveProfile(child)
        profileRepo.saveProfile(adult)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.profiles.size < 2) {
                state = awaitItem()
            }

            // Defaults to first profile (child)
            assertEquals("c1", state.activeProfile?.id)

            // Select adult
            viewModel.selectProfile("a1")
            var selectedState = awaitItem()
            while (selectedState.activeProfile?.id != "a1") {
                selectedState = awaitItem()
            }
            assertEquals("a1", selectedState.activeProfile.id)

            // Delete active adult profile
            viewModel.deleteProfile("a1")
            var fallbackState = awaitItem()
            while (fallbackState.profiles.size != 1) {
                fallbackState = awaitItem()
            }

            // Automatically fell back to remaining child
            assertEquals("c1", fallbackState.activeProfile?.id)

            // Delete remaining child
            viewModel.deleteProfile("c1")
            var emptyState = awaitItem()
            while (emptyState.profiles.isNotEmpty()) {
                emptyState = awaitItem()
            }
            assertNull(emptyState.activeProfile)
        }
    }

    @Test
    fun testLanguageToggle() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertTrue(state.isPersian)

            viewModel.toggleLanguage()
            var englishState = awaitItem()
            while (englishState.isPersian) {
                englishState = awaitItem()
            }
            assertFalse(englishState.isPersian)

            viewModel.setLanguage(true)
            var persianState = awaitItem()
            while (!persianState.isPersian) {
                persianState = awaitItem()
            }
            assertTrue(persianState.isPersian)
        }
    }

    @Test
    fun testUpdateNotificationPreferences() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            val newPrefs = NotificationPreferences(
                enabled = true,
                preferredHour = 10,
                preferredMinute = 30,
                notify7DaysBefore = false,
                notify1DayBefore = true,
                notifyOnDueDate = true,
                notifyWhenOverdue = false
            )
            viewModel.updateNotificationPreferences(newPrefs)

            var updatedState = awaitItem()
            while (updatedState.notificationPreferences != newPrefs) {
                updatedState = awaitItem()
            }

            assertEquals(10, updatedState.notificationPreferences.preferredHour)
            assertEquals(30, updatedState.notificationPreferences.preferredMinute)
            assertFalse(updatedState.notificationPreferences.notify7DaysBefore)
            assertTrue(updatedState.notificationPreferences.notify1DayBefore)
        }
    }
}
