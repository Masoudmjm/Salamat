package ir.salamat.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ir.salamat.core.database.DatabaseProvider
import ir.salamat.core.model.CheckupReminder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

interface CheckupRepository {
    fun getCheckupsForProfile(profileId: String): Flow<List<CheckupReminder>>
    suspend fun initializeStandardCheckupsForAdult(profileId: String, currentDate: LocalDate)
    suspend fun updateCheckupCompletion(id: String, completedDate: LocalDate, nextDueDate: LocalDate, notes: String?)
    suspend fun addCheckupReminder(reminder: CheckupReminder)
    suspend fun deleteCheckupReminder(id: String)
    suspend fun deleteCheckupsForProfile(profileId: String)
}

class CheckupRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CheckupRepository {

    override fun getCheckupsForProfile(profileId: String): Flow<List<CheckupReminder>> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectCheckupsForProfile(profileId)
                .asFlow()
                .mapToList(dispatcher)
                .map { list ->
                    list.map { entity ->
                        CheckupReminder(
                            id = entity.id,
                            profileId = entity.profile_id,
                            titleKey = entity.title_key,
                            intervalMonths = entity.interval_months.toInt(),
                            lastCompletedDate = entity.last_completed_date?.let { LocalDate.parse(it) },
                            nextDueDate = LocalDate.parse(entity.next_due_date),
                            notes = entity.notes,
                            createdAt = entity.created_at
                        )
                    }
                }
        )
    }

    override suspend fun initializeStandardCheckupsForAdult(profileId: String, currentDate: LocalDate) {
        val db = databaseProvider.getDatabase()
        val now = Clock.System.now().toEpochMilliseconds()
        val templates = listOf(
            Triple("blood_pressure", 6, currentDate.plus(1, DateTimeUnit.MONTH)),
            Triple("annual_blood_test", 12, currentDate.plus(3, DateTimeUnit.MONTH)),
            Triple("dental", 6, currentDate.plus(2, DateTimeUnit.MONTH)),
            Triple("eye_checkup", 24, currentDate.plus(6, DateTimeUnit.MONTH))
        )

        templates.forEachIndexed { index, (key, intervalMonths, nextDue) ->
            val id = "${profileId}_${key}_${index}"
            db.salamatDatabaseQueries.insertCheckupReminder(
                id = id,
                profile_id = profileId,
                title_key = key,
                interval_months = intervalMonths.toLong(),
                last_completed_date = null,
                next_due_date = nextDue.toString(),
                notes = null,
                created_at = now + index
            )
        }
    }

    override suspend fun updateCheckupCompletion(
        id: String,
        completedDate: LocalDate,
        nextDueDate: LocalDate,
        notes: String?
    ) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.updateCheckupCompletion(
            last_completed_date = completedDate.toString(),
            next_due_date = nextDueDate.toString(),
            notes = notes,
            id = id
        )
    }

    override suspend fun addCheckupReminder(reminder: CheckupReminder) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.insertCheckupReminder(
            id = reminder.id,
            profile_id = reminder.profileId,
            title_key = reminder.titleKey,
            interval_months = reminder.intervalMonths.toLong(),
            last_completed_date = reminder.lastCompletedDate?.toString(),
            next_due_date = reminder.nextDueDate.toString(),
            notes = reminder.notes,
            created_at = reminder.createdAt
        )
    }

    override suspend fun deleteCheckupReminder(id: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteCheckupReminder(id)
    }

    override suspend fun deleteCheckupsForProfile(profileId: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteCheckupsForProfile(profileId)
    }
}
