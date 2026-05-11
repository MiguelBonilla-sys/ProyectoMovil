package com.example.proyecto.data.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationUtilsTest {

    @Test
    fun `isValidEmail - emails válidos`() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"))
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.org"))
        assertTrue(ValidationUtils.isValidEmail("user+tag@gmail.com"))
        assertTrue(ValidationUtils.isValidEmail("abogado@test.com.co"))
    }

    @Test
    fun `isValidEmail - emails inválidos`() {
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("plainaddress"))
        assertFalse(ValidationUtils.isValidEmail("@domain.com"))
        assertFalse(ValidationUtils.isValidEmail("user@"))
        assertFalse(ValidationUtils.isValidEmail("user@domain"))
        assertFalse(ValidationUtils.isValidEmail("user space@domain.com"))
    }

    @Test
    fun `isValidPassword - contraseñas válidas`() {
        assertTrue(ValidationUtils.isValidPassword("Admin123"))
        assertTrue(ValidationUtils.isValidPassword("Cliente1abc"))
        assertTrue(ValidationUtils.isValidPassword("Password123"))
        assertTrue(ValidationUtils.isValidPassword("Abcdefg1"))
    }

    @Test
    fun `isValidPassword - contraseñas inválidas`() {
        assertFalse(ValidationUtils.isValidPassword("short1A")) // < 8 chars
        assertFalse(ValidationUtils.isValidPassword("nouppercase123")) // sin mayúscula
        assertFalse(ValidationUtils.isValidPassword("NOLOWERCASE123")) // sin minúscula
        assertFalse(ValidationUtils.isValidPassword("NoNumbersABC")) // sin número
        assertFalse(ValidationUtils.isValidPassword(""))
    }

    @Test
    fun `isValidPhone - teléfonos válidos`() {
        assertTrue(ValidationUtils.isValidPhone("+57 300 123 4567"))
        assertTrue(ValidationUtils.isValidPhone("573001234567"))
        assertTrue(ValidationUtils.isValidPhone("3001234567"))
        assertTrue(ValidationUtils.isValidPhone("+573001234567"))
    }

    @Test
    fun `isValidPhone - teléfonos inválidos`() {
        assertFalse(ValidationUtils.isValidPhone(""))
        assertFalse(ValidationUtils.isValidPhone("123")) // muy corto
        assertFalse(ValidationUtils.isValidPhone("abcdefghij"))
    }

    @Test
    fun `isValidNombreCompleto - nombres válidos`() {
        assertTrue(ValidationUtils.isValidNombreCompleto("Carlos"))
        assertTrue(ValidationUtils.isValidNombreCompleto("María José"))
        assertTrue(ValidationUtils.isValidNombreCompleto("O'Connor"))
        assertTrue(ValidationUtils.isValidNombreCompleto("Jean-Pierre"))
    }

    @Test
    fun `isValidNombreCompleto - nombres inválidos`() {
        assertFalse(ValidationUtils.isValidNombreCompleto(""))
        assertFalse(ValidationUtils.isValidNombreCompleto("AB")) // < 3 chars
        assertFalse(ValidationUtils.isValidNombreCompleto("Juan123")) // números
        assertFalse(ValidationUtils.isValidNombreCompleto("Test@name")) // caracteres especiales
    }

    @Test
    fun `isValidTarjetaProfesional - tarjetas válidas`() {
        assertTrue(ValidationUtils.isValidTarjetaProfesional("AB-12345"))
        assertTrue(ValidationUtils.isValidTarjetaProfesional("AB12345"))
        assertTrue(ValidationUtils.isValidTarjetaProfesional("TJ-1234567"))
    }

    @Test
    fun `isValidTarjetaProfesional - tarjetas inválidas`() {
        assertFalse(ValidationUtils.isValidTarjetaProfesional(""))
        assertFalse(ValidationUtils.isValidTarjetaProfesional("A-12345")) // solo 1 letra
        assertFalse(ValidationUtils.isValidTarjetaProfesional("ABC-12345")) // 3 letras
        assertFalse(ValidationUtils.isValidTarjetaProfesional("AB-1234")) // solo 4 dígitos
    }

    @Test
    fun `isValidUsername - usernames válidos`() {
        assertTrue(ValidationUtils.isValidUsername("user123"))
        assertTrue(ValidationUtils.isValidUsername("abogado_test"))
        assertTrue(ValidationUtils.isValidUsername("cliente"))
    }

    @Test
    fun `isValidUsername - usernames inválidos`() {
        assertFalse(ValidationUtils.isValidUsername(""))
        assertFalse(ValidationUtils.isValidUsername("ab")) // < 3 chars
        assertFalse(ValidationUtils.isValidUsername("user@name")) // @ no permitido
        assertFalse(ValidationUtils.isValidUsername("user name")) // espacio no permitido
    }

    @Test
    fun `isValidUrl - URLs válidas`() {
        assertTrue(ValidationUtils.isValidUrl("https://example.com"))
        assertTrue(ValidationUtils.isValidUrl("http://test.org/path"))
        assertTrue(ValidationUtils.isValidUrl("ftp://files.com"))
    }

    @Test
    fun `isValidUrl - URLs inválidas`() {
        assertFalse(ValidationUtils.isValidUrl(""))
        assertFalse(ValidationUtils.isValidUrl("not-a-url"))
        assertFalse(ValidationUtils.isValidUrl("htp://wrong.com"))
    }

    @Test
    fun `isValidTextLength - longitudes válidas`() {
        assertTrue(ValidationUtils.isValidTextLength("hello"))
        assertTrue(ValidationUtils.isValidTextLength("ab", minLength = 2))
        assertTrue(ValidationUtils.isValidTextLength("x".repeat(500), maxLength = 500))
    }

    @Test
    fun `isValidTextLength - longitudes inválidas`() {
        assertFalse(ValidationUtils.isValidTextLength("", minLength = 1))
        assertFalse(ValidationUtils.isValidTextLength("ab", minLength = 3))
        assertFalse(ValidationUtils.isValidTextLength("x".repeat(1001), maxLength = 1000))
    }
}
