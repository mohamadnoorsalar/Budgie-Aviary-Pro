package com.example.core.genetics

import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety

/**
 * Base color series in Budgerigars
 */
enum class BaseColorSeries(val displayName: String, val displayNameFa: String) {
    GREEN("Green Series (Dominant)", "سری سبز (غالب)"),
    BLUE("Blue Series (Recessive)", "سری آبی (مغلوب)")
}

/**
 * Dark Factor (Incomplete Dominant)
 */
enum class DarkFactorCount(val count: Int, val blueName: String, val greenName: String) {
    ZERO(0, "Sky Blue", "Light Green"),
    ONE(1, "Cobalt", "Dark Green"),
    TWO(2, "Mauve", "Olive")
}

/**
 * Sex-linked trait expression
 */
enum class SexLinkedAlleleState {
    NORMAL,
    SPLIT_CARRIER, // Only possible in males (ZZ)
    VISUAL
}

/**
 * Autosomal recessive trait state
 */
enum class AutosomalAlleleState {
    NORMAL,
    SPLIT_CARRIER,
    VISUAL
}

/**
 * Autosomal dominant / incomplete dominant trait state
 */
enum class DominantFactorState {
    NONE,
    SINGLE_FACTOR,
    DOUBLE_FACTOR
}

/**
 * Parsed genotype profile for a bird
 */
data class BirdGenotype(
    val ringNumber: String,
    val gender: BirdGender,
    val baseSeries: BaseColorSeries,
    val isSplitBlue: Boolean, // Green bird carrying blue allele (B/b)
    val darkFactors: Int, // 0, 1, 2
    val isViolet: Boolean,
    val isGrey: Boolean,
    val yellowFaceType: String? = null, // None, "YellowFace I", "YellowFace II", "Goldenface"
    val opaline: SexLinkedAlleleState = SexLinkedAlleleState.NORMAL,
    val ino: SexLinkedAlleleState = SexLinkedAlleleState.NORMAL,
    val cinnamon: SexLinkedAlleleState = SexLinkedAlleleState.NORMAL,
    val spangle: DominantFactorState = DominantFactorState.NONE,
    val dominantPied: Boolean = false,
    val recessivePied: AutosomalAlleleState = AutosomalAlleleState.NORMAL,
    val dilution: String = "NONE", // NONE, GREYWING, CLEARWING, DILUTE
    val crestedType: String = "NONE" // NONE, TUFTED, HALF_CIRCULAR, FULL_CIRCULAR, HAGOROMO
)

/**
 * Probabilistic offspring outcome.
 * Every prediction is explicitly framed as a probability, not a guarantee.
 */
data class PhenotypePrediction(
    val id: String,
    val phenotypeNameEn: String,
    val phenotypeNameFa: String,
    val baseColor: String,
    val visualMutations: List<String>,
    val splitCarriers: List<String>,
    val targetSex: BirdGender, // MALE, FEMALE, UNKNOWN (Either)
    val probability: Double, // Value between 0.0 and 1.0 (e.g. 0.25 = 25%)
    val probabilityPercent: String, // Formatted e.g. "25.0%"
    val biologicalNotes: String
)

/**
 * Complete offspring prediction calculation result
 */
data class OffspringPredictionResult(
    val sireRing: String,
    val damRing: String,
    val outcomes: List<PhenotypePrediction>,
    val maleOutcomes: List<PhenotypePrediction>,
    val femaleOutcomes: List<PhenotypePrediction>,
    val colorDistribution: Map<String, Double>, // e.g. "Cobalt Blue" -> 0.50
    val carrierSummary: List<String>,
    val totalProbability: Double,
    val scientificDisclaimer: String = "Probabilities are calculated using Mendelian and sex-linked inheritance laws per egg. Biological outcomes represent statistical chances and not fixed guarantees of any single clutch."
)

/**
 * Comparison of predicted vs actual recorded offspring
 */
data class PredictionVsActualItem(
    val phenotypeName: String,
    val expectedProbabilityPercent: Double,
    val actualCount: Int,
    val totalHatched: Int,
    val actualPercentage: Double,
    val difference: Double // actual - expected
)

data class PredictionVsActualReport(
    val pairId: Long?,
    val sireRing: String,
    val damRing: String,
    val totalRecordedOffspring: Int,
    val items: List<PredictionVsActualItem>,
    val sampleSizeSummary: String
)

/**
 * Relationship degrees between candidate breeding pairs
 */
