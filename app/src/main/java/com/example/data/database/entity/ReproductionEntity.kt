package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clutches")
data class ClutchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairId: Long,
    val clutchNumber: Int = 1,
    val matingDate: Long? = null,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val eggCount: Int = 0,
    val fertileCount: Int = 0,
    val hatchedCount: Int = 0,
    val isActive: Boolean = true,
    val notes: String? = null
)
