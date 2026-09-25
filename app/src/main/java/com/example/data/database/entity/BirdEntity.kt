package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import java.util.UUID

@Entity(
    tableName = "birds",
    indices = [
        Index(value = ["cageCode"]),
        Index(value = ["pairId"]),
        Index(value = ["fatherRing"]),
        Index(value = ["motherRing"]),
        Index(value = ["userId"])
    ]
)
data class BirdEntity(
    @PrimaryKey val ringNumber: String,
    val name: String? = null,
    val gender: BirdGender = BirdGender.UNKNOWN,
    val variety: BudgieVariety = BudgieVariety.ENGLISH_SHOW,
    val mutation: String = "Normal",
    val color: String = "Green",
    val birthDate: Long? = null,
    val placeOfBirth: String? = null,
    val generation: String? = null,
    val photoUri: String? = null,
    val cageCode: String? = null,
    val pairId: Long? = null,
    val status: BirdStatus = BirdStatus.ACTIVE,
    val fatherRing: String? = null,
    val motherRing: String? = null,
    val notes: String? = null,
    // Distributed sync, offline, and multi-user support
    val syncId: String = UUID.randomUUID().toString(),
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
