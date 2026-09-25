package com.example.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.NutritionRecordEntity

@Composable
fun LogNutritionDialog(
    initialBirdRing: String? = null,
    initialCageCode: String? = null,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (NutritionRecordEntity) -> Unit
) {
    var birdRing by remember { mutableStateOf(initialBirdRing ?: "") }
    var cageCode by remember { mutableStateOf(initialCageCode ?: "") }
    var foodType by remember { mutableStateOf(if (isPersian) "دان مخلوط پایه" else "Base Seed Mix") }
    var amount by remember { mutableStateOf("20g") }
    var consumption by remember { mutableStateOf("NORMAL") }
    var waterType by remember { mutableStateOf("FRESH_WATER") }
    var supplements by remember { mutableStateOf("") }
    var feedingSchedule by remember { mutableStateOf("MORNING") }
    var costStr by remember { mutableStateOf("0.50") }
    var notes by remember { mutableStateOf("") }

    val foodOptions = listOf(
        Pair(if (isPersian) "دان مخلوط پایه" else "Base Seed Mix", "Seeds"),
        Pair(if (isPersian) "غذای تخم‌مرغی / نرم" else "Soft / Egg Food", "Egg Food"),
        Pair(if (isPersian) "جوانه گندم / ماش" else "Sprouted Seeds", "Sprouts"),
        Pair(if (isPersian) "سبزیجات و هویج" else "Fresh Greens & Veggies", "Greens"),
        Pair(if (isPersian) "پلت تقویتی" else "Extruded Pellets", "Pellets"),
        Pair(if (isPersian) "خوشه ارزن" else "Millet Spray", "Millet Spray")
    )

    val consumptionOptions = listOf(
        Pair("FINISHED_ALL", if (isPersian) "کامل مصرف شد (۱۰۰٪)" else "100% Finished"),
        Pair("HIGH", if (isPersian) "مصرف بالا (۸۰٪)" else "High (80%)"),
        Pair("NORMAL", if (isPersian) "نرمال و عادی" else "Normal"),
        Pair("LOW", if (isPersian) "مصرف کم / باقیمانده زیاد" else "Low / Leftovers"),
        Pair("REFUSED", if (isPersian) "دست‌نخورده / رد شد" else "Refused")
    )

    val waterOptions = listOf(
        Pair("FRESH_WATER", if (isPersian) "آب تازه تصفیه شده" else "Fresh Filtered Water"),
        Pair("VITAMINS", if (isPersian) "آب با مولتی‌ویتامین" else "Vitamin Drops"),
        Pair("ELECTROLYTES", if (isPersian) "محلول الکترولیت" else "Electrolytes"),
        Pair("ACV_ACIDIFIED", if (isPersian) "آب اسیدی شده (سرکه سیب)" else "Apple Cider Vinegar (ACV)"),
        Pair("MEDICATED", if (isPersian) "آب دارویی" else "Medicated Water")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "ثبت وعده تغذیه و جیره" else "Log Feeding & Nutrition",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = birdRing,
                        onValueChange = { birdRing = it },
                        label = { Text(if (isPersian) "پلاک پرنده (اختیاری)" else "Bird Ring (Opt.)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nutrition_input_ring")
                    )
                    OutlinedTextField(
                        value = cageCode,
                        onValueChange = { cageCode = it },
                        label = { Text(if (isPersian) "کد قفس (اختیاری)" else "Cage Code (Opt.)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nutrition_input_cage")
                    )
                }

                Text(
                    text = if (isPersian) "نوع خوراک:" else "Food Type:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    foodOptions.forEach { (label, _) ->
                        FilterChip(
                            selected = foodType == label,
                            onClick = { foodType = label },
                            label = { Text(label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(if (isPersian) "مقدار / سهمیه" else "Amount") },
                        placeholder = { Text("e.g. 20g / 1 bowl") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nutrition_input_amount")
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text(if (isPersian) "هزینه ($)" else "Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nutrition_input_cost")
                    )
                }

                Text(
                    text = if (isPersian) "میزان مصرف (Consumption):" else "Consumption Level:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    consumptionOptions.take(3).forEach { (code, label) ->
                        FilterChip(
                            selected = consumption == code,
                            onClick = { consumption = code },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = if (isPersian) "آب آشامیدنی (Water):" else "Water Regimen:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    waterOptions.take(3).forEach { (code, label) ->
                        FilterChip(
                            selected = waterType == code,
                            onClick = { waterType = code },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = supplements,
                    onValueChange = { supplements = it },
                    label = { Text(if (isPersian) "مکمل‌ها و افزودنی‌ها" else "Supplements") },
                    placeholder = { Text(if (isPersian) "مثال: کالسی‌وت، پودر کلسیم، نکتون اس، پروبیوتیک" else "e.g. Calcivet, Calcium powder, Nekton S, Probiotic") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nutrition_input_supplements")
                )

                OutlinedTextField(
                    value = feedingSchedule,
                    onValueChange = { feedingSchedule = it },
                    label = { Text(if (isPersian) "برنامه نوبت غذا" else "Feeding Schedule") },
                    placeholder = { Text("MORNING / EVENING / TWICE_DAILY") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nutrition_input_schedule")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "یادداشت‌ها" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rec = NutritionRecordEntity(
                        birdRingNumber = birdRing.trim().ifBlank { null },
                        cageCode = cageCode.trim().ifBlank { null },
                        foodType = foodType,
                        amount = amount.trim().ifBlank { "Standard dish" },
                        consumptionRate = consumption,
                        waterType = waterType,
                        supplements = supplements.trim(),
                        feedingSchedule = feedingSchedule.trim().ifBlank { "MORNING" },
                        cost = costStr.toDoubleOrNull() ?: 0.0,
                        recordDate = System.currentTimeMillis(),
                        notes = notes.trim().ifBlank { null }
                    )
                    onSave(rec)
                },
                modifier = Modifier.testTag("nutrition_dialog_save_button")
            ) {
                Text(if (isPersian) "ثبت وعده" else "Save Log")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
