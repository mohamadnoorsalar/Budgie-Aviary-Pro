package com.example.feature.cages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.CageType
import com.example.core.common.EggFertilityStatus
import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.relation.CageWithDetails
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

data class CagesUiState(
    val cages: List<CageWithDetails> = emptyList(),
    val totalCount: Int = 0,
    val selectedCageCode: String? = null,
    val selectedCageDetails: CageWithDetails? = null,
    val selectedCageLogs: List<AuditLogEntity> = emptyList(),
    val selectedCageEggs: List<EggEntity> = emptyList(),
    val selectedCageChicks: List<ChickEntity> = emptyList(),
    val availableBirdsForAssignment: List<BirdEntity> = emptyList(),
    val searchQuery: String = "",
    val filterType: CageType? = null,
    val filterClean: Boolean? = null,
    val isAddDialogOpen: Boolean = false,
    val isScannerDialogOpen: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class CagesViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedCageCode = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<CageType?>(null)
    private val _filterClean = MutableStateFlow<Boolean?>(null)
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _isScannerDialogOpen = MutableStateFlow(false)

    // Flow for selected cage details
    private val selectedCageFlow = _selectedCageCode.flatMapLatest { code ->
        if (code != null) repository.getCageWithDetails(code) else flowOf(null)
    }

    // Flow for movement & audit history of selected cage
    private val selectedCageLogsFlow = _selectedCageCode.flatMapLatest { code ->
        if (code != null) repository.getLogsForEntity("CAGE", code) else flowOf(emptyList())
    }

    // Flow for eggs of active pair in selected cage
    private val selectedCageEggsFlow = selectedCageFlow.flatMapLatest { details ->
        val activePairId = details?.activePair?.id
        if (activePairId != null) repository.getEggsForPair(activePairId) else flowOf(emptyList())
    }

    // Flow for chicks of active pair in selected cage
    private val selectedCageChicksFlow = selectedCageFlow.flatMapLatest { details ->
        val activePairId = details?.activePair?.id
        if (activePairId != null) repository.getChicksForPair(activePairId) else flowOf(emptyList())
    }

    val uiState: StateFlow<CagesUiState> = combine(
        repository.allCagesWithDetails,
        _searchQuery,
        _filterType,
        _filterClean,
        selectedCageFlow,
        selectedCageLogsFlow,
        selectedCageEggsFlow,
        selectedCageChicksFlow,
        repository.allBirds
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allCages = args[0] as List<CageWithDetails>
        val query = args[1] as String
        val fType = args[2] as CageType?
        val fClean = args[3] as Boolean?
        val selDetails = args[4] as CageWithDetails?
        @Suppress("UNCHECKED_CAST")
        val logs = args[5] as List<AuditLogEntity>
        @Suppress("UNCHECKED_CAST")
        val eggs = args[6] as List<EggEntity>
        @Suppress("UNCHECKED_CAST")
        val chicks = args[7] as List<ChickEntity>
        @Suppress("UNCHECKED_CAST")
        val allBirds = args[8] as List<BirdEntity>

        val filtered = allCages.filter { details ->
            val cage = details.cage
            val matchesType = fType == null || cage.type == fType
            val matchesClean = fClean == null || cage.isClean == fClean
            val matchesQuery = query.isBlank() ||
                cage.code.contains(query, ignoreCase = true) ||
                (cage.location?.contains(query, ignoreCase = true) == true) ||
                details.birds.any { it.ringNumber.contains(query, ignoreCase = true) }

            matchesType && matchesClean && matchesQuery
        }

        // Birds not yet in this cage or in aviary
        val availableBirds = allBirds.filter { it.cageCode != _selectedCageCode.value }

        CagesUiState(
            cages = filtered,
            totalCount = allCages.size,
            selectedCageCode = _selectedCageCode.value,
            selectedCageDetails = selDetails,
            selectedCageLogs = logs,
            selectedCageEggs = eggs,
            selectedCageChicks = chicks,
            availableBirdsForAssignment = availableBirds,
            searchQuery = query,
            filterType = fType,
            filterClean = fClean,
            isAddDialogOpen = _isAddDialogOpen.value,
            isScannerDialogOpen = _isScannerDialogOpen.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CagesUiState()
    )

    fun selectCage(code: String?) {
        _selectedCageCode.value = code
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: CageType?) {
        _filterType.value = type
    }

    fun setFilterClean(clean: Boolean?) {
        _filterClean.value = clean
    }

    fun setAddDialogOpen(open: Boolean) {
        _isAddDialogOpen.value = open
    }

    fun setScannerDialogOpen(open: Boolean) {
        _isScannerDialogOpen.value = open
    }

    fun saveCage(cage: CageEntity) {
        viewModelScope.launch {
            repository.saveCage(cage)
            repository.recordAuditLog(
                actionType = "CAGE_SAVE",
                entityType = "CAGE",
                entityId = cage.code,
                summary = "Registered or updated cage ${cage.code} (${cage.type})"
            )
            _isAddDialogOpen.value = false
        }
    }

    fun setCageCleanStatus(cageCode: String, isClean: Boolean) {
        viewModelScope.launch {
            repository.setCageCleanStatus(cageCode, isClean)
        }
    }

    fun moveBirdToCage(birdRing: String, newCageCode: String?, reason: String? = null) {
        viewModelScope.launch {
            repository.moveBirdToCage(birdRing, newCageCode, reason)
        }
    }

    fun removeBirdFromCage(birdRing: String, reason: String? = null) {
        viewModelScope.launch {
            repository.moveBirdToCage(birdRing, null, reason ?: "Removed from cage")
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
                summary = "Egg #$eggNumber recorded with fertility ${fertility.name}"
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
                summary = "Chick #$hatchOrder hatched from pair #$pairId"
            )
        }
    }

    fun deleteCage(cage: CageEntity) {
        viewModelScope.launch {
            repository.deleteCage(cage)
            if (_selectedCageCode.value == cage.code) {
                _selectedCageCode.value = null
            }
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CagesViewModel(repository) as T
        }
    }
}
