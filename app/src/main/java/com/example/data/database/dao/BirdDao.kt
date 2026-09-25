package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.BirdEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import com.example.data.database.relation.BirdWithParents
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllBirds(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE ringNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    suspend fun getBirdByRing(ringNumber: String): BirdEntity?

    @Query("SELECT COUNT(*) FROM birds WHERE isDeleted = 0")
    fun getBirdCount(): Flow<Int>

    @Query("SELECT * FROM birds WHERE cageCode = :cageCode AND isDeleted = 0")
    fun getBirdsInCage(cageCode: String): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE pairId = :pairId AND isDeleted = 0")
    fun getBirdsInPair(pairId: Long): Flow<List<BirdEntity>>

    // Bird -> Parents
    @Query("SELECT * FROM birds WHERE ringNumber IN (:fatherRing, :motherRing) AND isDeleted = 0")
    fun getParentsOfBird(fatherRing: String?, motherRing: String?): Flow<List<BirdEntity>>

    // Bird -> Children
    @Query("SELECT * FROM birds WHERE (fatherRing = :ringNumber OR motherRing = :ringNumber) AND isDeleted = 0")
    fun getChildrenOfBird(ringNumber: String): Flow<List<BirdEntity>>

    @Transaction
    @Query("SELECT * FROM birds WHERE ringNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    fun getBirdWithDetails(ringNumber: String): Flow<BirdWithDetails?>

    @Transaction
    @Query("SELECT * FROM birds WHERE ringNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    fun getBirdWithParents(ringNumber: String): Flow<BirdWithParents?>

    @Transaction
    @Query("SELECT * FROM birds WHERE ringNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    fun getBirdWithChildren(ringNumber: String): Flow<BirdWithChildren?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBird(bird: BirdEntity)

    @Update
    suspend fun updateBird(bird: BirdEntity)

    @Delete
    suspend fun deleteBird(bird: BirdEntity)

    @Query("UPDATE birds SET isDeleted = 1, updatedAt = :timestamp WHERE ringNumber = :ringNumber")
    suspend fun softDeleteBird(ringNumber: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM birds WHERE ringNumber = :ringNumber")
    suspend fun deleteBirdByRing(ringNumber: String)
}
