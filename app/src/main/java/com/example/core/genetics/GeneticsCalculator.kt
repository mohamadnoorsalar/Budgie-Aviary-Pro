package com.example.core.genetics

import com.example.core.common.BirdGender
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.relation.PairWithBreedingDetails
import java.util.Locale
import java.util.UUID
import kotlin.math.pow

object GeneticsCalculator {

    val STANDARD_BREEDING_GOALS = listOf(
        GenerationBreedingGoal(
            id = "goal_rainbow",
            titleEn = "Rainbow Budgie (Opaline + Clearwing + YF II + Blue)",
            titleFa = "مرغ عشق رنگین‌کمانی (اوپالین + کلیروینگ + زردچهره ۲ + سری آبی)",
            targetDescription = "Exquisite combination of Opaline, Clearwing, YellowFace Type II on a Sky Blue or Cobalt body.",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredVisualMutations = listOf("Opaline", "Clearwing", "YellowFace")
        ),
        GenerationBreedingGoal(
            id = "goal_df_spangle_white",
            titleEn = "Double Factor Spangle (Pure White / Black Eye)",
            titleFa = "دابل فاکتور اسپنگل (سفید خالص چشم‌مشکی)",
            targetDescription = "Double factor spangle in Blue series resulting in total suppression of melanin: pure white plumage with dark eyes.",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredVisualMutations = listOf("Double Factor Spangle")
        ),
        GenerationBreedingGoal(
            id = "goal_df_spangle_yellow",
            titleEn = "Double Factor Spangle (Pure Buttercup Yellow)",
            titleFa = "دابل فاکتور اسپنگل (زرد خالص چشم‌مشکی)",
            targetDescription = "Double factor spangle in Green series creating an all-yellow bird without wing markings.",
            requiredBaseSeries = BaseColorSeries.GREEN,
            requiredVisualMutations = listOf("Double Factor Spangle")
        ),
        GenerationBreedingGoal(
            id = "goal_albino_hen",
            titleEn = "Albino Hen (Blue Series + Ino)",
            titleFa = "ماده آلبینو (سری آبی + اینو چشم قرمز)",
            targetDescription = "Pure white plumage with ruby red eyes and pink cere/legs via sex-linked Ino gene.",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredVisualMutations = listOf("Albino / Ino")
        ),
        GenerationBreedingGoal(
            id = "goal_lutino_cock",
            titleEn = "Lutino Cock (Green Series + Ino)",
            titleFa = "نر لوتینو (سری سبز + اینو چشم قرمز)",
            targetDescription = "Pure buttercup yellow cock with intense red eyes and pink feet.",
            requiredBaseSeries = BaseColorSeries.GREEN,
            requiredVisualMutations = listOf("Lutino / Ino")
        ),
        GenerationBreedingGoal(
            id = "goal_cobalt_violet_opaline",
            titleEn = "Cobalt Violet Opaline",
            titleFa = "کبالت وایولت اوپالین (بنفش سلطنتی اوپالین)",
            targetDescription = "Single dark factor Blue combined with dominant Violet factor and Opaline mantle.",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredDarkFactors = 1,
            requiredVisualMutations = listOf("Opaline", "Violet")
        ),
        GenerationBreedingGoal(
            id = "goal_recessive_pied_blue",
            titleEn = "Danish Recessive Pied Blue (Harlequin)",
            titleFa = "ابلق هلندی مغلوب آبی (هارلیکوین)",
            targetDescription = "Rich pied patches, solid dark plum eyes with no white iris ring throughout life.",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredVisualMutations = listOf("Recessive Pied")
        )
    )

