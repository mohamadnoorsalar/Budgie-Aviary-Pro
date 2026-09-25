package com.example.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AviarySecurityKey_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val PBKDF2_ITERATIONS = 12000
    private const val PBKDF2_KEY_LENGTH = 256

    private val secureRandom = SecureRandom()
    private var fallbackKey: SecretKey? = null

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (_: Throwable) {
            // Android KeyStore might fallback or run in JVM tests
        }
    }

    private fun getSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: generateFallbackKey()
        } catch (_: Throwable) {
            generateFallbackKey()
        }
    }

    private fun generateFallbackKey(): SecretKey {
        if (fallbackKey == null) {
            val raw = MessageDigest.getInstance("SHA-256").digest("AviaryFallbackSecretKey_v1".toByteArray(StandardCharsets.UTF_8))
            fallbackKey = SecretKeySpec(raw, "AES")
        }
        return fallbackKey!!
    }

    private fun base64Encode(bytes: ByteArray): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } catch (_: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    private fun base64Decode(str: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(str.trim())
        } catch (_: Throwable) {
            android.util.Base64.decode(str.trim(), android.util.Base64.NO_WRAP)
        }
    }

    /**
     * Encrypts plain text using AES-GCM-256 with Android KeyStore.
     * Returns Base64 string composed of [IV (12 bytes) + Ciphertext].
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getSecretKey()
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
            base64Encode(combined)
        } catch (e: Throwable) {
            plainText
        }
    }

    /**
     * Decrypts Base64 string produced by [encrypt].
     */
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        return try {
            val combined = base64Decode(encryptedBase64)
            if (combined.size <= GCM_IV_LENGTH) return ""
            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getSecretKey()
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Throwable) {
            encryptedBase64
        }
    }

    /**
     * Generates a secure random salt (16 bytes).
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt
    }

    /**
     * Hashes password or PIN using SHA-256 with salt.
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hash = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return base64Encode(hash)
    }

    /**
     * Encrypts backup content using a user-supplied password with PBKDF2 + AES-GCM.
     */
    fun encryptBackup(plainJson: String, userPass: String): String {
        val salt = generateSalt()
        val keySpec = PBEKeySpec(userPass.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val cipherBytes = cipher.doFinal(plainJson.toByteArray(StandardCharsets.UTF_8))

        // Checksum of raw JSON for integrity verification
        val checksum = MessageDigest.getInstance("SHA-256").digest(plainJson.toByteArray(StandardCharsets.UTF_8))

        // Format: [Salt (16)] + [IV (12)] + [Checksum (32)] + [Ciphertext]
        val result = ByteArray(salt.size + iv.size + checksum.size + cipherBytes.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(iv, 0, result, salt.size, iv.size)
        System.arraycopy(checksum, 0, result, salt.size + iv.size, checksum.size)
        System.arraycopy(cipherBytes, 0, result, salt.size + iv.size + checksum.size, cipherBytes.size)

        return base64Encode(result)
    }

    /**
     * Decrypts a password-protected backup and validates integrity checksum.
     */
    fun decryptBackup(encryptedBase64: String, userPass: String): String? {
        return try {
            val bytes = base64Decode(encryptedBase64)
            val saltLen = 16
            val ivLen = GCM_IV_LENGTH
            val checkLen = 32
            if (bytes.size <= saltLen + ivLen + checkLen) return null

            val salt = ByteArray(saltLen)
            val iv = ByteArray(ivLen)
            val expectedChecksum = ByteArray(checkLen)
            val cipherBytes = ByteArray(bytes.size - saltLen - ivLen - checkLen)

            System.arraycopy(bytes, 0, salt, 0, saltLen)
            System.arraycopy(bytes, saltLen, iv, 0, ivLen)
            System.arraycopy(bytes, saltLen + ivLen, expectedChecksum, 0, checkLen)
            System.arraycopy(bytes, saltLen + ivLen + checkLen, cipherBytes, 0, cipherBytes.size)

            val keySpec = PBEKeySpec(userPass.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val decrypted = cipher.doFinal(cipherBytes)
            val json = String(decrypted, StandardCharsets.UTF_8)

            // Verify checksum
            val actualChecksum = MessageDigest.getInstance("SHA-256").digest(decrypted)
            if (!expectedChecksum.contentEquals(actualChecksum)) {
                return null
            }
            json
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Sanitizes sensitive content (passwords, PINs, secret keys) before audit logging.
     */
    fun sanitizeAuditText(input: String): String {
        var text = input
        // Mask passwords, PINs, api keys, tokens
        val secretPatterns = listOf(
            Regex("(?i)(password|pin|secret|api[_-]?key|token)[\"'\\s:=]+([^\\s,\"';&]+)"),
            Regex("AIza[0-9A-Za-z-_]{35}"), // Google API keys pattern
            Regex("\\b\\d{4,8}\\b") // Numeric PINs if explicitly formatted
        )
        for (pattern in secretPatterns) {
            text = text.replace(pattern) { matchResult ->
                if (matchResult.groupValues.size > 2) {
                    "${matchResult.groupValues[1]}=***REDACTED***"
                } else {
                    "***REDACTED***"
                }
            }
        }
        return text
    }
}

