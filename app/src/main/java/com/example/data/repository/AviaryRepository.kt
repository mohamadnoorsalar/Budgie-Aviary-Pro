package com.example.data.repository

import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import com.example.data.database.entity.JudgeEntity
import com.example.data.database.entity.MediaDocumentEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.UserEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import com.example.data.database.relation.BirdWithParents
import com.example.data.database.relation.EggWithChick
import com.example.data.database.relation.PairWithBreedingDetails
import kotlinx.coroutines.flow.Flow

interface AviaryRepository {
    // --- Birds ---
    val allBirds: Flow<List<BirdEntity>>
    val birdCount: Flow<Int>
    suspend fun getBirdByRing(ring: String): BirdEntity?
    fun getBirdWithDetails(ringNumber: String): Flow<BirdWithDetails?>
    fun getBirdWithParents(ringNumber: String): Flow<BirdWithParents?>
    fun getBirdWithChildren(ringNumber: String): Flow<BirdWithChildren?>
    suspend fun saveBird(bird: BirdEntity)
    suspend fun deleteBird(ringNumber: String)

    // --- Pairs ---
    val allPairs: Flow<List<PairEntity>>
    val allPairsWithBreedingDetails: Flow<List<PairWithBreedingDetails>>
    val activePairs: Flow<List<PairEntity>>
    val activePairCount: Flow<Int>
    fun getPairById(id: Long): Flow<PairEntity?>
    fun getPairWithBreedingDetails(pairId: Long): Flow<PairWithBreedingDetails?>
    fun getPairsForBird(ringNumber: String): Flow<List<PairEntity>>
    fun getPairsForCage(cageCode: String): Flow<List<PairEntity>>
    fun getActivePairForCage(cageCode: String): Flow<PairEntity?>
    suspend fun savePair(pair: PairEntity): Long
    suspend fun updatePair(pair: PairEntity)
    suspend fun deletePair(pair: PairEntity)

    // --- Cages ---
    val allCages: Flow<List<CageEntity>>
    val allCagesWithDetails: Flow<List<com.example.data.database.relation.CageWithDetails>>
    val cageCount: Flow<Int>
    fun getCageByCode(code: String): Flow<CageEntity?>
    fun getCageWithDetails(code: String): Flow<com.example.data.database.relation.CageWithDetails?>
    fun getBirdsInCage(cageCode: String): Flow<List<BirdEntity>>
    fun getNestInCage(cageCode: String): Flow<NestEntity?>
    suspend fun saveCage(cage: CageEntity)
    suspend fun updateCage(cage: CageEntity)
    suspend fun deleteCage(cage: CageEntity)
    suspend fun moveBirdToCage(birdRing: String, newCageCode: String?, reason: String? = null)
    suspend fun setCageCleanStatus(cageCode: String, isClean: Boolean)

