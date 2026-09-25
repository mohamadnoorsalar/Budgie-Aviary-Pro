package com.example.core

import com.example.core.common.ReminderPriority
import com.example.core.health.WeightTrackerHelper
import com.example.core.health.WeightTrend
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.WeightRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verification test suite for Health, Nutrition, and Weight modules.
 */
class HealthNutritionWeightTest {

    @Test
    fun testWeightAnalysis_MultipleRecords_CalculatesCurrentPreviousAndChange() {
        val records = listOf(
            WeightRecordEntity(
                id = "W1",
                birdRingNumber = "IR-2026-001",
                weightGrams = 42.0,
                recordedDate = 1000L,
                conditionScore = "Good"
            ),
            WeightRecordEntity(
                id = "W2",
                birdRingNumber = "IR-2026-001",
                weightGrams = 45.5,
                recordedDate = 2000L,
                conditionScore = "Very Good"
            ),
            WeightRecordEntity(
                id = "W3",
                birdRingNumber = "IR-2026-001",
                weightGrams = 48.0,
                recordedDate = 3000L,
                conditionScore = "Excellent"
            )
        )

        val analysis = WeightTrackerHelper.analyzeWeights(records)

        // Latest record is at 3000L with 48.0g
        assertEquals(48.0, analysis.currentWeightGrams ?: 0.0, 0.001)
        // Previous record is at 2000L with 45.5g
        assertEquals(45.5, analysis.previousWeightGrams ?: 0.0, 0.001)
        // Weight change is 48.0 - 45.5 = +2.5g
        assertEquals(2.5, analysis.weightChangeGrams ?: 0.0, 0.001)
        // Trend should be GAIN
        assertEquals(WeightTrend.GAIN, analysis.trend)
        assertTrue((analysis.weightChangePercentage ?: 0.0) > 0)
        assertEquals(3, analysis.recordsSortedChronologically.size)
    }

    @Test
    fun testWeightAnalysis_LosingWeight_ReportsLossStatus() {
        val records = listOf(
            WeightRecordEntity(
                id = "W1",
                birdRingNumber = "IR-2026-002",
                weightGrams = 50.0,
                recordedDate = 1000L
            ),
            WeightRecordEntity(
                id = "W2",
                birdRingNumber = "IR-2026-002",
                weightGrams = 46.0,
                recordedDate = 2000L
            )
        )

        val analysis = WeightTrackerHelper.analyzeWeights(records)

        assertEquals(46.0, analysis.currentWeightGrams ?: 0.0, 0.001)
        assertEquals(50.0, analysis.previousWeightGrams ?: 0.0, 0.001)
        assertEquals(-4.0, analysis.weightChangeGrams ?: 0.0, 0.001)
        assertEquals(WeightTrend.LOSS, analysis.trend)
        assertTrue((analysis.weightChangePercentage ?: 0.0) < 0)
    }

    @Test
    fun testWeightAnalysis_SingleRecord_ReturnsNoPrevious() {
        val records = listOf(
            WeightRecordEntity(
                id = "W1",
                birdRingNumber = "IR-2026-003",
                weightGrams = 40.0,
                recordedDate = 1000L
            )
        )

        val analysis = WeightTrackerHelper.analyzeWeights(records)

        assertEquals(40.0, analysis.currentWeightGrams ?: 0.0, 0.001)
        assertNull(analysis.previousWeightGrams)
        assertNull(analysis.weightChangeGrams)
        assertEquals(WeightTrend.STABLE, analysis.trend)
    }

    @Test
    fun testWeightAnalysis_EmptyRecords_ReturnsGracefulDefaults() {
        val analysis = WeightTrackerHelper.analyzeWeights(emptyList<WeightRecordEntity>())

        assertNull(analysis.currentWeightGrams)
        assertNull(analysis.previousWeightGrams)
        assertNull(analysis.weightChangeGrams)
        assertEquals(WeightTrend.NO_DATA, analysis.trend)
        assertTrue(analysis.recordsSortedChronologically.isEmpty())
    }