    /**
     * Parses a BirdEntity and its associated BirdGeneticsEntity into a strongly typed BirdGenotype
     */
    fun parseGenotype(bird: BirdEntity, genetics: BirdGeneticsEntity?): BirdGenotype {
        val visual = (genetics?.visualMutations ?: bird.mutation).lowercase(Locale.ROOT)
        val split = (genetics?.splitMutations ?: "").lowercase(Locale.ROOT)
        val baseSeriesStr = (genetics?.baseSeries ?: bird.color).uppercase(Locale.ROOT)

        val isBlue = baseSeriesStr.contains("BLUE") ||
                bird.color.uppercase(Locale.ROOT).contains("BLUE") ||
                bird.color.uppercase(Locale.ROOT).contains("COBALT") ||
                bird.color.uppercase(Locale.ROOT).contains("MAUVE") ||
                bird.color.uppercase(Locale.ROOT).contains("SKY") ||
                visual.contains("albino")

        val baseSeries = if (isBlue) BaseColorSeries.BLUE else BaseColorSeries.GREEN
        val isSplitBlue = !isBlue && (split.contains("blue") || split.contains("آبی"))

        // Dark factors: 0 = Light/Sky, 1 = Cobalt/Dark Green, 2 = Mauve/Olive
        val darkFactors = when {
            genetics != null && genetics.darkFactors in 0..2 -> genetics.darkFactors
            bird.color.contains("Mauve", ignoreCase = true) || bird.color.contains("Olive", ignoreCase = true) -> 2
            bird.color.contains("Cobalt", ignoreCase = true) || bird.color.contains("Dark Green", ignoreCase = true) -> 1
            else -> 0
        }

        val isViolet = genetics?.violetFactor == true || visual.contains("violet") || bird.color.contains("violet", ignoreCase = true)
        val isGrey = genetics?.greyFactor == true || visual.contains("grey") || bird.color.contains("grey", ignoreCase = true)

        val yellowFaceType = when {
            !genetics?.yellowFaceType.isNullOrBlank() -> genetics?.yellowFaceType
            visual.contains("goldenface") -> "Goldenface"
            visual.contains("yellowface ii") || visual.contains("yf2") -> "YellowFace II"
            visual.contains("yellowface") || visual.contains("yf1") -> "YellowFace I"
            else -> null
        }

        // Sex-linked genes:
        val opalineState = when {
            genetics?.opalineFactor == true || visual.contains("opaline") || visual.contains("اوپالین") -> SexLinkedAlleleState.VISUAL
            split.contains("opaline") || split.contains("اوپالین") -> {
                if (bird.gender == BirdGender.FEMALE) SexLinkedAlleleState.NORMAL else SexLinkedAlleleState.SPLIT_CARRIER
            }
            else -> SexLinkedAlleleState.NORMAL
        }

        val inoState = when {
            genetics?.inoFactor == true || visual.contains("ino") || visual.contains("lutino") || visual.contains("albino") || visual.contains("لوتینو") || visual.contains("آلبینو") -> SexLinkedAlleleState.VISUAL
            split.contains("ino") || split.contains("lutino") || split.contains("albino") -> {
                if (bird.gender == BirdGender.FEMALE) SexLinkedAlleleState.NORMAL else SexLinkedAlleleState.SPLIT_CARRIER
            }
            else -> SexLinkedAlleleState.NORMAL
        }

        val cinnamonState = when {
            genetics?.cinnamonFactor == true || visual.contains("cinnamon") || visual.contains("دارچینی") -> SexLinkedAlleleState.VISUAL
            split.contains("cinnamon") || split.contains("دارچینی") -> {
                if (bird.gender == BirdGender.FEMALE) SexLinkedAlleleState.NORMAL else SexLinkedAlleleState.SPLIT_CARRIER
            }
            else -> SexLinkedAlleleState.NORMAL
        }

        // Spangle
        val spangleState = when {
            genetics?.spangleFactor == "DOUBLE_FACTOR" || visual.contains("df spangle") || visual.contains("double factor spangle") -> DominantFactorState.DOUBLE_FACTOR
            genetics?.spangleFactor == "SINGLE_FACTOR" || visual.contains("spangle") || visual.contains("اسپنگل") -> DominantFactorState.SINGLE_FACTOR
            else -> DominantFactorState.NONE
        }

        val dominantPied = genetics?.piebaldType == "DOMINANT_PIED" || visual.contains("dominant pied") || visual.contains("ابلق غالب")
        val recessivePiedState = when {
            genetics?.piebaldType == "RECESSIVE_PIED" || visual.contains("recessive pied") || visual.contains("ابلق مغلوب") -> AutosomalAlleleState.VISUAL
            split.contains("recessive pied") || split.contains("ابلق مغلوب") -> AutosomalAlleleState.SPLIT_CARRIER
            else -> AutosomalAlleleState.NORMAL
        }

        val dilution = when {
            !genetics?.dilution.isNullOrBlank() && genetics?.dilution != "NONE" -> genetics!!.dilution
            visual.contains("clearwing") -> "CLEARWING"
            visual.contains("greywing") -> "GREYWING"
            visual.contains("dilute") -> "DILUTE"
            else -> "NONE"
        }

        return BirdGenotype(
            ringNumber = bird.ringNumber,
            gender = bird.gender,
            baseSeries = baseSeries,
            isSplitBlue = isSplitBlue,
            darkFactors = darkFactors,
            isViolet = isViolet,
            isGrey = isGrey,
            yellowFaceType = yellowFaceType,
            opaline = opalineState,
            ino = inoState,
            cinnamon = cinnamonState,
            spangle = spangleState,
            dominantPied = dominantPied,
            recessivePied = recessivePiedState,
            dilution = dilution,
            crestedType = genetics?.crestedType ?: "NONE"
        )
    }

