package com.example.feature.health

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.health.WeightGrowthTrendChart
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.AppDatabase
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepositoryImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    onNavigateToBird: (String) -> Unit = {},
    viewModel: HealthViewModel = run {
        val context = LocalContext.current
        val db = AppDatabase.getInstance(context)
        val repo = AviaryRepositoryImpl(db)
        viewModel(factory = HealthViewModelFactory(repo))
    }
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val allHealthRecords by viewModel.allHealthRecords.collectAsState()
    val activeIssues by viewModel.activeHealthIssues.collectAsState()
    val activeMedications by viewModel.activeMedications.collectAsState()
    val allWeights by viewModel.allWeights.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showLogTreatmentDialog by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("health_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Clinical Disclaimer Banner
        item {
            MedicalDisclaimerBanner(isPersian = isFa)
        }

        // Summary Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active cases metric
                MetricCard(
                    title = if (isFa) "موارد فعال" else "Active Cases",
                    value = "${activeIssues.size}",
                    color = if (activeIssues.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.Healing,
                    modifier = Modifier.weight(1f)
                )

                // Scheduled medications metric
                MetricCard(
                    title = if (isFa) "داروهای جاری" else "Active Meds",
                    value = "${activeMedications.size}",
                    color = Color(0xFFE65100),
                    icon = Icons.Filled.Medication,
                    modifier = Modifier.weight(1f)
                )

                // Total weights logged
                MetricCard(
                    title = if (isFa) "ثبت وزن‌ها" else "Weights Logged",
                    value = "${allWeights.size}",
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Filled.Balance,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLogTreatmentDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_log_treatment"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFa) "ثبت سابقه درمان" else "Log Treatment")
                }

                OutlinedButton(
                    onClick = { showLogWeightDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_log_weight"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Balance,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFa) "ثبت وزن پرنده" else "Log Weight")
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ACTIVE",
                    onClick = { viewModel.setFilter("ACTIVE") },
                    label = { Text(if (isFa) "موارد فعال (${activeIssues.size})" else "Active (${activeIssues.size})") },
                    modifier = Modifier.testTag("filter_active")
                )
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { viewModel.setFilter("ALL") },
                    label = { Text(if (isFa) "سوابق درمان (${allHealthRecords.size})" else "History (${allHealthRecords.size})") },
                    modifier = Modifier.testTag("filter_history")
                )
                FilterChip(
                    selected = selectedFilter == "MEDICATIONS",
                    onClick = { viewModel.setFilter("MEDICATIONS") },
                    label = { Text(if (isFa) "زمان‌بندی داروها (${activeMedications.size})" else "Medications (${activeMedications.size})") },
                    modifier = Modifier.testTag("filter_medications")
                )
                FilterChip(
                    selected = selectedFilter == "WEIGHTS",
                    onClick = { viewModel.setFilter("WEIGHTS") },
                    label = { Text(if (isFa) "روند وزن" else "Weight Trends") },
                    modifier = Modifier.testTag("filter_weights")
                )
            }
        }

        // Section Content based on selected filter
        when (selectedFilter) {
            "MEDICATIONS" -> {
                if (activeMedications.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Medication,
                            title = if (isFa) "هیچ داروی زمان‌بندی شده فعالی وجود ندارد" else "No Active Medications Scheduled",
                            description = if (isFa)
                                "هنگام ثبت درمان، با فعال‌سازی یادآور، زمان‌بندی دوزها در اینجا نمایش داده می‌شود."
                            else
                                "When adding treatment, enable medication reminders to track active dosage regimens.",
                            actionLabel = if (isFa) "ثبت نسخه و دارو" else "Schedule Medication",
                            onActionClick = { showLogTreatmentDialog = true },
                            testTag = "empty_meds_view"
                        )
                    }
                } else {
                    items(activeMedications, key = { it.id }) { med ->
                        ActiveMedicationCard(
                            medication = med,
                            isPersian = isFa,
                            dateFormat = dateFormat,
                            onBirdClick = { ring -> if (!ring.isNullOrBlank()) onNavigateToBird(ring) },
                            onMarkCompleted = { viewModel.markMedicationCompleted(med) }
                        )
                    }
                }
            }
            "WEIGHTS" -> {
                item {
                    WeightGrowthTrendChart(
                        weightRecords = allWeights,
                        isPersian = isFa
                    )
                }

                if (allWeights.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Balance,
                            title = if (isFa) "هنوز رکوردی برای وزن ثبت نشده" else "No Weight Records Logged",
                            description = if (isFa)
                                "ثبت دوره‌ای وزن پرندگان به تشخیص زودهنگام بیماری‌ها و ارزیابی رشد کمک می‌کند."
                            else
                                "Regular weight tracking helps identify early illness and evaluate chick development.",
                            actionLabel = if (isFa) "ثبت وزن جدید" else "Log First Weight",
                            onActionClick = { showLogWeightDialog = true }
                        )
                    }
                } else {
                    items(allWeights.take(15), key = { it.id }) { weight ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
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
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.clickable { onNavigateToBird(weight.birdRingNumber) }
                                    ) {
                                        Text(
                                            text = weight.birdRingNumber,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${weight.weightGrams} g",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dateFormat.format(Date(weight.recordedDate)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (weight.conditionScore) {
                                        "OPTIMAL", "Good" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        "UNDERWEIGHT" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    }
                                ) {
                                    Text(
                                        text = weight.conditionScore,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (weight.conditionScore) {
                                            "OPTIMAL", "Good" -> Color(0xFF2E7D32)
                                            "UNDERWEIGHT" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // ALL or ACTIVE health treatment history
                val displayedRecords = if (selectedFilter == "ACTIVE") activeIssues else allHealthRecords

                if (displayedRecords.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.MedicalServices,
                            title = if (selectedFilter == "ACTIVE") {
                                if (isFa) "هیچ مورد بیماری فعالی ثبت نشده است" else "No Active Health Issues"
                            } else {
                                stringResource(R.string.empty_health_title)
                            },
                            description = if (isFa)
                                "تمام پرندگان در وضعیت سلامت قرار دارند یا سوابق ثبت نشده است."
                            else
                                "All birds are healthy or no treatment cases have been recorded yet.",
                            actionLabel = if (isFa) "ثبت سابقه درمان" else "Log Treatment Case",
                            onActionClick = { showLogTreatmentDialog = true },
                            testTag = "health_empty_view"
                        )
                    }
                } else {
                    items(displayedRecords, key = { it.id }) { record ->
                        TreatmentHistoryCard(
                            record = record,
                            isPersian = isFa,
                            dateFormat = dateFormat,
                            onBirdClick = { ring -> if (!ring.isNullOrBlank()) onNavigateToBird(ring) },
                            onMarkResolved = { viewModel.markHealthRecordResolved(record) }
                        )
                    }
                }
            }
        }
    }

    if (showLogTreatmentDialog) {
        LogHealthAndTreatmentDialog(
            isPersian = isFa,
            onDismiss = { showLogTreatmentDialog = false },
            onSave = { record, scheduleReminders ->
                viewModel.saveHealthRecord(record, scheduleReminders)
                showLogTreatmentDialog = false
            }
        )
    }

    if (showLogWeightDialog) {
        var ringInput by remember { mutableStateOf("") }
        var weightInput by remember { mutableStateOf("") }
        var conditionInput by remember { mutableStateOf("OPTIMAL") }
        var notesInput by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Balance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFa) "ثبت وزن پرنده" else "Log Bird Weight")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = ringInput,
                        onValueChange = { ringInput = it },
                        label = { Text(if (isFa) "شماره پلاک پرنده" else "Bird Ring Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text(if (isFa) "وزن (گرم)" else "Weight (Grams)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text(if (isFa) "توضیحات" else "Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val grams = weightInput.toDoubleOrNull()
                        if (ringInput.isNotBlank() && grams != null) {
                            viewModel.saveWeight(
                                WeightRecordEntity(
                                    birdRingNumber = ringInput.trim(),
                                    weightGrams = grams,
                                    recordedDate = System.currentTimeMillis(),
                                    conditionScore = conditionInput,
                                    notes = notesInput.trim().ifBlank { null }
                                )
                            )
                            showLogWeightDialog = false
                        }
                    }
                ) {
                    Text(if (isFa) "ذخیره وزن" else "Save Weight")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogWeightDialog = false }) {
                    Text(if (isFa) "انصراف" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun TreatmentHistoryCard(
    record: HealthRecordEntity,
    isPersian: Boolean,
    dateFormat: SimpleDateFormat,
    onBirdClick: (String?) -> Unit,
    onMarkResolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("treatment_history_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isResolved) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Problem & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.birdRingNumber != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onBirdClick(record.birdRingNumber) }
                        ) {
                            Text(
                                text = record.birdRingNumber,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = record.recordedProblem.ifBlank { record.diagnosis },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (record.isResolved) Color(0xFF2E7D32).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (record.isResolved) (if (isPersian) "بهبود یافته" else "Resolved")
                        else (if (isPersian) "تحت درمان" else "Active"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (record.isResolved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Symptoms
            if (record.symptoms.isNotBlank()) {
                Text(
                    text = "${if (isPersian) "علائم بالینی:" else "Symptoms:"} ${record.symptoms}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Medication, Dose, Frequency, Duration
            if (!record.medicationName.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${record.medicationName} ${record.dosage?.let { "- $it" } ?: ""}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isPersian) "تکرار:" else "Freq:"} ${record.frequency ?: "Daily"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (record.treatmentDurationDays > 0) {
                                Text(
                                    text = "${if (isPersian) "طول درمان:" else "Duration:"} ${record.treatmentDurationDays} ${if (isPersian) "روز" else "days"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Supplements
            if (!record.supplements.isNullOrBlank()) {
                Text(
                    text = "${if (isPersian) "مکمل‌ها و پشتیبانی:" else "Supplements:"} ${record.supplements}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Start & End Date and Resolution button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dateFormat.format(Date(record.startDate))} ${record.endDate?.let { "- ${dateFormat.format(Date(it))}" } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!record.isResolved) {
                    Button(
                        onClick = onMarkResolved,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPersian) "ثبت بهبود پرنده" else "Mark Resolved", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveMedicationCard(
    medication: MedicationEntity,
    isPersian: Boolean,
    dateFormat: SimpleDateFormat,
    onBirdClick: (String?) -> Unit,
    onMarkCompleted: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { onBirdClick(medication.birdRingNumber) }
                    ) {
                        Text(
                            text = medication.birdRingNumber ?: "Aviary",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = medication.medicationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isPersian) "دوز و روش:" else "Dose & Route:"} ${medication.dosage} (${medication.administrationRoute})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (isPersian) "نوبت مصرف:" else "Frequency:"} ${medication.frequency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${if (isPersian) "پایان دوره:" else "End Date:"} ${dateFormat.format(Date(medication.endDate))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onMarkCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = if (isPersian) "اتمام دوره دارو" else "Complete Treatment",
                    tint = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
