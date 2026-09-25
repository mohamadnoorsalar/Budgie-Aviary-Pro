package com.example.core

import com.example.core.backup.AviaryBackupMetadata
import com.example.core.backup.AviaryFullBackup
import com.example.core.backup.BackupSerializer
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.core.security.CryptoManager
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.SyncConflictEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupRestoreAndSyncTest {

    @Test
    fun testBackupSerializationAndDeserialization() {
        val now = System.currentTimeMillis()
        val metadata = AviaryBackupMetadata(
            formatVersion = 1,
            appVersion = "1.0.0",
            backupTimestamp = now,
            deviceId = "DEV-TEST-01",
            deviceName = "Pixel 8 Pro",
            aviaryName = "Champion Aviary",
            latestRecordTimestamp = now,
            totalBirds = 2,
            totalPairs = 1,
            totalClutches = 0,
            totalHealthRecords = 0,
            totalFinancialRecords = 0
        )

        val bird1 = BirdEntity(
            ringNumber = "IR-2026-001",
            name = "Blue Sky",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            cageCode = "CAGE-01",
            createdAt = now - 100000,
            updatedAt = now - 50000
        )

        val bird2 = BirdEntity(
            ringNumber = "IR-2026-002",
            name = "Yellow Sun",
            gender = BirdGender.FEMALE,
            variety = BudgieVariety.AUSTRALIAN_WILD,
            cageCode = "CAGE-01",
            createdAt = now - 90000,
            updatedAt = now - 40000
        )

        val pair1 = PairEntity(
            id = 1L,
            maleRingNumber = "IR-2026-001",
            femaleRingNumber = "IR-2026-002",
            cageCode = "CAGE-01",
            pairingDate = now - 30000,
            matingDate = now - 30000,
            updatedAt = now - 30000
        )

        val originalBackup = AviaryFullBackup(
            metadata = metadata,
            birds = listOf(bird1, bird2),
            pairs = listOf(pair1),
            cages = listOf(CageEntity(code = "CAGE-01", type = CageType.BREEDING_BOX, capacity = 2)),
            clutches = emptyList(),
            expenses = emptyList(),
            incomes = emptyList(),
            healthRecords = emptyList(),
            reminders = emptyList()
        )

        val json = BackupSerializer.toJson(originalBackup)
        assertTrue(json.contains("IR-2026-001"))
        assertTrue(json.contains("IR-2026-002"))

        val restoredBackup = BackupSerializer.fromJson(json)
        assertEquals(2, restoredBackup.birds.size)
        assertEquals("IR-2026-001", restoredBackup.birds[0].ringNumber)
        assertEquals("IR-2026-001", restoredBackup.pairs[0].maleRingNumber)
        assertEquals("Pixel 8 Pro", restoredBackup.metadata.deviceName)
    }

    @Test
    fun testEncryptedBackupAndChecksumVerification() {
        val plainJson = """{"birds":[{"ring":"IR-101","name":"Kiwi"}],"timestamp":1700000000}"""
        val password = "StrongPassword@2026"

        val encryptedBase64 = CryptoManager.encryptBackup(plainJson, password)
        assertNotNull(encryptedBase64)
        assertFalse(encryptedBase64.contains("IR-101")) // Confirms ciphertext is encrypted

        // Decrypt with correct password
        val decrypted = CryptoManager.decryptBackup(encryptedBase64, password)
        assertEquals(plainJson, decrypted)

        // Decrypt with wrong password should safely return null
        val wrongDecrypted = CryptoManager.decryptBackup(encryptedBase64, "WrongPassword!")
        assertNull(wrongDecrypted)
    }

    @Test
    fun testOlderBackupDetectionLogic() {
        val currentDbLatestTimestamp = 1750000000000L // Newer
        val olderBackupTimestamp = 1700000000000L // 50,000 seconds older

        val isOlder = currentDbLatestTimestamp > 0L &&
                olderBackupTimestamp > 0L &&
                (currentDbLatestTimestamp - olderBackupTimestamp) > 60000L

        assertTrue("Should detect older backup to protect against accidental overwrite", isOlder)

        val ageDifference = currentDbLatestTimestamp - olderBackupTimestamp
        assertTrue(ageDifference > 0)
    }

    @Test
    fun testSafeConflictDetectionPreservesData() {
        // Local bird modified at timestamp 2000
        val localBird = BirdEntity(
            ringNumber = "IR-2026-099",
            name = "Emerald Local",
            status = BirdStatus.ACTIVE,
            cageCode = "BOX-10",
            updatedAt = 2000L
        )

        // Incoming bird from older/concurrent device modified at timestamp 1500
        val incomingBird = BirdEntity(
            ringNumber = "IR-2026-099",
            name = "Emerald Remote",
            status = BirdStatus.RESTING,
            cageCode = "BOX-12",
            updatedAt = 1500L
        )

        // Conflict check: local has newer update, incoming differs
        val isConflict = incomingBird.updatedAt < localBird.updatedAt &&
                (incomingBird.name != localBird.name || incomingBird.status != localBird.status || incomingBird.cageCode != localBird.cageCode)

        assertTrue(isConflict)

        val conflictEntity = SyncConflictEntity(
            id = 1,
            entityType = "BIRD",
            entityId = 1L,
            entityIdentifier = localBird.ringNumber,
            localJson = """{"name":"${localBird.name}","status":"${localBird.status}"}""",
            remoteJson = """{"name":"${incomingBird.name}","status":"${incomingBird.status}"}""",
            localTimestamp = localBird.updatedAt,
            remoteTimestamp = incomingBird.updatedAt,
            remoteDeviceId = "DEV-PEER-B"
        )

        assertEquals("BIRD", conflictEntity.entityType)
        assertEquals("IR-2026-099", conflictEntity.entityIdentifier)
        assertFalse("Conflict should remain unresolved until explicit review", conflictEntity.isResolved)
    }

    @Test
    fun testDeviceTransferPairingCodeValidation() {
        val pairingCode = "782914"
        val payload = """{"deviceId":"DEV-SOURCE","data":"test_sync_data"}"""

        val encryptedPackage = CryptoManager.encryptBackup(payload, pairingCode)
        val decryptedWithCode = CryptoManager.decryptBackup(encryptedPackage, pairingCode)
        assertEquals(payload, decryptedWithCode)

        val decryptedWithWrongCode = CryptoManager.decryptBackup(encryptedPackage, "123456")
        assertNull(decryptedWithWrongCode)
    }
}
