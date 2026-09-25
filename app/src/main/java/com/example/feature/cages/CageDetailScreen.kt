package com.example.feature.cages

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.common.BirdGender
import com.example.core.common.EggFertilityStatus
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.relation.CageWithDetails
import com.example.feature.cages.qr.CageQrCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CageDetailScreen(
    cageDetails: CageWithDetails,
    logs: List<AuditLogEntity>,
    eggs: List<EggEntity>,
    chicks: List<ChickEntity>,
    availableBirds: List<BirdEntity>,
    onBack: () -> Unit,
    onCleanStatusToggle: (Boolean) -> Unit,
    onMoveBirdIn: (birdRing: String, reason: String?) -> Unit,
    onRemoveBird: (birdRing: String, reason: String?) -> Unit,
    onAddEgg: (eggNumber: Int, fertility: EggFertilityStatus) -> Unit,
    onAddChick: (ringNumber: String?, hatchOrder: Int) -> Unit,
    onUpdatePhoto: (uri: String) -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit,
    onNavigateToPair: (pairId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddBirdDialog by remember { mutableStateOf(false) }
    var showAddEggDialog by remember { mutableStateOf(false) }
    var showAddChickDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onUpdatePhoto(it.toString()) }
    }

    val tabs = listOf(
        if (isFa) "پرندگان و جفت" else "Birds & Pair",
        if (isFa) "لانه، تخم و جوجه‌ها" else "Nest, Eggs & Chicks",
        if (isFa) "تاریخچه جابه‌جایی و بهداشت" else "Movement History",
        if (isFa) "بارکد QR اختصاصی" else "Cage QR Code"
    )

    val cage = cageDetails.cage
    val activePair = cageDetails.activePair
    val nest = cageDetails.primaryNest

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("cage_detail_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cage_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isFa) "شناسنامه قفس ${cage.code}" else "Cage Profile ${cage.code}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${cage.type.name.replace("_", " ")} • ${cage.location ?: if (isFa) "موقعیت نامشخص" else "General"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Clean status indicator button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (cage.isClean) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f),
                    modifier = Modifier.clickable { onCleanStatusToggle(!cage.isClean) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = null,
                            tint = if (cage.isClean) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cage.isClean) (if (isFa) "ضدعفونی شده" else "CLEAN") else (if (isFa) "نیاز به نظافت" else "NEEDS CLEANING"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (cage.isClean) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }
        }

        // Header Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cage Photo or stylized icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { photoPickerLauncher.launch("image/*") }
                        .testTag("cage_photo_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cage.code,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFa) "ظرفیت استاندارد: ${cage.capacity} پرنده (ساکن فعلی: ${cageDetails.birds.size})" else "Capacity: ${cage.capacity} birds (Current: ${cageDetails.birds.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    cage.lastCleanedDate?.let {
                        Text(
                            text = if (isFa) "آخرین نظافت: ${dayFormat.format(Date(it))}" else "Last cleaned: ${dayFormat.format(Date(it))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> ResidentBirdsAndPairTab(
                    cageDetails = cageDetails,
                    isFa = isFa,
                    onAddBirdClick = { showAddBirdDialog = true },
                    onRemoveBirdClick = onRemoveBird,
                    onNavigateToBird = onNavigateToBird,
                    onNavigateToPair = onNavigateToPair
                )
                1 -> NestEggsAndChicksTab(
                    cageDetails = cageDetails,
                    eggs = eggs,
                    chicks = chicks,
                    isFa = isFa,
                    dayFormat = dayFormat,
                    onAddEggClick = { showAddEggDialog = true },
                    onAddChickClick = { showAddChickDialog = true }
                )
                2 -> MovementHistoryTab(
                    logs = logs,
                    isFa = isFa,
                    dateFormat = dateFormat
                )
                3 -> CageQrCard(
                    cage = cage,
                    onShare = {
                        val shareText = "Aviary Cage Enclosure ID: ${cage.code}\nType: ${cage.type}\nLocation: ${cage.location ?: "General"}\nStatus: ${if (cage.isClean) "Clean" else "Needs Cleaning"}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Cage ${cage.code}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Cage QR Data"))
                    }
                )
            }
        }
    }

    // Dialog: Add/Transfer Bird to Cage
    if (showAddBirdDialog) {
        AddBirdToCageDialog(
            availableBirds = availableBirds,
            isFa = isFa,
            onDismiss = { showAddBirdDialog = false },
            onConfirm = { ring, reason ->
                onMoveBirdIn(ring, reason)
                showAddBirdDialog = false
            }
        )
    }

    // Dialog: Add Egg
    if (showAddEggDialog && activePair != null) {
        AddEggDialog(
            nextEggNumber = eggs.size + 1,
            isFa = isFa,
            onDismiss = { showAddEggDialog = false },
            onConfirm = { eggNum, fert ->
                onAddEgg(eggNum, fert)
                showAddEggDialog = false
            }
        )
    }

    // Dialog: Add Chick
    if (showAddChickDialog && activePair != null) {
        AddChickDialog(
            nextHatchOrder = chicks.size + 1,
            isFa = isFa,
            onDismiss = { showAddChickDialog = false },
            onConfirm = { ring, order ->
                onAddChick(ring, order)
                showAddChickDialog = false
            }
        )
    }
}

