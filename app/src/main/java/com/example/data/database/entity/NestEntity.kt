package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "nests",
    indices = [
        Index(value = ["cageCode"]),
        Index(value = ["pairId"])
    ]
)
data class NestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val boxNumber: String,
    val cageCode: String? = null,
    val pairId: Long? = null,
    val nestMaterial: String = "Wood Shavings",
    val installedDate: Long = System.currentTimeMillis(),
    val isClean: Boolean = true,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
