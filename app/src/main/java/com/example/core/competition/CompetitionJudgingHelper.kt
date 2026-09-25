package com.example.core.competition

import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionScoreEntity

/**
 * Helper and calculation utilities for professional judging standards,
 * configurable criteria evaluations, weighted scores, and reports.
 */
object CompetitionJudgingHelper {

    /**
     * Built-in template standard: World Budgerigar Organisation (WBO)
     */
    fun createWboStandardCriteria(competitionId: String): List<CompetitionCriterionEntity> {
        return listOf(
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Head & Brow Profile",
                description = "Wide, full frontal rise with gentle curving dome above the eye",
                maxScore = 20.0,
                weight = 1.0,
                displayOrder = 1
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Mask & Throat Spots",
                description = "Deep, wide mask with six large, round, evenly spaced symmetrical spots",
                maxScore = 15.0,
                weight = 1.0,
                displayOrder = 2
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Body Structure & Size",
                description = "Well-balanced taper from broad shoulder line down to tail (ideal length 21.6 cm)",
                maxScore = 25.0,
                weight = 1.0,
                displayOrder = 3
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Feather Condition & Bloom",
                description = "Tight, smooth plumage, clean and free from pin feathers or fraying",
                maxScore = 15.0,
                weight = 1.0,
                displayOrder = 4
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Color & Wing Markings",
                description = "Rich, even base color tone and sharp distinct melanin markings",
                maxScore = 10.0,
                weight = 1.0,
                displayOrder = 5
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Stance & Perch Angle",
                description = "Confident grip at ideal 30-degree angle from vertical line",
                maxScore = 10.0,
                weight = 1.0,
                displayOrder = 6
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "Legs & Nails",
                description = "Clean, strong legs, two forward toes and two backward firmly gripping the perch",
                maxScore = 5.0,
                weight = 1.0,
                displayOrder = 7
            )
        )
    }

    /**
     * Built-in template standard: Persian National Show Standard
     */
    fun createNationalStandardCriteria(competitionId: String): List<CompetitionCriterionEntity> {
        return listOf(
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "فرم سر و تاج (Head & Crest/Brow)",
                description = "برآمدگی عریض پیشانی و قوس متقارن جمجمه",
                maxScore = 20.0,
                weight = 1.0,
                displayOrder = 1
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "ماسک و خال‌های طوق (Mask & Spots)",
                description = "عمق ماسک و اندازه یکدست ۶ خال طوق",
                maxScore = 15.0,
                weight = 1.0,
                displayOrder = 2
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "ساختار بدنی و شانه (Body & Shoulders)",
                description = "عرض سینه، زاویه شانه و تناسب طول بدن",
                maxScore = 20.0,
                weight = 1.0,
                displayOrder = 3
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "کیفیت و جلای پرها (Feathers)",
                description = "پرهای شاداب، صاف و فاقد افتادگی بال یا شکستگی دم",
                maxScore = 15.0,
                weight = 1.0,
                displayOrder = 4
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "رنگ و نقش بال (Color & Pattern)",
                description = "وضوح نقوش و یکنواختی رنگ شکم و پشت",
                maxScore = 10.0,
                weight = 1.0,
                displayOrder = 5
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "استقرار روی نشیمنگاه (Presentation & Stance)",
                description = "رفتار آرام، تسلط روی میله و زاویه مطلوب",
                maxScore = 10.0,
                weight = 1.0,
                displayOrder = 6
            ),
            CompetitionCriterionEntity(
                competitionId = competitionId,
                name = "پاها و ناخن‌ها (Legs & Nails)",
                description = "سالم بودن انگشتان و گیرایی کامل روی چوب",
                maxScore = 10.0,
                weight = 1.0,
                displayOrder = 7
            )
        )
    }

    /**
     * Parse raw scores stored in criteriaScoresJson format: Map<String, Double>
     */
    fun parseCriteriaScores(json: String?): Map<String, Double> {
        if (json.isNullOrBlank() || json == "{}") return emptyMap()
        val result = mutableMapOf<String, Double>()
        val clean = json.trim().removeSurrounding("{", "}").trim()
        if (clean.isBlank()) return emptyMap()
        clean.split(",").forEach { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim().replace("\"", "")
                val value = parts[1].trim().toDoubleOrNull() ?: 0.0
                result[key] = value
            }
        }
        return result
    }

    /**
     * Serialize score map to JSON format
     */
    fun serializeCriteriaScores(scores: Map<String, Double>): String {
        val entries = scores.map { (k, v) -> "\"$k\": $v" }
        return "{${entries.joinToString(", ")}}"
    }

    /**
     * Calculate total weighted score from criterion list and recorded score map
     */
    fun calculateTotalWeightedScore(
        criteria: List<CompetitionCriterionEntity>,
        scoreMap: Map<String, Double>
    ): Double {
        if (criteria.isEmpty()) {
            return scoreMap.values.sum()
        }
        var total = 0.0
        criteria.forEach { criterion ->
            val raw = scoreMap[criterion.id] ?: 0.0
            val capped = raw.coerceIn(0.0, criterion.maxScore)
            total += capped * criterion.weight
        }
        return Math.round(total * 100.0) / 100.0
    }

    /**
     * Suggest an award based on total score (standard 100-point show scale)
     */
    fun suggestAwardTitle(totalScore: Double): String {
        return when {
            totalScore >= 95.0 -> "BEST_IN_SHOW"
            totalScore >= 90.0 -> "FIRST_IN_CLASS"
            totalScore >= 85.0 -> "SECOND_IN_CLASS"
            totalScore >= 80.0 -> "THIRD_IN_CLASS"
            totalScore >= 70.0 -> "DIPLOMA_OF_MERIT"
            else -> "PARTICIPATION"
        }
    }

    /**
     * Generates visual AI assistance hints based on evaluation criteria
     */
    fun generateVisualAiSuggestion(
        birdMutation: String?,
        variety: String?,
        notes: String?
    ): String {
        val varietyDesc = variety?.replace("_", " ") ?: "English Show"
        return "AI Visual Assessment Hint: For $varietyDesc (${birdMutation ?: "Standard"}), check throat spot symmetry (minimum 6 target), forehead height convexity, and 30° stance stability. Final official marks must be awarded by authorized human judges."
    }

    /**
     * Parse categories / classes JSON list
     */
    fun parseJsonList(json: String?): List<String> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return json.trim()
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().replace("\"", "") }
            .filter { it.isNotBlank() }
    }

    /**
     * Serialize list to JSON array format
     */
    fun serializeJsonList(items: List<String>): String {
        val quoted = items.map { "\"$it\"" }
        return "[${quoted.joinToString(", ")}]"
    }
}
