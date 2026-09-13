package ir.salamat.core.database

import ir.salamat.database.SalamatDatabase

class DatabaseProvider(private val driverFactory: DatabaseDriverFactory) {
    private var database: SalamatDatabase? = null

    suspend fun getDatabase(): SalamatDatabase {
        return database ?: run {
            val driver = driverFactory.createDriver()
            val newDb = SalamatDatabase(driver)
            database = newDb
            newDb
        }
    }
}