    /**
     * Calculates probabilistic offspring predictions for a given Sire and Dam.
     * All predictions are mathematically guaranteed to represent probabilities per egg.
     */
    fun predictOffspring(sire: BirdGenotype, dam: BirdGenotype): OffspringPredictionResult {
        // 1. Base Series Punnett square
        // Green (B) dominant to Blue (b)
        val baseSeriesProbabilities: Map<Pair<BaseColorSeries, Boolean>, Double> = when {
            // Blue x Blue -> 100% Blue
            sire.baseSeries == BaseColorSeries.BLUE && dam.baseSeries == BaseColorSeries.BLUE -> {
                mapOf((BaseColorSeries.BLUE to false) to 1.0)
            }
            // Green (homozygous) x Blue -> 100% Green split Blue
            sire.baseSeries == BaseColorSeries.GREEN && !sire.isSplitBlue && dam.baseSeries == BaseColorSeries.BLUE -> {
                mapOf((BaseColorSeries.GREEN to true) to 1.0)
            }
            sire.baseSeries == BaseColorSeries.BLUE && dam.baseSeries == BaseColorSeries.GREEN && !dam.isSplitBlue -> {
                mapOf((BaseColorSeries.GREEN to true) to 1.0)
            }
            // Green (split Blue) x Blue -> 50% Green split Blue, 50% Blue
            sire.baseSeries == BaseColorSeries.GREEN && sire.isSplitBlue && dam.baseSeries == BaseColorSeries.BLUE -> {
                mapOf(
                    (BaseColorSeries.GREEN to true) to 0.50,
                    (BaseColorSeries.BLUE to false) to 0.50
                )
            }
            sire.baseSeries == BaseColorSeries.BLUE && dam.baseSeries == BaseColorSeries.GREEN && dam.isSplitBlue -> {
                mapOf(
                    (BaseColorSeries.GREEN to true) to 0.50,
                    (BaseColorSeries.BLUE to false) to 0.50
                )
            }
            // Green (split Blue) x Green (split Blue) -> 25% Green, 50% Green split Blue, 25% Blue
            sire.isSplitBlue && dam.isSplitBlue -> {
                mapOf(
                    (BaseColorSeries.GREEN to false) to 0.25,
                    (BaseColorSeries.GREEN to true) to 0.50,
                    (BaseColorSeries.BLUE to false) to 0.25
                )
            }
            // One is split Blue, other homozygous Green
            sire.isSplitBlue || dam.isSplitBlue -> {
                mapOf(
                    (BaseColorSeries.GREEN to false) to 0.50,
                    (BaseColorSeries.GREEN to true) to 0.50
                )
            }
            // Both homozygous Green
            else -> {
                mapOf((BaseColorSeries.GREEN to false) to 1.0)
            }
        }

        // 2. Dark Factor distribution (Incomplete Dominance)
        // D1 x D2
        val darkFactorProbabilities: Map<Int, Double> = when {
            sire.darkFactors == 0 && dam.darkFactors == 0 -> mapOf(0 to 1.0)
            (sire.darkFactors == 0 && dam.darkFactors == 1) || (sire.darkFactors == 1 && dam.darkFactors == 0) -> {
                mapOf(0 to 0.50, 1 to 0.50)
            }
            sire.darkFactors == 1 && dam.darkFactors == 1 -> {
                mapOf(0 to 0.25, 1 to 0.50, 2 to 0.25)
            }
            (sire.darkFactors == 0 && dam.darkFactors == 2) || (sire.darkFactors == 2 && dam.darkFactors == 0) -> {
                mapOf(1 to 1.0)
            }
            (sire.darkFactors == 1 && dam.darkFactors == 2) || (sire.darkFactors == 2 && dam.darkFactors == 1) -> {
                mapOf(1 to 0.50, 2 to 0.50)
            }
            sire.darkFactors == 2 && dam.darkFactors == 2 -> mapOf(2 to 1.0)
            else -> mapOf(0 to 1.0)
        }

        // 3. Spangle (Autosomal Incomplete Dominant)
        val spangleProbabilities: Map<DominantFactorState, Double> = when {
            sire.spangle == DominantFactorState.NONE && dam.spangle == DominantFactorState.NONE -> {
                mapOf(DominantFactorState.NONE to 1.0)
            }
            (sire.spangle == DominantFactorState.SINGLE_FACTOR && dam.spangle == DominantFactorState.NONE) ||
                    (sire.spangle == DominantFactorState.NONE && dam.spangle == DominantFactorState.SINGLE_FACTOR) -> {
                mapOf(DominantFactorState.NONE to 0.50, DominantFactorState.SINGLE_FACTOR to 0.50)
            }
            sire.spangle == DominantFactorState.SINGLE_FACTOR && dam.spangle == DominantFactorState.SINGLE_FACTOR -> {
                mapOf(DominantFactorState.NONE to 0.25, DominantFactorState.SINGLE_FACTOR to 0.50, DominantFactorState.DOUBLE_FACTOR to 0.25)
            }
            (sire.spangle == DominantFactorState.DOUBLE_FACTOR && dam.spangle == DominantFactorState.NONE) ||
                    (sire.spangle == DominantFactorState.NONE && dam.spangle == DominantFactorState.DOUBLE_FACTOR) -> {
                mapOf(DominantFactorState.SINGLE_FACTOR to 1.0)
            }
            else -> mapOf(sire.spangle to 0.50, dam.spangle to 0.50)
        }

        // 4. Sex-Linked Genes (Opaline, Ino, Cinnamon)
        // Cock is ZZ, Hen is ZW
        // Returns list of (MaleOutcomes, FemaleOutcomes)
        fun calculateSexLinkedLocus(
            sireState: SexLinkedAlleleState,
            damState: SexLinkedAlleleState,
            traitName: String
        ): Pair<Map<SexLinkedAlleleState, Double>, Map<SexLinkedAlleleState, Double>> {
            // Male Offspring (get one Z from Sire, one Z from Dam)
            // Dam can only give normal Z or mutant Z (she is either VISUAL or NORMAL)
            val maleOutcomes = when {
                sireState == SexLinkedAlleleState.VISUAL && damState == SexLinkedAlleleState.VISUAL -> {
                    mapOf(SexLinkedAlleleState.VISUAL to 1.0)
                }
                sireState == SexLinkedAlleleState.VISUAL && damState != SexLinkedAlleleState.VISUAL -> {
                    mapOf(SexLinkedAlleleState.SPLIT_CARRIER to 1.0)
                }
                sireState == SexLinkedAlleleState.SPLIT_CARRIER && damState == SexLinkedAlleleState.VISUAL -> {
                    mapOf(SexLinkedAlleleState.VISUAL to 0.50, SexLinkedAlleleState.SPLIT_CARRIER to 0.50)
                }
                sireState == SexLinkedAlleleState.SPLIT_CARRIER && damState != SexLinkedAlleleState.VISUAL -> {
                    mapOf(SexLinkedAlleleState.SPLIT_CARRIER to 0.50, SexLinkedAlleleState.NORMAL to 0.50)
                }
                sireState == SexLinkedAlleleState.NORMAL && damState == SexLinkedAlleleState.VISUAL -> {
                    mapOf(SexLinkedAlleleState.SPLIT_CARRIER to 1.0)
                }
                else -> {
                    mapOf(SexLinkedAlleleState.NORMAL to 1.0)
                }
            }

            // Female Offspring (get Z from Sire, W from Dam)
            val femaleOutcomes = when (sireState) {
                SexLinkedAlleleState.VISUAL -> mapOf(SexLinkedAlleleState.VISUAL to 1.0)
                SexLinkedAlleleState.SPLIT_CARRIER -> mapOf(SexLinkedAlleleState.VISUAL to 0.50, SexLinkedAlleleState.NORMAL to 0.50)
                SexLinkedAlleleState.NORMAL -> mapOf(SexLinkedAlleleState.NORMAL to 1.0)
            }

            return maleOutcomes to femaleOutcomes
        }

        val (maleOpaline, femaleOpaline) = calculateSexLinkedLocus(sire.opaline, dam.opaline, "Opaline")
        val (maleIno, femaleIno) = calculateSexLinkedLocus(sire.ino, dam.ino, "Ino")

        // 5. Generate Phenotype Outcome Combinations
        val outcomes = mutableListOf<PhenotypePrediction>()
        val colorDistribution = mutableMapOf<String, Double>()
        val carrierSummary = mutableListOf<String>()

        // Generate gendered outcomes (50% probability male, 50% probability female per egg)
        listOf(BirdGender.MALE to (maleOpaline to maleIno), BirdGender.FEMALE to (femaleOpaline to femaleIno)).forEach { (gender, sexLinkedMaps) ->
            val (opalineDist, inoDist) = sexLinkedMaps
            val genderFactor = 0.50

            baseSeriesProbabilities.forEach { (baseSeriesPair, baseProb) ->
                val (baseSeries, isSplit) = baseSeriesPair

                darkFactorProbabilities.forEach { (darkCount, darkProb) ->
                    val colorName = when (baseSeries) {
                        BaseColorSeries.BLUE -> when (darkCount) {
                            0 -> "Sky Blue"
                            1 -> "Cobalt"
                            else -> "Mauve"
                        }
                        BaseColorSeries.GREEN -> when (darkCount) {
                            0 -> "Light Green"
                            1 -> "Dark Green"
                            else -> "Olive"
                        }
                    }

                    spangleProbabilities.forEach { (spangleState, spangleProb) ->
                        opalineDist.forEach { (opState, opProb) ->
                            inoDist.forEach { (inoState, inoProb) ->
                                val prob = genderFactor * baseProb * darkProb * spangleProb * opProb * inoProb
                                if (prob > 0.005) { // Filter out negligible mathematical fractions
                                    val visualMutations = mutableListOf<String>()
                                    val splitMutations = mutableListOf<String>()

                                    if (spangleState == DominantFactorState.DOUBLE_FACTOR) {
                                        visualMutations.add("Double Factor Spangle")
                                    } else if (spangleState == DominantFactorState.SINGLE_FACTOR) {
                                        visualMutations.add("Spangle")
                                    }

                                    if (inoState == SexLinkedAlleleState.VISUAL) {
                                        visualMutations.add(if (baseSeries == BaseColorSeries.BLUE) "Albino" else "Lutino")
                                    } else if (inoState == SexLinkedAlleleState.SPLIT_CARRIER) {
                                        splitMutations.add("Split Ino")
                                    }

                                    if (opState == SexLinkedAlleleState.VISUAL) {
                                        visualMutations.add("Opaline")
                                    } else if (opState == SexLinkedAlleleState.SPLIT_CARRIER) {
                                        splitMutations.add("Split Opaline")
                                    }

                                    if (isSplit) {
                                        splitMutations.add("Split Blue")
                                    }

                                    // Body color summary
                                    val finalColor = if (inoState == SexLinkedAlleleState.VISUAL) {
                                        if (baseSeries == BaseColorSeries.BLUE) "Pure White (Albino)" else "Pure Yellow (Lutino)"
                                    } else if (spangleState == DominantFactorState.DOUBLE_FACTOR) {
                                        if (baseSeries == BaseColorSeries.BLUE) "White (DF Spangle)" else "Yellow (DF Spangle)"
                                    } else {
                                        colorName
                                    }

                                    // Build English and Persian names
                                    val mutStr = if (visualMutations.isEmpty()) "Normal" else visualMutations.joinToString(" ")
                                    val genderStrEn = if (gender == BirdGender.MALE) "Cock" else "Hen"
                                    val genderStrFa = if (gender == BirdGender.MALE) "نر" else "ماده"

                                    val nameEn = "$finalColor $mutStr $genderStrEn"
                                    val nameFa = "$genderStrFa $finalColor ${visualMutations.joinToString(" ")}"

                                    val probPercent = String.format(Locale.US, "%.1f%%", prob * 100.0)

                                    outcomes.add(
                                        PhenotypePrediction(
                                            id = UUID.randomUUID().toString(),
                                            phenotypeNameEn = nameEn,
                                            phenotypeNameFa = nameFa,
                                            baseColor = finalColor,
                                            visualMutations = visualMutations,
                                            splitCarriers = splitMutations,
                                            targetSex = gender,
                                            probability = prob,
                                            probabilityPercent = probPercent,
                                            biologicalNotes = buildBiologicalNote(gender, opState, inoState, isSplit)
                                        )
                                    )

                                    colorDistribution[finalColor] = (colorDistribution[finalColor] ?: 0.0) + prob
                                }
                            }
                        }
                    }
                }
            }
        }

        // Aggregate carrier notes
        if (sire.ino == SexLinkedAlleleState.VISUAL && dam.ino != SexLinkedAlleleState.VISUAL) {
            carrierSummary.add("100% of male offspring will be split (silent carriers) for Ino")
            carrierSummary.add("100% of female offspring will be visual Ino (Albino/Lutino)")
        } else if (sire.ino == SexLinkedAlleleState.SPLIT_CARRIER) {
            carrierSummary.add("50% of female offspring will be visual Ino, 50% normal")
            carrierSummary.add("50% of male offspring will carry split Ino")
        }

        if (sire.opaline == SexLinkedAlleleState.VISUAL && dam.opaline != SexLinkedAlleleState.VISUAL) {
            carrierSummary.add("100% of female offspring will be visual Opaline")
            carrierSummary.add("100% of male offspring will be split for Opaline")
        }

        if (sire.isSplitBlue || dam.isSplitBlue) {
            carrierSummary.add("Green offspring have 50-66% probability of carrying hidden split Blue allele")
        }

        // Sort outcomes descending by probability
        val sortedOutcomes = outcomes.sortedByDescending { it.probability }

        return OffspringPredictionResult(
            sireRing = sire.ringNumber,
            damRing = dam.ringNumber,
            outcomes = sortedOutcomes,
            maleOutcomes = sortedOutcomes.filter { it.targetSex == BirdGender.MALE },
            femaleOutcomes = sortedOutcomes.filter { it.targetSex == BirdGender.FEMALE },
            colorDistribution = colorDistribution,
            carrierSummary = carrierSummary.distinct(),
            totalProbability = outcomes.sumOf { it.probability }
        )
    }

