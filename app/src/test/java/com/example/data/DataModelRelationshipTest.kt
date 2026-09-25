package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataModelRelationshipTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCoreRelationships_Bird_Cage_Pair_Eggs_Chick_Lineage() = runBlocking {
        // 1. Create Cage (Bird -> Cage)
        val cage = CageEntity(
            code = "CAGE-A1",
            type = CageType.BREEDING_BOX,
            capacity = 2
        )
        database.cageDao().insertCage(cage)

        // 2. Create Sire (Father) and Dam (Mother)
        val sire = BirdEntity(
            ringNumber = "IR-2024-001",
            name = "Blue Thunder",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            mutation = "Opaline",
            color = "Cobalt",
            cageCode = "CAGE-A1"
        )
        val dam = BirdEntity(
            ringNumber = "IR-2024-002",
            name = "Golden Queen",
            gender = BirdGender.FEMALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            mutation = "Spangle",
            color = "Yellowface Skyblue",
            cageCode = "CAGE-A1"
        )
        database.birdDao().insertBird(sire)
        database.birdDao().insertBird(dam)

        // 3. Create Breeding Pair (Bird -> Pair)
        val pair = PairEntity(
            id = 101L,
            maleRingNumber = sire.ringNumber,
            femaleRingNumber = dam.ringNumber,
            cageCode = cage.code
        )
        val pairId = database.pairDao().insertPair(pair)
        assertEquals(101L, pairId)

        // Update birds to link to pair
        database.birdDao().insertBird(sire.copy(pairId = pairId))
        database.birdDao().insertBird(dam.copy(pairId = pairId))

        // 4. Create Nest and Eggs (Pair -> Eggs)
        val nest = NestEntity(
            boxNumber = "BOX-01",
            cageCode = cage.code,
            pairId = pairId
        )
        database.reproductionDao().insertNest(nest)

        val egg1 = EggEntity(
            pairId = pairId,
            nestId = nest.id,
            eggNumber = 1,
            fertilityStatus = "FERTILE"
        )
        database.reproductionDao().insertEgg(egg1)

        val pairEggs = database.reproductionDao().getEggsForPair(pairId).first()
        assertEquals(1, pairEggs.size)
        assertEquals(egg1.id, pairEggs[0].id)

        // 5. Egg -> Chick
        val chick1 = ChickEntity(
            eggId = egg1.id,
            pairId = pairId,
            hatchOrder = 1,
            bandedRingNumber = "IR-2024-050"
        )
        database.reproductionDao().insertChick(chick1)

        val eggWithChick = database.reproductionDao().getEggWithChick(egg1.id).first()
        assertNotNull(eggWithChick)
        assertNotNull(eggWithChick?.chick)
        assertEquals(chick1.id, eggWithChick?.chick?.id)

        // 6. Chick grows up into Bird with Parents (Bird -> Parents & Bird -> Children)
        val childBird = BirdEntity(
            ringNumber = "IR-2024-050",
            name = "Thunder Junior",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            fatherRing = sire.ringNumber,
            motherRing = dam.ringNumber,
            cageCode = cage.code
        )
        database.birdDao().insertBird(childBird)

        // Verify Bird -> Parents
        val parentsRelation = database.birdDao().getBirdWithParents(childBird.ringNumber).first()
        assertNotNull(parentsRelation)
        assertEquals(sire.ringNumber, parentsRelation?.father?.ringNumber)
        assertEquals(dam.ringNumber, parentsRelation?.mother?.ringNumber)

        // Verify Bird -> Children
        val sireChildrenRelation = database.birdDao().getBirdWithChildren(sire.ringNumber).first()
        assertNotNull(sireChildrenRelation)
        assertEquals(1, sireChildrenRelation?.fatheredChildren?.size)
        assertEquals(childBird.ringNumber, sireChildrenRelation?.fatheredChildren?.get(0)?.ringNumber)

        // 7. Bird -> Genetics
        val genetics = BirdGeneticsEntity(
            birdRingNumber = childBird.ringNumber,
            visualMutations = "Opaline Spangle",
            splitMutations = "Split Cinnamon",
            baseSeries = "BLUE",
            darkFactors = 1
        )
        database.geneticsDao().insertGenetics(genetics)

        val retrievedGenetics = database.geneticsDao().getGeneticsForBird(childBird.ringNumber).first()
        assertNotNull(retrievedGenetics)
        assertEquals("Opaline Spangle", retrievedGenetics?.visualMutations)

        // 8. Bird -> Pedigree
        val pedigree = PedigreeRecordEntity(
            birdRingNumber = childBird.ringNumber,
            sireRing = sire.ringNumber,
            damRing = dam.ringNumber,
            breederName = "Champion Aviary",
            inbreedingCoefficient = 0.0
        )
        database.pedigreeDao().insertPedigree(pedigree)

        val retrievedPedigree = database.pedigreeDao().getPedigreeForBird(childBird.ringNumber).first()
        assertNotNull(retrievedPedigree)
        assertEquals(sire.ringNumber, retrievedPedigree?.sireRing)

        // 9. Bird -> Health
        val health = HealthRecordEntity(
            birdRingNumber = childBird.ringNumber,
            recordType = "ROUTINE_CHECK",
            symptoms = "None, perfect feather condition",
            diagnosis = "Excellent health"
        )
        database.healthDao().insertHealthRecord(health)

        val birdHealth = database.healthDao().getHealthRecordsForBird(childBird.ringNumber).first()
        assertEquals(1, birdHealth.size)

        // 10. Bird -> Weight History
        val weight = WeightRecordEntity(
            birdRingNumber = childBird.ringNumber,
            weightGrams = 52.4,
            conditionScore = "OPTIMAL"
        )
        database.healthDao().insertWeight(weight)

        val weights = database.healthDao().getWeightHistoryForBird(childBird.ringNumber).first()
        assertEquals(1, weights.size)
        assertEquals(52.4, weights[0].weightGrams, 0.01)

        // 11. Bird -> Competition History
        val competition = CompetitionEntity(
            title = "National Budgerigar Show 2024",
            location = "Main Hall",
            organizingClub = "Iranian Budgerigar Society",
            showStandard = "WBO"
        )
        database.competitionDao().insertCompetition(competition)

        val score = CompetitionScoreEntity(
            competitionId = competition.id,
            birdRingNumber = childBird.ringNumber,
            showClass = "Young Cock Opaline Spangle",
            headScore = 28.0,
            maskSpotsScore = 14.5,
            stanceScore = 14.0,
            featherConditionScore = 19.0,
            totalScore = 93.5,
            awardTitle = "FIRST_IN_CLASS"
        )
        database.competitionDao().insertScore(score)

        val birdScores = database.competitionDao().getScoresForBird(childBird.ringNumber).first()
        assertEquals(1, birdScores.size)
        assertEquals(93.5, birdScores[0].totalScore, 0.01)

        // 12. Complete Relation Query: BirdWithDetails
        val birdDetails = database.birdDao().getBirdWithDetails(childBird.ringNumber).first()
        assertNotNull(birdDetails)
        assertEquals(cage.code, birdDetails?.cage?.code)
        assertEquals(genetics.visualMutations, birdDetails?.genetics?.visualMutations)
        assertEquals(pedigree.sireRing, birdDetails?.pedigree?.sireRing)
        assertEquals(1, birdDetails?.healthRecords?.size)
        assertEquals(1, birdDetails?.weightHistory?.size)
        assertEquals(1, birdDetails?.competitionScores?.size)
    }

    @Test
    fun testCompleteReproductionLineage_Parent_Pair_Egg_Chick_Offspring_Generation() = runBlocking {
        val repo = com.example.data.repository.AviaryRepositoryImpl(database)

        // 1. Setup Parent Birds with generation records in DB
        val sire = BirdEntity(
            ringNumber = "SIRE-2025-01",
            name = "Champion Sire",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            generation = "F1"
        )
        val dam = BirdEntity(
            ringNumber = "DAM-2025-02",
            name = "Prize Dam",
            gender = BirdGender.FEMALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            generation = "F1"
        )
        database.birdDao().insertBird(sire)
        database.birdDao().insertBird(dam)

        // 2. Setup Breeding Cage and Nest Box
        val breedingCage = CageEntity(code = "BOX-PAIR-1", type = CageType.BREEDING_BOX)
        database.cageDao().insertCage(breedingCage)

        val nest = NestEntity(
            boxNumber = "NEST-01",
            cageCode = breedingCage.code,
            nestMaterial = "Untreated Pine Shavings",
            isClean = true
        )
        repo.saveNest(nest)

        // 3. Form Breeding Pair with Mating Date
        val matingTimestamp = System.currentTimeMillis() - (18L * 86400000L) // 18 days ago
        val pair = PairEntity(
            id = 501L,
            maleRingNumber = sire.ringNumber,
            femaleRingNumber = dam.ringNumber,
            cageCode = breedingCage.code,
            nestId = nest.id,
            matingDate = matingTimestamp,
            status = "BREEDING"
        )
        val pairId = repo.savePair(pair)
        assertEquals(501L, pairId)

        // 4. Log Clutch with Mating Date
        val clutch = com.example.data.database.entity.ClutchEntity(
            pairId = pairId,
            clutchNumber = 1,
            matingDate = matingTimestamp,
            startDate = matingTimestamp + (8L * 86400000L),
            notes = "First clutch of the season"
        )
        val clutchId = repo.saveClutch(clutch)
        assertTrue(clutchId > 0)

        // 5. Log Individual Egg with Lay Date and Expected Hatch Date (Day 18)
        val layDate = matingTimestamp + (10L * 86400000L)
        val expectedHatch = layDate + (18L * 86400000L)
        val egg = EggEntity(
            pairId = pairId,
            clutchId = clutchId,
            nestId = nest.id,
            eggNumber = 1,
            layDate = layDate,
            expectedHatchDate = expectedHatch,
            fertilityStatus = "UNCANDLED"
        )
        repo.saveEgg(egg)

        // 6. Candle Egg -> updates fertility status
        repo.updateEggFertility(egg.id, "FERTILE", "Clear spiderweb veins seen at day 6")
        val candledEgg = database.reproductionDao().getEggById(egg.id).first()
        assertNotNull(candledEgg)
        assertEquals("FERTILE", candledEgg?.fertilityStatus)
        assertEquals("Clear spiderweb veins seen at day 6", candledEgg?.notes)

        // 7. Hatch Egg -> automatically updates egg result, actual hatch date and creates Chick record
        val actualHatchTimestamp = System.currentTimeMillis()
        val createdChick = repo.hatchEgg(
            eggId = egg.id,
            hatchDate = actualHatchTimestamp,
            initialWeightGrams = 1.6,
            ringNumber = "OFFSPRING-2026-001",
            notes = "Vigorous hatch, pink down"
        )
        assertNotNull(createdChick)

        // Verify Egg is updated
        val hatchedEgg = database.reproductionDao().getEggById(egg.id).first()
        assertNotNull(hatchedEgg)
        assertTrue(hatchedEgg?.isHatched == true)
        assertEquals(actualHatchTimestamp, hatchedEgg?.actualHatchDate)
        assertEquals("HATCHED", hatchedEgg?.eggResult)

        // Verify Chick is created and connected
        val retrievedChick = database.reproductionDao().getChickById(createdChick.id).first()
        assertNotNull(retrievedChick)
        assertEquals(egg.id, retrievedChick?.eggId)
        assertEquals(pairId, retrievedChick?.pairId)
        assertEquals(1, retrievedChick?.hatchOrder)
        assertEquals(1.6, retrievedChick?.weightGrams ?: 0.0, 0.01)
        assertEquals("OFFSPRING-2026-001", retrievedChick?.bandedRingNumber)

        // 8. Track Chick Weight & Growth
        repo.recordChickWeight(createdChick.id, 18.5, "OPTIMAL", "Feather pin tracks emerging")
        val weightedChick = database.reproductionDao().getChickById(createdChick.id).first()
        assertEquals(18.5, weightedChick?.weightGrams ?: 0.0, 0.01)

        // 9. Chick Cage Transfer upon Weaning
        val nurseryCage = CageEntity(code = "NURSERY-1", type = CageType.FLIGHT_CAGE)
        database.cageDao().insertCage(nurseryCage)
        repo.transferChickToCage(createdChick.id, nurseryCage.code, System.currentTimeMillis(), "Weaned and eating seeds")
        val transferredChick = database.reproductionDao().getChickById(createdChick.id).first()
        assertEquals(nurseryCage.code, transferredChick?.cageCode)
        assertEquals("TRANSFERRED", transferredChick?.status)

        // 10. Register Chick as Offspring Bird
        // Automatically connects Parent -> Pair -> Egg -> Chick -> Offspring -> Generation!
        val registeredBird = repo.registerChickAsOffspring(
            chickId = createdChick.id,
            ringNumber = "OFFSPRING-2026-001",
            name = "Sky Prince",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            color = "Cobalt Blue",
            notes = "Offspring from Pair 501"
        )
        assertNotNull(registeredBird)
        assertEquals(sire.ringNumber, registeredBird?.fatherRing)
        assertEquals(dam.ringNumber, registeredBird?.motherRing)
        // Generation must be dynamically calculated: Sire (F1) + Dam (F1) => F2!
        assertEquals("F2", registeredBird?.generation)

        // Verify Bird is in DB with all parental relationships
        val birdWithParents = database.birdDao().getBirdWithParents("OFFSPRING-2026-001").first()
        assertNotNull(birdWithParents)
        assertEquals(sire.ringNumber, birdWithParents?.father?.ringNumber)
        assertEquals(dam.ringNumber, birdWithParents?.mother?.ringNumber)
        assertEquals("F1", birdWithParents?.father?.generation)
        assertEquals("F1", birdWithParents?.mother?.generation)

        // 11. Verify Upcoming Hatch Reminders
        val egg2 = EggEntity(
            pairId = pairId,
            clutchId = clutchId,
            nestId = nest.id,
            eggNumber = 2,
            layDate = System.currentTimeMillis() - (16L * 86400000L),
            expectedHatchDate = System.currentTimeMillis() + (2L * 86400000L), // Due in 2 days
            fertilityStatus = "FERTILE"
        )
        repo.saveEgg(egg2)

        val upcomingEggs = repo.upcomingHatchEggs.first()
        assertTrue(upcomingEggs.any { it.eggNumber == 2 })

        // Sync to Reminders
        val reminderCount = repo.generateBreedingReminders()
        assertTrue(reminderCount >= 1)
        val allReminders = database.reminderDao().getPendingReminders().first()
        assertTrue(allReminders.any { it.title.contains("Egg #2") || it.description?.contains("Egg #2") == true })
    }
}
