package com.example.feature.aiassistant

import com.example.core.ai.AviaryDataRetriever
import com.example.core.ai.FacilityContextData
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.core.common.ReminderPriority
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AviaryDataRetrieverTest {

    // Simple fake repository without mocking libraries
    private val fakeRepository = object : FakeAviaryRepository() {}
    private val retriever = AviaryDataRetriever(fakeRepository)

    @Test
    fun testMissingDataIsExplicitlyReported() {
        val emptyContext = FacilityContextData(
            query = "پرنده‌های قفس ۹۹ را نشان بده",
            birds = emptyList(),
            pairs = emptyList(),
            clutches = emptyList(),
            eggs = emptyList(),
            chicks = emptyList(),
            cages = emptyList(),
            reminders = emptyList(),
            health = emptyList(),
            weights = emptyList(),
            inventory = emptyList(),
            expenses = emptyList(),
            incomes = emptyList()
        )
        val answer = retriever.answerLocallyWithoutApi(emptyContext)
        assertTrue(answer.contains("اطلاعات مورد نظر در پایگاه داده ثبت نشده است"))
    }

    @Test
    fun testCageBirdsRetrieval() {
        val cage = CageEntity(code = "C-25", type = CageType.BREEDING_BOX, capacity = 2, isClean = true)
        val bird1 = BirdEntity(ringNumber = "IR-2024-01", gender = BirdGender.MALE, variety = BudgieVariety.ENGLISH_SHOW, color = "Blue", cageCode = "C-25", status = BirdStatus.ACTIVE)
        val bird2 = BirdEntity(ringNumber = "IR-2024-02", gender = BirdGender.FEMALE, variety = BudgieVariety.ENGLISH_SHOW, color = "Yellow", cageCode = "C-25", status = BirdStatus.ACTIVE)

        val context = FacilityContextData(
            query = "پرنده‌های قفس ۲۵ را نشان بده",
            birds = listOf(bird1, bird2),
            pairs = emptyList(),
            clutches = emptyList(),
            eggs = emptyList(),
            chicks = emptyList(),
            cages = listOf(cage),
            reminders = emptyList(),
            health = emptyList(),
            weights = emptyList(),
            inventory = emptyList(),
            expenses = emptyList(),
            incomes = emptyList()
        )
        val answer = retriever.answerLocallyWithoutApi(context)
        assertTrue(answer.contains("C-25"))
        assertTrue(answer.contains("IR-2024-01"))
        assertTrue(answer.contains("IR-2024-02"))
    }

    @Test
    fun testPairPerformanceRetrieval() {
        val pair = PairEntity(id = 12L, maleRingNumber = "IR-M-12", femaleRingNumber = "IR-F-12", cageCode = "C-12", isActive = true)
        val clutch1 = ClutchEntity(id = 101L, pairId = 12L, clutchNumber = 1, startDate = System.currentTimeMillis() - 60L*86400000L, eggCount = 6, fertileCount = 5, hatchedCount = 5, isActive = false)
        val clutch2 = ClutchEntity(id = 102L, pairId = 12L, clutchNumber = 2, startDate = System.currentTimeMillis() - 20L*86400000L, eggCount = 5, fertileCount = 4, hatchedCount = 4, isActive = true)

        val context = FacilityContextData(
            query = "جفت ۱۲ در سه دوره اخیر چه عملکردی داشته؟",
            birds = emptyList(),
            pairs = listOf(pair),
            clutches = listOf(clutch1, clutch2),
            eggs = emptyList(),
            chicks = emptyList(),
            cages = emptyList(),
            reminders = emptyList(),
            health = emptyList(),
            weights = emptyList(),
            inventory = emptyList(),
            expenses = emptyList(),
            incomes = emptyList()
        )
        val answer = retriever.answerLocallyWithoutApi(context)
        assertTrue(answer.contains("12"))
        assertTrue(answer.contains("دوره 1"))
        assertTrue(answer.contains("دوره 2"))
        assertTrue(answer.contains("9")) // 5 + 4 hatched
    }

    @Test
    fun testTodayTasksRetrieval() {
        val reminder = ReminderEntity(id = 1L, title = "تعویض آب و دانه تقویتی", dueDate = System.currentTimeMillis(), isCompleted = false, priority = ReminderPriority.HIGH)
        val context = FacilityContextData(
            query = "امروز چه کارهایی دارم؟",
            birds = emptyList(),
            pairs = emptyList(),
            clutches = emptyList(),
            eggs = emptyList(),
            chicks = emptyList(),
            cages = emptyList(),
            reminders = listOf(reminder),
            health = emptyList(),
            weights = emptyList(),
            inventory = emptyList(),
            expenses = emptyList(),
            incomes = emptyList()
        )
        val answer = retriever.answerLocallyWithoutApi(context)
        assertTrue(answer.contains("تعویض آب و دانه تقویتی"))
    }

    @Test
    fun testBirdLatestWeightRetrieval() {
        val bird = BirdEntity(ringNumber = "IR-2024-88", gender = BirdGender.MALE, variety = BudgieVariety.ENGLISH_SHOW, cageCode = "C-01")
        val weight = WeightRecordEntity(id = UUID.randomUUID().toString(), birdRingNumber = "IR-2024-88", weightGrams = 42.5, recordedDate = System.currentTimeMillis(), notes = "عالی")

        val context = FacilityContextData(
            query = "آخرین وزن این پرنده IR-2024-88 چقدر است؟",
            birds = listOf(bird),
            pairs = emptyList(),
            clutches = emptyList(),
            eggs = emptyList(),
            chicks = emptyList(),
            cages = emptyList(),
            reminders = emptyList(),
            health = emptyList(),
            weights = listOf(weight),
            inventory = emptyList(),
            expenses = emptyList(),
            incomes = emptyList()
        )
        val answer = retriever.answerLocallyWithoutApi(context)
        assertTrue(answer.contains("42.5"))
        assertTrue(answer.contains("IR-2024-88"))
    }
}

