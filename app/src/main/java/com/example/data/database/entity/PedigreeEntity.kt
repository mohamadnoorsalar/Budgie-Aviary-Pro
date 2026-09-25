package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pedigrees",
    indices = [
        Index(value = ["birdRingNumber"], unique = true),
        Index(value = ["sireRing"]),
        Index(value = ["damRing"])
    ]
)
data class PedigreeRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val birdRingNumber: String,
    val sireRing: String? = null,
    val damRing: String? = null,
    val paternalGrandsire: String? = null,
    val paternalGranddam: String? = null,
    val maternalGrandsire: String? = null,
    val maternalGranddam: String? = null,
    val breederName: String? = null,
    val breederCode: String? = null,
    val inbreedingCoefficient: Double = 0.0,
    val lineageNotes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
