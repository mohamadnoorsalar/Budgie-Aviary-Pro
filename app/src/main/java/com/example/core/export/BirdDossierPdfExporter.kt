package com.example.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.qr.QrCodeGenerator
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BirdDossierPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateFullDossierPdf(
        context: Context,
        birdWithDetails: BirdWithDetails,
        pedigree: PedigreeRecordEntity?,
        genetics: BirdGeneticsEntity?,
        children: BirdWithChildren?,
        breedingPairs: List<PairEntity> = emptyList(),
        medications: List<MedicationEntity> = emptyList(),
        incomeRecords: List<IncomeEntity> = emptyList(),
        isPersian: Boolean = false
    ): File? {
        val bird = birdWithDetails.bird
        val document = PdfDocument()

        // Page 1: Bird Identity, Photo, Detailed Specs, Pedigree Tree, QR Seal
        val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(page1Info)
        renderPage1(
            context = context,
            page = page1,
            bird = bird,
            birdWithDetails = birdWithDetails,
            pedigree = pedigree,
            genetics = genetics,
            children = children,
            isPersian = isPersian
        )
        document.finishPage(page1)

        // Page 2: Breeding History, Offspring Chicks, Weights, Health, Medications, Competitions, Financials
        val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(page2Info)
        renderPage2(
            page = page2,
            bird = bird,
            birdWithDetails = birdWithDetails,
            children = children,
            breedingPairs = breedingPairs,
            medications = medications,
            incomeRecords = incomeRecords,
            isPersian = isPersian
        )
        document.finishPage(page2)

        return try {
            val file = File(context.cacheDir, "Bird_Dossier_${bird.ringNumber}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun renderPage1(
        context: Context,
        page: PdfDocument.Page,
        bird: BirdEntity,
        birdWithDetails: BirdWithDetails,
        pedigree: PedigreeRecordEntity?,
        genetics: BirdGeneticsEntity?,
        children: BirdWithChildren?,
        isPersian: Boolean
    ) {
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 76, 38)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(70, 80, 95)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(20, 60, 40)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val emptyTextPaint = Paint().apply {
            color = Color.rgb(130, 130, 130)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(210, 220, 230)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val primaryBg = Paint().apply {
            color = Color.rgb(243, 248, 244)
            style = Paint.Style.FILL
        }

        val cardBg = Paint().apply {
            color = Color.rgb(250, 252, 254)
            style = Paint.Style.FILL
        }

        // Outer Page Double Border
        canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
        canvas.drawRect(24f, 24f, 571f, 818f, borderPaint)

        // Header Top Bar
        var y = 48f
        val headerTitle = if (isPersian) "پرونده جامع پرنده و شجره‌نامه رسمی" else "COMPREHENSIVE BIRD DOSSIER & OFFICIAL PEDIGREE"
        canvas.drawText(headerTitle, 35f, y, titlePaint)

        y += 14f
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val headerSub = if (isPersian)
            "سیستم مدیریت سالن پرورش مرغ عشق | کد پرونده: ${bird.syncId.take(12).uppercase()} | تاریخ صدور: ${sdf.format(Date())}"
        else
            "Budgerigar Aviary Management System | Sync Ref: ${bird.syncId.take(12).uppercase()} | Generated: ${sdf.format(Date())}"
        canvas.drawText(headerSub, 35f, y, subTitlePaint)

        y += 10f
        canvas.drawLine(35f, y, 560f, y, borderPaint)

        // Section 1: Bird Identity & Physical Profile + Photo Box + QR Code
        y += 16f
        canvas.drawRect(35f, y, 560f, y + 140f, primaryBg)
        canvas.drawRect(35f, y, 560f, y + 140f, borderPaint)

        // Photo / Photo Placeholder
        val photoLeft = 45f
        val photoTop = y + 12f
        val photoWidth = 95f
        val photoHeight = 115f
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoWidth, photoTop + photoHeight, cardBg)
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoWidth, photoTop + photoHeight, borderPaint)

        var photoLoaded = false
        if (!bird.photoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(bird.photoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, RectF(photoLeft, photoTop, photoLeft + photoWidth, photoTop + photoHeight), Paint(Paint.FILTER_BITMAP_FLAG))
                        photoLoaded = true
                    }
                }
            } catch (_: Exception) {}
        }
        if (!photoLoaded) {
            canvas.drawText(if (isPersian) "تصویر ثبت نشده" else "No Photo", photoLeft + 18f, photoTop + 55f, emptyTextPaint)
            canvas.drawText(if (isPersian) "تصویر رسمی پرنده" else "Official Photo", photoLeft + 14f, photoTop + 70f, emptyTextPaint)
        }

        // Specs Grid
        val textLeft1 = 155f
        val textLeft2 = 300f
        val textLeft3 = 440f
        var rowY = y + 25f

        val birthStr = bird.birthDate?.let { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(it)) } ?: (if (isPersian) "ثبت نشده (نامشخص)" else "Unspecified")

        drawField(canvas, "Ring / پلاک:", bird.ringNumber, textLeft1, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Name / نام:", bird.name, textLeft2, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Sex / جنسیت:", bird.gender.name, textLeft3, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 22f
        drawField(canvas, "Breed / نژاد:", bird.variety.name.replace("_", " "), textLeft1, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Color / رنگ:", bird.color, textLeft2, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Mutation / جهش:", bird.mutation, textLeft3, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 22f
        drawField(canvas, "Birth Date / تولد:", birthStr, textLeft1, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Place / محل:", bird.placeOfBirth, textLeft2, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Generation / نسل:", bird.generation ?: "F1", textLeft3, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 22f
        drawField(canvas, "Cage / قفس:", bird.cageCode, textLeft1, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Status / وضعیت:", bird.status.name, textLeft2, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Father / پدر:", bird.fatherRing, textLeft3, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 22f
        drawField(canvas, "Mother / مادر:", bird.motherRing, textLeft1, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Weight / وزن فعلی:", birdWithDetails.weightHistory.maxByOrNull { it.recordedDate }?.let { "${it.weightGrams}g" }, textLeft2, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Condition / شاخص:", birdWithDetails.weightHistory.maxByOrNull { it.recordedDate }?.conditionScore, textLeft3, rowY, boldTextPaint, textPaint, emptyTextPaint)

        // Section 2: 3-Generation Pedigree Lineage Chart
        y += 158f
        val pedigreeTitle = if (isPersian) "شجره‌نامه و شجره والدین (۳ نسل)" else "3-GENERATION PEDIGREE & LINEAGE TREE"
        canvas.drawText(pedigreeTitle, 35f, y, sectionHeaderPaint)

        y += 8f
        val sireY = y
        canvas.drawRect(35f, sireY, 275f, sireY + 54f, cardBg)
        canvas.drawRect(35f, sireY, 275f, sireY + 54f, borderPaint)
        canvas.drawText("SIRE (Father / پدر):", 45f, sireY + 18f, boldTextPaint)
        val fatherRing = pedigree?.sireRing ?: bird.fatherRing
        drawField(canvas, "Ring:", fatherRing, 45f, sireY + 34f, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Genetics:", genetics?.visualMutations, 45f, sireY + 48f, boldTextPaint, textPaint, emptyTextPaint)

        val damY = sireY + 60f
        canvas.drawRect(35f, damY, 275f, damY + 54f, cardBg)
        canvas.drawRect(35f, damY, 275f, damY + 54f, borderPaint)
        canvas.drawText("DAM (Mother / مادر):", 45f, damY + 18f, boldTextPaint)
        val motherRing = pedigree?.damRing ?: bird.motherRing
        drawField(canvas, "Ring:", motherRing, 45f, damY + 34f, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Genetics:", pedigree?.lineageNotes, 45f, damY + 48f, boldTextPaint, textPaint, emptyTextPaint)

        // Grandparents
        val pgSireY = sireY
        canvas.drawRect(285f, pgSireY, 560f, pgSireY + 25f, cardBg)
        canvas.drawRect(285f, pgSireY, 560f, pgSireY + 25f, borderPaint)
        drawField(canvas, "Pat. Grandsire (پدر پدری):", pedigree?.paternalGrandsire, 292f, pgSireY + 16f, boldTextPaint, textPaint, emptyTextPaint)

        val pgDamY = sireY + 29f
        canvas.drawRect(285f, pgDamY, 560f, pgDamY + 25f, cardBg)
        canvas.drawRect(285f, pgDamY, 560f, pgDamY + 25f, borderPaint)
        drawField(canvas, "Pat. Granddam (مادر پدری):", pedigree?.paternalGranddam, 292f, pgDamY + 16f, boldTextPaint, textPaint, emptyTextPaint)

        val mgSireY = damY
        canvas.drawRect(285f, mgSireY, 560f, mgSireY + 25f, cardBg)
        canvas.drawRect(285f, mgSireY, 560f, mgSireY + 25f, borderPaint)
        drawField(canvas, "Mat. Grandsire (پدر مادری):", pedigree?.maternalGrandsire, 292f, mgSireY + 16f, boldTextPaint, textPaint, emptyTextPaint)

        val mgDamY = damY + 29f
        canvas.drawRect(285f, mgDamY, 560f, mgDamY + 25f, cardBg)
        canvas.drawRect(285f, mgDamY, 560f, mgDamY + 25f, borderPaint)
        drawField(canvas, "Mat. Granddam (مادر مادری):", pedigree?.maternalGranddam, 292f, mgDamY + 16f, boldTextPaint, textPaint, emptyTextPaint)

        // Lineage Stats / COI
        y = damY + 68f
        val coiText = String.format(Locale.US, "%.2f%%", (pedigree?.inbreedingCoefficient ?: 0.0) * 100)
        drawField(canvas, "Breeder / پرورش‌دهنده:", pedigree?.breederName ?: "Aviary Master", 35f, y, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Breeder Code:", pedigree?.breederCode ?: "IR-AV-01", 240f, y, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Inbreeding Coeff (COI):", coiText, 410f, y, boldTextPaint, textPaint, emptyTextPaint)

        // Section 3: Genetics Profile & Allele Factors
        y += 24f
        val genTitle = if (isPersian) "مشخصات ژنتیکی، جهش‌های آشکار و حامل" else "GENETICS PROFILE & ALLELE CONFIGURATION"
        canvas.drawText(genTitle, 35f, y, sectionHeaderPaint)

        y += 8f
        canvas.drawRect(35f, y, 560f, y + 68f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 68f, borderPaint)

        var genY = y + 18f
        drawField(canvas, "Visual Mutations (آشکار):", genetics?.visualMutations ?: bird.mutation, 45f, genY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Base Series (پایه):", genetics?.baseSeries ?: "GREEN / BLUE", 360f, genY, boldTextPaint, textPaint, emptyTextPaint)

        genY += 18f
        drawField(canvas, "Split Carrier (حامل):", genetics?.splitMutations, 45f, genY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Dark Factors:", genetics?.darkFactors?.let { "$it Factor(s)" } ?: "0", 360f, genY, boldTextPaint, textPaint, emptyTextPaint)

        genY += 18f
        val factors = buildString {
            if (genetics?.violetFactor == true) append("Violet ")
            if (genetics?.greyFactor == true) append("Grey ")
            if (genetics?.cinnamonFactor == true) append("Cinnamon ")
            if (genetics?.inoFactor == true) append("Ino ")
            if (genetics?.opalineFactor == true) append("Opaline ")
            if (genetics?.spangleFactor != null && genetics.spangleFactor != "NONE") append("Spangle(${genetics.spangleFactor}) ")
            if (isEmpty()) append("Standard Alleles")
        }
        drawField(canvas, "Special Factors (عوامل خاص):", factors, 45f, genY, boldTextPaint, textPaint, emptyTextPaint)

        // Section 4: Official Verification Seal & QR Code
        y += 86f
        canvas.drawRect(35f, y, 560f, y + 74f, primaryBg)
        canvas.drawRect(35f, y, 560f, y + 74f, borderPaint)

        val qrBoxX = 45f
        val qrBoxY = y + 10f
        val qrBitmap = QrCodeGenerator.generateQrBitmap(
            QrCodeGenerator.getBirdQrPayload(bird.ringNumber),
            sizePixels = 200
        )
        val qrDestRect = RectF(qrBoxX, qrBoxY, qrBoxX + 54f, qrBoxY + 54f)
        canvas.drawBitmap(qrBitmap, null, qrDestRect, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.drawRect(qrBoxX, qrBoxY, qrBoxX + 54f, qrBoxY + 54f, borderPaint)

        canvas.drawText("OFFICIAL AVIARY CERTIFICATE VERIFICATION / تاییدیه رسمی", 110f, y + 24f, boldTextPaint)
        canvas.drawText("Bird ID: ${bird.syncId.take(16).uppercase()} | Ring: ${bird.ringNumber}", 110f, y + 40f, textPaint)
        canvas.drawText("Scan QR code in-app or with camera to verify full digitized aviary record.", 110f, y + 56f, textPaint)

        // Footer Page 1
        canvas.drawText("Budgerigar Aviary Management - Page 1 of 2 | شناسنامه رسمی پرنده", 170f, 804f, subTitlePaint)
    }

    private fun renderPage2(
        page: PdfDocument.Page,
        bird: BirdEntity,
        birdWithDetails: BirdWithDetails,
        children: BirdWithChildren?,
        breedingPairs: List<PairEntity>,
        medications: List<MedicationEntity>,
        incomeRecords: List<IncomeEntity>,
        isPersian: Boolean
    ) {
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 76, 38)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(70, 80, 95)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(20, 60, 40)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val emptyTextPaint = Paint().apply {
            color = Color.rgb(130, 130, 130)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(210, 220, 230)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val cardBg = Paint().apply {
            color = Color.rgb(250, 252, 254)
            style = Paint.Style.FILL
        }

        // Outer Page Double Border
        canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
        canvas.drawRect(24f, 24f, 571f, 818f, borderPaint)

        var y = 48f
        val page2Title = if (isPersian) "سوابق تکثیر، سلامت، مسابقات و امور مالی: ${bird.ringNumber}" else "BREEDING, HEALTH, COMPETITION & FINANCIAL RECORDS: ${bird.ringNumber}"
        canvas.drawText(page2Title, 35f, y, titlePaint)

        y += 12f
        canvas.drawLine(35f, y, 560f, y, borderPaint)

        // 1. Breeding History & Registered Offspring
        y += 18f
        val breedTitle = if (isPersian) "سوابق جفت‌اندازی و جوجه‌های ثبت‌شده" else "1. BREEDING PAIR HISTORY & REGISTERED OFFSPRING"
        canvas.drawText(breedTitle, 35f, y, sectionHeaderPaint)

        y += 6f
        canvas.drawRect(35f, y, 560f, y + 80f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 80f, borderPaint)

        var rowY = y + 16f
        val pairCount = breedingPairs.size
        val allOffspring = children?.allChildren ?: emptyList()
        val offspringCount = allOffspring.size

        drawField(canvas, "Total Breeding Pairs:", "$pairCount pair(s)", 45f, rowY, boldTextPaint, textPaint, emptyTextPaint)
        drawField(canvas, "Total Offspring Produced:", "$offspringCount bird(s)", 280f, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 18f
        val offspringRings = if (allOffspring.isNotEmpty()) {
            allOffspring.joinToString(", ") { "${it.ringNumber} (${it.color})" }.take(90)
        } else {
            null
        }
        drawField(canvas, "Offspring Rings:", offspringRings, 45f, rowY, boldTextPaint, textPaint, emptyTextPaint)

        rowY += 18f
        val pairsSummary = if (breedingPairs.isNotEmpty()) {
            breedingPairs.joinToString(" | ") { "Pair #${it.id} [${it.maleRingNumber} x ${it.femaleRingNumber}] Cage: ${it.cageCode ?: "-"}" }.take(95)
        } else {
            null
        }
        drawField(canvas, "Breeding Pairs:", pairsSummary, 45f, rowY, boldTextPaint, textPaint, emptyTextPaint)

        // 2. Weight Tracking History
        y += 94f
        val weightTitle = if (isPersian) "سوابق ثبت وزن و شاخص رشد" else "2. WEIGHT TRACKING & BODY CONDITION HISTORY"
        canvas.drawText(weightTitle, 35f, y, sectionHeaderPaint)

        y += 6f
        canvas.drawRect(35f, y, 560f, y + 65f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 65f, borderPaint)

        val weights = birdWithDetails.weightHistory.sortedByDescending { it.recordedDate }
        if (weights.isEmpty()) {
            canvas.drawText(if (isPersian) "هیچ رکوردی برای وزن ثبت نشده است." else "No weight records logged for this bird.", 45f, y + 25f, emptyTextPaint)
        } else {
            var wY = y + 16f
            for (i in 0 until minOf(3, weights.size)) {
                val w = weights[i]
                val dStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(w.recordedDate))
                canvas.drawText("• $dStr | Weight: ${w.weightGrams}g | Condition: ${w.conditionScore} | Notes: ${w.notes ?: "-"}", 45f, wY, textPaint)
                wY += 15f
            }
        }

        // 3. Health Visits & Medications
        y += 78f
        val healthTitle = if (isPersian) "سوابق بیماری، ویزیت سلامت و داروها" else "3. HEALTH EXAMINATIONS & MEDICATION LOG"
        canvas.drawText(healthTitle, 35f, y, sectionHeaderPaint)

        y += 6f
        canvas.drawRect(35f, y, 560f, y + 80f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 80f, borderPaint)

        val healths: List<HealthRecordEntity> = birdWithDetails.healthRecords.sortedByDescending { it.recordDate }
        if (healths.isEmpty() && medications.isEmpty()) {
            canvas.drawText(if (isPersian) "هیچ سابقه بیماری یا درمانی ثبت نشده است." else "No health conditions or treatments recorded.", 45f, y + 25f, emptyTextPaint)
        } else {
            var hY = y + 16f
            for (i in 0 until minOf(2, healths.size)) {
                val h = healths[i]
                val dStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(h.recordDate))
                val isResolvedStr = if (h.isResolved) "RESOLVED" else "ACTIVE"
                canvas.drawText("• Health: $dStr | Problem: ${h.recordedProblem} | Status: $isResolvedStr | Med: ${h.medicationName ?: "-"}", 45f, hY, textPaint)
                hY += 15f
            }
            for (i in 0 until minOf(2, medications.size)) {
                val m = medications[i]
                val dStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(m.startDate))
                canvas.drawText("• Med: $dStr | Drug: ${m.medicationName} | Dose: ${m.dosage} | Completed: ${m.isCompleted}", 45f, hY, textPaint)
                hY += 15f
            }
        }

        // 4. Competition Results
        y += 94f
        val compTitle = if (isPersian) "افتخارات و نتایج مسابقات و نمایشگاه‌ها" else "4. COMPETITION & SHOW RESULTS"
        canvas.drawText(compTitle, 35f, y, sectionHeaderPaint)

        y += 6f
        canvas.drawRect(35f, y, 560f, y + 65f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 65f, borderPaint)

        val comps: List<CompetitionScoreEntity> = birdWithDetails.competitionScores.sortedByDescending { it.updatedAt }
        if (comps.isEmpty()) {
            canvas.drawText(if (isPersian) "این پرنده تاکنون در هیچ مسابقه‌ای شرکت نکرده است." else "No competition entries or awards recorded.", 45f, y + 25f, emptyTextPaint)
        } else {
            var cY = y + 16f
            for (i in 0 until minOf(3, comps.size)) {
                val c = comps[i]
                val awardStr = c.awardTitle ?: "Participated"
                canvas.drawText("• ${c.showClass} | Score: ${c.totalScore} pts | Award: $awardStr | Notes: ${c.notes ?: "-"}", 45f, cY, textPaint)
                cY += 15f
            }
        }

        // 5. Financial Performance & Income
        y += 78f
        val finTitle = if (isPersian) "تراز مالی و درآمدهای حاصل از پرنده" else "5. FINANCIAL LOG & SALES / LEASING INCOME"
        canvas.drawText(finTitle, 35f, y, sectionHeaderPaint)

        y += 6f
        canvas.drawRect(35f, y, 560f, y + 65f, cardBg)
        canvas.drawRect(35f, y, 560f, y + 65f, borderPaint)

        if (incomeRecords.isEmpty()) {
            canvas.drawText(if (isPersian) "هیچ تراکنش درآمدی مستقیم برای این پرنده ثبت نشده است." else "No direct income/sales transactions recorded for this bird.", 45f, y + 25f, emptyTextPaint)
        } else {
            var fY = y + 16f
            val totalInc = incomeRecords.sumOf { it.amount }
            canvas.drawText("Total Recorded Revenue: $$totalInc (${incomeRecords.size} transaction(s))", 45f, fY, boldTextPaint)
            fY += 16f
            for (i in 0 until minOf(2, incomeRecords.size)) {
                val inc = incomeRecords[i]
                val dStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(inc.date))
                canvas.drawText("• $dStr | Category: ${inc.category} | Amount: $${inc.amount} | Notes: ${inc.notes ?: "-"}", 45f, fY, textPaint)
                fY += 15f
            }
        }

        // Footer Page 2
        canvas.drawText("Budgerigar Aviary Management - Page 2 of 2 | پایان پرونده جامع", 185f, 804f, subTitlePaint)
    }

    private fun drawField(
        canvas: android.graphics.Canvas,
        label: String,
        value: String?,
        x: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint,
        emptyPaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        val labelWidth = labelPaint.measureText(label) + 4f
        if (!value.isNullOrBlank() && value != "-") {
            canvas.drawText(value, x + labelWidth, y, valuePaint)
        } else {
            canvas.drawText("—", x + labelWidth, y, emptyPaint)
        }
    }

    fun sharePdf(context: Context, pdfFile: File, title: String = "Bird Dossier & Pedigree") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$title: ${pdfFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
