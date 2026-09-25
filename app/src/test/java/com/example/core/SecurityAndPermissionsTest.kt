package com.example.core

import com.example.core.security.AppRole
import com.example.core.security.AutoLockTimeout
import com.example.core.security.AviaryModule
import com.example.core.security.CryptoManager
import com.example.core.security.PermissionLevel
import com.example.core.security.RolePermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndPermissionsTest {

    @Test
    fun testCryptoManagerEncryptionDecryption() {
        val originalText = "TopSecretAviaryData_2026_Ring#IR-1402-99"
        val encrypted = CryptoManager.encrypt(originalText)
        assertNotEquals(originalText, encrypted)
        assertTrue(encrypted.isNotBlank())

        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testPasswordHashingWithSalt() {
        val salt1 = CryptoManager.generateSalt()
        val salt2 = CryptoManager.generateSalt()
        val pin = "1234"

        val hash1 = CryptoManager.hashPassword(pin, salt1)
        val hash2 = CryptoManager.hashPassword(pin, salt2)

        // Hashes with different salts must differ
        assertNotEquals(hash1, hash2)

        // Same salt produces identical hash
        val hash1Repeat = CryptoManager.hashPassword(pin, salt1)
        assertEquals(hash1, hash1Repeat)
    }

    @Test
    fun testProtectedBackupEncryptionAndChecksumVerification() {
        val backupJson = """{"birdsCount":45,"pairsCount":12,"timestamp":1727220000}"""
        val backupPassword = "SuperStrongPassword2026!"

        val encryptedBackup = CryptoManager.encryptBackup(backupJson, backupPassword)
        assertNotNull(encryptedBackup)
        assertTrue(encryptedBackup.length > 50)

        // Successful decryption with correct password
        val decryptedJson = CryptoManager.decryptBackup(encryptedBackup, backupPassword)
        assertEquals(backupJson, decryptedJson)

        // Failed decryption with incorrect password returns null
        val invalidDecryption = CryptoManager.decryptBackup(encryptedBackup, "WrongPassword123")
        assertNull(invalidDecryption)
    }

    @Test
    fun testAuditSanitizerRemovesSecrets() {
        val rawMessage = "User set password: MySecretPass123! and API key: AIzaSyD98765432101234567890123456789012"
        val sanitized = CryptoManager.sanitizeAuditText(rawMessage)

        assertFalse(sanitized.contains("MySecretPass123!"))
        assertFalse(sanitized.contains("AIzaSyD98765432101234567890123456789012"))
        assertTrue(sanitized.contains("***REDACTED***"))
    }

    @Test
    fun testManagerRoleHasFullAccess() {
        val managerPerms = RolePermissions.defaultPermissions(AppRole.MANAGER)
        AviaryModule.entries.forEach { module ->
            assertTrue("Manager should have access to $module", managerPerms.canAccess(module))
            assertTrue("Manager should have write to $module", managerPerms.canWrite(module))
            assertTrue("Manager should have delete permission on $module", managerPerms.canDelete(module))
        }
    }

    @Test
    fun testFacilityWorkerPermissions() {
        val perms = RolePermissions.defaultPermissions(AppRole.FACILITY_WORKER)

        // Worker has care access
        assertTrue(perms.canAccess(AviaryModule.BIRDS))
        assertTrue(perms.canWrite(AviaryModule.BIRDS))
        assertFalse("Worker cannot delete birds", perms.canDelete(AviaryModule.BIRDS))

        assertTrue(perms.canAccess(AviaryModule.NUTRITION))
        assertTrue(perms.canWrite(AviaryModule.INVENTORY))

        // Worker has NO finance access
        assertFalse("Worker cannot access finance", perms.canAccess(AviaryModule.FINANCE))
        assertFalse(perms.canWrite(AviaryModule.FINANCE))

        // Worker has NO settings or audit log access
        assertFalse(perms.canAccess(AviaryModule.SETTINGS))
        assertFalse(perms.canAccess(AviaryModule.AUDIT_LOG))
    }

    @Test
    fun testVeterinarianPermissions() {
        val perms = RolePermissions.defaultPermissions(AppRole.VETERINARIAN)

        // Health full access
        assertTrue(perms.canAccess(AviaryModule.HEALTH))
        assertTrue(perms.canWrite(AviaryModule.HEALTH))
        assertTrue(perms.canDelete(AviaryModule.HEALTH))

        // Birds read-write
        assertTrue(perms.canAccess(AviaryModule.BIRDS))
        assertTrue(perms.canWrite(AviaryModule.BIRDS))

        // No finance access
        assertFalse(perms.canAccess(AviaryModule.FINANCE))
    }

    @Test
    fun testAccountantPermissions() {
        val perms = RolePermissions.defaultPermissions(AppRole.ACCOUNTANT)

        // Finance full access
        assertTrue(perms.canAccess(AviaryModule.FINANCE))
        assertTrue(perms.canWrite(AviaryModule.FINANCE))
        assertTrue(perms.canDelete(AviaryModule.FINANCE))

        // Reports full access
        assertTrue(perms.canAccess(AviaryModule.REPORTS))

        // Birds read-only
        assertTrue(perms.canAccess(AviaryModule.BIRDS))
        assertFalse("Accountant cannot edit birds", perms.canWrite(AviaryModule.BIRDS))

        // Genetics none
        assertFalse(perms.canAccess(AviaryModule.GENETICS_PEDIGREE))
    }

    @Test
    fun testJudgePermissions() {
        val perms = RolePermissions.defaultPermissions(AppRole.JUDGE)

        // Competitions read-write
        assertTrue(perms.canAccess(AviaryModule.COMPETITIONS))
        assertTrue(perms.canWrite(AviaryModule.COMPETITIONS))

        // Birds read-only
        assertTrue(perms.canAccess(AviaryModule.BIRDS))
        assertFalse(perms.canWrite(AviaryModule.BIRDS))

        // No pairs, cages, reproduction, or finance access
        assertFalse(perms.canAccess(AviaryModule.PAIRS))
        assertFalse(perms.canAccess(AviaryModule.CAGES))
        assertFalse(perms.canAccess(AviaryModule.FINANCE))
    }

    @Test
    fun testCustomRolePermissions() {
        val customMap = mapOf(
            AviaryModule.BIRDS to PermissionLevel.READ_WRITE,
            AviaryModule.FINANCE to PermissionLevel.READ_ONLY,
            AviaryModule.HEALTH to PermissionLevel.FULL
        )
        val customPerms = RolePermissions(AppRole.CUSTOM, customMap)

        assertTrue(customPerms.canWrite(AviaryModule.BIRDS))
        assertFalse(customPerms.canDelete(AviaryModule.BIRDS))

        assertTrue(customPerms.canAccess(AviaryModule.FINANCE))
        assertFalse(customPerms.canWrite(AviaryModule.FINANCE))

        assertTrue(customPerms.canDelete(AviaryModule.HEALTH))
        assertFalse(customPerms.canAccess(AviaryModule.INVENTORY))
    }

    @Test
    fun testAutoLockTimeoutResolution() {
        assertEquals(AutoLockTimeout.IMMEDIATE, AutoLockTimeout.fromSeconds(0L))
        assertEquals(AutoLockTimeout.THIRTY_SEC, AutoLockTimeout.fromSeconds(30L))
        assertEquals(AutoLockTimeout.ONE_MIN, AutoLockTimeout.fromSeconds(60L))
        assertEquals(AutoLockTimeout.FIVE_MIN, AutoLockTimeout.fromSeconds(300L))
        assertEquals(AutoLockTimeout.FIFTEEN_MIN, AutoLockTimeout.fromSeconds(900L))
        assertEquals(AutoLockTimeout.NEVER, AutoLockTimeout.fromSeconds(-1L))
    }
}
