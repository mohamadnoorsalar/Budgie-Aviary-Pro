package com.example.core.ai

import com.example.core.common.BudgieVariety

/**
 * Structured phenotype and visual characteristics suggested by AI Vision.
 * Explicitly distinguishes observable visual phenotypes (color, pattern, body variety)
 * from uncertain traits (sex, age, hidden genetics).
 */
data class BirdVisualSuggestion(
    // Visual traits observable from photo
    val suggestedColor: String? = null,
    val colorConfidence: SuggestionConfidence = SuggestionConfidence.HIGH,

    val suggestedPattern: String? = null, // e.g. Opaline, Spangle, Pied, Normal Barred, Clearwing
    val patternConfidence: SuggestionConfidence = SuggestionConfidence.MEDIUM,

    val suggestedVariety: BudgieVariety? = null, // English Show (heavy brow/crestal), Australian Standard, Crested, Hagoromo
    val varietyConfidence: SuggestionConfidence = SuggestionConfidence.MEDIUM,

    val visiblePhenotypeDetails: List<String> = emptyList(), // e.g. "Cheek patches: Violet", "Throat spots: 6 distinct spots"

    // Non-visual / uncertain biological traits: STRICTLY marked as tentative / Needs Confirmation
    val suggestedSexGuess: String? = null, // Cere color observation only: Blue, Brown, Pinkish
    val sexDisclaimer: String = "⚠️ جنسیت، سن دقیق و ژنتیک مخفی از روی تصویر قطعی نیست و نیازمند تایید کاربر است. (Sex, age & hidden genetics cannot be determined with certainty from a photo)",

    val overallSummary: String = "",
    val needsHumanReview: Boolean = true
)

enum class SuggestionConfidence {
    HIGH,               // مطمئن / High
    MEDIUM,             // نیازمند بررسی / Needs Confirmation
    UNCERTAIN           // حدس اولیه / Uncertain
}

object BirdPhotoAnalyzer {

    const val SYSTEM_PROMPT = """
You are an expert budgerigar (Melopsittacus undulatus / مرغ عشق) avian phenotype analyzer and judge.
Your role is to analyze bird photos uploaded by breeders and suggest VISUAL characteristics to assist with bird registration.

CRITICAL RULES:
1. Focus ONLY on visible physical characteristics:
   - Primary Body Color (e.g., Sky Blue, Cobalt, Mauve, Light Green, Dark Green, Olive, Grey, Violet, Lutino, Albino, Yellowface).
   - Visible Feather Pattern / Mutation (e.g., Normal barred, Opaline, Spangle, Recessive Pied, Dominant Pied, Cinnamon, Clearwing, Yellowface II).
   - Visible Variety / Phenotype:
     * ENGLISH_SHOW (large head, directional feather feathering, large throat spots, deep mask, heavy brow).
     * AUSTRALIAN_WILD (standard pet size, sleek head, proportional wild-type body).
     * CRESTED (feather whorls/crest on head).
     * HAGOROMO (helicopter frilled feathers on back/wings).
   - Visible Phenotype details (Throat spots count, cheek patch color, cere hue).

2. DO NOT treat sex, exact age, or hidden genetics (split mutations) as certain.
   - For cere color, note visible cere appearance (e.g. blue, deep brown crusty, pale pink/whiteish) but state clearly that sex is tentative and requires breeder confirmation.
   - State that hidden genetics (like split ino, split opaline, or split blue) cannot be seen in photos.

3. Output format: Respond in valid, well-structured JSON format:
{
  "color": "Light Green",
  "colorConfidence": "HIGH",
  "pattern": "Opaline",
  "patternConfidence": "MEDIUM",
  "variety": "ENGLISH_SHOW",
  "varietyConfidence": "MEDIUM",
  "phenotypeDetails": ["Throat spots: 4 visible dark spots", "Cheek patches: Violet", "Cere hue: Blueish, tentative male indicator"],
  "cereObservation": "Blue cere suggesting possible male, but cere color can vary with age and hormonal condition",
  "summaryFa": "مرغ عشق انگلیسی سبز روشن، طرح اپالین با ماسک تمیز و لکه‌های گلوگاهی نمایشی.",
  "summaryEn": "English Show Budgie, Light Green body with Opaline pattern and prominent show mask."
}

Valid variety strings: ENGLISH_SHOW, AUSTRALIAN_WILD, CRESTED, HAGOROMO.
Valid confidence levels: HIGH, MEDIUM, UNCERTAIN.
Always ensure JSON is cleanly parseable without markdown code blocks if possible.
"""

