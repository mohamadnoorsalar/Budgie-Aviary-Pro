package com.example.feature.reproduction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.core.ui.components.StatMetricCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReproductionScreen(
    viewModel: ReproductionViewModel,
    modifier: Modifier = Modifier,
    onNavigateToBird: ((String) -> Unit)? = null,
    onNavigateToCage: ((String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("reproduction_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (state.selectedTab) {
                        ReproductionTab.NESTS -> viewModel.openAddNestDialog()
                        ReproductionTab.TIMELINES_AND_REMINDERS -> viewModel.syncUpcomingReminders()
                        ReproductionTab.PAIRS_AND_CLUTCHES -> viewModel.openAddClutchDialog()
                        else -> viewModel.openAddEggDialog()
                    }
                },
                modifier = Modifier.testTag("reproduction_fab")
            ) {
                Icon(
                    imageVector = if (state.selectedTab == ReproductionTab.TIMELINES_AND_REMINDERS) {
                        Icons.Default.NotificationsActive
                    } else {
                        Icons.Default.Add
                    },
                    contentDescription = "Action"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-Level Stat Metrics Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = stringResource(R.string.reproduction_stat_total_eggs),
                    value = state.totalEggCount.toString(),
                    icon = Icons.Default.Egg,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(130.dp)
                )

                StatMetricCard(
                    title = stringResource(R.string.reproduction_stat_fertile_rate),
                    value = "${String.format(Locale.US, "%.1f", state.fertilityRate)}%",
                    icon = Icons.Default.CheckCircle,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.width(130.dp)
                )

                StatMetricCard(
                    title = stringResource(R.string.reproduction_stat_hatch_rate),
                    value = "${String.format(Locale.US, "%.1f", state.hatchRate)}%",
                    icon = Icons.Default.ChildCare,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.width(130.dp)
                )

                StatMetricCard(
                    title = stringResource(R.string.reproduction_stat_chicks),
                    value = state.activeChickCount.toString(),
                    icon = Icons.Default.ChildCare,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(130.dp)
                )

                StatMetricCard(
                    title = stringResource(R.string.reproduction_stat_mortality),
                    value = state.mortalityCount.toString(),
                    icon = Icons.Default.Egg,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.width(130.dp)
                )
            }

            // Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth().testTag("reproduction_tab_row")
            ) {
                Tab(
                    selected = state.selectedTab == ReproductionTab.PAIRS_AND_CLUTCHES,
                    onClick = { viewModel.selectTab(ReproductionTab.PAIRS_AND_CLUTCHES) },
                    text = { Text(stringResource(R.string.reproduction_tab_clutches)) },
                    modifier = Modifier.testTag("tab_clutches")
                )
                Tab(
                    selected = state.selectedTab == ReproductionTab.EGGS,
                    onClick = { viewModel.selectTab(ReproductionTab.EGGS) },
                    text = { Text(stringResource(R.string.reproduction_tab_eggs)) },
                    modifier = Modifier.testTag("tab_eggs")
                )
                Tab(
                    selected = state.selectedTab == ReproductionTab.CHICKS_NURSERY,
                    onClick = { viewModel.selectTab(ReproductionTab.CHICKS_NURSERY) },
                    text = { Text(stringResource(R.string.reproduction_tab_chicks)) },
                    modifier = Modifier.testTag("tab_chicks")
                )
                Tab(
                    selected = state.selectedTab == ReproductionTab.NESTS,
                    onClick = { viewModel.selectTab(ReproductionTab.NESTS) },
                    text = { Text(stringResource(R.string.reproduction_tab_nests)) },
                    modifier = Modifier.testTag("tab_nests")
                )
                Tab(
                    selected = state.selectedTab == ReproductionTab.TIMELINES_AND_REMINDERS,
                    onClick = { viewModel.selectTab(ReproductionTab.TIMELINES_AND_REMINDERS) },
                    text = { Text(stringResource(R.string.reproduction_tab_timeline)) },
                    modifier = Modifier.testTag("tab_timelines")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (state.selectedTab) {
                ReproductionTab.PAIRS_AND_CLUTCHES -> {
                    ClutchesTabView(
                        state = state,
                        isFa = isFa,
                        onAddEgg = { pairId -> viewModel.openAddEggDialog(preselectedPairId = pairId) },
                        onAddClutch = { pairId -> viewModel.openAddClutchDialog(pairId = pairId) },
                        onSelectPair = { pairId -> viewModel.selectPair(pairId) }
                    )
                }
                ReproductionTab.EGGS -> {
                    EggsTabView(
                        state = state,
                        onCandleEgg = { egg -> viewModel.openCandleDialog(egg) },
                        onHatchEgg = { egg -> viewModel.openHatchDialog(egg) },
                        onDeleteEgg = { eggId -> viewModel.deleteEgg(eggId) },
                        onFilterFertility = { status -> viewModel.setEggFilterFertility(status) },
                        onFilterPair = { pairId -> viewModel.setEggFilterPairId(pairId) }
                    )
                }
                ReproductionTab.CHICKS_NURSERY -> {
                    ChicksTabView(
                        state = state,
                        onWeight = { chick -> viewModel.openChickWeightDialog(chick) },
                        onTransfer = { chick -> viewModel.openChickTransferDialog(chick) },
                        onMortality = { chick -> viewModel.openChickMortalityDialog(chick) },
                        onRegisterBird = { chick -> viewModel.openRegisterBirdDialog(chick) },
                        onDeleteChick = { chickId -> viewModel.deleteChick(chickId) },
                        onFilterStatus = { status -> viewModel.setChickFilterStatus(status) }
                    )
                }
                ReproductionTab.NESTS -> {
                    NestsTabView(
                        state = state,
                        onDeleteNest = { nestId -> viewModel.deleteNest(nestId) }
                    )
                }
                ReproductionTab.TIMELINES_AND_REMINDERS -> {
                    TimelinesTabView(
                        state = state,
                        onSyncReminders = { viewModel.syncUpcomingReminders() },
                        onHatchEgg = { egg -> viewModel.openHatchDialog(egg) }
                    )
                }
            }
        }
    }

    // --- DIALOGS ---
    if (state.isAddEggDialogOpen) {
        AddEggDialog(
            pairs = state.pairs,
            nests = state.nests,
            selectedPairId = state.selectedPairId,
            onDismiss = { viewModel.closeAddEggDialog() },
            onConfirm = { pairId, clutchId, nestId, eggNum, layDate, hatchDate, fertility, notes ->
                viewModel.addEgg(pairId, clutchId, nestId, eggNum, layDate, hatchDate, fertility, notes)
            }
        )
    }

    state.candlingEgg?.let { egg ->
        CandleEggDialog(
            egg = egg,
            onDismiss = { viewModel.closeCandleDialog() },
            onConfirm = { status, notes ->
                viewModel.updateEggFertility(egg.id, status, notes)
            }
        )
    }

    state.hatchingEgg?.let { egg ->
        HatchEggDialog(
            egg = egg,
            onDismiss = { viewModel.closeHatchDialog() },
            onConfirm = { hatchDate, weight, ring, notes ->
                viewModel.hatchEgg(egg.id, hatchDate, weight, ring, notes)
            }
        )
    }

    state.editingChickWeight?.let { chick ->
        ChickWeightDialog(
            chick = chick,
            onDismiss = { viewModel.closeChickWeightDialog() },
            onConfirm = { weight, condition, notes ->
                viewModel.recordChickWeight(chick.id, weight, condition, notes)
            }
        )
    }

    state.transferringChick?.let { chick ->
        ChickTransferDialog(
            chick = chick,
            cages = state.cages,
            onDismiss = { viewModel.closeChickTransferDialog() },
            onConfirm = { cageCode, date, notes ->
                viewModel.transferChickToCage(chick.id, cageCode, date, notes)
            }
        )
    }

    state.recordingMortalityChick?.let { chick ->
        ChickMortalityDialog(
            chick = chick,
            onDismiss = { viewModel.closeChickMortalityDialog() },
            onConfirm = { date, reason, notes ->
                viewModel.recordChickMortality(chick.id, date, reason, notes)
            }
        )
    }

    state.registeringChickAsBird?.let { chick ->
        val pair = state.pairs.find { it.pair.id == chick.pairId }
        val father = pair?.pair?.maleRingNumber?.let { ring -> state.birds.find { it.ringNumber == ring } }
        val mother = pair?.pair?.femaleRingNumber?.let { ring -> state.birds.find { it.ringNumber == ring } }

        RegisterChickDialog(
            chick = chick,
            pair = pair,
            father = father,
            mother = mother,
            onDismiss = { viewModel.closeRegisterBirdDialog() },
            onConfirm = { ring, name, gender, variety, color, notes ->
                viewModel.registerChickAsBird(chick.id, ring, name, gender, variety, color, notes)
            }
        )
    }

    if (state.isAddNestDialogOpen) {
        AddNestDialog(
            cages = state.cages,
            onDismiss = { viewModel.closeAddNestDialog() },
            onConfirm = { boxNum, cageCode, pairId, mat, clean, notes ->
                viewModel.saveNest(boxNum, cageCode, pairId, mat, clean, notes)
            }
        )
    }

    if (state.isAddClutchDialogOpen) {
        AddClutchDialog(
            pairs = state.pairs,
            selectedPairId = state.selectedPairId,
            onDismiss = { viewModel.closeAddClutchDialog() },
            onConfirm = { pairId, num, matingDate, startDate, notes ->
                viewModel.saveClutch(pairId, num, matingDate, startDate, notes)
            }
        )
    }
}

