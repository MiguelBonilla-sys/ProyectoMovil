package com.example.proyecto.data.remote

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
 * TODO: Replace placeholder values with your actual Supabase credentials:
 * 1. Go to your Supabase project dashboard
 * 2. Navigate to Settings > API
 * 3. Copy "Project URL" and "anon/public key"
 * 4. Replace the values below
 */
object SupabaseClientProvider {
    
    /**
     * TODO: Replace with your Supabase project URL
     * Example: "https://xxxxxxxxxxxxx.supabase.co"
     */
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL_HERE"
    
    /**
     * TODO: Replace with your Supabase anon key
     * Example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     */
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY_HERE"
    
    /**
     * Lazy-initialized Supabase client.
     * Only created when first accessed.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
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
        return SUPABASE_URL != "YOUR_SUPABASE_URL_HERE" &&
               SUPABASE_ANON_KEY != "YOUR_SUPABASE_ANON_KEY_HERE"
    }
}
