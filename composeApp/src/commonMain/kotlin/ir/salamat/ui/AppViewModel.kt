package ir.salamat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.datetime.todayLocalDate
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.data.repository.CheckupRepository
import ir.salamat.data.repository.ProfileRepository
import ir.salamat.data.repository.VaccineRepository
import ir.salamat.ui.screens.home.AlertSeverity
import ir.salamat.ui.screens.home.AlertType
import ir.salamat.ui.screens.home.FamilyAlertItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.abs

data class AppUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val alerts: List<FamilyAlertItem> = emptyList(),
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val isPersian: Boolean = true,
    val isLoading: Boolean = true
)

class AppViewModel(
    private val profileRepository: ProfileRepository,
    private val vaccineRepository: VaccineRepository,
    private val checkupRepository: CheckupRepository
) : ViewModel() {

    private val _activeProfileId = MutableStateFlow<String?>(null)
    private val _isPersian = MutableStateFlow(true)

    val uiState: StateFlow<AppUiState> = combine(
        profileRepository.getAllProfiles(),
        vaccineRepository.getAllPendingVaccines(),
        checkupRepository.getAllCheckups(),
        _activeProfileId,
        _isPersian
    ) { profiles, pendingVaccines, checkups, activeId, isPersian ->
        val active = profiles.find { it.id == activeId } ?: profiles.firstOrNull()
        val today = todayLocalDate()
        val profileMap = profiles.associateBy { it.id }

        val vaccineAlerts = mutableListOf<FamilyAlertItem>()
        for (record in pendingVaccines) {
            val profile = profileMap[record.profileId] ?: continue
            val dueDate = IranVaccineSchedule.calculateDueDate(profile.birthDate, record.targetAgeMonths)
            val diffDays = (dueDate.toEpochDays() - today.toEpochDays()).toInt()

            if (diffDays <= 30) {
                val severity = if (diffDays < 0) AlertSeverity.OVERDUE else AlertSeverity.DUE_SOON
                val def = IranVaccineSchedule.getByCode(record.vaccineCode)
                val title = if (isPersian) (def?.nameFa ?: record.vaccineCode) else (def?.nameEn ?: record.vaccineCode)
                val desc = when {
                    diffDays < 0 -> if (isPersian) "${abs(diffDays)} روز تأخیر در تزریق" else "${abs(diffDays)} days overdue"
                    diffDays == 0 -> if (isPersian) "موعد تزریق امروز است" else "Due today"
                    else -> if (isPersian) "${diffDays} روز تا موعد تزریق" else "Due in $diffDays days"
                }
                val actionFa = "ثبت واکسیناسیون"
                val actionEn = "Record Vaccine"

                vaccineAlerts.add(
                    FamilyAlertItem(
                        id = "alert_vac_${record.id}",
                        profileId = profile.id,
                        profileName = profile.name,
                        profileAvatarColor = profile.avatarColor,
                        isChild = profile.type == ProfileType.CHILD,
                        title = title,
                        description = desc,
                        type = AlertType.VACCINE,
                        severity = severity,
                        dueDate = dueDate,
                        daysDifference = diffDays,
                        actionTextFa = actionFa,
                        actionTextEn = actionEn
                    )
                )
            }
        }

        val checkupAlerts = mutableListOf<FamilyAlertItem>()
        for (reminder in checkups) {
            val profile = profileMap[reminder.profileId] ?: continue
            val diffDays = (reminder.nextDueDate.toEpochDays() - today.toEpochDays()).toInt()

            if (diffDays <= 30) {
                val severity = if (diffDays < 0) AlertSeverity.OVERDUE else AlertSeverity.DUE_SOON
                val item = CheckupCatalog.findByKey(reminder.titleKey)
                val title = if (isPersian) (item?.nameFa ?: reminder.titleKey) else (item?.nameEn ?: reminder.titleKey)
                val desc = when {
                    diffDays < 0 -> if (isPersian) "${abs(diffDays)} روز تأخیر در مراجعه" else "${abs(diffDays)} days overdue"
                    diffDays == 0 -> if (isPersian) "موعد مراجعه امروز است" else "Due today"
                    else -> if (isPersian) "${diffDays} روز تا موعد مراجعه" else "Due in $diffDays days"
                }
                val actionFa = "ثبت چک‌آپ"
                val actionEn = "Record Checkup"

                checkupAlerts.add(
                    FamilyAlertItem(
                        id = "alert_chk_${reminder.id}",
                        profileId = profile.id,
                        profileName = profile.name,
                        profileAvatarColor = profile.avatarColor,
                        isChild = profile.type == ProfileType.CHILD,
                        title = title,
                        description = desc,
                        type = AlertType.CHECKUP,
                        severity = severity,
                        dueDate = reminder.nextDueDate,
                        daysDifference = diffDays,
                        actionTextFa = actionFa,
                        actionTextEn = actionEn
                    )
                )
            }
        }

        val allAlerts = (vaccineAlerts + checkupAlerts).sortedWith(
            compareBy<FamilyAlertItem> { it.severity.ordinal }
                .thenBy { it.daysDifference }
        )

        val overdue = allAlerts.count { it.severity == AlertSeverity.OVERDUE }
        val dueSoon = allAlerts.count { it.severity == AlertSeverity.DUE_SOON }

        AppUiState(
            profiles = profiles,
            activeProfile = active,
            alerts = allAlerts,
            overdueCount = overdue,
            dueSoonCount = dueSoon,
            isPersian = isPersian,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    fun selectProfile(profileId: String) {
        _activeProfileId.value = profileId
    }

    fun toggleLanguage() {
        _isPersian.value = !_isPersian.value
    }

    fun setLanguage(isPersian: Boolean) {
        _isPersian.value = isPersian
    }

    fun addProfile(
        name: String,
        birthDate: LocalDate,
        gender: ir.salamat.core.model.Gender,
        type: ProfileType,
        avatarColor: Int,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val id = "profile_$now"
            val profile = Profile(
                id = id,
                name = name,
                birthDate = birthDate,
                gender = gender,
                type = type,
                avatarColor = avatarColor,
                createdAt = now
            )
            profileRepository.saveProfile(profile)
            val today = todayLocalDate()
            if (type == ProfileType.CHILD) {
                vaccineRepository.initializeVaccinesForProfile(id, birthDate, today)
            } else {
                checkupRepository.initializeStandardCheckupsForProfile(profile, today)
            }
            _activeProfileId.value = id
            onSuccess(id)
        }
    }
}