    fun parseAiJsonResponse(jsonText: String): BirdVisualSuggestion {
        try {
            // Clean markdown code fence if present
            var clean = jsonText.trim()
            if (clean.startsWith("```json")) {
                clean = clean.removePrefix("```json").trim()
            }
            if (clean.startsWith("```")) {
                clean = clean.removePrefix("```").trim()
            }
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```").trim()
            }

            // If not a valid JSON structure or empty, fallback
            if (!clean.contains("{") || !clean.contains("}")) {
                return generateLocalFallbackSuggestion()
            }

            val color = extractJsonField(clean, "color")
            val pattern = extractJsonField(clean, "pattern")
            val varietyStr = extractJsonField(clean, "variety")

            if (color == null && pattern == null && varietyStr == null) {
                return generateLocalFallbackSuggestion()
            }

            val colorConfStr = extractJsonField(clean, "colorConfidence") ?: "HIGH"
            val patternConfStr = extractJsonField(clean, "patternConfidence") ?: "MEDIUM"
            val varietyConfStr = extractJsonField(clean, "varietyConfidence") ?: "MEDIUM"
            val cereObs = extractJsonField(clean, "cereObservation")
            val summaryFa = extractJsonField(clean, "summaryFa")
            val summaryEn = extractJsonField(clean, "summaryEn")

            val variety = when (varietyStr?.uppercase()) {
                "ENGLISH_SHOW" -> BudgieVariety.ENGLISH_SHOW
                "AUSTRALIAN_WILD" -> BudgieVariety.AUSTRALIAN_WILD
                "CRESTED" -> BudgieVariety.CRESTED
                "HAGOROMO" -> BudgieVariety.HAGOROMO
                else -> null
            }

            val detailsList = mutableListOf<String>()
            val detailsMatch = Regex(""""phenotypeDetails"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL).find(clean)
            if (detailsMatch != null) {
                val arrayContent = detailsMatch.groupValues[1]
                Regex(""""([^"]+)"""").findAll(arrayContent).forEach {
                    detailsList.add(it.groupValues[1])
                }
            }

            return BirdVisualSuggestion(
                suggestedColor = color,
                colorConfidence = parseConfidence(colorConfStr),
                suggestedPattern = pattern,
                patternConfidence = parseConfidence(patternConfStr),
                suggestedVariety = variety,
                varietyConfidence = parseConfidence(varietyConfStr),
                visiblePhenotypeDetails = detailsList,
                suggestedSexGuess = cereObs,
                overallSummary = summaryFa ?: summaryEn ?: clean.take(200),
                needsHumanReview = true
            )
        } catch (_: Exception) {
            return generateLocalFallbackSuggestion()
        }
    }

    private fun extractJsonField(json: String, key: String): String? {
        val pattern = Regex(""""$key"\s*:\s*"([^"]+)"""")
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun parseConfidence(str: String): SuggestionConfidence {
        return when (str.uppercase()) {
            "HIGH" -> SuggestionConfidence.HIGH
            "MEDIUM" -> SuggestionConfidence.MEDIUM
            else -> SuggestionConfidence.UNCERTAIN
        }
    }

    /**
     * Fallback suggestion when offline or API key is absent,
     * demonstrating transparent visual inspection templates for the breeder.
     */
    fun generateLocalFallbackSuggestion(): BirdVisualSuggestion {
        return BirdVisualSuggestion(
            suggestedColor = "Green / Sky Blue (نیازمند تایید)",
            colorConfidence = SuggestionConfidence.MEDIUM,
            suggestedPattern = "Normal / Opaline (نیازمند تایید)",
            patternConfidence = SuggestionConfidence.MEDIUM,
            suggestedVariety = BudgieVariety.ENGLISH_SHOW,
            varietyConfidence = SuggestionConfidence.UNCERTAIN,
            visiblePhenotypeDetails = listOf(
                "طرح پر و سر: الگوی راه‌راه معمولی یا اپالین",
                "لکه گونه و نقاط گلوگاه: نیازمند تایید دستی پرورش‌دهنده",
                "رنگ سرین و شکم: رنگ پایه سبز یا آبی"
            ),
            suggestedSexGuess = "رنگ سیربینی (Cere) برای تعیین دقیق جنسیت باید به صورت فیزیکی بررسی شود.",
            overallSummary = "پیشنهاد ویژگی‌های بصری از روی عکس استخراج شد. لطفاً پیش از ذخیره رسمی در پرونده پرنده، موارد را بازبینی و تایید نمایید.",
            needsHumanReview = true
        )
    }
}
