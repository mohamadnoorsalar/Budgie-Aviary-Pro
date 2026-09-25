package com.example.feature.reproduction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.relation.ChickWithBreedingDetails
import com.example.data.database.relation.PairWithBreedingDetails
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReproductionTab {
    PAIRS_AND_CLUTCHES,
    EGGS,
    CHICKS_NURSERY,
    NESTS,
    TIMELINES_AND_REMINDERS
}

data class ReproductionUiState(
    val pairs: List<PairWithBreedingDetails> = emptyList(),
    val eggs: List<EggEntity> = emptyList(),
    val chicks: List<ChickEntity> = emptyList(),
    val chicksWithDetails: List<ChickWithBreedingDetails> = emptyList(),
    val nests: List<NestEntity> = emptyList(),
    val clutches: List<ClutchEntity> = emptyList(),
    val cages: List<CageEntity> = emptyList(),
    val birds: List<BirdEntity> = emptyList(),
    val upcomingHatchEggs: List<EggEntity> = emptyList(),
    // Metrics
    val totalEggCount: Int = 0,
    val fertileEggCount: Int = 0,
    val hatchedChickCount: Int = 0,
    val activeChickCount: Int = 0,
    val mortalityCount: Int = 0,
    val fertilityRate: Double = 0.0,
    val hatchRate: Double = 0.0,
    // Active Navigation / Filters
    val selectedTab: ReproductionTab = ReproductionTab.PAIRS_AND_CLUTCHES,
    val selectedPairId: Long? = null,
    val eggFilterPairId: Long? = null,
    val eggFilterFertility: String? = null,
    val chickFilterStatus: String? = null,
    // Dialog states
    val isAddEggDialogOpen: Boolean = false,
    val candlingEgg: EggEntity? = null,
    val hatchingEgg: EggEntity? = null,
    val editingChickWeight: ChickEntity? = null,
    val transferringChick: ChickEntity? = null,
    val recordingMortalityChick: ChickEntity? = null,
    val registeringChickAsBird: ChickEntity? = null,
    val isAddNestDialogOpen: Boolean = false,
    val isAddClutchDialogOpen: Boolean = false,
    val userMessage: String? = null
)

