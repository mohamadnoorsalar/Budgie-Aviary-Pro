package com.example.feature.cages

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.common.BirdGender
import com.example.core.common.CageType
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.entity.CageEntity
import com.example.data.database.relation.CageWithDetails
import com.example.feature.cages.qr.CageQrCard
import com.example.feature.cages.qr.CageQrScannerDialog

@Composable
fun CagesScreen(
    viewModel: CagesViewModel,
    onNavigateToBird: (ringNumber: String) -> Unit = {},
    onNavigateToPair: (pairId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    var qrPreviewCage by remember { mutableStateOf<CageEntity?>(null) }

    // If a cage is selected, show the rich Cage Detail view!
    if (state.selectedCageDetails != null) {
        CageDetailScreen(
            cageDetails = state.selectedCageDetails!!,
            logs = state.selectedCageLogs,
            eggs = state.selectedCageEggs,
            chicks = state.selectedCageChicks,
            availableBirds = state.availableBirdsForAssignment,
            onBack = { viewModel.selectCage(null) },
            onCleanStatusToggle = { isClean ->
                viewModel.setCageCleanStatus(state.selectedCageDetails!!.cage.code, isClean)
            },
            onMoveBirdIn = { ring, reason ->
                viewModel.moveBirdToCage(ring, state.selectedCageDetails!!.cage.code, reason)
            },
            onRemoveBird = { ring, reason ->
                viewModel.removeBirdFromCage(ring, reason)
            },
            onAddEgg = { eggNum, fert ->
                state.selectedCageDetails?.activePair?.let { pair ->
                    viewModel.addEgg(pair.id, eggNum, fert)
                }
            },
            onAddChick = { ring, order ->
                state.selectedCageDetails?.activePair?.let { pair ->
                    viewModel.addChick(pair.id, ring, order)
                }
            },
            onUpdatePhoto = { uri ->
                viewModel.saveCage(state.selectedCageDetails!!.cage.copy(photoUri = uri))
            },
            onNavigateToBird = onNavigateToBird,
            onNavigateToPair = onNavigateToPair,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("cages_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Search and QR Scan Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cages_search_bar"),
                    placeholder = { Text(if (isFa) "جستجوی کد قفس، موقعیت، پرنده..." else "Search cages, location, bird ring...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Scan QR code button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .height(56.dp)
                        .clickable { viewModel.setScannerDialogOpen(true) }
                        .testTag("scan_cage_qr_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan Cage QR",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFa) "اسکن QR" else "Scan QR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filters Row: Type & Clean status
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterType == null && state.filterClean == null,
                        onClick = {
                            viewModel.setFilterType(null)
                            viewModel.setFilterClean(null)
                        },
                        label = { Text(if (isFa) "همه قفس‌ها (${state.totalCount})" else "All (${state.totalCount})") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.filterClean == true,
                        onClick = {
                            viewModel.setFilterClean(if (state.filterClean == true) null else true)
                        },
                        label = { Text(if (isFa) "تمیز و ضدعفونی" else "Clean") },
                        leadingIcon = {
                            Icon(Icons.Filled.CleaningServices, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = state.filterClean == false,
                        onClick = {
                            viewModel.setFilterClean(if (state.filterClean == false) null else false)
                        },
                        label = { Text(if (isFa) "نیاز به نظافت" else "Needs Cleaning") },
                        leadingIcon = {
                            Icon(Icons.Filled.CleaningServices, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                        }
                    )
                }
                items(CageType.values()) { type ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = {
                            viewModel.setFilterType(if (state.filterType == type) null else type)
                        },
                        label = { Text(type.name.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Cages
            if (state.cages.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.GridView,
                    title = stringResource(R.string.empty_cages_title),
                    description = stringResource(R.string.empty_cages_desc),
                    actionLabel = if (isFa) "تعریف قفس / باکس جدید" else "Register New Cage",
                    onActionClick = { viewModel.setAddDialogOpen(true) },
                    testTag = "cages_empty_view"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.cages, key = { it.cage.code }) { details ->
                        CageListItemCard(
                            details = details,
                            isFa = isFa,
                            onClick = { viewModel.selectCage(details.cage.code) },
                            onToggleClean = {
                                viewModel.setCageCleanStatus(details.cage.code, !details.cage.isClean)
                            },
                            onQrClick = { qrPreviewCage = details.cage },
                            onNavigateToBird = onNavigateToBird
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }

        // Add Cage FAB
        FloatingActionButton(
            onClick = { viewModel.setAddDialogOpen(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_cage_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Cage")
        }

        // Add Cage Dialog
        if (state.isAddDialogOpen) {
            AddCageDialog(
                isFa = isFa,
                onDismiss = { viewModel.setAddDialogOpen(false) },
                onConfirm = { cage -> viewModel.saveCage(cage) }
            )
        }

        // QR Scanner Dialog
        if (state.isScannerDialogOpen) {
            CageQrScannerDialog(
                availableCages = state.cages.map { it.cage },
                onCageScanned = { scannedCode ->
                    viewModel.selectCage(scannedCode)
                    viewModel.setScannerDialogOpen(false)
                },
                onDismiss = { viewModel.setScannerDialogOpen(false) }
            )
        }

        // Quick QR preview popup
        qrPreviewCage?.let { cage ->
            AlertDialog(
                onDismissRequest = { qrPreviewCage = null },
                confirmButton = {
                    Button(onClick = { qrPreviewCage = null }) {
                        Text(if (isFa) "بستن" else "Close")
                    }
                },
                text = {
                    CageQrCard(
                        cage = cage,
                        onShare = {}
                    )
                }
            )
        }
    }
}

@Composable
private fun CageListItemCard(
    details: CageWithDetails,
    isFa: Boolean,
    onClick: () -> Unit,
    onToggleClean: () -> Unit,
    onQrClick: () -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit
) {
    val cage = details.cage
    val birds = details.birds
    val activePair = details.activePair

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("cage_item_${cage.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Code, Type, Clean status & QR button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = cage.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${cage.type.name.replace("_", " ")}${cage.location?.let { " • $it" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 1-tap Clean status toggle badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (cage.isClean) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onToggleClean() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CleaningServices,
                                contentDescription = null,
                                tint = if (cage.isClean) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cage.isClean) (if (isFa) "تمیز" else "CLEAN") else (if (isFa) "نظافت" else "DIRTY"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cage.isClean) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "View QR",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Breeding Pair info if available
            if (activePair != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activePair.maleRingNumber} ♂ × ${activePair.femaleRingNumber} ♀",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${activePair.clutchCount} ${if (isFa) "دوره" else "clutches"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Resident Birds chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Pets, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFa) "ساکنان: ${birds.size} از ${cage.capacity}" else "Residents: ${birds.size} / ${cage.capacity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (birds.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(birds) { bird ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (bird.gender == BirdGender.MALE) Color(0xFF1E88E5).copy(alpha = 0.15f) else Color(0xFFE91E63).copy(alpha = 0.15f),
                                modifier = Modifier.clickable { onNavigateToBird(bird.ringNumber) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (bird.gender == BirdGender.MALE) Icons.Filled.Male else Icons.Filled.Female,
                                        contentDescription = null,
                                        tint = if (bird.gender == BirdGender.MALE) Color(0xFF1E88E5) else Color(0xFFE91E63),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = bird.ringNumber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCageDialog(
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (cage: CageEntity) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CageType.BREEDING_BOX) }
    var capacityText by remember { mutableStateOf("2") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isFa) "تعریف قفس / باکس تکثیر جدید" else "Register New Enclosure")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(if (isFa) "کد یا شناسه قفس (مثال: BOX-05)" else "Cage Code / ID (e.g. BOX-05)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_cage_code")
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isFa) "نوع قفس" else "Cage Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        CageType.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name.replace("_", " ")) },
                                onClick = {
                                    type = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text(if (isFa) "ظرفیت پرنده" else "Bird Capacity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (isFa) "موقعیت در سالن (مثال: ردیف ۲ بالا)" else "Location / Shelf (e.g. Rack 2 Top)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "توضیحات و مشخصات (اختیاری)" else "Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 2
                    onConfirm(
                        CageEntity(
                            code = code.trim().uppercase(),
                            type = type,
                            capacity = cap,
                            location = location.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    )
                },
                enabled = code.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_cage_button")
            ) {
                Text(if (isFa) "ذخیره قفس" else "Save Cage")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}
