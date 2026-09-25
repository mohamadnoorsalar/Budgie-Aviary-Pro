package com.example.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NutritionViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    val allNutritionPlans: StateFlow<List<NutritionPlanEntity>> = repository.allNutritionPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNutritionRecords: StateFlow<List<NutritionRecordEntity>> = repository.allNutritionRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBirds: StateFlow<List<BirdEntity>> = repository.allBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCages: StateFlow<List<CageEntity>> = repository.allCages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0) // 0 = Daily Logs, 1 = Seasonal Plans & Diet Recipes
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun saveNutritionRecord(
        record: NutritionRecordEntity,
        reduceInventoryItemId: String? = null,
        reduceQuantity: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.saveNutritionRecord(record)
            if (!reduceInventoryItemId.isNullOrBlank() && reduceQuantity > 0.0) {
                repository.recordConsumption(
                    itemId = reduceInventoryItemId,
                    quantity = reduceQuantity,
                    referenceType = "NUTRITION",
                    referenceId = record.id,
                    notes = "Feeding: ${record.foodType} (${record.birdRingNumber ?: record.cageCode ?: "Flock"})"
                )
            }
            repository.recordAuditLog(
                actionType = "ADD_NUTRITION_RECORD",
                entityType = "NUTRITION",
                entityId = record.id,
                summary = "Logged feeding: ${record.foodType} (${record.amount}) for ${record.birdRingNumber ?: record.cageCode ?: "Flock"}"
            )
        }
    }

    fun deleteNutritionRecord(record: NutritionRecordEntity) {
        viewModelScope.launch {
            repository.deleteNutritionRecord(record)
        }
    }

    fun saveNutritionPlan(plan: NutritionPlanEntity) {
        viewModelScope.launch {
            repository.saveNutritionPlan(plan)
        }
    }
}

class NutritionViewModelFactory(
    private val repository: AviaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NutritionViewModel(repository) as T
    }
}
