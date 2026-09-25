package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.common.CageType
import java.util.UUID

@Entity(tableName = "cages")
data class CageEntity(
    @PrimaryKey val code: String,
    val type: CageType = CageType.BREEDING_BOX,
    val capacity: Int = 2,
    val currentOccupancy: Int = 0,
    val location: String? = null,
    val isClean: Boolean = true,
    val lastCleanedDate: Long? = null,
    val photoUri: String? = null,
    val notes: String? = null,
    val syncId: String = UUID.randomUUID().toString(),
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
