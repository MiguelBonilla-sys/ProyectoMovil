package com.example.proyecto.data.storage

expect object LocalStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
}
