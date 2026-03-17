package com.example.proyecto.data.util

expect object SecurityUtils {
    fun hashPassword(plain: String): String
    fun verifyPassword(plain: String, hash: String): Boolean
}
