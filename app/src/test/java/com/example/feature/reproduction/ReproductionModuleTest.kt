package com.example.feature.reproduction

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.PairEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReproductionModuleTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AviaryRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AviaryRepositoryImpl(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testCompleteReproductionFlow_Parent_Pair_Egg_Chick_Offspring_Generation() = runBlocking {
        // 1. Create Sire (Father) and Dam (Mother)
        val sire = BirdEntity(
            ringNumber = "IR-2024-M01",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            color = "Cobalt Blue",
            generation = "F1",
            cageCode = "C-01"
        )
        val dam = BirdEntity(
            ringNumber = "IR-2024-F01",
            gender = BirdGender.FEMALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            color = "Grey Green",
            generation = "F1",
            cageCode = "C-01"
        )
        repository.saveBird(sire)
        repository.saveBird(dam)

        // 2. Setup Cage and Nest
        val cage = CageEntity(
            code = "C-01",
            type = CageType.BREEDING_BOX,
            location = "Breeding Row A"
        )
        repository.saveCage(cage)

        val nest = NestEntity(
            id = "NEST-01",
            cageCode = "C-01",
            boxNumber = "N-01",
            nestMaterial = "Pine Shavings"
        )
        repository.saveNest(nest)

        // 3. Create Breeding Pair
        val pair = PairEntity(
            maleRingNumber = sire.ringNumber,
            femaleRingNumber = dam.ringNumber,
            cageCode = "C-01",
            nestId = "NEST-01",
            pairingDate = System.currentTimeMillis() - (15L * 86400000L) // 15 days ago
        )
        val pairId = repository.savePair(pair)
        assertTrue(pairId > 0)

        // 4. Record Egg with Laying Date & Expected Hatch Date
        val layDate = System.currentTimeMillis() - (10L * 86400000L)
        val expectedHatch = layDate + (18L * 86400000L)
        val egg = EggEntity(
            id = "EGG-101-1",
            pairId = pairId,
            nestId = "NEST-01",
            eggNumber = 1,
            layDate = layDate,
            expectedHatchDate = expectedHatch,
            fertilityStatus = "UNCANDLED"
        )
        repository.saveEgg(egg)

        val savedEgg = repository.getEggById("EGG-101-1").first()
        assertNotNull(savedEgg)
        assertEquals(1, savedEgg?.eggNumber)
        assertEquals("UNCANDLED", savedEgg?.fertilityStatus)

        // 5. Update Candling Status
        val candledEgg = savedEgg!!.copy(
            fertilityStatus = "FERTILE",
            candlingDate = System.currentTimeMillis()
        )
        repository.saveEgg(candledEgg)
        val updatedEgg = repository.getEggById("EGG-101-1").first()
        assertEquals("FERTILE", updatedEgg?.fertilityStatus)

        // 6. Hatch Egg -> Chick Record with Weight and Notes
        val hatchDate = System.currentTimeMillis()
        val chick = repository.hatchEgg(
            eggId = "EGG-101-1",
            hatchDate = hatchDate,
            initialWeightGrams = 1.6,
            ringNumber = null,
            notes = "Healthy vigorous chick"
        )
        assertNotNull(chick)
        assertEquals("EGG-101-1", chick.eggId)
        assertEquals(pairId, chick.pairId)
        assertEquals(1, chick.hatchOrder)
        assertEquals(1.6, chick.weightGrams ?: 0.0, 0.01)

        // Verify egg was updated to hatched
        val postHatchEgg = repository.getEggById("EGG-101-1").first()
        assertTrue(postHatchEgg!!.isHatched)
        assertEquals("HATCHED", postHatchEgg.eggResult)
        assertEquals(hatchDate, postHatchEgg.actualHatchDate)

        // 7. Cage Transfer of Chick
        val nurseryCage = CageEntity(
            code = "C-NURSERY",
            type = CageType.STOCK_CAGE,
            location = "Weaning Nursery"
        )
        repository.saveCage(nurseryCage)
        repository.transferChickToCage(chick.id, "C-NURSERY", notes = "Fledging stage")

        val movedChick = repository.getChickById(chick.id).first()
        assertEquals("C-NURSERY", movedChick?.cageCode)
        assertEquals("TRANSFERRED", movedChick?.status)

        // 8. Graduate Chick into Official Offspring Bird -> Generation Calculation
        val offspringBird = repository.registerChickAsOffspring(
            chickId = chick.id,
            ringNumber = "IR-2024-OFFSPRING-01",
            name = "Champion 1",
            gender = BirdGender.MALE,
            color = "Cobalt Blue"
        )

        // Validate complete lineage connections
        assertEquals("IR-2024-OFFSPRING-01", offspringBird.ringNumber)
        assertEquals("IR-2024-M01", offspringBird.fatherRing)
        assertEquals("IR-2024-F01", offspringBird.motherRing)
        assertEquals("F2", offspringBird.generation) // F1 x F1 -> F2 automatically!

        // Chick record updated with ring and growth stage
        val graduatedChick = repository.getChickById(chick.id).first()
        assertEquals("IR-2024-OFFSPRING-01", graduatedChick?.bandedRingNumber)
        assertEquals("WEANED", graduatedChick?.growthStage)
    }

    @Test
    fun testChickMortalityTracking() = runBlocking {
        val egg = EggEntity(
            id = "EGG-MORT-1",
            pairId = 202L,
            eggNumber = 2,
            layDate = System.currentTimeMillis()
        )
        repository.saveEgg(egg)

        val chick = repository.hatchEgg(
            eggId = "EGG-MORT-1",
            hatchDate = System.currentTimeMillis(),
            initialWeightGrams = 1.2,
            ringNumber = null,
            notes = "Weak chick"
        )

        val deathDate = System.currentTimeMillis() + 86400000L
        repository.recordChickMortality(chick.id, mortalityDate = deathDate, reason = "Failure to thrive / crop stasis")

        val deceasedChick = repository.getChickById(chick.id).first()
        assertNotNull(deceasedChick)
        assertEquals("DECEASED", deceasedChick?.status)
        assertEquals("Failure to thrive / crop stasis", deceasedChick?.mortalityReason)
        assertEquals(deathDate, deceasedChick?.mortalityDate)
    }
}
