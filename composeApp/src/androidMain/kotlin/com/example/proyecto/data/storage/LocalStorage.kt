package com.example.proyecto.data.storage

import android.content.Context
import android.content.SharedPreferences

actual object LocalStorage {

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("lexsign_session", Context.MODE_PRIVATE)
    }

    actual fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    actual fun getString(key: String): String? = prefs?.getString(key, null)

    actual fun remove(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }
}
