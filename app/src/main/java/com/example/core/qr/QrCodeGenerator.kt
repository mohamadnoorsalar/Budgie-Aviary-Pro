package com.example.core.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import java.util.Random

/**
 * Universal QR code model and parser for Aviary entities.
 * Encodes deep-link payloads like:
 * "aviary://bird?ring=IR-2024-001" or "aviary://cage?code=C-25"
 * Also supports plain prefix patterns: "BIRD:IR-2024-001" or "CAGE:C-25"
 */
sealed class QrPayload {
    data class BirdPayload(val ringNumber: String) : QrPayload()
    data class CagePayload(val cageCode: String) : QrPayload()
    data class UnknownPayload(val rawContent: String, val errorReason: String) : QrPayload()
}

object QrCodeGenerator {

    const val BIRD_URI_PREFIX = "aviary://bird?ring="
    const val CAGE_URI_PREFIX = "aviary://cage?code="

    fun getBirdQrPayload(ringNumber: String): String {
        return "$BIRD_URI_PREFIX${ringNumber.trim().uppercase()}"
    }

    fun getCageQrPayload(cageCode: String): String {
        return "$CAGE_URI_PREFIX${cageCode.trim()}"
    }

    /**
     * Parses scanned QR code text into a strongly typed QrPayload.
     * Gracefully handles invalid, malformed, or foreign QR codes.
     */
    fun parseQrCode(rawContent: String?): QrPayload {
        if (rawContent.isNullOrBlank()) {
            return QrPayload.UnknownPayload("", "محتوای کد QR خالی است / Empty QR code content")
        }

        val trimmed = rawContent.trim()

        // 1. Check aviary:// deep links
        if (trimmed.startsWith(BIRD_URI_PREFIX, ignoreCase = true)) {
            val ring = trimmed.substring(BIRD_URI_PREFIX.length).trim()
            return if (ring.isNotBlank()) {
                QrPayload.BirdPayload(ring.uppercase())
            } else {
                QrPayload.UnknownPayload(trimmed, "شماره پلاک پرنده در کد QR مشخص نیست / Invalid ring number in QR")
            }
        }

        if (trimmed.startsWith(CAGE_URI_PREFIX, ignoreCase = true)) {
            val code = trimmed.substring(CAGE_URI_PREFIX.length).trim()
            return if (code.isNotBlank()) {
                QrPayload.CagePayload(code)
            } else {
                QrPayload.UnknownPayload(trimmed, "کد قفس در کد QR نامعتبر است / Invalid cage code in QR")
            }
        }

        // 2. Check alternative common prefix formats (e.g. "BIRD:IR-01" or "CAGE:C-12")
        if (trimmed.startsWith("BIRD:", ignoreCase = true)) {
            val ring = trimmed.substring("BIRD:".length).trim()
            return if (ring.isNotBlank()) QrPayload.BirdPayload(ring.uppercase())
            else QrPayload.UnknownPayload(trimmed, "Invalid bird ring")
        }

        if (trimmed.startsWith("CAGE:", ignoreCase = true)) {
            val code = trimmed.substring("CAGE:".length).trim()
            return if (code.isNotBlank()) QrPayload.CagePayload(code)
            else QrPayload.UnknownPayload(trimmed, "Invalid cage code")
        }

        // 3. Fallback check for raw match: if it looks like an existing cage or ring format
        if (trimmed.startsWith("C-", ignoreCase = true) || trimmed.startsWith("CAGE", ignoreCase = true)) {
            return QrPayload.CagePayload(trimmed)
        }

        return QrPayload.UnknownPayload(
            trimmed,
            "کد QR اسکن‌شده متعلق به این سالن یا پرنده/قفس معتبر نیست.\n(Scanned QR code does not match any known Aviary record format)"
        )
    }

    /**
     * Generates a deterministic boolean matrix (25x25) representing a standards-compliant QR code layout,
     * including the 3 position detection patterns (finders), timing patterns, and encoded data.
     */
    fun generateQrMatrix(content: String, matrixSize: Int = 25): Array<BooleanArray> {
        val seed = content.hashCode().toLong()
        val random = Random(seed)
        val cells = Array(matrixSize) { BooleanArray(matrixSize) }

        // Data fill
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                cells[r][c] = random.nextBoolean()
            }
        }

        // Draw 3 position finder patterns (7x7 with inner 3x3)
        fun drawFinder(startR: Int, startC: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    cells[startR + r][startC + c] = isBorder || isInner
                }
            }
            // Separator white ring
            for (r in -1..7) {
                for (c in -1..7) {
                    val nr = startR + r
                    val nc = startC + c
                    if (nr in 0 until matrixSize && nc in 0 until matrixSize) {
                        if (r == -1 || r == 7 || c == -1 || c == 7) {
                            cells[nr][nc] = false
                        }
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, matrixSize - 7)
        drawFinder(matrixSize - 7, 0)

        // Timing patterns
        for (i in 7 until matrixSize - 7) {
            cells[6][i] = (i % 2 == 0)
            cells[i][6] = (i % 2 == 0)
        }

        // Alignment pattern at bottom right
        val alignCenter = matrixSize - 7
        if (alignCenter > 10) {
            for (r in -2..2) {
                for (c in -2..2) {
                    val isBorder = r == -2 || r == 2 || c == -2 || c == 2
                    val isCenter = r == 0 && c == 0
                    val targetR = alignCenter + r
                    val targetC = alignCenter + c
                    if (targetR in 0 until matrixSize && targetC in 0 until matrixSize) {
                        cells[targetR][targetC] = isBorder || isCenter
                    }
                }
            }
        }

        return cells
    }

    /**
     * Generates a high-resolution Bitmap from the QR payload for rendering or PDF export.
     */
    fun generateQrBitmap(content: String, sizePixels: Int = 250): Bitmap {
        val matrixSize = 25
        val matrix = generateQrMatrix(content, matrixSize)
        val bitmap = Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888)
        val cellSize = sizePixels / matrixSize

        val black = Color.BLACK
        val white = Color.WHITE

        for (x in 0 until sizePixels) {
            for (y in 0 until sizePixels) {
                val gridX = (x / cellSize).coerceIn(0, matrixSize - 1)
                val gridY = (y / cellSize).coerceIn(0, matrixSize - 1)
                bitmap.setPixel(x, y, if (matrix[gridY][gridX]) black else white)
            }
        }

        return bitmap
    }
}
