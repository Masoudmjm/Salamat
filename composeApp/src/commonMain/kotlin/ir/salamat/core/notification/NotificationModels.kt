package ir.salamat.core.notification

import kotlinx.serialization.Serializable

enum class NotificationChannelType(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val descriptionFa: String,
    val descriptionEn: String
) {
    HEALTH_REMINDERS(
        id = "salamat_health_reminders",
        nameFa = "یادآورهای سلامت خانواده",
        nameEn = "Family Health Reminders",
        descriptionFa = "اعلان‌ها و یادآورهای نوبت‌های واکسیناسیون و چک‌آپ‌های دوره‌ای اعضای خانواده",
        descriptionEn = "Notifications and reminders for childhood immunizations and periodic adult checkups"
    )
}

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val scheduledEpochMillis: Long,
    val profileId: String,
    val channel: NotificationChannelType = NotificationChannelType.HEALTH_REMINDERS,
    val payloadRoute: String? = null
)

@Serializable
data class NotificationPreferences(
    val enabled: Boolean = true,
    val preferredHour: Int = 9,
    val preferredMinute: Int = 0,
    val notify7DaysBefore: Boolean = true,
    val notify1DayBefore: Boolean = true,
    val notifyOnDueDate: Boolean = true,
    val notifyWhenOverdue: Boolean = true
) {
    fun formatTime(): String {
        val h = preferredHour.toString().padStart(2, '0')
        val m = preferredMinute.toString().padStart(2, '0')
        return "$h:$m"
    }
}
