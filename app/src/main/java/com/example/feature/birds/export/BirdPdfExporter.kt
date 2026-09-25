package com.example.feature.birds.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BirdPdfExporter {

    fun generateAndSharePdf(
        context: Context,
        birdWithDetails: BirdWithDetails,
        pedigree: PedigreeRecordEntity?,
        genetics: BirdGeneticsEntity?,
        children: BirdWithChildren?
    ): File? {
        val bird = birdWithDetails.bird
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (in points: 595 x 842)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(27, 94, 32) // Forest green
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(55, 71, 79)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(38, 50, 56)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(200, 214, 229)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }

        val accentCardBg = Paint().apply {
            color = Color.rgb(232, 245, 233)
            style = Paint.Style.FILL
        }

        // Outer Page Border
        canvas.drawRect(24f, 24f, 571f, 818f, borderPaint)
        canvas.drawRect(28f, 28f, 567f, 814f, borderPaint)

        var y = 55f

        // Document Title
        canvas.drawText("BUDGERIGAR BREEDING FACILITY - OFFICIAL CERTIFICATE", 40f, y, titlePaint)
        y += 18f
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("مدیریت سالن پرورش مرغ عشق | شناسنامه و شجره‌نامه رسمی | Issued: ${dateFormat.format(Date())}", 40f, y, subTitlePaint)
        y += 12f
        canvas.drawLine(40f, y, 555f, y, borderPaint)

        // 1. Primary Bird Info Card
        y += 20f
        canvas.drawRect(40f, y, 555f, y + 105f, accentCardBg)
        canvas.drawRect(40f, y, 555f, y + 105f, borderPaint)

        canvas.drawText("BIRD PROFILE / مشخصات پرنده", 52f, y + 18f, headerPaint)
        val birthDateStr = bird.birthDate?.let { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(it)) } ?: "Unknown"

        canvas.drawText("Ring Number: ", 52f, y + 38f, boldTextPaint)
        canvas.drawText(bird.ringNumber, 130f, y + 38f, textPaint)

        canvas.drawText("Name: ", 240f, y + 38f, boldTextPaint)
        canvas.drawText(bird.name ?: "-", 285f, y + 38f, textPaint)

        canvas.drawText("Sex: ", 410f, y + 38f, boldTextPaint)
        canvas.drawText(bird.gender.name, 445f, y + 38f, textPaint)

        canvas.drawText("Variety/Breed: ", 52f, y + 56f, boldTextPaint)
        canvas.drawText(bird.variety.name.replace("_", " "), 135f, y + 56f, textPaint)

        canvas.drawText("Mutation: ", 240f, y + 56f, boldTextPaint)
        canvas.drawText(bird.mutation, 295f, y + 56f, textPaint)

        canvas.drawText("Color: ", 410f, y + 56f, boldTextPaint)
        canvas.drawText(bird.color, 445f, y + 56f, textPaint)

        canvas.drawText("Birth Date: ", 52f, y + 74f, boldTextPaint)
        canvas.drawText(birthDateStr, 115f, y + 74f, textPaint)

        canvas.drawText("Birth Place: ", 240f, y + 74f, boldTextPaint)
        canvas.drawText(bird.placeOfBirth ?: "Aviary Main Hall", 305f, y + 74f, textPaint)

        canvas.drawText("Generation: ", 410f, y + 74f, boldTextPaint)
        canvas.drawText(bird.generation ?: "F1", 475f, y + 74f, textPaint)

        canvas.drawText("Current Cage: ", 52f, y + 92f, boldTextPaint)
        canvas.drawText(bird.cageCode ?: "Unassigned", 130f, y + 92f, textPaint)

        canvas.drawText("Status: ", 240f, y + 92f, boldTextPaint)
        canvas.drawText(bird.status.name, 285f, y + 92f, textPaint)

        // 2. Pedigree Lineage (3-Generation Chart)
        y += 125f
        canvas.drawText("PEDIGREE & LINEAGE / شجره‌نامه ۳ نسل", 40f, y, headerPaint)
        y += 10f

        // Sire Box
        val sireBoxY = y
        canvas.drawRect(40f, sireBoxY, 280f, sireBoxY + 50f, cardBgPaint)
        canvas.drawRect(40f, sireBoxY, 280f, sireBoxY + 50f, borderPaint)
        canvas.drawText("SIRE (Father / پدر):", 48f, sireBoxY + 16f, boldTextPaint)
        canvas.drawText(pedigree?.sireRing ?: bird.fatherRing ?: "Ring: Not Recorded", 48f, sireBoxY + 34f, textPaint)

        // Dam Box
        val damBoxY = sireBoxY + 58f
        canvas.drawRect(40f, damBoxY, 280f, damBoxY + 50f, cardBgPaint)
        canvas.drawRect(40f, damBoxY, 280f, damBoxY + 50f, borderPaint)
        canvas.drawText("DAM (Mother / مادر):", 48f, damBoxY + 16f, boldTextPaint)
        canvas.drawText(pedigree?.damRing ?: bird.motherRing ?: "Ring: Not Recorded", 48f, damBoxY + 34f, textPaint)

        // Paternal Grandparents
        val pgSireY = sireBoxY
        canvas.drawRect(290f, pgSireY, 555f, pgSireY + 23f, cardBgPaint)
        canvas.drawRect(290f, pgSireY, 555f, pgSireY + 23f, borderPaint)
        canvas.drawText("Paternal Grandsire: ${pedigree?.paternalGrandsire ?: "-"}", 298f, pgSireY + 15f, textPaint)

        val pgDamY = sireBoxY + 27f
        canvas.drawRect(290f, pgDamY, 555f, pgDamY + 23f, cardBgPaint)
        canvas.drawRect(290f, pgDamY, 555f, pgDamY + 23f, borderPaint)
        canvas.drawText("Paternal Granddam: ${pedigree?.paternalGranddam ?: "-"}", 298f, pgDamY + 15f, textPaint)

        // Maternal Grandparents
        val mgSireY = damBoxY
        canvas.drawRect(290f, mgSireY, 555f, mgSireY + 23f, cardBgPaint)
        canvas.drawRect(290f, mgSireY, 555f, mgSireY + 23f, borderPaint)
        canvas.drawText("Maternal Grandsire: ${pedigree?.maternalGrandsire ?: "-"}", 298f, mgSireY + 15f, textPaint)

        val mgDamY = damBoxY + 27f
        canvas.drawRect(290f, mgDamY, 555f, mgDamY + 23f, cardBgPaint)
        canvas.drawRect(290f, mgDamY, 555f, mgDamY + 23f, borderPaint)
        canvas.drawText("Maternal Granddam: ${pedigree?.maternalGranddam ?: "-"}", 298f, mgDamY + 15f, textPaint)

        // Breeder & Inbreeding details
        y = damBoxY + 62f
        canvas.drawText("Breeder: ${pedigree?.breederName ?: "Aviary Master"} (Code: ${pedigree?.breederCode ?: "IR-AV-01"})", 42f, y, boldTextPaint)
        canvas.drawText("Inbreeding Coeff (COI): ${String.format(Locale.US, "%.2f%%", (pedigree?.inbreedingCoefficient ?: 0.0) * 100)}", 360f, y, boldTextPaint)

        // 3. Genetics Breakdown
        y += 24f
        canvas.drawText("GENETICS PROFILE / ژنتیک و مشخصات آللی", 40f, y, headerPaint)
        y += 10f
        canvas.drawRect(40f, y, 555f, y + 60f, cardBgPaint)
        canvas.drawRect(40f, y, 555f, y + 60f, borderPaint)

        val visualMut = genetics?.visualMutations ?: bird.mutation
        val splitMut = genetics?.splitMutations ?: "None detected"
        val baseSeries = genetics?.baseSeries ?: "GREEN/BLUE"
        val factors = buildString {
            if (genetics?.violetFactor == true) append("Violet ")
            if (genetics?.greyFactor == true) append("Grey ")
            if (genetics?.cinnamonFactor == true) append("Cinnamon ")
            if (genetics?.inoFactor == true) append("Ino ")
            if (isEmpty()) append("Standard Factors")
        }

        canvas.drawText("Visual Mutations: ", 50f, y + 18f, boldTextPaint)
        canvas.drawText(visualMut, 140f, y + 18f, textPaint)

        canvas.drawText("Base Series: ", 360f, y + 18f, boldTextPaint)
        canvas.drawText(baseSeries, 430f, y + 18f, textPaint)

        canvas.drawText("Split (Carrier): ", 50f, y + 36f, boldTextPaint)
        canvas.drawText(splitMut, 130f, y + 36f, textPaint)

        canvas.drawText("Special Factors: ", 50f, y + 52f, boldTextPaint)
        canvas.drawText(factors, 140f, y + 52f, textPaint)

        // 4. Health & Weight Records Summary
        y += 75f
        canvas.drawText("HEALTH & WEIGHT HISTORY / سوابق سلامت و رشد", 40f, y, headerPaint)
        y += 10f
        canvas.drawRect(40f, y, 555f, y + 50f, cardBgPaint)
        canvas.drawRect(40f, y, 555f, y + 50f, borderPaint)

        val latestWeight = birdWithDetails.weightHistory.maxByOrNull { it.recordedDate }
        val latestWeightStr = latestWeight?.let { "${it.weightGrams}g (${it.conditionScore})" } ?: "No weigh-ins"
        val healthCount = birdWithDetails.healthRecords.size
        val compCount = birdWithDetails.competitionScores.size
        val offSpringCount = children?.allChildren?.size ?: 0

        canvas.drawText("Latest Weight: $latestWeightStr", 50f, y + 18f, textPaint)
        canvas.drawText("Recorded Health Visits: $healthCount", 260f, y + 18f, textPaint)
        canvas.drawText("Recorded Offspring: $offSpringCount birds", 50f, y + 36f, textPaint)
        canvas.drawText("Competitions Entered: $compCount", 260f, y + 36f, textPaint)

        // 5. Verification Barcode / QR Simulation Block & Official Seal
        y += 65f
        canvas.drawRect(40f, y, 555f, y + 70f, accentCardBg)
        canvas.drawRect(40f, y, 555f, y + 70f, borderPaint)

        // Draw genuine high-resolution QR matrix for official record verification
        val qrBoxX = 54f
        val qrBoxY = y + 10f
        val qrBitmap = com.example.core.qr.QrCodeGenerator.generateQrBitmap(
            com.example.core.qr.QrCodeGenerator.getBirdQrPayload(bird.ringNumber),
            sizePixels = 200
        )
        val qrDestRect = android.graphics.RectF(qrBoxX, qrBoxY, qrBoxX + 50f, qrBoxY + 50f)
        canvas.drawBitmap(qrBitmap, null, qrDestRect, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.drawRect(qrBoxX, qrBoxY, qrBoxX + 50f, qrBoxY + 50f, borderPaint)

        canvas.drawText("OFFICIAL AVIARY CERTIFICATE VERIFICATION", 120f, y + 25f, boldTextPaint)
        canvas.drawText("Certificate ID: ${bird.syncId.take(16).uppercase()}", 120f, y + 42f, textPaint)
        canvas.drawText("Verified budgerigar registration from local aviary management database.", 120f, y + 56f, textPaint)

        // Footer
        canvas.drawText("Budgerigar Aviary Management System | Persian RTL & English LTR Supported", 120f, 795f, textPaint)

        document.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Pedigree_${bird.ringNumber}.pdf")
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

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Pedigree Certificate: ${pdfFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Pedigree Certificate")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
