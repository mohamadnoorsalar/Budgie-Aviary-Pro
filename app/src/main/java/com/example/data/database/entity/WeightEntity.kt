package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "weight_records",
    indices = [
        Index(value = ["birdRingNumber"]),
        Index(value = ["recordedDate"])
    ]
)
data class WeightRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val birdRingNumber: String,
    val weightGrams: Double,
    val recordedDate: Long = System.currentTimeMillis(),
    val conditionScore: String = "OPTIMAL", // UNDERWEIGHT, OPTIMAL, OVERWEIGHT, OBESE
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
