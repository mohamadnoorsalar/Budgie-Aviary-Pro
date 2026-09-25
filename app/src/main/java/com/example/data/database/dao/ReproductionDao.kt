package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.relation.EggWithChick
import com.example.data.database.relation.PairWithBreedingDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ReproductionDao {
    // --- Nests ---
    @Query("SELECT * FROM nests WHERE isDeleted = 0 ORDER BY boxNumber ASC")
    fun getAllNests(): Flow<List<NestEntity>>

    @Query("SELECT * FROM nests WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getNestById(id: String): Flow<NestEntity?>

    @Query("SELECT * FROM nests WHERE cageCode = :cageCode AND isDeleted = 0 LIMIT 1")
    fun getNestInCage(cageCode: String): Flow<NestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNest(nest: NestEntity)

    @Update
    suspend fun updateNest(nest: NestEntity)

    @Delete
    suspend fun deleteNest(nest: NestEntity)

    @Query("UPDATE nests SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteNest(id: String)

    // --- Eggs (Pair -> Eggs) ---
    @Query("SELECT * FROM eggs WHERE isDeleted = 0 ORDER BY layDate DESC")
    fun getAllEggs(): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getEggById(id: String): Flow<EggEntity?>

    @Query("SELECT * FROM eggs WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getEggSync(id: String): EggEntity?

    @Query("SELECT * FROM eggs WHERE pairId = :pairId AND isDeleted = 0 ORDER BY eggNumber ASC")
    fun getEggsForPair(pairId: Long): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE clutchId = :clutchId AND isDeleted = 0 ORDER BY eggNumber ASC")
    fun getEggsForClutch(clutchId: Long): Flow<List<EggEntity>>

    @Query("SELECT COUNT(*) FROM eggs WHERE isDeleted = 0")
    fun getTotalEggCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM eggs WHERE fertilityStatus = 'FERTILE' AND isDeleted = 0")
    fun getFertileEggCount(): Flow<Int>

    @Query("SELECT * FROM eggs WHERE isHatched = 0 AND isDeleted = 0 AND fertilityStatus != 'INFERTILE' ORDER BY COALESCE(expectedHatchDate, layDate + 1555200000) ASC LIMIT :limit")
    fun getUpcomingHatchEggs(limit: Int = 10): Flow<List<EggEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEgg(egg: EggEntity)

    @Update
    suspend fun updateEgg(egg: EggEntity)

    @Delete
    suspend fun deleteEgg(egg: EggEntity)

    @Query("UPDATE eggs SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteEgg(id: String)

    // --- Chicks (Egg -> Chick, Pair -> Chicks) ---
    @Query("SELECT * FROM chicks WHERE isDeleted = 0 ORDER BY hatchDate DESC")
    fun getAllChicks(): Flow<List<ChickEntity>>

    @Query("SELECT * FROM chicks WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getChickById(id: String): Flow<ChickEntity?>

    @Query("SELECT * FROM chicks WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getChickSync(id: String): ChickEntity?

    @Query("SELECT * FROM chicks WHERE pairId = :pairId AND isDeleted = 0 ORDER BY hatchOrder ASC")
    fun getChicksForPair(pairId: Long): Flow<List<ChickEntity>>

    @Query("SELECT * FROM chicks WHERE eggId = :eggId AND isDeleted = 0 LIMIT 1")
    fun getChickForEgg(eggId: String): Flow<ChickEntity?>

    @Query("SELECT COUNT(*) FROM chicks WHERE isDeleted = 0")
    fun getTotalChicksCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChick(chick: ChickEntity)

    @Update
    suspend fun updateChick(chick: ChickEntity)

    @Delete
    suspend fun deleteChick(chick: ChickEntity)

    @Query("UPDATE chicks SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteChick(id: String)

    // --- Clutches ---
    @Query("SELECT * FROM clutches ORDER BY startDate DESC")
    fun getAllClutches(): Flow<List<ClutchEntity>>

    @Query("SELECT * FROM clutches WHERE pairId = :pairId AND isActive = 1 LIMIT 1")
    fun getActiveClutchForPair(pairId: Long): Flow<ClutchEntity?>

    @Query("SELECT * FROM clutches WHERE pairId = :pairId ORDER BY clutchNumber ASC")
    fun getClutchesForPair(pairId: Long): Flow<List<ClutchEntity>>

    // --- Relational Queries ---
    @Transaction
    @Query("SELECT * FROM breeding_pairs WHERE id = :pairId AND isDeleted = 0 LIMIT 1")
    fun getPairWithBreedingDetails(pairId: Long): Flow<PairWithBreedingDetails?>

    @Transaction
    @Query("SELECT * FROM eggs WHERE id = :eggId AND isDeleted = 0 LIMIT 1")
    fun getEggWithChick(eggId: String): Flow<EggWithChick?>

    @Transaction
    @Query("SELECT * FROM chicks WHERE id = :chickId AND isDeleted = 0 LIMIT 1")
    fun getChickWithBreedingDetails(chickId: String): Flow<com.example.data.database.relation.ChickWithBreedingDetails?>

    @Transaction
    @Query("SELECT * FROM chicks WHERE isDeleted = 0 ORDER BY hatchDate DESC")
    fun getAllChicksWithBreedingDetails(): Flow<List<com.example.data.database.relation.ChickWithBreedingDetails>>
}
