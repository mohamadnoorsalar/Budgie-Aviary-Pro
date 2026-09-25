package com.example.feature.reminders

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import com.example.core.common.displayNameEn
import com.example.core.common.displayNameFa
import com.example.core.localization.AppLanguage
import com.example.core.notification.AviaryNotificationManager
import com.example.core.notification.NotificationPreferences
import com.example.core.notification.NotificationPreferencesStore
import com.example.data.database.AppDatabase
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.repository.AviaryRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RemindersModuleTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AviaryRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AviaryRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAllTwelveReminderTypesHavePersianAndEnglishNames() {
        val types = ReminderType.entries
        assertEquals(12, types.size)

        for (type in types) {
            assertTrue(type.displayNameEn.isNotBlank())
            assertTrue(type.displayNameFa.isNotBlank())
        }

        assertEquals("Egg Laying", ReminderType.EGG_LAYING.displayNameEn)
        assertEquals("تخم‌گذاری و کندلینگ", ReminderType.EGG_LAYING.displayNameFa)
        assertEquals("Expected Hatch", ReminderType.EXPECTED_HATCH.displayNameEn)
        assertEquals("زمان پیش‌بینی تولد جوجه", ReminderType.EXPECTED_HATCH.displayNameFa)
        assertEquals("Medication", ReminderType.MEDICATION.displayNameEn)
        assertEquals("دارو و درمان", ReminderType.MEDICATION.displayNameFa)
        assertEquals("Inventory & Reorder", ReminderType.INVENTORY.displayNameEn)
        assertEquals("موجودی انبار و دان", ReminderType.INVENTORY.displayNameFa)
    }

    @Test
    fun testReminderTaskStateCategorization() = runBlocking {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // 1. Overdue task (yesterday)
        val overdue = ReminderEntity(
            title = "Overdue Cage Cleaning",
            dueDate = now - (2 * dayMs),
            priority = ReminderPriority.HIGH,
            type = ReminderType.CLEANING
        )

        // 2. Today's task
        val today = ReminderEntity(
            title = "Morning Softfood",
            dueDate = now + 3600000L,
            priority = ReminderPriority.NORMAL,
            type = ReminderType.FEEDING
        )

        // 3. Upcoming task (in 5 days)
        val upcoming = ReminderEntity(
            title = "Egg Candling Day 6",
            dueDate = now + (5 * dayMs),
            priority = ReminderPriority.NORMAL,
            type = ReminderType.EGG_LAYING
        )

        // 4. Completed task
        val completed = ReminderEntity(
            title = "Vitamin Administration",
            dueDate = now - dayMs,
            priority = ReminderPriority.LOW,
            type = ReminderType.SUPPLEMENTS,
            isCompleted = true,
            completedAt = now - 1000L
        )

        val id1 = repository.saveReminder(overdue)
        val id2 = repository.saveReminder(today)
        val id3 = repository.saveReminder(upcoming)
        val id4 = repository.saveReminder(completed)

        val all = repository.allReminders.first()
        assertEquals(4, all.size)

        val pending = repository.pendingReminders.first()
        assertEquals(3, pending.size)

        val completedList = repository.completedReminders.first()
        assertEquals(1, completedList.size)
        assertEquals("Vitamin Administration", completedList[0].title)

        val pendingCount = repository.pendingReminderCount.first()
        assertEquals(3, pendingCount)
    }

    @Test
    fun testDeduplicationPreventsDuplicateRemindersOnMultipleSyncs() = runBlocking {
        // Insert sample egg, medication, inventory item, and cage
        val egg = EggEntity(
            id = "EGG-101",
            pairId = 1L,
            clutchId = 1L,
            eggNumber = 1,
            layDate = System.currentTimeMillis() - (5 * 86400000L),
            fertilityStatus = "UNCANDLED",
            isHatched = false
        )
        database.reproductionDao().insertEgg(egg)

        val med = MedicationEntity(
            id = "MED-01",
            birdRingNumber = "IR-2024-TEST",
            medicationName = "Baytril 10%",
            dosage = "2 drops in beak",
            startDate = System.currentTimeMillis(),
            isCompleted = false
        )
        database.healthDao().insertMedication(med)

        val inv = InventoryItemEntity(
            id = "INV-01",
            sku = "SKU-SEED-01",
            name = "Prestige Budgie Premium Seed",
            category = "SEED",
            currentStock = 2.0,
            minStockThreshold = 5.0,
            unit = "kg"
        )
        database.inventoryDao().insertItem(inv)

        val cage = CageEntity(
            code = "C-10",
            type = com.example.core.common.CageType.BREEDING_BOX,
            isClean = false,
            lastCleanedDate = System.currentTimeMillis() - (10 * 86400000L)
        )
        database.cageDao().insertCage(cage)

        // First sync
        val sync1Count = repository.syncSmartReminders()
        assertTrue("First sync should create reminders", sync1Count >= 4)

        val countAfterSync1 = repository.pendingReminderCount.first()

        // Second sync immediately after - SHOULD NOT create duplicates!
        val sync2Count = repository.syncSmartReminders()
        assertEquals("Second sync should create 0 duplicates", 0, sync2Count)

        val countAfterSync2 = repository.pendingReminderCount.first()
        assertEquals("Pending reminder count must remain exact", countAfterSync1, countAfterSync2)
    }

    @Test
    fun testNotificationPreferencesSaveAndLoad() {
        val customPrefs = NotificationPreferences(
            isGloballyEnabled = true,
            enabledTypes = setOf(ReminderType.EGG_LAYING, ReminderType.EXPECTED_HATCH, ReminderType.MEDICATION),
            reminderHour = 7,
            reminderMinute = 45,
            isSoundEnabled = true,
            isVibrationEnabled = false
        )

        NotificationPreferencesStore.save(context, customPrefs)
        val loaded = NotificationPreferencesStore.load(context)

        assertEquals(true, loaded.isGloballyEnabled)
        assertEquals(3, loaded.enabledTypes.size)
        assertTrue(loaded.enabledTypes.contains(ReminderType.EGG_LAYING))
        assertTrue(loaded.enabledTypes.contains(ReminderType.EXPECTED_HATCH))
        assertTrue(loaded.enabledTypes.contains(ReminderType.MEDICATION))
        assertFalse(loaded.enabledTypes.contains(ReminderType.CLEANING))
        assertEquals(7, loaded.reminderHour)
        assertEquals(45, loaded.reminderMinute)
        assertEquals(false, loaded.isVibrationEnabled)
    }

    @Test
    fun testToggleCompletionAndRecurringInstance() = runBlocking {
        val recurringReminder = ReminderEntity(
            title = "Weekly Aviary Disinfection",
            dueDate = System.currentTimeMillis(),
            priority = ReminderPriority.NORMAL,
            type = ReminderType.CLEANING,
            repeatIntervalDays = 7,
            isCompleted = false
        )

        val id = repository.saveReminder(recurringReminder)
        val fetched = database.reminderDao().getReminderById(id)
        assertNotNull(fetched)

        // Mark completed
        repository.updateReminder(fetched!!.copy(isCompleted = true, completedAt = System.currentTimeMillis()))

        // Simulate ViewModel recurring schedule creation
        if (fetched.repeatIntervalDays > 0) {
            repository.saveReminder(
                fetched.copy(
                    id = 0,
                    dueDate = fetched.dueDate + (7 * 86400000L),
                    isCompleted = false,
                    completedAt = null
                )
            )
        }

        val all = repository.allReminders.first()
        assertEquals(2, all.size) // 1 completed, 1 new recurring pending
        val pending = repository.pendingReminders.first()
        assertEquals(1, pending.size)
        assertTrue(pending[0].dueDate > System.currentTimeMillis())
    }

    @Test
    fun testClearCompletedReminders() = runBlocking {
        repository.saveReminder(ReminderEntity(title = "Task 1", dueDate = System.currentTimeMillis(), isCompleted = true))
        repository.saveReminder(ReminderEntity(title = "Task 2", dueDate = System.currentTimeMillis(), isCompleted = true))
        repository.saveReminder(ReminderEntity(title = "Task 3", dueDate = System.currentTimeMillis(), isCompleted = false))

        assertEquals(3, repository.allReminders.first().size)
        assertEquals(2, repository.completedReminders.first().size)

        repository.clearCompletedReminders()

        assertEquals(1, repository.allReminders.first().size)
        assertEquals(0, repository.completedReminders.first().size)
        assertEquals("Task 3", repository.pendingReminders.first()[0].title)
    }
}
