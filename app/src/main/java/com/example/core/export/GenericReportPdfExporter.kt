package com.example.core.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GenericReportPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun exportTableReport(
        context: Context,
        reportTitleEn: String,
        reportTitleFa: String,
        filterSummary: String,
        headers: List<String>,
        rows: List<List<String>>,
        isPersian: Boolean = false
    ): File? {
        val document = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 76, 38)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(70, 80, 95)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerCellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val dataCellPaint = Paint().apply {
            color = Color.rgb(35, 35, 35)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val emptyCellPaint = Paint().apply {
            color = Color.rgb(130, 130, 130)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(215, 225, 235)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(235, 245, 238)
            style = Paint.Style.FILL
        }

        val altRowBgPaint = Paint().apply {
            color = Color.rgb(250, 252, 254)
            style = Paint.Style.FILL
        }

        val rowsPerPage = 26
        val totalPages = maxOf(1, (rows.size + rowsPerPage - 1) / rowsPerPage)
        val colCount = maxOf(1, headers.size)
        val tableLeft = 30f
        val tableRight = 565f
        val tableWidth = tableRight - tableLeft
        val colWidth = tableWidth / colCount
        val rowHeight = 22f

        for (pageIdx in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIdx + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Outer borders
            canvas.drawRect(20f, 20f, 575f, 822f, borderPaint)
            canvas.drawRect(24f, 24f, 571f, 818f, borderPaint)

            var y = 48f
            val title = if (isPersian) reportTitleFa else reportTitleEn
            canvas.drawText(title, 32f, y, titlePaint)

            y += 14f
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val subText = "Generated: ${sdf.format(Date())} | Filter: $filterSummary | Total: ${rows.size} record(s)"
            canvas.drawText(subText, 32f, y, subTitlePaint)

            y += 10f
            canvas.drawLine(32f, y, 565f, y, borderPaint)

            // Draw Table Header
            y += 14f
            val headerTop = y
            canvas.drawRect(tableLeft, headerTop, tableRight, headerTop + rowHeight, headerBgPaint)
            canvas.drawRect(tableLeft, headerTop, tableRight, headerTop + rowHeight, borderPaint)

            for (c in 0 until colCount) {
                val cx = tableLeft + (c * colWidth)
                canvas.drawLine(cx, headerTop, cx, headerTop + rowHeight, borderPaint)
                val hText = headers.getOrNull(c) ?: ""
                canvas.drawText(hText.take(18), cx + 4f, headerTop + 14f, headerCellPaint)
            }

            // Draw Rows
            var currentY = headerTop + rowHeight
            val startRow = pageIdx * rowsPerPage
            val endRow = minOf(rows.size, startRow + rowsPerPage)

            for (r in startRow until endRow) {
                val rowData = rows[r]
                if ((r % 2) == 1) {
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, altRowBgPaint)
                }
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

                for (c in 0 until colCount) {
                    val cx = tableLeft + (c * colWidth)
                    canvas.drawLine(cx, currentY, cx, currentY + rowHeight, borderPaint)
                    val cellVal = rowData.getOrNull(c) ?: ""
                    if (cellVal.isBlank() || cellVal == "-") {
                        canvas.drawText("—", cx + 4f, currentY + 14f, emptyCellPaint)
                    } else {
                        canvas.drawText(cellVal.take(22), cx + 4f, currentY + 14f, dataCellPaint)
                    }
                }
                currentY += rowHeight
            }

            // Footer
            val footerText = "Budgerigar Aviary Management - Page ${pageIdx + 1} of $totalPages"
            canvas.drawText(footerText, 210f, 804f, subTitlePaint)

            document.finishPage(page)
        }

        val sanitizedTitle = reportTitleEn.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        return try {
            val file = File(context.cacheDir, "Report_${sanitizedTitle}_${System.currentTimeMillis()}.pdf")
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
}
