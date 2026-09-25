package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.NutritionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    // --- Plans ---
    @Query("SELECT * FROM nutrition_plans WHERE isDeleted = 0 ORDER BY planName ASC")
    fun getAllNutritionPlans(): Flow<List<NutritionPlanEntity>>

    @Query("SELECT * FROM nutrition_plans WHERE seasonPhase = :season AND isDeleted = 0")
    fun getPlansForSeason(season: String): Flow<List<NutritionPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionPlan(plan: NutritionPlanEntity)

    @Update
    suspend fun updateNutritionPlan(plan: NutritionPlanEntity)

    @Delete
    suspend fun deleteNutritionPlan(plan: NutritionPlanEntity)

    // --- Records (Bird / Cage / Aviary logs) ---
    @Query("SELECT * FROM nutrition_records WHERE isDeleted = 0 ORDER BY recordDate DESC")
    fun getAllNutritionRecords(): Flow<List<NutritionRecordEntity>>

    @Query("SELECT * FROM nutrition_records WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getNutritionRecordsForBird(ringNumber: String): Flow<List<NutritionRecordEntity>>

    @Query("SELECT * FROM nutrition_records WHERE cageCode = :cageCode AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getNutritionRecordsForCage(cageCode: String): Flow<List<NutritionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionRecord(record: NutritionRecordEntity)

    @Update
    suspend fun updateNutritionRecord(record: NutritionRecordEntity)

    @Delete
    suspend fun deleteNutritionRecord(record: NutritionRecordEntity)
}
