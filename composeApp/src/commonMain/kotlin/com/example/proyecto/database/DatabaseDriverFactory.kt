package com.example.proyecto.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific factory for creating SQLDelight database drivers.
 * 
 * This uses Kotlin Multiplatform's expect/actual pattern to provide
 * platform-specific implementations for Android and iOS.
 * 
 * Usage:
 * - Android: Uses AndroidSqliteDriver with application context
 * - iOS: Uses NativeSqliteDriver
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
