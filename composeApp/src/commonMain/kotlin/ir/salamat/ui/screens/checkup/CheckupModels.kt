package ir.salamat.ui.screens.checkup

import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.checkup.CheckupDefinition
import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.Profile
import kotlinx.datetime.LocalDate

enum class CheckupStatus(
    val titleFa: String,
    val titleEn: String,
    val colorHex: Long
) {
    OVERDUE("دارای تأخیر", "Overdue", 0xFFD32F2F),       // Red
    DUE("موعد فرا رسیده", "Due Soon", 0xFFED6C02),       // Orange / Amber
    UPCOMING("در آینده", "Upcoming", 0xFF0288D1),        // Blue / Info
    COMPLETED("انجام شده", "Completed", 0xFF2E7D32)      // Green
}

enum class CheckupFilter(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    DUE_OR_OVERDUE("نیازمند پیگیری", "Due / Overdue"),
    UPCOMING("موعدهای آینده", "Upcoming"),
    COMPLETED("تکمیل شده", "Completed")
}

data class CheckupCardUiModel(
    val id: String,
    val profileId: String,
    val titleKey: String,
    val title: String,
    val description: String,
    val intervalMonths: Int,
    val lastCompletedDate: LocalDate?,
    val nextDueDate: LocalDate,
    val status: CheckupStatus,
    val daysUntilDue: Int,
    val notes: String?
)

data class CheckupUiState(
    val profile: Profile? = null,
    val checkups: List<CheckupCardUiModel> = emptyList(),
    val filteredCheckups: List<CheckupCardUiModel> = emptyList(),
    val selectedFilter: CheckupFilter = CheckupFilter.ALL,
    val activeDialogCheckup: CheckupCardUiModel? = null,
    val isAddDialogOpen: Boolean = false,
    val dueCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0,
    val isLoading: Boolean = true
)