open class FakeAviaryRepository : AviaryRepository {
    override val allBirds = flowOf(emptyList<BirdEntity>())
    override val birdCount = flowOf(0)
    override suspend fun getBirdByRing(ring: String): BirdEntity? = null
    override fun getBirdWithDetails(ringNumber: String) = flowOf(null)
    override fun getBirdWithParents(ringNumber: String) = flowOf(null)
    override fun getBirdWithChildren(ringNumber: String) = flowOf(null)
    override suspend fun saveBird(bird: BirdEntity) {}
    override suspend fun deleteBird(ringNumber: String) {}

    override val allPairs = flowOf(emptyList<PairEntity>())
    override val allPairsWithBreedingDetails = flowOf(emptyList<com.example.data.database.relation.PairWithBreedingDetails>())
    override val activePairs = flowOf(emptyList<PairEntity>())
    override val activePairCount = flowOf(0)
    override fun getPairById(id: Long) = flowOf(null)
    override fun getPairWithBreedingDetails(pairId: Long) = flowOf(null)
    override fun getPairsForBird(ringNumber: String) = flowOf(emptyList<PairEntity>())
    override fun getPairsForCage(cageCode: String) = flowOf(emptyList<PairEntity>())
    override fun getActivePairForCage(cageCode: String) = flowOf(null)
    override suspend fun savePair(pair: PairEntity): Long = 1L
    override suspend fun updatePair(pair: PairEntity) {}
    override suspend fun deletePair(pair: PairEntity) {}

    override val allCages = flowOf(emptyList<CageEntity>())
    override val allCagesWithDetails = flowOf(emptyList<com.example.data.database.relation.CageWithDetails>())
    override val cageCount = flowOf(0)
    override fun getCageByCode(code: String) = flowOf(null)
    override fun getCageWithDetails(code: String) = flowOf(null)
    override fun getBirdsInCage(cageCode: String) = flowOf(emptyList<BirdEntity>())
    override fun getNestInCage(cageCode: String) = flowOf(null)
    override suspend fun saveCage(cage: CageEntity) {}
    override suspend fun updateCage(cage: CageEntity) {}
    override suspend fun deleteCage(cage: CageEntity) {}
    override suspend fun moveBirdToCage(birdRing: String, newCageCode: String?, reason: String?) {}
    override suspend fun setCageCleanStatus(cageCode: String, isClean: Boolean) {}

