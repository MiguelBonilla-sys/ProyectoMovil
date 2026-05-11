package com.example.proyecto.data.repository

import com.example.proyecto.data.model.User
import com.example.proyecto.data.remote.supabase
import com.example.proyecto.data.util.SecurityUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

class AuthRepository {

    suspend fun registrarCliente(
        nombre: String,
        email: String,
        password: String
    ): User {
        val existing = obtenerPorEmail(email)
        if (existing != null) throw Exception("El correo ya está registrado")

        val hashedPassword = SecurityUtils.hashPassword(password)
        val usuario = User(
            nombre = nombre,
            email = email,
            password = hashedPassword,
            tipo = "cliente"
        )
        return supabase.from("users")
            .insert(usuario) { select() }
            .decodeSingle<User>()
    }

    suspend fun registrarAbogado(
        nombre: String,
        email: String,
        password: String,
        tarjeta: String,
        especialidad: String,
        experiencia: Int,
        descripcion: String,
        telefono: String
    ): User {
        val existing = obtenerPorEmail(email)
        if (existing != null) throw Exception("El correo ya está registrado")

        val hashedPassword = SecurityUtils.hashPassword(password)
        val usuario = User(
            nombre = nombre,
            email = email,
            password = hashedPassword,
            tipo = "abogado",
            tarjeta = tarjeta,
            especialidad = especialidad,
            experiencia = experiencia,
            descripcion = descripcion,
            telefono = telefono
        )
        return supabase.from("users")
            .insert(usuario) { select() }
            .decodeSingle<User>()
    }

    suspend fun autenticarCliente(email: String, password: String): User? {
        val usuario = obtenerPorEmail(email) ?: return null
        if (usuario.tipo != "cliente" && usuario.tipo != "administrador") return null
        return if (SecurityUtils.verifyPassword(password, usuario.password)) usuario else null
    }

    suspend fun autenticarAbogado(email: String, tarjeta: String, password: String): User? {
        val usuario = obtenerPorEmail(email) ?: return null
        if (usuario.tipo != "abogado") return null
        if (usuario.tarjeta != tarjeta) return null
        return if (SecurityUtils.verifyPassword(password, usuario.password)) usuario else null
    }

    suspend fun actualizarPassword(userId: String, passwordActual: String, passwordNuevo: String) {
        val user = supabase.from("users")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<User>() ?: throw Exception("Usuario no encontrado")
        if (!SecurityUtils.verifyPassword(passwordActual, user.password))
            throw Exception("La contraseña actual es incorrecta")
        val nuevoHash = SecurityUtils.hashPassword(passwordNuevo)
        supabase.from("users")
            .update({ set("password", nuevoHash) }) { filter { eq("id", userId) } }
    }

    suspend fun eliminarUsuario(userId: String) {
        supabase.from("users").delete { filter { eq("id", userId) } }
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * Crea un token de recuperación válido por 15 minutos.
     * FASE 1: Recuperación de Contraseña
     */
    suspend fun solicitarRecuperacionContrasena(email: String) {
        val usuario = obtenerPorEmail(email) ?: throw Exception("Email no registrado")
        val usuarioId = usuario.id ?: throw Exception("Usuario sin ID")

        // Generar token único (alphanumerico aleatorio)
        val token = (1..32).map { (('a'..'z') + ('0'..'9')).random() }.joinToString("")
        
        // Token válido por 15 minutos
        val expiresAt = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
            .plusMinutes(15)
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

        // Crear registro en tabla password_reset_tokens
        supabase.from("password_reset_tokens")
            .insert(mapOf(
                "user_id" to (usuarioId as Any),
                "token" to (token as Any),
                "expires_at" to (expiresAt as Any)
            ))

        // TODO: Enviar email con el token
        // val email_body = "Tu código de recuperación: $token"
        // sendgrid.send(to = usuario.email, subject = "Recuperar Contraseña", body = email_body)
        // Por ahora, solo guardar en BD
    }

    /**
     * Verifica que el token sea válido y no haya expirado.
     */
    suspend fun verificarTokenRecuperacion(email: String, token: String) {
        val usuario = obtenerPorEmail(email) ?: throw Exception("Email no registrado")
        val usuarioId = usuario.id ?: throw Exception("Usuario sin ID")

        val tokenRecord = supabase.from("password_reset_tokens")
            .select {
                filter { eq("user_id", usuarioId as Any) }
                filter { eq("token", token) }
            }
            .decodeSingleOrNull<Map<String, Any?>>()
            ?: throw Exception("Token inválido")

        // Verificar que no haya expirado
        val expiresAt = tokenRecord["expires_at"] as? String
            ?: throw Exception("Token no tiene fecha de vencimiento")

        val expiresDateTime = java.time.LocalDateTime.parse(
            expiresAt,
            java.time.format.DateTimeFormatter.ISO_DATE_TIME
        )
        val ahora = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)

        if (ahora.isAfter(expiresDateTime)) {
            throw Exception("Token expirado. Solicita uno nuevo.")
        }

        // Verificar que no haya sido usado ya
        val usedAt = tokenRecord["used_at"]
        if (usedAt != null) {
            throw Exception("Token ya fue utilizado")
        }
    }

    /**
     * Restablece la contraseña del usuario con un token válido.
     */
    suspend fun restablecerContrasena(email: String, token: String, nuevaContrasena: String) {
        // Primero verificar que el token sea válido
        verificarTokenRecuperacion(email, token)

        val usuario = obtenerPorEmail(email) ?: throw Exception("Email no registrado")

        // Actualizar contraseña
        val nuevoHash = SecurityUtils.hashPassword(nuevaContrasena)
        val usuarioId = usuario.id ?: throw Exception("Usuario sin ID")
        supabase.from("users")
            .update({ set("password", nuevoHash as Any) }) {
                filter { eq("id", usuarioId) }
            }

        // Marcar token como usado
        val ahora = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

        supabase.from("password_reset_tokens")
            .update({ set("used_at", ahora as Any) }) {
                filter { eq("token", token) }
                filter { eq("user_id", usuarioId) }
            }
    }

    suspend fun obtenerPorId(userId: String): User? =
        supabase.from("users")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<User>()

    private suspend fun obtenerPorEmail(email: String): User? =
        supabase.from("users")
            .select {
                filter {
                    eq("email", email)
                }
            }
            .decodeSingleOrNull<User>()
}
