package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.AuditLogDao
import com.example.data.database.dao.BirdDao
import com.example.data.database.dao.CageDao
import com.example.data.database.dao.ClutchDao
import com.example.data.database.dao.CompetitionDao
import com.example.data.database.dao.FinanceDao
import com.example.data.database.dao.GeneticsDao
import com.example.data.database.dao.HealthDao
import com.example.data.database.dao.InventoryDao
import com.example.data.database.dao.MediaDocumentDao
import com.example.data.database.dao.NutritionDao
import com.example.data.database.dao.PairDao
import com.example.data.database.dao.PedigreeDao
import com.example.data.database.dao.ReminderDao
import com.example.data.database.dao.ReproductionDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entity.AuditLogEntity
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

@Database(
    entities = [
        BirdEntity::class,
        CageEntity::class,
        PairEntity::class,
        NestEntity::class,
        ClutchEntity::class,
        EggEntity::class,
        ChickEntity::class,
        BirdGeneticsEntity::class,
        PedigreeRecordEntity::class,
        HealthRecordEntity::class,
        MedicationEntity::class,
        WeightRecordEntity::class,
        NutritionPlanEntity::class,
        NutritionRecordEntity::class,
        InventoryItemEntity::class,
        InventoryTransactionEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        CompetitionEntity::class,
        JudgeEntity::class,
        CompetitionCriterionEntity::class,
        CompetitionScoreEntity::class,
        ReminderEntity::class,
        UserEntity::class,
        AuditLogEntity::class,
        MediaDocumentEntity::class,
        com.example.data.database.entity.SyncQueueEntity::class,
        com.example.data.database.entity.SyncConflictEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao
    abstract fun pairDao(): PairDao
    abstract fun cageDao(): CageDao
    abstract fun clutchDao(): ClutchDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reproductionDao(): ReproductionDao
    abstract fun geneticsDao(): GeneticsDao
    abstract fun pedigreeDao(): PedigreeDao
    abstract fun healthDao(): HealthDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun financeDao(): FinanceDao
    abstract fun competitionDao(): CompetitionDao
    abstract fun userDao(): UserDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun mediaDocumentDao(): MediaDocumentDao
    abstract fun syncQueueDao(): com.example.data.database.dao.SyncQueueDao
    abstract fun syncConflictDao(): com.example.data.database.dao.SyncConflictDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgie_aviary.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
