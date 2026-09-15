package ir.salamat.ui.screens.checkup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.datetime.todayLocalDate
import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.data.repository.CheckupRepository
import ir.salamat.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class CheckupViewModel(
    private val profileId: String,
    private val checkupRepository: CheckupRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(CheckupFilter.ALL)
    private val _activeDialogCheckup = MutableStateFlow<CheckupCardUiModel?>(null)
    private val _isAddDialogOpen = MutableStateFlow(false)
    private var hasAttemptedAutoInit = false

    val uiState: StateFlow<CheckupUiState> = combine(
        profileRepository.getProfileById(profileId),
        checkupRepository.getCheckupsForProfile(profileId),
        _selectedFilter,
        _activeDialogCheckup,
        _isAddDialogOpen
    ) { profile, rawCheckups, filter, dialogCheckup, isAddOpen ->
        val today = todayLocalDate()

        // Auto-initialize standard adult checkups if database is empty for this adult
        if (profile != null && profile.type == ProfileType.ADULT && rawCheckups.isEmpty() && !hasAttemptedAutoInit) {
            hasAttemptedAutoInit = true
            viewModelScope.launch {
                checkupRepository.initializeStandardCheckupsForProfile(profile, today)
            }
        }

        val mappedCheckups = rawCheckups.map { reminder ->
            val catalogItem = CheckupCatalog.findByKey(reminder.titleKey)
            val title = catalogItem?.nameFa ?: reminder.titleKey
            val description = catalogItem?.descriptionFa ?: (reminder.notes ?: "")

            val daysUntilDue = reminder.nextDueDate.toEpochDays() - today.toEpochDays()
            val status = when {
                daysUntilDue < 0 -> CheckupStatus.OVERDUE
                daysUntilDue <= 30 -> CheckupStatus.DUE
                reminder.lastCompletedDate != null -> CheckupStatus.COMPLETED
                else -> CheckupStatus.UPCOMING
            }

            CheckupCardUiModel(
                id = reminder.id,
                profileId = reminder.profileId,
                titleKey = reminder.titleKey,
                title = title,
                description = description,
                intervalMonths = reminder.intervalMonths,
                lastCompletedDate = reminder.lastCompletedDate,
                nextDueDate = reminder.nextDueDate,
                status = status,
                daysUntilDue = daysUntilDue.toInt(),
                notes = reminder.notes
            )
        }

        val filtered = when (filter) {
            CheckupFilter.ALL -> mappedCheckups
            CheckupFilter.DUE_OR_OVERDUE -> mappedCheckups.filter {
                it.status == CheckupStatus.DUE || it.status == CheckupStatus.OVERDUE
            }
            CheckupFilter.UPCOMING -> mappedCheckups.filter { it.status == CheckupStatus.UPCOMING }
            CheckupFilter.COMPLETED -> mappedCheckups.filter { it.status == CheckupStatus.COMPLETED }
        }

        CheckupUiState(
            profile = profile,
            checkups = mappedCheckups,
            filteredCheckups = filtered,
            selectedFilter = filter,
            activeDialogCheckup = dialogCheckup,
            isAddDialogOpen = isAddOpen,
            dueCount = mappedCheckups.count { it.status == CheckupStatus.DUE },
            overdueCount = mappedCheckups.count { it.status == CheckupStatus.OVERDUE },
            completedCount = mappedCheckups.count { it.status == CheckupStatus.COMPLETED },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckupUiState()
    )

    fun setFilter(filter: CheckupFilter) {
        _selectedFilter.value = filter
    }

    fun openRecordCompletionDialog(checkup: CheckupCardUiModel?) {
        _activeDialogCheckup.value = checkup
    }

    fun setAddDialogOpen(isOpen: Boolean) {
        _isAddDialogOpen.value = isOpen
    }

    fun recordCheckupCompleted(
        id: String,
        completedDate: LocalDate,
        nextDueDate: LocalDate,
        notes: String?
    ) {
        viewModelScope.launch {
            checkupRepository.updateCheckupCompletion(id, completedDate, nextDueDate, notes)
            _activeDialogCheckup.value = null
        }
    }

    fun addCheckup(
        titleKey: String,
        intervalMonths: Int,
        nextDueDate: LocalDate,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val reminder = CheckupReminder(
                id = "${profileId}_${titleKey}_$now",
                profileId = profileId,
                titleKey = titleKey,
                intervalMonths = intervalMonths,
                lastCompletedDate = null,
                nextDueDate = nextDueDate,
                notes = notes,
                createdAt = now
            )
            checkupRepository.addCheckupReminder(reminder)
            _isAddDialogOpen.value = false
        }
    }

    fun deleteCheckup(id: String) {
        viewModelScope.launch {
            checkupRepository.deleteCheckupReminder(id)
        }
    }
}
