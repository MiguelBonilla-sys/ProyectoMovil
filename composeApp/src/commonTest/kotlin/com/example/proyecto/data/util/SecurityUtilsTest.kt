package com.example.proyecto.data.util

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SecurityUtilsTest {

    @Test
    fun hashPasswordGeneraHashNoNulo() {
        val hash = SecurityUtils.hashPassword("TestPassword123")
        assertNotNull(hash)
        assertTrue(hash.isNotBlank())
    }

    @Test
    fun hashPasswordGeneraHashDiferenteAlInput() {
        val password = "TestPassword123"
        val hash = SecurityUtils.hashPassword(password)
        assertTrue(hash != password)
    }

    @Test
    fun verifyPasswordContrasenaCorrecta() {
        val password = "TestPassword123"
        val hash = SecurityUtils.hashPassword(password)
        assertTrue(SecurityUtils.verifyPassword(password, hash))
    }

    @Test
    fun verifyPasswordContrasenaIncorrecta() {
        val password = "TestPassword123"
        val hash = SecurityUtils.hashPassword(password)
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun hashPasswordGeneraSalesDiferentes() {
        val password = "TestPassword123"
        val hash1 = SecurityUtils.hashPassword(password)
        val hash2 = SecurityUtils.hashPassword(password)
        assertTrue(hash1 != hash2)
    }

    @Test
    fun hashEsFormatoEsperado() {
        val hash = SecurityUtils.hashPassword("Test123")
        assertTrue(hash.contains(":"))
    }
}