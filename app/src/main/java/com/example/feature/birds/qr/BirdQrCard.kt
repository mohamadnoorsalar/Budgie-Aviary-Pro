package com.example.feature.birds.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.qr.QrCodeGenerator
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity

@Composable
fun UniversalQrCard(
    title: String,
    subtitle: String,
    codeIdentifier: String,
    payloadText: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matrixSize = 25
    val grid = remember(payloadText) {
        QrCodeGenerator.generateQrMatrix(payloadText, matrixSize)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("universal_qr_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // QR Code Container (white background with generous padding)
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .testTag("qr_canvas")
                ) {
                    val cellSize = size.width / matrixSize

                    for (r in 0 until matrixSize) {
                        for (c in 0 until matrixSize) {
                            if (grid[r][c]) {
                                drawRect(
                                    color = Color(0xFF1A1A1A),
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Code Label
            Text(
                text = codeIdentifier,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Deep-link Payload note
            Text(
                text = "کد استاندارد جهت الصاق به قفس یا حلقه پرنده\n(اسکن با دوربین مستقیماً پرونده این مورد را باز می‌کند)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_qr_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "اشتراک‌گذاری / Share", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BirdQrCard(
    bird: BirdEntity,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val payload = remember(bird.ringNumber) {
        QrCodeGenerator.getBirdQrPayload(bird.ringNumber)
    }

    UniversalQrCard(
        title = "شناسنامه QR پرنده / Bird QR ID",
        subtitle = "پلاک: ${bird.ringNumber} | ${bird.name ?: bird.variety.name}",
        codeIdentifier = bird.ringNumber,
        payloadText = payload,
        onShare = onShare,
        modifier = modifier
    )
}

@Composable
fun CageQrCard(
    cage: CageEntity,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val payload = remember(cage.code) {
        QrCodeGenerator.getCageQrPayload(cage.code)
    }

    UniversalQrCard(
        title = "کد QR اختصاصی قفس / Cage QR Tag",
        subtitle = "کد قفس: ${cage.code} | ظرفیت: ${cage.capacity} پرنده",
        codeIdentifier = "CAGE: ${cage.code}",
        payloadText = payload,
        onShare = onShare,
        modifier = modifier
    )
}
