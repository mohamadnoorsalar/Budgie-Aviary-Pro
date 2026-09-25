package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.SyncConflictEntity
import com.example.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingOperations(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingOperationsList(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE isSynced = 0")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncQueueEntity): Long

    @Update
    suspend fun updateOperation(operation: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE isSynced = 1 OR id = :id")
    suspend fun deleteOperation(id: Long)

    @Query("DELETE FROM sync_queue WHERE isSynced = 1")
    suspend fun clearCompletedOperations()
}

@Dao
interface SyncConflictDao {
    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0 ORDER BY localTimestamp DESC")
    fun getUnresolvedConflicts(): Flow<List<SyncConflictEntity>>

    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0 ORDER BY localTimestamp DESC")
    suspend fun getUnresolvedConflictsList(): List<SyncConflictEntity>

    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE isResolved = 0")
    fun getUnresolvedConflictCount(): Flow<Int>

    @Query("SELECT * FROM sync_conflicts ORDER BY localTimestamp DESC")
    fun getAllConflicts(): Flow<List<SyncConflictEntity>>

    @Query("SELECT * FROM sync_conflicts WHERE id = :id")
    suspend fun getConflictById(id: Long): SyncConflictEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity): Long

    @Update
    suspend fun updateConflict(conflict: SyncConflictEntity)

    @Query("DELETE FROM sync_conflicts WHERE id = :id")
    suspend fun deleteConflict(id: Long)

    @Query("DELETE FROM sync_conflicts WHERE isResolved = 1")
    suspend fun clearResolvedConflicts()
}
