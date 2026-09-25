package com.example.core.inventory

import com.example.data.database.entity.InventoryCategory
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import com.example.data.database.entity.StockTransactionType

enum class StockStatus {
    OPTIMAL,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class InventoryStockSummary(
    val totalItemsCount: Int,
    val lowStockCount: Int,
    val outOfStockCount: Int,
    val totalInventoryValue: Double,
    val categoryCounts: Map<String, Int>
)

object InventoryAnalyticsHelper {

    fun getStockStatus(item: InventoryItemEntity): StockStatus {
        return when {
            item.currentStock <= 0.0 -> StockStatus.OUT_OF_STOCK
            item.currentStock <= item.minStockThreshold -> StockStatus.LOW_STOCK
            else -> StockStatus.OPTIMAL
        }
    }

    fun isLowStock(item: InventoryItemEntity): Boolean {
        return item.currentStock <= item.minStockThreshold
    }

    fun computeSummary(items: List<InventoryItemEntity>): InventoryStockSummary {
        val activeItems = items.filter { !it.isDeleted }
        val lowStock = activeItems.count { it.currentStock in 0.001..it.minStockThreshold }
        val outOfStock = activeItems.count { it.currentStock <= 0.0 }
        val totalValue = activeItems.sumOf { it.currentStock * it.costPerUnit }

        val categoryCounts = mutableMapOf<String, Int>()
        InventoryCategory.ALL.forEach { categoryCounts[it] = 0 }
        activeItems.forEach { item ->
            val cat = if (InventoryCategory.ALL.contains(item.category)) item.category else InventoryCategory.CONSUMABLES
            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
        }

        return InventoryStockSummary(
            totalItemsCount = activeItems.size,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock,
            totalInventoryValue = totalValue,
            categoryCounts = categoryCounts
        )
    }

    /**
     * Prepares updated item and a transaction for Stock In (Restock).
     */
    fun buildStockIn(
        item: InventoryItemEntity,
        quantity: Double,
        unitPrice: Double = item.costPerUnit,
        notes: String? = null
    ): Pair<InventoryItemEntity, InventoryTransactionEntity> {
        val prev = item.currentStock
        val updatedStock = prev + quantity
        val newUnitCost = if (unitPrice > 0) unitPrice else item.costPerUnit

        val updatedItem = item.copy(
            currentStock = updatedStock,
            costPerUnit = newUnitCost,
            updatedAt = System.currentTimeMillis()
        )

        val tx = InventoryTransactionEntity(
            itemId = item.id,
            transactionType = StockTransactionType.STOCK_IN,
            quantity = quantity,
            previousStock = prev,
            newStock = updatedStock,
            unitPrice = newUnitCost,
            totalCost = quantity * newUnitCost,
            notes = notes,
            referenceType = "PURCHASE"
        )

        return Pair(updatedItem, tx)
    }

    /**
     * Prepares updated item and a transaction for Stock Out (Manual removal, disposal, transfer).
     */
    fun buildStockOut(
        item: InventoryItemEntity,
        quantity: Double,
        reason: String,
        notes: String? = null
    ): Pair<InventoryItemEntity, InventoryTransactionEntity> {
        val prev = item.currentStock
        val updatedStock = (prev - quantity).coerceAtLeast(0.0)

        val updatedItem = item.copy(
            currentStock = updatedStock,
            updatedAt = System.currentTimeMillis()
        )

        val tx = InventoryTransactionEntity(
            itemId = item.id,
            transactionType = StockTransactionType.STOCK_OUT,
            quantity = quantity,
            previousStock = prev,
            newStock = updatedStock,
            unitPrice = item.costPerUnit,
            totalCost = quantity * item.costPerUnit,
            notes = if (!notes.isNullOrBlank()) "$reason: $notes" else reason,
            referenceType = "MANUAL_OUT"
        )

        return Pair(updatedItem, tx)
    }

    /**
     * Prepares updated item and a transaction for Consumption (Diet feed, medication dosage, egg bedding).
     */
    fun buildConsumption(
        item: InventoryItemEntity,
        quantity: Double,
        referenceType: String? = "NUTRITION",
        referenceId: String? = null,
        notes: String? = null
    ): Pair<InventoryItemEntity, InventoryTransactionEntity> {
        val prev = item.currentStock
        val updatedStock = (prev - quantity).coerceAtLeast(0.0)

        val updatedItem = item.copy(
            currentStock = updatedStock,
            updatedAt = System.currentTimeMillis()
        )

        val tx = InventoryTransactionEntity(
            itemId = item.id,
            transactionType = StockTransactionType.CONSUMPTION,
            quantity = quantity,
            previousStock = prev,
            newStock = updatedStock,
            unitPrice = item.costPerUnit,
            totalCost = quantity * item.costPerUnit,
            referenceType = referenceType,
            referenceId = referenceId,
            notes = notes
        )

        return Pair(updatedItem, tx)
    }
}
