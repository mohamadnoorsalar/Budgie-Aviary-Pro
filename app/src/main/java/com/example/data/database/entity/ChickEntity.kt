package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chicks",
    indices = [
        Index(value = ["eggId"], unique = true),
        Index(value = ["pairId"]),
        Index(value = ["bandedRingNumber"])
    ]
)
data class ChickEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eggId: String,
    val pairId: Long,
    val hatchDate: Long = System.currentTimeMillis(),
    val hatchOrder: Int = 1,
    val bandedRingNumber: String? = null,
    val bandingDate: Long? = null,
    val downColor: String? = null,
    val weightGrams: Double? = null,
    val growthStage: String = "HATCHLING", // HATCHLING, PIN_FEATHERS, BANDED, FLEDGLING, WEANED
    val status: String = "IN_NEST", // IN_NEST, WEANED, TRANSFERRED, DECEASED
    val mortalityDate: Long? = null,
    val mortalityReason: String? = null,
    val cageCode: String? = null,
    val transferDate: Long? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
