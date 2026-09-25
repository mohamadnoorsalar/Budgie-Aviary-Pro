package com.example.feature.health

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MedicationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicalDisclaimerBanner(
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isPersian) "ثبت و مدیریت اطلاعات پزشکی سالن" else "Aviary Health & Treatment Records",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPersian)
                        "این بخش صرفاً برای ثبت پرونده بالینی، سوابق درمان و زمان‌بندی داروها است و جایگزین تشخیص دامپزشک متخصص پرندگان نمی‌باشد."
                    else
                        "This module is strictly for recording and managing flock medical history, medications, and health logs. It is not an automated medical diagnostic tool. Always consult an avian veterinarian.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LogHealthAndTreatmentDialog(
    initialBirdRing: String? = null,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (HealthRecordEntity, Boolean) -> Unit
) {
    var birdRing by remember { mutableStateOf(initialBirdRing ?: "") }
    var symptoms by remember { mutableStateOf("") }
    var recordedProblem by remember { mutableStateOf("") }
    var medicationName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("ONCE_DAILY") }
    var durationDaysStr by remember { mutableStateOf("7") }
    var supplements by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var scheduleReminders by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "ثبت پرونده درمان و سلامت" else "Log Health & Treatment Record",
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
                MedicalDisclaimerBanner(isPersian = isPersian)

                OutlinedTextField(
                    value = birdRing,
                    onValueChange = { birdRing = it },
                    label = { Text(if (isPersian) "شماره پلاک پرنده" else "Bird Ring Number") },
                    placeholder = { Text("e.g. IR-2024-001") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_ring")
                )

                OutlinedTextField(
                    value = recordedProblem,
                    onValueChange = { recordedProblem = it },
                    label = { Text(if (isPersian) "مشکل یا بیماری ثبت شده" else "Recorded Problem / Issue") },
                    placeholder = { Text(if (isPersian) "مثال: عفونت تنفسی، کنه کیسه هوایی، ورم روده" else "e.g. Respiratory Infection, Scaly Mites, Enteritis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_problem")
                )

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text(if (isPersian) "علائم مشاهده شده" else "Symptoms") },
                    placeholder = { Text(if (isPersian) "مثال: بی‌حالی، پف‌کردگی، عطسه، دم‌زدن، مدفوع آبکی" else "e.g. Fluffed feathers, sneezing, tail bobbing, loose droppings") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_symptoms")
                )

                Text(
                    text = if (isPersian) "اطلاعات دارو و درمان:" else "Medication & Treatment Protocol:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    label = { Text(if (isPersian) "نام دارو" else "Medication") },
                    placeholder = { Text(if (isPersian) "مثال: بایتریل، رونیدازول، اسکات، تایلوزین" else "e.g. Baytril, Ronidazole, Scatt, Doxycycline") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_medication")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        label = { Text(if (isPersian) "دوز مصرفی" else "Dose") },
                        placeholder = { Text("0.05 ml / 2 drops") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_input_dose")
                    )

                    OutlinedTextField(
                        value = durationDaysStr,
                        onValueChange = { durationDaysStr = it },
                        label = { Text(if (isPersian) "طول درمان (روز)" else "Duration (Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_input_duration")
                    )
                }

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text(if (isPersian) "نوبت مصرف (تکرار)" else "Frequency") },
                    placeholder = { Text("ONCE_DAILY / TWICE_DAILY") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_frequency")
                )

                OutlinedTextField(
                    value = supplements,
                    onValueChange = { supplements = it },
                    label = { Text(if (isPersian) "مکمل‌ها و تقویتی همراه" else "Supplements") },
                    placeholder = { Text(if (isPersian) "مثال: پروبیوتیک، الکترولیت، ویتامین ب کمپلکس" else "e.g. Probiotic, Electrolytes, B-Complex, Calcivet") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_supplements")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات تکمیلی" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (medicationName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = scheduleReminders,
                            onCheckedChange = { scheduleReminders = it },
                            modifier = Modifier.testTag("health_checkbox_reminders")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersian)
                                "تنظیم یادآور برای موعد مصرف داروها"
                            else
                                "Add reminders for scheduled medication doses",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (symptoms.isNotBlank() || recordedProblem.isNotBlank() || medicationName.isNotBlank()) {
                        val duration = durationDaysStr.toIntOrNull() ?: 7
                        val now = System.currentTimeMillis()
                        val endDate = now + (duration * 86400000L)
                        val record = HealthRecordEntity(
                            birdRingNumber = birdRing.trim().ifBlank { null },
                            symptoms = symptoms.trim().ifBlank { "Unspecified symptoms" },
                            diagnosis = recordedProblem.trim().ifBlank { "General Observation" },
                            recordedProblem = recordedProblem.trim().ifBlank { "General Observation" },
                            medicationName = medicationName.trim().ifBlank { null },
                            dosage = dose.trim().ifBlank { null },
                            frequency = frequency.trim().ifBlank { "ONCE_DAILY" },
                            treatmentDurationDays = duration,
                            startDate = now,
                            endDate = endDate,
                            supplements = supplements.trim().ifBlank { null },
                            notes = notes.trim().ifBlank { null }
                        )
                        onSave(record, scheduleReminders && medicationName.isNotBlank())
                    }
                },
                modifier = Modifier.testTag("health_dialog_save_button")
            ) {
                Text(if (isPersian) "ذخیره پرونده" else "Save Record")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
