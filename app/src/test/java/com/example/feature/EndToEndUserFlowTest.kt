package com.example.feature

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepositoryImpl
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EndToEndUserFlowTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AviaryRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getInstance(context)
        repository = AviaryRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCompleteBreedingCycleAndFacilityWorkflow() = runBlocking {
        // Step 1: Create Cage
        val cageCode = "CAGE-AUDIT-01"
        val cage = CageEntity(
            code = cageCode,
            type = CageType.BREEDING_BOX,
            capacity = 2,
            notes = "Test Breeding Box"
        )
        repository.saveCage(cage)
        val retrievedCage = repository.getCageByCode(cageCode).first()
        assertNotNull(retrievedCage)
        assertEquals(cageCode, retrievedCage!!.code)

        // Step 2: Create Founder Sire (Male) and Dam (Female) Birds
        val sireRing = "IR-2026-M01"
        val damRing = "IR-2026-F01"
        val sire = BirdEntity(
            ringNumber = sireRing,
            name = "Champion Sky",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            birthDate = System.currentTimeMillis() - 365L * 24 * 3600 * 1000,
            status = BirdStatus.BREEDING,
            cageCode = cageCode,
            notes = "Blue series opaline cock"
        )
        val dam = BirdEntity(
            ringNumber = damRing,
            name = "Emerald Queen",
            gender = BirdGender.FEMALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            birthDate = System.currentTimeMillis() - 365L * 24 * 3600 * 1000,
            status = BirdStatus.BREEDING,
            cageCode = cageCode,
            notes = "Green series spangle hen"
        )
        repository.saveBird(sire)
        repository.saveBird(dam)

        val retrievedSire = repository.getBirdByRing(sireRing)
        assertNotNull(retrievedSire)
        assertEquals("Champion Sky", retrievedSire!!.name)

        // Step 3: Create Pair and assign to cage
        val pair = PairEntity(
            maleRingNumber = sireRing,
            femaleRingNumber = damRing,
            cageCode = cageCode,
            pairingDate = System.currentTimeMillis(),
            status = "ACTIVE",
            notes = "Tested pairing"
        )
        val pairId = repository.savePair(pair)
        assertTrue(pairId > 0)

        // Step 4: Record Clutch / Breeding Cycle
        val clutch = ClutchEntity(
            pairId = pairId,
            clutchNumber = 1,
            startDate = System.currentTimeMillis(),
            notes = "First spring clutch"
        )
        val clutchId = repository.saveClutch(clutch)
        assertTrue(clutchId > 0)

        // Step 5: Add Eggs
        val eggId = UUID.randomUUID().toString()
        val egg1 = EggEntity(
            id = eggId,
            pairId = pairId,
            clutchId = clutchId,
            eggNumber = 1,
            layDate = System.currentTimeMillis(),
            fertilityStatus = "FERTILE",
            candlingDate = System.currentTimeMillis(),
            notes = "Veins visible"
        )
        repository.saveEgg(egg1)
        val allEggs = repository.allEggs.first()
        assertTrue(allEggs.any { it.id == eggId })

        // Step 6: Hatch Chick
        val chickId = UUID.randomUUID().toString()
        val chickRing = "IR-2026-J01"
        val chick = ChickEntity(
            id = chickId,
            eggId = eggId,
            pairId = pairId,
            hatchDate = System.currentTimeMillis(),
            hatchOrder = 1,
            bandedRingNumber = chickRing,
            growthStage = "BANDED",
            notes = "Strong chick"
        )
        repository.saveChick(chick)
        val allChicks = repository.allChicks.first()
        assertTrue(allChicks.any { it.id == chickId })

        // Step 7: Record Weight Log
        val weightRecord = WeightRecordEntity(
            birdRingNumber = sireRing,
            weightGrams = 48.5,
            recordedDate = System.currentTimeMillis(),
            notes = "Good show condition"
        )
        database.healthDao().insertWeight(weightRecord)

        // Step 8: View Pedigree & Lineage
        val birdWithLineage = repository.getBirdByRing(sireRing)
        assertNotNull(birdWithLineage)
        assertEquals("Champion Sky", birdWithLineage!!.name)

        // Step 9: Record Health Log
        val healthRecord = HealthRecordEntity(
            birdRingNumber = sireRing,
            recordDate = System.currentTimeMillis(),
            recordType = "EXAMINATION",
            symptoms = "Routine checkup",
            diagnosis = "Excellent plumage and vigor",
            veterinarianName = "Dr. Aviary"
        )
        database.healthDao().insertHealthRecord(healthRecord)

        // Step 10: Enter Competition
        val compId = UUID.randomUUID().toString()
        val competition = CompetitionEntity(
            id = compId,
            title = "National Budgerigar Championship 2026",
            location = "Tehran Exhibition Center",
            organizingClub = "Iran Budgerigar Society",
            eventDate = System.currentTimeMillis(),
            showStandard = "WBO",
            status = "COMPLETED"
        )
        database.competitionDao().insertCompetition(competition)

        val score = CompetitionScoreEntity(
            competitionId = compId,
            birdRingNumber = sireRing,
            showClass = "English Show Cock",
            totalScore = 94.5,
            awardTitle = "1st Place - Gold Medal",
            notes = "Best in Show English Variety"
        )
        database.competitionDao().insertScore(score)

        // Step 11: Verify Dashboard Flow Metrics
        val birdsCount = repository.allBirds.first().size
        assertTrue("Flock size should reflect registered birds", birdsCount >= 2)

        val pairsCount = repository.allPairs.first().size
        assertTrue("Active pairs should be present", pairsCount >= 1)
    }
}
