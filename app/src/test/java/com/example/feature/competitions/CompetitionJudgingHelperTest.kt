package com.example.feature.competitions

import com.example.core.competition.CompetitionJudgingHelper
import com.example.data.database.entity.CompetitionCriterionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetitionJudgingHelperTest {

    @Test
    fun testWboCriteriaCreation() {
        val criteria = CompetitionJudgingHelper.createWboStandardCriteria("comp_1")
        assertEquals(7, criteria.size)
        val maxTotal = criteria.sumOf { it.maxScore * it.weight }
        assertEquals(100.0, maxTotal, 0.001)
    }

    @Test
    fun testPersianNationalCriteriaCreation() {
        val criteria = CompetitionJudgingHelper.createNationalStandardCriteria("comp_2")
        assertEquals(7, criteria.size)
        val maxTotal = criteria.sumOf { it.maxScore * it.weight }
        assertEquals(100.0, maxTotal, 0.001)
    }

    @Test
    fun testWeightedScoreCalculation() {
        val criteria = listOf(
            CompetitionCriterionEntity(id = "c1", competitionId = "comp_1", name = "Head", maxScore = 20.0, weight = 1.0),
            CompetitionCriterionEntity(id = "c2", competitionId = "comp_1", name = "Body", maxScore = 25.0, weight = 1.5),
            CompetitionCriterionEntity(id = "c3", competitionId = "comp_1", name = "Mask", maxScore = 15.0, weight = 1.0)
        )
        val scoreMap = mapOf(
            "c1" to 18.0,
            "c2" to 20.0, // 20.0 * 1.5 = 30.0
            "c3" to 14.0
        )
        val total = CompetitionJudgingHelper.calculateTotalWeightedScore(criteria, scoreMap)
        assertEquals(62.0, total, 0.001)
    }

    @Test
    fun testScoreSerializationAndParsing() {
        val original = mapOf("crit_1" to 19.5, "crit_2" to 14.0)
        val json = CompetitionJudgingHelper.serializeCriteriaScores(original)
        val parsed = CompetitionJudgingHelper.parseCriteriaScores(json)
        assertEquals(2, parsed.size)
        assertEquals(19.5, parsed["crit_1"] ?: 0.0, 0.001)
        assertEquals(14.0, parsed["crit_2"] ?: 0.0, 0.001)
    }

    @Test
    fun testAwardTitleSuggestion() {
        assertEquals("BEST_IN_SHOW", CompetitionJudgingHelper.suggestAwardTitle(96.5))
        assertEquals("FIRST_IN_CLASS", CompetitionJudgingHelper.suggestAwardTitle(91.0))
        assertEquals("SECOND_IN_CLASS", CompetitionJudgingHelper.suggestAwardTitle(86.0))
        assertEquals("THIRD_IN_CLASS", CompetitionJudgingHelper.suggestAwardTitle(81.5))
        assertEquals("DIPLOMA_OF_MERIT", CompetitionJudgingHelper.suggestAwardTitle(72.0))
        assertEquals("PARTICIPATION", CompetitionJudgingHelper.suggestAwardTitle(60.0))
    }

    @Test
    fun testJsonListSerializationAndParsing() {
        val classes = listOf("English Show Cock", "Young Hen", "Normal Green")
        val json = CompetitionJudgingHelper.serializeJsonList(classes)
        val parsed = CompetitionJudgingHelper.parseJsonList(json)
        assertEquals(3, parsed.size)
        assertEquals("English Show Cock", parsed[0])
        assertEquals("Young Hen", parsed[1])
        assertEquals("Normal Green", parsed[2])
    }
}
