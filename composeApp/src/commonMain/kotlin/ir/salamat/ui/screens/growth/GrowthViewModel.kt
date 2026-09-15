package ir.salamat.ui.screens.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.salamat.core.model.GrowthRecord
import ir.salamat.data.repository.GrowthRepository
import ir.salamat.data.repository.ProfileRepository
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class GrowthViewModel(
    private val profileId: String,
    private val profileRepository: ProfileRepository,
    private val growthRepository: GrowthRepository
) : ViewModel() {

    private val _isAddDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<GrowthUiState> = combine(
        profileRepository.getProfileById(profileId),
        growthRepository.getGrowthRecordsForProfile(profileId),
        _isAddDialogOpen
    ) { profile, records, isAddDialogOpen ->
        val sortedRecords = records.sortedWith(
            compareByDescending<GrowthRecord> { it.date }
                .thenByDescending { it.createdAt }
        )
        val latest = sortedRecords.firstOrNull()
        val bmiCategory = latest?.let { BmiCalculator.getBmiCategory(it.bmi) }
        val idealWeightRange = latest?.let { BmiCalculator.calculateIdealWeightRange(it.heightCm) }

        GrowthUiState(
            profile = profile,
            records = sortedRecords,
            latestRecord = latest,
            bmiCategory = bmiCategory,
            idealWeightRange = idealWeightRange,
            isAddDialogOpen = isAddDialogOpen,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GrowthUiState(isLoading = true)
    )

    fun setAddDialogOpen(isOpen: Boolean) {
        _isAddDialogOpen.value = isOpen
    }

    fun addRecord(
        date: LocalDate,
        weightKg: Double,
        heightCm: Double,
        headCircumferenceCm: Double?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val id = "gr_${now}_${Random.nextInt(1000, 9999)}"
            val bmi = BmiCalculator.calculateBmi(weightKg, heightCm)

            val record = GrowthRecord(
                id = id,
                profileId = profileId,
                date = date,
                weightKg = weightKg,
                heightCm = heightCm,
                headCircumferenceCm = headCircumferenceCm,
                bmi = bmi,
                notes = notes?.trim()?.ifEmpty { null },
                createdAt = now
            )
            growthRepository.addGrowthRecord(record)
            _isAddDialogOpen.value = false
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            growthRepository.deleteGrowthRecord(id)
        }
    }
}