    override val allNests = flowOf(emptyList<com.example.data.database.entity.NestEntity>())
    override suspend fun saveNest(nest: com.example.data.database.entity.NestEntity) {}
    override suspend fun deleteNest(nestId: String) {}
    override val activeClutches = flowOf(emptyList<ClutchEntity>())
    override val allClutches = flowOf(emptyList<ClutchEntity>())
    override val totalActiveEggs = flowOf(0)
    override val totalHatchedChicks = flowOf(0)
    override val totalEggCount = flowOf(0)
    override val totalChickCount = flowOf(0)
    override val upcomingHatchEggs = flowOf(emptyList<com.example.data.database.entity.EggEntity>())
    override val allEggs = flowOf(emptyList<com.example.data.database.entity.EggEntity>())
    override val allChicks = flowOf(emptyList<com.example.data.database.entity.ChickEntity>())
    override val allChicksWithDetails = flowOf(emptyList<com.example.data.database.relation.ChickWithBreedingDetails>())
    override suspend fun saveClutch(clutch: ClutchEntity): Long = 1L
    override fun getClutchesForPair(pairId: Long) = flowOf(emptyList<ClutchEntity>())
    override fun getEggsForPair(pairId: Long) = flowOf(emptyList<com.example.data.database.entity.EggEntity>())
    override fun getEggById(eggId: String) = flowOf(null)
    override fun getEggWithChick(eggId: String) = flowOf(null)
    override suspend fun saveEgg(egg: com.example.data.database.entity.EggEntity) {}
    override suspend fun updateEgg(egg: com.example.data.database.entity.EggEntity) {}
    override suspend fun deleteEgg(eggId: String) {}
    override suspend fun updateEggFertility(eggId: String, status: String, notes: String?) {}
    override suspend fun hatchEgg(eggId: String, hatchDate: Long, initialWeightGrams: Double?, ringNumber: String?, notes: String?) = com.example.data.database.entity.ChickEntity(eggId = eggId, pairId = 1L)
    override fun getChicksForPair(pairId: Long) = flowOf(emptyList<com.example.data.database.entity.ChickEntity>())
    override fun getChickById(chickId: String) = flowOf(null)
    override suspend fun saveChick(chick: com.example.data.database.entity.ChickEntity) {}
    override suspend fun updateChick(chick: com.example.data.database.entity.ChickEntity) {}
    override suspend fun deleteChick(chickId: String) {}
    override suspend fun recordChickWeight(chickId: String, weightGrams: Double, conditionScore: String, notes: String?) {}
    override suspend fun recordChickMortality(chickId: String, mortalityDate: Long, reason: String, notes: String?) {}
    override suspend fun transferChickToCage(chickId: String, newCageCode: String, transferDate: Long, notes: String?) {}
    override suspend fun registerChickAsOffspring(chickId: String, ringNumber: String, name: String?, gender: BirdGender, variety: BudgieVariety?, color: String?, notes: String?) = BirdEntity(ringNumber = ringNumber)
    override suspend fun generateBreedingReminders(): Int = 0

    override val allGenetics = flowOf(emptyList<com.example.data.database.entity.BirdGeneticsEntity>())
    override fun getGeneticsForBird(ringNumber: String) = flowOf(null)
    override suspend fun saveGenetics(genetics: com.example.data.database.entity.BirdGeneticsEntity) {}

    override val allPedigrees = flowOf(emptyList<com.example.data.database.entity.PedigreeRecordEntity>())
    override fun getPedigreeForBird(ringNumber: String) = flowOf(null)
    override suspend fun savePedigree(pedigree: com.example.data.database.entity.PedigreeRecordEntity) {}
    override fun getPedigreesWithParent(parentRing: String) = flowOf(emptyList<com.example.data.database.entity.PedigreeRecordEntity>())

