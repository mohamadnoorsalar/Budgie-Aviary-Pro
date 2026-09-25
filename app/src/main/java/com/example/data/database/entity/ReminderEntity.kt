package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import java.util.UUID

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["dueDate"]),
        Index(value = ["isCompleted"]),
        Index(value = ["type"]),
        Index(value = ["dedupKey"]),
        Index(value = ["relatedRingNumber"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long,
    val priority: ReminderPriority = ReminderPriority.NORMAL,
    val type: ReminderType = ReminderType.DAILY_TASK,
    val isCompleted: Boolean = false,
    val relatedRingNumber: String? = null,
    val pairId: Long? = null,
    val cageCode: String? = null,
    val eggId: String? = null,
    val inventoryItemId: String? = null,
    val repeatIntervalDays: Int = 0, // 0 = one-time, 1 = daily, 7 = weekly, etc.
    val isNotificationEnabled: Boolean = true,
    val dedupKey: String? = null,
    val completedAt: Long? = null,
    val syncId: String = UUID.randomUUID().toString(),
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
