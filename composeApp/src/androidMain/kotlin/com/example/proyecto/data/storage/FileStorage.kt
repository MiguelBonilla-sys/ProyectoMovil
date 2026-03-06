package com.example.proyecto.data.storage

import java.io.File

lateinit var appContext: android.content.Context

actual class FileStorage actual constructor() {
    private val file: File
        get() = File(appContext.filesDir, "users.json")

    actual fun readUsers(): String? = runCatching { file.readText() }.getOrNull()

    actual fun writeUsers(content: String) {
        runCatching { file.writeText(content) }
    }
}
