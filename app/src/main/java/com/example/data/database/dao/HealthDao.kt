package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.HealthRecordWithMedications
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // --- Health Records (Bird -> Health) ---
    @Query("SELECT * FROM health_records WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getHealthRecordsForBird(ringNumber: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE isDeleted = 0 ORDER BY recordDate DESC")
    fun getAllHealthRecords(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE isResolved = 0 AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getActiveHealthIssues(): Flow<List<HealthRecordEntity>>

    @Transaction
    @Query("SELECT * FROM health_records WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getHealthRecordWithMedications(id: String): Flow<HealthRecordWithMedications?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity)

    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    // --- Medications ---
    @Query("SELECT * FROM medications WHERE healthRecordId = :healthId AND isDeleted = 0")
    fun getMedicationsForHealthRecord(healthId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE birdRingNumber = :ringNumber AND isDeleted = 0")
    fun getMedicationsForBird(ringNumber: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY endDate ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    // --- Weight History (Bird -> Weight History) ---
    @Query("SELECT * FROM weight_records WHERE isDeleted = 0 ORDER BY recordedDate DESC")
    fun getAllWeightRecords(): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY recordedDate DESC")
    fun getWeightHistoryForBird(ringNumber: String): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY recordedDate DESC LIMIT 1")
    fun getLatestWeightForBird(ringNumber: String): Flow<WeightRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(record: WeightRecordEntity)

    @Delete
    suspend fun deleteWeight(record: WeightRecordEntity)
}
