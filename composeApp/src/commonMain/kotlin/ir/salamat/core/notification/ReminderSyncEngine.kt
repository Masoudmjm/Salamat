package ir.salamat.core.notification

import ir.salamat.core.datetime.toPersianDigits
import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.Profile
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.vaccine.IranVaccineSchedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.math.abs

class ReminderSyncEngine(
    private val scheduler: NotificationScheduler
) {

    fun generateReminders(
        profiles: List<Profile>,
        pendingVaccines: List<VaccineRecord>,
        checkups: List<CheckupReminder>,
        preferences: NotificationPreferences,
        currentDate: LocalDate,
        isPersian: Boolean = true
    ): List<NotificationItem> {
        if (!preferences.enabled) return emptyList()

        val profileMap = profiles.associateBy { it.id }
        val notifications = mutableListOf<NotificationItem>()

        // 1. Pending Childhood Vaccines
        for (record in pendingVaccines) {
            val profile = profileMap[record.profileId] ?: continue
            val dueDate = IranVaccineSchedule.calculateDueDate(profile.birthDate, record.targetAgeMonths)
            val diffDays = (dueDate.toEpochDays() - currentDate.toEpochDays()).toInt()
            val def = IranVaccineSchedule.getByCode(record.vaccineCode)
            val vaccineName = if (isPersian) (def?.nameFa ?: record.vaccineCode) else (def?.nameEn ?: record.vaccineCode)

            val triggerDays = getTargetTriggerDays(diffDays, preferences)
            for (triggerDay in triggerDays) {
                val notifyDate = dueDate.minusDays(triggerDay)
                val scheduledEpoch = calculateScheduledTime(notifyDate, preferences.preferredHour, preferences.preferredMinute)

                val (title, body) = when {
                    triggerDay == 7 -> {
                        val t = if (isPersian) "یادآور واکسیناسیون (${profile.name})" else "Vaccine Reminder (${profile.name})"
                        val b = if (isPersian) "۷ روز تا موعد $vaccineName باقی مانده است." else "7 days remaining until $vaccineName."
                        Pair(t, b)
                    }
                    triggerDay == 1 -> {
                        val t = if (isPersian) "یادآور واکسیناسیون (${profile.name})" else "Vaccine Reminder (${profile.name})"
                        val b = if (isPersian) "فردا موعد $vaccineName است." else "Tomorrow is due for $vaccineName."
                        Pair(t, b)
                    }
                    triggerDay == 0 -> {
                        val t = if (isPersian) "موعد واکسیناسیون امروز (${profile.name})" else "Vaccine Due Today (${profile.name})"
                        val b = if (isPersian) "امروز موعد تزریق $vaccineName است. لطفاً به پایگاه سلامت مراجعه فرمایید." else "Today is the scheduled date for $vaccineName."
                        Pair(t, b)
                    }
                    triggerDay < 0 -> {
                        val daysLate = abs(triggerDay)
                        val daysStr = if (isPersian) daysLate.toString().toPersianDigits() else daysLate.toString()
                        val t = if (isPersian) "تأخیر در واکسیناسیون (${profile.name})" else "Overdue Vaccine Alert (${profile.name})"
                        val b = if (isPersian) "نوبت $vaccineName دارای $daysStr روز تأخیر است." else "$vaccineName is $daysLate days overdue."
                        Pair(t, b)
                    }
                    else -> Pair("یادآور واکسن", vaccineName)
                }

                notifications.add(
                    NotificationItem(
                        id = "vaccine_${record.id}_day_$triggerDay",
                        title = title,
                        body = body,
                        scheduledEpochMillis = scheduledEpoch,
                        profileId = profile.id,
                        channel = NotificationChannelType.HEALTH_REMINDERS,
                        payloadRoute = "member/${profile.id}"
                    )
                )
            }
        }

        // 2. Adult Health Checkups & Screenings
        for (checkup in checkups) {
            val profile = profileMap[checkup.profileId] ?: continue
            val dueDate = checkup.nextDueDate
            val diffDays = (dueDate.toEpochDays() - currentDate.toEpochDays()).toInt()
            val def = ir.salamat.core.checkup.CheckupCatalog.findByKey(checkup.titleKey)
            val checkupTitle = if (isPersian) (def?.nameFa ?: checkup.titleKey) else (def?.nameEn ?: checkup.titleKey)

            val triggerDays = getTargetTriggerDays(diffDays, preferences)
            for (triggerDay in triggerDays) {
                val notifyDate = dueDate.minusDays(triggerDay)
                val scheduledEpoch = calculateScheduledTime(notifyDate, preferences.preferredHour, preferences.preferredMinute)

                val (title, body) = when {
                    triggerDay == 7 -> {
                        val t = if (isPersian) "یادآور چک‌آپ سلامت (${profile.name})" else "Health Checkup Reminder (${profile.name})"
                        val b = if (isPersian) "۷ روز تا موعد $checkupTitle باقی مانده است." else "7 days remaining until $checkupTitle."
                        Pair(t, b)
                    }
                    triggerDay == 1 -> {
                        val t = if (isPersian) "یادآور چک‌آپ سلامت (${profile.name})" else "Health Checkup Reminder (${profile.name})"
                        val b = if (isPersian) "فردا موعد $checkupTitle است." else "Tomorrow is due for $checkupTitle."
                        Pair(t, b)
                    }
                    triggerDay == 0 -> {
                        val t = if (isPersian) "موعد چک‌آپ امروز (${profile.name})" else "Checkup Due Today (${profile.name})"
                        val b = if (isPersian) "امروز موعد انجام $checkupTitle است. لطفاً نسبت به پیگیری اقدام فرمایید." else "Today is scheduled for $checkupTitle."
                        Pair(t, b)
                    }
                    triggerDay < 0 -> {
                        val daysLate = abs(triggerDay)
                        val daysStr = if (isPersian) daysLate.toString().toPersianDigits() else daysLate.toString()
                        val t = if (isPersian) "تأخیر در چک‌آپ سلامت (${profile.name})" else "Overdue Checkup Alert (${profile.name})"
                        val b = if (isPersian) "$checkupTitle دارای $daysStr روز تأخیر در پیگیری است." else "$checkupTitle is $daysLate days overdue."
                        Pair(t, b)
                    }
                    else -> Pair("یادآور چک‌آپ", checkupTitle)
                }

                notifications.add(
                    NotificationItem(
                        id = "checkup_${checkup.id}_day_$triggerDay",
                        title = title,
                        body = body,
                        scheduledEpochMillis = scheduledEpoch,
                        profileId = profile.id,
                        channel = NotificationChannelType.HEALTH_REMINDERS,
                        payloadRoute = "member/${profile.id}"
                    )
                )
            }
        }

        return notifications
    }

    suspend fun sync(
        profiles: List<Profile>,
        pendingVaccines: List<VaccineRecord>,
        checkups: List<CheckupReminder>,
        preferences: NotificationPreferences,
        currentDate: LocalDate,
        isPersian: Boolean = true
    ) {
        scheduler.cancelAll()
        if (!preferences.enabled) return

        val notifications = generateReminders(
            profiles = profiles,
            pendingVaccines = pendingVaccines,
            checkups = checkups,
            preferences = preferences,
            currentDate = currentDate,
            isPersian = isPersian
        )

        for (item in notifications) {
            scheduler.schedule(item)
        }
    }

    private fun getTargetTriggerDays(diffDays: Int, preferences: NotificationPreferences): List<Int> {
        val targets = mutableListOf<Int>()
        if (diffDays == 7 && preferences.notify7DaysBefore) targets.add(7)
        if (diffDays == 1 && preferences.notify1DayBefore) targets.add(1)
        if (diffDays == 0 && preferences.notifyOnDueDate) targets.add(0)
        if (diffDays == -1 && preferences.notifyWhenOverdue) targets.add(-1)
        return targets
    }

    private fun LocalDate.minusDays(days: Int): LocalDate {
        return LocalDate.fromEpochDays(this.toEpochDays() - days)
    }

    private fun calculateScheduledTime(date: LocalDate, hour: Int, minute: Int): Long {
        return try {
            val localDateTime = date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } catch (_: Exception) {
            date.toEpochDays() * 86_400_000L
        }
    }
}
