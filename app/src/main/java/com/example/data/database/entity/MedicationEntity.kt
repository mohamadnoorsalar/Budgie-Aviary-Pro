package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "medications",
    indices = [
        Index(value = ["healthRecordId"]),
        Index(value = ["birdRingNumber"])
    ]
)
data class MedicationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val healthRecordId: String? = null,
    val birdRingNumber: String? = null,
    val medicationName: String,
    val dosage: String,
    val administrationRoute: String = "WATER", // WATER, TOPICAL, ORAL, CROP_NEEDLE, FOOD
    val frequency: String = "ONCE_DAILY",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 7 * 86400000L,
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
