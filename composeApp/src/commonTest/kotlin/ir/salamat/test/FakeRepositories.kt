package ir.salamat.test

import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.model.CheckupReminder
import ir.salamat.core.model.GrowthRecord
import ir.salamat.core.model.Profile
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.data.repository.CheckupRepository
import ir.salamat.data.repository.GrowthRepository
import ir.salamat.data.repository.ProfileRepository
import ir.salamat.data.repository.VaccineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class FakeProfileRepository : ProfileRepository {
    val profilesFlow = MutableStateFlow<List<Profile>>(emptyList())
    var onDeleteProfileCallback: (suspend (String) -> Unit)? = null

    override fun getAllProfiles(): Flow<List<Profile>> = profilesFlow

    override fun getProfileById(id: String): Flow<Profile?> =
        profilesFlow.map { list -> list.find { it.id == id } }

    override suspend fun saveProfile(profile: Profile) {
        profilesFlow.update { list ->
            val index = list.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                list.toMutableList().apply { set(index, profile) }
            } else {
                list + profile
            }
        }
    }

    override suspend fun deleteProfile(id: String) {
        onDeleteProfileCallback?.invoke(id)
        profilesFlow.update { list -> list.filter { it.id != id } }
    }
}

class FakeVaccineRepository : VaccineRepository {
    val vaccinesFlow = MutableStateFlow<List<VaccineRecord>>(emptyList())

    override fun getVaccinesForProfile(profileId: String): Flow<List<VaccineRecord>> =
        vaccinesFlow.map { list -> list.filter { it.profileId == profileId } }

    override fun getAllPendingVaccines(): Flow<List<VaccineRecord>> =
        vaccinesFlow.map { list -> list.filter { it.status != VaccineStatus.COMPLETED } }

    override suspend fun initializeVaccinesForProfile(
        profileId: String,
        birthDate: LocalDate,
        currentDate: LocalDate
    ) {
        val newRecords = IranVaccineSchedule.ALL_VACCINES.mapIndexed { index, def ->
            val status = IranVaccineSchedule.computeStatus(
                targetAgeMonths = def.targetAgeMonths,
                birthDate = birthDate,
                administeredDate = null,
                currentDate = currentDate
            )
            VaccineRecord(
                id = "v_${profileId}_$index",
                profileId = profileId,
                vaccineCode = def.code,
                targetAgeMonths = def.targetAgeMonths,
                status = status,
                administeredDate = null,
                notes = null,
                createdAt = index.toLong()
            )
        }
        vaccinesFlow.update { it + newRecords }
    }

    override suspend fun updateVaccineRecord(
        id: String,
        status: VaccineStatus,
        administeredDate: LocalDate?,
        notes: String?
    ) {
        vaccinesFlow.update { list ->
            list.map {
                if (it.id == id) it.copy(status = status, administeredDate = administeredDate, notes = notes)
                else it
            }
        }
    }

    override suspend fun deleteVaccinesForProfile(profileId: String) {
        vaccinesFlow.update { list -> list.filter { it.profileId != profileId } }
    }
}

class FakeGrowthRepository : GrowthRepository {
    val recordsFlow = MutableStateFlow<List<GrowthRecord>>(emptyList())

    override fun getGrowthRecordsForProfile(profileId: String): Flow<List<GrowthRecord>> =
        recordsFlow.map { list -> list.filter { it.profileId == profileId } }

    override fun getLatestGrowthRecord(profileId: String): Flow<GrowthRecord?> =
        recordsFlow.map { list -> list.filter { it.profileId == profileId }.maxByOrNull { it.date } }

    override suspend fun addGrowthRecord(record: GrowthRecord) {
        recordsFlow.update { it + record }
    }

    override suspend fun deleteGrowthRecord(id: String) {
        recordsFlow.update { list -> list.filter { it.id != id } }
    }

    override suspend fun deleteGrowthRecordsForProfile(profileId: String) {
        recordsFlow.update { list -> list.filter { it.profileId != profileId } }
    }
}

class FakeCheckupRepository : CheckupRepository {
    val checkupsFlow = MutableStateFlow<List<CheckupReminder>>(emptyList())

    override fun getCheckupsForProfile(profileId: String): Flow<List<CheckupReminder>> =
        checkupsFlow.map { list -> list.filter { it.profileId == profileId } }

    override fun getAllCheckups(): Flow<List<CheckupReminder>> = checkupsFlow

    override suspend fun initializeStandardCheckupsForProfile(
        profile: Profile,
        currentDate: LocalDate
    ) {
        val eligible = CheckupCatalog.getEligibleCheckups(profile, currentDate)
        val newReminders = eligible.mapIndexed { index, def ->
            CheckupReminder(
                id = "chk_${profile.id}_$index",
                profileId = profile.id,
                titleKey = def.key,
                intervalMonths = def.defaultIntervalMonths,
                lastCompletedDate = null,
                nextDueDate = currentDate.plus(def.defaultIntervalMonths, DateTimeUnit.MONTH),
                notes = def.descriptionFa,
                createdAt = index.toLong()
            )
        }
        checkupsFlow.update { it + newReminders }
    }

    override suspend fun initializeStandardCheckupsForAdult(
        profileId: String,
        currentDate: LocalDate
    ) {
        // Fallback
    }

    override suspend fun updateCheckupCompletion(
        id: String,
        completedDate: LocalDate,
        nextDueDate: LocalDate,
        notes: String?
    ) {
        checkupsFlow.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    lastCompletedDate = completedDate,
                    nextDueDate = nextDueDate,
                    notes = notes
                ) else it
            }
        }
    }

    override suspend fun addCheckupReminder(reminder: CheckupReminder) {
        checkupsFlow.update { it + reminder }
    }

    override suspend fun deleteCheckupReminder(id: String) {
        checkupsFlow.update { list -> list.filter { it.id != id } }
    }

    override suspend fun deleteCheckupsForProfile(profileId: String) {
        checkupsFlow.update { list -> list.filter { it.profileId != profileId } }
    }
}
