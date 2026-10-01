package ir.behpay.core.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    suspend fun createDriver(): SqlDriver
}
