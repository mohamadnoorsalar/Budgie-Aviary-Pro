package com.example.core

import com.example.core.common.BirdGender
import com.example.core.genetics.BaseColorSeries
import com.example.core.genetics.BirdGenotype
import com.example.core.genetics.DominantFactorState
import com.example.core.genetics.GenerationBreedingGoal
import com.example.core.genetics.GeneticsCalculator
import com.example.core.genetics.InbreedingWarningLevel
import com.example.core.genetics.SexLinkedAlleleState
import com.example.core.pedigree.PedigreeNodeRole
import com.example.core.pedigree.PedigreeTreeBuilder
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneticsAndPedigreeTest {

    @Test
    fun testRecessiveBlueInheritance_TwoCarriersYield25PercentVisualBlue() {
        // Sire: Green split Blue (B/b)
        val sire = BirdGenotype(
            ringNumber = "COCK-1",
            gender = BirdGender.MALE,
            baseSeries = BaseColorSeries.GREEN,
            isSplitBlue = true,
            darkFactors = 0,
            isViolet = false,
            isGrey = false
        )
        // Dam: Green split Blue (B/b)
        val dam = BirdGenotype(
            ringNumber = "HEN-1",
            gender = BirdGender.FEMALE,
            baseSeries = BaseColorSeries.GREEN,
            isSplitBlue = true,
            darkFactors = 0,
            isViolet = false,
            isGrey = false
        )

        val prediction = GeneticsCalculator.predictOffspring(sire, dam)

        // Find the probability of Blue offspring
        val blueOutcomes = prediction.outcomes.filter { it.baseColor.contains("Blue") || it.phenotypeNameEn.contains("Blue") }
        val totalBlueProbability = blueOutcomes.sumOf { it.probability }

        // Expected Mendelian ratio: 25% (0.25)
        assertEquals(0.25, totalBlueProbability, 0.001)

        // Green outcomes should be 75%
        val greenOutcomes = prediction.outcomes.filter { it.baseColor.contains("Green") || it.phenotypeNameEn.contains("Green") }
        val totalGreenProbability = greenOutcomes.sumOf { it.probability }
        assertEquals(0.75, totalGreenProbability, 0.001)
    }

    @Test
    fun testSexLinkedInheritance_CarrierCockAndNormalHen() {
        // Cock: Split Ino (Z-Ino / Z-Normal)
        val sire = BirdGenotype(
            ringNumber = "COCK-INO-SPLIT",
            gender = BirdGender.MALE,
            baseSeries = BaseColorSeries.GREEN,
            isSplitBlue = false,
            darkFactors = 0,
            isViolet = false,
            isGrey = false,
            ino = SexLinkedAlleleState.SPLIT_CARRIER
        )
        // Hen: Normal (Z-Normal / W)
        val dam = BirdGenotype(
            ringNumber = "HEN-NORMAL",
            gender = BirdGender.FEMALE,
            baseSeries = BaseColorSeries.GREEN,
            isSplitBlue = false,
            darkFactors = 0,
            isViolet = false,
            isGrey = false,
            ino = SexLinkedAlleleState.NORMAL
        )

        val prediction = GeneticsCalculator.predictOffspring(sire, dam)

        // Female offspring (Hens): 50% Visual Ino, 50% Normal
        val femaleInoProbability = prediction.femaleOutcomes.filter { it.visualMutations.any { v -> v.contains("Ino") || v.contains("Albino") || v.contains("Lutino") } }.sumOf { it.probability }
        val femaleTotal = prediction.femaleOutcomes.sumOf { it.probability }
        val femaleInoPercentage = femaleInoProbability / femaleTotal
        assertEquals(0.50, femaleInoPercentage, 0.01)

        // Male offspring (Cocks): 0% Visual Ino, 50% Split Ino
        val maleVisualInoProbability = prediction.maleOutcomes.filter { it.visualMutations.any { v -> v.contains("Ino") || v.contains("Albino") || v.contains("Lutino") } }.sumOf { it.probability }
        assertEquals(0.0, maleVisualInoProbability, 0.001)

        val maleSplitInoProbability = prediction.maleOutcomes.filter { it.splitCarriers.any { c -> c.contains("Ino") } }.sumOf { it.probability }
        val maleTotal = prediction.maleOutcomes.sumOf { it.probability }
        val maleSplitPercentage = maleSplitInoProbability / maleTotal
        assertEquals(0.50, maleSplitPercentage, 0.01)
    }

    @Test
    fun testDarkFactorIncompleteDominance_SkyBlueAndMauveProduce100PercentCobalt() {
        // Sky Blue (0 Dark Factors)
        val sire = BirdGenotype(
            ringNumber = "COCK-SKY",
            gender = BirdGender.MALE,
            baseSeries = BaseColorSeries.BLUE,
            isSplitBlue = false,
            darkFactors = 0,
            isViolet = false,
            isGrey = false
        )
        // Mauve (2 Dark Factors)
        val dam = BirdGenotype(
            ringNumber = "HEN-MAUVE",
            gender = BirdGender.FEMALE,
            baseSeries = BaseColorSeries.BLUE,
            isSplitBlue = false,
            darkFactors = 2,
            isViolet = false,
            isGrey = false
        )

        val prediction = GeneticsCalculator.predictOffspring(sire, dam)

        // 100% of offspring must be 1 Dark Factor (Cobalt)
        prediction.outcomes.forEach { outcome ->
            assertTrue(outcome.phenotypeNameEn.contains("Cobalt") || outcome.baseColor.contains("Cobalt"))
        }
    }

    @Test
    fun testRelatednessAnalysis_FullSiblingsTriggerCriticalWarning() {
        // Two birds with identical parents (Father: SIRE-A, Mother: DAM-A)
        val brother = BirdEntity(ringNumber = "BROTHER-1", gender = BirdGender.MALE, fatherRing = "SIRE-A", motherRing = "DAM-A", color = "Blue", mutation = "Normal")
        val sister = BirdEntity(ringNumber = "SISTER-1", gender = BirdGender.FEMALE, fatherRing = "SIRE-A", motherRing = "DAM-A", color = "Blue", mutation = "Normal")
        val sireA = BirdEntity(ringNumber = "SIRE-A", gender = BirdGender.MALE, color = "Blue", mutation = "Normal")
        val damA = BirdEntity(ringNumber = "DAM-A", gender = BirdGender.FEMALE, color = "Blue", mutation = "Normal")

        val allBirds = listOf(brother, sister, sireA, damA)
        val allPedigrees = emptyList<PedigreeRecordEntity>()

        val report = GeneticsCalculator.calculateRelatedness("BROTHER-1", "SISTER-1", allBirds, allPedigrees)

        // Kinship R = 0.50, Inbreeding F = 0.25 (25%)
        assertEquals(0.50, report.relationshipCoefficientR, 0.01)
        assertEquals(0.25, report.inbreedingCoefficientF, 0.01)
        assertEquals(InbreedingWarningLevel.CRITICAL_WARNING, report.warningLevel)
        assertTrue(report.warningTitle.contains("CRITICAL") && report.warningTitle.contains("Siblings"))
        assertTrue(report.warningMessage.contains("Brother-sister") || report.warningMessage.contains("Sire"))
        assertTrue(report.biologicalHazards.isNotEmpty())
        assertEquals(2, report.commonAncestors.size) // Both SIRE-A and DAM-A
    }

    @Test
    fun testRelatednessAnalysis_UnrelatedBirdsAreSafe() {
        val cock = BirdEntity(ringNumber = "COCK-OUT", gender = BirdGender.MALE, fatherRing = "FAR-F1", motherRing = "FAR-M1", color = "Green", mutation = "Spangle")
        val hen = BirdEntity(ringNumber = "HEN-OUT", gender = BirdGender.FEMALE, fatherRing = "BLOOD-F2", motherRing = "BLOOD-M2", color = "Blue", mutation = "Opaline")

        val allBirds = listOf(cock, hen)
        val report = GeneticsCalculator.calculateRelatedness("COCK-OUT", "HEN-OUT", allBirds, emptyList())

        assertEquals(0.0, report.relationshipCoefficientR, 0.001)
        assertEquals(0.0, report.inbreedingCoefficientF, 0.001)
        assertEquals(InbreedingWarningLevel.SAFE, report.warningLevel)
    }

    @Test
    fun testInteractivePedigreeTreeBuilder_BuildsThreeGenerationsAndDescendants() {
        // Foundation ancestors
        val patGrandsire = BirdEntity(ringNumber = "PAT-GS", name = "Grandpa 1", gender = BirdGender.MALE, color = "Green", mutation = "Normal")
        val patGranddam = BirdEntity(ringNumber = "PAT-GD", name = "Grandma 1", gender = BirdGender.FEMALE, color = "Blue", mutation = "Opaline")
        val matGrandsire = BirdEntity(ringNumber = "MAT-GS", name = "Grandpa 2", gender = BirdGender.MALE, color = "Grey", mutation = "Normal")
        val matGranddam = BirdEntity(ringNumber = "MAT-GD", name = "Grandma 2", gender = BirdGender.FEMALE, color = "Yellow", mutation = "Spangle")

        // Parents
        val sire = BirdEntity(ringNumber = "SIRE-1", name = "Sire Champion", gender = BirdGender.MALE, fatherRing = "PAT-GS", motherRing = "PAT-GD", color = "Cobalt", mutation = "Spangle")
        val dam = BirdEntity(ringNumber = "DAM-1", name = "Dam Queen", gender = BirdGender.FEMALE, fatherRing = "MAT-GS", motherRing = "MAT-GD", color = "Sky Blue", mutation = "Opaline")

        // Subject bird
        val subject = BirdEntity(ringNumber = "SUBJECT-001", name = "Hero Bird", gender = BirdGender.MALE, fatherRing = "SIRE-1", motherRing = "DAM-1", color = "Cobalt Spangle", mutation = "Spangle Opaline")

        // Descendants: Children and Grandchildren
        val child = BirdEntity(ringNumber = "CHILD-1", gender = BirdGender.FEMALE, fatherRing = "SUBJECT-001", motherRing = "HEN-X", color = "Blue", mutation = "Spangle")
        val grandchild = BirdEntity(ringNumber = "GCHILD-1", gender = BirdGender.MALE, fatherRing = "COCK-Y", motherRing = "CHILD-1", color = "Cobalt", mutation = "Normal")

        val allBirds = listOf(patGrandsire, patGranddam, matGrandsire, matGranddam, sire, dam, subject, child, grandchild)

        val tree = PedigreeTreeBuilder.buildPedigreeTree(
            subjectRing = "SUBJECT-001",
            allBirds = allBirds,
            allPedigrees = emptyList(),
            allGenetics = emptyList()
        )

        assertNotNull(tree)
        assertEquals("SUBJECT-001", tree!!.subjectBird.ringNumber)

        // Check Parents
        assertNotNull(tree.sireNode)
        assertEquals("SIRE-1", tree.sireNode!!.ringNumber)
        assertEquals(PedigreeNodeRole.SIRE, tree.sireNode!!.role)

        assertNotNull(tree.damNode)
        assertEquals("DAM-1", tree.damNode!!.ringNumber)
        assertEquals(PedigreeNodeRole.DAM, tree.damNode!!.role)

        // Check Grandparents
        assertNotNull(tree.paternalGrandsireNode)
        assertEquals("PAT-GS", tree.paternalGrandsireNode!!.ringNumber)

        assertNotNull(tree.maternalGranddamNode)
        assertEquals("MAT-GD", tree.maternalGranddamNode!!.ringNumber)

        // Check Descendants
        assertEquals(1, tree.directChildren.size)
        assertEquals("CHILD-1", tree.directChildren.first().ringNumber)

        assertEquals(1, tree.grandchildren.size)
        assertEquals("GCHILD-1", tree.grandchildren.first().ringNumber)

        // Check Ancestry completeness percent
        assertTrue(tree.ancestryCompletenessPercent > 40)
    }

    @Test
    fun testGenerationPlanner_FindsDirectMatchInFlock() {
        val cock = BirdEntity(ringNumber = "COCK-SPANGLE", gender = BirdGender.MALE, color = "Sky Blue", mutation = "Spangle")
        val hen = BirdEntity(ringNumber = "HEN-SPANGLE", gender = BirdGender.FEMALE, color = "Cobalt", mutation = "Spangle")

        val cockGenetics = BirdGeneticsEntity(birdRingNumber = "COCK-SPANGLE", visualMutations = "Spangle", baseSeries = "BLUE", spangleFactor = "SINGLE_FACTOR")
        val henGenetics = BirdGeneticsEntity(birdRingNumber = "HEN-SPANGLE", visualMutations = "Spangle", baseSeries = "BLUE", spangleFactor = "SINGLE_FACTOR")

        val allBirds = listOf(cock, hen)
        val allGenetics = listOf(cockGenetics, henGenetics)

        val dfSpangleGoal = GenerationBreedingGoal(
            id = "df_spangle_white",
            titleEn = "Double Factor Spangle (White)",
            titleFa = "دابل فاکتور اسپنگل سفید",
            targetDescription = "Targeting DF Spangle on Blue series",
            requiredBaseSeries = BaseColorSeries.BLUE,
            requiredVisualMutations = listOf("Double Factor Spangle")
        )

        val plan = GeneticsCalculator.planGeneration(dfSpangleGoal, allBirds, allGenetics)

        assertEquals(1, plan.directMatches.size)
        val match = plan.directMatches.first()
        assertEquals("COCK-SPANGLE", match.sireRing)
        assertEquals("HEN-SPANGLE", match.damRing)
        assertTrue(match.probabilityPercent > 10.0)
    }

    @Test
    fun testPredictionVsActual_CalculatesAccurately() {
        val sire = BirdGenotype(ringNumber = "COCK-1", gender = BirdGender.MALE, baseSeries = BaseColorSeries.BLUE, isSplitBlue = false, darkFactors = 0, isViolet = false, isGrey = false)
        val dam = BirdGenotype(ringNumber = "HEN-1", gender = BirdGender.FEMALE, baseSeries = BaseColorSeries.BLUE, isSplitBlue = false, darkFactors = 0, isViolet = false, isGrey = false)
        val prediction = GeneticsCalculator.predictOffspring(sire, dam)

        // 4 recorded children in DB
        val recordedOffspring = listOf(
            BirdEntity(ringNumber = "K1", gender = BirdGender.MALE, fatherRing = "COCK-1", motherRing = "HEN-1", color = "Sky Blue", mutation = "Normal"),
            BirdEntity(ringNumber = "K2", gender = BirdGender.FEMALE, fatherRing = "COCK-1", motherRing = "HEN-1", color = "Sky Blue", mutation = "Normal"),
            BirdEntity(ringNumber = "K3", gender = BirdGender.MALE, fatherRing = "COCK-1", motherRing = "HEN-1", color = "Cobalt", mutation = "Normal"),
            BirdEntity(ringNumber = "K4", gender = BirdGender.FEMALE, fatherRing = "COCK-1", motherRing = "HEN-1", color = "Sky Blue", mutation = "Normal")
        )

        val report = GeneticsCalculator.comparePredictionVsActual(prediction, recordedOffspring)
        assertEquals(4, report.totalRecordedOffspring)
        assertTrue(report.sampleSizeSummary.contains("4"))
    }
}
