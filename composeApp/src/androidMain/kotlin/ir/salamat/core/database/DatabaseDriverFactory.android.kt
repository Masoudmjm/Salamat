package ir.salamat.core.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ir.salamat.database.SalamatDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual suspend fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = SalamatDatabase.Schema.synchronous(),
            context = context,
            name = "salamat.db",
            callback = object : AndroidSqliteDriver.Callback(SalamatDatabase.Schema.synchronous()) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    try {
                        db.execSQL("ALTER TABLE profile ADD COLUMN avatar_photo TEXT;")
                    } catch (_: Exception) {
                        // Column already exists
                    }
                }
            }
        )
    }
}
