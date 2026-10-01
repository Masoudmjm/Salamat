package ir.behpay.core.database

import ir.behpay.database.BehpayDatabase

class DatabaseProvider(private val driverFactory: DatabaseDriverFactory) {
    private var database: BehpayDatabase? = null

    suspend fun getDatabase(): BehpayDatabase {
        return database ?: run {
            val driver = driverFactory.createDriver()
            val newDb = BehpayDatabase(driver)
            database = newDb
            newDb
        }
    }
}
