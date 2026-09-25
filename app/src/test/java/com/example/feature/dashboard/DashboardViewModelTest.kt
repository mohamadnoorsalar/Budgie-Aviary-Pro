package com.example.feature.dashboard

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.data.database.AppDatabase
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.repository.AviaryRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardViewModelTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AviaryRepositoryImpl
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AviaryRepositoryImpl(database)
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testDashboardMetricsAndQuickActions() = runBlocking {
        // 1. Initial State Check
        val initialBirds = repository.birdCount.first()
        assertEquals(0, initialBirds)

        // 2. Add Bird Quick Action
        viewModel.addBird(
            ringNumber = "IR-2026-BUDGIE-01",
            name = "Sky Lord",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            mutation = "Spangle Opaline",
            color = "Sky Blue",
            cageCode = "CAGE-01"
        )
        val birdsCount = repository.birdCount.first()
        assertEquals(1, birdsCount)

        // 3. Add Cage and Pair Quick Action
        repository.saveCage(CageEntity(code = "CAGE-01", type = CageType.BREEDING_BOX, capacity = 2))
        viewModel.addPair(
            maleRing = "IR-2026-BUDGIE-01",
            femaleRing = "IR-2026-BUDGIE-02",
            cageCode = "CAGE-01",
            notes = "Pair for spangle exhibition"
        )
        val pairCount = repository.activePairCount.first()
        assertEquals(1, pairCount)

        // 4. Add Egg Quick Action
        val pairs = repository.allPairs.first()
        val pairId = pairs.first().id
        viewModel.addEgg(pairId = pairId, eggNumber = 1, notes = "Laid this morning")
        val eggCount = repository.totalEggCount.first()
        assertEquals(1, eggCount)

        // 5. Add Chick Quick Action
        val eggs = repository.upcomingHatchEggs.first()
        val egg = eggs.first()
        viewModel.addChick(
            pairId = pairId,
            eggId = egg.id,
            bandNumber = "IR-2026-CHICK-01",
            notes = "Healthy strong chick"
        )
        val chickCount = repository.totalChickCount.first()
        assertEquals(1, chickCount)

        // 6. Record Weight Quick Action
        viewModel.recordWeight(
            birdRingNumber = "IR-2026-BUDGIE-01",
            weightGrams = 52.4,
            conditionScore = "OPTIMAL",
            notes = "Show condition weight"
        )
        val weights = repository.getWeightHistoryForBird("IR-2026-BUDGIE-01").first()
        assertEquals(1, weights.size)
        assertEquals(52.4, weights.first().weightGrams, 0.01)

        // 7. Record Feeding Quick Action
        viewModel.recordFeeding(
            planName = "Sprouted Seeds & Egg Mix",
            cageCode = "CAGE-01",
            notes = "Enriched with vitamins"
        )
        val feedingPlans = repository.allNutritionPlans.first()
        assertEquals(1, feedingPlans.size)

        // 8. Record Medication Quick Action
        viewModel.recordMedication(
            birdRingNumber = "IR-2026-BUDGIE-01",
            medicationName = "Ronidazole 10%",
            dosage = "2g / Litre",
            route = "WATER",
            frequency = "DAILY",
            durationDays = 5,
            notes = "Canker prevention treatment"
        )
        val medications = repository.activeMedications.first()
        assertEquals(1, medications.size)
        assertEquals("Ronidazole 10%", medications.first().medicationName)

        // 9. Financial Summary Flow Verification
        repository.saveIncome(
            IncomeEntity(
                title = "Sold English show hen",
                category = "BIRD_SALE",
                amount = 2500000.0
            )
        )
        repository.saveExpense(
            ExpenseEntity(
                title = "Purchased premium millet seed bag",
                category = "FEED",
                amount = 800000.0
            )
        )
        val totalInc = repository.totalIncome.first()
        val totalExp = repository.totalExpenses.first()
        assertEquals(2500000.0, totalInc, 0.01)
        assertEquals(800000.0, totalExp, 0.01)

        // 10. Low Inventory Alert Check
        repository.saveInventoryItem(
            InventoryItemEntity(
                sku = "SEED-MILLET-CANARY",
                name = "Canary Seed",
                currentStock = 1.5,
                unit = "KG",
                minStockThreshold = 5.0
            )
        )
        val lowStock = repository.lowStockItems.first()
        assertEquals(1, lowStock.size)
        assertEquals("Canary Seed", lowStock.first().name)

        // 11. Reminders & Task Completion
        val remId = repository.saveReminder(
            ReminderEntity(
                title = "Check egg candling Day 7",
                description = "Candle clutch eggs for embryo development",
                dueDate = System.currentTimeMillis() + 86400000L
            )
        )
        val pendingReminders = repository.pendingReminders.first()
        assertEquals(1, pendingReminders.size)
        viewModel.markReminderCompleted(pendingReminders.first())
        val completedReminders = repository.pendingReminders.first()
        assertEquals(0, completedReminders.size)

        // 12. Audit Logs Check
        val auditLogs = repository.recentAuditLogs.first()
        assertTrue(auditLogs.isNotEmpty())
    }
}
