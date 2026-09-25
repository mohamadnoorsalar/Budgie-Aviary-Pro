package com.example.data.repository

import com.example.core.inventory.InventoryAnalyticsHelper
import com.example.data.database.AppDatabase
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
import com.example.data.database.entity.ExpenseCategory
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
import com.example.data.database.relation.ChickWithBreedingDetails
import com.example.data.database.relation.EggWithChick
import com.example.data.database.relation.PairWithBreedingDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AviaryRepositoryImpl(private val database: AppDatabase) : AviaryRepository {

    // --- Birds ---
    override val allBirds: Flow<List<BirdEntity>> = database.birdDao().getAllBirds()
    override val birdCount: Flow<Int> = database.birdDao().getBirdCount()
    override suspend fun getBirdByRing(ring: String): BirdEntity? = database.birdDao().getBirdByRing(ring)
    override fun getBirdWithDetails(ringNumber: String): Flow<BirdWithDetails?> = database.birdDao().getBirdWithDetails(ringNumber)
    override fun getBirdWithParents(ringNumber: String): Flow<BirdWithParents?> = database.birdDao().getBirdWithParents(ringNumber)
    override fun getBirdWithChildren(ringNumber: String): Flow<BirdWithChildren?> = database.birdDao().getBirdWithChildren(ringNumber)
    override suspend fun saveBird(bird: BirdEntity) = database.birdDao().insertBird(bird)
    override suspend fun deleteBird(ringNumber: String) = database.birdDao().deleteBirdByRing(ringNumber)

    // --- Pairs ---
    override val allPairs: Flow<List<PairEntity>> = database.pairDao().getAllPairs()
    override val allPairsWithBreedingDetails: Flow<List<PairWithBreedingDetails>> =
        database.pairDao().getAllPairsWithBreedingDetails()
    override val activePairs: Flow<List<PairEntity>> = database.pairDao().getActivePairs()
    override val activePairCount: Flow<Int> = database.pairDao().getActivePairCount()
    override fun getPairById(id: Long): Flow<PairEntity?> = database.pairDao().getPairById(id)
    override fun getPairWithBreedingDetails(pairId: Long): Flow<PairWithBreedingDetails?> =
        database.reproductionDao().getPairWithBreedingDetails(pairId)
    override fun getPairsForBird(ringNumber: String): Flow<List<PairEntity>> =
        database.pairDao().getPairsForBird(ringNumber)
    override fun getPairsForCage(cageCode: String): Flow<List<PairEntity>> =
        database.pairDao().getPairsForCage(cageCode)
    override fun getActivePairForCage(cageCode: String): Flow<PairEntity?> =
        database.pairDao().getActivePairForCage(cageCode)
    override suspend fun savePair(pair: PairEntity): Long = database.pairDao().insertPair(pair)
    override suspend fun updatePair(pair: PairEntity) = database.pairDao().updatePair(pair)
    override suspend fun deletePair(pair: PairEntity) = database.pairDao().deletePair(pair)

    // --- Cages ---
    override val allCages: Flow<List<CageEntity>> = database.cageDao().getAllCages()
    override val allCagesWithDetails: Flow<List<com.example.data.database.relation.CageWithDetails>> =
        database.cageDao().getAllCagesWithDetails()
    override val cageCount: Flow<Int> = database.cageDao().getCageCount()
    override fun getCageByCode(code: String): Flow<CageEntity?> = database.cageDao().getCageByCode(code)
    override fun getCageWithDetails(code: String): Flow<com.example.data.database.relation.CageWithDetails?> =
        database.cageDao().getCageWithDetails(code)
    override fun getBirdsInCage(cageCode: String): Flow<List<BirdEntity>> =
        database.birdDao().getBirdsInCage(cageCode)
    override fun getNestInCage(cageCode: String): Flow<NestEntity?> =
        database.reproductionDao().getNestInCage(cageCode)
    override suspend fun saveCage(cage: CageEntity) = database.cageDao().insertCage(cage)
    override suspend fun updateCage(cage: CageEntity) = database.cageDao().updateCage(cage)
    override suspend fun deleteCage(cage: CageEntity) = database.cageDao().deleteCage(cage)

    override suspend fun moveBirdToCage(birdRing: String, newCageCode: String?, reason: String?) {
        val bird = database.birdDao().getBirdByRing(birdRing)
        if (bird != null) {
            val oldCage = bird.cageCode ?: "Unassigned"
            val targetCage = newCageCode ?: "Unassigned"
            database.birdDao().updateBird(bird.copy(cageCode = newCageCode))

            if (bird.cageCode != null) {
                recordAuditLog(
                    actionType = "MOVEMENT",
                    entityType = "CAGE",
                    entityId = bird.cageCode!!,
                    summary = "Bird ${bird.ringNumber} moved out to $targetCage. ${reason ?: ""}".trim()
                )
            }
            if (newCageCode != null) {
                recordAuditLog(
                    actionType = "MOVEMENT",
                    entityType = "CAGE",
                    entityId = newCageCode,
                    summary = "Bird ${bird.ringNumber} moved in from $oldCage. ${reason ?: ""}".trim()
                )
            }
            recordAuditLog(
                actionType = "MOVEMENT",
                entityType = "BIRD",
                entityId = bird.ringNumber,
                summary = "Moved from $oldCage to $targetCage. ${reason ?: ""}".trim()
            )
        }
    }

    override suspend fun setCageCleanStatus(cageCode: String, isClean: Boolean) {
        database.cageDao().updateCleanStatus(cageCode, isClean)
        recordAuditLog(
            actionType = if (isClean) "CLEANING" else "STATUS_CHANGE",
            entityType = "CAGE",
            entityId = cageCode,
            summary = if (isClean) "Cage $cageCode sanitized and marked clean" else "Cage $cageCode marked as scheduled for cleaning"
        )
    }

    // --- Reproduction (Nests, Clutches, Eggs, Chicks) ---
    override val allNests: Flow<List<NestEntity>> = database.reproductionDao().getAllNests()
    override suspend fun saveNest(nest: NestEntity) = database.reproductionDao().insertNest(nest)
    override suspend fun deleteNest(nestId: String) = database.reproductionDao().softDeleteNest(nestId)
    override val activeClutches: Flow<List<ClutchEntity>> = database.clutchDao().getActiveClutches()
    override val allClutches: Flow<List<ClutchEntity>> = database.reproductionDao().getAllClutches()
    override val totalActiveEggs: Flow<Int> = database.clutchDao().getTotalActiveEggs()
    override val totalHatchedChicks: Flow<Int> = database.clutchDao().getTotalHatchedChicks()
    override val totalEggCount: Flow<Int> = database.reproductionDao().getTotalEggCount()
    override val totalChickCount: Flow<Int> = database.reproductionDao().getTotalChicksCount()
    override val upcomingHatchEggs: Flow<List<EggEntity>> = database.reproductionDao().getUpcomingHatchEggs(15)
    override val allEggs: Flow<List<EggEntity>> = database.reproductionDao().getAllEggs()
    override val allChicks: Flow<List<ChickEntity>> = database.reproductionDao().getAllChicks()
    override val allChicksWithDetails: Flow<List<ChickWithBreedingDetails>> =
        database.reproductionDao().getAllChicksWithBreedingDetails()

    override suspend fun saveClutch(clutch: ClutchEntity): Long = database.clutchDao().insertClutch(clutch)
    override fun getClutchesForPair(pairId: Long): Flow<List<ClutchEntity>> = database.reproductionDao().getClutchesForPair(pairId)
    override fun getEggsForPair(pairId: Long): Flow<List<EggEntity>> = database.reproductionDao().getEggsForPair(pairId)
    override fun getEggById(eggId: String): Flow<EggEntity?> = database.reproductionDao().getEggById(eggId)
    override fun getEggWithChick(eggId: String): Flow<EggWithChick?> = database.reproductionDao().getEggWithChick(eggId)
    override suspend fun saveEgg(egg: EggEntity) = database.reproductionDao().insertEgg(egg)
    override suspend fun updateEgg(egg: EggEntity) = database.reproductionDao().updateEgg(egg)
    override suspend fun deleteEgg(eggId: String) = database.reproductionDao().softDeleteEgg(eggId)

    override suspend fun updateEggFertility(eggId: String, status: String, notes: String?) {
        val egg = database.reproductionDao().getEggSync(eggId) ?: return
        val updated = egg.copy(
            fertilityStatus = status,
            candlingDate = System.currentTimeMillis(),
            notes = if (notes != null) "${egg.notes.orEmpty()} | $notes".trimStart(' ', '|') else egg.notes,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateEgg(updated)
    }

    override suspend fun hatchEgg(
        eggId: String,
        hatchDate: Long,
        initialWeightGrams: Double?,
        ringNumber: String?,
        notes: String?
    ): ChickEntity {
        val egg = database.reproductionDao().getEggSync(eggId)
            ?: throw IllegalArgumentException("Egg not found with id $eggId")

        val updatedEgg = egg.copy(
            isHatched = true,
            actualHatchDate = hatchDate,
            eggResult = "HATCHED",
            fertilityStatus = "FERTILE",
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateEgg(updatedEgg)

        val existingChicks = database.reproductionDao().getChicksForPair(egg.pairId).first()
        val hatchOrder = existingChicks.size + 1

        val chick = ChickEntity(
            eggId = egg.id,
            pairId = egg.pairId,
            hatchDate = hatchDate,
            hatchOrder = hatchOrder,
            bandedRingNumber = ringNumber?.trim()?.ifEmpty { null },
            weightGrams = initialWeightGrams,
            growthStage = if (!ringNumber.isNullOrBlank()) "BANDED" else "HATCHLING",
            status = "IN_NEST",
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().insertChick(chick)

        if (initialWeightGrams != null) {
            val weightRecord = WeightRecordEntity(
                birdRingNumber = ringNumber?.trim()?.ifEmpty { null } ?: chick.id,
                weightGrams = initialWeightGrams,
                recordedDate = hatchDate,
                conditionScore = "OPTIMAL",
                notes = "Initial hatch weight (Order #$hatchOrder)"
            )
            database.healthDao().insertWeight(weightRecord)
        }

        // Automatically connect: Parent -> Pair -> Egg -> Chick -> Offspring -> Generation
        if (!ringNumber.isNullOrBlank()) {
            registerChickAsOffspring(
                chickId = chick.id,
                ringNumber = ringNumber.trim(),
                name = "Chick #$hatchOrder",
                notes = notes
            )
        }

        return chick
    }

    override fun getChicksForPair(pairId: Long): Flow<List<ChickEntity>> = database.reproductionDao().getChicksForPair(pairId)
    override fun getChickById(chickId: String): Flow<ChickEntity?> = database.reproductionDao().getChickById(chickId)
    override suspend fun saveChick(chick: ChickEntity) = database.reproductionDao().insertChick(chick)
    override suspend fun updateChick(chick: ChickEntity) = database.reproductionDao().updateChick(chick)
    override suspend fun deleteChick(chickId: String) = database.reproductionDao().softDeleteChick(chickId)

    override suspend fun recordChickWeight(
        chickId: String,
        weightGrams: Double,
        conditionScore: String,
        notes: String?
    ) {
        val chick = database.reproductionDao().getChickSync(chickId) ?: return
        val updated = chick.copy(
            weightGrams = weightGrams,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateChick(updated)

        val weightRef = chick.bandedRingNumber ?: chick.id
        val record = WeightRecordEntity(
            birdRingNumber = weightRef,
            weightGrams = weightGrams,
            recordedDate = System.currentTimeMillis(),
            conditionScore = conditionScore,
            notes = notes ?: "Chick weight log (${chick.growthStage})"
        )
        database.healthDao().insertWeight(record)
    }

    override suspend fun recordChickMortality(
        chickId: String,
        mortalityDate: Long,
        reason: String,
        notes: String?
    ) {
        val chick = database.reproductionDao().getChickSync(chickId) ?: return
        val updated = chick.copy(
            status = "DECEASED",
            mortalityDate = mortalityDate,
            mortalityReason = reason,
            notes = if (notes != null) "${chick.notes.orEmpty()} | $notes".trimStart(' ', '|') else chick.notes,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateChick(updated)

        chick.bandedRingNumber?.let { ring ->
            database.birdDao().getBirdByRing(ring)?.let { bird ->
                database.birdDao().insertBird(
                    bird.copy(
                        status = com.example.core.common.BirdStatus.DECEASED,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun transferChickToCage(
        chickId: String,
        newCageCode: String,
        transferDate: Long,
        notes: String?
    ) {
        val chick = database.reproductionDao().getChickSync(chickId) ?: return
        val updated = chick.copy(
            cageCode = newCageCode,
            transferDate = transferDate,
            status = "TRANSFERRED",
            growthStage = "WEANED",
            notes = if (notes != null) "${chick.notes.orEmpty()} | $notes".trimStart(' ', '|') else chick.notes,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateChick(updated)

        chick.bandedRingNumber?.let { ring ->
            database.birdDao().getBirdByRing(ring)?.let { bird ->
                database.birdDao().insertBird(
                    bird.copy(
                        cageCode = newCageCode,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun registerChickAsOffspring(
        chickId: String,
        ringNumber: String,
        name: String?,
        gender: com.example.core.common.BirdGender,
        variety: com.example.core.common.BudgieVariety?,
        color: String?,
        notes: String?
    ): BirdEntity {
        val chick = database.reproductionDao().getChickSync(chickId)
            ?: throw IllegalArgumentException("Chick not found with id $chickId")

        val pair = database.pairDao().getPairById(chick.pairId).first()
        val father = pair?.maleRingNumber?.let { database.birdDao().getBirdByRing(it) }
        val mother = pair?.femaleRingNumber?.let { database.birdDao().getBirdByRing(it) }

        val generation = com.example.core.common.GenerationCalculator.calculateOffspringGeneration(
            fatherGeneration = father?.generation,
            motherGeneration = mother?.generation,
            hasKnownParents = father != null || mother != null
        )

        val offspringVariety = variety
            ?: father?.variety
            ?: mother?.variety
            ?: com.example.core.common.BudgieVariety.ENGLISH_SHOW

        val offspringColor = color ?: father?.color ?: "Green"
        val destinationCage = chick.cageCode ?: pair?.cageCode

        val bird = BirdEntity(
            ringNumber = ringNumber.trim(),
            name = name?.trim()?.ifBlank { "Chick #${chick.hatchOrder}" } ?: "Chick #${chick.hatchOrder}",
            gender = gender,
            variety = offspringVariety,
            color = offspringColor,
            birthDate = chick.hatchDate,
            generation = generation,
            cageCode = destinationCage,
            pairId = null,
            status = if (chick.status == "DECEASED") com.example.core.common.BirdStatus.DECEASED else com.example.core.common.BirdStatus.ACTIVE,
            fatherRing = pair?.maleRingNumber,
            motherRing = pair?.femaleRingNumber,
            notes = notes ?: chick.notes
        )
        database.birdDao().insertBird(bird)

        val updatedChick = chick.copy(
            bandedRingNumber = ringNumber.trim(),
            growthStage = if (chick.growthStage == "HATCHLING") "BANDED" else chick.growthStage,
            updatedAt = System.currentTimeMillis()
        )
        database.reproductionDao().updateChick(updatedChick)

        return bird
    }

    override suspend fun generateBreedingReminders(): Int {
        var count = 0
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        val eggs = database.reproductionDao().getAllEggs().first()
        for (egg in eggs.filter { !it.isHatched && !it.isDeleted && it.fertilityStatus != "INFERTILE" }) {
            val expectedHatch = egg.expectedHatchDate ?: (egg.layDate + 18 * dayMs)
            if (expectedHatch >= now - dayMs && expectedHatch <= now + 7 * dayMs) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Hatch Due: Egg #${egg.eggNumber} (Pair #${egg.pairId})",
                        description = "Egg #${egg.eggNumber} is due to hatch around ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(expectedHatch))}.",
                        dueDate = expectedHatch,
                        priority = com.example.core.common.ReminderPriority.HIGH
                    )
                )
                count++
            }

            if (egg.fertilityStatus == "UNCANDLED") {
                val candlingDue = egg.layDate + 6 * dayMs
                if (candlingDue >= now - dayMs && candlingDue <= now + 5 * dayMs) {
                    database.reminderDao().insertReminder(
                        ReminderEntity(
                            title = "Fertility Candling Due: Egg #${egg.eggNumber}",
                            description = "Candle egg #${egg.eggNumber} (Pair #${egg.pairId}) to verify embryo viability.",
                            dueDate = candlingDue,
                            priority = com.example.core.common.ReminderPriority.NORMAL
                        )
                    )
                    count++
                }
            }
        }

        val chicks = database.reproductionDao().getAllChicks().first()
        for (chick in chicks.filter { it.bandedRingNumber.isNullOrBlank() && it.status == "IN_NEST" && !it.isDeleted }) {
            val bandingDue = chick.hatchDate + 7 * dayMs
            if (bandingDue >= now - 2 * dayMs && bandingDue <= now + 4 * dayMs) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Banding Window Due: Chick #${chick.hatchOrder}",
                        description = "Chick #${chick.hatchOrder} (Pair #${chick.pairId}) is 6-8 days old and ready for closed leg banding.",
                        dueDate = bandingDue,
                        priority = com.example.core.common.ReminderPriority.URGENT
                    )
                )
                count++
            }
        }

        return count
    }

    // --- Genetics ---
    override val allGenetics: Flow<List<BirdGeneticsEntity>> = database.geneticsDao().getAllGenetics()
    override fun getGeneticsForBird(ringNumber: String): Flow<BirdGeneticsEntity?> = database.geneticsDao().getGeneticsForBird(ringNumber)
    override suspend fun saveGenetics(genetics: BirdGeneticsEntity) = database.geneticsDao().insertGenetics(genetics)

    // --- Pedigree ---
    override val allPedigrees: Flow<List<PedigreeRecordEntity>> = database.pedigreeDao().getAllPedigrees()
    override fun getPedigreeForBird(ringNumber: String): Flow<PedigreeRecordEntity?> = database.pedigreeDao().getPedigreeForBird(ringNumber)
    override suspend fun savePedigree(pedigree: PedigreeRecordEntity) = database.pedigreeDao().insertPedigree(pedigree)
    override fun getPedigreesWithParent(parentRing: String): Flow<List<PedigreeRecordEntity>> = database.pedigreeDao().getPedigreesWithParent(parentRing)

    // --- Health & Weights ---
    override val allHealthRecords: Flow<List<HealthRecordEntity>> = database.healthDao().getAllHealthRecords()
    override val activeHealthIssues: Flow<List<HealthRecordEntity>> = database.healthDao().getActiveHealthIssues()
    override fun getHealthRecordsForBird(ringNumber: String): Flow<List<HealthRecordEntity>> = database.healthDao().getHealthRecordsForBird(ringNumber)
    override suspend fun saveHealthRecord(record: HealthRecordEntity) {
        database.healthDao().insertHealthRecord(record)
        // Auto-schedule medication and reminder if medication details provided
        if (!record.medicationName.isNullOrBlank()) {
            val med = MedicationEntity(
                healthRecordId = record.id,
                birdRingNumber = record.birdRingNumber,
                medicationName = record.medicationName,
                dosage = record.dosage ?: "As prescribed",
                frequency = record.frequency ?: "ONCE_DAILY",
                startDate = record.startDate,
                endDate = record.endDate ?: (record.startDate + (record.treatmentDurationDays.coerceAtLeast(1) * 86400000L)),
                notes = record.recordedProblem
            )
            scheduleMedicationWithReminders(med)
        }
    }
    override suspend fun updateHealthRecord(record: HealthRecordEntity) = database.healthDao().updateHealthRecord(record)
    override suspend fun deleteHealthRecord(record: HealthRecordEntity) = database.healthDao().deleteHealthRecord(record)

    override val allMedications: Flow<List<MedicationEntity>> = database.healthDao().getAllMedications()
    override val activeMedications: Flow<List<MedicationEntity>> = database.healthDao().getActiveMedications()
    override fun getMedicationsForBird(ringNumber: String): Flow<List<MedicationEntity>> = database.healthDao().getMedicationsForBird(ringNumber)
    override suspend fun saveMedication(medication: MedicationEntity) = database.healthDao().insertMedication(medication)

    override suspend fun scheduleMedicationWithReminders(medication: MedicationEntity): Long {
        database.healthDao().insertMedication(medication)
        val ring = medication.birdRingNumber ?: "Flock"
        val reminder = ReminderEntity(
            title = "Medication: ${medication.medicationName} ($ring)",
            description = "Dose: ${medication.dosage} | Frequency: ${medication.frequency} | Route: ${medication.administrationRoute}",
            dueDate = medication.startDate,
            priority = com.example.core.common.ReminderPriority.HIGH,
            relatedRingNumber = medication.birdRingNumber
        )
        return database.reminderDao().insertReminder(reminder)
    }

    override suspend fun deleteMedication(medication: MedicationEntity) = database.healthDao().deleteMedication(medication)

    override suspend fun generateMedicationReminders(): Int {
        var count = 0
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val activeMeds = database.healthDao().getActiveMedications().first()
        for (med in activeMeds) {
            if (med.endDate >= now - dayMs) {
                val targetRing = med.birdRingNumber ?: "Flock"
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Active Treatment: ${med.medicationName} ($targetRing)",
                        description = "Administer ${med.dosage} (${med.frequency}). Active through ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(med.endDate))}.",
                        dueDate = now,
                        priority = com.example.core.common.ReminderPriority.HIGH,
                        relatedRingNumber = med.birdRingNumber
                    )
                )
                count++
            }
        }
        return count
    }

    override val allWeightRecords: Flow<List<WeightRecordEntity>> = database.healthDao().getAllWeightRecords()
    override fun getWeightHistoryForBird(ringNumber: String): Flow<List<WeightRecordEntity>> = database.healthDao().getWeightHistoryForBird(ringNumber)
    override suspend fun saveWeight(weight: WeightRecordEntity) = database.healthDao().insertWeight(weight)
    override suspend fun deleteWeight(weight: WeightRecordEntity) = database.healthDao().deleteWeight(weight)

    // --- Nutrition & Inventory ---
    override val allNutritionPlans: Flow<List<NutritionPlanEntity>> = database.nutritionDao().getAllNutritionPlans()
    override suspend fun saveNutritionPlan(plan: NutritionPlanEntity) = database.nutritionDao().insertNutritionPlan(plan)
    override suspend fun deleteNutritionPlan(plan: NutritionPlanEntity) = database.nutritionDao().deleteNutritionPlan(plan)

    override val allNutritionRecords: Flow<List<NutritionRecordEntity>> = database.nutritionDao().getAllNutritionRecords()
    override fun getNutritionRecordsForBird(ringNumber: String): Flow<List<NutritionRecordEntity>> = database.nutritionDao().getNutritionRecordsForBird(ringNumber)
    override fun getNutritionRecordsForCage(cageCode: String): Flow<List<NutritionRecordEntity>> = database.nutritionDao().getNutritionRecordsForCage(cageCode)
    override suspend fun saveNutritionRecord(record: NutritionRecordEntity) = database.nutritionDao().insertNutritionRecord(record)
    override suspend fun deleteNutritionRecord(record: NutritionRecordEntity) = database.nutritionDao().deleteNutritionRecord(record)

    // --- Inventory & Stock Management ---
    override val allInventoryItems: Flow<List<InventoryItemEntity>> = database.inventoryDao().getAllInventory()
    override val lowStockItems: Flow<List<InventoryItemEntity>> = database.inventoryDao().getLowStockItems()
    override fun getInventoryByCategory(category: String): Flow<List<InventoryItemEntity>> =
        database.inventoryDao().getItemsByCategory(category)
    override suspend fun saveInventoryItem(item: InventoryItemEntity) = database.inventoryDao().insertItem(item)
    override suspend fun deleteInventoryItem(item: InventoryItemEntity) = database.inventoryDao().deleteItem(item)

    override val allInventoryTransactions: Flow<List<InventoryTransactionEntity>> = database.inventoryDao().getAllTransactions()
    override fun getTransactionsForItem(itemId: String): Flow<List<InventoryTransactionEntity>> =
        database.inventoryDao().getTransactionsForItem(itemId)

    override suspend fun recordStockIn(
        itemId: String,
        quantity: Double,
        unitPrice: Double,
        notes: String?,
        createExpense: Boolean
    ) {
        val item = database.inventoryDao().getItemById(itemId) ?: return
        val (updatedItem, tx) = InventoryAnalyticsHelper.buildStockIn(item, quantity, unitPrice, notes)
        database.inventoryDao().updateItem(updatedItem)
        database.inventoryDao().insertTransaction(tx)

        if (createExpense && tx.totalCost > 0.0) {
            val expenseCat = when (item.category) {
                "FOOD" -> ExpenseCategory.FOOD
                "SUPPLEMENTS" -> ExpenseCategory.SUPPLEMENTS
                "MEDICINE" -> ExpenseCategory.MEDICINE
                "EQUIPMENT" -> ExpenseCategory.EQUIPMENT
                else -> ExpenseCategory.OTHER
            }
            database.financeDao().insertExpense(
                ExpenseEntity(
                    title = "Restock: ${item.name} (${quantity} ${item.unit})",
                    category = expenseCat,
                    amount = tx.totalCost,
                    inventoryItemId = item.id,
                    notes = notes
                )
            )
        }
    }

    override suspend fun recordStockOut(
        itemId: String,
        quantity: Double,
        reason: String,
        notes: String?
    ) {
        val item = database.inventoryDao().getItemById(itemId) ?: return
        val (updatedItem, tx) = InventoryAnalyticsHelper.buildStockOut(item, quantity, reason, notes)
        database.inventoryDao().updateItem(updatedItem)
        database.inventoryDao().insertTransaction(tx)
    }

    override suspend fun recordConsumption(
        itemId: String,
        quantity: Double,
        referenceType: String?,
        referenceId: String?,
        notes: String?
    ) {
        val item = database.inventoryDao().getItemById(itemId) ?: return
        val (updatedItem, tx) = InventoryAnalyticsHelper.buildConsumption(item, quantity, referenceType, referenceId, notes)
        database.inventoryDao().updateItem(updatedItem)
        database.inventoryDao().insertTransaction(tx)
    }

    // --- Finance ---
    override val allExpenses: Flow<List<ExpenseEntity>> = database.financeDao().getAllExpenses()
    override val allIncome: Flow<List<IncomeEntity>> = database.financeDao().getAllIncome()
    override val totalExpenses: Flow<Double> = database.financeDao().getTotalExpenses()
    override val totalIncome: Flow<Double> = database.financeDao().getTotalIncome()
    override fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> =
        database.financeDao().getExpensesByCategory(category)
    override fun getExpensesForPair(pairId: String): Flow<List<ExpenseEntity>> =
        database.financeDao().getExpensesForPair(pairId)
    override fun getIncomeByCategory(category: String): Flow<List<IncomeEntity>> =
        database.financeDao().getIncomeByCategory(category)
    override fun getIncomeForBird(ringNumber: String): Flow<List<IncomeEntity>> =
        database.financeDao().getIncomeForBird(ringNumber)
    override suspend fun saveExpense(expense: ExpenseEntity) = database.financeDao().insertExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = database.financeDao().deleteExpense(expense)
    override suspend fun saveIncome(income: IncomeEntity) = database.financeDao().insertIncome(income)
    override suspend fun deleteIncome(income: IncomeEntity) = database.financeDao().deleteIncome(income)

    // --- Competitions, Judges, Criteria, Scores ---
    override val allCompetitions: Flow<List<CompetitionEntity>> = database.competitionDao().getAllCompetitions()
    override fun getCompetitionById(competitionId: String): Flow<CompetitionEntity?> =
        database.competitionDao().getCompetitionById(competitionId)
    override suspend fun saveCompetition(competition: CompetitionEntity) = database.competitionDao().insertCompetition(competition)
    override suspend fun deleteCompetition(competitionId: String) = database.competitionDao().softDeleteCompetition(competitionId)

    override val allJudges: Flow<List<JudgeEntity>> = database.competitionDao().getAllJudges()
    override suspend fun saveJudge(judge: JudgeEntity) = database.competitionDao().insertJudge(judge)
    override suspend fun deleteJudge(judge: JudgeEntity) = database.competitionDao().deleteJudge(judge)

    override fun getCriteriaForCompetition(competitionId: String): Flow<List<CompetitionCriterionEntity>> =
        database.competitionDao().getCriteriaForCompetition(competitionId)
    override suspend fun saveCriteriaForCompetition(competitionId: String, criteria: List<CompetitionCriterionEntity>) {
        database.competitionDao().deleteCriteriaForCompetition(competitionId)
        database.competitionDao().insertCriteria(criteria)
    }

    override fun getScoresForBird(ringNumber: String): Flow<List<CompetitionScoreEntity>> =
        database.competitionDao().getScoresForBird(ringNumber)
    override fun getScoresForCompetition(competitionId: String): Flow<List<CompetitionScoreEntity>> =
        database.competitionDao().getScoresForCompetition(competitionId)
    override fun getScoresForClass(competitionId: String, showClass: String): Flow<List<CompetitionScoreEntity>> =
        database.competitionDao().getScoresForClass(competitionId, showClass)
    override suspend fun saveCompetitionScore(score: CompetitionScoreEntity) = database.competitionDao().insertScore(score)
    override suspend fun deleteCompetitionScore(scoreId: String) = database.competitionDao().deleteScoreById(scoreId)

    // --- Reminders ---
    override val allReminders: Flow<List<ReminderEntity>> = database.reminderDao().getAllReminders()
    override val pendingReminders: Flow<List<ReminderEntity>> = database.reminderDao().getPendingReminders()
    override val completedReminders: Flow<List<ReminderEntity>> = database.reminderDao().getCompletedReminders()
    override val pendingReminderCount: Flow<Int> = database.reminderDao().getPendingReminderCount()
    override fun getRemindersByType(type: com.example.core.common.ReminderType): Flow<List<ReminderEntity>> =
        database.reminderDao().getRemindersByType(type)
    override suspend fun saveReminder(reminder: ReminderEntity): Long = database.reminderDao().insertReminder(reminder)
    override suspend fun updateReminder(reminder: ReminderEntity) = database.reminderDao().updateReminder(reminder)
    override suspend fun deleteReminder(reminder: ReminderEntity) = database.reminderDao().deleteReminder(reminder)
    override suspend fun deleteReminderById(id: Long) = database.reminderDao().deleteReminderById(id)
    override suspend fun clearCompletedReminders() = database.reminderDao().clearCompletedReminders()

    override suspend fun syncSmartReminders(): Int {
        var count = 0
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        // 1. Egg Laying & Candling Reminders
        val eggs = database.reproductionDao().getAllEggs().first()
        for (egg in eggs.filter { !it.isHatched && !it.isDeleted && it.fertilityStatus != "INFERTILE" }) {
            // Candling
            if (egg.fertilityStatus == "UNCANDLED" || egg.fertilityStatus == "PENDING") {
                val candlingDue = egg.layDate + 6 * dayMs
                val dedupKey = "EGG_CANDLE_${egg.id}"
                if (database.reminderDao().findActiveByDedupKey(dedupKey) == null) {
                    database.reminderDao().insertReminder(
                        ReminderEntity(
                            title = "Fertility Candling: Egg #${egg.eggNumber}",
                            description = "Candle egg #${egg.eggNumber} (Pair #${egg.pairId}) to inspect embryo development.",
                            dueDate = candlingDue,
                            priority = com.example.core.common.ReminderPriority.NORMAL,
                            type = com.example.core.common.ReminderType.EGG_LAYING,
                            eggId = egg.id,
                            pairId = egg.pairId,
                            dedupKey = dedupKey
                        )
                    )
                    count++
                }
            }

            // Expected Hatch
            val expectedHatch = egg.expectedHatchDate ?: (egg.layDate + 18 * dayMs)
            val hatchDedupKey = "EGG_HATCH_${egg.id}"
            if (database.reminderDao().findActiveByDedupKey(hatchDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Expected Hatch: Egg #${egg.eggNumber} (Pair #${egg.pairId})",
                        description = "Egg #${egg.eggNumber} is due to hatch around ${sdf.format(java.util.Date(expectedHatch))}.",
                        dueDate = expectedHatch,
                        priority = com.example.core.common.ReminderPriority.HIGH,
                        type = com.example.core.common.ReminderType.EXPECTED_HATCH,
                        eggId = egg.id,
                        pairId = egg.pairId,
                        dedupKey = hatchDedupKey
                    )
                )
                count++
            }
        }

        // 2. Mating & Chick Banding Reminders
        val pairs = database.pairDao().getActivePairs().first()
        for (pair in pairs) {
            val matingDedupKey = "PAIR_MATING_${pair.id}"
            if (now - pair.pairingDate < 14 * dayMs && database.reminderDao().findActiveByDedupKey(matingDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Mating & Nest Acceptance: Pair #${pair.id}",
                        description = "Inspect pair in Cage ${pair.cageCode} (${pair.maleRingNumber} x ${pair.femaleRingNumber}) for nest box interest.",
                        dueDate = pair.pairingDate + 7 * dayMs,
                        priority = com.example.core.common.ReminderPriority.NORMAL,
                        type = com.example.core.common.ReminderType.MATING,
                        pairId = pair.id,
                        cageCode = pair.cageCode,
                        dedupKey = matingDedupKey
                    )
                )
                count++
            }
        }

        val chicks = database.reproductionDao().getAllChicks().first()
        for (chick in chicks.filter { it.bandedRingNumber.isNullOrBlank() && it.status == "IN_NEST" && !it.isDeleted }) {
            val bandingDue = chick.hatchDate + 7 * dayMs
            val chickDedupKey = "CHICK_BAND_${chick.id}"
            if (database.reminderDao().findActiveByDedupKey(chickDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Closed Leg Banding: Chick #${chick.hatchOrder}",
                        description = "Chick #${chick.hatchOrder} (Pair #${chick.pairId}) is in the optimal 6-8 day closed banding window.",
                        dueDate = bandingDue,
                        priority = com.example.core.common.ReminderPriority.URGENT,
                        type = com.example.core.common.ReminderType.WEIGHT_MEASUREMENT,
                        pairId = chick.pairId,
                        dedupKey = chickDedupKey
                    )
                )
                count++
            }
        }

        // 3. Active Medications
        val medications = database.healthDao().getAllMedications().first()
        for (med in medications.filter { !it.isCompleted }) {
            val medDedupKey = "MED_ACTIVE_${med.id}"
            if (database.reminderDao().findActiveByDedupKey(medDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Medication Dose: ${med.medicationName}",
                        description = "Administer ${med.dosage} for ${med.birdRingNumber ?: "Aviary"} (${med.administrationRoute}).",
                        dueDate = maxOf(now, med.startDate),
                        priority = com.example.core.common.ReminderPriority.HIGH,
                        type = com.example.core.common.ReminderType.MEDICATION,
                        relatedRingNumber = med.birdRingNumber,
                        dedupKey = medDedupKey
                    )
                )
                count++
            }
        }

        // 4. Low Inventory Stock Alerts
        val inventoryItems = database.inventoryDao().getAllInventory().first()
        for (item in inventoryItems.filter { it.currentStock <= it.minStockThreshold }) {
            val invDedupKey = "INV_LOW_${item.id}"
            if (database.reminderDao().findActiveByDedupKey(invDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Low Inventory: ${item.name}",
                        description = "Current stock is ${item.currentStock} ${item.unit} (Minimum threshold: ${item.minStockThreshold} ${item.unit}). Reorder recommended.",
                        dueDate = now,
                        priority = com.example.core.common.ReminderPriority.NORMAL,
                        type = com.example.core.common.ReminderType.INVENTORY,
                        inventoryItemId = item.id,
                        dedupKey = invDedupKey
                    )
                )
                count++
            }
        }

        // 5. Cage Sanitation & Cleaning
        val cages = database.cageDao().getAllCages().first()
        for (cage in cages.filter { cageItem -> !cageItem.isClean || ((cageItem.lastCleanedDate ?: 0L) < (now - 7 * dayMs)) }) {
            val cleanDedupKey = "CAGE_CLEAN_${cage.code}"
            if (database.reminderDao().findActiveByDedupKey(cleanDedupKey) == null) {
                database.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Sanitation & Cleaning: Cage ${cage.code}",
                        description = "Clean tray, perches, and replace liners in Cage ${cage.code} (${cage.type}).",
                        dueDate = now + 4 * 3600000L,
                        priority = com.example.core.common.ReminderPriority.NORMAL,
                        type = com.example.core.common.ReminderType.CLEANING,
                        cageCode = cage.code,
                        dedupKey = cleanDedupKey
                    )
                )
                count++
            }
        }

        // 6. Routine Daily Feeding & Supplements
        val dailyFeedingKey = "DAILY_FEED_${sdf.format(java.util.Date(now))}"
        if (database.reminderDao().findActiveByDedupKey(dailyFeedingKey) == null) {
            database.reminderDao().insertReminder(
                ReminderEntity(
                    title = "Daily Nutrition & Fresh Water",
                    description = "Replenish seed blend, soft eggfood, clean drinker bottles, and provide fresh green veggies.",
                    dueDate = now + 2 * 3600000L,
                    priority = com.example.core.common.ReminderPriority.NORMAL,
                    type = com.example.core.common.ReminderType.FEEDING,
                    dedupKey = dailyFeedingKey
                )
            )
            count++
        }

        val weeklySuppKey = "WEEKLY_SUPP_${System.currentTimeMillis() / (7 * dayMs)}"
        if (database.reminderDao().findActiveByDedupKey(weeklySuppKey) == null) {
            database.reminderDao().insertReminder(
                ReminderEntity(
                    title = "Weekly Aviary Supplement Routine",
                    description = "Supply liquid calcium + D3 in drinking water and check cuttlebone / iodine block availability.",
                    dueDate = now + 6 * 3600000L,
                    priority = com.example.core.common.ReminderPriority.LOW,
                    type = com.example.core.common.ReminderType.SUPPLEMENTS,
                    dedupKey = weeklySuppKey
                )
            )
            count++
        }

        return count
    }

    // --- Media Documents & Users & Audit ---
    override fun getMediaForEntity(type: String, id: String): Flow<List<MediaDocumentEntity>> =
        database.mediaDocumentDao().getMediaForEntity(type, id)
    override suspend fun saveMedia(media: MediaDocumentEntity) = database.mediaDocumentDao().insertMedia(media)
    override val activeUsers: Flow<List<UserEntity>> = database.userDao().getActiveUsers()
    override suspend fun saveUser(user: UserEntity) = database.userDao().insertUser(user)
    override val recentAuditLogs: Flow<List<AuditLogEntity>> = database.auditLogDao().getRecentLogs(10)
    override fun getLogsForEntity(type: String, id: String): Flow<List<AuditLogEntity>> =
        database.auditLogDao().getLogsForEntity(type, id)
    override suspend fun recordAuditLog(
        actionType: String,
        entityType: String,
        entityId: String,
        summary: String
    ) {
        database.auditLogDao().insertLog(
            AuditLogEntity(
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                summary = summary
            )
        )
    }
}