    @Test
    fun testHealthRecord_AllRequiredFieldsSupported() {
        val start = System.currentTimeMillis()
        val durationDays = 7
        val end = start + (durationDays * 86400000L)

        val healthRecord = HealthRecordEntity(
            id = "H10",
            birdRingNumber = "IR-2026-BIRD-99",
            recordDate = start,
            recordType = "RESPIRATORY_TREATMENT",
            symptoms = "Wheezing, tail bobbing, lethargy",
            diagnosis = "Upper Respiratory Tract Infection",
            recordedProblem = "Severe Respiratory Distress",
            medicationName = "Doxycycline 5%",
            dosage = "3 drops per 50ml water",
            frequency = "TWICE_DAILY",
            treatmentDurationDays = durationDays,
            startDate = start,
            endDate = end,
            supplements = "Vitamin B-Complex & Probiotics",
            isResolved = false,
            veterinarianName = "Dr. Rostami",
            notes = "Bird isolated in quarantine hospital cage"
        )

        assertEquals("IR-2026-BIRD-99", healthRecord.birdRingNumber)
        assertEquals("Wheezing, tail bobbing, lethargy", healthRecord.symptoms)
        assertEquals("Severe Respiratory Distress", healthRecord.recordedProblem)
        assertEquals("Doxycycline 5%", healthRecord.medicationName)
        assertEquals("3 drops per 50ml water", healthRecord.dosage)
        assertEquals("TWICE_DAILY", healthRecord.frequency)
        assertEquals(7, healthRecord.treatmentDurationDays)
        assertEquals(start, healthRecord.startDate)
        assertEquals(end, healthRecord.endDate)
        assertEquals("Vitamin B-Complex & Probiotics", healthRecord.supplements)
        assertEquals(false, healthRecord.isResolved)
    }

    @Test
    fun testMedicationReminders_ScheduleGeneration() {
        val start = 1700000000000L
        val durationDays = 5
        val end = start + (durationDays * 86400000L)

        val medication = MedicationEntity(
            id = "MED-01",
            healthRecordId = "H10",
            birdRingNumber = "IR-2026-BIRD-99",
            medicationName = "Baytril 10%",
            dosage = "0.2 ml",
            frequency = "ONCE_DAILY",
            startDate = start,
            endDate = end,
            notes = "Morning oral dose"
        )

        // Generate reminders simulating repository logic
        val reminders = (0 until durationDays).map { dayIndex ->
            val reminderTime = start + (dayIndex * 86400000L)
            ReminderEntity(
                title = "Medication Dose: ${medication.medicationName}",
                description = "Administer ${medication.dosage} (${medication.frequency}) for bird ${medication.birdRingNumber}. Day ${dayIndex + 1} of $durationDays",
                dueDate = reminderTime,
                priority = ReminderPriority.HIGH,
                relatedRingNumber = medication.birdRingNumber,
                isCompleted = false
            )
        }

        assertEquals(5, reminders.size)
        assertEquals(ReminderPriority.HIGH, reminders[0].priority)
        assertEquals("IR-2026-BIRD-99", reminders[0].relatedRingNumber)
        assertTrue(reminders[0].description!!.contains("Day 1 of 5"))
        assertTrue(reminders[4].description!!.contains("Day 5 of 5"))
        assertEquals(start + 4 * 86400000L, reminders[4].dueDate)
    }

    @Test
    fun testNutritionRecord_AllRequiredFieldsSupported() {
        val now = System.currentTimeMillis()
        val nutrition = NutritionRecordEntity(
            id = "N5",
            birdRingNumber = "IR-2026-BIRD-01",
            cageCode = "CAGE-A1",
            recordDate = now,
            foodType = "Sprouted Seeds & Egg Food",
            amount = "25 grams",
            consumptionRate = "FINISHED_ALL",
            waterType = "Fresh Mineral Water with Apple Cider Vinegar",
            supplements = "Nekton-S + Calcium Calcivet",
            feedingSchedule = "MORNING",
            cost = 1.75,
            notes = "Preparation for breeding condition"
        )

        assertEquals("IR-2026-BIRD-01", nutrition.birdRingNumber)
        assertEquals("Sprouted Seeds & Egg Food", nutrition.foodType)
        assertEquals("25 grams", nutrition.amount)
        assertEquals("FINISHED_ALL", nutrition.consumptionRate)
        assertEquals("Fresh Mineral Water with Apple Cider Vinegar", nutrition.waterType)
        assertEquals("Nekton-S + Calcium Calcivet", nutrition.supplements)
        assertEquals("MORNING", nutrition.feedingSchedule)
        assertEquals(1.75, nutrition.cost, 0.001)
    }

    @Test
    fun testNutritionCost_AggregationAcrossFeedings() {
        val logs = listOf(
            NutritionRecordEntity(birdRingNumber = "B1", recordDate = 100L, foodType = "Mix", amount = "20g", consumptionRate = "HIGH", cost = 0.50),
            NutritionRecordEntity(birdRingNumber = "B1", recordDate = 200L, foodType = "Egg food", amount = "15g", consumptionRate = "HIGH", cost = 0.75),
            NutritionRecordEntity(birdRingNumber = "B2", recordDate = 300L, foodType = "Greens", amount = "10g", consumptionRate = "MEDIUM", cost = 0.25)
        )

        val totalCost = logs.sumOf { it.cost }
        val b1Cost = logs.filter { it.birdRingNumber == "B1" }.sumOf { it.cost }

        assertEquals(1.50, totalCost, 0.001)
        assertEquals(1.25, b1Cost, 0.001)
    }
}
