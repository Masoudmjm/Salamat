package ir.behpay.core.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ir.behpay.database.BehpayDatabase

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = BehpayDatabase.Schema.synchronous(),
            name = "behpay.db"
        )
    }
}
