package ir.salamat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.model.Profile
import ir.salamat.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val isPersian: Boolean = true,
    val isLoading: Boolean = true
)

class AppViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _activeProfileId = MutableStateFlow<String?>(null)
    private val _isPersian = MutableStateFlow(true)

    val uiState: StateFlow<AppUiState> = combine(
        profileRepository.getAllProfiles(),
        _activeProfileId,
        _isPersian
    ) { profiles, activeId, isPersian ->
        val active = profiles.find { it.id == activeId } ?: profiles.firstOrNull()
        AppUiState(
            profiles = profiles,
            activeProfile = active,
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
        birthDate: kotlinx.datetime.LocalDate,
        gender: ir.salamat.core.model.Gender,
        type: ir.salamat.core.model.ProfileType,
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
            _activeProfileId.value = id
            onSuccess(id)
        }
    }
}
