package ir.salamat.ui.screens.home

import kotlinx.datetime.LocalDate

enum class AlertType(val titleFa: String, val titleEn: String, val iconEmoji: String) {
    VACCINE("واکسیناسیون", "Vaccine", "💉"),
    CHECKUP("چک‌آپ دوره‌ای", "Checkup", "🩺"),
    GROWTH("پایش رشد", "Growth", "📊")
}

enum class AlertSeverity(
    val titleFa: String,
    val titleEn: String,
    val colorHex: Long
) {
    OVERDUE("دارای تأخیر", "Overdue", 0xFFD32F2F),       // Red
    DUE_SOON("موعد رسیده", "Due Soon", 0xFFED6C02),      // Orange / Amber
    UPCOMING("در آینده", "Upcoming", 0xFF0288D1)         // Blue
}

data class FamilyAlertItem(
    val id: String,
    val profileId: String,
    val profileName: String,
    val profileAvatarColor: Int,
    val profileAvatarPhoto: String? = null,
    val isChild: Boolean,
    val title: String,
    val description: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val dueDate: LocalDate,
    val daysDifference: Int, // Negative: days overdue, 0: today, Positive: days remaining
    val actionTextFa: String,
    val actionTextEn: String
)
