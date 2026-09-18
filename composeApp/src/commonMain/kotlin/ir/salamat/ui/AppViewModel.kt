package ir.salamat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.datetime.todayLocalDate
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.notification.NotificationItem
import ir.salamat.core.notification.NotificationPreferences
import ir.salamat.core.notification.NotificationScheduler
import ir.salamat.core.notification.ReminderSyncEngine
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
import kotlinx.coroutines.flow.firstOrNull
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
    val isLoading: Boolean = true,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val hasNotificationPermission: Boolean = false,
    val testNotificationMessage: String? = null
)

class AppViewModel(
    private val profileRepository: ProfileRepository,
    private val vaccineRepository: VaccineRepository,
    private val checkupRepository: CheckupRepository,
    private val notificationScheduler: NotificationScheduler,
    private val reminderSyncEngine: ReminderSyncEngine
) : ViewModel() {

    private val _activeProfileId = MutableStateFlow<String?>(null)
    private val _isPersian = MutableStateFlow(true)
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    private val _hasNotificationPermission = MutableStateFlow(false)
    private val _testNotificationMessage = MutableStateFlow<String?>(null)

    init {
        checkNotificationPermission()
    }

    private data class NotificationSettingsState(
        val preferences: NotificationPreferences,
        val hasPermission: Boolean,
        val testMessage: String?
    )

    private val _notificationSettingsState = combine(
        _notificationPreferences,
        _hasNotificationPermission,
        _testNotificationMessage
    ) { prefs, hasPermission, testMsg ->
        NotificationSettingsState(prefs, hasPermission, testMsg)
    }

    val uiState: StateFlow<AppUiState> = combine(
        profileRepository.getAllProfiles(),
        vaccineRepository.getAllPendingVaccines(),
        checkupRepository.getAllCheckups(),
        combine(_activeProfileId, _isPersian) { id, lang -> Pair(id, lang) },
        _notificationSettingsState
    ) { profiles, pendingVaccines, checkups, (activeId, isPersian), notifSettings ->
        val notificationPreferences = notifSettings.preferences
        val hasPermission = notifSettings.hasPermission
        val testMessage = notifSettings.testMessage

        val active = profiles.find { it.id == activeId } ?: profiles.firstOrNull()
        val today = todayLocalDate()
        val profileMap = profiles.associateBy { it.id }

        // Sync local notifications with current state
        viewModelScope.launch {
            reminderSyncEngine.sync(
                profiles = profiles,
                pendingVaccines = pendingVaccines,
                checkups = checkups,
                preferences = notificationPreferences,
                currentDate = today,
                isPersian = isPersian
            )
        }

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
                        id = "vaccine_${record.id}",
                        profileId = profile.id,
                        profileName = profile.name,
                        profileAvatarColor = profile.avatarColor,
                        profileAvatarPhoto = profile.avatarPhoto,
                        isChild = profile.type == ProfileType.CHILD,
                        title = title,
                        type = AlertType.VACCINE,
                        severity = severity,
                        dueDate = dueDate,
                        daysDifference = diffDays,
                        description = desc,
                        actionTextFa = actionFa,
                        actionTextEn = actionEn
                    )
                )
            }
        }

        val checkupAlerts = mutableListOf<FamilyAlertItem>()
        for (checkup in checkups) {
            val profile = profileMap[checkup.profileId] ?: continue
            val dueDate = checkup.nextDueDate
            val diffDays = (dueDate.toEpochDays() - today.toEpochDays()).toInt()

            if (diffDays <= 30) {
                val severity = if (diffDays < 0) AlertSeverity.OVERDUE else AlertSeverity.DUE_SOON
                val def = CheckupCatalog.findByKey(checkup.titleKey)
                val title = if (isPersian) (def?.nameFa ?: checkup.titleKey) else (def?.nameEn ?: checkup.titleKey)
                val desc = when {
                    diffDays < 0 -> if (isPersian) "${abs(diffDays)} روز تأخیر در پیگیری" else "${abs(diffDays)} days overdue"
                    diffDays == 0 -> if (isPersian) "موعد پیگیری امروز است" else "Due today"
                    else -> if (isPersian) "${diffDays} روز تا موعد پیگیری" else "Due in $diffDays days"
                }
                val actionFa = "ثبت انجام چک‌آپ"
                val actionEn = "Record Checkup"

                checkupAlerts.add(
                    FamilyAlertItem(
                        id = "checkup_${checkup.id}",
                        profileId = profile.id,
                        profileName = profile.name,
                        profileAvatarColor = profile.avatarColor,
                        profileAvatarPhoto = profile.avatarPhoto,
                        isChild = profile.type == ProfileType.CHILD,
                        title = title,
                        type = AlertType.CHECKUP,
                        severity = severity,
                        dueDate = dueDate,
                        daysDifference = diffDays,
                        description = desc,
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
            isLoading = false,
            notificationPreferences = notificationPreferences,
            hasNotificationPermission = hasPermission,
            testNotificationMessage = testMessage
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

    fun checkNotificationPermission() {
        viewModelScope.launch {
            _hasNotificationPermission.value = notificationScheduler.hasPermission()
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            val granted = notificationScheduler.requestPermission()
            _hasNotificationPermission.value = granted
        }
    }

    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        _notificationPreferences.value = preferences
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val hasPerm = notificationScheduler.hasPermission()
            _hasNotificationPermission.value = hasPerm
            val isFa = _isPersian.value
            if (!hasPerm) {
                _testNotificationMessage.value = if (isFa)
                    "⚠️ دسترسی اعلان‌های سیستم غیرفعال است. لطفاً ابتدا در تنظیمات دستگاه دسترسی را فعال کنید."
                else
                    "⚠️ System notifications are disabled. Please enable notification permission in device settings first."
                return@launch
            }
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val item = NotificationItem(
                id = "test_$now",
                title = if (isFa) "سلامت: اعلان آزمایشی یادآور" else "Salamat: Test Reminder",
                body = if (isFa)
                    "سیستم یادآورهای واکسن و چک‌آپ خانواده فعال و آماده است."
                else
                    "Family health notification system is active and synced.",
                scheduledEpochMillis = now,
                profileId = _activeProfileId.value ?: "default"
            )
            notificationScheduler.showImmediateNotification(item)
            _testNotificationMessage.value = if (isFa)
                "اعلان آزمایشی با موفقیت ارسال شد."
            else
                "Test notification sent successfully."
        }
    }

    fun clearTestNotificationMessage() {
        _testNotificationMessage.value = null
    }

    fun addProfile(
        name: String,
        birthDate: LocalDate,
        gender: ir.salamat.core.model.Gender,
        type: ProfileType,
        avatarColor: Int,
        avatarPhoto: String? = null,
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
                avatarPhoto = avatarPhoto,
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

    fun updateProfile(
        id: String,
        name: String,
        birthDate: LocalDate,
        gender: ir.salamat.core.model.Gender,
        type: ProfileType,
        avatarColor: Int,
        avatarPhoto: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = profileRepository.getProfileById(id).firstOrNull() ?: return@launch
            val updated = existing.copy(
                name = name,
                birthDate = birthDate,
                gender = gender,
                type = type,
                avatarColor = avatarColor,
                avatarPhoto = avatarPhoto
            )
            profileRepository.saveProfile(updated)
            onSuccess()
        }
    }

    fun deleteProfile(
        profileId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
            if (_activeProfileId.value == profileId) {
                val remaining = profileRepository.getAllProfiles().firstOrNull()?.filter { it.id != profileId }
                _activeProfileId.value = remaining?.firstOrNull()?.id
            }
            onSuccess()
        }
    }
}