    private fun buildBiologicalNote(
        gender: BirdGender,
        opaline: SexLinkedAlleleState,
        ino: SexLinkedAlleleState,
        isSplitBlue: Boolean
    ): String {
        val notes = mutableListOf<String>()
        if (gender == BirdGender.FEMALE && ino == SexLinkedAlleleState.VISUAL) {
            notes.add("Sex-linked Ino transmitted from Sire's Z chromosome")
        }
        if (gender == BirdGender.MALE && ino == SexLinkedAlleleState.SPLIT_CARRIER) {
            notes.add("Male carries unexpressed Ino allele (split)")
        }
        if (isSplitBlue) {
            notes.add("Heterozygous carrier for Blue series (B/b)")
        }
        return if (notes.isEmpty()) "Standard Mendelian assortment" else notes.joinToString("; ")
    }

    /**
     * Compares theoretical predicted probabilities with real recorded offspring from the database.
     * Rule: Do not invent genetic results. Use recorded genetic information only.
     */
    fun comparePredictionVsActual(
        prediction: OffspringPredictionResult,
        recordedOffspring: List<BirdEntity>
    ): PredictionVsActualReport {
        val total = recordedOffspring.size
        if (total == 0) {
            return PredictionVsActualReport(
                pairId = null,
                sireRing = prediction.sireRing,
                damRing = prediction.damRing,
                totalRecordedOffspring = 0,
                items = emptyList(),
                sampleSizeSummary = "No actual offspring registered in database yet for this pairing. Predicted probabilities reflect theoretical expectations per clutch."
            )
        }

        // Group actual birds by color / mutation similarity
        val actualPhenotypeCounts = mutableMapOf<String, Int>()
        recordedOffspring.forEach { bird ->
            val key = "${bird.color} ${bird.mutation}".trim()
            actualPhenotypeCounts[key] = (actualPhenotypeCounts[key] ?: 0) + 1
        }

        val items = mutableListOf<PredictionVsActualItem>()

        // Match against top predictions
        val topPredictions = prediction.outcomes.take(6)
        topPredictions.forEach { pred ->
            val matchingCount = recordedOffspring.count { bird ->
                val birdDesc = "${bird.color} ${bird.mutation}".lowercase(Locale.ROOT)
                val predDesc = pred.phenotypeNameEn.lowercase(Locale.ROOT)
                birdDesc.contains(pred.baseColor.lowercase(Locale.ROOT)) ||
                        predDesc.contains(bird.color.lowercase(Locale.ROOT))
            }

            val actualPercent = (matchingCount.toDouble() / total) * 100.0
            val expectedPercent = pred.probability * 100.0

            items.add(
                PredictionVsActualItem(
                    phenotypeName = pred.phenotypeNameEn,
                    expectedProbabilityPercent = expectedPercent,
                    actualCount = matchingCount,
                    totalHatched = total,
                    actualPercentage = actualPercent,
                    difference = actualPercent - expectedPercent
                )
            )
        }

        val summary = "Based on $total recorded offspring from this pair. In small sample sizes (under 30-50 eggs), empirical clutch distributions naturally fluctuate around Mendelian probabilities."

        return PredictionVsActualReport(
            pairId = null,
            sireRing = prediction.sireRing,
            damRing = prediction.damRing,
            totalRecordedOffspring = total,
            items = items,
            sampleSizeSummary = summary
        )
    }