    // --- Reproduction (Nests, Clutches, Eggs, Chicks) ---
    val allNests: Flow<List<NestEntity>>
    suspend fun saveNest(nest: NestEntity)
    suspend fun deleteNest(nestId: String)
    val activeClutches: Flow<List<ClutchEntity>>
    val allClutches: Flow<List<ClutchEntity>>
    val totalActiveEggs: Flow<Int>
    val totalHatchedChicks: Flow<Int>
    val totalEggCount: Flow<Int>
    val totalChickCount: Flow<Int>
    val upcomingHatchEggs: Flow<List<EggEntity>>
    val allEggs: Flow<List<EggEntity>>
    val allChicks: Flow<List<ChickEntity>>
    val allChicksWithDetails: Flow<List<com.example.data.database.relation.ChickWithBreedingDetails>>
    suspend fun saveClutch(clutch: ClutchEntity): Long
    fun getClutchesForPair(pairId: Long): Flow<List<ClutchEntity>>
    fun getEggsForPair(pairId: Long): Flow<List<EggEntity>>
    fun getEggById(eggId: String): Flow<EggEntity?>
    fun getEggWithChick(eggId: String): Flow<EggWithChick?>
    suspend fun saveEgg(egg: EggEntity)
    suspend fun updateEgg(egg: EggEntity)
    suspend fun deleteEgg(eggId: String)
    suspend fun updateEggFertility(eggId: String, status: String, notes: String? = null)
    suspend fun hatchEgg(
        eggId: String,
        hatchDate: Long = System.currentTimeMillis(),
        initialWeightGrams: Double? = null,
        ringNumber: String? = null,
        notes: String? = null
    ): ChickEntity
    fun getChicksForPair(pairId: Long): Flow<List<ChickEntity>>
    fun getChickById(chickId: String): Flow<ChickEntity?>
    suspend fun saveChick(chick: ChickEntity)
    suspend fun updateChick(chick: ChickEntity)
    suspend fun deleteChick(chickId: String)
    suspend fun recordChickWeight(
        chickId: String,
        weightGrams: Double,
        conditionScore: String = "OPTIMAL",
        notes: String? = null
    )
    suspend fun recordChickMortality(
        chickId: String,
        mortalityDate: Long = System.currentTimeMillis(),
        reason: String,
        notes: String? = null
    )
    suspend fun transferChickToCage(
        chickId: String,
        newCageCode: String,
        transferDate: Long = System.currentTimeMillis(),
        notes: String? = null
    )
    suspend fun registerChickAsOffspring(
        chickId: String,
        ringNumber: String,
        name: String? = null,
        gender: com.example.core.common.BirdGender = com.example.core.common.BirdGender.UNKNOWN,
        variety: com.example.core.common.BudgieVariety? = null,
        color: String? = null,
        notes: String? = null
    ): BirdEntity
    suspend fun generateBreedingReminders(): Int

    // --- Genetics ---
    val allGenetics: Flow<List<BirdGeneticsEntity>>
    fun getGeneticsForBird(ringNumber: String): Flow<BirdGeneticsEntity?>
    suspend fun saveGenetics(genetics: BirdGeneticsEntity)

    // --- Pedigree ---
    val allPedigrees: Flow<List<PedigreeRecordEntity>>
    fun getPedigreeForBird(ringNumber: String): Flow<PedigreeRecordEntity?>
    suspend fun savePedigree(pedigree: PedigreeRecordEntity)
    fun getPedigreesWithParent(parentRing: String): Flow<List<PedigreeRecordEntity>>

    // --- Health & Weights ---
    val allHealthRecords: Flow<List<HealthRecordEntity>>
    val activeHealthIssues: Flow<List<HealthRecordEntity>>
    fun getHealthRecordsForBird(ringNumber: String): Flow<List<HealthRecordEntity>>
    suspend fun saveHealthRecord(record: HealthRecordEntity)
    suspend fun updateHealthRecord(record: HealthRecordEntity)
    suspend fun deleteHealthRecord(record: HealthRecordEntity)
    val allMedications: Flow<List<MedicationEntity>>
    val activeMedications: Flow<List<MedicationEntity>>
    fun getMedicationsForBird(ringNumber: String): Flow<List<MedicationEntity>>
    suspend fun saveMedication(medication: MedicationEntity)
    suspend fun scheduleMedicationWithReminders(medication: MedicationEntity): Long
    suspend fun deleteMedication(medication: MedicationEntity)
    suspend fun generateMedicationReminders(): Int
    val allWeightRecords: Flow<List<WeightRecordEntity>>
    fun getWeightHistoryForBird(ringNumber: String): Flow<List<WeightRecordEntity>>
    suspend fun saveWeight(weight: WeightRecordEntity)
    suspend fun deleteWeight(weight: WeightRecordEntity)