    override val allHealthRecords = flowOf(emptyList<com.example.data.database.entity.HealthRecordEntity>())
    override val activeHealthIssues = flowOf(emptyList<com.example.data.database.entity.HealthRecordEntity>())
    override fun getHealthRecordsForBird(ringNumber: String) = flowOf(emptyList<com.example.data.database.entity.HealthRecordEntity>())
    override suspend fun saveHealthRecord(record: com.example.data.database.entity.HealthRecordEntity) {}
    override suspend fun updateHealthRecord(record: com.example.data.database.entity.HealthRecordEntity) {}
    override suspend fun deleteHealthRecord(record: com.example.data.database.entity.HealthRecordEntity) {}
    override val allMedications = flowOf(emptyList<com.example.data.database.entity.MedicationEntity>())
    override val activeMedications = flowOf(emptyList<com.example.data.database.entity.MedicationEntity>())
    override fun getMedicationsForBird(ringNumber: String) = flowOf(emptyList<com.example.data.database.entity.MedicationEntity>())
    override suspend fun saveMedication(medication: com.example.data.database.entity.MedicationEntity) {}
    override suspend fun scheduleMedicationWithReminders(medication: com.example.data.database.entity.MedicationEntity): Long = 1L
    override suspend fun deleteMedication(medication: com.example.data.database.entity.MedicationEntity) {}
    override suspend fun generateMedicationReminders(): Int = 0
    override val allWeightRecords = flowOf(emptyList<WeightRecordEntity>())
    override fun getWeightHistoryForBird(ringNumber: String) = flowOf(emptyList<WeightRecordEntity>())
    override suspend fun saveWeight(weight: WeightRecordEntity) {}
    override suspend fun deleteWeight(weight: WeightRecordEntity) {}

    override val allNutritionPlans = flowOf(emptyList<com.example.data.database.entity.NutritionPlanEntity>())
    override suspend fun saveNutritionPlan(plan: com.example.data.database.entity.NutritionPlanEntity) {}
    override suspend fun deleteNutritionPlan(plan: com.example.data.database.entity.NutritionPlanEntity) {}
    override val allNutritionRecords = flowOf(emptyList<com.example.data.database.entity.NutritionRecordEntity>())
    override fun getNutritionRecordsForBird(ringNumber: String) = flowOf(emptyList<com.example.data.database.entity.NutritionRecordEntity>())
    override fun getNutritionRecordsForCage(cageCode: String) = flowOf(emptyList<com.example.data.database.entity.NutritionRecordEntity>())
    override suspend fun saveNutritionRecord(record: com.example.data.database.entity.NutritionRecordEntity) {}
    override suspend fun deleteNutritionRecord(record: com.example.data.database.entity.NutritionRecordEntity) {}

    override val allInventoryItems = flowOf(emptyList<com.example.data.database.entity.InventoryItemEntity>())
    override val lowStockItems = flowOf(emptyList<com.example.data.database.entity.InventoryItemEntity>())
    override fun getInventoryByCategory(category: String) = flowOf(emptyList<com.example.data.database.entity.InventoryItemEntity>())
    override suspend fun saveInventoryItem(item: com.example.data.database.entity.InventoryItemEntity) {}
    override suspend fun deleteInventoryItem(item: com.example.data.database.entity.InventoryItemEntity) {}
    override val allInventoryTransactions = flowOf(emptyList<com.example.data.database.entity.InventoryTransactionEntity>())
    override fun getTransactionsForItem(itemId: String) = flowOf(emptyList<com.example.data.database.entity.InventoryTransactionEntity>())
    override suspend fun recordStockIn(itemId: String, quantity: Double, unitPrice: Double, notes: String?, createExpense: Boolean) {}
    override suspend fun recordStockOut(itemId: String, quantity: Double, reason: String, notes: String?) {}
    override suspend fun recordConsumption(itemId: String, quantity: Double, referenceType: String?, referenceId: String?, notes: String?) {}

