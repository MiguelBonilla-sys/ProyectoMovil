package com.example.proyecto.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.proyecto.data.model.User
import com.example.proyecto.data.remote.SupabaseClientProvider
import com.example.proyecto.database.LexSignDatabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Repository for managing User data with offline-first architecture.
 * 
 * Architecture:
 * - Local: SQLDelight (SQLite) for fast, offline access
 * - Remote: Supabase (PostgreSQL) for cloud sync and backup
 * 
 * Flow:
 * 1. All reads come from local database (fast)
 * 2. All writes go to local database first (offline-capable)
 * 3. Writes sync to Supabase when online (background)
 * 4. Conflicts are marked for manual resolution
 * 
 * @param database SQLDelight database instance
 */
class UserRepository(private val database: LexSignDatabase) {
    
    private val queries = database.userQueries
    private val supabase = SupabaseClientProvider.client
    
    /**
     * Supabase table schema (for serialization)
     */
    @Serializable
    private data class SupabaseUser(
        val id: String? = null,
        val nombre: String,
        val email: String,
        val password: String,
        val tipo: String,
        val tarjeta: String = "",
        val created_at: String? = null,
        val updated_at: String? = null
    )
    
    // ========== INITIALIZATION ==========
    
    /**
     * Initialize repository with seed data.
     * Called once on app startup.
     * 
     * @param seedUsers List of users to seed if database is empty
     */
    suspend fun init(seedUsers: List<User>) = withContext(Dispatchers.IO) {
        val count = queries.countAll().executeAsOne()
        if (count == 0L) {
            // Database is empty, seed with initial data
            seedUsers.forEach { user ->
                insertUser(user, syncToSupabase = false)
            }
            // After seeding, sync all to Supabase if configured
            if (SupabaseClientProvider.isConfigured()) {
                syncAllToSupabase()
            }
        }
    }
    
    // ========== AUTHENTICATION ==========
    
    /**
     * Authenticate a client with email and password.
     * 
     * @return User if credentials are valid, null otherwise
     */
    suspend fun authenticateCliente(email: String, password: String): User? = 
        withContext(Dispatchers.IO) {
            queries.authenticateCliente(email, password)
                .executeAsOneOrNull()
                ?.toUser()
        }
    
    /**
     * Authenticate a lawyer with email, tarjeta profesional, and password.
     * 
     * @return User if credentials are valid, null otherwise
     */
    suspend fun authenticateAbogado(email: String, tarjeta: String, password: String): User? = 
        withContext(Dispatchers.IO) {
            queries.authenticateAbogado(email, tarjeta, password)
                .executeAsOneOrNull()
                ?.toUser()
        }
    
    // ========== REGISTRATION ==========
    
