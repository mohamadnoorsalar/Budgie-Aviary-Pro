package com.example.feature.genetics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.common.BirdGender
import com.example.core.common.displayName
import com.example.core.genetics.BaseColorSeries
import com.example.core.genetics.GenerationBreedingGoal
import com.example.core.genetics.GeneticsCalendarEvent
import com.example.core.genetics.InbreedingWarningLevel
import com.example.core.genetics.OffspringPredictionResult
import com.example.core.genetics.PhenotypePrediction
import com.example.core.genetics.PredictionVsActualReport
import com.example.core.genetics.RelatednessAnalysisReport
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.PairEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneticsScreen(
    modifier: Modifier = Modifier,
    viewModel: GeneticsViewModel = viewModel(),
    onNavigateToBird: (String) -> Unit = {},
    onNavigateToPedigree: (String) -> Unit = {}
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val allBirds by viewModel.allBirds.collectAsState()
    val allPairs by viewModel.allPairs.collectAsState()
    val selectedSireRing by viewModel.selectedSireRing.collectAsState()
    val selectedDamRing by viewModel.selectedDamRing.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()
    val relatednessReport by viewModel.relatednessReport.collectAsState()
    val predictionVsActual by viewModel.predictionVsActual.collectAsState()
    val searchFilters by viewModel.searchFilters.collectAsState()
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val generationPlanningResult by viewModel.generationPlanningResult.collectAsState()
    val calendarMilestones by viewModel.calendarMilestones.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        if (isFa) "شبیه‌ساز جفت و پیش‌بینی" else "Pair Simulator",
        if (isFa) "برنامه‌ریزی نسل‌ها" else "Generation Planner",
        if (isFa) "جستجوی ژنتیکی گله" else "Flock Genetic Search",
        if (isFa) "تقویم تکامل ژنتیک" else "Genetics Calendar",
        if (isFa) "راهنمای جهش‌ها" else "Mutation Guide"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("genetics_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (activeTab) {
            0 -> PairSimulatorTab(
                allBirds = allBirds,
                allPairs = allPairs,
                selectedSireRing = selectedSireRing,
                selectedDamRing = selectedDamRing,
                predictionResult = predictionResult,
                relatednessReport = relatednessReport,
                predictionVsActual = predictionVsActual,
                isFa = isFa,
                onSelectSire = { viewModel.selectSire(it) },
                onSelectDam = { viewModel.selectDam(it) },
                onSelectPair = { viewModel.selectBreedingPair(it) },
                onNavigateToBird = onNavigateToBird,
                onNavigateToPedigree = onNavigateToPedigree
            )
            1 -> GenerationPlannerTab(
                availableGoals = viewModel.availableGoals,
                selectedGoal = selectedGoal,
                planningResult = generationPlanningResult,
                isFa = isFa,
                onSelectGoal = { viewModel.selectGoal(it) },
                onTestPairInSimulator = { sire, dam ->
                    viewModel.selectSire(sire)
                    viewModel.selectDam(dam)
                    activeTab = 0
                }
            )
            2 -> GeneticSearchTab(
                allBirds = allBirds,
                filters = searchFilters,
                isFa = isFa,
                onFiltersChanged = { viewModel.updateSearchFilters(it) },
                onNavigateToBird = onNavigateToBird,
                onSelectForSimulator = { ring, gender ->
                    if (gender == BirdGender.MALE) viewModel.selectSire(ring) else viewModel.selectDam(ring)
                    activeTab = 0
                },
                onNavigateToPedigree = onNavigateToPedigree
            )
            3 -> GeneticsCalendarTab(
                milestones = calendarMilestones,
                isFa = isFa
            )
            4 -> MutationGuideTab(isFa = isFa)
        }
    }
}

