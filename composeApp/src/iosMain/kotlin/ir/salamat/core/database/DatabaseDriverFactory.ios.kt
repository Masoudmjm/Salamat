package ir.salamat.core.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ir.salamat.database.SalamatDatabase

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = SalamatDatabase.Schema.synchronous(),
            name = "salamat.db"
        )
    }
}
