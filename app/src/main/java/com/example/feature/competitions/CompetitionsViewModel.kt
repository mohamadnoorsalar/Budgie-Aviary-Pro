package com.example.feature.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.competition.CompetitionJudgingHelper
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.JudgeEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompetitionsUiState(
    val competitions: List<CompetitionEntity> = emptyList(),
    val judges: List<JudgeEntity> = emptyList(),
    val birds: List<BirdEntity> = emptyList(),
    val selectedCompetitionId: String? = null,
    val selectedCompetition: CompetitionEntity? = null,
    val currentCriteria: List<CompetitionCriterionEntity> = emptyList(),
    val currentScores: List<CompetitionScoreEntity> = emptyList(),
    val selectedClassFilter: String? = null,
    val searchQuery: String = ""
)

class CompetitionsViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedCompetitionId = MutableStateFlow<String?>(null)
    val selectedCompetitionId: StateFlow<String?> = _selectedCompetitionId.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow<String?>(null)
    val selectedClassFilter: StateFlow<String?> = _selectedClassFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun selectCompetition(competitionId: String?) {
        _selectedCompetitionId.value = competitionId
        _selectedClassFilter.value = null
    }

    fun setClassFilter(showClass: String?) {
        _selectedClassFilter.value = showClass
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Current competition details
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val currentCompetitionFlow = _selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getCompetitionById(id)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val currentCriteriaFlow = _selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCriteriaForCompetition(id)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val currentScoresFlow = _selectedCompetitionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getScoresForCompetition(id)
    }

    private val baseCompetitionData = combine(
        repository.allCompetitions,
        repository.allJudges,
        repository.allBirds,
        _selectedCompetitionId,
        currentCompetitionFlow
    ) { comps, judges, birds, selectedId, selectedComp ->
        BaseCompData(comps, judges, birds, selectedId, selectedComp)
    }

    private val activeScoreDetails = combine(
        currentCriteriaFlow,
        currentScoresFlow,
        _selectedClassFilter,
        _searchQuery
    ) { criteria, scores, classFilter, query ->
        ActiveScoreData(criteria, scores, classFilter, query)
    }

    val uiState: StateFlow<CompetitionsUiState> = combine(
        baseCompetitionData,
        activeScoreDetails
    ) { base, details ->
        val filteredScores = details.scores.filter { score ->
            (details.classFilter == null || score.showClass.equals(details.classFilter, ignoreCase = true)) &&
            (details.query.isBlank() || score.birdRingNumber.contains(details.query, ignoreCase = true) ||
                    (score.participantName?.contains(details.query, ignoreCase = true) == true) ||
                    (score.judgingCode?.contains(details.query, ignoreCase = true) == true) ||
                    (score.awardTitle?.contains(details.query, ignoreCase = true) == true))
        }

        CompetitionsUiState(
            competitions = base.competitions,
            judges = base.judges,
            birds = base.birds,
            selectedCompetitionId = base.selectedId,
            selectedCompetition = base.selectedComp,
            currentCriteria = details.criteria,
            currentScores = filteredScores,
            selectedClassFilter = details.classFilter,
            searchQuery = details.query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompetitionsUiState()
    )

    private data class BaseCompData(
        val competitions: List<CompetitionEntity>,
        val judges: List<JudgeEntity>,
        val birds: List<BirdEntity>,
        val selectedId: String?,
        val selectedComp: CompetitionEntity?
    )

    private data class ActiveScoreData(
        val criteria: List<CompetitionCriterionEntity>,
        val scores: List<CompetitionScoreEntity>,
        val classFilter: String?,
        val query: String
    )

    fun createCompetition(
        title: String,
        location: String,
        organizingClub: String,
        eventDate: Long,
        showStandard: String,
        categories: List<String>,
        judgingCodes: List<String>,
        notes: String?,
        customCriteria: List<CompetitionCriterionEntity>? = null
    ) {
        viewModelScope.launch {
            val competition = CompetitionEntity(
                title = title.trim(),
                location = location.trim(),
                organizingClub = organizingClub.trim(),
                eventDate = eventDate,
                showStandard = showStandard,
                categoriesJson = CompetitionJudgingHelper.serializeJsonList(categories),
                judgingCodesJson = CompetitionJudgingHelper.serializeJsonList(judgingCodes),
                status = "SCHEDULED",
                notes = notes?.trim()
            )
            repository.saveCompetition(competition)

            // Setup criteria based on selected show standard or custom
            val criteriaToSave = if (!customCriteria.isNullOrEmpty()) {
                customCriteria.map { it.copy(competitionId = competition.id) }
            } else when (showStandard) {
                "NATIONAL" -> CompetitionJudgingHelper.createNationalStandardCriteria(competition.id)
                else -> CompetitionJudgingHelper.createWboStandardCriteria(competition.id)
            }
            repository.saveCriteriaForCompetition(competition.id, criteriaToSave)

            repository.recordAuditLog(
                actionType = "CREATE_COMPETITION",
                entityType = "COMPETITION",
                entityId = competition.id,
                summary = "Created competition: ${competition.title} with standard $showStandard"
            )

            // Auto-select newly created competition
            _selectedCompetitionId.value = competition.id
        }
    }

    fun updateCompetition(competition: CompetitionEntity) {
        viewModelScope.launch {
            repository.saveCompetition(competition)
            repository.recordAuditLog(
                actionType = "UPDATE_COMPETITION",
                entityType = "COMPETITION",
                entityId = competition.id,
                summary = "Updated competition details: ${competition.title}"
            )
        }
    }

    fun deleteCompetition(competitionId: String) {
        viewModelScope.launch {
            repository.deleteCompetition(competitionId)
            if (_selectedCompetitionId.value == competitionId) {
                _selectedCompetitionId.value = null
            }
        }
    }

    fun saveJudge(
        name: String,
        certification: String,
        affiliation: String?,
        country: String?,
        contactInfo: String?,
        assignedCodes: String?
    ) {
        viewModelScope.launch {
            val judge = JudgeEntity(
                name = name.trim(),
                certification = certification,
                affiliation = affiliation?.trim(),
                country = country?.trim(),
                contactInfo = contactInfo?.trim(),
                assignedCodes = assignedCodes?.trim()
            )
            repository.saveJudge(judge)
            repository.recordAuditLog(
                actionType = "REGISTER_JUDGE",
                entityType = "JUDGE",
                entityId = judge.id,
                summary = "Registered judge: ${judge.name} ($certification)"
            )
        }
    }

    fun deleteJudge(judge: JudgeEntity) {
        viewModelScope.launch {
            repository.deleteJudge(judge)
        }
    }

    fun saveCriteria(competitionId: String, criteria: List<CompetitionCriterionEntity>) {
        viewModelScope.launch {
            repository.saveCriteriaForCompetition(competitionId, criteria)
        }
    }

    fun recordOfficialScore(
        competitionId: String,
        birdRingNumber: String,
        participantName: String?,
        judgeId: String?,
        judgeName: String?,
        judgingCode: String?,
        showClass: String,
        cageNumberInShow: String?,
        criteriaScores: Map<String, Double>,
        criteriaList: List<CompetitionCriterionEntity>,
        awardTitleOverride: String?,
        notes: String?,
        photoUri: String?,
        isDigitallyConfirmed: Boolean,
        judgeSignature: String?
    ) {
        viewModelScope.launch {
            val totalScore = CompetitionJudgingHelper.calculateTotalWeightedScore(criteriaList, criteriaScores)
            val award = awardTitleOverride?.ifBlank { null } ?: CompetitionJudgingHelper.suggestAwardTitle(totalScore)
            val scoresJson = CompetitionJudgingHelper.serializeCriteriaScores(criteriaScores)

            val bird = repository.getBirdByRing(birdRingNumber)
            val visualAiAdvice = CompetitionJudgingHelper.generateVisualAiSuggestion(
                birdMutation = bird?.mutation,
                variety = bird?.variety?.name,
                notes = notes
            )

            val scoreEntity = CompetitionScoreEntity(
                competitionId = competitionId,
                birdRingNumber = birdRingNumber.trim(),
                participantName = participantName?.trim(),
                judgeId = judgeId,
                judgeName = judgeName?.trim(),
                judgingCode = judgingCode?.trim(),
                showClass = showClass.trim(),
                cageNumberInShow = cageNumberInShow?.trim(),
                criteriaScoresJson = scoresJson,
                totalScore = totalScore,
                awardTitle = award,
                notes = notes?.trim(),
                photoUri = photoUri,
                isDigitallyConfirmed = isDigitallyConfirmed,
                confirmedAt = if (isDigitallyConfirmed) System.currentTimeMillis() else null,
                confirmedByJudgeSignature = judgeSignature?.trim(),
                visualAiSuggestion = visualAiAdvice
            )

            repository.saveCompetitionScore(scoreEntity)
            repository.recordAuditLog(
                actionType = "RECORD_SCORE",
                entityType = "COMPETITION_SCORE",
                entityId = scoreEntity.id,
                summary = "Judge ${judgeName ?: "Official"} awarded $totalScore pts to $birdRingNumber ($award)"
            )
        }
    }

    fun deleteScore(scoreId: String) {
        viewModelScope.launch {
            repository.deleteCompetitionScore(scoreId)
        }
    }

    class Factory(
        private val repository: AviaryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CompetitionsViewModel(repository) as T
        }
    }
}