    /**
     * Relatedness & Inbreeding Analysis:
     * Calculates Wright's Inbreeding Coefficient (F) and Relationship Coefficient (R)
     * by recursively traversing ancestry trees up to 4 generations deep.
     */
    fun calculateRelatedness(
        sireRing: String,
        damRing: String,
        allBirds: List<BirdEntity>,
        allPedigrees: List<PedigreeRecordEntity>
    ): RelatednessAnalysisReport {
        if (sireRing.isBlank() || damRing.isBlank()) {
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 0.0,
                inbreedingCoefficientF = 0.0,
                degree = RelationshipDegree.UNRELATED,
                warningLevel = InbreedingWarningLevel.SAFE,
                warningTitle = "No Pedigree Data",
                warningMessage = "Select both Sire and Dam to analyze pedigree kinship and inbreeding risks.",
                commonAncestors = emptyList(),
                biologicalHazards = emptyList()
            )
        }

        // Quick check: identical bird ring (accidental self-pairing)
        if (sireRing == damRing) {
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 1.0,
                inbreedingCoefficientF = 0.50,
                degree = RelationshipDegree.IDENTICAL,
                warningLevel = InbreedingWarningLevel.CRITICAL_WARNING,
                warningTitle = "CRITICAL: Self-Pairing Detected",
                warningMessage = "Sire and Dam have identical ring numbers ($sireRing). Self-pairing is biologically impossible in budgerigars.",
                commonAncestors = emptyList(),
                biologicalHazards = listOf("Biological impossibility: same individual selected for both sexes")
            )
        }

        val birdMap = allBirds.associateBy { it.ringNumber }
        val pedigreeMap = allPedigrees.associateBy { it.birdRingNumber }

        // Trace ancestors for a bird up to 4 generations
        // Returns Map<AncestorRing, GenerationDepth> (1 = Parent, 2 = Grandparent, 3 = Great-Grandparent, 4 = Great-Great)
        fun traceAncestors(ring: String, currentDepth: Int = 1, maxDepth: Int = 4, visited: MutableSet<String> = mutableSetOf()): Map<String, Int> {
            if (currentDepth > maxDepth || ring in visited) return emptyMap()
            visited.add(ring)

            val ancestors = mutableMapOf<String, Int>()
            val bird = birdMap[ring]
            val ped = pedigreeMap[ring]

            val sire = ped?.sireRing ?: bird?.fatherRing
            val dam = ped?.damRing ?: bird?.motherRing

            if (!sire.isNullOrBlank()) {
                ancestors[sire] = minOf(ancestors[sire] ?: Int.MAX_VALUE, currentDepth)
                val sub = traceAncestors(sire, currentDepth + 1, maxDepth, visited)
                sub.forEach { (a, d) -> ancestors[a] = minOf(ancestors[a] ?: Int.MAX_VALUE, d) }
            }

            if (!dam.isNullOrBlank()) {
                ancestors[dam] = minOf(ancestors[dam] ?: Int.MAX_VALUE, currentDepth)
                val sub = traceAncestors(dam, currentDepth + 1, maxDepth, visited)
                sub.forEach { (a, d) -> ancestors[a] = minOf(ancestors[a] ?: Int.MAX_VALUE, d) }
            }

            // Also check pedigree grandsires if bird entity parents weren't populated
            if (currentDepth == 1 && ped != null) {
                listOfNotNull(ped.paternalGrandsire, ped.paternalGranddam, ped.maternalGrandsire, ped.maternalGranddam).forEach { gRing ->
                    if (gRing.isNotBlank() && gRing != "-") {
                        ancestors[gRing] = minOf(ancestors[gRing] ?: Int.MAX_VALUE, 2)
                    }
                }
            }

            return ancestors
        }

        // Direct parent-child check
        val sireBird = birdMap[sireRing]
        val damBird = birdMap[damRing]
        val isSireChildOfDam = sireBird?.motherRing == damRing
        val isDamChildOfSire = damBird?.fatherRing == sireRing

        if (isSireChildOfDam || isDamChildOfSire) {
            val common = listOf(
                CommonAncestorRecord(
                    ancestorRing = if (isSireChildOfDam) damRing else sireRing,
                    ancestorName = if (isSireChildOfDam) damBird?.name else sireBird?.name,
                    generationDepthPaternal = if (isSireChildOfDam) 1 else 0,
                    generationDepthMaternal = if (isSireChildOfDam) 0 else 1,
                    pathDescription = "Direct Parent-Offspring relationship"
                )
            )
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 0.50,
                inbreedingCoefficientF = 0.25,
                degree = RelationshipDegree.PARENT_CHILD,
                warningLevel = InbreedingWarningLevel.CRITICAL_WARNING,
                warningTitle = "CRITICAL CLOSE-RELATIVE WARNING: Parent-Offspring Pairing",
                warningMessage = "Direct parent-offspring mating results in an inbreeding coefficient F = 25.0%. This severe level of inbreeding is strongly discouraged in standard aviculture.",
                commonAncestors = common,
                biologicalHazards = listOf(
                    "High probability of lethal homozygous recessive alleles manifesting in embryo",
                    "Elevated embryonic death in shell during days 14-18 of incubation",
                    "Significant drop in chick immune vigor and juvenile survivability",
                    "Risk of feather cysts, spinal micro-deformities, and reduced organ volume"
                )
            )
        }

        // Full or half siblings check
        val sireSire = pedigreeMap[sireRing]?.sireRing ?: sireBird?.fatherRing
        val sireDam = pedigreeMap[sireRing]?.damRing ?: sireBird?.motherRing
        val damSire = pedigreeMap[damRing]?.sireRing ?: damBird?.fatherRing
        val damDam = pedigreeMap[damRing]?.damRing ?: damBird?.motherRing

        val sharedFather = !sireSire.isNullOrBlank() && sireSire == damSire
        val sharedMother = !sireDam.isNullOrBlank() && sireDam == damDam

        if (sharedFather && sharedMother) {
            val common = listOfNotNull(
                sireSire?.let { CommonAncestorRecord(it, birdMap[it]?.name, 1, 1, "Shared Father (Sire)") },
                sireDam?.let { CommonAncestorRecord(it, birdMap[it]?.name, 1, 1, "Shared Mother (Dam)") }
            )
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 0.50,
                inbreedingCoefficientF = 0.25,
                degree = RelationshipDegree.FULL_SIBLING,
                warningLevel = InbreedingWarningLevel.CRITICAL_WARNING,
                warningTitle = "CRITICAL CLOSE-RELATIVE WARNING: Full Siblings",
                warningMessage = "Both birds share the exact same Sire ($sireSire) and Dam ($sireDam). Brother-sister pairings yield F = 25.0% and cause immediate inbreeding depression.",
                commonAncestors = common,
                biologicalHazards = listOf(
                    "Severe risk of embryonic mortality and dead-in-shell eggs",
                    "Loss of clutch hatchability (often drops by >40%)",
                    "Accumulation of deleterious recessive mutations across generations",
                    "Weakened flight feather growth and metabolic deficiencies"
                )
            )
        }

        if (sharedFather || sharedMother) {
            val sharedParent = if (sharedFather) sireSire!! else sireDam!!
            val common = listOf(
                CommonAncestorRecord(
                    sharedParent,
                    birdMap[sharedParent]?.name,
                    1,
                    1,
                    if (sharedFather) "Shared Paternal Sire" else "Shared Maternal Dam"
                )
            )
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 0.25,
                inbreedingCoefficientF = 0.125,
                degree = RelationshipDegree.HALF_SIBLING,
                warningLevel = InbreedingWarningLevel.HIGH_RISK,
                warningTitle = "HIGH RISK: Half-Siblings Pairing",
                warningMessage = "These birds share one parent ($sharedParent), giving an inbreeding coefficient F = 12.5%. Close relative pairing requires caution.",
                commonAncestors = common,
                biologicalHazards = listOf(
                    "Moderate risk of expression of unobserved recessive defects",
                    "Reduced clutch size and higher juvenile chick mortality",
                    "Should only be performed by experienced pedigree breeders for strict linebreeding"
                )
            )
        }

        // Deep ancestry search for shared ancestors
        val sireAncestors = traceAncestors(sireRing)
        val damAncestors = traceAncestors(damRing)

        val commonRings = sireAncestors.keys.intersect(damAncestors.keys)

        if (commonRings.isEmpty()) {
            return RelatednessAnalysisReport(
                sireRing = sireRing,
                damRing = damRing,
                relationshipCoefficientR = 0.0,
                inbreedingCoefficientF = 0.0,
                degree = RelationshipDegree.UNRELATED,
                warningLevel = InbreedingWarningLevel.SAFE,
                warningTitle = "Optimal Genetic Outcross (Safe Pairing)",
                warningMessage = "No common ancestors found within 4 recorded ancestral generations. Excellent hybrid vigor and genetic diversity.",
                commonAncestors = emptyList(),
                biologicalHazards = listOf("Optimal vigor: minimal risk of homozygous recessive defects")
            )
        }

        // Calculate Wright's coefficient: F = sum( (1/2)^(depthSire + depthDam + 1) )
        var calculatedF = 0.0
        val commonRecords = mutableListOf<CommonAncestorRecord>()

        commonRings.forEach { ancRing ->
            val dSire = sireAncestors[ancRing] ?: 1
            val dDam = damAncestors[ancRing] ?: 1
            val pathLen = dSire + dDam
            val contribution = (0.5).pow(pathLen.toDouble())
            calculatedF += contribution

            commonRecords.add(
                CommonAncestorRecord(
                    ancestorRing = ancRing,
                    ancestorName = birdMap[ancRing]?.name,
                    generationDepthPaternal = dSire,
                    generationDepthMaternal = dDam,
                    pathDescription = "Ancestor at generation $dSire (sire side) and $dDam (dam side)"
                )
            )
        }

        val relationshipR = calculatedF * 2.0

        val (degree, level, title, msg) = when {
            calculatedF >= 0.125 -> Quadruple(
                RelationshipDegree.FIRST_COUSIN,
                InbreedingWarningLevel.HIGH_RISK,
                "HIGH RISK: Close Inbreeding (F = ${String.format(Locale.US, "%.2f", calculatedF * 100)}%)",
                "Significant pedigree overlap through common ancestors: ${commonRings.joinToString()}. Elevated risk of inbreeding depression."
            )
            calculatedF >= 0.03125 -> Quadruple(
                RelationshipDegree.SECOND_COUSIN,
                InbreedingWarningLevel.CAUTION_LINEBREEDING,
                "CAUTION: Linebreeding Detected (F = ${String.format(Locale.US, "%.2f", calculatedF * 100)}%)",
                "Common ancestors detected in 3rd/4th generations. Acceptable for deliberate linebreeding if ancestors had proven vitality and show standard conformity."
            )
            else -> Quadruple(
                RelationshipDegree.DISTANT,
                InbreedingWarningLevel.SAFE,
                "SAFE: Distant Kinship (F < 3%)",
                "Negligible ancestral overlap. Low risk of deleterious recessive expression."
            )
        }

        return RelatednessAnalysisReport(
            sireRing = sireRing,
            damRing = damRing,
            relationshipCoefficientR = relationshipR,
            inbreedingCoefficientF = calculatedF,
            degree = degree,
            warningLevel = level,
            warningTitle = title,
            warningMessage = msg,
            commonAncestors = commonRecords,
            biologicalHazards = if (level == InbreedingWarningLevel.HIGH_RISK) {
                listOf(
                    "Elevated juvenile mortality in chicks",
                    "Possible unfertilized clutches due to sperm/ovum genetic incompatibility",
                    "Potential loss of size and feather condition"
                )
            } else {
                listOf("Moderate gene fixation: monitor chick feather quality and vigor")
            }
        )
    }

    /**
     * Generation Planning:
     * Analyzes flock to plan how to produce a target breeding goal phenotype
     */
    fun planGeneration(
        goal: GenerationBreedingGoal,
        allBirds: List<BirdEntity>,
        allGenetics: List<BirdGeneticsEntity>
    ): GenerationPlanningResult {
        val geneticsMap = allGenetics.associateBy { it.birdRingNumber }
        val genotypes = allBirds.map { parseGenotype(it, geneticsMap[it.ringNumber]) }

        val males = genotypes.filter { it.gender == BirdGender.MALE }
        val females = genotypes.filter { it.gender == BirdGender.FEMALE }

        val directMatches = mutableListOf<DirectPairCandidate>()

        // 1. Check all direct (Male x Female) pairs in the aviary
        males.forEach { sire ->
            females.forEach { dam ->
                val prediction = predictOffspring(sire, dam)

                val matchingOutcome = prediction.outcomes.firstOrNull { outcome ->
                    val hasBaseSeries = if (goal.requiredBaseSeries == BaseColorSeries.BLUE) {
                        outcome.baseColor.contains("Blue", ignoreCase = true) ||
                                outcome.baseColor.contains("Cobalt", ignoreCase = true) ||
                                outcome.baseColor.contains("Mauve", ignoreCase = true) ||
                                outcome.baseColor.contains("Sky", ignoreCase = true) ||
                                outcome.baseColor.contains("Albino", ignoreCase = true) ||
                                outcome.baseColor.contains("White", ignoreCase = true)
                    } else {
                        outcome.baseColor.contains("Green", ignoreCase = true) ||
                                outcome.baseColor.contains("Lutino", ignoreCase = true) ||
                                outcome.baseColor.contains("Yellow", ignoreCase = true)
                    }

                    val hasMutations = goal.requiredVisualMutations.all { reqMut ->
                        outcome.visualMutations.any { it.contains(reqMut, ignoreCase = true) } ||
                                outcome.phenotypeNameEn.contains(reqMut, ignoreCase = true)
                    }

                    hasBaseSeries && hasMutations
                }

                if (matchingOutcome != null && matchingOutcome.probability > 0.01) {
                    val sireBird = allBirds.firstOrNull { it.ringNumber == sire.ringNumber }
                    val damBird = allBirds.firstOrNull { it.ringNumber == dam.ringNumber }

                    directMatches.add(
                        DirectPairCandidate(
                            sireRing = sire.ringNumber,
                            sireName = sireBird?.name,
                            damRing = dam.ringNumber,
                            damName = damBird?.name,
                            matchingPhenotype = matchingOutcome.phenotypeNameEn,
                            probabilityPercent = matchingOutcome.probability * 100.0,
                            notes = "Direct 1-generation pair: ${matchingOutcome.probabilityPercent} probability per egg"
                        )
                    )
                }
            }
        }

        // 2. Multi-step Generation Roadmaps (2-step breeding plan)
        val roadmaps = mutableListOf<MultiStepBreedingRoadmap>()

        if (directMatches.isEmpty() && males.isNotEmpty() && females.isNotEmpty()) {
            // Find a pairing that produces necessary carrier/split offspring for step 2
            val candidateSire = males.first()
            val candidateDam = females.first()

            val step1 = TwoStepBreedingStep(
                generationIndex = 1,
                stepTitle = "Gen 1: Produce Carrier Foundation Stock",
                sireRing = candidateSire.ringNumber,
                damRing = candidateDam.ringNumber,
                targetOffspringToKeep = "Keep Cock offspring carrying split ${goal.requiredVisualMutations.firstOrNull() ?: "target alleles"}",
                projectedProbability = 50.0,
                instructions = "Breed ${candidateSire.ringNumber} x ${candidateDam.ringNumber}. Retain banded offspring identified with split carriers."
            )

            val step2 = TwoStepBreedingStep(
                generationIndex = 2,
                stepTitle = "Gen 2: Backcross / Outcross to Express Visual Phenotype",
                sireRing = "Selected Gen 1 Offspring",
                damRing = if (females.size > 1) females[1].ringNumber else candidateDam.ringNumber,
                targetOffspringToKeep = goal.titleEn,
                projectedProbability = 25.0,
                instructions = "Pair carrier offspring with target partner to unlock the 25% homozygous visual expression."
            )

            roadmaps.add(
                MultiStepBreedingRoadmap(
                    goalId = goal.id,
                    goalTitle = goal.titleEn,
                    steps = listOf(step1, step2),
                    finalOffspringProbability = 12.5
                )
            )
        }

        return GenerationPlanningResult(
            goal = goal,
            directMatches = directMatches.sortedByDescending { it.probabilityPercent },
            twoStepRoadmaps = roadmaps
        )
    }

    /**
     * Generates chronological developmental milestones for the Genetics Calendar
     * based on active pairs, clutches, eggs, and chicks.
     */
    fun generateGeneticsCalendarMilestones(
        pairs: List<PairWithBreedingDetails>
    ): List<GeneticsCalendarEvent> {
        val events = mutableListOf<GeneticsCalendarEvent>()
        val now = System.currentTimeMillis()
        val oneDayMillis = 86400000L

        pairs.forEach { pairWithDetails ->
            val pair = pairWithDetails.pair
            val pairDesc = "Pair #${pair.id} (${pair.maleRingNumber} × ${pair.femaleRingNumber})"

            // 1. Egg milestones (Candling at Day 6-8, Hatch date)
            pairWithDetails.eggs.forEach { egg ->
                val layDate = egg.layDate
                if (layDate > 0 && egg.eggResult != "DISCARDED") {
                    // Candling milestone
                    val candlingDueDate = layDate + (7L * oneDayMillis)
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_candle_${egg.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = egg.eggNumber, // using egg number for order
                            eggNumber = egg.eggNumber,
                            chickRingNumber = null,
                            milestoneType = GeneticMilestoneType.CANDLING,
                            dueDateMillis = candlingDueDate,
                            geneticSignificance = "Confirm embryo spiderweb veins and heartbeat. Fertile eggs establish clutch genetic viability.",
                            isCompleted = egg.fertilityStatus != "UNCANDLED"
                        )
                    )
                }
            }

            // 2. Chick milestones (Eye check day 0-2, Down check day 5-7, Ring banding day 6-8, Pin feather check day 14-21, Weaning day 30-35)
            pairWithDetails.chicks.forEach { chick ->
                val hatchDate = chick.hatchDate
                val chickAgeDays = ((now - hatchDate) / oneDayMillis).toInt()
                if (hatchDate > 0 && chick.status != "DECEASED") {
                    // Eye pigment check
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_eye_${chick.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = 1,
                            eggNumber = null,
                            chickRingNumber = chick.bandedRingNumber ?: "Chick #${chick.hatchOrder}",
                            milestoneType = GeneticMilestoneType.HATCH_EYE_CHECK,
                            dueDateMillis = hatchDate + (1L * oneDayMillis),
                            geneticSignificance = "Inspect eye color at emergence: Plum/Ruby Red eyes indicate sex-linked Ino (Albino/Lutino) or Fallow; Black eyes indicate Normal.",
                            isCompleted = chickAgeDays >= 3
                        )
                    )

                    // Down color check
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_down_${chick.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = 1,
                            eggNumber = null,
                            chickRingNumber = chick.bandedRingNumber ?: "Chick #${chick.hatchOrder}",
                            milestoneType = GeneticMilestoneType.DOWN_FEATHER_CHECK,
                            dueDateMillis = hatchDate + (6L * oneDayMillis),
                            geneticSignificance = "White down indicates Blue series base mutation; Grey down indicates Green series base mutation.",
                            isCompleted = chickAgeDays >= 7
                        )
                    )

                    // Closed ring banding
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_band_${chick.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = 1,
                            eggNumber = null,
                            chickRingNumber = chick.bandedRingNumber ?: "Chick #${chick.hatchOrder}",
                            milestoneType = GeneticMilestoneType.CLOSED_RING_BANDING,
                            dueDateMillis = hatchDate + (7L * oneDayMillis),
                            geneticSignificance = "Apply official closed ring (4.0-4.2mm for English show, 3.8-4.0mm for Color) for immutable genealogical lineage certification.",
                            isCompleted = !chick.bandedRingNumber.isNullOrBlank()
                        )
                    )

                    // Feather pin check
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_pin_${chick.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = 1,
                            eggNumber = null,
                            chickRingNumber = chick.bandedRingNumber ?: "Chick #${chick.hatchOrder}",
                            milestoneType = GeneticMilestoneType.PIN_FEATHER_CHECK,
                            dueDateMillis = hatchDate + (16L * oneDayMillis),
                            geneticSignificance = "Erupting wing pins reveal Spangle wing margins, Opaline V-shape mantle clear zone, and Cinnamon melanin pigment.",
                            isCompleted = chickAgeDays >= 20
                        )
                    )

                    // Weaning & Phenotype assessment
                    events.add(
                        GeneticsCalendarEvent(
                            id = "milestone_wean_${chick.id}",
                            pairId = pair.id,
                            pairDescription = pairDesc,
                            clutchNumber = 1,
                            eggNumber = null,
                            chickRingNumber = chick.bandedRingNumber ?: "Chick #${chick.hatchOrder}",
                            milestoneType = GeneticMilestoneType.WEANING_PHENOTYPE,
                            dueDateMillis = hatchDate + (32L * oneDayMillis),
                            geneticSignificance = "Weaned chick enters registry: verify full visual mutations, mask spots, ceres color for sex confirmation.",
                            isCompleted = chick.status == "TRANSFERRED" || chickAgeDays >= 35
                        )
                    )
                }
            }
        }

        return events.sortedBy { it.dueDateMillis }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
