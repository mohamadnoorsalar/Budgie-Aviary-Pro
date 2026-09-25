package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairDao {
    @Query("SELECT * FROM breeding_pairs ORDER BY pairingDate DESC")
    fun getAllPairs(): Flow<List<PairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE isActive = 1")
    fun getActivePairs(): Flow<List<PairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE id = :id LIMIT 1")
    fun getPairById(id: Long): Flow<PairEntity?>

    @androidx.room.Transaction
    @Query("SELECT * FROM breeding_pairs WHERE id = :id LIMIT 1")
    fun getPairWithBreedingDetails(id: Long): Flow<com.example.data.database.relation.PairWithBreedingDetails?>

    @androidx.room.Transaction
    @Query("SELECT * FROM breeding_pairs ORDER BY pairingDate DESC")
    fun getAllPairsWithBreedingDetails(): Flow<List<com.example.data.database.relation.PairWithBreedingDetails>>

    @Query("SELECT COUNT(*) FROM breeding_pairs WHERE isActive = 1")
    fun getActivePairCount(): Flow<Int>

    @Query("SELECT * FROM breeding_pairs WHERE maleRingNumber = :ringNumber OR femaleRingNumber = :ringNumber ORDER BY pairingDate DESC")
    fun getPairsForBird(ringNumber: String): Flow<List<PairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE cageCode = :cageCode ORDER BY pairingDate DESC")
    fun getPairsForCage(cageCode: String): Flow<List<PairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE cageCode = :cageCode AND isActive = 1 LIMIT 1")
    fun getActivePairForCage(cageCode: String): Flow<PairEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPair(pair: PairEntity): Long

    @Update
    suspend fun updatePair(pair: PairEntity)

    @Delete
    suspend fun deletePair(pair: PairEntity)
}

@Dao
interface CageDao {
    @Query("SELECT * FROM cages ORDER BY code ASC")
    fun getAllCages(): Flow<List<CageEntity>>

    @Query("SELECT * FROM cages WHERE code = :code LIMIT 1")
    fun getCageByCode(code: String): Flow<CageEntity?>

    @androidx.room.Transaction
    @Query("SELECT * FROM cages WHERE code = :code LIMIT 1")
    fun getCageWithDetails(code: String): Flow<com.example.data.database.relation.CageWithDetails?>

    @androidx.room.Transaction
    @Query("SELECT * FROM cages ORDER BY code ASC")
    fun getAllCagesWithDetails(): Flow<List<com.example.data.database.relation.CageWithDetails>>

    @Query("SELECT COUNT(*) FROM cages")
    fun getCageCount(): Flow<Int>

    @Query("SELECT * FROM cages WHERE code = :code LIMIT 1")
    suspend fun getCageSync(code: String): CageEntity?

    @Query("UPDATE cages SET isClean = :isClean, lastCleanedDate = :timestamp WHERE code = :code")
    suspend fun updateCleanStatus(code: String, isClean: Boolean, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCage(cage: CageEntity)

    @Update
    suspend fun updateCage(cage: CageEntity)

    @Delete
    suspend fun deleteCage(cage: CageEntity)
}

@Dao
interface ClutchDao {
    @Query("SELECT * FROM clutches WHERE isActive = 1")
    fun getActiveClutches(): Flow<List<ClutchEntity>>

    @Query("SELECT COALESCE(SUM(eggCount), 0) FROM clutches WHERE isActive = 1")
    fun getTotalActiveEggs(): Flow<Int>

    @Query("SELECT COALESCE(SUM(hatchedCount), 0) FROM clutches")
    fun getTotalHatchedChicks(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClutch(clutch: ClutchEntity): Long

    @Update
    suspend fun updateClutch(clutch: ClutchEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY completedAt DESC, dueDate DESC")
    fun getCompletedReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE type = :type ORDER BY dueDate ASC")
    fun getRemindersByType(type: com.example.core.common.ReminderType): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 0")
    fun getPendingReminderCount(): Flow<Int>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE dedupKey = :dedupKey AND isCompleted = 0 LIMIT 1")
    suspend fun findActiveByDedupKey(dedupKey: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun clearCompletedReminders()
}
