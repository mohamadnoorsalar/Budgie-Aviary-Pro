package com.example.feature.genetics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdGender
import com.example.core.genetics.BaseColorSeries
import com.example.core.genetics.BirdGenotype
import com.example.core.genetics.GenerationBreedingGoal
import com.example.core.genetics.GenerationPlanningResult
import com.example.core.genetics.GeneticsCalculator
import com.example.core.genetics.GeneticsCalendarEvent
import com.example.core.genetics.OffspringPredictionResult
import com.example.core.genetics.PredictionVsActualReport
import com.example.core.genetics.RelatednessAnalysisReport
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.relation.PairWithBreedingDetails
import com.example.data.repository.AviaryRepository
import com.example.data.repository.AviaryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GeneticSearchFilters(
    val query: String = "",
    val visualMutation: String = "ALL",
    val splitCarrier: String = "ALL",
    val baseSeries: String = "ALL", // ALL, GREEN, BLUE
    val darkFactor: Int = -1 // -1 = ALL, 0, 1, 2
)

class GeneticsViewModel(
    application: Application,
    private val repository: AviaryRepository = AviaryRepositoryImpl(AppDatabase.getInstance(application))
) : AndroidViewModel(application) {

    val allBirds: StateFlow<List<BirdEntity>> = repository.allBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenetics: StateFlow<List<BirdGeneticsEntity>> = repository.allGenetics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPairs: StateFlow<List<PairEntity>> = repository.allPairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPedigrees: StateFlow<List<PedigreeRecordEntity>> = repository.allPedigrees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pairsWithDetails: StateFlow<List<PairWithBreedingDetails>> = repository.allPairsWithBreedingDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulator selections
    private val _selectedSireRing = MutableStateFlow("")
    val selectedSireRing = _selectedSireRing.asStateFlow()

    private val _selectedDamRing = MutableStateFlow("")
    val selectedDamRing = _selectedDamRing.asStateFlow()

    private val _selectedPairId = MutableStateFlow<Long?>(null)
    val selectedPairId = _selectedPairId.asStateFlow()

    // Calculated Pair Predictions
    private val _predictionResult = MutableStateFlow<OffspringPredictionResult?>(null)
    val predictionResult = _predictionResult.asStateFlow()

    private val _relatednessReport = MutableStateFlow<RelatednessAnalysisReport?>(null)
    val relatednessReport = _relatednessReport.asStateFlow()

    private val _predictionVsActual = MutableStateFlow<PredictionVsActualReport?>(null)
    val predictionVsActual = _predictionVsActual.asStateFlow()

    // Search filters
    private val _searchFilters = MutableStateFlow(GeneticSearchFilters())
    val searchFilters = _searchFilters.asStateFlow()

    // Breeder Goal Planner
    val availableGoals = GeneticsCalculator.STANDARD_BREEDING_GOALS
    private val _selectedGoal = MutableStateFlow(availableGoals.first())
    val selectedGoal = _selectedGoal.asStateFlow()

    private val _generationPlanningResult = MutableStateFlow<GenerationPlanningResult?>(null)
    val generationPlanningResult = _generationPlanningResult.asStateFlow()

    // Genetics Calendar Milestones
    private val _calendarMilestones = MutableStateFlow<List<GeneticsCalendarEvent>>(emptyList())
    val calendarMilestones = _calendarMilestones.asStateFlow()

    init {
        // Automatically re-run calculations whenever birds, genetics, or selections update
        viewModelScope.launch {
            combine(
                allBirds,
                allGenetics,
                allPedigrees,
                _selectedSireRing,
                _selectedDamRing
            ) { birds, genetics, pedigrees, sireRing, damRing ->
                recalculatePairSimulations(birds, genetics, pedigrees, sireRing, damRing)
            }.collect {}
        }

        // Keep Generation Planning synced
        viewModelScope.launch {
            combine(allBirds, allGenetics, _selectedGoal) { birds, genetics, goal ->
                _generationPlanningResult.value = GeneticsCalculator.planGeneration(goal, birds, genetics)
            }.collect {}
        }

        // Keep Calendar Milestones synced
        viewModelScope.launch {
            pairsWithDetails.collect { pairs ->
                _calendarMilestones.value = GeneticsCalculator.generateGeneticsCalendarMilestones(pairs)
            }
        }
    }

    private fun recalculatePairSimulations(
        birds: List<BirdEntity>,
        geneticsList: List<BirdGeneticsEntity>,
        pedigrees: List<PedigreeRecordEntity>,
        sireRing: String,
        damRing: String
    ) {
        if (sireRing.isBlank() || damRing.isBlank()) {
            _predictionResult.value = null
            _relatednessReport.value = null
            _predictionVsActual.value = null
            return
        }

        val sireBird = birds.firstOrNull { it.ringNumber == sireRing }
        val damBird = birds.firstOrNull { it.ringNumber == damRing }

        if (sireBird == null || damBird == null) {
            _predictionResult.value = null
            _relatednessReport.value = null
            _predictionVsActual.value = null
            return
        }

        val geneticsMap = geneticsList.associateBy { it.birdRingNumber }
        val sireGenotype = GeneticsCalculator.parseGenotype(sireBird, geneticsMap[sireRing])
        val damGenotype = GeneticsCalculator.parseGenotype(damBird, geneticsMap[damRing])

        // 1. Calculate Offspring Probabilities
        val prediction = GeneticsCalculator.predictOffspring(sireGenotype, damGenotype)
        _predictionResult.value = prediction

        // 2. Calculate Kinship and Close-relative Warnings
        val relatedness = GeneticsCalculator.calculateRelatedness(sireRing, damRing, birds, pedigrees)
        _relatednessReport.value = relatedness

        // 3. Prediction vs Actual from recorded offspring
        val recordedChildren = birds.filter {
            (it.fatherRing == sireRing && it.motherRing == damRing) ||
                    (it.fatherRing == sireRing && damRing.isBlank()) ||
                    (it.motherRing == damRing && sireRing.isBlank())
        }
        val comp = GeneticsCalculator.comparePredictionVsActual(prediction, recordedChildren)
        _predictionVsActual.value = comp
    }

    fun selectSire(ring: String) {
        _selectedSireRing.value = ring
        _selectedPairId.value = null
    }

    fun selectDam(ring: String) {
        _selectedDamRing.value = ring
        _selectedPairId.value = null
    }

    fun selectBreedingPair(pair: PairEntity) {
        _selectedPairId.value = pair.id
        _selectedSireRing.value = pair.maleRingNumber
        _selectedDamRing.value = pair.femaleRingNumber
    }

    fun selectGoal(goal: GenerationBreedingGoal) {
        _selectedGoal.value = goal
        _generationPlanningResult.value = GeneticsCalculator.planGeneration(goal, allBirds.value, allGenetics.value)
    }

    fun updateSearchFilters(filters: GeneticSearchFilters) {
        _searchFilters.value = filters
    }

    fun saveBirdGenetics(genetics: BirdGeneticsEntity) {
        viewModelScope.launch {
            repository.saveGenetics(genetics)
            repository.recordAuditLog(
                actionType = "UPDATE_GENETICS",
                entityType = "GENETICS",
                entityId = genetics.birdRingNumber,
                summary = "Updated genetic traits for ${genetics.birdRingNumber}"
            )
        }
    }
}
