package com.example.feature.birds

import com.example.core.ai.BirdPhotoAnalyzer
import com.example.core.ai.SuggestionConfidence
import com.example.core.common.BudgieVariety
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BirdPhotoAnalyzerTest {

    @Test
    fun testParseValidAiVisionJson() {
        val sampleJson = """
            {
              "color": "Cobalt Blue",
              "colorConfidence": "HIGH",
              "pattern": "Spangle",
              "patternConfidence": "MEDIUM",
              "variety": "ENGLISH_SHOW",
              "varietyConfidence": "MEDIUM",
              "phenotypeDetails": ["Large directional facial feathers", "Violet cheek patches", "Clean mask"],
              "cereObservation": "Brownish crusty cere indicating breeding condition female, needs breeder check",
              "summaryFa": "مرغ عشق انگلیسی آبی کبالت، طرح اسپنگل با فرم سر نمایشی.",
              "summaryEn": "English Show Budgie, Cobalt Blue with Spangle pattern."
            }
        """.trimIndent()

        val parsed = BirdPhotoAnalyzer.parseAiJsonResponse(sampleJson)

        assertEquals("Cobalt Blue", parsed.suggestedColor)
        assertEquals(SuggestionConfidence.HIGH, parsed.colorConfidence)
        assertEquals("Spangle", parsed.suggestedPattern)
        assertEquals(SuggestionConfidence.MEDIUM, parsed.patternConfidence)
        assertEquals(BudgieVariety.ENGLISH_SHOW, parsed.suggestedVariety)
        assertEquals(SuggestionConfidence.MEDIUM, parsed.varietyConfidence)
        assertTrue(parsed.needsHumanReview)
        assertTrue(parsed.sexDisclaimer.contains("قطعی نیست"))
        assertNotNull(parsed.suggestedSexGuess)
        assertEquals(3, parsed.visiblePhenotypeDetails.size)
    }

    @Test
    fun testUncertainResultFallback() {
        val brokenJson = "Invalid response from server"
        val fallback = BirdPhotoAnalyzer.parseAiJsonResponse(brokenJson)

        assertNotNull(fallback.suggestedColor)
        assertTrue(fallback.needsHumanReview)
        assertTrue(fallback.sexDisclaimer.contains("Sex, age & hidden genetics"))
    }

    @Test
    fun testNeverTreatsSexAsCertain() {
        val sample = BirdPhotoAnalyzer.generateLocalFallbackSuggestion()
        assertTrue(sample.needsHumanReview)
        assertTrue(sample.sexDisclaimer.contains("قطعی نیست"))
    }
}
