package com.example.feature.pairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdGender
import com.example.core.common.EggFertilityStatus
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.relation.PairWithBreedingDetails
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PairsUiState(
    val pairs: List<PairWithBreedingDetails> = emptyList(),
    val totalCount: Int = 0,
    val selectedPairId: Long? = null,
    val selectedPairDetails: PairWithBreedingDetails? = null,
    val availableMales: List<BirdEntity> = emptyList(),
    val availableFemales: List<BirdEntity> = emptyList(),
    val availableCages: List<CageEntity> = emptyList(),
    val searchQuery: String = "",
    val filterStatus: String? = null,
    val isAddDialogOpen: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PairsViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedPairId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow<String?>(null)
    private val _isAddDialogOpen = MutableStateFlow(false)

    private val selectedPairFlow = _selectedPairId.flatMapLatest { id ->
        if (id != null) repository.getPairWithBreedingDetails(id) else flowOf(null)
    }

    val uiState: StateFlow<PairsUiState> = combine(
        repository.allPairsWithBreedingDetails,
        _searchQuery,
        _filterStatus,
        selectedPairFlow,
        repository.allBirds,
        repository.allCages
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allPairs = args[0] as List<PairWithBreedingDetails>
        val query = args[1] as String
        val fStatus = args[2] as String?
        val selPair = args[3] as PairWithBreedingDetails?
        @Suppress("UNCHECKED_CAST")
        val allBirds = args[4] as List<BirdEntity>
        @Suppress("UNCHECKED_CAST")
        val allCages = args[5] as List<com.example.data.database.entity.CageEntity>

        val filtered = allPairs.filter { details ->
            val p = details.pair
            val matchesStatus = fStatus == null || p.status.equals(fStatus, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                p.maleRingNumber.contains(query, ignoreCase = true) ||
                p.femaleRingNumber.contains(query, ignoreCase = true) ||
                (p.cageCode?.contains(query, ignoreCase = true) == true) ||
                (p.notes?.contains(query, ignoreCase = true) == true) ||
                (p.results?.contains(query, ignoreCase = true) == true)

            matchesStatus && matchesQuery
        }

        val males = allBirds.filter { it.gender == BirdGender.MALE }
        val females = allBirds.filter { it.gender == BirdGender.FEMALE }

        PairsUiState(
            pairs = filtered,
            totalCount = allPairs.size,
            selectedPairId = _selectedPairId.value,
            selectedPairDetails = selPair,
            availableMales = males,
            availableFemales = females,
            availableCages = allCages,
            searchQuery = query,
            filterStatus = fStatus,
            isAddDialogOpen = _isAddDialogOpen.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PairsUiState()
    )

    fun selectPair(pairId: Long?) {
        _selectedPairId.value = pairId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String?) {
        _filterStatus.value = status
    }

    fun setAddDialogOpen(open: Boolean) {
        _isAddDialogOpen.value = open
    }

    fun addPair(
        maleRing: String,
        femaleRing: String,
        cageCode: String?,
        nestId: String? = null,
        notes: String? = null,
        matingHistory: String? = null
    ) {
        viewModelScope.launch {
            val pairId = repository.savePair(
                PairEntity(
                    maleRingNumber = maleRing.trim().uppercase(),
                    femaleRingNumber = femaleRing.trim().uppercase(),
                    cageCode = cageCode?.trim()?.uppercase()?.takeIf { it.isNotBlank() },
                    nestId = nestId,
                    pairingDate = System.currentTimeMillis(),
                    status = "ACTIVE",
                    isActive = true,
                    notes = notes?.takeIf { it.isNotBlank() },
                    matingHistory = matingHistory?.takeIf { it.isNotBlank() }
                )
            )

            // Auto-create initial clutch #1 for new pair
            repository.saveClutch(
                ClutchEntity(
                    pairId = pairId,
                    clutchNumber = 1,
                    startDate = System.currentTimeMillis(),
                    isActive = true
                )
            )

            // Audit log
            repository.recordAuditLog(
                actionType = "PAIR_CREATED",
                entityType = "PAIR",
                entityId = pairId.toString(),
                summary = "Formed pair: Cock $maleRing x Hen $femaleRing in cage ${cageCode ?: "N/A"}"
            )

            _isAddDialogOpen.value = false
        }
    }

    fun updatePairStatus(pairId: Long, newStatus: String, endCurrent: Boolean = false) {
        viewModelScope.launch {
            val current = uiState.value.selectedPairDetails?.pair
            if (current != null && current.id == pairId) {
                val updated = current.copy(
                    status = newStatus,
                    isActive = !endCurrent && newStatus == "ACTIVE",
                    endDate = if (endCurrent) System.currentTimeMillis() else current.endDate
                )
                repository.updatePair(updated)
                repository.recordAuditLog(
                    actionType = "PAIR_STATUS_UPDATE",
                    entityType = "PAIR",
                    entityId = pairId.toString(),
                    summary = "Updated pair #$pairId status to $newStatus"
                )
            }
        }
    }

    fun addClutch(pairId: Long, clutchNumber: Int) {
        viewModelScope.launch {
            repository.saveClutch(
                ClutchEntity(
                    pairId = pairId,
                    clutchNumber = clutchNumber,
                    startDate = System.currentTimeMillis(),
                    isActive = true
                )
            )
            val currentPair = uiState.value.selectedPairDetails?.pair
            if (currentPair != null) {
                repository.updatePair(currentPair.copy(clutchCount = clutchNumber))
            }
            repository.recordAuditLog(
                actionType = "CLUTCH_STARTED",
                entityType = "PAIR",
                entityId = pairId.toString(),
                summary = "Started breeding clutch #$clutchNumber for pair #$pairId"
            )
        }
    }

    fun addEgg(pairId: Long, eggNumber: Int, fertility: EggFertilityStatus) {
        viewModelScope.launch {
            repository.saveEgg(
                EggEntity(
                    pairId = pairId,
                    eggNumber = eggNumber,
                    layDate = System.currentTimeMillis(),
                    fertilityStatus = fertility.name
                )
            )
            repository.recordAuditLog(
                actionType = "EGG_LAID",
                entityType = "PAIR",
                entityId = pairId.toString(),
                summary = "Logged Egg #$eggNumber for pair #$pairId (${fertility.name})"
            )
        }
    }

    fun updateEggFertility(egg: EggEntity, fertility: EggFertilityStatus, isHatched: Boolean) {
        viewModelScope.launch {
            repository.saveEgg(
                egg.copy(
                    fertilityStatus = fertility.name,
                    isHatched = isHatched
                )
            )
        }
    }

    fun addChick(pairId: Long, ringNumber: String?, hatchOrder: Int) {
        viewModelScope.launch {
            repository.saveChick(
                ChickEntity(
                    eggId = java.util.UUID.randomUUID().toString(),
                    pairId = pairId,
                    bandedRingNumber = ringNumber?.trim()?.uppercase()?.takeIf { it.isNotBlank() },
                    hatchOrder = hatchOrder,
                    hatchDate = System.currentTimeMillis()
                )
            )
            repository.recordAuditLog(
                actionType = "CHICK_HATCHED",
                entityType = "PAIR",
                entityId = pairId.toString(),
                summary = "Logged chick #$hatchOrder for pair #$pairId"
            )
        }
    }

    fun saveMatingHistory(pairId: Long, history: String) {
        viewModelScope.launch {
            val current = uiState.value.selectedPairDetails?.pair
            if (current != null) {
                repository.updatePair(current.copy(matingHistory = history))
            }
        }
    }

    fun saveResults(pairId: Long, results: String) {
        viewModelScope.launch {
            val current = uiState.value.selectedPairDetails?.pair
            if (current != null) {
                repository.updatePair(current.copy(results = results))
            }
        }
    }

    fun saveNotes(pairId: Long, notes: String) {
        viewModelScope.launch {
            val current = uiState.value.selectedPairDetails?.pair
            if (current != null) {
                repository.updatePair(current.copy(notes = notes))
            }
        }
    }

    fun deletePair(pair: PairEntity) {
        viewModelScope.launch {
            repository.deletePair(pair)
            if (_selectedPairId.value == pair.id) {
                _selectedPairId.value = null
            }
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PairsViewModel(repository) as T
        }
    }
}
