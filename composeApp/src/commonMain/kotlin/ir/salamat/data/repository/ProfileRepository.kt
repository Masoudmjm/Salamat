package ir.salamat.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import ir.salamat.core.database.DatabaseProvider
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<Profile>>
    fun getProfileById(id: String): Flow<Profile?>
    suspend fun saveProfile(profile: Profile)
    suspend fun deleteProfile(id: String)
}

class ProfileRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<Profile>> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectAllProfiles()
                .asFlow()
                .mapToList(dispatcher)
                .map { list ->
                    list.map { entity ->
                        Profile(
                            id = entity.id,
                            name = entity.name,
                            birthDate = LocalDate.parse(entity.birth_date),
                            gender = Gender.valueOf(entity.gender),
                            type = ProfileType.valueOf(entity.type),
                            avatarColor = entity.avatar_color.toInt(),
                            avatarPhoto = entity.avatar_photo,
                            createdAt = entity.created_at
                        )
                    }
                }
        )
    }

    override fun getProfileById(id: String): Flow<Profile?> = flow {
        val db = databaseProvider.getDatabase()
        emitAll(
            db.salamatDatabaseQueries.selectProfileById(id)
                .asFlow()
                .mapToOneOrNull(dispatcher)
                .map { entity ->
                    entity?.let {
                        Profile(
                            id = it.id,
                            name = it.name,
                            birthDate = LocalDate.parse(it.birth_date),
                            gender = Gender.valueOf(it.gender),
                            type = ProfileType.valueOf(it.type),
                            avatarColor = it.avatar_color.toInt(),
                            avatarPhoto = it.avatar_photo,
                            createdAt = it.created_at
                        )
                    }
                }
        )
    }

    override suspend fun saveProfile(profile: Profile) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.insertProfile(
            id = profile.id,
            name = profile.name,
            birth_date = profile.birthDate.toString(),
            gender = profile.gender.name,
            type = profile.type.name,
            avatar_color = profile.avatarColor.toLong(),
            avatar_photo = profile.avatarPhoto,
            created_at = profile.createdAt
        )
    }

    override suspend fun deleteProfile(id: String) {
        val db = databaseProvider.getDatabase()
        db.salamatDatabaseQueries.deleteVaccinesForProfile(id)
        db.salamatDatabaseQueries.deleteGrowthRecordsForProfile(id)
        db.salamatDatabaseQueries.deleteCheckupsForProfile(id)
        db.salamatDatabaseQueries.deleteProfile(id)
    }
}
