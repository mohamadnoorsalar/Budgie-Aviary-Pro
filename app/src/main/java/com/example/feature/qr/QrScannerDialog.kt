package com.example.feature.qr

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.qr.QrCodeGenerator
import com.example.core.qr.QrPayload
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Full-featured QR Scanning Dialog.
 * Enables live viewfinder simulation with quick test tags and camera input / text barcode input.
 * When scanned:
 * - Safely looks up the exact bird or cage record in the database.
 * - Opens the exact record profile inside the app.
 * - Gracefully reports invalid/unknown QR codes without creating duplicates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerDialog(
    repository: AviaryRepository,
    onDismiss: () -> Unit,
    onOpenBird: (String) -> Unit,
    onOpenCage: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputQrText by remember { mutableStateOf("") }
    var isCheckingDatabase by remember { mutableStateOf(false) }

    var resolvedBird by remember { mutableStateOf<BirdEntity?>(null) }
    var resolvedCage by remember { mutableStateOf<CageEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Recent birds/cages for quick-scan demonstration and testing
    var registeredBirds by remember { mutableStateOf<List<BirdEntity>>(emptyList()) }
    var registeredCages by remember { mutableStateOf<List<CageEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        registeredBirds = repository.allBirds.first().take(5)
        registeredCages = repository.allCages.first().take(5)
    }

    fun processScannedCode(rawText: String) {
        if (rawText.isBlank()) return
        isCheckingDatabase = true
        errorMessage = null
        resolvedBird = null
        resolvedCage = null

        coroutineScope.launch {
            val payload = QrCodeGenerator.parseQrCode(rawText)

            when (payload) {
                is QrPayload.BirdPayload -> {
                    val bird = repository.getBirdByRing(payload.ringNumber)
                    if (bird != null) {
                        resolvedBird = bird
                    } else {
                        errorMessage = "پرنده‌ای با شماره پلاک ${payload.ringNumber} در پایگاه داده یافت نشد.\n(Bird not found in database. No duplicate was created)"
                    }
                }
                is QrPayload.CagePayload -> {
                    val cage = repository.allCages.first().firstOrNull { it.code.equals(payload.cageCode, ignoreCase = true) }
                    if (cage != null) {
                        resolvedCage = cage
                    } else {
                        errorMessage = "قفسی با شناسه ${payload.cageCode} در سیستم یافت نشد.\n(Cage not found in database. No duplicate was created)"
                    }
                }
                is QrPayload.UnknownPayload -> {
                    errorMessage = payload.errorReason
                }
            }
            isCheckingDatabase = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.testTag("qr_scanner_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اسکنر بارکد و QR / QR Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Viewfinder Animation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E272E)),
                    contentAlignment = Alignment.Center
                ) {
                    // QR viewfinder border
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    // Scanner status hint
                    Text(
                        text = "دوربین آماده اسکن بارکد حلقه یا برچسب قفس",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                // Manual Input or Scan Text Bar
                OutlinedTextField(
                    value = inputQrText,
                    onValueChange = {
                        inputQrText = it
                        if (it.isNotBlank()) {
                            processScannedCode(it)
                        }
                    },
                    label = { Text("ورود یا اسکن کد (aviary:// یا شماره پلاک)") },
                    placeholder = { Text("aviary://bird?ring=... یا aviary://cage?code=...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (inputQrText.isNotBlank()) {
                            IconButton(onClick = { inputQrText = ""; errorMessage = null; resolvedBird = null; resolvedCage = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_input_field")
                )

                // Quick Scan Chips for existing birds and cages
                if (registeredBirds.isNotEmpty() || registeredCages.isNotEmpty()) {
                    Text(
                        text = "کدهای سریع موجود در سالن جهت تست اسکن:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        registeredBirds.take(2).forEach { b ->
                            Surface(
                                onClick = {
                                    inputQrText = QrCodeGenerator.getBirdQrPayload(b.ringNumber)
                                    processScannedCode(inputQrText)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = b.ringNumber, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                        registeredCages.take(2).forEach { c ->
                            Surface(
                                onClick = {
                                    inputQrText = QrCodeGenerator.getCageQrPayload(c.code)
                                    processScannedCode(inputQrText)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "قفس ${c.code}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Loading
                if (isCheckingDatabase) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال جستجوی پرونده در پایگاه داده...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Error / Unknown QR Code Alert
                if (errorMessage != null && !isCheckingDatabase) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("qr_scan_error_card")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // SUCCESS: Resolved Bird Match
                if (resolvedBird != null) {
                    val bird = resolvedBird!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("scanned_bird_result_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "پرونده پرنده یافت شد",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "شماره پلاک: ${bird.ringNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "نام / نژاد: ${bird.name ?: "-"} | ${bird.variety.name}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "قفس فعلی: ${bird.cageCode ?: "ثبت نشده"}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenBird(bird.ringNumber)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("open_scanned_bird_button")
                            ) {
                                Text("مشاهده پرونده کامل پرنده")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // SUCCESS: Resolved Cage Match
                if (resolvedCage != null) {
                    val cage = resolvedCage!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("scanned_cage_result_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "پرونده قفس یافت شد",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "کد قفس: ${cage.code}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "نوع / ظرفیت: ${cage.type} | ${cage.capacity} پرنده", style = MaterialTheme.typography.bodySmall)
                            Text(text = "محل استقرار: ${cage.location ?: "سالن اصلی"}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenCage(cage.code)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("open_scanned_cage_button")
                            ) {
                                Text("مشاهده جزئیات و پرندگان قفس")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن / Close")
            }
        }
    )
}