// ==========================================
// SUB-TAB VIEWS
// ==========================================

@Composable
private fun ClutchesTabView(
    state: ReproductionUiState,
    isFa: Boolean,
    onAddEgg: (Long) -> Unit,
    onAddClutch: (Long) -> Unit,
    onSelectPair: (Long) -> Unit
) {
    if (state.pairs.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Egg,
            title = stringResource(R.string.empty_reproduction_title),
            description = stringResource(R.string.empty_reproduction_desc)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(state.pairs, key = { it.pair.id }) { pairDetails ->
                val pair = pairDetails.pair
                val eggCount = pairDetails.eggs.size
                val chickCount = pairDetails.chicks.size
                val fertileCount = pairDetails.eggs.count { it.fertilityStatus == "FERTILE" }

                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("pair_breeding_card_${pair.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pair #${pair.id}: ♂ ${pair.maleRingNumber} × ♀ ${pair.femaleRingNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = pair.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Dates info: Mating date & Cage & Nest
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Mating Date: ${pair.matingDate?.let { dateFormatter.format(Date(it)) } ?: "Recorded on pairing"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Cage: ${pair.cageCode ?: "Box"} | Nest: ${pair.nestId ?: "NB-1"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Stats counters row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Eggs", style = MaterialTheme.typography.labelSmall)
                                Text("$eggCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fertile", style = MaterialTheme.typography.labelSmall)
                                Text("$fertileCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF1976D2))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Chicks", style = MaterialTheme.typography.labelSmall)
                                Text("$chickCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF2E7D32))
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onAddClutch(pair.id) },
                                modifier = Modifier.testTag("pair_new_clutch_${pair.id}")
                            ) {
                                Text("New Clutch", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Button(
                                onClick = { onAddEgg(pair.id) },
                                modifier = Modifier.testTag("pair_log_egg_${pair.id}")
                            ) {
                                Icon(Icons.Default.Egg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Egg", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EggsTabView(
    state: ReproductionUiState,
    onCandleEgg: (com.example.data.database.entity.EggEntity) -> Unit,
    onHatchEgg: (com.example.data.database.entity.EggEntity) -> Unit,
    onDeleteEgg: (String) -> Unit,
    onFilterFertility: (String?) -> Unit,
    onFilterPair: (Long?) -> Unit
) {
    val filteredEggs = state.eggs.filter { egg ->
        (state.eggFilterPairId == null || egg.pairId == state.eggFilterPairId) &&
                (state.eggFilterFertility == null || egg.fertilityStatus == state.eggFilterFertility)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.eggFilterFertility == null,
                onClick = { onFilterFertility(null) },
                label = { Text("All Eggs (${state.eggs.size})") },
                modifier = Modifier.testTag("egg_filter_all")
            )
            listOf("UNCANDLED", "FERTILE", "INFERTILE", "DEAD_IN_SHELL").forEach { status ->
                val count = state.eggs.count { it.fertilityStatus == status }
                FilterChip(
                    selected = state.eggFilterFertility == status,
                    onClick = {
                        onFilterFertility(if (state.eggFilterFertility == status) null else status)
                    },
                    label = { Text("$status ($count)") },
                    modifier = Modifier.testTag("egg_filter_$status")
                )
            }
        }

        if (filteredEggs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Egg,
                title = "No Eggs Found",
                description = "No eggs matching the current filter. Tap the '+' button below to log an individual egg."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
            ) {
                items(filteredEggs, key = { it.id }) { egg ->
                    EggItemCard(
                        egg = egg,
                        onCandle = { onCandleEgg(egg) },
                        onHatch = { onHatchEgg(egg) },
                        onDelete = { onDeleteEgg(egg.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChicksTabView(
    state: ReproductionUiState,
    onWeight: (com.example.data.database.entity.ChickEntity) -> Unit,
    onTransfer: (com.example.data.database.entity.ChickEntity) -> Unit,
    onMortality: (com.example.data.database.entity.ChickEntity) -> Unit,
    onRegisterBird: (com.example.data.database.entity.ChickEntity) -> Unit,
    onDeleteChick: (String) -> Unit,
    onFilterStatus: (String?) -> Unit
) {
    val filteredChicksWithDetails = state.chicksWithDetails.filter { details ->
        state.chickFilterStatus == null || details.chick.status == state.chickFilterStatus
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Status filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.chickFilterStatus == null,
                onClick = { onFilterStatus(null) },
                label = { Text("All Nursery (${state.chicks.size})") },
                modifier = Modifier.testTag("chick_filter_all")
            )
            listOf("IN_NEST", "WEANED", "TRANSFERRED", "DECEASED").forEach { st ->
                val count = state.chicks.count { it.status == st }
                FilterChip(
                    selected = state.chickFilterStatus == st,
                    onClick = {
                        onFilterStatus(if (state.chickFilterStatus == st) null else st)
                    },
                    label = { Text("$st ($count)") },
                    modifier = Modifier.testTag("chick_filter_$st")
                )
            }
        }

        if (filteredChicksWithDetails.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ChildCare,
                title = "No Chicks in Registry",
                description = "Chicks will appear here once fertile eggs hatch."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
            ) {
                items(filteredChicksWithDetails, key = { it.chick.id }) { chickDetails ->
                    ChickItemCard(
                        chickDetails = chickDetails,
                        onLogWeight = { onWeight(chickDetails.chick) },
                        onTransfer = { onTransfer(chickDetails.chick) },
                        onMortality = { onMortality(chickDetails.chick) },
                        onRegisterBird = { onRegisterBird(chickDetails.chick) },
                        onDelete = { onDeleteChick(chickDetails.chick.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NestsTabView(
    state: ReproductionUiState,
    onDeleteNest: (String) -> Unit
) {
    if (state.nests.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Home,
            title = "No Nest Boxes Registered",
            description = "Register your wooden breeding nest boxes to track occupancy and sanitation."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(state.nests, key = { it.id }) { nest ->
                NestItemCard(
                    nest = nest,
                    onDelete = { onDeleteNest(nest.id) }
                )
            }
        }
    }
}

@Composable
private fun TimelinesTabView(
    state: ReproductionUiState,
    onSyncReminders: () -> Unit,
    onHatchEgg: (com.example.data.database.entity.EggEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        item {
            val selectedPair = state.pairs.find { it.pair.id == state.selectedPairId } ?: state.pairs.firstOrNull()
            BreedingTimelineCard(
                pair = selectedPair,
                onSyncReminders = onSyncReminders
            )
        }

        item {
            UpcomingHatchesCard(
                upcomingEggs = state.upcomingHatchEggs,
                onHatchEgg = onHatchEgg
            )
        }
    }
}
