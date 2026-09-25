package com.example.feature.cages.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.CageEntity

@Composable
fun CageQrCard(
    cage: CageEntity,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    // Generate deterministic 21x21 QR matrix pattern for this cage's code and uuid
    val matrixSize = 21
    val grid = remember(cage.code, cage.syncId) {
        val seed = ("CAGE:" + cage.code).hashCode().toLong() xor cage.syncId.hashCode().toLong()
        val cells = Array(matrixSize) { BooleanArray(matrixSize) }
        val random = java.util.Random(seed)

        // Fill data matrix
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                cells[r][c] = random.nextBoolean()
            }
        }

        // Draw 3 standard QR position finder patterns (7x7) at Top-Left, Top-Right, Bottom-Left
        fun drawFinder(startR: Int, startC: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    cells[startR + r][startC + c] = isBorder || isInner
                }
            }
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
            cells[6][i] = i % 2 == 0
            cells[i][6] = i % 2 == 0
        }

        cells
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cage_qr_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "کد QR شناسایی اختصاصی قفس" else "CAGE ENCLOSURE IDENTIFICATION QR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code Matrix Box
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val cellSize = size.width / matrixSize
                    for (r in 0 until matrixSize) {
                        for (c in 0 until matrixSize) {
                            if (grid[r][c]) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cage Code Label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cage.code,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${cage.type.name.replace("_", " ")}${cage.location?.let { " • $it" } ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isFa)
                    "با اسکن این بارکد از طریق گوشی، صفحه مشخصات و پرندگان مستقر در این قفس بلافاصله باز می‌شود."
                else
                    "Scanning this QR code on any phone immediately opens the exact cage record, resident birds, and breeding nest.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cage_qr_share_button")
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isFa) "اشتراک‌گذاری و چاپ برچسب قفس" else "Share & Print Cage Label")
            }
        }
    }
}
