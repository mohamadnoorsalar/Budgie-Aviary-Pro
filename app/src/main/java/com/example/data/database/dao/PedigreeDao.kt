package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.PedigreeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedigreeDao {
    @Query("SELECT * FROM pedigrees WHERE birdRingNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    fun getPedigreeForBird(ringNumber: String): Flow<PedigreeRecordEntity?>

    @Query("SELECT * FROM pedigrees WHERE (sireRing = :parentRing OR damRing = :parentRing) AND isDeleted = 0")
    fun getPedigreesWithParent(parentRing: String): Flow<List<PedigreeRecordEntity>>

    @Query("SELECT * FROM pedigrees WHERE isDeleted = 0")
    fun getAllPedigrees(): Flow<List<PedigreeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedigree(pedigree: PedigreeRecordEntity)

    @Update
    suspend fun updatePedigree(pedigree: PedigreeRecordEntity)

    @Delete
    suspend fun deletePedigree(pedigree: PedigreeRecordEntity)
}
