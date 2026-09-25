package com.example.core.backup

import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import com.example.data.database.entity.JudgeEntity
import com.example.data.database.entity.MediaDocumentEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.UserEntity
import com.example.data.database.entity.WeightRecordEntity

data class AviaryBackupMetadata(
    val formatVersion: Int = 1,
    val appVersion: String = "1.0.0",
    val backupTimestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val deviceName: String = "",
    val aviaryName: String = "Main Aviary",
    val latestRecordTimestamp: Long = 0L,
    val totalBirds: Int = 0,
    val totalPairs: Int = 0,
    val totalClutches: Int = 0,
    val totalHealthRecords: Int = 0,
    val totalFinancialRecords: Int = 0
)

data class AviaryFullBackup(
    val metadata: AviaryBackupMetadata,
    val birds: List<BirdEntity> = emptyList(),
    val cages: List<CageEntity> = emptyList(),
    val pairs: List<PairEntity> = emptyList(),
    val nests: List<NestEntity> = emptyList(),
    val clutches: List<ClutchEntity> = emptyList(),
    val eggs: List<EggEntity> = emptyList(),
    val chicks: List<ChickEntity> = emptyList(),
    val genetics: List<BirdGeneticsEntity> = emptyList(),
    val pedigrees: List<PedigreeRecordEntity> = emptyList(),
    val healthRecords: List<HealthRecordEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val weights: List<WeightRecordEntity> = emptyList(),
    val nutritionPlans: List<NutritionPlanEntity> = emptyList(),
    val nutritionRecords: List<NutritionRecordEntity> = emptyList(),
    val inventoryItems: List<InventoryItemEntity> = emptyList(),
    val inventoryTransactions: List<InventoryTransactionEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val incomes: List<IncomeEntity> = emptyList(),
    val competitions: List<CompetitionEntity> = emptyList(),
    val judges: List<JudgeEntity> = emptyList(),
    val competitionCriteria: List<CompetitionCriterionEntity> = emptyList(),
    val competitionScores: List<CompetitionScoreEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val mediaDocuments: List<MediaDocumentEntity> = emptyList(),
    val users: List<UserEntity> = emptyList()
)

enum class RestoreMode {
    SMART_MERGE,
    REPLACE_ALL
}

data class BackupInspection(
    val isValid: Boolean,
    val metadata: AviaryBackupMetadata?,
    val isOlderThanCurrent: Boolean = false,
    val ageDifferenceMillis: Long = 0L,
    val currentDbLatestTimestamp: Long = 0L,
    val currentDbBirdCount: Int = 0,
    val errorMessage: String? = null
)

data class RestoreResult(
    val isSuccess: Boolean,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val conflictsDetectedCount: Int = 0,
    val message: String = ""
)
