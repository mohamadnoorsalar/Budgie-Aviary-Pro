package com.example.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.inventory.InventoryAnalyticsHelper
import com.example.core.inventory.InventoryStockSummary
import com.example.data.database.entity.InventoryCategory
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0: All Items, 1: Low-Stock Alerts, 2: Consumption History, 3: Transactions
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val rawInventory: StateFlow<List<InventoryItemEntity>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItemEntity>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<InventoryTransactionEntity>> = repository.allInventoryTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consumption transactions filtered
    val consumptionHistory: StateFlow<List<InventoryTransactionEntity>> = allTransactions.combine(_searchQuery) { txs, _ ->
        txs.filter { it.transactionType == "CONSUMPTION" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered inventory based on category and search query
    val filteredInventory: StateFlow<List<InventoryItemEntity>> = combine(
        rawInventory,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        items.filter { item ->
            val matchesCategory = (category == "ALL" || item.category == category)
            val matchesSearch = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true) ||
                    item.storageLocation?.contains(query, ignoreCase = true) == true
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockSummary: StateFlow<InventoryStockSummary> = rawInventory.combine(_selectedTab) { items, _ ->
        InventoryAnalyticsHelper.computeSummary(items)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        InventoryStockSummary(0, 0, 0, 0.0, emptyMap())
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.saveInventoryItem(item)
            repository.recordAuditLog(
                actionType = "SAVE_INVENTORY_ITEM",
                entityType = "INVENTORY",
                entityId = item.id,
                summary = "Saved item: ${item.name} (${item.sku})"
            )
        }
    }

    fun deleteItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
            repository.recordAuditLog(
                actionType = "DELETE_INVENTORY_ITEM",
                entityType = "INVENTORY",
                entityId = item.id,
                summary = "Deleted inventory item: ${item.name}"
            )
        }
    }

    fun recordStockIn(
        itemId: String,
        quantity: Double,
        unitPrice: Double,
        notes: String?,
        createExpense: Boolean
    ) {
        viewModelScope.launch {
            repository.recordStockIn(
                itemId = itemId,
                quantity = quantity,
                unitPrice = unitPrice,
                notes = notes,
                createExpense = createExpense
            )
            repository.recordAuditLog(
                actionType = "STOCK_IN",
                entityType = "INVENTORY",
                entityId = itemId,
                summary = "Stock in: +$quantity units @ $$unitPrice (Expense: $createExpense)"
            )
        }
    }

    fun recordStockOut(
        itemId: String,
        quantity: Double,
        reason: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.recordStockOut(
                itemId = itemId,
                quantity = quantity,
                reason = reason,
                notes = notes
            )
            repository.recordAuditLog(
                actionType = "STOCK_OUT",
                entityType = "INVENTORY",
                entityId = itemId,
                summary = "Stock out: -$quantity units ($reason)"
            )
        }
    }

    fun recordConsumption(
        itemId: String,
        quantity: Double,
        referenceType: String? = "NUTRITION",
        referenceId: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.recordConsumption(
                itemId = itemId,
                quantity = quantity,
                referenceType = referenceType,
                referenceId = referenceId,
                notes = notes
            )
            repository.recordAuditLog(
                actionType = "LOG_CONSUMPTION",
                entityType = "INVENTORY",
                entityId = itemId,
                summary = "Consumed $quantity units for ${referenceType ?: "flock feeding"}"
            )
        }
    }
}

class InventoryViewModelFactory(
    private val repository: AviaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