    /**
     * Register a new client user.
     * 
     * @return Result with User if successful, or Exception if email already exists
     */
    suspend fun registerCliente(nombre: String, email: String, password: String): Result<User> = 
        withContext(Dispatchers.IO) {
            try {
                // Check if email already exists
                val exists = queries.emailExists(email).executeAsOne()
                if (exists) {
                    return@withContext Result.failure(Exception("Email ya registrado"))
                }
                
                // Create new user
                val user = User(
                    nombre = nombre,
                    email = email,
                    password = password,
                    tipo = "cliente",
                    tarjeta = ""
                )
                
                // Insert locally
                insertUser(user, syncToSupabase = true)
                
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Get all users as a Flow (reactive).
     */
    fun getAllUsers(): Flow<List<User>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toUser() } }
    }
    
    /**
     * Get a user by email.
     */
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        queries.selectByEmail(email)
            .executeAsOneOrNull()
            ?.toUser()
    }
    
    /**
     * Get users by type (cliente or abogado).
     */
    suspend fun getUsersByTipo(tipo: String): List<User> = withContext(Dispatchers.IO) {
        queries.selectByTipo(tipo)
            .executeAsList()
            .map { it.toUser() }
    }
    
    /**
     * Insert a new user into the database.
     * 
     * @param user User to insert
     * @param syncToSupabase Whether to sync to Supabase immediately
     */
    private suspend fun insertUser(user: User, syncToSupabase: Boolean) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        
        queries.insert(
            nombre = user.nombre,
            email = user.email,
            password = user.password,
            tipo = user.tipo,
            tarjeta = user.tarjeta,
            supabaseId = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = if (syncToSupabase) "pending" else "synced"
        )
        
        if (syncToSupabase && SupabaseClientProvider.isConfigured()) {
            syncPendingToSupabase()
        }
    }
    
    /**
     * Update an existing user.
     */
    suspend fun updateUser(
        id: Long,
        nombre: String,
        email: String,
        password: String,
        tipo: String,
        tarjeta: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        
        queries.update(
            nombre = nombre,
            email = email,
            password = password,
            tipo = tipo,
            tarjeta = tarjeta,
            supabaseId = null,
            updatedAt = now,
            syncStatus = "pending",
            id = id
        )
        
        if (SupabaseClientProvider.isConfigured()) {
            syncPendingToSupabase()
        }
    }
    
    /**
     * Delete a user by email.
     */
    suspend fun deleteUser(email: String) = withContext(Dispatchers.IO) {
        queries.deleteByEmail(email)
        // TODO: Also delete from Supabase if configured
    }
    
    // ========== SYNC OPERATIONS ==========
    
    /**
     * Sync all pending changes to Supabase.
     * Called automatically after local changes, or manually by the app.
     */
    suspend fun syncPendingToSupabase() = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured()) return@withContext
        
        try {
            val pendingUsers = queries.selectPendingSync().executeAsList()
            
            pendingUsers.forEach { dbUser ->
                val supabaseUser = SupabaseUser(
                    nombre = dbUser.nombre,
                    email = dbUser.email,
                    password = dbUser.password,
                    tipo = dbUser.tipo,
                    tarjeta = dbUser.tarjeta
                )
                
                // Upsert to Supabase (insert or update)
                val response = supabase.from("users")
                    .upsert(supabaseUser)
                
                // Mark as synced in local database
                queries.updateSyncStatus(
                    syncStatus = "synced",
                    updatedAt = System.currentTimeMillis(),
                    id = dbUser.id
                )
            }
        } catch (e: Exception) {
            // Log error but don't crash - offline-first means sync failures are OK
            println("Sync to Supabase failed: ${e.message}")
        }
    }
    
    /**
     * Sync all users to Supabase (used during initial seed).
     */
    private suspend fun syncAllToSupabase() = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured()) return@withContext
        
        try {
            val allUsers = queries.selectAll().executeAsList()
            
            allUsers.forEach { dbUser ->
                val supabaseUser = SupabaseUser(
                    nombre = dbUser.nombre,
                    email = dbUser.email,
                    password = dbUser.password,
                    tipo = dbUser.tipo,
                    tarjeta = dbUser.tarjeta
                )
                
                supabase.from("users").insert(supabaseUser)
                
                queries.updateSyncStatus(
                    syncStatus = "synced",
                    updatedAt = System.currentTimeMillis(),
                    id = dbUser.id
                )
            }
        } catch (e: Exception) {
            println("Initial sync to Supabase failed: ${e.message}")
        }
    }
    
    /**
     * Pull latest data from Supabase and merge with local.
     * Used to get updates from other devices or web admin panel.
     */
    suspend fun pullFromSupabase() = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured()) return@withContext
        
        try {
            val remoteUsers = supabase.from("users")
                .select()
                .decodeList<SupabaseUser>()
            
            remoteUsers.forEach { remote ->
                val local = remote.email?.let { queries.selectByEmail(it).executeAsOneOrNull() }
                
                if (local == null) {
                    // New user from remote - insert locally
                    queries.insert(
                        nombre = remote.nombre,
                        email = remote.email,
                        password = remote.password,
                        tipo = remote.tipo,
                        tarjeta = remote.tarjeta,
                        supabaseId = remote.id,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = "synced"
                    )
                } else {
                    // Existing user - check timestamps and merge
                    // TODO: Implement conflict resolution based on updated_at
                }
            }
        } catch (e: Exception) {
            println("Pull from Supabase failed: ${e.message}")
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Convert database User entity to domain User model.
     */
    private fun com.example.proyecto.database.User.toUser() = User(
        nombre = this.nombre,
        email = this.email,
        password = this.password,
        tipo = this.tipo,
        tarjeta = this.tarjeta
    )
}