// -------------------------------------------------------------------------
// TAB 1: PAIR SIMULATOR & PROBABILISTIC PREDICTIONS
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PairSimulatorTab(
    allBirds: List<BirdEntity>,
    allPairs: List<PairEntity>,
    selectedSireRing: String,
    selectedDamRing: String,
    predictionResult: OffspringPredictionResult?,
    relatednessReport: RelatednessAnalysisReport?,
    predictionVsActual: PredictionVsActualReport?,
    isFa: Boolean,
    onSelectSire: (String) -> Unit,
    onSelectDam: (String) -> Unit,
    onSelectPair: (PairEntity) -> Unit,
    onNavigateToBird: (String) -> Unit,
    onNavigateToPedigree: (String) -> Unit
) {
    val males = allBirds.filter { it.gender == BirdGender.MALE || it.gender == BirdGender.UNKNOWN }
    val females = allBirds.filter { it.gender == BirdGender.FEMALE || it.gender == BirdGender.UNKNOWN }

    var sexFilter by remember { mutableStateOf("ALL") } // ALL, MALE, FEMALE
    var sireExpanded by remember { mutableStateOf(false) }
    var damExpanded by remember { mutableStateOf(false) }
    var pairExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Select Existing Active Breeding Pair
        if (allPairs.isNotEmpty()) {
            item {
                ExposedDropdownMenuBox(
                    expanded = pairExpanded,
                    onExpandedChange = { pairExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedSireRing.isNotBlank() && selectedDamRing.isNotBlank()) {
                            "Pair: $selectedSireRing (Sire) × $selectedDamRing (Dam)"
                        } else {
                            if (isFa) "انتخاب سریع از جفت‌های مولد فعال..." else "Quick-Select Active Breeding Pair..."
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pairExpanded) },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        label = { Text(if (isFa) "جفت‌های مولد ثبت شده" else "Registered Breeding Pairs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = pairExpanded,
                        onDismissRequest = { pairExpanded = false }
                    ) {
                        allPairs.forEach { pair ->
                            DropdownMenuItem(
                                text = {
                                    Text("Pair #${pair.id}: ${pair.maleRingNumber} (Cock) × ${pair.femaleRingNumber} (Hen)")
                                },
                                onClick = {
                                    onSelectPair(pair)
                                    pairExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Sire and Dam Selector Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sire Selector Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Male,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isFa) "پدر (نر - Sire)" else "Sire (Cock)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = sireExpanded,
                            onExpandedChange = { sireExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSireRing.ifBlank { if (isFa) "انتخاب نر..." else "Choose Cock..." },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sireExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = sireExpanded,
                                onDismissRequest = { sireExpanded = false }
                            ) {
                                males.forEach { bird ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${bird.ringNumber} (${bird.name ?: bird.color})")
                                        },
                                        onClick = {
                                            onSelectSire(bird.ringNumber)
                                            sireExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val sireBird = allBirds.firstOrNull { it.ringNumber == selectedSireRing }
                        if (sireBird != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${sireBird.color} • ${sireBird.mutation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Dam Selector Card
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Female,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isFa) "مادر (ماده - Dam)" else "Dam (Hen)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = damExpanded,
                            onExpandedChange = { damExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedDamRing.ifBlank { if (isFa) "انتخاب ماده..." else "Choose Hen..." },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = damExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = damExpanded,
                                onDismissRequest = { damExpanded = false }
                            ) {
                                females.forEach { bird ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${bird.ringNumber} (${bird.name ?: bird.color})")
                                        },
                                        onClick = {
                                            onSelectDam(bird.ringNumber)
                                            damExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val damBird = allBirds.firstOrNull { it.ringNumber == selectedDamRing }
                        if (damBird != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${damBird.color} • ${damBird.mutation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // RELATEDNESS ANALYSIS & CLOSE-RELATIVE WARNING BANNER
        if (relatednessReport != null) {
            item {
                RelatednessWarningCard(
                    report = relatednessReport,
                    isFa = isFa,
                    onViewPedigree = { onNavigateToPedigree(selectedSireRing) }
                )
            }
        }

        // OFFSPRING PROBABILISTIC PREDICTIONS
        if (predictionResult != null) {
            // Scientific Probability Disclaimer Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isFa)
                                "پیش‌بینی‌های ژنتیکی همواره به صورت «احتمالات آماری به ازای هر تخم» ارائه می‌شوند و تضمین قطعی نیستند. هر لقاح تخم یک رخداد احتمالی مستقل ژنتیکی است."
                            else
                                "Genetic predictions are presented as probabilistic odds per individual egg, not guarantees. Each fertilization is an independent Mendelian event.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sex Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFa) "نتایج پیش‌بینی فنوتیپ جوجه‌ها" else "Probabilistic Offspring Outcomes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = sexFilter == "ALL",
                            onClick = { sexFilter = "ALL" },
                            label = { Text(if (isFa) "همه" else "All") }
                        )
                        FilterChip(
                            selected = sexFilter == "MALE",
                            onClick = { sexFilter = "MALE" },
                            label = { Text(if (isFa) "نرها" else "Cocks") }
                        )
                        FilterChip(
                            selected = sexFilter == "FEMALE",
                            onClick = { sexFilter = "FEMALE" },
                            label = { Text(if (isFa) "ماده‌ها" else "Hens") }
                        )
                    }
                }
            }

            // List of Probabilistic Outcomes
            val displayedOutcomes = when (sexFilter) {
                "MALE" -> predictionResult.maleOutcomes
                "FEMALE" -> predictionResult.femaleOutcomes
                else -> predictionResult.outcomes
            }

            items(displayedOutcomes) { outcome ->
                PhenotypeOutcomeCard(outcome = outcome, isFa = isFa)
            }

            // Carrier Summary Banner
            if (predictionResult.carrierSummary.isNotEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isFa) "ژن‌های ناقل پنهان (Split Carriers) در نسل بعدی" else "Expected Carrier (Split) Inheritance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            predictionResult.carrierSummary.forEach { note ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(note, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // PREDICTION VS ACTUAL OFFSPRING SECTION
            if (predictionVsActual != null) {
                item {
                    PredictionVsActualSection(
                        report = predictionVsActual,
                        isFa = isFa
                    )
                }
            }
        } else {
            item {
                EmptySimulatorPlaceholder(isFa = isFa)
            }
        }
    }
}

@Composable
private fun RelatednessWarningCard(
    report: RelatednessAnalysisReport,
    isFa: Boolean,
    onViewPedigree: () -> Unit
) {
    val containerColor = when (report.warningLevel) {
        InbreedingWarningLevel.CRITICAL_WARNING -> MaterialTheme.colorScheme.errorContainer
        InbreedingWarningLevel.HIGH_RISK -> Color(0xFFFFF3E0)
        InbreedingWarningLevel.CAUTION_LINEBREEDING -> Color(0xFFFFFDE7)
        InbreedingWarningLevel.SAFE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }

    val contentColor = when (report.warningLevel) {
        InbreedingWarningLevel.CRITICAL_WARNING -> MaterialTheme.colorScheme.onErrorContainer
        InbreedingWarningLevel.HIGH_RISK -> Color(0xFFE65100)
        InbreedingWarningLevel.CAUTION_LINEBREEDING -> Color(0xFFF57F17)
        InbreedingWarningLevel.SAFE -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("relatedness_warning_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (report.warningLevel == InbreedingWarningLevel.SAFE) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFa) report.degree.labelFa else report.degree.labelEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                Badge(containerColor = contentColor) {
                    Text(
                        text = "F = ${String.format(Locale.US, "%.1f", report.inbreedingCoefficientF * 100)}%",
                        color = Color.White,
                        modifier = Modifier.padding(4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = report.warningMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )

            if (report.commonAncestors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFa) "اجداد مشترک شناسایی شده در شجره‌نامه:" else "Common Ancestors Identified in Lineage:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                report.commonAncestors.forEach { anc ->
                    Text(
                        text = "• ${anc.ancestorRing} ${anc.ancestorName?.let { "($it)" } ?: ""} - ${anc.pathDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor
                    )
                }
            }

            if (report.biologicalHazards.isNotEmpty() && report.warningLevel != InbreedingWarningLevel.SAFE) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = contentColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isFa) "خطرات بیولوژیکی هم‌خونی:" else "Potential Inbreeding Hazards:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                report.biologicalHazards.forEach { hazard ->
                    Text(
                        text = "⚠️ $hazard",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onViewPedigree,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFa) "مشاهده درخت شجره‌نامه" else "Inspect Lineage Tree")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhenotypeOutcomeCard(
    outcome: PhenotypePrediction,
    isFa: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("phenotype_outcome_${outcome.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sexIcon = if (outcome.targetSex == BirdGender.MALE) Icons.Default.Male else Icons.Default.Female
                    val sexColor = if (outcome.targetSex == BirdGender.MALE) Color(0xFF1976D2) else Color(0xFFE91E63)
                    Icon(
                        imageVector = sexIcon,
                        contentDescription = null,
                        tint = sexColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFa) outcome.phenotypeNameFa else outcome.phenotypeNameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = outcome.probabilityPercent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Probability progress bar
            LinearProgressIndicator(
                progress = { outcome.probability.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visual and Carrier Mutation Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                outcome.visualMutations.forEach { mut ->
                    AssistChip(
                        onClick = {},
                        label = { Text(mut, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    )
                }
                outcome.splitCarriers.forEach { split ->
                    AssistChip(
                        onClick = {},
                        label = { Text(split, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = outcome.biologicalNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PredictionVsActualSection(
    report: PredictionVsActualReport,
    isFa: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prediction_vs_actual_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFa) "مقایسه پیش‌بینی با نتایج واقعی ثبت شده" else "Prediction vs. Actual Offspring Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = report.sampleSizeSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (report.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                report.items.forEach { item ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = item.phenotypeName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Predicted: ${String.format(Locale.US, "%.1f", item.expectedProbabilityPercent)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Actual: ${item.actualCount}/${item.totalHatched} (${String.format(Locale.US, "%.1f", item.actualPercentage)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySimulatorPlaceholder(isFa: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Biotech,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isFa) "یک نر و یک ماده را برای شبیه‌سازی جفت انتخاب کنید" else "Select a Sire and Dam to Run Genetic Simulation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFa)
                    "موتور ژنتیک به طور خودکار قوانین مندلی، پیوستگی‌های جنسی Z/W، فاکتورهای تیره، اسپنگل و ضرایب هم‌خونی را تحلیل خواهد کرد."
                else
                    "The engine will instantly compute Mendelian ratios, sex-linked Z/W inheritance, dark factors, spangles, and Wright's inbreeding coefficients.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: BREEDER GOAL PLANNER (GENERATION PLANNING)
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenerationPlannerTab(
    availableGoals: List<GenerationBreedingGoal>,
    selectedGoal: GenerationBreedingGoal,
    planningResult: com.example.core.genetics.GenerationPlanningResult?,
    isFa: Boolean,
    onSelectGoal: (GenerationBreedingGoal) -> Unit,
    onTestPairInSimulator: (String, String) -> Unit
) {
    var goalMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFa) "برنامه‌ریزی نسل‌ها برای دستیابی به جهش هدف" else "Breeder Generation Roadmap Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFa)
                            "هدف پرورشی خود را مشخص کنید تا گله شما اسکن شده و جفت‌های مستقیم (نسل ۱) یا مسیرهای چندنسله (نسل ۲) به صورت خودکار محاسبه شوند."
                        else
                            "Select a target breeding goal to scan your flock for 1-generation direct pairings or 2-generation multi-step breeding roadmaps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Goal Selection Dropdown
        item {
            ExposedDropdownMenuBox(
                expanded = goalMenuExpanded,
                onExpandedChange = { goalMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (isFa) selectedGoal.titleFa else selectedGoal.titleEn,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalMenuExpanded) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    label = { Text(if (isFa) "هدف پرورشی مورد نظر" else "Breeding Goal") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = goalMenuExpanded,
                    onDismissRequest = { goalMenuExpanded = false }
                ) {
                    availableGoals.forEach { goal ->
                        DropdownMenuItem(
                            text = { Text(if (isFa) goal.titleFa else goal.titleEn) },
                            onClick = {
                                onSelectGoal(goal)
                                goalMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Direct 1-Generation Matches
        item {
            Text(
                text = if (isFa) "جفت‌های مستقیم موجود در سالن (تولید در نسل اول)" else "Direct 1-Generation Pair Candidates in Flock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (planningResult != null && planningResult.directMatches.isNotEmpty()) {
            items(planningResult.directMatches) { match ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${match.sireRing} (Sire) × ${match.damRing} (Dam)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", match.probabilityPercent)}% Per Egg",
                                    color = Color.White,
                                    modifier = Modifier.padding(4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target Match: ${match.matchingPhenotype}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = match.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onTestPairInSimulator(match.sireRing, match.damRing) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (isFa) "آزمایش در شبیه‌ساز" else "Test in Simulator")
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = if (isFa) "هیچ جفت مستقیمی در سالن وجود ندارد که بتواند این هدف را در یک مرحله به وجود آورد." else "No direct 1-step pairings in your flock currently carry the combined alleles for this target.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Multi-Step 2-Generation Roadmaps
        if (planningResult != null && planningResult.twoStepRoadmaps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFa) "مسیر پرورشی ۲ نسلی پیشنهادی" else "2-Generation Breeding Roadmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(planningResult.twoStepRoadmaps) { roadmap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        roadmap.steps.forEach { step ->
                            Row(verticalAlignment = Alignment.Top) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${step.generationIndex}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = step.stepTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${step.sireRing} × ${step.damRing}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = step.instructions,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: FLOCK GENETIC SEARCH & FILTER
// -------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneticSearchTab(
    allBirds: List<BirdEntity>,
    filters: GeneticSearchFilters,
    isFa: Boolean,
    onFiltersChanged: (GeneticSearchFilters) -> Unit,
    onNavigateToBird: (String) -> Unit,
    onSelectForSimulator: (String, BirdGender) -> Unit,
    onNavigateToPedigree: (String) -> Unit
) {
    var searchTxt by remember { mutableStateOf(filters.query) }

    val filtered = allBirds.filter { bird ->
        val queryMatch = filters.query.isBlank() ||
                bird.ringNumber.contains(filters.query, ignoreCase = true) ||
                (bird.name?.contains(filters.query, ignoreCase = true) == true) ||
                bird.color.contains(filters.query, ignoreCase = true) ||
                bird.mutation.contains(filters.query, ignoreCase = true)

        val seriesMatch = when (filters.baseSeries) {
            "BLUE" -> bird.color.contains("Blue", ignoreCase = true) || bird.color.contains("Cobalt", ignoreCase = true)
            "GREEN" -> bird.color.contains("Green", ignoreCase = true) || bird.color.contains("Olive", ignoreCase = true)
            else -> true
        }

        val mutationMatch = when (filters.visualMutation) {
            "ALL" -> true
            else -> bird.mutation.contains(filters.visualMutation, ignoreCase = true)
        }

        queryMatch && seriesMatch && mutationMatch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search text field
        item {
            OutlinedTextField(
                value = searchTxt,
                onValueChange = {
                    searchTxt = it
                    onFiltersChanged(filters.copy(query = it))
                },
                label = { Text(if (isFa) "جستجوی شماره حلقه، نام یا جهش..." else "Search ring, name, or mutation...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchTxt.isNotEmpty()) {
                        IconButton(onClick = {
                            searchTxt = ""
                            onFiltersChanged(filters.copy(query = ""))
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Base Series Filter Chips
        item {
            Text(
                text = if (isFa) "فیلتر بر اساس سری پایه رنگی:" else "Filter by Base Color Series:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filters.baseSeries == "ALL",
                    onClick = { onFiltersChanged(filters.copy(baseSeries = "ALL")) },
                    label = { Text(if (isFa) "همه سری‌ها" else "All Series") }
                )
                FilterChip(
                    selected = filters.baseSeries == "BLUE",
                    onClick = { onFiltersChanged(filters.copy(baseSeries = "BLUE")) },
                    label = { Text(if (isFa) "سری آبی (مغلوب)" else "Blue Series") }
                )
                FilterChip(
                    selected = filters.baseSeries == "GREEN",
                    onClick = { onFiltersChanged(filters.copy(baseSeries = "GREEN")) },
                    label = { Text(if (isFa) "سری سبز (غالب)" else "Green Series") }
                )
            }
        }

        // Visual Mutation Filter Chips
        item {
            Text(
                text = if (isFa) "فیلتر بر اساس جهش‌های ظاهری:" else "Filter by Visual Mutations:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ALL", "Spangle", "Opaline", "Ino", "Lutino", "Albino", "Pied", "Cinnamon", "Clearwing").forEach { mut ->
                    FilterChip(
                        selected = filters.visualMutation == mut,
                        onClick = { onFiltersChanged(filters.copy(visualMutation = mut)) },
                        label = { Text(mut) }
                    )
                }
            }
        }

        // Result Count Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "تعداد پرندگان یافت شده: ${filtered.size}" else "Matching Birds in Flock: ${filtered.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Results List
        items(filtered) { bird ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBird(bird.ringNumber) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sexIcon = if (bird.gender == BirdGender.MALE) Icons.Default.Male else Icons.Default.Female
                            val sexColor = if (bird.gender == BirdGender.MALE) Color(0xFF1976D2) else Color(0xFFE91E63)
                            Icon(sexIcon, contentDescription = null, tint = sexColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${bird.ringNumber} ${bird.name?.let { "($it)" } ?: ""}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = bird.variety?.displayName ?: "Standard",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${bird.color} • ${bird.mutation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToPedigree(bird.ringNumber) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(if (isFa) "شجره‌نامه" else "Pedigree")
                        }

                        Button(
                            onClick = { onSelectForSimulator(bird.ringNumber, bird.gender) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isFa) "ارسال به شبیه‌ساز" else "Add to Simulator")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 4: GENETICS DEVELOPMENTAL CALENDAR
// -------------------------------------------------------------------------
@Composable
private fun GeneticsCalendarTab(
    milestones: List<GeneticsCalendarEvent>,
    isFa: Boolean
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFa) "تقویم تکامل و بازرسی‌های ژنتیکی کلاچ‌ها" else "Genetics Developmental Milestones Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFa)
                            "زمان‌بندی مراحل کلیدی تشخیص ژنتیکی: کندلینگ تخم، رنگ چشم در تولد، رنگ کرک جوجه، حلقه‌گذاری بسته و باز شدن غلاف پرها."
                        else
                            "Scheduled milestones for phenotypic confirmation: candling, eye pigment check at emergence, down color inspection, closed banding, and feather pin eruption.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (milestones.isNotEmpty()) {
            items(milestones) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFa) event.milestoneType.titleFa else event.milestoneType.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(
                                containerColor = if (event.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (event.isCompleted) (if (isFa) "انجام شده" else "Completed") else dateFormat.format(Date(event.dueDateMillis)),
                                    color = if (event.isCompleted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${event.pairDescription} • ${event.chickRingNumber ?: "Egg #${event.eggNumber}"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = event.geneticSignificance,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFa) "هیچ رخداد تکاملی فعالی در سالن وجود ندارد" else "No active clutch milestones currently scheduled",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 5: MUTATION REFERENCE GUIDE
// -------------------------------------------------------------------------
@Composable
private fun MutationGuideTab(isFa: Boolean) {
    val sections = listOf(
        Triple(
            if (isFa) "وراثت وابسته به جنس بر روی کروموزوم Z" else "Sex-Linked Inheritance (Z Chromosome)",
            if (isFa) "اوپالین (Opaline)، اینو/لوتینو/آلبینو (Ino)، دارچینی (Cinnamon)" else "Opaline, Ino (Albino/Lutino), Cinnamon, Slate",
            if (isFa)
                "در پرندگان، نرها هموگامتیک (ZZ) و ماده‌ها هتروگامتیک (ZW) هستند. ماده‌ها فقط یک کروموزوم Z دارند، بنابراین ماده‌ها هرگز نمی‌توانند ناقل (Split) صفات وابسته به جنس باشند؛ آن‌ها یا فنوتیپ را نشان می‌دهند یا فاقد آن هستند. نرها دارای دو کروموزوم Z بوده و می‌توانند ناقل پنهان باشند."
            else
                "In birds, males are ZZ and females are ZW. Females possess only one Z chromosome, so hens CANNOT be split (carriers) for sex-linked genes; they are either visual or normal. Cocks possess two Z chromosomes and can carry hidden split mutations."
        ),
        Triple(
            if (isFa) "وراثت غالب و نیمه‌غالب اتوزومال" else "Autosomal Dominant & Incomplete Dominance",
            if (isFa) "اسپنگل (Spangle)، فاکتور تیره (Dark Factor)، ابلق استرالیایی (Dominant Pied)" else "Spangle, Dark Factors (Cobalt/Mauve/Olive), Dominant Pied, YellowFace",
            if (isFa)
                "صفات تک‌فاکتور (SF) حتی با داشتن یک الل نیز ظاهر می‌شوند. در اسپنگل، تک‌فاکتور باعث ایجاد لبه‌های مشکی مشخص روی بال می‌شود، در حالی که جفت شدن دو فاکتور (DF) تولید ملانین را متوقف کرده و پرنده تمام‌سفید یا تمام‌زرد چشم‌مشکی پدید می‌آورد."
            else
                "Single Factor (SF) expresses visibly with just one mutant allele. In Spangle, SF produces characteristic wing outlines, whereas Double Factor (DF) completely suppresses melanin to produce pure yellow or white plumage with solid black eyes."
        ),
        Triple(
            if (isFa) "وراثت مغلوب اتوزومال (Autosomal Recessive)" else "Autosomal Recessive Inheritance",
            if (isFa) "سری آبی (Blue)، بال‌روشن (Clearwing)، بال‌خاکستری (Greywing)، ابلق دانمارکی (Recessive Pied)" else "Blue series, Clearwing, Greywing, Dilute, Danish Recessive Pied, Fallow",
            if (isFa)
                "برای نمایش ظاهری، هر دو والد باید حداقل ناقل الل مغلوب باشند (۲۵٪ شانس در جفت ناقل × ناقل، یا ۵۰٪ در جفت نمایان × ناقل)."
            else
                "Requires two copies of the mutant allele (one from each parent) for visual expression. Carrier x Carrier yields 25% visual, 50% split, 25% normal."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(sections) { (title, examples, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Examples: $examples",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
