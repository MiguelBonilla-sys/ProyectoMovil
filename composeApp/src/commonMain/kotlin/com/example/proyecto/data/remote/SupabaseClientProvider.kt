package com.example.proyecto.data.remote

import com.example.proyecto.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL != "YOUR_SUPABASE_URL_HERE" &&
               BuildConfig.SUPABASE_ANON_KEY != "YOUR_SUPABASE_ANON_KEY_HERE" &&
               BuildConfig.SUPABASE_URL.isNotBlank() &&
               BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    }
}

/** Top-level accessor for convenience in repositories */
val supabase get() = SupabaseClientProvider.client
