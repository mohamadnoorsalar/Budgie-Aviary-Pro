package com.example.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.navigation.AviaryDestination
import com.example.core.ui.components.EmptyStateView
import com.example.core.ui.components.StatMetricCard
import com.example.data.database.entity.AuditLogEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.ReminderEntity
import com.example.feature.dashboard.components.AddBirdDialog
import com.example.feature.dashboard.components.AddChickDialog
import com.example.feature.dashboard.components.AddEggDialog
import com.example.feature.dashboard.components.AddPairDialog
import com.example.feature.dashboard.components.RecordFeedingDialog
import com.example.feature.dashboard.components.RecordMedicationDialog
import com.example.feature.dashboard.components.RecordWeightDialog
import com.example.feature.dashboard.components.ScanQrDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (AviaryDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddBirdDialog by remember { mutableStateOf(false) }
    var showAddPairDialog by remember { mutableStateOf(false) }
    var showAddEggDialog by remember { mutableStateOf(false) }
    var showAddChickDialog by remember { mutableStateOf(false) }
    var showRecordWeightDialog by remember { mutableStateOf(false) }
    var showRecordFeedingDialog by remember { mutableStateOf(false) }
    var showRecordMedicationDialog by remember { mutableStateOf(false) }
    var showScanQrDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dashboard_screen")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Overview Banner
                FacilityHeroBanner(isFa = isFa, totalBirds = state.totalBirds, activePairs = state.activePairs)
            }

            // Core Facility Metrics (Total birds, Active pairs, Cages, Eggs, Chicks)
            item {
                Text(
                    text = if (isFa) "آمار کلیدی سالن پرورش" else "Key Facility Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = stringResource(R.string.stat_total_birds),
                        value = "${state.totalBirds}",
                        icon = Icons.Filled.Pets,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_birds",
                        iconTint = Color(0xFF00897B)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_active_pairs),
                        value = "${state.activePairs}",
                        icon = Icons.Filled.Favorite,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_active_pairs",
                        iconTint = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = stringResource(R.string.stat_cages),
                        value = "${state.totalCages}",
                        icon = Icons.Filled.GridView,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_cages",
                        iconTint = Color(0xFF3949AB)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_eggs_in_nest),
                        value = "${state.totalEggs}",
                        icon = Icons.Filled.Egg,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_eggs_in_nest",
                        iconTint = Color(0xFFFFB300)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_chicks_hatched),
                        value = "${state.totalChicks}",
                        icon = Icons.Filled.ChildCare,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_chicks_hatched",
                        iconTint = Color(0xFF00ACC1)
                    )
                }
            }

            // Quick Actions Bar (8 requested actions)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Quick Actions Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_add_bird),
                            icon = Icons.Filled.Add,
                            color = Color(0xFF10B981),
                            testTag = "quick_action_add_bird",
                            onClick = { showAddBirdDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_add_pair),
                            icon = Icons.Filled.Favorite,
                            color = Color(0xFFEC4899),
                            testTag = "quick_action_add_pair",
                            onClick = { showAddPairDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_add_egg),
                            icon = Icons.Filled.Egg,
                            color = Color(0xFFF59E0B),
                            testTag = "quick_action_add_egg",
                            onClick = { showAddEggDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_add_chick),
                            icon = Icons.Filled.ChildCare,
                            color = Color(0xFF06B6D4),
                            testTag = "quick_action_add_chick",
                            onClick = { showAddChickDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_record_weight),
                            icon = Icons.Filled.Scale,
                            color = Color(0xFF8B5CF6),
                            testTag = "quick_action_record_weight",
                            onClick = { showRecordWeightDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_record_feeding),
                            icon = Icons.Filled.Restaurant,
                            color = Color(0xFF16A34A),
                            testTag = "quick_action_record_feeding",
                            onClick = { showRecordFeedingDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_record_medication),
                            icon = Icons.Filled.MedicalServices,
                            color = Color(0xFFEF4444),
                            testTag = "quick_action_record_medication",
                            onClick = { showRecordMedicationDialog = true }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = stringResource(R.string.action_scan_qr),
                            icon = Icons.Filled.QrCodeScanner,
                            color = Color(0xFF3B82F6),
                            testTag = "quick_action_scan_qr",
                            onClick = { showScanQrDialog = true }
                        )
                    }
                }
            }

            // Financial Summary Section
            item {
                FinancialSummaryCard(summary = state.financialSummary, isFa = isFa)
            }

            // Today's Tasks & Reminders
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.todays_tasks),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigate(AviaryDestination.REMINDERS) }
                            .padding(4.dp)
                    )
                }
            }

            if (state.todaysTasks.isEmpty()) {
                item {
                    EmptyStatusCard(
                        message = stringResource(R.string.no_tasks_today),
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Color(0xFF10B981)
                    )
                }
            } else {
                items(state.todaysTasks.take(3), key = { it.id }) { reminder ->
                    ReminderTaskItem(
                        reminder = reminder,
                        onComplete = { viewModel.markReminderCompleted(reminder) }
                    )
                }
            }

            // Upcoming Hatch Dates
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.upcoming_hatch_dates),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigate(AviaryDestination.REPRODUCTION) }
                            .padding(4.dp)
                    )
                }
            }

            if (state.upcomingHatchList.isEmpty()) {
                item {
                    EmptyStatusCard(
                        message = stringResource(R.string.no_upcoming_hatches),
                        icon = Icons.Filled.Egg,
                        iconTint = Color(0xFFF59E0B)
                    )
                }
            } else {
                items(state.upcomingHatchList.take(3)) { hatchItem ->
                    UpcomingHatchCard(hatchItem = hatchItem, isFa = isFa)
                }
            }

            // Medication Reminders
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MedicalServices,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.medication_reminders),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigate(AviaryDestination.HEALTH) }
                            .padding(4.dp)
                    )
                }
            }

            if (state.activeMedications.isEmpty()) {
                item {
                    EmptyStatusCard(
                        message = stringResource(R.string.no_active_medications),
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Color(0xFF10B981)
                    )
                }
            } else {
                items(state.activeMedications.take(3), key = { it.id }) { med ->
                    MedicationReminderCard(med = med, isFa = isFa)
                }
            }

            // Low Inventory Alerts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Inventory,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.low_inventory_alerts),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigate(AviaryDestination.INVENTORY) }
                            .padding(4.dp)
                    )
                }
            }

            if (state.lowStockAlerts.isEmpty()) {
                item {
                    EmptyStatusCard(
                        message = stringResource(R.string.no_inventory_alerts),
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Color(0xFF10B981)
                    )
                }
            } else {
                items(state.lowStockAlerts.take(3), key = { it.id }) { item ->
                    LowInventoryAlertCard(item = item, isFa = isFa)
                }
            }

            // Recent Facility Activity (Real Audit Logs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.recent_activity),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (state.recentActivity.isEmpty()) {
                item {
                    EmptyStatusCard(
                        message = stringResource(R.string.no_recent_activity),
                        icon = Icons.Filled.History,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.recentActivity.take(4), key = { it.id }) { log ->
                    ActivityLogItem(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Quick Action Dialogs
    if (showAddBirdDialog) {
        AddBirdDialog(
            availableCages = state.availableCages,
            onDismiss = { showAddBirdDialog = false },
            onConfirm = { ring, name, gender, variety, mutation, color, cage ->
                viewModel.addBird(ring, name, gender, variety, mutation, color, cage)
                showAddBirdDialog = false
            }
        )
    }

    if (showAddPairDialog) {
        AddPairDialog(
            availableBirds = state.availableBirds,
            availableCages = state.availableCages,
            onDismiss = { showAddPairDialog = false },
            onConfirm = { male, female, cage, notes ->
                viewModel.addPair(male, female, cage, notes)
                showAddPairDialog = false
            }
        )
    }

    if (showAddEggDialog) {
        AddEggDialog(
            availablePairs = state.availablePairs,
            onDismiss = { showAddEggDialog = false },
            onConfirm = { pairId, eggNumber, notes ->
                viewModel.addEgg(pairId, eggNumber, System.currentTimeMillis(), notes)
                showAddEggDialog = false
            }
        )
    }

    if (showAddChickDialog) {
        AddChickDialog(
            availablePairs = state.availablePairs,
            onDismiss = { showAddChickDialog = false },
            onConfirm = { pairId, eggId, bandNumber, notes ->
                viewModel.addChick(pairId, eggId, bandNumber, System.currentTimeMillis(), notes)
                showAddChickDialog = false
            }
        )
    }

    if (showRecordWeightDialog) {
        RecordWeightDialog(
            availableBirds = state.availableBirds,
            onDismiss = { showRecordWeightDialog = false },
            onConfirm = { ring, weight, condition, notes ->
                viewModel.recordWeight(ring, weight, condition, notes)
                showRecordWeightDialog = false
            }
        )
    }

    if (showRecordFeedingDialog) {
        RecordFeedingDialog(
            availableCages = state.availableCages,
            onDismiss = { showRecordFeedingDialog = false },
            onConfirm = { planName, cageCode, notes ->
                viewModel.recordFeeding(planName, cageCode, notes)
                showRecordFeedingDialog = false
            }
        )
    }

    if (showRecordMedicationDialog) {
        RecordMedicationDialog(
            availableBirds = state.availableBirds,
            onDismiss = { showRecordMedicationDialog = false },
            onConfirm = { ring, medName, dosage, route, freq, days, notes ->
                viewModel.recordMedication(ring, medName, dosage, route, freq, days, notes)
                showRecordMedicationDialog = false
            }
        )
    }

    if (showScanQrDialog) {
        ScanQrDialog(
            availableBirds = state.availableBirds,
            availableCages = state.availableCages,
            onDismiss = { showScanQrDialog = false },
            onItemSelected = { showScanQrDialog = false }
        )
    }
}

@Composable
private fun FacilityHeroBanner(isFa: Boolean, totalBirds: Int, activePairs: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("facility_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.facility_healthy),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = if (isFa) "سالن فعال • $activePairs جفت مولد" else "Active Aviary • $activePairs Pairs",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isFa) "سالن تکثیر و پرورش مرغ عشق" else "Budgerigar Breeding Facility",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isFa)
                        "مدیریت سالن با $totalBirds پرنده ثبت شده، پایش چرخه‌های هچ، داروها، انبار و تراز مالی"
                    else
                        "Aviary management for $totalBirds registered birds, clutches, medications, and financial balances.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FinancialSummaryCard(summary: FinancialSummary, isFa: Boolean) {
    val currencyUnit = stringResource(R.string.currency_unit)
    val numberFormat = remember { NumberFormat.getInstance() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("financial_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.financial_summary),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (summary.netBalance >= 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${stringResource(R.string.net_balance_label)}: ${numberFormat.format(summary.netBalance)} $currencyUnit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.netBalance >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.TrendingUp, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.income_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${numberFormat.format(summary.totalIncome)} $currencyUnit",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // Expense
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.TrendingDown, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.expense_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${numberFormat.format(summary.totalExpenses)} $currencyUnit",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderTaskItem(reminder: ReminderEntity, onComplete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_item_${reminder.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!reminder.description.isNullOrEmpty()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onComplete,
                modifier = Modifier.testTag("btn_complete_reminder_${reminder.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Complete task",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun UpcomingHatchCard(hatchItem: com.example.feature.dashboard.UpcomingHatchItem, isFa: Boolean) {
    val egg = hatchItem.egg
    val isDueNow = hatchItem.daysRemaining == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_hatch_${egg.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Egg, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isFa) "تخم شماره ${egg.eggNumber} (جفت #${egg.pairId})" else "Egg #${egg.eggNumber} (Pair #${egg.pairId})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${egg.fertilityStatus} • ${egg.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDueNow) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isDueNow) stringResource(R.string.due_today) else stringResource(R.string.days_remaining_fmt, hatchItem.daysRemaining),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDueNow) Color(0xFF059669) else Color(0xFFD97706),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MedicationReminderCard(med: MedicationEntity, isFa: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_item_${med.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MedicalServices, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = med.medicationName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${med.dosage} • ${med.administrationRoute} (${med.frequency})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = med.birdRingNumber ?: if (isFa) "کل سالن" else "Flock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LowInventoryAlertCard(item: InventoryItemEntity, isFa: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Warning, null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(R.string.stock_units_fmt, item.currentStock, item.unit, item.minStockThreshold),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = if (isFa) "کمبود موجودی" else "Low Stock",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityLogItem(log: AuditLogEntity) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_log_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = log.actionType,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStatusCard(message: String, icon: ImageVector, iconTint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
