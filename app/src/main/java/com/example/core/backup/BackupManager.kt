package com.example.core.backup

import android.content.Context
import android.os.Build
import com.example.core.security.CryptoManager
import com.example.data.database.AppDatabase
import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.SyncConflictEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AutoBackupSnapshot(
    val fileName: String,
    val timestamp: Long,
    val formattedDate: String,
    val sizeBytes: Long,
    val file: File
)

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val autoBackupDir = File(context.filesDir, "auto_backups").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("aviary_backup_prefs", Context.MODE_PRIVATE)

    var isAutoBackupEnabled: Boolean
        get() = prefs.getBoolean("auto_backup_enabled", true)
        set(value) = prefs.edit().putBoolean("auto_backup_enabled", value).apply()

    var autoBackupFrequencyDays: Int
        get() = prefs.getInt("auto_backup_freq_days", 1) // 1 = daily
        set(value) = prefs.edit().putInt("auto_backup_freq_days", value).apply()

    var lastAutoBackupTimestamp: Long
        get() = prefs.getLong("last_auto_backup_ts", 0L)
        set(value) = prefs.edit().putLong("last_auto_backup_ts", value).apply()

    private val deviceId: String
        get() {
            var id = prefs.getString("device_sync_id", null)
            if (id == null) {
                id = "DEV-" + UUID.randomUUID().toString().take(8).uppercase()
                prefs.edit().putString("device_sync_id", id).apply()
            }
            return id
        }

    /**
     * Gathers all data across database tables and builds a full backup object.
     */
    suspend fun buildFullBackupObject(): AviaryFullBackup = withContext(Dispatchers.IO) {
        val birds = database.birdDao().getAllBirds().first()
        val pairs = database.pairDao().getAllPairs().first()
        val cages = database.cageDao().getAllCages().first()
        val clutches = database.clutchDao().getActiveClutches().first()
        val expenses = database.financeDao().getAllExpenses().first()
        val incomes = database.financeDao().getAllIncome().first()
        val healthRecords = database.healthDao().getAllHealthRecords().first()
        val reminders = database.reminderDao().getAllReminders().first()

        var latestTimestamp = 0L
        birds.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.createdAt) }
        pairs.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.pairingDate) }
        cages.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt) }
        clutches.forEach { latestTimestamp = maxOf(latestTimestamp, it.startDate) }
        expenses.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.date) }
        incomes.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.date) }
        healthRecords.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.recordDate) }
        reminders.forEach { latestTimestamp = maxOf(latestTimestamp, it.updatedAt, it.dueDate) }

        if (latestTimestamp == 0L) {
            latestTimestamp = System.currentTimeMillis()
        }

        val metadata = AviaryBackupMetadata(
            formatVersion = 1,
            appVersion = "1.0.0",
            backupTimestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            aviaryName = "Budgie Aviary Master",
            latestRecordTimestamp = latestTimestamp,
            totalBirds = birds.size,
            totalPairs = pairs.size,
            totalClutches = clutches.size,
            totalHealthRecords = healthRecords.size,
            totalFinancialRecords = expenses.size + incomes.size
        )

        AviaryFullBackup(
            metadata = metadata,
            birds = birds,
            pairs = pairs,
            cages = cages,
            clutches = clutches,
            expenses = expenses,
            incomes = incomes,
            healthRecords = healthRecords,
            reminders = reminders
        )
    }

    /**
     * Exports full encrypted backup string.
     */
    suspend fun createEncryptedBackup(password: String): String = withContext(Dispatchers.IO) {
        require(password.length >= 4) { "Password must be at least 4 characters" }
        val backup = buildFullBackupObject()
        val json = BackupSerializer.toJson(backup)
        val encrypted = CryptoManager.encryptBackup(json, password)

        // Log audit event
        database.auditLogDao().insertLog(
            AuditLogEntity(
                actionType = "BACKUP_EXPORT",
                entityType = "BACKUP",
                entityId = "ALL",
                summary = "Created encrypted backup containing ${backup.birds.size} birds, ${backup.pairs.size} pairs"
            )
        )

        encrypted
    }

    /**
     * Inspects backup header and compares timestamps against current database state.
     */
    suspend fun inspectEncryptedBackup(
        encryptedData: String,
        password: String
    ): BackupInspection = withContext(Dispatchers.IO) {
        val decryptedJson = CryptoManager.decryptBackup(encryptedData, password)
            ?: return@withContext BackupInspection(
                isValid = false,
                metadata = null,
                errorMessage = "Incorrect password or corrupted backup checksum"
            )

        return@withContext try {
            val backup = BackupSerializer.fromJson(decryptedJson)
            val currentBirds = database.birdDao().getAllBirds().first()
            var currentDbLatestTs = 0L
            currentBirds.forEach { currentDbLatestTs = maxOf(currentDbLatestTs, it.updatedAt, it.createdAt) }

            val isOlder = currentDbLatestTs > 0L &&
                    backup.metadata.latestRecordTimestamp > 0L &&
                    (currentDbLatestTs - backup.metadata.latestRecordTimestamp) > 60000L // > 1 min older

            val ageDiff = if (isOlder) currentDbLatestTs - backup.metadata.latestRecordTimestamp else 0L

            BackupInspection(
                isValid = true,
                metadata = backup.metadata,
                isOlderThanCurrent = isOlder,
                ageDifferenceMillis = ageDiff,
                currentDbLatestTimestamp = currentDbLatestTs,
                currentDbBirdCount = currentBirds.size
            )
        } catch (e: Exception) {
            BackupInspection(
                isValid = false,
                metadata = null,
                errorMessage = "Failed to parse backup contents: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Restores backup using either SMART_MERGE (prevents overwriting newer data / records conflicts)
     * or REPLACE_ALL (full overwrite with warning).
     */
    suspend fun restoreBackup(
        encryptedData: String,
        password: String,
        mode: RestoreMode
    ): RestoreResult = withContext(Dispatchers.IO) {
        val decryptedJson = CryptoManager.decryptBackup(encryptedData, password)
            ?: return@withContext RestoreResult(
                isSuccess = false,
                message = "Decryption failed. Please verify password."
            )

        val backup = try {
            BackupSerializer.fromJson(decryptedJson)
        } catch (e: Exception) {
            return@withContext RestoreResult(
                isSuccess = false,
                message = "Corrupted backup structure: ${e.message}"
            )
        }

        var insertedCount = 0
        var updatedCount = 0
        var conflictsCount = 0

        if (mode == RestoreMode.REPLACE_ALL) {
            // Replace mode: insert/replace entities
            backup.birds.forEach {
                database.birdDao().insertBird(it)
                insertedCount++
            }
            backup.pairs.forEach { database.pairDao().insertPair(it) }
            backup.cages.forEach { database.cageDao().insertCage(it) }
            backup.clutches.forEach { database.clutchDao().insertClutch(it) }
            backup.expenses.forEach { database.financeDao().insertExpense(it) }
            backup.incomes.forEach { database.financeDao().insertIncome(it) }
            backup.healthRecords.forEach { database.healthDao().insertHealthRecord(it) }
            backup.reminders.forEach { database.reminderDao().insertReminder(it) }
        } else {
            // Smart Merge Mode
            val existingBirds = database.birdDao().getAllBirds().first().associateBy { it.ringNumber }
            backup.birds.forEach { incoming ->
                val local = existingBirds[incoming.ringNumber]
                if (local == null) {
                    database.birdDao().insertBird(incoming)
                    insertedCount++
                } else if (incoming.updatedAt > local.updatedAt) {
                    database.birdDao().updateBird(incoming)
                    updatedCount++
                } else if (incoming.updatedAt < local.updatedAt && (incoming.name != local.name || incoming.status != local.status || incoming.cageCode != local.cageCode)) {
                    // Safe Conflict Detection: Do not silently delete or overwrite!
                    conflictsCount++
                    database.syncConflictDao().insertConflict(
                        SyncConflictEntity(
                            entityType = "BIRD",
                            entityId = 0L,
                            entityIdentifier = local.ringNumber,
                            localJson = """{"ring":"${local.ringNumber}","name":"${local.name ?: ""}","status":"${local.status}","cage":"${local.cageCode ?: ""}","updatedAt":${local.updatedAt}}""",
                            remoteJson = """{"ring":"${incoming.ringNumber}","name":"${incoming.name ?: ""}","status":"${incoming.status}","cage":"${incoming.cageCode ?: ""}","updatedAt":${incoming.updatedAt}}""",
                            localTimestamp = local.updatedAt,
                            remoteTimestamp = incoming.updatedAt,
                            remoteDeviceId = backup.metadata.deviceId
                        )
                    )
                }
            }

            // Merge Pairs
            val existingPairs = database.pairDao().getAllPairs().first()
            val existingPairsMap = existingPairs.associateBy { "${it.maleRingNumber}_${it.femaleRingNumber}" }
            backup.pairs.forEach { incoming ->
                val key = "${incoming.maleRingNumber}_${incoming.femaleRingNumber}"
                val local = existingPairsMap[key]
                if (local == null) {
                    database.pairDao().insertPair(incoming.copy(id = 0L))
                    insertedCount++
                }
            }

            // Merge Cages
            val existingCages = database.cageDao().getAllCages().first().associateBy { it.code }
            backup.cages.forEach { incoming ->
                val local = existingCages[incoming.code]
                if (local == null) {
                    database.cageDao().insertCage(incoming)
                    insertedCount++
                }
            }

            // Merge Finances
            backup.expenses.forEach {
                database.financeDao().insertExpense(it)
                insertedCount++
            }
            backup.incomes.forEach {
                database.financeDao().insertIncome(it)
                insertedCount++
            }
            backup.healthRecords.forEach {
                database.healthDao().insertHealthRecord(it)
                insertedCount++
            }
            backup.reminders.forEach {
                database.reminderDao().insertReminder(it.copy(id = 0L))
                insertedCount++
            }
        }

        // Record audit
        database.auditLogDao().insertLog(
            AuditLogEntity(
                actionType = "BACKUP_RESTORE",
                entityType = "BACKUP",
                entityId = "ALL",
                summary = "Restored backup in $mode mode. Inserted: $insertedCount, Updated: $updatedCount, Conflicts: $conflictsCount"
            )
        )

        RestoreResult(
            isSuccess = true,
            insertedCount = insertedCount,
            updatedCount = updatedCount,
            conflictsDetectedCount = conflictsCount,
            message = "Successfully restored ${backup.metadata.totalBirds} bird records"
        )
    }

    /**
     * Automatic Backup snapshot generator.
     */
    suspend fun createAutoSnapshot(passphrase: String = "AviaryAutoBackupPass_2026"): File? = withContext(Dispatchers.IO) {
        if (!isAutoBackupEnabled) return@withContext null

        return@withContext try {
            val backup = buildFullBackupObject()
            val json = BackupSerializer.toJson(backup)
            val encrypted = CryptoManager.encryptBackup(json, passphrase)

            val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "auto_backup_${formatter.format(Date())}.aviary"
            val file = File(autoBackupDir, fileName)
            file.writeText(encrypted)

            lastAutoBackupTimestamp = System.currentTimeMillis()
            cleanOldSnapshots(maxKeep = 7)
            file
        } catch (_: Exception) {
            null
        }
    }

    fun listAutoSnapshots(): List<AutoBackupSnapshot> {
        val files = autoBackupDir.listFiles { _, name -> name.endsWith(".aviary") } ?: return emptyList()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return files.map { f ->
            AutoBackupSnapshot(
                fileName = f.name,
                timestamp = f.lastModified(),
                formattedDate = formatter.format(Date(f.lastModified())),
                sizeBytes = f.length(),
                file = f
            )
        }.sortedByDescending { it.timestamp }
    }

    private fun cleanOldSnapshots(maxKeep: Int = 7) {
        val files = autoBackupDir.listFiles { _, name -> name.endsWith(".aviary") } ?: return
        if (files.size > maxKeep) {
            files.sortedBy { it.lastModified() }
                .take(files.size - maxKeep)
                .forEach { it.delete() }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BackupManager? = null

        fun getInstance(context: Context, database: AppDatabase): BackupManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackupManager(context.applicationContext, database).also { INSTANCE = it }
            }
        }
    }
}
