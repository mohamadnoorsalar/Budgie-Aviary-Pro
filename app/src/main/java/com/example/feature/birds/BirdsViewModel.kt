package com.example.feature.birds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.data.database.entity.BirdEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BirdsUiState(
    val birds: List<BirdEntity> = emptyList(),
    val totalCount: Int = 0,
    val filterGender: BirdGender? = null,
    val filterStatus: BirdStatus? = null,
    val filterVariety: BudgieVariety? = null,
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val selectedBirdRing: String? = null
)

class BirdsViewModel(
    val repository: AviaryRepository
) : ViewModel() {

    private val _filterGender = MutableStateFlow<BirdGender?>(null)
    private val _filterStatus = MutableStateFlow<BirdStatus?>(null)
    private val _filterVariety = MutableStateFlow<BudgieVariety?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _selectedBirdRing = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BirdsUiState> = combine(
        repository.allBirds,
        _filterGender,
        _filterStatus,
        _filterVariety,
        combine(_searchQuery, _isAddDialogOpen, _selectedBirdRing) { query, isAddOpen, selectedRing ->
            Triple(query, isAddOpen, selectedRing)
        }
    ) { birds, filterGender, filterStatus, filterVariety, (query, isAddOpen, selectedRing) ->
        val filtered = birds.filter { bird ->
            (filterGender == null || bird.gender == filterGender) &&
            (filterStatus == null || bird.status == filterStatus) &&
            (filterVariety == null || bird.variety == filterVariety) &&
            (query.isBlank() ||
                bird.ringNumber.contains(query, ignoreCase = true) ||
                (bird.name?.contains(query, ignoreCase = true) == true) ||
                bird.mutation.contains(query, ignoreCase = true) ||
                bird.color.contains(query, ignoreCase = true) ||
                (bird.cageCode?.contains(query, ignoreCase = true) == true)
            )
        }
        BirdsUiState(
            birds = filtered,
            totalCount = birds.size,
            filterGender = filterGender,
            filterStatus = filterStatus,
            filterVariety = filterVariety,
            searchQuery = query,
            isAddDialogOpen = isAddOpen,
            selectedBirdRing = selectedRing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BirdsUiState()
    )

    fun setFilterGender(gender: BirdGender?) {
        _filterGender.value = gender
    }

    fun setFilterStatus(status: BirdStatus?) {
        _filterStatus.value = status
    }

    fun setFilterVariety(variety: BudgieVariety?) {
        _filterVariety.value = variety
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAddDialogOpen(open: Boolean) {
        _isAddDialogOpen.value = open
    }

    fun selectBird(ring: String?) {
        _selectedBirdRing.value = ring
    }

    fun saveBird(bird: BirdEntity) {
        viewModelScope.launch {
            repository.saveBird(bird)
            repository.recordAuditLog(
                actionType = "SAVE_BIRD",
                entityType = "BIRD",
                entityId = bird.ringNumber,
                summary = "Saved bird ${bird.ringNumber}"
            )
            _isAddDialogOpen.value = false
        }
    }

    fun deleteBird(ringNumber: String) {
        viewModelScope.launch {
            repository.deleteBird(ringNumber)
            repository.recordAuditLog(
                actionType = "DELETE_BIRD",
                entityType = "BIRD",
                entityId = ringNumber,
                summary = "Deleted bird $ringNumber"
            )
            if (_selectedBirdRing.value == ringNumber) {
                _selectedBirdRing.value = null
            }
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BirdsViewModel(repository) as T
        }
    }
}
