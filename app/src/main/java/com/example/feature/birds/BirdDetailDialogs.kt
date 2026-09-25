package com.example.feature.birds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity
import kotlin.math.roundToInt

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (grams: Double, score: Int, condition: String, notes: String?) -> Unit
) {
    var gramsText by remember { mutableStateOf("45.0") }
    var score by remember { mutableFloatStateOf(3f) }
    var condition by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_weight_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.bird_add_weight),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { gramsText = it },
                    label = { Text("Weight (grams) / وزن به گرم") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_grams_input")
                )

                Text(text = "Body Condition Score: ${score.roundToInt()} / 5 (نمره وضعیت بدنی)")
                Slider(
                    value = score,
                    onValueChange = { score = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Keel Bone / وضعیت تیغه سینه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / توضیحات") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = gramsText.toDoubleOrNull() ?: 45.0
                    onSave(g, score.roundToInt(), condition, notes.takeIf { it.isNotBlank() })
                },
                modifier = Modifier.testTag("save_weight_button")
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
fun AddHealthRecordDialog(
    onDismiss: () -> Unit,
    onSave: (issue: String, symptoms: String, diagnosis: String, vet: String, cost: Double) -> Unit
) {
    var issue by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var vet by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_health_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.bird_add_health),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = issue,
                    onValueChange = { issue = it },
                    label = { Text("Issue Title / عنوان عارضه *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_issue_input")
                )

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms / علائم بالینی") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosis / تشخیص و تجویز") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vet,
                    onValueChange = { vet = it },
                    label = { Text("Attending Vet / دامپزشک معالج") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Treatment Cost / هزینه درمان (USD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (issue.isNotBlank()) {
                        val cost = costText.toDoubleOrNull() ?: 0.0
                        onSave(issue, symptoms, diagnosis, vet, cost)
                    }
                },
                modifier = Modifier.testTag("save_health_button")
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
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, uri: String, isPrimary: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.bird_add_photo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Photo Title / عنوان عکس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uri,
                    onValueChange = { uri = it },
                    label = { Text("Image URI / Path / آدرس فایل") },
                    placeholder = { Text("https:// or file://") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                    Text(text = "Set as Primary Profile Photo / عکس اصلی")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (uri.isNotBlank()) {
                    onSave(title, uri, isPrimary)
                }
            }) {
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
fun EditPedigreeDialog(
    current: PedigreeRecordEntity?,
    birdRing: String,
    onDismiss: () -> Unit,
    onSave: (PedigreeRecordEntity) -> Unit
) {
    var sireRing by remember { mutableStateOf(current?.sireRing ?: "") }
    var damRing by remember { mutableStateOf(current?.damRing ?: "") }
    var pgSire by remember { mutableStateOf(current?.paternalGrandsire ?: "") }
    var pgDam by remember { mutableStateOf(current?.paternalGranddam ?: "") }
    var mgSire by remember { mutableStateOf(current?.maternalGrandsire ?: "") }
    var mgDam by remember { mutableStateOf(current?.maternalGranddam ?: "") }
    var breederName by remember { mutableStateOf(current?.breederName ?: "Aviary Master") }
    var breederCode by remember { mutableStateOf(current?.breederCode ?: "IR-AV-01") }
    var coiText by remember { mutableStateOf(((current?.inbreedingCoefficient ?: 0.0) * 100).toString()) }
    var lineageNotes by remember { mutableStateOf(current?.lineageNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_pedigree_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Edit Pedigree Lineage / ویرایش شجره‌نامه",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = sireRing,
                    onValueChange = { sireRing = it },
                    label = { Text("Sire Ring (Father / پدر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = damRing,
                    onValueChange = { damRing = it },
                    label = { Text("Dam Ring (Mother / مادر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pgSire,
                    onValueChange = { pgSire = it },
                    label = { Text("Paternal Grandsire / پدربزرگ پدری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pgDam,
                    onValueChange = { pgDam = it },
                    label = { Text("Paternal Granddam / مادربزرگ پدری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mgSire,
                    onValueChange = { mgSire = it },
                    label = { Text("Maternal Grandsire / پدربزرگ مادری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mgDam,
                    onValueChange = { mgDam = it },
                    label = { Text("Maternal Granddam / مادربزرگ مادری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = breederName,
                    onValueChange = { breederName = it },
                    label = { Text("Breeder Name / نام پرورش‌دهنده") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = breederCode,
                    onValueChange = { breederCode = it },
                    label = { Text("Breeder Ring Code / کد حلقه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = coiText,
                    onValueChange = { coiText = it },
                    label = { Text("COI % / درصد ضریب هم‌خونی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lineageNotes,
                    onValueChange = { lineageNotes = it },
                    label = { Text("Lineage Notes / یادداشت‌های نسب") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val coi = (coiText.toDoubleOrNull() ?: 0.0) / 100.0
                val entity = current?.copy(
                    sireRing = sireRing.takeIf { it.isNotBlank() },
                    damRing = damRing.takeIf { it.isNotBlank() },
                    paternalGrandsire = pgSire.takeIf { it.isNotBlank() },
                    paternalGranddam = pgDam.takeIf { it.isNotBlank() },
                    maternalGrandsire = mgSire.takeIf { it.isNotBlank() },
                    maternalGranddam = mgDam.takeIf { it.isNotBlank() },
                    breederName = breederName.takeIf { it.isNotBlank() },
                    breederCode = breederCode.takeIf { it.isNotBlank() },
                    inbreedingCoefficient = coi,
                    lineageNotes = lineageNotes.takeIf { it.isNotBlank() },
                    updatedAt = System.currentTimeMillis()
                ) ?: PedigreeRecordEntity(
                    birdRingNumber = birdRing,
                    sireRing = sireRing.takeIf { it.isNotBlank() },
                    damRing = damRing.takeIf { it.isNotBlank() },
                    paternalGrandsire = pgSire.takeIf { it.isNotBlank() },
                    paternalGranddam = pgDam.takeIf { it.isNotBlank() },
                    maternalGrandsire = mgSire.takeIf { it.isNotBlank() },
                    maternalGranddam = mgDam.takeIf { it.isNotBlank() },
                    breederName = breederName.takeIf { it.isNotBlank() },
                    breederCode = breederCode.takeIf { it.isNotBlank() },
                    inbreedingCoefficient = coi,
                    lineageNotes = lineageNotes.takeIf { it.isNotBlank() }
                )
                onSave(entity)
            }) {
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
fun EditGeneticsDialog(
    current: BirdGeneticsEntity?,
    birdRing: String,
    onDismiss: () -> Unit,
    onSave: (BirdGeneticsEntity) -> Unit
) {
    var visual by remember { mutableStateOf(current?.visualMutations ?: "Spangle Opaline") }
    var split by remember { mutableStateOf(current?.splitMutations ?: "Split Blue, Cinnamon") }
    var baseSeries by remember { mutableStateOf(current?.baseSeries ?: "BLUE") }
    var violet by remember { mutableStateOf(current?.violetFactor ?: false) }
    var grey by remember { mutableStateOf(current?.greyFactor ?: false) }
    var cinnamon by remember { mutableStateOf(current?.cinnamonFactor ?: false) }
    var ino by remember { mutableStateOf(current?.inoFactor ?: false) }
    var notes by remember { mutableStateOf(current?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_genetics_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Edit Genetics / مشخصات ژنتیکی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = visual,
                    onValueChange = { visual = it },
                    label = { Text("Visual Mutations / جهش‌های ظاهری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = split,
                    onValueChange = { split = it },
                    label = { Text("Split (Carrier) / ژن‌های ناقل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = baseSeries,
                    onValueChange = { baseSeries = it },
                    label = { Text("Base Series (GREEN / BLUE) / سری پایه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Special Genetic Factors / فاکتورهای ویژه:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = violet, onCheckedChange = { violet = it })
                        Text("Violet Factor")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = grey, onCheckedChange = { grey = it })
                        Text("Grey Factor")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = cinnamon, onCheckedChange = { cinnamon = it })
                        Text("Cinnamon Factor")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = ino, onCheckedChange = { ino = it })
                        Text("Ino Factor")
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Genetics Notes / یادداشت‌های ژنتیکی") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val entity = current?.copy(
                    visualMutations = visual,
                    splitMutations = split,
                    baseSeries = baseSeries,
                    violetFactor = violet,
                    greyFactor = grey,
                    cinnamonFactor = cinnamon,
                    inoFactor = ino,
                    notes = notes.takeIf { it.isNotBlank() },
                    updatedAt = System.currentTimeMillis()
                ) ?: BirdGeneticsEntity(
                    birdRingNumber = birdRing,
                    visualMutations = visual,
                    splitMutations = split,
                    baseSeries = baseSeries,
                    violetFactor = violet,
                    greyFactor = grey,
                    cinnamonFactor = cinnamon,
                    inoFactor = ino,
                    notes = notes.takeIf { it.isNotBlank() }
                )
                onSave(entity)
            }) {
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
