package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "eggs",
    indices = [
        Index(value = ["pairId"]),
        Index(value = ["clutchId"]),
        Index(value = ["nestId"])
    ]
)
data class EggEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pairId: Long,
    val clutchId: Long? = null,
    val nestId: String? = null,
    val eggNumber: Int,
    val layDate: Long = System.currentTimeMillis(),
    val fertilityStatus: String = "UNCANDLED", // UNCANDLED, FERTILE, INFERTILE, DEAD_IN_SHELL
    val candlingDate: Long? = null,
    val expectedHatchDate: Long? = null,
    val actualHatchDate: Long? = null,
    val isHatched: Boolean = false,
    val eggResult: String? = null, // HATCHED, INFERTILE, DEAD_IN_SHELL, BROKEN, DISCARDED, PENDING
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
