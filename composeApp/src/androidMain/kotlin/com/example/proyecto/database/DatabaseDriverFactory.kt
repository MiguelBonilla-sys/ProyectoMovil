package com.example.proyecto.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * 
 * Creates an AndroidSqliteDriver that stores the database in the app's
 * private storage directory.
 * 
 * @param context Android application context
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = LexSignDatabase.Schema,
            context = context,
            name = "lexsign.db"
        )
    }
}
