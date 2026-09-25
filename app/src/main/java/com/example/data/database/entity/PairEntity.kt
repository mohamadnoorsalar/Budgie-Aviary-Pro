package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "breeding_pairs",
    indices = [
        Index(value = ["maleRingNumber"]),
        Index(value = ["femaleRingNumber"]),
        Index(value = ["cageCode"]),
        Index(value = ["nestId"])
    ]
)
data class PairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val maleRingNumber: String,
    val femaleRingNumber: String,
    val cageCode: String? = null,
    val nestId: String? = null,
    val pairingDate: Long = System.currentTimeMillis(),
    val matingDate: Long? = null,
    val endDate: Long? = null,
    val status: String = "ACTIVE",
    val isActive: Boolean = true,
    val clutchCount: Int = 0,
    val totalChicks: Int = 0,
    val matingHistory: String? = null,
    val results: String? = null,
    val notes: String? = null,
    val syncId: String = UUID.randomUUID().toString(),
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
