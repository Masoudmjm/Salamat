package ir.salamat.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ir.salamat.core.database.DatabaseProvider
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.vaccine.IranVaccineSchedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

interface VaccineRepository {
    fun getVaccinesForProfile(profileId: String): Flow<List<VaccineRecord>>
    fun getAllPendingVaccines(): Flow<List<VaccineRecord>>
    suspend fun initializeVaccinesForProfile(profileId: String, birthDate: LocalDate, currentDate: LocalDate)
    suspend fun updateVaccineRecord(id: String, status: VaccineStatus, administeredDate: LocalDate?, notes: String?)
    suspend fun deleteVaccinesForProfile(profileId: String)
}

class VaccineRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : VaccineRepository {

    override fun getVaccinesForProfile(profileId: String): Flow<List<VaccineRecord>> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectVaccinesForProfile(profileId)
                .asFlow()
                .mapToList(dispatcher)
                .map { list ->
                    list.map { entity ->
                        VaccineRecord(
                            id = entity.id,
                            profileId = entity.profile_id,
                            vaccineCode = entity.vaccine_code,
                            targetAgeMonths = entity.target_age_months.toInt(),
                            status = VaccineStatus.valueOf(entity.status),
                            administeredDate = entity.administered_date?.let { LocalDate.parse(it) },
                            notes = entity.notes,
                            createdAt = entity.created_at
                        )
                    }
                }
        )
    }

    override fun getAllPendingVaccines(): Flow<List<VaccineRecord>> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectAllPendingVaccines()
                .asFlow()
                .mapToList(dispatcher)
                .map { list ->
                    list.map { entity ->
                        VaccineRecord(
                            id = entity.id,
                            profileId = entity.profile_id,
                            vaccineCode = entity.vaccine_code,
                            targetAgeMonths = entity.target_age_months.toInt(),
                            status = VaccineStatus.valueOf(entity.status),
                            administeredDate = entity.administered_date?.let { LocalDate.parse(it) },
                            notes = entity.notes,
                            createdAt = entity.created_at
                        )
                    }
                }
        )
    }

    override suspend fun initializeVaccinesForProfile(
        profileId: String,
        birthDate: LocalDate,
        currentDate: LocalDate
    ) {
        val db = databaseProvider.getDatabase()
        val now = Clock.System.now().toEpochMilliseconds()
        IranVaccineSchedule.ALL_VACCINES.forEachIndexed { index, def ->
            val status = IranVaccineSchedule.computeStatus(
                targetAgeMonths = def.targetAgeMonths,
                birthDate = birthDate,
                administeredDate = null,
                currentDate = currentDate
            )
            val recordId = "${profileId}_${def.code}_${index}"
            db.salamatDatabaseQueries.insertVaccineRecord(
                id = recordId,
                profile_id = profileId,
                vaccine_code = def.code,
                target_age_months = def.targetAgeMonths.toLong(),
                status = status.name,
                administered_date = null,
                notes = null,
                created_at = now + index
            )
        }
    }

    override suspend fun updateVaccineRecord(
        id: String,
        status: VaccineStatus,
        administeredDate: LocalDate?,
        notes: String?
    ) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.updateVaccineStatus(
            status = status.name,
            administered_date = administeredDate?.toString(),
            notes = notes,
            id = id
        )
    }

    override suspend fun deleteVaccinesForProfile(profileId: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteVaccinesForProfile(profileId)
    }
}