class ReproductionViewModel(
    val repository: AviaryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ReproductionTab.PAIRS_AND_CLUTCHES)
    private val _selectedPairId = MutableStateFlow<Long?>(null)
    private val _eggFilterPairId = MutableStateFlow<Long?>(null)
    private val _eggFilterFertility = MutableStateFlow<String?>(null)
    private val _chickFilterStatus = MutableStateFlow<String?>(null)

    // Dialog state flows
    private val _isAddEggDialogOpen = MutableStateFlow(false)
    private val _candlingEgg = MutableStateFlow<EggEntity?>(null)
    private val _hatchingEgg = MutableStateFlow<EggEntity?>(null)
    private val _editingChickWeight = MutableStateFlow<ChickEntity?>(null)
    private val _transferringChick = MutableStateFlow<ChickEntity?>(null)
    private val _recordingMortalityChick = MutableStateFlow<ChickEntity?>(null)
    private val _registeringChickAsBird = MutableStateFlow<ChickEntity?>(null)
    private val _isAddNestDialogOpen = MutableStateFlow(false)
    private val _isAddClutchDialogOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReproductionUiState> = combine(
        repository.allPairsWithBreedingDetails,
        repository.allEggs,
        repository.allChicks,
        repository.allChicksWithDetails,
        repository.allNests,
        repository.allClutches,
        repository.allCages,
        repository.allBirds,
        repository.upcomingHatchEggs
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val pairs = args[0] as List<PairWithBreedingDetails>
        @Suppress("UNCHECKED_CAST")
        val eggs = args[1] as List<EggEntity>
        @Suppress("UNCHECKED_CAST")
        val chicks = args[2] as List<ChickEntity>
        @Suppress("UNCHECKED_CAST")
        val chicksWithDetails = args[3] as List<ChickWithBreedingDetails>
        @Suppress("UNCHECKED_CAST")
        val nests = args[4] as List<NestEntity>
        @Suppress("UNCHECKED_CAST")
        val clutches = args[5] as List<ClutchEntity>
        @Suppress("UNCHECKED_CAST")
        val cages = args[6] as List<CageEntity>
        @Suppress("UNCHECKED_CAST")
        val birds = args[7] as List<BirdEntity>
        @Suppress("UNCHECKED_CAST")
        val upcomingHatchEggs = args[8] as List<EggEntity>

        val nonDeletedEggs = eggs.filter { !it.isDeleted }
        val nonDeletedChicks = chicks.filter { !it.isDeleted }
        val totalEggCount = nonDeletedEggs.size
        val fertileEggCount = nonDeletedEggs.count { it.fertilityStatus == "FERTILE" }
        val hatchedChickCount = nonDeletedEggs.count { it.isHatched }
        val activeChickCount = nonDeletedChicks.count { it.status != "DECEASED" }
        val mortalityCount = nonDeletedChicks.count { it.status == "DECEASED" }

        val candledCount = nonDeletedEggs.count { it.fertilityStatus != "UNCANDLED" }
        val fertilityRate = if (candledCount > 0) (fertileEggCount.toDouble() / candledCount) * 100.0 else 0.0
        val hatchRate = if (fertileEggCount > 0) (hatchedChickCount.toDouble() / fertileEggCount) * 100.0 else 0.0

        ReproductionUiState(
            pairs = pairs,
            eggs = nonDeletedEggs,
            chicks = nonDeletedChicks,
            chicksWithDetails = chicksWithDetails,
            nests = nests.filter { !it.isDeleted },
            clutches = clutches,
            cages = cages,
            birds = birds.filter { !it.isDeleted },
            upcomingHatchEggs = upcomingHatchEggs,
            totalEggCount = totalEggCount,
            fertileEggCount = fertileEggCount,
            hatchedChickCount = hatchedChickCount,
            activeChickCount = activeChickCount,
            mortalityCount = mortalityCount,
            fertilityRate = fertilityRate,
            hatchRate = hatchRate
        )
    }.combine(_selectedTab) { state, tab ->
        state.copy(selectedTab = tab)
    }.combine(_selectedPairId) { state, pairId ->
        state.copy(selectedPairId = pairId)
    }.combine(_eggFilterPairId) { state, eggPair ->
        state.copy(eggFilterPairId = eggPair)
    }.combine(_eggFilterFertility) { state, fertility ->
        state.copy(eggFilterFertility = fertility)
    }.combine(_chickFilterStatus) { state, status ->
        state.copy(chickFilterStatus = status)
    }.combine(_isAddEggDialogOpen) { state, isOpen ->
        state.copy(isAddEggDialogOpen = isOpen)
    }.combine(_candlingEgg) { state, egg ->
        state.copy(candlingEgg = egg)
    }.combine(_hatchingEgg) { state, egg ->
        state.copy(hatchingEgg = egg)
    }.combine(_editingChickWeight) { state, chick ->
        state.copy(editingChickWeight = chick)
    }.combine(_transferringChick) { state, chick ->
        state.copy(transferringChick = chick)
    }.combine(_recordingMortalityChick) { state, chick ->
        state.copy(recordingMortalityChick = chick)
    }.combine(_registeringChickAsBird) { state, chick ->
        state.copy(registeringChickAsBird = chick)
    }.combine(_isAddNestDialogOpen) { state, isOpen ->
        state.copy(isAddNestDialogOpen = isOpen)
    }.combine(_isAddClutchDialogOpen) { state, isOpen ->
        state.copy(isAddClutchDialogOpen = isOpen)
    }.combine(_userMessage) { state, msg ->
        state.copy(userMessage = msg)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReproductionUiState()
    )

    fun selectTab(tab: ReproductionTab) {
        _selectedTab.value = tab
    }

    fun selectPair(pairId: Long?) {
        _selectedPairId.value = pairId
    }

    fun setEggFilterPairId(pairId: Long?) {
        _eggFilterPairId.value = pairId
    }

    fun setEggFilterFertility(fertility: String?) {
        _eggFilterFertility.value = fertility
    }

    fun setChickFilterStatus(status: String?) {
        _chickFilterStatus.value = status
    }

    // --- Egg Management ---
    fun openAddEggDialog(preselectedPairId: Long? = null) {
        if (preselectedPairId != null) {
            _selectedPairId.value = preselectedPairId
        }
        _isAddEggDialogOpen.value = true
    }

    fun closeAddEggDialog() {
        _isAddEggDialogOpen.value = false
    }

    fun addEgg(
        pairId: Long,
        clutchId: Long?,
        nestId: String?,
        eggNumber: Int,
        layDate: Long,
        expectedHatchDate: Long?,
        fertilityStatus: String = "UNCANDLED",
        notes: String?
    ) {
        viewModelScope.launch {
            val calculatedHatchDate = expectedHatchDate ?: (layDate + 18L * 86400000L)
            val egg = EggEntity(
                pairId = pairId,
                clutchId = clutchId,
                nestId = nestId,
                eggNumber = eggNumber,
                layDate = layDate,
                expectedHatchDate = calculatedHatchDate,
                fertilityStatus = fertilityStatus,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveEgg(egg)
            _isAddEggDialogOpen.value = false
            _userMessage.value = "Egg #$eggNumber logged successfully"
        }
    }

    fun openCandleDialog(egg: EggEntity) {
        _candlingEgg.value = egg
    }

    fun closeCandleDialog() {
        _candlingEgg.value = null
    }

    fun updateEggFertility(eggId: String, status: String, notes: String?) {
        viewModelScope.launch {
            repository.updateEggFertility(eggId, status, notes)
            _candlingEgg.value = null
            _userMessage.value = "Egg fertility updated to $status"
        }
    }

    fun openHatchDialog(egg: EggEntity) {
        _hatchingEgg.value = egg
    }

    fun closeHatchDialog() {
        _hatchingEgg.value = null
    }

    fun hatchEgg(
        eggId: String,
        hatchDate: Long,
        initialWeightGrams: Double?,
        ringNumber: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val chick = repository.hatchEgg(
                    eggId = eggId,
                    hatchDate = hatchDate,
                    initialWeightGrams = initialWeightGrams,
                    ringNumber = ringNumber,
                    notes = notes
                )
                _hatchingEgg.value = null
                _userMessage.value = "Chick #${chick.hatchOrder} recorded successfully!"
            } catch (e: Exception) {
                _userMessage.value = "Error hatching egg: ${e.localizedMessage}"
            }
        }
    }

    fun deleteEgg(eggId: String) {
        viewModelScope.launch {
            repository.deleteEgg(eggId)
            _userMessage.value = "Egg deleted"
        }
    }

    // --- Chick Management ---
    fun openChickWeightDialog(chick: ChickEntity) {
        _editingChickWeight.value = chick
    }

    fun closeChickWeightDialog() {
        _editingChickWeight.value = null
    }

    fun recordChickWeight(chickId: String, weightGrams: Double, conditionScore: String, notes: String?) {
        viewModelScope.launch {
            repository.recordChickWeight(chickId, weightGrams, conditionScore, notes)
            _editingChickWeight.value = null
            _userMessage.value = "Weight logged: ${weightGrams}g"
        }
    }

    fun openChickTransferDialog(chick: ChickEntity) {
        _transferringChick.value = chick
    }

    fun closeChickTransferDialog() {
        _transferringChick.value = null
    }

    fun transferChickToCage(chickId: String, newCageCode: String, transferDate: Long, notes: String?) {
        viewModelScope.launch {
            repository.transferChickToCage(chickId, newCageCode, transferDate, notes)
            _transferringChick.value = null
            _userMessage.value = "Chick transferred to cage $newCageCode"
        }
    }

    fun openChickMortalityDialog(chick: ChickEntity) {
        _recordingMortalityChick.value = chick
    }

    fun closeChickMortalityDialog() {
        _recordingMortalityChick.value = null
    }

    fun recordChickMortality(chickId: String, mortalityDate: Long, reason: String, notes: String?) {
        viewModelScope.launch {
            repository.recordChickMortality(chickId, mortalityDate, reason, notes)
            _recordingMortalityChick.value = null
            _userMessage.value = "Mortality recorded"
        }
    }

    fun openRegisterBirdDialog(chick: ChickEntity) {
        _registeringChickAsBird.value = chick
    }

    fun closeRegisterBirdDialog() {
        _registeringChickAsBird.value = null
    }

    fun registerChickAsBird(
        chickId: String,
        ringNumber: String,
        name: String?,
        gender: BirdGender,
        variety: BudgieVariety?,
        color: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val bird = repository.registerChickAsOffspring(
                    chickId = chickId,
                    ringNumber = ringNumber,
                    name = name,
                    gender = gender,
                    variety = variety,
                    color = color,
                    notes = notes
                )
                _registeringChickAsBird.value = null
                _userMessage.value = "Registered bird ${bird.ringNumber} (Gen: ${bird.generation})"
            } catch (e: Exception) {
                _userMessage.value = "Registration error: ${e.localizedMessage}"
            }
        }
    }

    fun deleteChick(chickId: String) {
        viewModelScope.launch {
            repository.deleteChick(chickId)
            _userMessage.value = "Chick record removed"
        }
    }

    // --- Nests Management ---
    fun openAddNestDialog() {
        _isAddNestDialogOpen.value = true
    }

    fun closeAddNestDialog() {
        _isAddNestDialogOpen.value = false
    }

    fun saveNest(
        boxNumber: String,
        cageCode: String?,
        pairId: Long?,
        nestMaterial: String,
        isClean: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            val nest = NestEntity(
                boxNumber = boxNumber.trim(),
                cageCode = cageCode?.ifBlank { null },
                pairId = pairId,
                nestMaterial = nestMaterial,
                isClean = isClean,
                notes = notes
            )
            repository.saveNest(nest)
            _isAddNestDialogOpen.value = false
            _userMessage.value = "Nest Box $boxNumber saved"
        }
    }

    fun deleteNest(nestId: String) {
        viewModelScope.launch {
            repository.deleteNest(nestId)
            _userMessage.value = "Nest Box deleted"
        }
    }

    // --- Clutches Management ---
    fun openAddClutchDialog(pairId: Long? = null) {
        if (pairId != null) {
            _selectedPairId.value = pairId
        }
        _isAddClutchDialogOpen.value = true
    }

    fun closeAddClutchDialog() {
        _isAddClutchDialogOpen.value = false
    }

    fun saveClutch(
        pairId: Long,
        clutchNumber: Int,
        matingDate: Long?,
        startDate: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val clutch = ClutchEntity(
                pairId = pairId,
                clutchNumber = clutchNumber,
                matingDate = matingDate,
                startDate = startDate,
                notes = notes,
                isActive = true
            )
            repository.saveClutch(clutch)
            _isAddClutchDialogOpen.value = false
            _userMessage.value = "Clutch #$clutchNumber created for Pair #$pairId"
        }
    }

    // --- Reminders & Notifications ---
    fun syncUpcomingReminders() {
        viewModelScope.launch {
            val created = repository.generateBreedingReminders()
            _userMessage.value = if (created > 0) {
                "Synced $created breeding reminders into system!"
            } else {
                "Breeding reminders are up to date."
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReproductionViewModel(repository) as T
        }
    }
}
