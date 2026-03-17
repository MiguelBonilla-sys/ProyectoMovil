package com.example.proyecto.data.util

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
actual object SecurityUtils {

    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val SALT_BYTES = 16

    actual fun hashPassword(plain: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(plain, salt)
        val saltB64 = Base64.encode(salt)
        val hashB64 = Base64.encode(hash)
        return "$saltB64:$hashB64"
    }

    actual fun verifyPassword(plain: String, hash: String): Boolean {
        return try {
            val parts = hash.split(":")
            if (parts.size != 2) return false
            val salt = Base64.decode(parts[0])
            val storedHash = Base64.decode(parts[1])
            val candidateHash = pbkdf2(plain, salt)
            storedHash.contentEquals(candidateHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}
