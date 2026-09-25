package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

object InventoryCategory {
    const val FOOD = "FOOD"
    const val SUPPLEMENTS = "SUPPLEMENTS"
    const val MEDICINE = "MEDICINE"
    const val EQUIPMENT = "EQUIPMENT"
    const val CONSUMABLES = "CONSUMABLES"

    val ALL = listOf(FOOD, SUPPLEMENTS, MEDICINE, EQUIPMENT, CONSUMABLES)
}

object StockTransactionType {
    const val STOCK_IN = "STOCK_IN"
    const val STOCK_OUT = "STOCK_OUT"
    const val CONSUMPTION = "CONSUMPTION"
    const val ADJUSTMENT = "ADJUSTMENT"
}

@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["category"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sku: String,
    val name: String,
    val category: String = InventoryCategory.FOOD, // FOOD, SUPPLEMENTS, MEDICINE, EQUIPMENT, CONSUMABLES
    val currentStock: Double = 0.0,
    val unit: String = "KG", // KG, G, PIECES, LITER, ML, PACK
    val minStockThreshold: Double = 5.0,
    val costPerUnit: Double = 0.0,
    val expirationDate: Long? = null,
    val storageLocation: String? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inventory_transactions",
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["date"]),
        Index(value = ["transactionType"])
    ]
)
data class InventoryTransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val transactionType: String = StockTransactionType.CONSUMPTION, // STOCK_IN, STOCK_OUT, CONSUMPTION, ADJUSTMENT
    val quantity: Double,
    val previousStock: Double = 0.0,
    val newStock: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalCost: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val referenceType: String? = null, // NUTRITION, MEDICATION, PURCHASE, MANUAL
    val referenceId: String? = null,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
