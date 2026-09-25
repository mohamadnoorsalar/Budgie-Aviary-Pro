package com.example.feature.competitions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.competition.CompetitionJudgingHelper
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.JudgeEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompetitionsScreen(
    viewModel: CompetitionsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreateCompDialog by remember { mutableStateOf(false) }
    var showAddJudgeDialog by remember { mutableStateOf(false) }
    var showEditCriteriaDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var selectedScoreForDetails by remember { mutableStateOf<CompetitionScoreEntity?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Competitions/Overview, 1: Active Show Scores, 2: Judges & Assignments, 3: WBO Standards & Rules

    val activeComp = uiState.selectedCompetition

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("competitions_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.selectedCompetitionId != null && selectedTab == 1) {
                        showScoreDialog = true
                    } else if (selectedTab == 2) {
                        showAddJudgeDialog = true
                    } else {
                        showCreateCompDialog = true
                    }
                },
                modifier = Modifier.testTag("competitions_fab")
            ) {
                Icon(
                    imageVector = when {
                        uiState.selectedCompetitionId != null && selectedTab == 1 -> Icons.Filled.Gavel
                        selectedTab == 2 -> Icons.Filled.Person
                        else -> Icons.Filled.Add
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
            // Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isFa) "مدیریت مسابقات و داوری رسمی" else "Competitions & Professional Judging",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFa)
                                        "${uiState.competitions.size} مسابقه ثبت‌شده • ${uiState.judges.size} داور معتبر"
                                    else
                                        "${uiState.competitions.size} Competitions • ${uiState.judges.size} Registered Judges",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            if (activeComp != null) {
                                IconButton(
                                    onClick = { showReportDialog = true },
                                    modifier = Modifier.testTag("btn_comp_report")
                                ) {
                                    Icon(Icons.Filled.Assessment, contentDescription = "Show Report", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { showEditCriteriaDialog = true },
                                    modifier = Modifier.testTag("btn_comp_settings")
                                ) {
                                    Icon(Icons.Filled.Tune, contentDescription = "Criteria", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            IconButton(onClick = { showCreateCompDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = "New Competition")
                            }
                        }
                    }

                    // Active competition banner if selected
                    if (activeComp != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeComp.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = activeComp.showStandard,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${activeComp.location} • ${activeComp.organizingClub}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                TextButton(onClick = { viewModel.selectCompetition(null) }) {
                                    Text("تغییر / Change", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Tab Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isFa) "رویدادها (${uiState.competitions.size})" else "Events (${uiState.competitions.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isFa) "نتایج و داوری (${uiState.currentScores.size})" else "Judging & Scores (${uiState.currentScores.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (isFa) "هیئت داوران (${uiState.judges.size})" else "Judges Panel (${uiState.judges.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(if (isFa) "استاندارد WBO" else "WBO Matrix") }
                )
            }

            // Tab Body Content
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                when (selectedTab) {
                    0 -> CompetitionsListView(
                        competitions = uiState.competitions,
                        selectedId = uiState.selectedCompetitionId,
                        onSelect = { id ->
                            viewModel.selectCompetition(id)
                            selectedTab = 1
                        },
                        onDelete = { id -> viewModel.deleteCompetition(id) },
                        onCreateNew = { showCreateCompDialog = true },
                        isFa = isFa
                    )
                    1 -> JudgingScoresView(
                        competition = activeComp,
                        scores = uiState.currentScores,
                        criteria = uiState.currentCriteria,
                        selectedClassFilter = uiState.selectedClassFilter,
                        onClassFilterChange = { viewModel.setClassFilter(it) },
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onAddScore = { showScoreDialog = true },
                        onSelectScore = { selectedScoreForDetails = it },
                        onDeleteScore = { id -> viewModel.deleteScore(id) },
                        onOpenCriteria = { showEditCriteriaDialog = true },
                        onOpenReport = { showReportDialog = true },
                        isFa = isFa
                    )
                    2 -> JudgesPanelView(
                        judges = uiState.judges,
                        onAddJudge = { showAddJudgeDialog = true },
                        onDeleteJudge = { j -> viewModel.deleteJudge(j) },
                        isFa = isFa
                    )
                    3 -> WboStandardMatrixView(isFa = isFa)
                }
            }
        }
    }

    // Dialogs
    if (showCreateCompDialog) {
        CreateCompetitionDialog(
            onDismiss = { showCreateCompDialog = false },
            onSave = { title, loc, club, date, std, cats, codes, notes ->
                viewModel.createCompetition(
                    title = title,
                    location = loc,
                    organizingClub = club,
                    eventDate = date,
                    showStandard = std,
                    categories = cats,
                    judgingCodes = codes,
                    notes = notes
                )
                showCreateCompDialog = false
                selectedTab = 1
            }
        )
    }

    if (showAddJudgeDialog) {
        AddJudgeDialog(
            onDismiss = { showAddJudgeDialog = false },
            onSave = { name, cert, aff, country, contact, codes ->
                viewModel.saveJudge(name, cert, aff, country, contact, codes)
                showAddJudgeDialog = false
            }
        )
    }

    if (showEditCriteriaDialog && activeComp != null) {
        EditCriteriaDialog(
            competition = activeComp,
            currentCriteria = uiState.currentCriteria,
            onDismiss = { showEditCriteriaDialog = false },
            onSave = { criteria ->
                viewModel.saveCriteria(activeComp.id, criteria)
                showEditCriteriaDialog = false
            }
        )
    }

    if (showScoreDialog && activeComp != null) {
        OfficialJudgingScoringDialog(
            competition = activeComp,
            criteriaList = if (uiState.currentCriteria.isEmpty()) {
                when (activeComp.showStandard) {
                    "NATIONAL" -> CompetitionJudgingHelper.createNationalStandardCriteria(activeComp.id)
                    else -> CompetitionJudgingHelper.createWboStandardCriteria(activeComp.id)
                }
            } else uiState.currentCriteria,
            judges = uiState.judges,
            registeredBirds = uiState.birds,
            onDismiss = { showScoreDialog = false },
            onSaveScore = { birdRing, exhibitor, judgeId, judgeName, code, sClass, cageNo, scMap, award, comments, photo, confirmed, sig ->
                viewModel.recordOfficialScore(
                    competitionId = activeComp.id,
                    birdRingNumber = birdRing,
                    participantName = exhibitor,
                    judgeId = judgeId,
                    judgeName = judgeName,
                    judgingCode = code,
                    showClass = sClass,
                    cageNumberInShow = cageNo,
                    criteriaScores = scMap,
                    criteriaList = if (uiState.currentCriteria.isEmpty()) {
                        when (activeComp.showStandard) {
                            "NATIONAL" -> CompetitionJudgingHelper.createNationalStandardCriteria(activeComp.id)
                            else -> CompetitionJudgingHelper.createWboStandardCriteria(activeComp.id)
                        }
                    } else uiState.currentCriteria,
                    awardTitleOverride = award,
                    notes = comments,
                    photoUri = photo,
                    isDigitallyConfirmed = confirmed,
                    judgeSignature = sig
                )
                showScoreDialog = false
            }
        )
    }

    // Score Detail Dialog
    if (selectedScoreForDetails != null) {
        ScoreDetailDialog(
            score = selectedScoreForDetails!!,
            criteria = uiState.currentCriteria,
            onDismiss = { selectedScoreForDetails = null },
            onDelete = {
                viewModel.deleteScore(selectedScoreForDetails!!.id)
                selectedScoreForDetails = null
            },
            isFa = isFa
        )
    }

    // Full Report Dialog
    if (showReportDialog && activeComp != null) {
        CompetitionReportDialog(
            competition = activeComp,
            scores = uiState.currentScores,
            judges = uiState.judges,
            criteria = uiState.currentCriteria,
            onDismiss = { showReportDialog = false },
            isFa = isFa
        )
    }
}

@Composable
private fun CompetitionsListView(
    competitions: List<CompetitionEntity>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCreateNew: () -> Unit,
    isFa: Boolean
) {
    if (competitions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = if (isFa) "هیچ مسابقه‌ای هنوز تعریف نشده است" else "No competitions registered yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isFa)
                        "می‌توانید مسابقه جدیدی بر اساس استانداردهای WBO، ملی یا معیارهای دلخواه بسازید."
                    else
                        "Create a new show based on WBO, National, or custom judging criteria standards.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onCreateNew,
                    modifier = Modifier.testTag("btn_create_first_comp")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFa) "ایجاد مسابقه جدید" else "Create Competition")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(competitions) { comp ->
                val isSelected = comp.id == selectedId
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val dateStr = dateFormat.format(Date(comp.eventDate))
                val categories = CompetitionJudgingHelper.parseJsonList(comp.categoriesJson)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(comp.id) }
                        .testTag("comp_card_${comp.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comp.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = comp.showStandard,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = comp.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "کلاس‌ها: ${categories.take(3).joinToString(", ")}${if (categories.size > 3) " +${categories.size - 3}" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "باشگاه: ${comp.organizingClub}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row {
                                IconButton(
                                    onClick = { onDelete(comp.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                                Button(
                                    onClick = { onSelect(comp.id) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isFa) "ورود به مسابقه" else "Enter Show")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JudgingScoresView(
    competition: CompetitionEntity?,
    scores: List<CompetitionScoreEntity>,
    criteria: List<CompetitionCriterionEntity>,
    selectedClassFilter: String?,
    onClassFilterChange: (String?) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddScore: () -> Unit,
    onSelectScore: (CompetitionScoreEntity) -> Unit,
    onDeleteScore: (String) -> Unit,
    onOpenCriteria: () -> Unit,
    onOpenReport: () -> Unit,
    isFa: Boolean
) {
    if (competition == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isFa) "لطفاً ابتدا یک مسابقه را از برگه رویدادها انتخاب کنید." else "Please select a competition from the Events tab.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val categories = remember(competition.categoriesJson) {
        CompetitionJudgingHelper.parseJsonList(competition.categoriesJson)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Search and Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(if (isFa) "جستجوی پلاک، شرکت‌کننده، داور…" else "Search ring, exhibitor, judge…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f).testTag("search_scores_input"),
                    singleLine = true
                )
                OutlinedButton(onClick = onOpenCriteria) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "معیارها" else "Criteria")
                }
            }
        }

        // Category Filter Chips
        if (categories.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedClassFilter == null,
                        onClick = { onClassFilterChange(null) },
                        label = { Text("همه رده‌ها (${scores.size})") }
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedClassFilter == cat,
                            onClick = { onClassFilterChange(if (selectedClassFilter == cat) null else cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        // Stats Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${scores.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "پرندگان داوری‌شده", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val maxScore = scores.maxOfOrNull { it.totalScore } ?: 0.0
                        Text(text = "%.1f".format(Locale.US, maxScore), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "بالاترین امتیاز", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average() else 0.0
                        Text(text = "%.1f".format(Locale.US, avgScore), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "میانگین نمرات", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val confirmed = scores.count { it.isDigitallyConfirmed }
                        Text(text = "$confirmed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text(text = "تایید دیجیتال", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (scores.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Text(
                            text = if (isFa) "هنوز امتیازی برای این مسابقه ثبت نشده است" else "No official scores recorded yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = onAddScore, modifier = Modifier.testTag("btn_first_score")) {
                            Icon(Icons.Filled.Gavel, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFa) "ثبت نمره داوری جدید" else "Record Official Score")
                        }
                    }
                }
            }
        } else {
            items(scores) { score ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScore(score) }
                        .testTag("score_card_${score.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = score.birdRingNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (score.isDigitallyConfirmed) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.Verified,
                                        contentDescription = "Digitally Confirmed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "رده: ${score.showClass} • ${score.participantName ?: "سالن پرورشی"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!score.judgeName.isNullOrBlank()) {
                                Text(
                                    text = "داور: ${score.judgeName} ${score.judgingCode?.let { "($it)" } ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (!score.awardTitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        score.awardTitle.contains("BEST") -> MaterialTheme.colorScheme.primaryContainer
                                        score.awardTitle.contains("FIRST") -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = score.awardTitle.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.1f pts".format(Locale.US, score.totalScore),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { onDeleteScore(score.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JudgesPanelView(
    judges: List<JudgeEntity>,
    onAddJudge: () -> Unit,
    onDeleteJudge: (JudgeEntity) -> Unit,
    isFa: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFa) "هیئت داوران و کدهای قضاوت" else "Panel of Judges & Assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onAddJudge, modifier = Modifier.testTag("btn_register_judge")) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isFa) "معرفی داور" else "Add Judge")
            }
        }

        if (judges.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isFa) "هیچ داوری هنوز ثبت نشده است." else "No judges registered yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(judges) { judge ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = judge.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "گواهینامه: ${judge.certification} • ${judge.affiliation ?: "مستقل"} (${judge.country ?: "Iran"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!judge.assignedCodes.isNullOrBlank()) {
                                    Text(
                                        text = "کدهای مجاز داوری: ${judge.assignedCodes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteJudge(judge) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WboStandardMatrixView(isFa: Boolean) {
    val criteria = listOf(
        Pair(if (isFa) "فرم سر و پیشانی (Head & Brow Profile)" else "Head & Brow Profile (20 pts)", if (isFa) "پیشانی عریض و پُر با قوس ملایم تا تاج و تقارن کامل در بالای چشم‌ها" else "Wide, full frontal rise with gentle curving dome above the eye"),
        Pair(if (isFa) "ماسک و خال‌های طوق (Mask & Spots)" else "Mask & Throat Spots (15 pts)", if (isFa) "شش خال گرد بزرگ، منظم و متقارن در پایین نقاب بدون لکه اضافی" else "Six large, round, evenly spaced symmetrical throat spots on deep mask"),
        Pair(if (isFa) "ساختار بدن و طول (Body Structure & Size)" else "Body Structure & Size (25 pts)", if (isFa) "مخروطی منظم از شانه عریض تا انتهای دم، طول ایده‌آل ۲۱.۶ سانتی‌متر" else "Well-balanced taper from broad shoulder line down to tail tip"),
        Pair(if (isFa) "کیفیت و جلای پرها (Feather Quality & Bloom)" else "Feather Condition & Bloom (15 pts)", if (isFa) "پرهای فشرده، براق، سالم و تمیز، بدون بال افتاده یا پرهای لوله‌ای" else "Tight, silky feathering with deep bloom and complete tail/wings"),
        Pair(if (isFa) "رنگ و نقوش بال (Color & Markings)" else "Color & Wing Markings (10 pts)", if (isFa) "یکنواختی رنگ بدن و وضوح نقوش ملانین طبق ژنتیک جهش" else "Rich, even base color tone and distinct wing markings"),
        Pair(if (isFa) "زاویه ایستادن روی چوب (Stance & Position)" else "Stance & Perch Angle (10 pts)", if (isFa) "زاویه استاندارد ۳۰ درجه نسبت به خط عمود، تسلط و آرامش کامل" else "Ideal 30-degree posture from vertical with confident perch grip"),
        Pair(if (isFa) "پاها و ناخن‌ها (Legs & Nails)" else "Legs & Nails (5 pts)", if (isFa) "انگشتان سالم و تمیز با دو انگشت رو به جلو و دو انگشت رو به عقب" else "Clean, strong toes gripping firmly without deformities")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isFa) "ماتریس استاندارد کنفدراسیون جهانی مرغ عشق (WBO)" else "World Budgerigar Organisation (WBO) Standard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFa)
                            "سیستم امتیازدهی ۱۰۰ امتیازی استاندارد برای مرغ عشق‌های انگلیسی نمایشگاهی. این ماتریس در بخش پیکربندی مسابقه به صورت کامل قابل تغییر است."
                        else
                            "Standard 100-point evaluation matrix for English show budgerigars. Can be completely customized per competition.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        items(criteria) { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ScoreDetailDialog(
    score: CompetitionScoreEntity,
    criteria: List<CompetitionCriterionEntity>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    isFa: Boolean
) {
    val scoreMap = remember(score.criteriaScoresJson) {
        CompetitionJudgingHelper.parseCriteriaScores(score.criteriaScoresJson)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "برگه داوری: ${score.birdRingNumber}", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "%.1f pts".format(Locale.US, score.totalScore),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "کلاس: ${score.showClass}", fontWeight = FontWeight.SemiBold)
                if (!score.participantName.isNullOrBlank()) {
                    Text(text = "شرکت‌کننده: ${score.participantName}")
                }
                if (!score.judgeName.isNullOrBlank()) {
                    Text(text = "داور: ${score.judgeName} ${score.judgingCode?.let { "($it)" } ?: ""}")
                }
                if (!score.cageNumberInShow.isNullOrBlank()) {
                    Text(text = "شماره قفس نمایش: ${score.cageNumberInShow}")
                }
                if (!score.awardTitle.isNullOrBlank()) {
                    Text(text = "رتبه و عنوان: ${score.awardTitle.replace("_", " ")}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text(text = "ریز نمرات معیارهای ارزیابی:", fontWeight = FontWeight.Bold)

                if (criteria.isNotEmpty()) {
                    criteria.forEach { criterion ->
                        val awarded = scoreMap[criterion.id]
                        if (awarded != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = criterion.name, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "%.1f / %.1f pts".format(Locale.US, awarded, criterion.maxScore),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (!score.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "یادداشت و نظر کارشناسی داور:", fontWeight = FontWeight.Bold)
                    Text(text = score.notes, style = MaterialTheme.typography.bodySmall)
                }

                if (!score.visualAiSuggestion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = score.visualAiSuggestion, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (score.isDigitallyConfirmed) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تایید دیجیتال رسمی توسط داور: ${score.confirmedByJudgeSignature ?: "Judge Certified"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن / Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("حذف / Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun CompetitionReportDialog(
    competition: CompetitionEntity,
    scores: List<CompetitionScoreEntity>,
    judges: List<JudgeEntity>,
    criteria: List<CompetitionCriterionEntity>,
    onDismiss: () -> Unit,
    isFa: Boolean
) {
    val categories = remember(competition.categoriesJson) {
        CompetitionJudgingHelper.parseJsonList(competition.categoriesJson)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("گزارش جامع مسابقه و کلاس‌ها / Competition Official Report", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${competition.title} (${competition.showStandard})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "مکان: ${competition.location} • برگزارکننده: ${competition.organizingClub}")

                Divider()

                Text(
                    text = "قهرمان مسابقه (Best in Show):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val bestInShow = scores.maxByOrNull { it.totalScore }
                if (bestInShow != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "پلاک: ${bestInShow.birdRingNumber}", fontWeight = FontWeight.Bold)
                            Text(text = "امتیاز: ${bestInShow.totalScore} pts • رده: ${bestInShow.showClass}")
                            if (!bestInShow.participantName.isNullOrBlank()) {
                                Text(text = "پرورش‌دهنده: ${bestInShow.participantName}")
                            }
                        }
                    }
                } else {
                    Text(text = "هنوز پرنده‌ای قضاوت نشده است.")
                }

                Divider()

                Text(
                    text = "رده‌بندی به تفکیک کلاس‌ها (Class Standings):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                val classGroups = scores.groupBy { it.showClass }
                if (classGroups.isEmpty()) {
                    Text(text = "رکوردی ثبت نشده است.", style = MaterialTheme.typography.bodySmall)
                } else {
                    classGroups.forEach { (showClass, classScores) ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "کلاس: $showClass (${classScores.size} شرکت‌کننده)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                classScores.sortedByDescending { it.totalScore }.take(3).forEachIndexed { idx, sc ->
                                    val rank = when (idx) {
                                        0 -> "🥇 مقام اول"
                                        1 -> "🥈 مقام دوم"
                                        2 -> "🥉 مقام سوم"
                                        else -> "مقام ${idx + 1}"
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "$rank: ${sc.birdRingNumber} (${sc.participantName ?: "سالن"})", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "${sc.totalScore} pts", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("تایید و بستن / Done")
            }
        }
    )
}
