package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val isSynced: Boolean = false,
    val retryCount: Int = 0
)

@Entity(tableName = "sync_conflicts")
data class SyncConflictEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val entityIdentifier: String, // e.g. Ring number "IR-2026-99" or title
    val localJson: String,
    val remoteJson: String,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val remoteDeviceId: String,
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null,
    val resolutionStrategy: String? = null, // "KEEP_LOCAL", "KEEP_REMOTE", "MERGED"
    val mergedJson: String? = null
)
