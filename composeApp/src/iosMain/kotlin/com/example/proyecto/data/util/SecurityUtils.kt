package com.example.proyecto.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.UByteVar
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
actual object SecurityUtils {

    private const val ITERATIONS = 10_000u
    private const val KEY_LENGTH = 32
    private const val SALT_BYTES = 16

    actual fun hashPassword(plain: String): String {
        val salt = ByteArray(SALT_BYTES)
        memScoped {
            val saltPtr = allocArray<UByteVar>(SALT_BYTES)
            SecRandomCopyBytes(kSecRandomDefault, SALT_BYTES.toULong(), saltPtr)
            for (i in 0 until SALT_BYTES) {
                salt[i] = saltPtr[i].toByte()
            }
        }
        val hash = pbkdf2(plain, salt)
        return "${Base64.encode(salt)}:${Base64.encode(hash)}"
    }

    actual fun verifyPassword(plain: String, hash: String): Boolean {
        return try {
            val parts = hash.split(":")
            if (parts.size != 2) return false
            val salt = Base64.decode(parts[0])
            val storedHash = Base64.decode(parts[1])
            pbkdf2(plain, salt).contentEquals(storedHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val passwordBytes = password.encodeToByteArray()
        val derivedKey = ByteArray(KEY_LENGTH)
        memScoped {
            val passwordPtr = passwordBytes.toCValues()
            val saltPtr = salt.toCValues()
            val keyPtr = allocArray<UByteVar>(KEY_LENGTH)
            CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2,
                password = passwordPtr.ptr.reinterpret(),
                passwordLen = passwordBytes.size.toULong(),
                salt = saltPtr.ptr.reinterpret(),
                saltLen = salt.size.toULong(),
                prf = kCCPRFHmacAlgSHA256,
                rounds = ITERATIONS,
                derivedKey = keyPtr,
                derivedKeyLen = KEY_LENGTH.toULong()
            )
            for (i in 0 until KEY_LENGTH) {
                derivedKey[i] = keyPtr[i].toByte()
            }
        }
        return derivedKey
    }
}
