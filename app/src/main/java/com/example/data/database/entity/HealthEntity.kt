package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "health_records",
    indices = [
        Index(value = ["birdRingNumber"]),
        Index(value = ["cageCode"]),
        Index(value = ["recordDate"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val birdRingNumber: String? = null,
    val cageCode: String? = null,
    val recordDate: Long = System.currentTimeMillis(),
    val recordType: String = "EXAMINATION", // EXAMINATION, QUARANTINE, ILLNESS, VACCINATION, INJURY
    val symptoms: String,
    val diagnosis: String,
    val recordedProblem: String = diagnosis,
    val veterinarianName: String? = null,
    val medicationName: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val treatmentDurationDays: Int = 0,
    val startDate: Long = recordDate,
    val endDate: Long? = null,
    val supplements: String? = null,
    val isResolved: Boolean = false,
    val resolutionDate: Long? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
