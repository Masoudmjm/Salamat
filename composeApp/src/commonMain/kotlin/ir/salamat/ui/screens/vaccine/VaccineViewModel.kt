package ir.salamat.ui.screens.vaccine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.datetime.todayLocalDate
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineStatus
import ir.salamat.data.repository.ProfileRepository
import ir.salamat.data.repository.VaccineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

enum class VaccineFilter {
    ALL,
    DUE_OR_OVERDUE,
    COMPLETED,
    UPCOMING
}

data class VaccineUiState(
    val profile: Profile? = null,
    val milestones: List<VaccineMilestoneUiModel> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val selectedFilter: VaccineFilter = VaccineFilter.ALL,
    val activeDialogVaccine: VaccineCardUiModel? = null,
    val isLoading: Boolean = true
)

class VaccineViewModel(
    private val profileId: String,
    private val profileRepository: ProfileRepository,
    private val vaccineRepository: VaccineRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(VaccineFilter.ALL)
    private val _activeDialogVaccine = MutableStateFlow<VaccineCardUiModel?>(null)
    private var hasInitialized = false

    val uiState: StateFlow<VaccineUiState> = combine(
        profileRepository.getProfileById(profileId),
        vaccineRepository.getVaccinesForProfile(profileId),
        _selectedFilter,
        _activeDialogVaccine
    ) { profile, records, filter, dialogVaccine ->
        val today = todayLocalDate()

        // Auto-initialize vaccines for child if not populated yet
        if (profile != null && profile.type == ProfileType.CHILD && records.isEmpty() && !hasInitialized) {
            hasInitialized = true
            viewModelScope.launch {
                vaccineRepository.initializeVaccinesForProfile(profileId, profile.birthDate, today)
            }
        }

        val allMilestones = if (profile != null) {
            VaccineUiMapper.mapToMilestones(
                birthDate = profile.birthDate,
                records = records,
                currentDate = today
            )
        } else {
            emptyList()
        }

        val allVaccines = allMilestones.flatMap { it.vaccines }
        val totalCount = allVaccines.size
        val completedCount = allVaccines.count { it.status == VaccineStatus.COMPLETED }
        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

        // Apply filter
        val filteredMilestones = when (filter) {
            VaccineFilter.ALL -> allMilestones
            VaccineFilter.DUE_OR_OVERDUE -> allMilestones.mapNotNull { m ->
                val filtered = m.vaccines.filter { it.status == VaccineStatus.DUE || it.status == VaccineStatus.OVERDUE }
                if (filtered.isNotEmpty()) m.copy(vaccines = filtered) else null
            }
            VaccineFilter.COMPLETED -> allMilestones.mapNotNull { m ->
                val filtered = m.vaccines.filter { it.status == VaccineStatus.COMPLETED }
                if (filtered.isNotEmpty()) m.copy(vaccines = filtered) else null
            }
            VaccineFilter.UPCOMING -> allMilestones.mapNotNull { m ->
                val filtered = m.vaccines.filter { it.status == VaccineStatus.UPCOMING }
                if (filtered.isNotEmpty()) m.copy(vaccines = filtered) else null
            }
        }

        VaccineUiState(
            profile = profile,
            milestones = filteredMilestones,
            completedCount = completedCount,
            totalCount = totalCount,
            progress = progress,
            selectedFilter = filter,
            activeDialogVaccine = dialogVaccine,
            isLoading = profile == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaccineUiState()
    )

    fun setFilter(filter: VaccineFilter) {
        _selectedFilter.value = filter
    }

    fun openDialog(vaccine: VaccineCardUiModel?) {
        _activeDialogVaccine.value = vaccine
    }

    fun markAdministered(recordId: String, administeredDate: LocalDate, notes: String?) {
        viewModelScope.launch {
            vaccineRepository.updateVaccineRecord(
                id = recordId,
                status = VaccineStatus.COMPLETED,
                administeredDate = administeredDate,
                notes = notes
            )
            _activeDialogVaccine.value = null
        }
    }

    fun markPending(recordId: String) {
        viewModelScope.launch {
            val profile = uiState.value.profile ?: return@launch
            val rec = uiState.value.milestones.flatMap { it.vaccines }.find { it.record.id == recordId }?.record
            val targetAge = rec?.targetAgeMonths ?: 0
            val computedStatus = ir.salamat.core.vaccine.IranVaccineSchedule.computeStatus(
                targetAgeMonths = targetAge,
                birthDate = profile.birthDate,
                administeredDate = null,
                currentDate = todayLocalDate()
            )
            vaccineRepository.updateVaccineRecord(
                id = recordId,
                status = computedStatus,
                administeredDate = null,
                notes = null
            )
            _activeDialogVaccine.value = null
        }
    }
}