enum class RelationshipDegree(val labelEn: String, val labelFa: String, val approxKinship: Double) {
    UNRELATED("Unrelated (Outcross)", "غیرخویشاوند (برون‌آمیزی)", 0.0),
    DISTANT("Distant Ancestry", "اجداد دور", 0.03125),
    SECOND_COUSIN("Second Cousins", "پسر/دختر عموی دوم", 0.03125),
    FIRST_COUSIN("First Cousins", "پسر/دختر عمو / خاله / دایی", 0.125),
    HALF_SIBLING("Half-Siblings (Shared 1 Parent)", "خواهر/برادر ناتنی", 0.25),
    GRANDPARENT_GRANDCHILD("Grandparent / Grandchild", "پدربزرگ-مادربزرگ / نوه", 0.25),
    FULL_SIBLING("Full Siblings (Shared Sire & Dam)", "خواهر/برادر تنی کامل", 0.50),
    PARENT_CHILD("Parent / Offspring (Direct)", "والد / فرزند مستقیم", 0.50),
    IDENTICAL("Identical Clones", "همسان", 1.0)
}

enum class InbreedingWarningLevel {
    SAFE,
    CAUTION_LINEBREEDING,
    HIGH_RISK,
    CRITICAL_WARNING
}

data class CommonAncestorRecord(
    val ancestorRing: String,
    val ancestorName: String?,
    val generationDepthPaternal: Int,
    val generationDepthMaternal: Int,
    val pathDescription: String
)

data class RelatednessAnalysisReport(
    val sireRing: String,
    val damRing: String,
    val relationshipCoefficientR: Double, // e.g. 0.50 for full siblings
    val inbreedingCoefficientF: Double, // e.g. 0.25 for offspring of full siblings
    val degree: RelationshipDegree,
    val warningLevel: InbreedingWarningLevel,
    val warningTitle: String,
    val warningMessage: String,
    val commonAncestors: List<CommonAncestorRecord>,
    val biologicalHazards: List<String>
)

/**
 * Goal for breeding generation planning
 */
data class GenerationBreedingGoal(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val targetDescription: String,
    val requiredBaseSeries: BaseColorSeries,
    val requiredDarkFactors: Int? = null,
    val requiredVisualMutations: List<String>,
    val requiredTraits: List<String> = emptyList()
)

data class DirectPairCandidate(
    val sireRing: String,
    val sireName: String?,
    val damRing: String,
    val damName: String?,
    val matchingPhenotype: String,
    val probabilityPercent: Double,
    val notes: String
)

data class TwoStepBreedingStep(
    val generationIndex: Int, // 1 or 2
    val stepTitle: String,
    val sireRing: String,
    val damRing: String,
    val targetOffspringToKeep: String,
    val projectedProbability: Double,
    val instructions: String
)

data class MultiStepBreedingRoadmap(
    val goalId: String,
    val goalTitle: String,
    val steps: List<TwoStepBreedingStep>,
    val finalOffspringProbability: Double
)

data class GenerationPlanningResult(
    val goal: GenerationBreedingGoal,
    val directMatches: List<DirectPairCandidate>,
    val twoStepRoadmaps: List<MultiStepBreedingRoadmap>
)

/**
 * Milestone in the Genetics Developmental Calendar
 */
enum class GeneticMilestoneType(val titleEn: String, val titleFa: String) {
    CANDLING("Day 6-8: Egg Candling & Embryo Viability", "روز ۶ تا ۸: کندلینگ و مشاهده رگ‌های جنین"),
    HATCH_EYE_CHECK("Day 0-2 Post-Hatch: Eye Pigment Inspection", "روز ۰ تا ۲ تولد: رنگ چشم (قرمز آلبینو/اینو در برابر مشکی عادی)"),
    DOWN_FEATHER_CHECK("Day 5-8 Post-Hatch: Down Color (Base Series)", "روز ۵ تا ۸: رنگ کرک (سفید=سری آبی، طوسی=سری سبز)"),
    CLOSED_RING_BANDING("Day 6-8 Post-Hatch: Closed Leg Ring Banding", "روز ۶ تا ۸: حلقه‌گذاری بسته پای جوجه"),
    PIN_FEATHER_CHECK("Day 14-21 Post-Hatch: Wing & Mantle Feather Pin Eruption", "روز ۱۴ تا ۲۱: باز شدن غلاف پرها و تشخیص اسپنگل/اوپالین"),
    WEANING_PHENOTYPE("Day 30-35 Post-Hatch: Weaning & Final Phenotype Confirmation", "روز ۳۰ تا ۳۵: دان‌خوری کامل و تایید فنوتیپ نهایی")
}

data class GeneticsCalendarEvent(
    val id: String,
    val pairId: Long,
    val pairDescription: String,
    val clutchNumber: Int,
    val eggNumber: Int?,
    val chickRingNumber: String?,
    val milestoneType: GeneticMilestoneType,
    val dueDateMillis: Long,
    val geneticSignificance: String,
    val isCompleted: Boolean = false,
    val notes: String? = null
)
