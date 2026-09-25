package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.BirdGeneticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneticsDao {
    @Query("SELECT * FROM bird_genetics WHERE birdRingNumber = :ringNumber AND isDeleted = 0 LIMIT 1")
    fun getGeneticsForBird(ringNumber: String): Flow<BirdGeneticsEntity?>

    @Query("SELECT * FROM bird_genetics WHERE isDeleted = 0")
    fun getAllGenetics(): Flow<List<BirdGeneticsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenetics(genetics: BirdGeneticsEntity)

    @Update
    suspend fun updateGenetics(genetics: BirdGeneticsEntity)

    @Delete
    suspend fun deleteGenetics(genetics: BirdGeneticsEntity)
}