@Composable
private fun ResidentBirdsAndPairTab(
    cageDetails: CageWithDetails,
    isFa: Boolean,
    onAddBirdClick: () -> Unit,
    onRemoveBirdClick: (ringNumber: String, reason: String?) -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit,
    onNavigateToPair: (pairId: Long) -> Unit
) {
    val birds = cageDetails.birds
    val activePair = cageDetails.activePair

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Active Breeding Pair
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "جفت مولد مستقر در باکس" else "Active Breeding Pair",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (activePair != null) {
                    TextButton(onClick = { onNavigateToPair(activePair.id) }) {
                        Text(if (isFa) "مشاهده جزئیات جفت" else "View Pair Details")
                    }
                }
            }

            if (activePair == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (isFa) "هیچ جفت فعالی در حال حاضر در این قفس ثبت نشده است." else "No active breeding pair currently assigned to this enclosure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPair(activePair.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFa) "جفت کد #${activePair.id}" else "Pair #${activePair.id}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = activePair.status,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Sire
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToBird(activePair.maleRingNumber) }
                            ) {
                                Icon(Icons.Filled.Male, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(if (isFa) "نر (پدر)" else "Sire", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = activePair.maleRingNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF1E88E5)
                                    )
                                }
                            }

                            // Dam
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToBird(activePair.femaleRingNumber) }
                            ) {
                                Icon(Icons.Filled.Female, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(if (isFa) "ماده (مادر)" else "Dam", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = activePair.femaleRingNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFE91E63)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Resident Birds List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "پرندگان ساکن در قفس (${birds.size})" else "Resident Birds (${birds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddBirdClick,
                    modifier = Modifier.testTag("transfer_bird_in_button")
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "انتقال پرنده به قفس" else "Move Bird In")
                }
            }
        }

        if (birds.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFa) "در حال حاضر هیچ پرنده‌ای به این قفس تخصیص داده نشده است." else "No birds currently reside in this cage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(birds, key = { it.ringNumber }) { bird ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBird(bird.ringNumber) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (bird.gender == BirdGender.MALE) Color(0xFF1E88E5).copy(alpha = 0.15f)
                                        else Color(0xFFE91E63).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (bird.gender == BirdGender.MALE) Icons.Filled.Male else Icons.Filled.Female,
                                    contentDescription = null,
                                    tint = if (bird.gender == BirdGender.MALE) Color(0xFF1E88E5) else Color(0xFFE91E63)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = bird.ringNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${bird.gender.name} • ${bird.variety.name.replace("_", " ")} • ${bird.color}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { onRemoveBirdClick(bird.ringNumber, "Transferred out of cage") },
                            modifier = Modifier.testTag("remove_bird_${bird.ringNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Move out",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NestEggsAndChicksTab(
    cageDetails: CageWithDetails,
    eggs: List<EggEntity>,
    chicks: List<ChickEntity>,
    isFa: Boolean,
    dayFormat: SimpleDateFormat,
    onAddEggClick: () -> Unit,
    onAddChickClick: () -> Unit
) {
    val nest = cageDetails.primaryNest
    val activePair = cageDetails.activePair

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Nest Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Home, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (nest != null) (if (isFa) "لانه باکس #${nest.boxNumber}" else "Nest Box #${nest.boxNumber}") else (if (isFa) "لانه تخم‌گذاری" else "Breeding Nest Box"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (nest?.isClean == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                        ) {
                            Text(
                                text = if (nest?.isClean == true) (if (isFa) "لانه آماده" else "READY") else (if (isFa) "نیاز به بررسی" else "CHECK"),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (nest != null)
                            (if (isFa) "پوشال و مواد لانه: ${nest.nestMaterial ?: "استاندارد"} • تاریخ نصب: ${dayFormat.format(Date(nest.installedDate))}" else "Nesting material: ${nest.nestMaterial ?: "Standard"} • Installed: ${dayFormat.format(Date(nest.installedDate))}")
                        else
                            (if (isFa) "باکس مجهز به لانه استاندارد چوبی تهویه‌دار." else "Equipped with standard ventilated wooden breeding nest box."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Eggs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "تخم‌های داخل لانه (${eggs.size})" else "Eggs in Nest (${eggs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (activePair != null) {
                    OutlinedButton(onClick = onAddEggClick) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFa) "ثبت تخم جدید" else "Log Egg")
                    }
                }
            }
        }

        if (eggs.isEmpty()) {
            item {
                Text(
                    text = if (isFa) "هنوز تخمی در این لانه ثبت نشده است." else "No eggs logged yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(eggs) { egg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Egg, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isFa) "تخم شماره #${egg.eggNumber}" else "Egg #${egg.eggNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFa) "تاریخ تخم‌گذاری: ${dayFormat.format(Date(egg.layDate))}" else "Laid: ${dayFormat.format(Date(egg.layDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (egg.fertilityStatus) {
                                "FERTILE" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                "INFERTILE" -> Color(0xFFC62828).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (egg.isHatched) (if (isFa) "جوجه شد" else "HATCHED") else egg.fertilityStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (egg.fertilityStatus) {
                                    "FERTILE" -> Color(0xFF2E7D32)
                                    "INFERTILE" -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chicks Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "جوجه‌های متولد شده (${chicks.size})" else "Hatched Chicks (${chicks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (activePair != null) {
                    OutlinedButton(onClick = onAddChickClick) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFa) "ثبت تولد جوجه" else "Log Chick")
                    }
                }
            }
        }

        if (chicks.isEmpty()) {
            item {
                Text(
                    text = if (isFa) "هنوز جوجه‌ای در این دوره متولد نشده است." else "No chicks hatched yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(chicks) { chick ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = chick.bandedRingNumber?.let { if (isFa) "حلقه $it" else "Ring $it" } ?: (if (isFa) "جوجه #${chick.hatchOrder}" else "Chick #${chick.hatchOrder}"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isFa) "تولد: ${dayFormat.format(Date(chick.hatchDate))}" else "Hatch date: ${dayFormat.format(Date(chick.hatchDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = chick.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovementHistoryTab(
    logs: List<AuditLogEntity>,
    isFa: Boolean,
    dateFormat: SimpleDateFormat
) {
    if (logs.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFa) "هیچ سابقه‌ای از جابه‌جایی یا رویداد نظافت ثبت نشده است." else "No movement or sanitation logs recorded for this enclosure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (log.actionType) {
                                        "MOVEMENT" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        "CLEANING" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (log.actionType) {
                                    "MOVEMENT" -> Icons.Filled.SwapHoriz
                                    "CLEANING" -> Icons.Filled.CleaningServices
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = when (log.actionType) {
                                    "MOVEMENT" -> MaterialTheme.colorScheme.primary
                                    "CLEANING" -> Color(0xFF2E7D32)
                                    else -> MaterialTheme.colorScheme.secondary
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBirdToCageDialog(
    availableBirds: List<BirdEntity>,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (birdRing: String, reason: String?) -> Unit
) {
    var selectedRing by remember { mutableStateOf(availableBirds.firstOrNull()?.ringNumber ?: "") }
    var reason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isFa) "انتقال پرنده به این قفس" else "Assign / Move Bird into Cage")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isFa) "پرنده مورد نظر را از لیست پرندگان سالن انتخاب کنید (بدون ایجاد رکورد تکراری):" else "Select existing bird to transfer without creating duplicates:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (availableBirds.isEmpty()) {
                    Text(
                        text = if (isFa) "همه پرندگان در حال حاضر در این قفس مستقر هستند یا پرنده‌ای ثبت نشده است." else "All birds are already in this cage or no birds exist.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRing,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isFa) "شماره حلقه پرنده" else "Bird Ring Number") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableBirds.forEach { bird ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${bird.ringNumber} • ${bird.gender.name} • ${bird.variety.name.replace("_", " ")}")
                                    },
                                    onClick = {
                                        selectedRing = bird.ringNumber
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(if (isFa) "علت جابه‌جایی (اختیاری)" else "Reason for transfer (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRing, reason.takeIf { it.isNotBlank() }) },
                enabled = selectedRing.isNotBlank()
            ) {
                Text(if (isFa) "ثبت انتقال" else "Confirm Transfer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun AddEggDialog(
    nextEggNumber: Int,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (eggNumber: Int, fertility: EggFertilityStatus) -> Unit
) {
    var fertility by remember { mutableStateOf(EggFertilityStatus.PENDING) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "ثبت تخم جدید" else "Log New Egg") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isFa) "تخم شماره #$nextEggNumber برای جفت فعال این قفس ثبت می‌شود." else "Logging egg #$nextEggNumber for the active pair in this cage.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(if (isFa) "وضعیت اولیه نطفه:" else "Initial Fertility Status:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (status in EggFertilityStatus.entries.take(3)) {
                        FilterChip(
                            selected = fertility == status,
                            onClick = { fertility = status },
                            label = { Text(status.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nextEggNumber, fertility) }) {
                Text(if (isFa) "ثبت تخم" else "Save Egg")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun AddChickDialog(
    nextHatchOrder: Int,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ringNumber: String?, hatchOrder: Int) -> Unit
) {
    var ringNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "ثبت تولد جوجه" else "Log Chick Hatch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isFa) "جوجه شماره #$nextHatchOrder متولد شده در لانه این قفس." else "Hatching chick #$nextHatchOrder in this enclosure nest.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = ringNumber,
                    onValueChange = { ringNumber = it },
                    label = { Text(if (isFa) "شماره حلقه اختصاصی (اختیاری)" else "Band Ring Number (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(ringNumber.takeIf { it.isNotBlank() }, nextHatchOrder) }) {
                Text(if (isFa) "ثبت تولد" else "Confirm Hatch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}
