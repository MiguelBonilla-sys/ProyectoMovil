package com.example.proyecto.data

import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.database.DatabaseDriverFactory
import com.example.proyecto.database.LexSignDatabase

/**
 * Dependency injection container for the application.
 * 
 * Holds singleton instances of repository and database.
 * Initialized once in App.kt on application startup.
 */
object AppContainer {
    
    private var _database: LexSignDatabase? = null
    private var _userRepository: UserRepository? = null
    
    /**
     * Initialize the container with a DatabaseDriverFactory.
     * Must be called once on app startup.
     * 
     * @param driverFactory Platform-specific database driver factory
     */
    fun initialize(driverFactory: DatabaseDriverFactory) {
        val driver = driverFactory.createDriver()
        _database = LexSignDatabase(driver)
        _userRepository = UserRepository(_database!!)
    }
    
    /**
     * Get the UserRepository instance.
     * Throws exception if not initialized.
     */
    val userRepository: UserRepository
        get() = _userRepository ?: error("AppContainer not initialized. Call initialize() first.")
    
    /**
     * Get the database instance.
     * Throws exception if not initialized.
     */
    val database: LexSignDatabase
        get() = _database ?: error("AppContainer not initialized. Call initialize() first.")
}
