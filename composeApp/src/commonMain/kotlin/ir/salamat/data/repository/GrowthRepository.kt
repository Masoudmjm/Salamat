package ir.salamat.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import ir.salamat.core.database.DatabaseProvider
import ir.salamat.core.model.GrowthRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

interface GrowthRepository {
    fun getGrowthRecordsForProfile(profileId: String): Flow<List<GrowthRecord>>
    fun getLatestGrowthRecord(profileId: String): Flow<GrowthRecord?>
    suspend fun addGrowthRecord(record: GrowthRecord)
    suspend fun deleteGrowthRecord(id: String)
    suspend fun deleteGrowthRecordsForProfile(profileId: String)
}

class GrowthRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : GrowthRepository {

    override fun getGrowthRecordsForProfile(profileId: String): Flow<List<GrowthRecord>> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectGrowthRecordsForProfile(profileId)
                .asFlow()
                .mapToList(dispatcher)
                .map { list ->
                    list.map { entity ->
                        GrowthRecord(
                            id = entity.id,
                            profileId = entity.profile_id,
                            date = LocalDate.parse(entity.date),
                            weightKg = entity.weight_kg,
                            heightCm = entity.height_cm,
                            headCircumferenceCm = entity.head_circumference_cm,
                            bmi = entity.bmi,
                            notes = entity.notes,
                            createdAt = entity.created_at
                        )
                    }
                }
        )
    }

    override fun getLatestGrowthRecord(profileId: String): Flow<GrowthRecord?> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectLatestGrowthRecord(profileId)
                .asFlow()
                .mapToOneOrNull(dispatcher)
                .map { entity ->
                    entity?.let {
                        GrowthRecord(
                            id = it.id,
                            profileId = it.profile_id,
                            date = LocalDate.parse(it.date),
                            weightKg = it.weight_kg,
                            heightCm = it.height_cm,
                            headCircumferenceCm = it.head_circumference_cm,
                            bmi = it.bmi,
                            notes = it.notes,
                            createdAt = it.created_at
                        )
                    }
                }
        )
    }

    override suspend fun addGrowthRecord(record: GrowthRecord) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.insertGrowthRecord(
            id = record.id,
            profile_id = record.profileId,
            date = record.date.toString(),
            weight_kg = record.weightKg,
            height_cm = record.heightCm,
            head_circumference_cm = record.headCircumferenceCm,
            bmi = record.bmi,
            notes = record.notes,
            created_at = record.createdAt
        )
    }

    override suspend fun deleteGrowthRecord(id: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteGrowthRecord(id)
    }

    override suspend fun deleteGrowthRecordsForProfile(profileId: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteGrowthRecordsForProfile(profileId)
    }
}
