package com.example.core.sync

import android.content.Context
import com.example.core.backup.BackupManager
import com.example.core.backup.BackupSerializer
import com.example.core.backup.RestoreMode
import com.example.core.backup.RestoreResult
import com.example.core.security.CryptoManager
import com.example.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.SecureRandom

data class TransferPackage(
    val pairingCode: String,
    val sourceDeviceId: String,
    val sourceDeviceName: String,
    val payloadEncryptedBase64: String,
    val timestamp: Long
)

class DeviceTransferManager(
    private val context: Context,
    private val database: AppDatabase,
    private val backupManager: BackupManager
) {
    private val random = SecureRandom()

    /**
     * Generates a 6-digit one-time transfer pairing code and encrypted payload.
     */
    suspend fun createTransferPackage(pairingCode: String): String = withContext(Dispatchers.IO) {
        val fullBackup = backupManager.buildFullBackupObject()
        val rawJson = BackupSerializer.toJson(fullBackup)
        val encryptedPayload = CryptoManager.encryptBackup(rawJson, pairingCode)

        val obj = JSONObject().apply {
            put("type", "AVIARY_DEVICE_TRANSFER")
            put("version", 1)
            put("pairingCode", pairingCode)
            put("sourceDeviceId", fullBackup.metadata.deviceId)
            put("sourceDeviceName", fullBackup.metadata.deviceName)
            put("timestamp", System.currentTimeMillis())
            put("encryptedPayload", encryptedPayload)
        }

        obj.toString()
    }

    /**
     * Receives and imports a transfer package from another device.
     */
    suspend fun importTransferPackage(
        packageJson: String,
        pairingCode: String,
        mode: RestoreMode = RestoreMode.SMART_MERGE
    ): RestoreResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val obj = JSONObject(packageJson)
            val encryptedPayload = obj.getString("encryptedPayload")
            backupManager.restoreBackup(encryptedPayload, pairingCode, mode)
        } catch (e: Exception) {
            RestoreResult(
                isSuccess = false,
                message = "Failed to import transfer package: ${e.localizedMessage}"
            )
        }
    }

    fun generatePairingCode(): String {
        val num = random.nextInt(900000) + 100000
        return num.toString()
    }
}
