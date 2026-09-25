package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["entityType", "entityId"]),
        Index(value = ["timestamp"]),
        Index(value = ["userId"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String? = "default_user",
    val actionType: String, // INSERT, UPDATE, DELETE, SYNC_PUSH, SYNC_PULL, BACKUP
    val entityType: String, // BIRD, PAIR, EGG, CHICK, HEALTH, MEDICATION, INVENTORY, FINANCE
    val entityId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String,
    val payloadJson: String? = null
)
