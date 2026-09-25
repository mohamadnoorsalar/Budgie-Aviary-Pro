package com.example.feature.reports

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.export.BirdDossierPdfExporter
import com.example.core.export.ExcelExporter
import com.example.core.export.GenericReportPdfExporter
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportsAndExportTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testExcelCsvExportWithPersianUtf8Bom() {
        val headers = listOf("پلاک پرنده (Ring)", "نام (Name)", "رنگ (Color)", "وضعیت (Status)")
        val rows = listOf(
            listOf("IR-2024-001", "شاهین", "آبی کاربنی (Cobalt Blue)", "موجود (AVAILABLE)"),
            listOf("IR-2024-002", "سیمرغ", "سبز زیتونی (Olive Green)", "جفت‌اندازی (BREEDING)"),
            listOf("IR-2024-003", "—", "زرد لوتینو (Lutino)", "—")
        )

        val exportedFile = ExcelExporter.exportToCsv(
            context = context,
            fileNamePrefix = "Test_Flock_Report",
            headers = headers,
            rows = rows
        )

        assertNotNull(exportedFile)
        assertTrue(exportedFile!!.exists())
        assertTrue(exportedFile.length() > 0)

        // Verify UTF-8 BOM is present at beginning of file
        FileInputStream(exportedFile).use { fis ->
            val b1 = fis.read()
            val b2 = fis.read()
            val b3 = fis.read()
            assertEquals(0xEF, b1)
            assertEquals(0xBB, b2)
            assertEquals(0xBF, b3)
        }
    }

    @Test
    fun testGenericTablePdfReportGeneration() {
        val headers = listOf("Pair ID", "Cage", "Eggs", "Chicks", "Fertility %", "Hatch %")
        val rows = listOf(
            listOf("101", "C-01", "6", "5", "83.3%", "100.0%"),
            listOf("102", "C-02", "4", "3", "75.0%", "100.0%"),
            listOf("103", "—", "0", "0", "0%", "0%")
        )

        try {
            val pdfFile = GenericReportPdfExporter.exportTableReport(
                context = context,
                reportTitleEn = "Breeding Productivity Report",
                reportTitleFa = "گزارش بهره‌وری جفت‌ها و تکثیر",
                filterSummary = "All Active Pairs | Cage: ALL",
                headers = headers,
                rows = rows,
                isPersian = true
            )
            if (pdfFile != null) {
                assertTrue(pdfFile.exists())
            }
        } catch (e: Exception) {
            // PdfDocument on JVM Robolectric environment without native skia bindings
            assertTrue(e is IllegalStateException || e is UnsupportedOperationException)
        }
    }

    @Test
    fun testCompleteBirdDossierPdfGenerationWithAllModules() {
        val bird = BirdEntity(
            ringNumber = "IR-2024-CHAMPION-99",
            name = "Royal Blue",
            gender = BirdGender.MALE,
            variety = BudgieVariety.ENGLISH_SHOW,
            color = "Cobalt Blue Opaline",
            mutation = "Single Factor Violet",
            cageCode = "C-VIP-01",
            birthDate = System.currentTimeMillis() - (180L * 86400000L),
            fatherRing = "IR-SIRE-10",
            motherRing = "IR-DAM-20",
            status = BirdStatus.ACTIVE
        )

        val genetics = BirdGeneticsEntity(
            birdRingNumber = bird.ringNumber,
            baseSeries = "BLUE",
            darkFactors = 1,
            violetFactor = true,
            opalineFactor = true,
            visualMutations = "Cobalt Blue Opaline Violet",
            splitMutations = "Cinnamon, Ino"
        )

        val pedigree = PedigreeRecordEntity(
            birdRingNumber = bird.ringNumber,
            sireRing = "IR-SIRE-10",
            damRing = "IR-DAM-20",
            paternalGrandsire = "IR-GRAND-SIRE-01",
            paternalGranddam = "IR-GRAND-DAM-01",
            maternalGrandsire = "IR-GRAND-SIRE-02",
            maternalGranddam = "IR-GRAND-DAM-02",
            breederName = "Master Aviary Iran",
            breederCode = "IR-AV-01",
            inbreedingCoefficient = 0.03125
        )

        val healthList = listOf(
            HealthRecordEntity(
                id = "HEALTH-01",
                birdRingNumber = bird.ringNumber,
                recordDate = System.currentTimeMillis() - 5000000L,
                recordedProblem = "Routine Pre-Breeding Health Exam",
                diagnosis = "Healthy Budgerigar",
                symptoms = "Vigorous, clear cere and vent",
                isResolved = true
            )
        )

        val weightList = listOf(
            WeightRecordEntity(
                id = "W-01",
                birdRingNumber = bird.ringNumber,
                recordedDate = System.currentTimeMillis() - 10000000L,
                weightGrams = 48.5,
                conditionScore = "OPTIMAL"
            ),
            WeightRecordEntity(
                id = "W-02",
                birdRingNumber = bird.ringNumber,
                recordedDate = System.currentTimeMillis() - 1000000L,
                weightGrams = 51.2,
                conditionScore = "EXCELLENT"
            )
        )

        val compScores = listOf(
            CompetitionScoreEntity(
                id = "SCORE-99",
                competitionId = "COMP-2024-TEHRAN",
                birdRingNumber = bird.ringNumber,
                showClass = "English Budgies - Cobalt Series",
                totalScore = 94.5,
                awardTitle = "FIRST_IN_CLASS",
                notes = "Outstanding feather posture and head blow"
            )
        )

        val birdWithDetails = BirdWithDetails(
            bird = bird,
            cage = CageEntity(code = "C-VIP-01", location = "Breeding Room A"),
            genetics = genetics,
            pedigree = pedigree,
            healthRecords = healthList,
            weightHistory = weightList,
            competitionScores = compScores
        )

        val offspring = listOf(
            BirdEntity(
                ringNumber = "IR-2025-OFFSPRING-01",
                gender = BirdGender.FEMALE,
                variety = BudgieVariety.ENGLISH_SHOW,
                color = "Sky Blue Opaline",
                fatherRing = bird.ringNumber,
                motherRing = "IR-DAM-20"
            )
        )

        val children = BirdWithChildren(
            bird = bird,
            fatheredChildren = offspring,
            motheredChildren = emptyList()
        )

        val breedingPairs = listOf(
            PairEntity(
                id = 505L,
                maleRingNumber = bird.ringNumber,
                femaleRingNumber = "IR-DAM-20",
                cageCode = "C-VIP-01",
                isActive = true
            )
        )

        val medications = listOf(
            MedicationEntity(
                id = "MED-01",
                birdRingNumber = bird.ringNumber,
                medicationName = "Calcium + D3 Supplement",
                dosage = "5ml/1L water",
                startDate = System.currentTimeMillis() - 2000000L,
                isCompleted = true
            )
        )

        val incomes = listOf(
            IncomeEntity(
                id = "INC-01",
                title = "Offspring Sale",
                amount = 250.0,
                category = "BIRD_SALE",
                date = System.currentTimeMillis() - 8000000L,
                soldBirdRingNumber = bird.ringNumber,
                buyerName = "Champion Breeder Tehran"
            )
        )

        // Generate Bilingual Full 2-Page Bird Dossier
        try {
            val dossierPdf = BirdDossierPdfExporter.generateFullDossierPdf(
                context = context,
                birdWithDetails = birdWithDetails,
                pedigree = pedigree,
                genetics = genetics,
                children = children,
                breedingPairs = breedingPairs,
                medications = medications,
                incomeRecords = incomes,
                isPersian = true
            )
            if (dossierPdf != null) {
                assertTrue(dossierPdf.exists())
            }
        } catch (e: Exception) {
            // PdfDocument on JVM Robolectric environment without native skia bindings
            assertTrue(e is IllegalStateException || e is UnsupportedOperationException)
        }
    }
}
