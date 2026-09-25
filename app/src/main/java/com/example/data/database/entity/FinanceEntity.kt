package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

object ExpenseCategory {
    const val BIRD_PURCHASE = "BIRD_PURCHASE"
    const val FOOD = "FOOD"
    const val SUPPLEMENTS = "SUPPLEMENTS"
    const val MEDICINE = "MEDICINE"
    const val EQUIPMENT = "EQUIPMENT"
    const val OTHER = "OTHER"

    val ALL = listOf(BIRD_PURCHASE, FOOD, SUPPLEMENTS, MEDICINE, EQUIPMENT, OTHER)
}

object IncomeCategory {
    const val BIRD_SALE = "BIRD_SALE"
    const val CHICK_SALE = "CHICK_SALE"
    const val OTHER = "OTHER"

    val ALL = listOf(BIRD_SALE, CHICK_SALE, OTHER)
}

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category"]),
        Index(value = ["date"]),
        Index(value = ["inventoryItemId"]),
        Index(value = ["pairId"]),
        Index(value = ["birdRingNumber"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String = ExpenseCategory.FOOD, // BIRD_PURCHASE, FOOD, SUPPLEMENTS, MEDICINE, EQUIPMENT, OTHER
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val inventoryItemId: String? = null,
    val pairId: String? = null, // link expense directly to a breeding pair
    val birdRingNumber: String? = null, // link expense to a specific bird
    val receiptNumber: String? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "income",
    indices = [
        Index(value = ["category"]),
        Index(value = ["date"]),
        Index(value = ["soldBirdRingNumber"]),
        Index(value = ["soldChickId"])
    ]
)
data class IncomeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String = IncomeCategory.BIRD_SALE, // BIRD_SALE, CHICK_SALE, OTHER
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val soldBirdRingNumber: String? = null,
    val soldChickId: String? = null,
    val buyerName: String? = null,
    val buyerContact: String? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
