package com.example.feature.reproduction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.core.common.GenerationCalculator
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.relation.PairWithBreedingDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

@Composable
fun AddEggDialog(
    pairs: List<PairWithBreedingDetails>,
    nests: List<NestEntity>,
    selectedPairId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (
        pairId: Long,
        clutchId: Long?,
        nestId: String?,
        eggNumber: Int,
        layDate: Long,
        expectedHatchDate: Long?,
        fertility: String,
        notes: String?
    ) -> Unit
) {
    var chosenPairId by remember {
        mutableStateOf(selectedPairId ?: pairs.firstOrNull()?.pair?.id ?: 0L)
    }
    var eggNumberText by remember { mutableStateOf("1") }
    var layDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var fertilityStatus by remember { mutableStateOf("UNCANDLED") }
    var notes by remember { mutableStateOf("") }
    var pairDropdownExpanded by remember { mutableStateOf(false) }

    val activePair = pairs.find { it.pair.id == chosenPairId }
    val calculatedHatchDate = layDate + (18L * 86400000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_egg_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Egg, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Individual Egg", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pair Selector
                Text("Breeding Pair *", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { pairDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("egg_pair_selector")
                    ) {
                        Text(
                            text = activePair?.let {
                                "Pair #${it.pair.id}: ♂ ${it.pair.maleRingNumber} × ♀ ${it.pair.femaleRingNumber}"
                            } ?: "Select Breeding Pair",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = pairDropdownExpanded,
                        onDismissRequest = { pairDropdownExpanded = false }
                    ) {
                        pairs.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text("Pair #${p.pair.id} (♂ ${p.pair.maleRingNumber} × ♀ ${p.pair.femaleRingNumber})")
                                },
                                onClick = {
                                    chosenPairId = p.pair.id
                                    val nextNum = (p.eggs.maxOfOrNull { it.eggNumber } ?: 0) + 1
                                    eggNumberText = nextNum.toString()
                                    pairDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Egg Number
                OutlinedTextField(
                    value = eggNumberText,
                    onValueChange = { eggNumberText = it },
                    label = { Text("Egg Number (in clutch)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("egg_number_input")
                )

                // Dates info card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Laying Date: ${dateFormatter.format(Date(layDate))}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Expected Hatch: ~${dateFormatter.format(Date(calculatedHatchDate))} (Day 18)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Initial Fertility
                Text("Fertility Status", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UNCANDLED", "FERTILE", "INFERTILE").forEach { status ->
                        val isSelected = fertilityStatus == status
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { fertilityStatus = status }
                                .padding(vertical = 8.dp)
                                .testTag("fertility_option_$status")
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (shell thickness, nest condition, etc.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("egg_notes_input"),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = eggNumberText.toIntOrNull() ?: 1
                    onConfirm(
                        chosenPairId,
                        activePair?.clutches?.firstOrNull()?.id,
                        activePair?.pair?.nestId,
                        num,
                        layDate,
                        calculatedHatchDate,
                        fertilityStatus,
                        notes.ifBlank { null }
                    )
                },
                enabled = chosenPairId > 0,
                modifier = Modifier.testTag("egg_save_button")
            ) {
                Text("Log Egg")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("egg_cancel_button")) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CandleEggDialog(
    egg: EggEntity,
    onDismiss: () -> Unit,
    onConfirm: (status: String, notes: String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(egg.fertilityStatus) }
    var notes by remember { mutableStateOf(egg.notes.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("candle_egg_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Egg, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Candle Egg #${egg.eggNumber}", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Laid on: ${dateFormatter.format(Date(egg.layDate))}", style = MaterialTheme.typography.bodyMedium)
                Text("Expected Hatch: ~${dateFormatter.format(Date(egg.expectedHatchDate ?: (egg.layDate + 18L * 86400000L)))}", style = MaterialTheme.typography.bodyMedium)

                Text("Candling Observation", style = MaterialTheme.typography.labelMedium)
                val options = listOf(
                    "FERTILE" to "Fertile (Red veins & embryo heartbeat visible)",
                    "INFERTILE" to "Infertile / Clear (Clear yolk, no development)",
                    "DEAD_IN_SHELL" to "Dead in Shell (Blood ring / arrested growth)",
                    "UNCANDLED" to "Uncandled / Pending verification"
                )

                options.forEach { (status, desc) ->
                    val isSelected = selectedStatus == status
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(8.dp)
                            .testTag("candle_status_$status")
                    ) {
                        Column {
                            Text(status, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Candling Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("candling_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, notes.ifBlank { null }) },
                modifier = Modifier.testTag("candle_save_button")
            ) {
                Text("Save Candling")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HatchEggDialog(
    egg: EggEntity,
    onDismiss: () -> Unit,
    onConfirm: (hatchDate: Long, weightGrams: Double?, ringNumber: String?, notes: String?) -> Unit
) {
    var hatchDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var weightText by remember { mutableStateOf("1.5") }
    var ringNumberText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("hatch_egg_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hatch Egg #${egg.eggNumber}", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Pair ID: #${egg.pairId}", style = MaterialTheme.typography.labelMedium)
                        Text("Laid: ${dateFormatter.format(Date(egg.layDate))}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "This automatically records the Chick, connects Parents, and links generation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = dateFormatter.format(Date(hatchDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Actual Hatch Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ringNumberText,
                    onValueChange = { ringNumberText = it },
                    label = { Text("Closed Leg Ring / Band (Optional)") },
                    placeholder = { Text("e.g. IR-2026-B102") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hatch_ring_input")
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Initial Hatch Weight (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hatch_weight_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Hatch Notes (assisted, vigorous, down color, etc.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hatch_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightText.toDoubleOrNull()
                    val ring = ringNumberText.trim().ifBlank { null }
                    onConfirm(hatchDate, weight, ring, notes.ifBlank { null })
                },
                modifier = Modifier.testTag("hatch_confirm_button")
            ) {
                Text("Confirm Hatch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChickWeightDialog(
    chick: ChickEntity,
    onDismiss: () -> Unit,
    onConfirm: (weightGrams: Double, condition: String, notes: String?) -> Unit
) {
    var weightText by remember { mutableStateOf(chick.weightGrams?.toString() ?: "15.0") }
    var condition by remember { mutableStateOf("OPTIMAL") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("chick_weight_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Weight: Chick #${chick.hatchOrder}", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val ageDays = ((System.currentTimeMillis() - chick.hatchDate) / 86400000L).coerceAtLeast(0)
                Text("Age: $ageDays days | Stage: ${chick.growthStage}", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Current Weight (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chick_weight_input")
                )

                Text("Crop & Body Condition", style = MaterialTheme.typography.labelMedium)
                listOf("OPTIMAL", "SLIGHTLY_UNDERWEIGHT", "FULL_CROP", "DEHYDRATED").forEach { cond ->
                    val isSelected = condition == cond
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { condition = cond }
                            .padding(6.dp)
                    ) {
                        Text(cond, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: 10.0
                    onConfirm(w, condition, notes.ifBlank { null })
                },
                modifier = Modifier.testTag("chick_weight_save_button")
            ) {
                Text("Save Weight")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChickTransferDialog(
    chick: ChickEntity,
    cages: List<CageEntity>,
    onDismiss: () -> Unit,
    onConfirm: (cageCode: String, transferDate: Long, notes: String?) -> Unit
) {
    var selectedCageCode by remember {
        mutableStateOf(cages.firstOrNull()?.code ?: "FLIGHT-1")
    }
    var notes by remember { mutableStateOf("") }
    var cageDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("chick_transfer_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MoveToInbox, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transfer Chick #${chick.hatchOrder}", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Transfer weaned chick from breeding nest to nursery or flight cage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Destination Cage *", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { cageDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_cage_dropdown")
                    ) {
                        Text("Cage: $selectedCageCode", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = cageDropdownExpanded,
                        onDismissRequest = { cageDropdownExpanded = false }
                    ) {
                        cages.forEach { cage ->
                            DropdownMenuItem(
                                text = { Text("${cage.code} (${cage.type.name}) - Occ: ${cage.currentOccupancy}/${cage.capacity}") },
                                onClick = {
                                    selectedCageCode = cage.code
                                    cageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transfer Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedCageCode, System.currentTimeMillis(), notes.ifBlank { null })
                },
                modifier = Modifier.testTag("transfer_confirm_button")
            ) {
                Text("Confirm Transfer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChickMortalityDialog(
    chick: ChickEntity,
    onDismiss: () -> Unit,
    onConfirm: (mortalityDate: Long, reason: String, notes: String?) -> Unit
) {
    var reason by remember { mutableStateOf("Failure to thrive") }
    var notes by remember { mutableStateOf("") }

    val commonReasons = listOf(
        "Failure to thrive",
        "Crop stasis / Aspiration",
        "Smothered in nest",
        "Parental rejection / Pecking",
        "Bacterial infection",
        "Cold stress / Hypothermia",
        "Unknown cause"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("chick_mortality_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Chick Mortality", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Recording mortality for Chick #${chick.hatchOrder} (Pair #${chick.pairId}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Text("Primary Cause / Observation", style = MaterialTheme.typography.labelMedium)
                commonReasons.forEach { r ->
                    val isSelected = reason == r
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(6.dp)
                    ) {
                        Text(r, modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Detailed Notes & Necropsy Info") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(System.currentTimeMillis(), reason, notes.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("mortality_confirm_button")
            ) {
                Text("Confirm Mortality Record")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RegisterChickDialog(
    chick: ChickEntity,
    pair: PairWithBreedingDetails?,
    father: BirdEntity?,
    mother: BirdEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        ringNumber: String,
        name: String?,
        gender: BirdGender,
        variety: BudgieVariety?,
        color: String?,
        notes: String?
    ) -> Unit
) {
    var ringNumber by remember { mutableStateOf(chick.bandedRingNumber.orEmpty()) }
    var name by remember { mutableStateOf("Chick #${chick.hatchOrder}") }
    var gender by remember { mutableStateOf(BirdGender.UNKNOWN) }
    var variety by remember { mutableStateOf(father?.variety ?: BudgieVariety.ENGLISH_SHOW) }
    var color by remember { mutableStateOf(father?.color ?: "Sky Blue") }
    var notes by remember { mutableStateOf(chick.notes.orEmpty()) }
    var genderExpanded by remember { mutableStateOf(false) }
    var varietyExpanded by remember { mutableStateOf(false) }

    // Dynamic generation computation from database records!
    val computedGen = GenerationCalculator.calculateOffspringGeneration(
        fatherGeneration = father?.generation,
        motherGeneration = mother?.generation,
        hasKnownParents = father != null || mother != null
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("register_chick_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Chick as Bird", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Lineage Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Parent Lineage Chain:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text("Father: ♂ ${pair?.pair?.maleRingNumber ?: "Unknown"} (Gen: ${father?.generation ?: "P0"})")
                        Text("Mother: ♀ ${pair?.pair?.femaleRingNumber ?: "Unknown"} (Gen: ${mother?.generation ?: "P0"})")
                        Text(
                            "Offspring Computed Generation: $computedGen",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = ringNumber,
                    onValueChange = { ringNumber = it },
                    label = { Text("Closed Ring Number *") },
                    placeholder = { Text("e.g. B-2026-042") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_ring_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bird Name / Nickname") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender selector
                Text("Gender", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { genderExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(gender.name, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        BirdGender.values().forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = {
                                    gender = g
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Variety selector
                Text("Budgie Variety / Mutation", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { varietyExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(variety.name, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = varietyExpanded,
                        onDismissRequest = { varietyExpanded = false }
                    ) {
                        BudgieVariety.values().take(12).forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v.name) },
                                onClick = {
                                    variety = v
                                    varietyExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Body Color") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ringNumber.isNotBlank()) {
                        onConfirm(
                            ringNumber.trim(),
                            name.ifBlank { null },
                            gender,
                            variety,
                            color.ifBlank { null },
                            notes.ifBlank { null }
                        )
                    }
                },
                enabled = ringNumber.isNotBlank(),
                modifier = Modifier.testTag("register_save_button")
            ) {
                Text("Register Bird")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddNestDialog(
    cages: List<CageEntity>,
    onDismiss: () -> Unit,
    onConfirm: (boxNumber: String, cageCode: String?, pairId: Long?, material: String, isClean: Boolean, notes: String?) -> Unit
) {
    var boxNumber by remember { mutableStateOf("") }
    var selectedCageCode by remember { mutableStateOf<String?>(cages.firstOrNull()?.code) }
    var nestMaterial by remember { mutableStateOf("Pine Wood Shavings") }
    var isClean by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_nest_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Nest Box", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = boxNumber,
                    onValueChange = { boxNumber = it },
                    label = { Text("Nest Box Code / Number *") },
                    placeholder = { Text("e.g. NB-12") },
                    modifier = Modifier.fillMaxWidth().testTag("nest_box_input")
                )

                OutlinedTextField(
                    value = nestMaterial,
                    onValueChange = { nestMaterial = it },
                    label = { Text("Bedding Material") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isClean, onCheckedChange = { isClean = it })
                    Text("Disinfected & Clean", style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (dimensions, concavity, ventilation)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (boxNumber.isNotBlank()) {
                        onConfirm(boxNumber, selectedCageCode, null, nestMaterial, isClean, notes.ifBlank { null })
                    }
                },
                enabled = boxNumber.isNotBlank(),
                modifier = Modifier.testTag("nest_save_button")
            ) {
                Text("Save Nest Box")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddClutchDialog(
    pairs: List<PairWithBreedingDetails>,
    selectedPairId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (pairId: Long, clutchNumber: Int, matingDate: Long?, startDate: Long, notes: String?) -> Unit
) {
    var chosenPairId by remember {
        mutableStateOf(selectedPairId ?: pairs.firstOrNull()?.pair?.id ?: 0L)
    }
    var clutchNumberText by remember { mutableStateOf("1") }
    var matingDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var pairDropdownExpanded by remember { mutableStateOf(false) }

    val activePair = pairs.find { it.pair.id == chosenPairId }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_clutch_dialog"),
        title = {
            Text("Log Breeding Clutch", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Breeding Pair *", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { pairDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activePair?.let {
                                "Pair #${it.pair.id}: ♂ ${it.pair.maleRingNumber} × ♀ ${it.pair.femaleRingNumber}"
                            } ?: "Select Pair",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = pairDropdownExpanded,
                        onDismissRequest = { pairDropdownExpanded = false }
                    ) {
                        pairs.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("Pair #${p.pair.id} (♂ ${p.pair.maleRingNumber} × ♀ ${p.pair.femaleRingNumber})") },
                                onClick = {
                                    chosenPairId = p.pair.id
                                    clutchNumberText = ((p.clutches.size) + 1).toString()
                                    pairDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = clutchNumberText,
                    onValueChange = { clutchNumberText = it },
                    label = { Text("Clutch Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Mating Date: ${dateFormatter.format(Date(matingDate))}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Budgies typically lay the first egg 8-12 days after successful mating.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clutch Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = clutchNumberText.toIntOrNull() ?: 1
                    onConfirm(chosenPairId, num, matingDate, matingDate, notes.ifBlank { null })
                },
                enabled = chosenPairId > 0,
                modifier = Modifier.testTag("clutch_save_button")
            ) {
                Text("Save Clutch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
