package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE sku = :sku AND isDeleted = 0 LIMIT 1")
    suspend fun getItemBySku(sku: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minStockThreshold AND isDeleted = 0")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE category = :category AND isDeleted = 0 ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    // --- Stock Transactions & Consumption History ---
    @Query("SELECT * FROM inventory_transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE itemId = :itemId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE transactionType = :type AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<InventoryTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: InventoryTransactionEntity)
}
