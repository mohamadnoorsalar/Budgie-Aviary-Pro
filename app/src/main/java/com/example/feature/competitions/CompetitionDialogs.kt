package com.example.feature.competitions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.competition.CompetitionJudgingHelper
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.JudgeEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCompetitionDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        location: String,
        organizingClub: String,
        eventDate: Long,
        showStandard: String,
        categories: List<String>,
        judgingCodes: List<String>,
        notes: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organizingClub by remember { mutableStateOf("") }
    var showStandard by remember { mutableStateOf("WBO") } // WBO, NATIONAL, CUSTOM
    var standardExpanded by remember { mutableStateOf(false) }

    var categoriesText by remember {
        mutableStateOf("English Show Cock, English Show Hen, Young Birds (2026), Rare Mutations, Team Class")
    }
    var judgingCodesText by remember {
        mutableStateOf("JC-01, JC-02, JC-03, JC-04, JC-05")
    }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_competition_dialog"),
        title = {
            Text("ایجاد مسابقه جدید / Create Competition", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان مسابقه / Competition Title *") },
                    placeholder = { Text("e.g. National Budgie Championship 2026") },
                    modifier = Modifier.fillMaxWidth().testTag("comp_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("مکان برگزاری / Location *") },
                    placeholder = { Text("e.g. Tehran Exhibition Arena / Hall B") },
                    modifier = Modifier.fillMaxWidth().testTag("comp_location_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = organizingClub,
                    onValueChange = { organizingClub = it },
                    label = { Text("باشگاه برگزارکننده / Organizing Club") },
                    placeholder = { Text("e.g. Iranian Show Budgie Association (ISBA)") },
                    modifier = Modifier.fillMaxWidth().testTag("comp_club_input"),
                    singleLine = true
                )

                // Show Standard Selector
                ExposedDropdownMenuBox(
                    expanded = standardExpanded,
                    onExpandedChange = { standardExpanded = !standardExpanded }
                ) {
                    OutlinedTextField(
                        value = when (showStandard) {
                            "WBO" -> "World Budgerigar Organisation (WBO Standard)"
                            "NATIONAL" -> "Persian National Standard (استاندارد ملی ایران)"
                            else -> "Custom Configurable Criteria Standard"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("استاندارد داوری / Judging Standard") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = standardExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = standardExpanded,
                        onDismissRequest = { standardExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("WBO - World Budgerigar Organisation") },
                            onClick = {
                                showStandard = "WBO"
                                standardExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("NATIONAL - Persian National Show Standard") },
                            onClick = {
                                showStandard = "NATIONAL"
                                standardExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("CUSTOM - Configurable Criteria Standard") },
                            onClick = {
                                showStandard = "CUSTOM"
                                standardExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = categoriesText,
                    onValueChange = { categoriesText = it },
                    label = { Text("کلاس‌ها و رده‌ها / Classes & Categories (کاما جدا کنید)") },
                    modifier = Modifier.fillMaxWidth().testTag("comp_categories_input"),
                    minLines = 2
                )

                OutlinedTextField(
                    value = judgingCodesText,
                    onValueChange = { judgingCodesText = it },
                    label = { Text("کدهای اختصاصی داوری / Judging Codes (کاما جدا کنید)") },
                    placeholder = { Text("e.g. JC-01, JC-02, JC-03") },
                    modifier = Modifier.fillMaxWidth().testTag("comp_codes_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و قوانین / Rules & Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && location.isNotBlank()) {
                        val cats = categoriesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val codes = judgingCodesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(
                            title,
                            location,
                            organizingClub.ifBlank { "National Club" },
                            System.currentTimeMillis(),
                            showStandard,
                            cats,
                            codes,
                            notes.ifBlank { null }
                        )
                    }
                },
                enabled = title.isNotBlank() && location.isNotBlank(),
                modifier = Modifier.testTag("save_competition_button")
            ) {
                Text("ایجاد مسابقه / Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف / Cancel")
            }
        }
    )
}

@Composable
fun AddJudgeDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        certification: String,
        affiliation: String?,
        country: String?,
        contactInfo: String?,
        assignedCodes: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var certification by remember { mutableStateOf("WBO_INTERNATIONAL") }
    var affiliation by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Iran") }
    var contactInfo by remember { mutableStateOf("") }
    var assignedCodes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("معرفی داور رسمی / Register Official Judge", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام داور / Judge Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("judge_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = certification,
                    onValueChange = { certification = it },
                    label = { Text("درجه و گواهینامه / Certification *") },
                    placeholder = { Text("e.g. WBO_INTERNATIONAL, NATIONAL, SENIOR_CLUB") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = affiliation,
                    onValueChange = { affiliation = it },
                    label = { Text("انجمن / Affiliation Club") },
                    placeholder = { Text("e.g. European Budgerigar Society (EBS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("کشور / Country") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = assignedCodes,
                    onValueChange = { assignedCodes = it },
                    label = { Text("کدهای داوری مجاز / Assigned Judging Codes") },
                    placeholder = { Text("e.g. JC-01, JC-02") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("اطلاعات تماس / Contact Info") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            certification,
                            affiliation.ifBlank { null },
                            country.ifBlank { null },
                            contactInfo.ifBlank { null },
                            assignedCodes.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_judge_button")
            ) {
                Text("ثبت داور / Save Judge")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف / Cancel")
            }
        }
    )
}

@Composable
fun EditCriteriaDialog(
    competition: CompetitionEntity,
    currentCriteria: List<CompetitionCriterionEntity>,
    onDismiss: () -> Unit,
    onSave: (List<CompetitionCriterionEntity>) -> Unit
) {
    val criteriaList = remember {
        val list = if (currentCriteria.isEmpty()) {
            when (competition.showStandard) {
                "NATIONAL" -> CompetitionJudgingHelper.createNationalStandardCriteria(competition.id)
                else -> CompetitionJudgingHelper.createWboStandardCriteria(competition.id)
            }
        } else {
            currentCriteria
        }
        mutableListOf<CompetitionCriterionEntity>().apply { addAll(list) }
    }

    var itemsState by remember { mutableStateOf(criteriaList.toList()) }

    var newName by remember { mutableStateOf("") }
    var newMaxScore by remember { mutableStateOf("15.0") }
    var newWeight by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("پیکربندی معیارهای داوری / Configure Criteria", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "استاندارد فعلی: ${competition.showStandard} (${itemsState.size} معیار تعریف شده)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                itemsState.forEachIndexed { index, criterion ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${criterion.name}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "حداکثر امتیاز: ${criterion.maxScore} pts | ضریب وزن: ${criterion.weight}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                val updated = itemsState.toMutableList()
                                updated.removeAt(index)
                                itemsState = updated
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "افزودن معیار سفارشی جدید / Add Custom Criterion",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("نام معیار (مثلاً فرم منقار یا طول دم)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newMaxScore,
                        onValueChange = { newMaxScore = it },
                        label = { Text("حداکثر نمره") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("ضریب وزن") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val max = newMaxScore.toDoubleOrNull() ?: 15.0
                            val wt = newWeight.toDoubleOrNull() ?: 1.0
                            val added = CompetitionCriterionEntity(
                                competitionId = competition.id,
                                name = newName.trim(),
                                maxScore = max,
                                weight = wt,
                                displayOrder = itemsState.size + 1
                            )
                            itemsState = itemsState + added
                            newName = ""
                            newMaxScore = "15.0"
                            newWeight = "1.0"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن این معیار به لیست")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(itemsState) },
                enabled = itemsState.isNotEmpty(),
                modifier = Modifier.testTag("save_criteria_button")
            ) {
                Text("ذخیره تغییرات / Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف / Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialJudgingScoringDialog(
    competition: CompetitionEntity,
    criteriaList: List<CompetitionCriterionEntity>,
    judges: List<JudgeEntity>,
    registeredBirds: List<BirdEntity>,
    onDismiss: () -> Unit,
    onSaveScore: (
        birdRing: String,
        participantName: String?,
        judgeId: String?,
        judgeName: String?,
        judgingCode: String?,
        showClass: String,
        cageNumber: String?,
        criteriaScores: Map<String, Double>,
        awardOverride: String?,
        notes: String?,
        photoUri: String?,
        isConfirmed: Boolean,
        signature: String?
    ) -> Unit
) {
    val categories = remember(competition.categoriesJson) {
        val parsed = CompetitionJudgingHelper.parseJsonList(competition.categoriesJson)
        if (parsed.isEmpty()) listOf("English Show Cock", "English Show Hen", "Young Birds", "Open Class") else parsed
    }

    var selectedBirdRing by remember { mutableStateOf(registeredBirds.firstOrNull()?.ringNumber ?: "") }
    var participantName by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(categories.firstOrNull() ?: "Standard Class") }
    var classDropdownOpen by remember { mutableStateOf(false) }

    var selectedJudge by remember { mutableStateOf(judges.firstOrNull()) }
    var judgeDropdownOpen by remember { mutableStateOf(false) }

    var judgingCode by remember { mutableStateOf("") }
    var cageNumberInShow by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    var judgeComments by remember { mutableStateOf("") }

    var isDigitallyConfirmed by remember { mutableStateOf(false) }
    var judgeSignature by remember { mutableStateOf("") }

    // Map of criterion ID to awarded score
    val scoreMap = remember {
        mutableStateMapOf<String, Double>().apply {
            criteriaList.forEach { c -> put(c.id, c.maxScore * 0.85) }
        }
    }

    val liveTotalScore = remember(scoreMap.toMap(), criteriaList) {
        CompetitionJudgingHelper.calculateTotalWeightedScore(criteriaList, scoreMap)
    }

    val liveAward = remember(liveTotalScore) {
        CompetitionJudgingHelper.suggestAwardTitle(liveTotalScore)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("داوری رسمی و ثبت امتیاز / Official Show Judging", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bird selection
                OutlinedTextField(
                    value = selectedBirdRing,
                    onValueChange = { selectedBirdRing = it },
                    label = { Text("شماره پلاک پرنده / Bird Ring Number *") },
                    placeholder = { Text("e.g. IR-2026-042") },
                    modifier = Modifier.fillMaxWidth().testTag("score_bird_ring_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = participantName,
                    onValueChange = { participantName = it },
                    label = { Text("نام شرکت‌کننده یا سالن / Exhibitor / Breeder") },
                    placeholder = { Text("e.g. Master Aviary / Ali Rezaei") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Show Class Selector
                ExposedDropdownMenuBox(
                    expanded = classDropdownOpen,
                    onExpandedChange = { classDropdownOpen = !classDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = selectedClass,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("کلاس مسابقه / Show Class *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("score_class_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownOpen,
                        onDismissRequest = { classDropdownOpen = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedClass = cat
                                    classDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Judge Selector
                ExposedDropdownMenuBox(
                    expanded = judgeDropdownOpen,
                    onExpandedChange = { judgeDropdownOpen = !judgeDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = selectedJudge?.let { "${it.name} (${it.certification})" } ?: "انتخاب داور یا داور مستقل",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("داور رسمی / Official Judge *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = judgeDropdownOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("score_judge_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = judgeDropdownOpen,
                        onDismissRequest = { judgeDropdownOpen = false }
                    ) {
                        judges.forEach { judge ->
                            DropdownMenuItem(
                                text = { Text("${judge.name} - ${judge.certification} (${judge.country ?: "Iran"})") },
                                onClick = {
                                    selectedJudge = judge
                                    judgeDropdownOpen = false
                                    if (!judge.assignedCodes.isNullOrBlank()) {
                                        judgingCode = judge.assignedCodes!!.split(",").firstOrNull()?.trim() ?: ""
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = judgingCode,
                        onValueChange = { judgingCode = it },
                        label = { Text("کد داوری / Code") },
                        placeholder = { Text("JC-01") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cageNumberInShow,
                        onValueChange = { cageNumberInShow = it },
                        label = { Text("شماره قفس نمایش / Cage") },
                        placeholder = { Text("C-12") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = photoUri,
                    onValueChange = { photoUri = it },
                    label = { Text("عکس پرنده در قفس داوری / Photo URL / Path") },
                    placeholder = { Text("e.g. content://media/photos/show_cage_01.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Criteria Sliders Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ارزیابی معیارهای استاندارد (${criteriaList.size} معیار)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        criteriaList.forEach { criterion ->
                            val currentVal = scoreMap[criterion.id] ?: (criterion.maxScore * 0.8)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = criterion.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "%.1f / %.1f pts".format(Locale.US, currentVal, criterion.maxScore),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = currentVal.toFloat(),
                                    onValueChange = { scoreMap[criterion.id] = it.toDouble() },
                                    valueRange = 0f..criterion.maxScore.toFloat(),
                                    steps = (criterion.maxScore * 2).toInt() - 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع امتیاز نهایی:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "%.2f pts".format(Locale.US, liveTotalScore),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "عنوان رتبه پیشنهادی: $liveAward",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                OutlinedTextField(
                    value = judgeComments,
                    onValueChange = { judgeComments = it },
                    label = { Text("نظر کارشناسی و توصیه‌های داور / Comments") },
                    modifier = Modifier.fillMaxWidth().testTag("judge_comments_input"),
                    minLines = 2
                )

                // Digital Confirmation & Signature
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDigitallyConfirmed = !isDigitallyConfirmed }
                ) {
                    Checkbox(
                        checked = isDigitallyConfirmed,
                        onCheckedChange = { isDigitallyConfirmed = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تایید دیجیتال رسمی توسط داور مسابقه (Digital Confirmation)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isDigitallyConfirmed) {
                    OutlinedTextField(
                        value = judgeSignature,
                        onValueChange = { judgeSignature = it },
                        label = { Text("امضای دیجیتال / نام تاییدکننده *") },
                        placeholder = { Text("Judge Electronic Signature / e.g. M. Moradi, WBO Judge") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedBirdRing.isNotBlank() && selectedClass.isNotBlank()) {
                        onSaveScore(
                            selectedBirdRing,
                            participantName.ifBlank { null },
                            selectedJudge?.id,
                            selectedJudge?.name,
                            judgingCode.ifBlank { null },
                            selectedClass,
                            cageNumberInShow.ifBlank { null },
                            scoreMap.toMap(),
                            liveAward,
                            judgeComments.ifBlank { null },
                            photoUri.ifBlank { null },
                            isDigitallyConfirmed,
                            judgeSignature.ifBlank { null }
                        )
                    }
                },
                enabled = selectedBirdRing.isNotBlank() && selectedClass.isNotBlank(),
                modifier = Modifier.testTag("confirm_save_score_button")
            ) {
                Text("ثبت قطعی نمره داور / Submit Score")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف / Cancel")
            }
        }
    )
}
