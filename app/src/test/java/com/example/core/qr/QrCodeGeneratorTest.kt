package com.example.core.qr

import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrCodeGeneratorTest {

    @Test
    fun testGenerateAndParseBirdQrPayload() {
        val ring = "IR-2024-ENGLISH-001"
        val payload = QrCodeGenerator.getBirdQrPayload(ring)
        assertEquals("aviary://bird?ring=IR-2024-ENGLISH-001", payload)

        val parsed = QrCodeGenerator.parseQrCode(payload)
        assertTrue(parsed is QrPayload.BirdPayload)
        assertEquals(ring, (parsed as QrPayload.BirdPayload).ringNumber)
    }

    @Test
    fun testGenerateAndParseCageQrPayload() {
        val cageCode = "C-25-BREEDING"
        val payload = QrCodeGenerator.getCageQrPayload(cageCode)
        assertEquals("aviary://cage?code=C-25-BREEDING", payload)

        val parsed = QrCodeGenerator.parseQrCode(payload)
        assertTrue(parsed is QrPayload.CagePayload)
        assertEquals(cageCode, (parsed as QrPayload.CagePayload).cageCode)
    }

    @Test
    fun testParseAlternativeFormats() {
        val birdAlt = QrCodeGenerator.parseQrCode("BIRD:IR-999")
        assertTrue(birdAlt is QrPayload.BirdPayload)
        assertEquals("IR-999", (birdAlt as QrPayload.BirdPayload).ringNumber)

        val cageAlt = QrCodeGenerator.parseQrCode("CAGE:C-101")
        assertTrue(cageAlt is QrPayload.CagePayload)
        assertEquals("C-101", (cageAlt as QrPayload.CagePayload).cageCode)

        val cageRaw = QrCodeGenerator.parseQrCode("C-12")
        assertTrue(cageRaw is QrPayload.CagePayload)
        assertEquals("C-12", (cageRaw as QrPayload.CagePayload).cageCode)
    }

    @Test
    fun testHandleInvalidQrCodesGracefully() {
        val emptyResult = QrCodeGenerator.parseQrCode("")
        assertTrue(emptyResult is QrPayload.UnknownPayload)

        val randomUrl = QrCodeGenerator.parseQrCode("https://example.com/unknown")
        assertTrue(randomUrl is QrPayload.UnknownPayload)
        assertNotNull((randomUrl as QrPayload.UnknownPayload).errorReason)
    }

    @Test
    fun testMatrixAndBitmapGeneration() {
        val payload = "aviary://bird?ring=IR-01"
        val matrix = QrCodeGenerator.generateQrMatrix(payload, 25)
        assertEquals(25, matrix.size)
        assertEquals(25, matrix[0].size)

        // Finders at (0,0), (0, 18), (18, 0)
        assertTrue(matrix[0][0])
        assertTrue(matrix[0][24])
        assertTrue(matrix[24][0])

        val bitmap = QrCodeGenerator.generateQrBitmap(payload, 200)
        assertNotNull(bitmap)
        assertEquals(200, bitmap.width)
        assertEquals(200, bitmap.height)
    }
}
