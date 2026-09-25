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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Repeat
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
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.relation.PairWithBreedingDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PairsScreen(
    viewModel: PairsViewModel,
    onNavigateToBird: (ringNumber: String) -> Unit = {},
    onNavigateToCage: (cageCode: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    // If a pair is selected, show the rich PairDetailScreen
    if (state.selectedPairDetails != null) {
        PairDetailScreen(
            pairDetails = state.selectedPairDetails!!,
            onBack = { viewModel.selectPair(null) },
            onUpdateStatus = { status, endCurrent ->
                viewModel.updatePairStatus(state.selectedPairDetails!!.pair.id, status, endCurrent)
            },
            onAddClutch = { clutchNum ->
                viewModel.addClutch(state.selectedPairDetails!!.pair.id, clutchNum)
            },
            onAddEgg = { eggNum, fert ->
                viewModel.addEgg(state.selectedPairDetails!!.pair.id, eggNum, fert)
            },
            onUpdateEggFertility = { egg, fert, hatched ->
                viewModel.updateEggFertility(egg, fert, hatched)
            },
            onAddChick = { ring, order ->
                viewModel.addChick(state.selectedPairDetails!!.pair.id, ring, order)
            },
            onSaveMatingHistory = { hist ->
                viewModel.saveMatingHistory(state.selectedPairDetails!!.pair.id, hist)
            },
            onSaveResults = { res ->
                viewModel.saveResults(state.selectedPairDetails!!.pair.id, res)
            },
            onSaveNotes = { n ->
                viewModel.saveNotes(state.selectedPairDetails!!.pair.id, n)
            },
            onNavigateToBird = onNavigateToBird,
            onNavigateToCage = onNavigateToCage,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("pairs_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pairs_search_bar"),
                placeholder = { Text(if (isFa) "جستجوی جفت، حلقه نر، حلقه ماده، قفس..." else "Search pairs, sire, dam, cage...") },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterStatus == null,
                        onClick = { viewModel.setFilterStatus(null) },
                        label = { Text(if (isFa) "همه جفت‌ها (${state.totalCount})" else "All Pairs (${state.totalCount})") }
                    )
                }
                listOf("ACTIVE", "RESTING", "SEPARATED", "COMPLETED").forEach { status ->
                    item {
                        FilterChip(
                            selected = state.filterStatus.equals(status, ignoreCase = true),
                            onClick = {
                                viewModel.setFilterStatus(
                                    if (state.filterStatus.equals(status, ignoreCase = true)) null else status
                                )
                            },
                            label = { Text(status) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.pairs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.Favorite,
                    title = stringResource(R.string.empty_pairs_title),
                    description = stringResource(R.string.empty_pairs_desc),
                    actionLabel = if (isFa) "تشکیل جفت جدید" else "Create New Pair",
                    onActionClick = { viewModel.setAddDialogOpen(true) },
                    testTag = "pairs_empty_view"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.pairs, key = { it.pair.id }) { pairDetails ->
                        PairListItemCard(
                            pairDetails = pairDetails,
                            isFa = isFa,
                            dateFormat = dateFormat,
                            onClick = { viewModel.selectPair(pairDetails.pair.id) },
                            onNavigateToBird = onNavigateToBird,
                            onNavigateToCage = onNavigateToCage
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }

        // Add Pair FAB
        FloatingActionButton(
            onClick = { viewModel.setAddDialogOpen(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_pair_fab"),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Pair")
        }

        // Add Pair Dialog
        if (state.isAddDialogOpen) {
            AddPairDialog(
                availableMales = state.availableMales,
                availableFemales = state.availableFemales,
                availableCages = state.availableCages,
                isFa = isFa,
                onDismiss = { viewModel.setAddDialogOpen(false) },
                onConfirm = { male, female, cage, nestId, notes, mating ->
                    viewModel.addPair(male, female, cage, nestId, notes, mating)
                }
            )
        }
    }
}

@Composable
private fun PairListItemCard(
    pairDetails: PairWithBreedingDetails,
    isFa: Boolean,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onNavigateToBird: (ringNumber: String) -> Unit,
    onNavigateToCage: (cageCode: String) -> Unit
) {
    val pair = pairDetails.pair
    val male = pairDetails.maleBird
    val female = pairDetails.femaleBird
    val eggs = pairDetails.eggs
    val chicks = pairDetails.chicks

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pair_card_${pair.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Pair ID, Status & Cage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isFa) "جفت کد #${pair.id}" else "Pair #${pair.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${dateFormat.format(Date(pair.pairingDate))}${pair.endDate?.let { " ~ " + dateFormat.format(Date(it)) } ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Cage chip
                    pair.cageCode?.let { code ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { onNavigateToCage(code) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.GridView, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (pair.status.uppercase()) {
                            "ACTIVE" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                            "RESTING" -> Color(0xFF0288D1).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = pair.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (pair.status.uppercase()) {
                                "ACTIVE" -> Color(0xFF2E7D32)
                                "RESTING" -> Color(0xFF0288D1)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sire and Dam details with direct links to bird profiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sire
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onNavigateToBird(pair.maleRingNumber) }
                        .padding(4.dp)
                ) {
                    Icon(Icons.Filled.Male, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isFa) "نر (پدر)" else "Sire (Cock)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pair.maleRingNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }

                // Dam
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onNavigateToBird(pair.femaleRingNumber) }
                        .padding(4.dp)
                ) {
                    Icon(Icons.Filled.Female, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isFa) "ماده (مادر)" else "Dam (Hen)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pair.femaleRingNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE91E63)
                        )
                    }
                }

                // Clutches & Eggs & Chicks count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isFa) "تخم / جوجه" else "Eggs / Chicks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${eggs.size} / ${chicks.size}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Results or Notes Preview
            if (!pair.results.isNullOrBlank() || !pair.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pair.results ?: pair.notes ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPairDialog(
    availableMales: List<BirdEntity>,
    availableFemales: List<BirdEntity>,
    availableCages: List<CageEntity>,
    isFa: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (maleRing: String, femaleRing: String, cageCode: String?, nestId: String?, notes: String?, matingHistory: String?) -> Unit
) {
    var maleRing by remember { mutableStateOf(availableMales.firstOrNull()?.ringNumber ?: "") }
    var femaleRing by remember { mutableStateOf(availableFemales.firstOrNull()?.ringNumber ?: "") }
    var cageCode by remember { mutableStateOf(availableCages.firstOrNull()?.code ?: "") }
    var notes by remember { mutableStateOf("") }
    var matingHistory by remember { mutableStateOf("") }

    var maleExpanded by remember { mutableStateOf(false) }
    var femaleExpanded by remember { mutableStateOf(false) }
    var cageExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isFa) "تشکیل جفت جدید (بدون ثبت پرنده تکراری)" else "Form New Breeding Pair")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Male (Cock) Selector
                ExposedDropdownMenuBox(
                    expanded = maleExpanded,
                    onExpandedChange = { maleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = maleRing,
                        onValueChange = { maleRing = it },
                        label = { Text(if (isFa) "حلقه نر (پدر / Cock)" else "Sire Ring Number") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    if (availableMales.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = maleExpanded,
                            onDismissRequest = { maleExpanded = false }
                        ) {
                            availableMales.forEach { bird ->
                                DropdownMenuItem(
                                    text = { Text("${bird.ringNumber} • ${bird.variety.name.replace("_", " ")} (${bird.color})") },
                                    onClick = {
                                        maleRing = bird.ringNumber
                                        maleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Female (Hen) Selector
                ExposedDropdownMenuBox(
                    expanded = femaleExpanded,
                    onExpandedChange = { femaleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = femaleRing,
                        onValueChange = { femaleRing = it },
                        label = { Text(if (isFa) "حلقه ماده (مادر / Hen)" else "Dam Ring Number") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = femaleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    if (availableFemales.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = femaleExpanded,
                            onDismissRequest = { femaleExpanded = false }
                        ) {
                            availableFemales.forEach { bird ->
                                DropdownMenuItem(
                                    text = { Text("${bird.ringNumber} • ${bird.variety.name.replace("_", " ")} (${bird.color})") },
                                    onClick = {
                                        femaleRing = bird.ringNumber
                                        femaleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Cage Selector
                ExposedDropdownMenuBox(
                    expanded = cageExpanded,
                    onExpandedChange = { cageExpanded = it }
                ) {
                    OutlinedTextField(
                        value = cageCode,
                        onValueChange = { cageCode = it },
                        label = { Text(if (isFa) "قفس یا باکس تکثیر" else "Breeding Cage / Box") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cageExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    if (availableCages.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = cageExpanded,
                            onDismissRequest = { cageExpanded = false }
                        ) {
                            availableCages.forEach { cage ->
                                DropdownMenuItem(
                                    text = { Text("${cage.code} • ${cage.type.name.replace("_", " ")}") },
                                    onClick = {
                                        cageCode = cage.code
                                        cageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = matingHistory,
                    onValueChange = { matingHistory = it },
                    label = { Text(if (isFa) "مشاهدات اولیه رفتار جفت‌گیری (اختیاری)" else "Courtship observations (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "یادداشت‌ها و هدف اصلاح نژاد (اختیاری)" else "Breeding goals / Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        maleRing.trim().uppercase(),
                        femaleRing.trim().uppercase(),
                        cageCode.trim().uppercase().takeIf { it.isNotBlank() },
                        null,
                        notes.takeIf { it.isNotBlank() },
                        matingHistory.takeIf { it.isNotBlank() }
                    )
                },
                enabled = maleRing.isNotBlank() && femaleRing.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_pair_button")
            ) {
                Text(if (isFa) "تشکیل جفت" else "Create Pair")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}
