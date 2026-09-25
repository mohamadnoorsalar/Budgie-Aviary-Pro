package com.example.feature.pairs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.common.BirdGender
import com.example.core.common.EggFertilityStatus
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.relation.PairWithBreedingDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDetailScreen(
    pairDetails: PairWithBreedingDetails,
    onBack: () -> Unit,
    onUpdateStatus: (newStatus: String, endCurrent: Boolean) -> Unit,
    onAddClutch: (clutchNumber: Int) -> Unit,
    onAddEgg: (eggNumber: Int, fertility: EggFertilityStatus) -> Unit,
    onUpdateEggFertility: (egg: EggEntity, fertility: EggFertilityStatus, isHatched: Boolean) -> Unit,
    onAddChick: (ringNumber: String?, hatchOrder: Int) -> Unit,
    onSaveMatingHistory: (history: String) -> Unit,
    onSaveResults: (results: String) -> Unit,
    onSaveNotes: (notes: String) -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit,
    onNavigateToCage: (cageCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddEggDialog by remember { mutableStateOf(false) }
    var showAddChickDialog by remember { mutableStateOf(false) }
    var showEditMatingDialog by remember { mutableStateOf(false) }
    var showEditResultsDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        if (isFa) "اطلاعات جفت و مولدین" else "Profile & Mates",
        if (isFa) "دوره‌ها و لانه‌تخم" else "Clutches & Eggs",
        if (isFa) "جوجه‌های متولد شده" else "Chicks",
        if (isFa) "جفت‌گیری و نتایج" else "Mating & Results"
    )

    val pair = pairDetails.pair
    val male = pairDetails.maleBird
    val female = pairDetails.femaleBird
    val cage = pairDetails.cage
    val eggs = pairDetails.eggs
    val chicks = pairDetails.chicks
    val clutches = pairDetails.clutches

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pair_detail_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pair_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isFa) "شناسنامه جفت #${pair.id}" else "Pair Profile #${pair.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pair.maleRingNumber} ♂ × ${pair.femaleRingNumber} ♀",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (pair.status.uppercase()) {
                        "ACTIVE" -> Color(0xFF2E7D32).copy(alpha = 0.18f)
                        "RESTING" -> Color(0xFF0288D1).copy(alpha = 0.18f)
                        "SEPARATED" -> Color(0xFFE65100).copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = pair.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (pair.status.uppercase()) {
                            "ACTIVE" -> Color(0xFF2E7D32)
                            "RESTING" -> Color(0xFF0288D1)
                            "SEPARATED" -> Color(0xFFE65100)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Header Card with Sire and Dam Side-by-Side
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Male Sire
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToBird(pair.maleRingNumber) }
                            .testTag("sire_link_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5).copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E88E5).copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Male, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (isFa) "نر (پدر / Sire)" else "Cock (Sire)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1E88E5))
                            Text(
                                text = pair.maleRingNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            male?.let {
                                Text(
                                    text = it.variety.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }

                    // Female Dam
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToBird(pair.femaleRingNumber) }
                            .testTag("dam_link_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE91E63).copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Female, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (isFa) "ماده (مادر / Dam)" else "Hen (Dam)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE91E63))
                            Text(
                                text = pair.femaleRingNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            female?.let {
                                Text(
                                    text = it.variety.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isFa) "دوره‌ها" else "Clutches", style = MaterialTheme.typography.labelSmall)
                        Text("${clutches.size.coerceAtLeast(pair.clutchCount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isFa) "تخم‌ها" else "Eggs", style = MaterialTheme.typography.labelSmall)
                        Text("${eggs.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isFa) "جوجه‌ها" else "Chicks", style = MaterialTheme.typography.labelSmall)
                        Text("${chicks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isFa) "قفس" else "Cage", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = pair.cageCode ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { pair.cageCode?.let { onNavigateToCage(it) } }
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
                0 -> PairProfileTab(
                    pair = pair,
                    male = male,
                    female = female,
                    cage = cage,
                    isFa = isFa,
                    dateFormat = dateFormat,
                    onUpdateStatus = onUpdateStatus,
                    onNavigateToBird = onNavigateToBird,
                    onNavigateToCage = onNavigateToCage,
                    onEditNotes = { showEditNotesDialog = true }
                )
                1 -> ClutchesAndEggsTab(
                    pair = pair,
                    clutches = clutches,
                    eggs = eggs,
                    isFa = isFa,
                    dateFormat = dateFormat,
                    onAddClutch = { onAddClutch(clutches.size + 1) },
                    onAddEgg = { showAddEggDialog = true },
                    onUpdateEggFertility = onUpdateEggFertility
                )
                2 -> ChicksTab(
                    chicks = chicks,
                    isFa = isFa,
                    dateFormat = dateFormat,
                    onAddChick = { showAddChickDialog = true },
                    onNavigateToBird = onNavigateToBird
                )
                3 -> MatingAndResultsTab(
                    pair = pair,
                    isFa = isFa,
                    onEditMating = { showEditMatingDialog = true },
                    onEditResults = { showEditResultsDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showAddEggDialog) {
        AddEggDialog(
            nextEggNumber = eggs.size + 1,
            isFa = isFa,
            onDismiss = { showAddEggDialog = false },
            onConfirm = { num, fert ->
                onAddEgg(num, fert)
                showAddEggDialog = false
            }
        )
    }

    if (showAddChickDialog) {
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

    if (showEditMatingDialog) {
        EditTextDialog(
            title = if (isFa) "ویرایش سابقه جفت‌گیری و رفتار شناسی" else "Edit Mating & Courtship History",
            initialValue = pair.matingHistory ?: "",
            isFa = isFa,
            onDismiss = { showEditMatingDialog = false },
            onConfirm = {
                onSaveMatingHistory(it)
                showEditMatingDialog = false
            }
        )
    }

    if (showEditResultsDialog) {
        EditTextDialog(
            title = if (isFa) "ویرایش نتایج تکثیر و بازدهی ژنتیکی" else "Edit Breeding Results & Outcomes",
            initialValue = pair.results ?: "",
            isFa = isFa,
            onDismiss = { showEditResultsDialog = false },
            onConfirm = {
                onSaveResults(it)
                showEditResultsDialog = false
            }
        )
    }

    if (showEditNotesDialog) {
        EditTextDialog(
            title = if (isFa) "یادداشت‌های اختصاصی جفت" else "Pair Notes",
            initialValue = pair.notes ?: "",
            isFa = isFa,
            onDismiss = { showEditNotesDialog = false },
            onConfirm = {
                onSaveNotes(it)
                showEditNotesDialog = false
            }
        )
    }
}

@Composable
private fun PairProfileTab(
    pair: com.example.data.database.entity.PairEntity,
    male: com.example.data.database.entity.BirdEntity?,
    female: com.example.data.database.entity.BirdEntity?,
    cage: com.example.data.database.entity.CageEntity?,
    isFa: Boolean,
    dateFormat: SimpleDateFormat,
    onUpdateStatus: (newStatus: String, endCurrent: Boolean) -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit,
    onNavigateToCage: (cageCode: String) -> Unit,
    onEditNotes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(if (isFa) "تغییر وضعیت جفت:" else "Pair Status Management:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ACTIVE", "RESTING", "SEPARATED", "COMPLETED").forEach { s ->
                            FilterChip(
                                selected = pair.status.equals(s, ignoreCase = true),
                                onClick = { onUpdateStatus(s, s == "SEPARATED" || s == "COMPLETED") },
                                label = { Text(s) }
                            )
                        }
                    }
                }
            }
        }

        // Dates and Cage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "تاریخ شروع جفت‌اندازی" else "Pairing Date", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(dateFormat.format(Date(pair.pairingDate)), fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "تاریخ پایان جفت" else "End Date", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = pair.endDate?.let { dateFormat.format(Date(it)) } ?: (if (isFa) "جفت فعال و در حال تکثیر" else "Active / Ongoing"),
                            fontWeight = FontWeight.Bold,
                            color = if (pair.endDate != null) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                    }

                    pair.cageCode?.let { code ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToCage(code) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.GridView, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFa) "قفس مستقر" else "Assigned Enclosure", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "$code (${cage?.location ?: "Aviary"})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Notes section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "یادداشت‌های جفت" else "Pair Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onEditNotes) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit notes", modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pair.notes?.takeIf { it.isNotBlank() } ?: (if (isFa) "یادداشتی ثبت نشده است. جهت ثبت لمس کنید." else "No notes recorded. Tap to edit."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ClutchesAndEggsTab(
    pair: com.example.data.database.entity.PairEntity,
    clutches: List<ClutchEntity>,
    eggs: List<EggEntity>,
    isFa: Boolean,
    dateFormat: SimpleDateFormat,
    onAddClutch: () -> Unit,
    onAddEgg: () -> Unit,
    onUpdateEggFertility: (egg: EggEntity, fertility: EggFertilityStatus, isHatched: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Breeding Cycles (Clutches) Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "دوره‌های تخم‌گذاری (${clutches.size})" else "Breeding Cycles (${clutches.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onAddClutch) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "شروع دوره جدید" else "New Clutch")
                }
            }
        }

        if (clutches.isEmpty()) {
            item {
                Text(
                    text = if (isFa) "دوره‌ای برای این جفت ثبت نشده است." else "No clutches recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(clutches) { clutch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Repeat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isFa) "دوره تخم‌گذاری شماره #${clutch.clutchNumber}" else "Clutch #${clutch.clutchNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFa) "آغاز دوره: ${dateFormat.format(Date(clutch.startDate))}" else "Started: ${dateFormat.format(Date(clutch.startDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (clutch.isActive) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (clutch.isActive) (if (isFa) "فعال" else "ACTIVE") else (if (isFa) "پایان‌یافته" else "FINISHED"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (clutch.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Eggs Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "تخم‌های ثبت شده (${eggs.size})" else "Eggs Recorded (${eggs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onAddEgg) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "ثبت تخم" else "Add Egg")
                }
            }
        }

        if (eggs.isEmpty()) {
            item {
                Text(
                    text = if (isFa) "تخمی در این دوره ثبت نشده است." else "No eggs logged yet.",
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
                            Icon(Icons.Filled.Egg, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isFa) "تخم #${egg.eggNumber}" else "Egg #${egg.eggNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dateFormat.format(Date(egg.layDate))}${if (egg.isHatched) " • Hatched" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Cycle through fertility status on tap
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (egg.fertilityStatus) {
                                "FERTILE" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                "INFERTILE" -> Color(0xFFC62828).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.clickable {
                                val next = when (egg.fertilityStatus) {
                                    "PENDING", "UNCANDLED" -> EggFertilityStatus.FERTILE
                                    "FERTILE" -> EggFertilityStatus.INFERTILE
                                    "INFERTILE" -> EggFertilityStatus.PENDING
                                    else -> EggFertilityStatus.FERTILE
                                }
                                onUpdateEggFertility(egg, next, egg.isHatched)
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
    }
}

@Composable
private fun ChicksTab(
    chicks: List<ChickEntity>,
    isFa: Boolean,
    dateFormat: SimpleDateFormat,
    onAddChick: () -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "جوجه‌های متولد شده از این جفت (${chicks.size})" else "Hatched Offspring (${chicks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onAddChick) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "ثبت تولد جوجه" else "Log Chick")
                }
            }
        }

        if (chicks.isEmpty()) {
            item {
                Text(
                    text = if (isFa) "هنوز جوجه‌ای برای این جفت ثبت نشده است." else "No chicks recorded yet for this pair.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(chicks) { chick ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chick.bandedRingNumber?.let { onNavigateToBird(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Pets, null, tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = chick.bandedRingNumber?.let { if (isFa) "شماره حلقه: $it" else "Ring: $it" } ?: (if (isFa) "جوجه شماره #${chick.hatchOrder}" else "Chick #${chick.hatchOrder}"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isFa) "تاریخ تولد: ${dateFormat.format(Date(chick.hatchDate))}" else "Hatch Date: ${dateFormat.format(Date(chick.hatchDate))}",
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
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
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
private fun MatingAndResultsTab(
    pair: com.example.data.database.entity.PairEntity,
    isFa: Boolean,
    onEditMating: () -> Unit,
    onEditResults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mating History
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Psychology, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFa) "تاریخچه جفت‌گیری و رفتار شناسی" else "Mating History & Courtship Behavior",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onEditMating) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pair.matingHistory?.takeIf { it.isNotBlank() } ?: (if (isFa) "مشاهدات جفت‌گیری، تغذیه متقابل، یا رفتار ورود به لانه هنوز ثبت نشده است." else "No courtship or copulation observations logged yet."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Breeding Results
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFa) "نتایج تکثیر و بازدهی ژنتیکی" else "Breeding Results & Offspring Quality",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onEditResults) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pair.results?.takeIf { it.isNotBlank() } ?: (if (isFa) "نتایج کیفی جوجه‌ها، انطباق جهش‌های رنگی و عملکرد تکثیر هنوز ثبت نشده است." else "No final breeding results or genetic mutation outcomes logged yet."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
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
        title = { Text(if (isFa) "ثبت تخم جدید برای جفت" else "Log New Egg for Pair") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isFa) "تخم شماره #$nextEggNumber در تاریخ جاری ثبت می‌شود." else "Logging egg #$nextEggNumber today.",
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
        title = { Text(if (isFa) "ثبت تولد جوجه جدید" else "Log Chick Hatch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isFa) "جوجه شماره #$nextHatchOrder برای این جفت متولد شد." else "Hatching chick #$nextHatchOrder for this pair.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = ringNumber,
                    onValueChange = { ringNumber = it },
                    label = { Text(if (isFa) "شماره پلاک یا حلقه اختصاصی (اختیاری)" else "Band Ring Number (optional)") },
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

@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(if (isFa) "متن توضیحات" else "Description / Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text.trim()) }) {
                Text(if (isFa) "ذخیره" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}
