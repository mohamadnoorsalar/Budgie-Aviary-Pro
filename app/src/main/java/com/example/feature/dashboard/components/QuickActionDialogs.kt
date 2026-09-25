package com.example.feature.dashboard.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.PairEntity

@Composable
fun AddBirdDialog(
    availableCages: List<CageEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        ringNumber: String,
        name: String?,
        gender: BirdGender,
        variety: BudgieVariety,
        mutation: String,
        color: String,
        cageCode: String?
    ) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var ringNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(BirdGender.MALE) }
    var variety by remember { mutableStateOf(BudgieVariety.ENGLISH_SHOW) }
    var mutation by remember { mutableStateOf("Spangle") }
    var color by remember { mutableStateOf("Sky Blue") }
    var cageCode by remember { mutableStateOf(availableCages.firstOrNull()?.code ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "افزودن پرنده جدید به سالن" else "Add New Bird to Aviary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = ringNumber,
                    onValueChange = {
                        ringNumber = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text(if (isFa) "شماره حلقه (الزامی)" else "Ring Number (Required)") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bird_ring")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isFa) "نام / شناسه اختیاری" else "Name / Nickname (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (isFa) "جنسیت:" else "Gender:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == BirdGender.MALE,
                        onClick = { gender = BirdGender.MALE },
                        label = { Text(if (isFa) "نر" else "Cock (Male)") },
                        leadingIcon = { Icon(Icons.Filled.Male, null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = gender == BirdGender.FEMALE,
                        onClick = { gender = BirdGender.FEMALE },
                        label = { Text(if (isFa) "ماده" else "Hen (Female)") },
                        leadingIcon = { Icon(Icons.Filled.Female, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Text(
                    text = if (isFa) "نژاد / تیپ:" else "Variety:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BudgieVariety.values().take(3).forEach { v ->
                        FilterChip(
                            selected = variety == v,
                            onClick = { variety = v },
                            label = { Text(v.name.replace("_", " ")) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mutation,
                        onValueChange = { mutation = it },
                        label = { Text(if (isFa) "جهش (Mutation)" else "Mutation") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text(if (isFa) "رنگ پایه" else "Base Color") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = cageCode,
                    onValueChange = { cageCode = it },
                    label = { Text(if (isFa) "کد قفس یا باکس" else "Cage / Aviary Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ringNumber.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(ringNumber, name, gender, variety, mutation, color, cageCode)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_add_bird")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun AddPairDialog(
    availableBirds: List<BirdEntity>,
    availableCages: List<CageEntity>,
    onDismiss: () -> Unit,
    onConfirm: (maleRing: String, femaleRing: String, cageCode: String, notes: String?) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    val maleBirds = remember(availableBirds) { availableBirds.filter { it.gender == BirdGender.MALE } }
    val femaleBirds = remember(availableBirds) { availableBirds.filter { it.gender == BirdGender.FEMALE } }

    var maleRing by remember { mutableStateOf(maleBirds.firstOrNull()?.ringNumber ?: "") }
    var femaleRing by remember { mutableStateOf(femaleBirds.firstOrNull()?.ringNumber ?: "") }
    var cageCode by remember { mutableStateOf(availableCages.firstOrNull()?.code ?: "BOX-1") }
    var notes by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFE91E63))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "تشکیل جفت مولد جدید" else "Create New Breeding Pair",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = maleRing,
                    onValueChange = { maleRing = it },
                    label = { Text(if (isFa) "شماره حلقه پرنده نر (Cock)" else "Male Ring (Cock)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pair_male")
                )

                OutlinedTextField(
                    value = femaleRing,
                    onValueChange = { femaleRing = it },
                    label = { Text(if (isFa) "شماره حلقه پرنده ماده (Hen)" else "Female Ring (Hen)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pair_female")
                )

                OutlinedTextField(
                    value = cageCode,
                    onValueChange = { cageCode = it },
                    label = { Text(if (isFa) "کد قفس یا باکس تکثیر" else "Breeding Cage / Box Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pair_cage")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "توضیحات و اهداف جفت‌اندازی" else "Breeding Goal / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (maleRing.isBlank() || femaleRing.isBlank() || cageCode.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(maleRing, femaleRing, cageCode, notes)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_add_pair")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun AddEggDialog(
    availablePairs: List<PairEntity>,
    onDismiss: () -> Unit,
    onConfirm: (pairId: Long, eggNumber: Int, notes: String?) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var selectedPairId by remember { mutableStateOf(availablePairs.firstOrNull()?.id ?: 1L) }
    var eggNumber by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Egg, contentDescription = null, tint = Color(0xFFFFB300))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "ثبت تخم جدید در لانه" else "Log Egg in Nest",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isFa) "شناسه جفت مولد:" else "Breeding Pair ID:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )

                if (availablePairs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availablePairs.take(3).forEach { pair ->
                            FilterChip(
                                selected = selectedPairId == pair.id,
                                onClick = { selectedPairId = pair.id },
                                label = { Text("#${pair.id} (${pair.cageCode})") }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedPairId.toString(),
                        onValueChange = { selectedPairId = it.toLongOrNull() ?: 1L },
                        label = { Text(if (isFa) "شناسه جفت (عدد)" else "Pair ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFa) "شماره تخم در دوره:" else "Egg Number in Clutch:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (eggNumber > 1) eggNumber-- },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-")
                        }
                        Text(
                            text = "$eggNumber",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { eggNumber++ },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+")
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "یادداشت تخم‌گذاری (اختیاری)" else "Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPairId, eggNumber, notes) },
                modifier = Modifier.testTag("btn_confirm_add_egg")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun AddChickDialog(
    availablePairs: List<PairEntity>,
    onDismiss: () -> Unit,
    onConfirm: (pairId: Long, eggId: String, bandNumber: String?, notes: String?) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var pairId by remember { mutableStateOf(availablePairs.firstOrNull()?.id?.toString() ?: "1") }
    var eggId by remember { mutableStateOf("EGG-${System.currentTimeMillis() % 10000}") }
    var bandNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Pets, contentDescription = null, tint = Color(0xFF00B0FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "ثبت هچ جوجه در لانه" else "Record Chick Hatch",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = pairId,
                    onValueChange = { pairId = it },
                    label = { Text(if (isFa) "شناسه جفت مولد" else "Pair ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bandNumber,
                    onValueChange = { bandNumber = it },
                    label = { Text(if (isFa) "شماره حلقه پای جوجه (اختیاری)" else "Chick Band / Ring Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "مشخصات و رنگ کرک جوجه" else "Down Color / Hatch Condition") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pairId.toLongOrNull() ?: 1L, eggId, bandNumber, notes) },
                modifier = Modifier.testTag("btn_confirm_add_chick")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun RecordWeightDialog(
    availableBirds: List<BirdEntity>,
    onDismiss: () -> Unit,
    onConfirm: (birdRing: String, weightGrams: Double, condition: String, notes: String?) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var birdRing by remember { mutableStateOf(availableBirds.firstOrNull()?.ringNumber ?: "") }
    var weightText by remember { mutableStateOf("45.0") }
    var condition by remember { mutableStateOf("OPTIMAL") }
    var notes by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Scale, contentDescription = null, tint = Color(0xFF7E57C2))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "ثبت وزن و شرایط جسمی پرنده" else "Record Bird Weight",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = birdRing,
                    onValueChange = {
                        birdRing = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text(if (isFa) "شماره حلقه پرنده" else "Bird Ring Number") },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth().testTag("input_weight_ring")
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(if (isFa) "وزن به گرم (استاندارد ۳۵ تا ۶۰)" else "Weight (Grams)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_weight_grams")
                )

                Text(
                    text = if (isFa) "وضعیت عضله سینه:" else "Body Condition:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UNDERWEIGHT", "OPTIMAL", "OVERWEIGHT").forEach { c ->
                        FilterChip(
                            selected = condition == c,
                            onClick = { condition = c },
                            label = {
                                Text(
                                    when (c) {
                                        "OPTIMAL" -> if (isFa) "مطلوب" else "Optimal"
                                        "UNDERWEIGHT" -> if (isFa) "کم‌وزن" else "Underweight"
                                        else -> if (isFa) "چاق" else "Overweight"
                                    }
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "یادداشت‌ها" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: 0.0
                    if (birdRing.isBlank() || w <= 0.0) {
                        isError = true
                    } else {
                        onConfirm(birdRing, w, condition, notes)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_record_weight")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun RecordFeedingDialog(
    availableCages: List<CageEntity>,
    onDismiss: () -> Unit,
    onConfirm: (planName: String, cageCode: String?, notes: String?) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var planName by remember { mutableStateOf(if (isFa) "غذای تخم‌مرغی و جوانه گندم" else "Egg Food & Sprouted Seeds") }
    var targetCage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(if (isFa) "همراه با پودر مولتی ویتامین و کلسیم" else "Enriched with Vitamins and Calcium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color(0xFF43A047))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "ثبت وعده تغذیه سالن" else "Record Aviary Feeding",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text(if (isFa) "عنوان رژیم / خوراک" else "Diet / Feeding Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_feeding_name")
                )

                OutlinedTextField(
                    value = targetCage,
                    onValueChange = { targetCage = it },
                    label = { Text(if (isFa) "قفس هدف (خالی = کل سالن)" else "Target Cage (Blank = All Aviary)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "مکمل‌ها و ترکیبات افزوده" else "Supplements & Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(planName, targetCage.ifBlank { null }, notes) },
                modifier = Modifier.testTag("btn_confirm_record_feeding")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun RecordMedicationDialog(
    availableBirds: List<BirdEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        birdRing: String?,
        medName: String,
        dosage: String,
        route: String,
        frequency: String,
        durationDays: Int,
        notes: String?
    ) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var birdRing by remember { mutableStateOf("") }
    var medName by remember { mutableStateOf("Baytril 10%") }
    var dosage by remember { mutableStateOf("1.5 ml / Litre") }
    var route by remember { mutableStateOf("WATER") }
    var durationDays by remember { mutableIntStateOf(5) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = Color(0xFFE53935))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "تجویز دارو و دوره درمانی" else "Prescribe Medication",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = birdRing,
                    onValueChange = { birdRing = it },
                    label = { Text(if (isFa) "شماره حلقه (خالی = کل گله)" else "Bird Ring (Blank = Whole Flock)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_med_ring")
                )

                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text(if (isFa) "نام دارو" else "Medication Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_med_name")
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(if (isFa) "دوز مصرفی" else "Dosage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (isFa) "روش تجویز:" else "Administration Route:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("WATER", "ORAL", "CROP_NEEDLE").forEach { r ->
                        FilterChip(
                            selected = route == r,
                            onClick = { route = r },
                            label = { Text(r) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFa) "مدت دوره (روز):" else "Duration (Days):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (durationDays > 1) durationDays-- },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-")
                        }
                        Text(
                            text = "$durationDays",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { durationDays++ },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+")
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isFa) "علائم و دستورات درمانی" else "Symptoms & Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        birdRing.ifBlank { null },
                        medName,
                        dosage,
                        route,
                        "DAILY",
                        durationDays,
                        notes
                    )
                },
                modifier = Modifier.testTag("btn_confirm_record_medication")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun ScanQrDialog(
    availableBirds: List<BirdEntity>,
    availableCages: List<CageEntity>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    val isFa = LocalAppLanguage.current == AppLanguage.PERSIAN
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "اسکنر بارکد و حلقه سالن" else "Aviary Ring & Cage Scanner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Visual Scanner Viewport
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )

                    // Animated scan line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color(0xFF00E676))
                    )
                }

                Text(
                    text = if (isFa) "دوربین یا جستجوی مستقیم کد حلقه/قفس" else "Scan ring QR or lookup code directly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        val matchedBird = availableBirds.firstOrNull { it.ringNumber.contains(query, ignoreCase = true) }
                        val matchedCage = availableCages.firstOrNull { it.code.contains(query, ignoreCase = true) }
                        searchResult = when {
                            matchedBird != null -> if (isFa) "پرنده پیدا شد: ${matchedBird.ringNumber} (${matchedBird.variety})" else "Bird Found: ${matchedBird.ringNumber} (${matchedBird.variety})"
                            matchedCage != null -> if (isFa) "قفس پیدا شد: ${matchedCage.code} (${matchedCage.type})" else "Cage Found: ${matchedCage.code} (${matchedCage.type})"
                            query.isNotBlank() -> if (isFa) "رکوردی با این شناسه یافت نشد" else "No match found"
                            else -> null
                        }
                    },
                    label = { Text(if (isFa) "شماره حلقه یا کد قفس" else "Ring Number or Cage Code") },
                    trailingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_scan_lookup")
                )

                searchResult?.let { result ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) onItemSelected(searchQuery)
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_close_scanner")
            ) {
                Text(if (isFa) "تأیید" else "Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
