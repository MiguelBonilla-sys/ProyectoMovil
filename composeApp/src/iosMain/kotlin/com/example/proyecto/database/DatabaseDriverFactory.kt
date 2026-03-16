package com.example.proyecto.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory.
 * 
 * Creates a NativeSqliteDriver that stores the database in the app's
 * documents directory.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = LexSignDatabase.Schema,
            name = "lexsign.db"
        )
    }
}