    override val allExpenses = flowOf(emptyList<com.example.data.database.entity.ExpenseEntity>())
    override val allIncome = flowOf(emptyList<com.example.data.database.entity.IncomeEntity>())
    override val totalExpenses = flowOf(0.0)
    override val totalIncome = flowOf(0.0)
    override fun getExpensesByCategory(category: String) = flowOf(emptyList<com.example.data.database.entity.ExpenseEntity>())
    override fun getExpensesForPair(pairId: String) = flowOf(emptyList<com.example.data.database.entity.ExpenseEntity>())
    override fun getIncomeByCategory(category: String) = flowOf(emptyList<com.example.data.database.entity.IncomeEntity>())
    override fun getIncomeForBird(ringNumber: String) = flowOf(emptyList<com.example.data.database.entity.IncomeEntity>())
    override suspend fun saveExpense(expense: com.example.data.database.entity.ExpenseEntity) {}
    override suspend fun deleteExpense(expense: com.example.data.database.entity.ExpenseEntity) {}
    override suspend fun saveIncome(income: com.example.data.database.entity.IncomeEntity) {}
    override suspend fun deleteIncome(income: com.example.data.database.entity.IncomeEntity) {}

    override val allCompetitions = flowOf(emptyList<com.example.data.database.entity.CompetitionEntity>())
    override fun getCompetitionById(competitionId: String) = flowOf(null)
    override suspend fun saveCompetition(competition: com.example.data.database.entity.CompetitionEntity) {}
    override suspend fun deleteCompetition(competitionId: String) {}
    override val allJudges = flowOf(emptyList<com.example.data.database.entity.JudgeEntity>())
    override suspend fun saveJudge(judge: com.example.data.database.entity.JudgeEntity) {}
    override suspend fun deleteJudge(judge: com.example.data.database.entity.JudgeEntity) {}
    override fun getCriteriaForCompetition(competitionId: String) = flowOf(emptyList<com.example.data.database.entity.CompetitionCriterionEntity>())
    override suspend fun saveCriteriaForCompetition(competitionId: String, criteria: List<com.example.data.database.entity.CompetitionCriterionEntity>) {}
    override fun getScoresForBird(ringNumber: String) = flowOf(emptyList<com.example.data.database.entity.CompetitionScoreEntity>())
    override fun getScoresForCompetition(competitionId: String) = flowOf(emptyList<com.example.data.database.entity.CompetitionScoreEntity>())
    override fun getScoresForClass(competitionId: String, showClass: String) = flowOf(emptyList<com.example.data.database.entity.CompetitionScoreEntity>())
    override suspend fun saveCompetitionScore(score: com.example.data.database.entity.CompetitionScoreEntity) {}
    override suspend fun deleteCompetitionScore(scoreId: String) {}

    override val allReminders = flowOf(emptyList<ReminderEntity>())
    override val pendingReminders = flowOf(emptyList<ReminderEntity>())
    override val completedReminders = flowOf(emptyList<ReminderEntity>())
    override val pendingReminderCount = flowOf(0)
    override fun getRemindersByType(type: com.example.core.common.ReminderType) = flowOf(emptyList<ReminderEntity>())
    override suspend fun saveReminder(reminder: ReminderEntity): Long = 1L
    override suspend fun updateReminder(reminder: ReminderEntity) {}
    override suspend fun deleteReminder(reminder: ReminderEntity) {}
    override suspend fun deleteReminderById(id: Long) {}
    override suspend fun clearCompletedReminders() {}
    override suspend fun syncSmartReminders(): Int = 0

    override fun getMediaForEntity(type: String, id: String) = flowOf(emptyList<com.example.data.database.entity.MediaDocumentEntity>())
    override suspend fun saveMedia(media: com.example.data.database.entity.MediaDocumentEntity) {}
    override val activeUsers = flowOf(emptyList<com.example.data.database.entity.UserEntity>())
    override suspend fun saveUser(user: com.example.data.database.entity.UserEntity) {}
    override val recentAuditLogs = flowOf(emptyList<com.example.data.database.entity.AuditLogEntity>())
    override fun getLogsForEntity(type: String, id: String) = flowOf(emptyList<com.example.data.database.entity.AuditLogEntity>())
    override suspend fun recordAuditLog(actionType: String, entityType: String, entityId: String, summary: String) {}
}
