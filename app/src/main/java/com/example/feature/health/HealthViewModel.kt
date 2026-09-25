package com.example.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    val allBirds: StateFlow<List<BirdEntity>> = repository.allBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHealthRecords: StateFlow<List<HealthRecordEntity>> = repository.allHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeHealthIssues: StateFlow<List<HealthRecordEntity>> = repository.activeHealthIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMedications: StateFlow<List<MedicationEntity>> = repository.activeMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedications: StateFlow<List<MedicationEntity>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeights: StateFlow<List<WeightRecordEntity>> = repository.allWeightRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, ACTIVE, RESOLVED, WEIGHTS
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun saveHealthRecord(record: HealthRecordEntity, scheduleReminders: Boolean) {
        viewModelScope.launch {
            repository.saveHealthRecord(record)
            if (scheduleReminders && !record.medicationName.isNullOrBlank()) {
                val med = MedicationEntity(
                    healthRecordId = record.id,
                    birdRingNumber = record.birdRingNumber,
                    medicationName = record.medicationName,
                    dosage = record.dosage ?: "Standard dose",
                    frequency = record.frequency ?: "ONCE_DAILY",
                    startDate = record.startDate,
                    endDate = record.endDate ?: (record.startDate + (record.treatmentDurationDays.coerceAtLeast(1) * 86400000L)),
                    notes = "Prescribed for ${record.recordedProblem}"
                )
                repository.scheduleMedicationWithReminders(med)
            }
            repository.recordAuditLog(
                actionType = "ADD_HEALTH_RECORD",
                entityType = "HEALTH",
                entityId = record.id,
                summary = "Logged treatment: ${record.recordedProblem} for bird ${record.birdRingNumber ?: "Aviary"}"
            )
        }
    }

    fun markHealthRecordResolved(record: HealthRecordEntity) {
        viewModelScope.launch {
            repository.updateHealthRecord(
                record.copy(
                    isResolved = true,
                    resolutionDate = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveWeight(record: WeightRecordEntity) {
        viewModelScope.launch {
            repository.saveWeight(record)
            repository.recordAuditLog(
                actionType = "LOG_WEIGHT",
                entityType = "WEIGHT",
                entityId = record.birdRingNumber,
                summary = "Weighed bird ${record.birdRingNumber}: ${record.weightGrams}g"
            )
        }
    }

    fun markMedicationCompleted(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.saveMedication(
                medication.copy(
                    isCompleted = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun refreshMedicationReminders() {
        viewModelScope.launch {
            repository.generateMedicationReminders()
        }
    }
}

class HealthViewModelFactory(
    private val repository: AviaryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HealthViewModel(repository) as T
    }
}