    // --- Nutrition & Inventory ---
    val allNutritionPlans: Flow<List<NutritionPlanEntity>>
    suspend fun saveNutritionPlan(plan: NutritionPlanEntity)
    suspend fun deleteNutritionPlan(plan: NutritionPlanEntity)
    val allNutritionRecords: Flow<List<NutritionRecordEntity>>
    fun getNutritionRecordsForBird(ringNumber: String): Flow<List<NutritionRecordEntity>>
    fun getNutritionRecordsForCage(cageCode: String): Flow<List<NutritionRecordEntity>>
    suspend fun saveNutritionRecord(record: NutritionRecordEntity)
    suspend fun deleteNutritionRecord(record: NutritionRecordEntity)
    // --- Inventory & Stock Management ---
    val allInventoryItems: Flow<List<InventoryItemEntity>>
    val lowStockItems: Flow<List<InventoryItemEntity>>
    fun getInventoryByCategory(category: String): Flow<List<InventoryItemEntity>>
    suspend fun saveInventoryItem(item: InventoryItemEntity)
    suspend fun deleteInventoryItem(item: InventoryItemEntity)
    val allInventoryTransactions: Flow<List<InventoryTransactionEntity>>
    fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransactionEntity>>
    suspend fun recordStockIn(itemId: String, quantity: Double, unitPrice: Double, notes: String?, createExpense: Boolean = false)
    suspend fun recordStockOut(itemId: String, quantity: Double, reason: String, notes: String?)
    suspend fun recordConsumption(itemId: String, quantity: Double, referenceType: String? = null, referenceId: String? = null, notes: String? = null)

    // --- Finance & Accounting ---
    val allExpenses: Flow<List<ExpenseEntity>>
    val allIncome: Flow<List<IncomeEntity>>
    val totalExpenses: Flow<Double>
    val totalIncome: Flow<Double>
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>
    fun getExpensesForPair(pairId: String): Flow<List<ExpenseEntity>>
    fun getIncomeByCategory(category: String): Flow<List<IncomeEntity>>
    fun getIncomeForBird(ringNumber: String): Flow<List<IncomeEntity>>
    suspend fun saveExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun saveIncome(income: IncomeEntity)
    suspend fun deleteIncome(income: IncomeEntity)

    // --- Competitions, Judges, Criteria, Scores ---
    val allCompetitions: Flow<List<CompetitionEntity>>
    fun getCompetitionById(competitionId: String): Flow<CompetitionEntity?>
    suspend fun saveCompetition(competition: CompetitionEntity)
    suspend fun deleteCompetition(competitionId: String)
    val allJudges: Flow<List<JudgeEntity>>
    suspend fun saveJudge(judge: JudgeEntity)
    suspend fun deleteJudge(judge: JudgeEntity)
    fun getCriteriaForCompetition(competitionId: String): Flow<List<CompetitionCriterionEntity>>
    suspend fun saveCriteriaForCompetition(competitionId: String, criteria: List<CompetitionCriterionEntity>)
    fun getScoresForBird(ringNumber: String): Flow<List<CompetitionScoreEntity>>
    fun getScoresForCompetition(competitionId: String): Flow<List<CompetitionScoreEntity>>
    fun getScoresForClass(competitionId: String, showClass: String): Flow<List<CompetitionScoreEntity>>
    suspend fun saveCompetitionScore(score: CompetitionScoreEntity)
    suspend fun deleteCompetitionScore(scoreId: String)

    // --- Reminders ---
    val allReminders: Flow<List<ReminderEntity>>
    val pendingReminders: Flow<List<ReminderEntity>>
    val completedReminders: Flow<List<ReminderEntity>>
    val pendingReminderCount: Flow<Int>
    fun getRemindersByType(type: com.example.core.common.ReminderType): Flow<List<ReminderEntity>>
    suspend fun saveReminder(reminder: ReminderEntity): Long
    suspend fun updateReminder(reminder: ReminderEntity)
    suspend fun deleteReminder(reminder: ReminderEntity)
    suspend fun deleteReminderById(id: Long)
    suspend fun clearCompletedReminders()
    suspend fun syncSmartReminders(): Int

    // --- Media Documents & Users & Audit ---
    fun getMediaForEntity(type: String, id: String): Flow<List<MediaDocumentEntity>>
    suspend fun saveMedia(media: MediaDocumentEntity)
    val activeUsers: Flow<List<UserEntity>>
    suspend fun saveUser(user: UserEntity)
    val recentAuditLogs: Flow<List<AuditLogEntity>>
    fun getLogsForEntity(type: String, id: String): Flow<List<AuditLogEntity>>
    suspend fun recordAuditLog(actionType: String, entityType: String, entityId: String, summary: String)
}
