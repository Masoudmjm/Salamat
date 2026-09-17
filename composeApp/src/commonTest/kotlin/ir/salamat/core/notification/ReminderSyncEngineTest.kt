package ir.salamat.core.notification

import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeNotificationScheduler : NotificationScheduler {
    val scheduledItems = mutableListOf<NotificationItem>()
    var cancelledIds = mutableListOf<String>()
    var cancelAllCalled = false

    override suspend fun schedule(item: NotificationItem) {
        scheduledItems.add(item)
    }

    override suspend fun cancel(id: String) {
        cancelledIds.add(id)
    }

    override suspend fun cancelAll() {
        cancelAllCalled = true
        scheduledItems.clear()
    }

    override suspend fun hasPermission(): Boolean = true
    override suspend fun requestPermission(): Boolean = true
    override suspend fun showImmediateNotification(item: NotificationItem) {
        scheduledItems.add(item)
    }
}

class ReminderSyncEngineTest {

    private val fakeScheduler = FakeNotificationScheduler()
    private val engine = ReminderSyncEngine(fakeScheduler)

    private val sampleChild = Profile(
        id = "c1",
        name = "آرتین رضایی",
        birthDate = LocalDate(2024, 2, 1),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
        createdAt = 0L
    )

    private val sampleAdult = Profile(
        id = "a1",
        name = "سارا رضایی",
        birthDate = LocalDate(1990, 1, 1),
        gender = Gender.FEMALE,
        type = ProfileType.ADULT,
        avatarColor = 0xFF1565C0.toInt(),
        createdAt = 0L
    )

    @Test
    fun testGenerates7DayVaccineReminder() {
        // Child born 2024-02-01. 2-month vaccine due ~ 2024-04-01 (60 days later).
        // Let's set currentDate to 7 days before due date.
        val record = VaccineRecord(
            id = "rec_1",
            profileId = "c1",
            vaccineCode = "PENTA_1",
            targetAgeMonths = 2,
            status = VaccineStatus.DUE,
            administeredDate = null,
            notes = null,
            createdAt = 0L
        )
        // targetDays = 60 from 2024-02-01 is 2024-04-01.
        // 7 days before is 2024-03-25.
        val currentDate = LocalDate(2024, 3, 25)
        val prefs = NotificationPreferences(
            enabled = true,
            notify7DaysBefore = true,
            notify1DayBefore = true,
            notifyOnDueDate = true,
            notifyWhenOverdue = true
        )

        val reminders = engine.generateReminders(
            profiles = listOf(sampleChild),
            pendingVaccines = listOf(record),
            checkups = emptyList(),
            preferences = prefs,
            currentDate = currentDate,
            isPersian = true
        )

        assertEquals(1, reminders.size)
        assertTrue(reminders[0].title.contains("آرتین رضایی"))
        assertTrue(reminders[0].body.contains("۷ روز"))
    }

    @Test
    fun testGeneratesOverdueCheckupReminder() {
        val checkup = CheckupReminder(
            id = "chk_1",
            profileId = "a1",
            titleKey = "blood_pressure",
            intervalMonths = 6,
            lastCompletedDate = null,
            nextDueDate = LocalDate(2024, 5, 1),
            notes = null,
            createdAt = 0L
        )
        // Current date is 5 days past due date (2024-05-06)
        val currentDate = LocalDate(2024, 5, 6)
        val prefs = NotificationPreferences(
            enabled = true,
            notifyWhenOverdue = true
        )

        val reminders = engine.generateReminders(
            profiles = listOf(sampleAdult),
            pendingVaccines = emptyList(),
            checkups = listOf(checkup),
            preferences = prefs,
            currentDate = currentDate,
            isPersian = true
        )

        assertEquals(1, reminders.size)
        assertTrue(reminders[0].title.contains("تأخیر"))
        assertTrue(reminders[0].body.contains("سنجش فشار خون"))
        assertTrue(reminders[0].body.contains("۵ روز تأخیر"))
    }

    @Test
    fun testDisabledPreferencesReturnsEmptyList() {
        val checkup = CheckupReminder(
            id = "chk_1",
            profileId = "a1",
            titleKey = "blood_pressure",
            intervalMonths = 6,
            lastCompletedDate = null,
            nextDueDate = LocalDate(2024, 5, 1),
            notes = null,
            createdAt = 0L
        )
        val reminders = engine.generateReminders(
            profiles = listOf(sampleAdult),
            pendingVaccines = emptyList(),
            checkups = listOf(checkup),
            preferences = NotificationPreferences(enabled = false),
            currentDate = LocalDate(2024, 5, 1)
        )
        assertTrue(reminders.isEmpty())
    }

    @Test
    fun testSyncSchedulesItemsViaScheduler() = runTest {
        val checkup = CheckupReminder(
            id = "chk_1",
            profileId = "a1",
            titleKey = "blood_pressure",
            intervalMonths = 6,
            lastCompletedDate = null,
            nextDueDate = LocalDate(2024, 5, 1),
            notes = null,
            createdAt = 0L
        )
        val currentDate = LocalDate(2024, 5, 1) // Due today!

        engine.sync(
            profiles = listOf(sampleAdult),
            pendingVaccines = emptyList(),
            checkups = listOf(checkup),
            preferences = NotificationPreferences(enabled = true, notifyOnDueDate = true),
            currentDate = currentDate
        )

        assertTrue(fakeScheduler.cancelAllCalled)
        assertEquals(1, fakeScheduler.scheduledItems.size)
        assertTrue(fakeScheduler.scheduledItems[0].title.contains("امروز"))
    }
}
