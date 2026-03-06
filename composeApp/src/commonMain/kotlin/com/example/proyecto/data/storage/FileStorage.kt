package com.example.proyecto.data.storage

expect class FileStorage() {
    fun readUsers(): String?
    fun writeUsers(content: String)
}
