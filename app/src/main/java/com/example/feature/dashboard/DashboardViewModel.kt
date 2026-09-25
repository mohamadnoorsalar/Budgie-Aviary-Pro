package com.example.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UpcomingHatchItem(
    val egg: EggEntity,
    val daysRemaining: Int
)

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0
)

data class DashboardUiState(
    val totalBirds: Int = 0,
    val activePairs: Int = 0,
    val totalCages: Int = 0,
    val totalEggs: Int = 0,
    val totalChicks: Int = 0,
    val todaysTasks: List<ReminderEntity> = emptyList(),
    val upcomingHatchList: List<UpcomingHatchItem> = emptyList(),
    val activeMedications: List<MedicationEntity> = emptyList(),
    val lowStockAlerts: List<InventoryItemEntity> = emptyList(),
    val recentActivity: List<AuditLogEntity> = emptyList(),
    val financialSummary: FinancialSummary = FinancialSummary(),
    val availableBirds: List<BirdEntity> = emptyList(),
    val availablePairs: List<PairEntity> = emptyList(),
    val availableCages: List<CageEntity> = emptyList(),
    val isReady: Boolean = true
)

private data class FacilityMetrics(
    val birds: Int,
    val pairs: Int,
    val cages: Int,
    val eggs: Int,
    val chicks: Int
)

private data class CareAndAlerts(
    val tasks: List<ReminderEntity>,
    val upcomingEggs: List<EggEntity>,
    val medications: List<MedicationEntity>,
    val lowStock: List<InventoryItemEntity>
)

private data class ActivityAndFinance(
    val logs: List<AuditLogEntity>,
    val income: Double,
    val expenses: Double
)

private data class QuickActionOptions(
    val birds: List<BirdEntity>,
    val pairs: List<PairEntity>,
    val cages: List<CageEntity>
)

class DashboardViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val facilityMetricsFlow = combine(
        repository.birdCount,
        repository.activePairCount,
        repository.cageCount,
        repository.totalEggCount,
        repository.totalChickCount
    ) { birds, pairs, cages, eggs, chicks ->
        FacilityMetrics(birds, pairs, cages, eggs, chicks)
    }

    private val careAndAlertsFlow = combine(
        repository.pendingReminders,
        repository.upcomingHatchEggs,
        repository.activeMedications,
        repository.lowStockItems
    ) { reminders, upcomingEggs, medications, lowStock ->
        CareAndAlerts(reminders, upcomingEggs, medications, lowStock)
    }

    private val activityAndFinanceFlow = combine(
        repository.recentAuditLogs,
        repository.totalIncome,
        repository.totalExpenses
    ) { logs, income, expenses ->
        ActivityAndFinance(logs, income, expenses)
    }

    private val quickActionOptionsFlow = combine(
        repository.allBirds,
        repository.allPairs,
        repository.allCages
    ) { birds, pairs, cages ->
        QuickActionOptions(birds, pairs, cages)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        facilityMetricsFlow,
        careAndAlertsFlow,
        activityAndFinanceFlow,
        quickActionOptionsFlow
    ) { metrics, care, activityFinance, options ->
        val now = System.currentTimeMillis()
        val hatchItems = care.upcomingEggs.map { egg ->
            val targetDate = egg.expectedHatchDate ?: (egg.layDate + 18L * 86400000L)
            val diffDays = ((targetDate - now) / 86400000L).coerceAtLeast(0).toInt()
            UpcomingHatchItem(egg = egg, daysRemaining = diffDays)
        }

        DashboardUiState(
            totalBirds = metrics.birds,
            activePairs = metrics.pairs,
            totalCages = metrics.cages,
            totalEggs = metrics.eggs,
            totalChicks = metrics.chicks,
            todaysTasks = care.tasks,
            upcomingHatchList = hatchItems,
            activeMedications = care.medications,
            lowStockAlerts = care.lowStock,
            recentActivity = activityFinance.logs,
            financialSummary = FinancialSummary(
                totalIncome = activityFinance.income,
                totalExpenses = activityFinance.expenses,
                netBalance = activityFinance.income - activityFinance.expenses
            ),
            availableBirds = options.birds,
            availablePairs = options.pairs,
            availableCages = options.cages,
            isReady = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun markReminderCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = true))
            repository.recordAuditLog(
                actionType = "UPDATE",
                entityType = "REMINDER",
                entityId = reminder.id.toString(),
                summary = "Completed reminder: ${reminder.title}"
            )
        }
    }

    // Quick Actions Real Database Operations
    fun addBird(
        ringNumber: String,
        name: String?,
        gender: BirdGender,
        variety: BudgieVariety,
        mutation: String,
        color: String,
        cageCode: String?
    ) {
        viewModelScope.launch {
            val bird = BirdEntity(
                ringNumber = ringNumber.trim(),
                name = name?.takeIf { it.isNotBlank() },
                gender = gender,
                variety = variety,
                mutation = mutation.ifBlank { "Normal" },
                color = color.ifBlank { "Green" },
                cageCode = cageCode?.takeIf { it.isNotBlank() },
                status = BirdStatus.ACTIVE
            )
            repository.saveBird(bird)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "BIRD",
                entityId = bird.ringNumber,
                summary = "Added bird $ringNumber ($color $mutation)"
            )
            _userMessage.emit("Bird $ringNumber saved successfully.")
        }
    }

    fun addPair(
        maleRing: String,
        femaleRing: String,
        cageCode: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val pair = PairEntity(
                maleRingNumber = maleRing.trim(),
                femaleRingNumber = femaleRing.trim(),
                cageCode = cageCode.trim(),
                notes = notes?.takeIf { it.isNotBlank() },
                isActive = true
            )
            val id = repository.savePair(pair)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "PAIR",
                entityId = id.toString(),
                summary = "Paired cock $maleRing with hen $femaleRing in $cageCode"
            )
            _userMessage.emit("Breeding pair saved successfully.")
        }
    }

    fun addEgg(
        pairId: Long,
        eggNumber: Int,
        layDate: Long = System.currentTimeMillis(),
        notes: String?
    ) {
        viewModelScope.launch {
            val expectedHatch = layDate + 18L * 86400000L
            val egg = EggEntity(
                pairId = pairId,
                eggNumber = eggNumber,
                layDate = layDate,
                expectedHatchDate = expectedHatch,
                fertilityStatus = "UNCANDLED",
                notes = notes?.takeIf { it.isNotBlank() }
            )
            repository.saveEgg(egg)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "EGG",
                entityId = egg.id,
                summary = "Recorded egg #$eggNumber for pair #$pairId"
            )
            _userMessage.emit("Egg #$eggNumber logged successfully.")
        }
    }

    fun addChick(
        pairId: Long,
        eggId: String,
        bandNumber: String?,
        hatchDate: Long = System.currentTimeMillis(),
        notes: String?
    ) {
        viewModelScope.launch {
            val chick = ChickEntity(
                eggId = eggId,
                pairId = pairId,
                hatchDate = hatchDate,
                bandedRingNumber = bandNumber?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() },
                status = "IN_NEST"
            )
            repository.saveChick(chick)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "CHICK",
                entityId = chick.id,
                summary = "Hatched chick ${bandNumber ?: eggId} for pair #$pairId"
            )
            _userMessage.emit("Chick logged successfully.")
        }
    }

    fun recordWeight(
        birdRingNumber: String,
        weightGrams: Double,
        conditionScore: String = "OPTIMAL",
        notes: String?
    ) {
        viewModelScope.launch {
            val weight = WeightRecordEntity(
                birdRingNumber = birdRingNumber.trim(),
                weightGrams = weightGrams,
                conditionScore = conditionScore,
                notes = notes?.takeIf { it.isNotBlank() }
            )
            repository.saveWeight(weight)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "WEIGHT",
                entityId = birdRingNumber,
                summary = "Recorded weight ${weightGrams}g for bird $birdRingNumber"
            )
            _userMessage.emit("Weight logged for bird $birdRingNumber.")
        }
    }

    fun recordFeeding(
        planName: String,
        cageCode: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val plan = NutritionPlanEntity(
                planName = planName.trim(),
                seasonPhase = "BREEDING",
                seedMixDescription = "Canary seed, white millet, red millet, oats",
                softFoodRecipe = "Egg food, sprouted seeds, grated carrot",
                freshGreensSchedule = "Broccoli, spinach (daily)",
                supplementsSchedule = "Vitamins + Calcium in drinking water",
                targetCageCode = cageCode?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() },
                isActive = true
            )
            repository.saveNutritionPlan(plan)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "NUTRITION",
                entityId = plan.id,
                summary = "Logged feeding plan: $planName"
            )
            _userMessage.emit("Feeding recorded successfully.")
        }
    }

    fun recordMedication(
        birdRingNumber: String?,
        medicationName: String,
        dosage: String,
        route: String = "WATER",
        frequency: String = "DAILY",
        durationDays: Int = 5,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val medication = MedicationEntity(
                birdRingNumber = birdRingNumber?.takeIf { it.isNotBlank() },
                medicationName = medicationName.trim(),
                dosage = dosage.trim(),
                administrationRoute = route,
                frequency = frequency,
                startDate = now,
                endDate = now + durationDays * 86400000L,
                notes = notes?.takeIf { it.isNotBlank() },
                isCompleted = false
            )
            repository.saveMedication(medication)
            repository.recordAuditLog(
                actionType = "INSERT",
                entityType = "MEDICATION",
                entityId = medication.id,
                summary = "Prescribed $medicationName ($dosage) for ${birdRingNumber ?: "Aviary Flock"}"
            )
            _userMessage.emit("Medication treatment scheduled.")
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}

