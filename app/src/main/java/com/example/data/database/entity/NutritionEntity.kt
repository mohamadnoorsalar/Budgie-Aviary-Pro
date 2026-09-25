package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "nutrition_plans",
    indices = [
        Index(value = ["targetCageCode"]),
        Index(value = ["seasonPhase"])
    ]
)
data class NutritionPlanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val planName: String,
    val seasonPhase: String = "BREEDING", // RESTING, PRE_BREEDING, BREEDING, WEANING, MOLTING
    val seedMixDescription: String,
    val softFoodRecipe: String,
    val freshGreensSchedule: String,
    val supplementsSchedule: String,
    val targetCageCode: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Individual Feeding / Nutrition Log:
 * Supports:
 * - Food type
 * - Amount
 * - Consumption
 * - Water
 * - Supplements
 * - Feeding schedule
 * - Cost
 * - Connects to birdRingNumber, cageCode, or general flock
 */
@Entity(
    tableName = "nutrition_records",
    indices = [
        Index(value = ["birdRingNumber"]),
        Index(value = ["cageCode"]),
        Index(value = ["recordDate"])
    ]
)
data class NutritionRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val birdRingNumber: String? = null,
    val cageCode: String? = null,
    val foodType: String, // Base Seed Mix, Soft Food / Egg Food, Sprouted Seeds, Fresh Greens, Pellets, Millet Spray
    val amount: String, // e.g. "20g", "2 tbsp", "1 bowl"
    val consumptionRate: String = "NORMAL", // LOW, NORMAL, HIGH, FINISHED_ALL, LEFTOVERS
    val waterType: String = "FRESH_WATER", // FRESH_WATER, VITAMINS, ELECTROLYTES, ACV_ACIDIFIED, MEDICATED
    val supplements: String = "", // e.g. "Calcivet, Nekton S, Probiotic"
    val feedingSchedule: String = "MORNING", // MORNING, NOON, EVENING, TWICE_DAILY, AD_LIBITUM
    val cost: Double = 0.0,
    val recordDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
