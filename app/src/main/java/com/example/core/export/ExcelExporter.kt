package com.example.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExporter {

    /**
     * Generates a UTF-8 CSV with Byte Order Mark (BOM) so Microsoft Excel, LibreOffice Calc,
     * and Google Sheets correctly recognize Persian/Arabic UTF-8 characters without encoding distortion.
     */
    fun exportToCsv(
        context: Context,
        fileNamePrefix: String,
        headers: List<String>,
        rows: List<List<String>>
    ): File? {
        return try {
            val sanitizedPrefix = fileNamePrefix.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val fileName = "${sanitizedPrefix}_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM (\uFEFF)
                fos.write(0xEF)
                fos.write(0xBB)
                fos.write(0xBF)

                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Write header row
                    writer.write(headers.joinToString(",") { escapeCsvCell(it) })
                    writer.write("\r\n")

                    // Write data rows
                    for (row in rows) {
                        writer.write(row.joinToString(",") { escapeCsvCell(it) })
                        writer.write("\r\n")
                    }
                    writer.flush()
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsvCell(cell: String): String {
        var value = cell.replace("\"", "\"\"")
        if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            value = "\"$value\""
        }
        return value
    }

    fun shareCsv(context: Context, file: File, title: String = "Exported Report (Excel / CSV)") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
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
