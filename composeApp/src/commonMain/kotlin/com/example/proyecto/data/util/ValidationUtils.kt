package com.example.proyecto.data.util

/**
 * Utilidades para validar datos de entrada.
 * Incluye validaciones para email, contraseña, teléfono, tarjeta profesional, etc.
 */
object ValidationUtils {

    /**
     * Valida formato de correo electrónico.
     * Usa patrón RFC 5322 simplificado (no es 100% RFC compliant pero suficiente para UX).
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank() || email.length > 254) return false
        
        val emailRegex = Regex(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
        )
        return emailRegex.matches(email)
    }

    /**
     * Valida que contraseña cumpla requisitos mínimos:
     * - Mínimo 8 caracteres
     * - Al menos 1 mayúscula
     * - Al menos 1 minúscula
     * - Al menos 1 número
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUppercase && hasLowercase && hasDigit
    }

    /**
     * Valida formato de teléfono colombiano.
     * Acepta: +57 1234567890, 573001234567, 1234567890, etc.
     */
    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        
        val phoneClean = phone.replace(Regex("[\\s\\-\\(\\)]+"), "")
        
        // Validar que sea numérico
        if (!phoneClean.matches(Regex("^\\+?\\d{7,15}$"))) return false
        
        // Si comienza con +57, OK
        // Si comienza con 57, OK
        // Si es 10-11 dígitos, probablemente es colombiano
        return when {
            phoneClean.startsWith("+57") -> true
            phoneClean.startsWith("57") && phoneClean.length >= 10 -> true
            phoneClean.length in 10..11 -> true
            else -> false
        }
    }

    /**
     * Valida nombre completo:
     * - Mínimo 3 caracteres
     * - No contiene números
     * - Solo letras, espacios y apóstrofes (para nombres como "O'Connor")
     */
    fun isValidNombreCompleto(nombre: String): Boolean {
        if (nombre.length < 3 || nombre.length > 100) return false
        
        val nombreClean = nombre.trim()
        
        // Solo letras, espacios, apóstrofes y guiones (para hiphenated names)
        val validoRegex = Regex("^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s'-]+$")
        
        return validoRegex.matches(nombreClean) && nombreClean.any { it.isLetter() }
    }

    /**
     * Valida tarjeta profesional colombiana.
     * Formato típico: AB-12345 o AB12345 (2 letras + 5-7 dígitos)
     */
    fun isValidTarjetaProfesional(tarjeta: String): Boolean {
        if (tarjeta.isBlank()) return false
        
        val tarjetaClean = tarjeta.trim().uppercase()
        
        // Formato: XX-XXXXX o XXXXXXX
        val validoRegex = Regex("^[A-Z]{2}-?\\d{5,7}$")
        
        return validoRegex.matches(tarjetaClean)
    }

    /**
     * Valida que un nombre de usuario sea válido.
     * - Mínimo 3, máximo 50 caracteres
     * - Solo letras, números y guiones bajos
     */
    fun isValidUsername(username: String): Boolean {
        if (username.length < 3 || username.length > 50) return false
        
        val validoRegex = Regex("^[a-zA-Z0-9_-]+$")
        
        return validoRegex.matches(username)
    }

    /**
     * Valida URL.
     * Acepta http, https, ftp.
     */
    fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        
        return try {
            val urlObj = java.net.URL(url)
            val protocol = urlObj.protocol
            protocol in listOf("http", "https", "ftp")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida que campo no esté vacío y tenga longitud mínima.
     */
    fun isValidTextLength(text: String, minLength: Int = 1, maxLength: Int = 1000): Boolean {
        val cleaned = text.trim()
        return cleaned.length in minLength..maxLength
    }

    /**
     * Extrae y valida error de acuerdo al tipo de validación fallida.
     */
    fun getValidationError(fieldName: String, fieldValue: String, validationType: ValidationType): String? {
        return when (validationType) {
            ValidationType.EMAIL -> {
                if (!isValidEmail(fieldValue)) "Email inválido"
                else null
            }
            ValidationType.PASSWORD -> {
                when {
                    fieldValue.length < 8 -> "Contraseña debe tener al menos 8 caracteres"
                    !fieldValue.any { it.isUpperCase() } -> "Contraseña debe contener mayúscula"
                    !fieldValue.any { it.isLowerCase() } -> "Contraseña debe contener minúscula"
                    !fieldValue.any { it.isDigit() } -> "Contraseña debe contener número"
                    else -> null
                }
            }
            ValidationType.PHONE -> {
                if (!isValidPhone(fieldValue)) "Teléfono inválido"
                else null
            }
            ValidationType.NOMBRE_COMPLETO -> {
                if (!isValidNombreCompleto(fieldValue)) "Nombre debe tener solo letras (mín. 3 caracteres)"
                else null
            }
            ValidationType.TARJETA_PROFESIONAL -> {
                if (!isValidTarjetaProfesional(fieldValue)) "Tarjeta profesional inválida (ej: AB-12345)"
                else null
            }
            ValidationType.USERNAME -> {
                if (!isValidUsername(fieldValue)) "Usuario debe tener 3-50 caracteres alfanuméricos"
                else null
            }
            ValidationType.URL -> {
                if (!isValidUrl(fieldValue)) "URL inválida"
                else null
            }
            ValidationType.REQUIRED -> {
                if (fieldValue.trim().isEmpty()) "$fieldName es requerido"
                else null
            }
        }
    }

    /**
     * Enum de tipos de validación disponibles.
     */
    enum class ValidationType {
        EMAIL,
        PASSWORD,
        PHONE,
        NOMBRE_COMPLETO,
        TARJETA_PROFESIONAL,
        USERNAME,
        URL,
        REQUIRED
    }
}
