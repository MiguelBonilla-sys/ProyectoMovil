package com.example.proyecto.data.remote

import com.example.proyecto.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Provides a singleton Supabase client for the application.
 * 
 * This client is used for:
 * - Authentication (Auth module)
 * - Database operations (Postgrest module)
 * - Real-time subscriptions (Realtime module)
 * 
 * Configuration:
 * Credentials are loaded from .env file at build time via BuildConfig.
 * To configure:
 * 1. Copy .env.example to .env
 * 2. Fill in your Supabase URL and anon key from Supabase Dashboard > Settings > API
 * 3. Rebuild the project
 */
object SupabaseClientProvider {
    
    /**
     * Lazy-initialized Supabase client.
     * Only created when first accessed.
     * Credentials come from BuildConfig (generated from .env file).
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
    
    /**
     * Check if Supabase is configured with valid credentials.
     * Returns false if placeholder values are still in use.
     */
    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL != "YOUR_SUPABASE_URL_HERE" &&
               BuildConfig.SUPABASE_ANON_KEY != "YOUR_SUPABASE_ANON_KEY_HERE" &&
               BuildConfig.SUPABASE_URL.isNotBlank() &&
               BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    }
}
