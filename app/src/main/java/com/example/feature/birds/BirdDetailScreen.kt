package com.example.feature.birds

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.health.WeightGrowthTrendChart
import com.example.data.database.entity.NutritionRecordEntity
import com.example.feature.health.LogHealthAndTreatmentDialog
import com.example.feature.health.MedicalDisclaimerBanner
import com.example.feature.nutrition.LogNutritionDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MediaDocumentEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.feature.birds.qr.BirdQrCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BirdDetailScreen(
    viewModel: BirdDetailViewModel,
    onBack: () -> Unit,
    onSelectOtherBird: (ringNumber: String) -> Unit,
    onNavigateToCage: (cageCode: String) -> Unit = {},
    onNavigateToPair: (pairId: Long) -> Unit = {},
    onNavigateToPedigree: (ringNumber: String) -> Unit = {},
    onNavigateToGenetics: (ringNumber: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN
    val birdWithDetails by viewModel.birdWithDetails.collectAsStateWithLifecycle()
    val parents by viewModel.parents.collectAsStateWithLifecycle()
    val children by viewModel.children.collectAsStateWithLifecycle()
    val pairs by viewModel.pairs.collectAsStateWithLifecycle()
    val genetics by viewModel.genetics.collectAsStateWithLifecycle()
    val pedigree by viewModel.pedigree.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val incomeRecords by viewModel.incomeRecords.collectAsStateWithLifecycle()
    val nutritionRecords by viewModel.nutritionRecords.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var isDeleteConfirmOpen by remember { mutableStateOf(false) }
    var isAddWeightOpen by remember { mutableStateOf(false) }
    var isAddHealthOpen by remember { mutableStateOf(false) }
    var isAddHealthFullOpen by remember { mutableStateOf(false) }
    var isAddNutritionOpen by remember { mutableStateOf(false) }
    var isAddPhotoOpen by remember { mutableStateOf(false) }
    var isEditPedigreeOpen by remember { mutableStateOf(false) }
    var isEditGeneticsOpen by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Overview" to Icons.Filled.Pets,
        "Photos" to Icons.Filled.Photo,
        "Weights" to Icons.Filled.FitnessCenter,
        "Health" to Icons.Filled.MedicalServices,
        "Nutrition" to Icons.Filled.Restaurant,
        "Breeding" to Icons.Filled.Egg,
        "Genetics" to Icons.Filled.Favorite,
        "Pedigree" to Icons.Filled.Home,
        "Competitions" to Icons.Filled.EmojiEvents,
        "Cage" to Icons.Filled.Home,
        "Finances" to Icons.Filled.MonetizationOn,
        "QR Code" to Icons.Filled.QrCode2,
        "PDF Export" to Icons.Filled.PictureAsPdf
    )

    if (birdWithDetails == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("bird_detail_loading"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val bird = birdWithDetails!!.bird

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("bird_detail_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("bird_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = stringResource(R.string.bird_detail_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // PDF Action
            IconButton(
                onClick = {
                    val file = viewModel.exportAndSharePdf(context)
                    if (file != null) {
                        Toast.makeText(context, context.getString(R.string.bird_pdf_generated), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.bird_pdf_failed), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("bird_detail_export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Edit Action
            IconButton(
                onClick = { isEditDialogOpen = true },
                modifier = Modifier.testTag("bird_detail_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Bird"
                )
            }

            // Delete Action
            IconButton(
                onClick = { isDeleteConfirmOpen = true },
                modifier = Modifier.testTag("bird_detail_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Bird",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Header Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("bird_detail_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bird Avatar Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            when (bird.gender) {
                                BirdGender.MALE -> Color(0xFF1976D2).copy(alpha = 0.2f)
                                BirdGender.FEMALE -> Color(0xFFC2185B).copy(alpha = 0.2f)
                                BirdGender.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            2.dp,
                            when (bird.gender) {
                                BirdGender.MALE -> Color(0xFF1976D2)
                                BirdGender.FEMALE -> Color(0xFFC2185B)
                                BirdGender.UNKNOWN -> MaterialTheme.colorScheme.outline
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (bird.gender) {
                            BirdGender.MALE -> Icons.Filled.Male
                            BirdGender.FEMALE -> Icons.Filled.Female
                            BirdGender.UNKNOWN -> Icons.Filled.Pets
                        },
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = when (bird.gender) {
                            BirdGender.MALE -> Color(0xFF1976D2)
                            BirdGender.FEMALE -> Color(0xFFC2185B)
                            BirdGender.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bird.ringNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (!bird.name.isNullOrBlank()) {
                        Text(
                            text = bird.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = bird.variety.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = bird.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // One-Click "Full Dossier + Pedigree" Export Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFa) "پرونده جامع + شجره‌نامه (PDF ۲ صفحه‌ای)" else "Full Dossier + Pedigree (2-Page PDF)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isFa) "شامل عکس، اطلاعات کامل، ژنتیک، شجره ۳ نسل، سوابق تکثیر، جوجه‌ها، وزن، سلامت، داروها، مسابقات و QR" else "Includes photo, genetics, 3-gen pedigree, breeding, chicks, weights, health, meds, comps & QR",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val file = viewModel.exportFullDossierPdf(context, isPersian = isFa)
                            if (file != null) {
                                Toast.makeText(context, if (isFa) "پرونده جامع PDF با موفقیت صادر شد" else "Full Bird Dossier Generated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (isFa) "خطا در صدور پرونده جامع" else "Failed to generate dossier", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("bird_detail_one_click_full_dossier_button")
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFa) "صدور پرونده کامل" else "Full Dossier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Horizontal Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (label, icon) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(label, fontSize = 12.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("bird_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> OverviewSection(
                    bird = bird,
                    parents = parents,
                    children = children,
                    onSelectOtherBird = onSelectOtherBird
                )
                1 -> PhotosSection(
                    photos = photos,
                    onAddPhoto = { isAddPhotoOpen = true },
                    onSetPrimary = { viewModel.setPrimaryPhoto(it) }
                )
                2 -> WeightsSection(
                    weights = birdWithDetails!!.weightHistory,
                    onAddWeight = { isAddWeightOpen = true }
                )
                3 -> HealthSection(
                    healthRecords = birdWithDetails!!.healthRecords,
                    medications = medications,
                    onAddHealth = { isAddHealthFullOpen = true }
                )
                4 -> NutritionSection(
                    records = nutritionRecords,
                    onAddNutrition = { isAddNutritionOpen = true }
                )
                5 -> BreedingSection(
                    bird = bird,
                    pairs = pairs,
                    children = children,
                    onSelectOtherBird = onSelectOtherBird,
                    onNavigateToPair = onNavigateToPair,
                    onNavigateToCage = onNavigateToCage
                )
                6 -> GeneticsSection(
                    bird = bird,
                    genetics = genetics,
                    onEdit = { isEditGeneticsOpen = true },
                    onOpenSimulator = { onNavigateToGenetics(bird.ringNumber) }
                )
                7 -> PedigreeSection(
                    bird = bird,
                    pedigree = pedigree,
                    parents = parents,
                    onEdit = { isEditPedigreeOpen = true },
                    onSelectOtherBird = onSelectOtherBird,
                    onOpenPedigreeTree = { onNavigateToPedigree(bird.ringNumber) }
                )
                8 -> CompetitionsSection(scores = birdWithDetails!!.competitionScores)
                9 -> CageSection(
                    bird = bird,
                    cage = birdWithDetails!!.cage,
                    onNavigateToCage = onNavigateToCage
                )
                10 -> FinancesSection(incomeRecords = incomeRecords)
                11 -> BirdQrCard(
                    bird = bird,
                    onShare = {
                        val file = viewModel.exportAndSharePdf(context)
                        if (file != null) {
                            Toast.makeText(context, context.getString(R.string.bird_pdf_generated), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                12 -> PdfExportSection(
                    bird = bird,
                    onExport = {
                        val file = viewModel.exportAndSharePdf(context)
                        if (file != null) {
                            Toast.makeText(context, context.getString(R.string.bird_pdf_generated), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.bird_pdf_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Dialogs
    if (isEditDialogOpen) {
        BirdFormDialog(
            initialBird = bird,
            onDismiss = { isEditDialogOpen = false },
            onSave = { updated ->
                viewModel.saveBird(updated)
                isEditDialogOpen = false
                Toast.makeText(context, context.getString(R.string.bird_save_success), Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (isDeleteConfirmOpen) {
        val securityManager = remember {
            com.example.core.security.SecurityManager.getInstance(
                context,
                com.example.data.database.AppDatabase.getInstance(context).auditLogDao()
            )
        }
        com.example.core.security.ui.SensitiveActionDialog(
            securityManager = securityManager,
            actionTitle = if (isFa) "حذف پرونده پرنده" else "Delete Bird Record",
            actionMessage = if (isFa)
                "آیا از حذف پرنده با پلاک ${bird.ringNumber} مطمئن هستید؟ این عملیات دائمی است و سوابق این پرنده را حذف خواهد کرد."
            else
                "Are you sure you want to permanently delete bird ${bird.ringNumber}? All associated weights and records will be removed.",
            currentLanguage = currentLang,
            onDismiss = { isDeleteConfirmOpen = false },
            onConfirm = {
                isDeleteConfirmOpen = false
                viewModel.deleteBird(onDeleted = onBack)
            }
        )
    }

    if (isAddWeightOpen) {
        AddWeightDialog(
            onDismiss = { isAddWeightOpen = false },
            onSave = { grams, score, condition, notes ->
                viewModel.addWeight(grams, score, condition, notes)
                isAddWeightOpen = false
            }
        )
    }

    if (isAddHealthOpen) {
        AddHealthRecordDialog(
            onDismiss = { isAddHealthOpen = false },
            onSave = { issue, symptoms, diagnosis, vet, cost ->
                viewModel.addHealthRecord(issue, symptoms, diagnosis, vet, cost)
                isAddHealthOpen = false
            }
        )
    }

    if (isAddHealthFullOpen) {
        LogHealthAndTreatmentDialog(
            initialBirdRing = bird.ringNumber,
            onDismiss = { isAddHealthFullOpen = false },
            onSave = { record, scheduleReminders ->
                viewModel.addFullHealthRecord(record, scheduleReminders)
                isAddHealthFullOpen = false
            }
        )
    }

    if (isAddNutritionOpen) {
        LogNutritionDialog(
            initialBirdRing = bird.ringNumber,
            onDismiss = { isAddNutritionOpen = false },
            onSave = { record ->
                viewModel.addNutritionRecord(record)
                isAddNutritionOpen = false
            }
        )
    }

    if (isAddPhotoOpen) {
        AddPhotoDialog(
            onDismiss = { isAddPhotoOpen = false },
            onSave = { title, uri, isPrimary ->
                viewModel.addPhoto(title, uri, isPrimary)
                isAddPhotoOpen = false
            }
        )
    }

    if (isEditPedigreeOpen) {
        EditPedigreeDialog(
            current = pedigree,
            birdRing = bird.ringNumber,
            onDismiss = { isEditPedigreeOpen = false },
            onSave = { updated ->
                viewModel.savePedigree(updated)
                isEditPedigreeOpen = false
            }
        )
    }

    if (isEditGeneticsOpen) {
        EditGeneticsDialog(
            current = genetics,
            birdRing = bird.ringNumber,
            onDismiss = { isEditGeneticsOpen = false },
            onSave = { updated ->
                viewModel.saveGenetics(updated)
                isEditGeneticsOpen = false
            }
        )
    }
}

// -------------------------------------------------------------
// Sub-Sections
// -------------------------------------------------------------

@Composable
private fun OverviewSection(
    bird: BirdEntity,
    parents: com.example.data.database.relation.BirdWithParents?,
    children: com.example.data.database.relation.BirdWithChildren?,
    onSelectOtherBird: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val birthDateStr = bird.birthDate?.let { dateFormat.format(Date(it)) } ?: "Not Recorded"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CORE SPECIFICATIONS / مشخصات پایه",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    DetailRow(label = "Unique Ring Number", value = bird.ringNumber, isMonospace = true)
                    DetailRow(label = "Sex / جنسیت", value = bird.gender.name)
                    DetailRow(label = "Variety / نژاد", value = bird.variety.name.replace("_", " "))
                    DetailRow(label = "Mutation / جهش", value = bird.mutation)
                    DetailRow(label = "Base Color / رنگ پایه", value = bird.color)
                    DetailRow(label = "Birth Date / تاریخ تولد", value = birthDateStr)
                    DetailRow(label = "Place of Birth / محل تولد", value = bird.placeOfBirth ?: "Aviary Facility")
                    DetailRow(label = "Generation / نسل", value = bird.generation ?: "F1")
                    DetailRow(label = "Current Cage / قفس", value = bird.cageCode ?: "Unassigned")
                    DetailRow(label = "Status / وضعیت", value = bird.status.name)
                }
            }
        }

        // Parents Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PARENTS / والدین مستقیم",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Father
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val ring = parents?.father?.ringNumber ?: bird.fatherRing
                                    if (!ring.isNullOrBlank()) onSelectOtherBird(ring)
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SIRE (Father / پدر)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parents?.father?.ringNumber ?: bird.fatherRing ?: "Not Recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = parents?.father?.mutation ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Mother
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val ring = parents?.mother?.ringNumber ?: bird.motherRing
                                    if (!ring.isNullOrBlank()) onSelectOtherBird(ring)
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("DAM (Mother / مادر)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC2185B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parents?.mother?.ringNumber ?: bird.motherRing ?: "Not Recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = parents?.mother?.mutation ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Children Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val offspring = children?.allChildren ?: emptyList()
                    Text(
                        text = "CHILDREN / فرزندان و نوچه‌ها (${offspring.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    if (offspring.isEmpty()) {
                        Text(
                            text = "No direct offspring recorded for this bird yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(offspring) { child ->
                                OutlinedCard(
                                    modifier = Modifier.clickable { onSelectOtherBird(child.ringNumber) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = child.ringNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${child.gender.name} • ${child.mutation}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notes
        if (!bird.notes.isNullOrBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NOTES / یادداشت‌ها",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = bird.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotosSection(
    photos: List<MediaDocumentEntity>,
    onAddPhoto: () -> Unit,
    onSetPrimary: (MediaDocumentEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photo Gallery / گالری تصاویر (${photos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddPhoto,
                modifier = Modifier.testTag("add_photo_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.bird_add_photo))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No photos added to this bird's gallery yet.\nTap 'Add Photo' to store images.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(photos) { photo ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Photo, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = photo.caption ?: photo.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = photo.filePathOrUri,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                if (photo.isPrimaryPhoto) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "Primary Photo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (!photo.isPrimaryPhoto) {
                                OutlinedButton(onClick = { onSetPrimary(photo) }) {
                                    Text("Make Primary", fontSize = 11.sp)
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
private fun WeightsSection(
    weights: List<WeightRecordEntity>,
    onAddWeight: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight History & Growth / روند وزن‌کشی (${weights.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddWeight,
                    modifier = Modifier.testTag("add_weight_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.bird_add_weight))
                }
            }
        }

        item {
            WeightGrowthTrendChart(weightRecords = weights)
        }

        if (weights.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weight records yet for this bird.\nRegular weighing ensures health monitoring.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(weights.sortedByDescending { it.recordedDate }) { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = dateFormat.format(Date(record.recordedDate)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Condition: ${record.conditionScore}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!record.notes.isNullOrBlank()) {
                                Text(
                                    text = record.notes,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "${record.weightGrams} g",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSection(
    healthRecords: List<HealthRecordEntity>,
    medications: List<com.example.data.database.entity.MedicationEntity> = emptyList(),
    onAddHealth: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            MedicalDisclaimerBanner()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health & Treatments / سوابق درمان و داروها (${healthRecords.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddHealth,
                    modifier = Modifier.testTag("add_health_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Treatment")
                }
            }
        }

        if (medications.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE MEDICATIONS / داروهای تجویز شده:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(medications) { med ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = med.medicationName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(text = "Dose: ${med.dosage} | Freq: ${med.frequency}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "End: ${dateFormat.format(Date(med.endDate))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        if (healthRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No health issues or treatments on record.\nBird is in optimal wellness.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(healthRecords.sortedByDescending { it.recordDate }) { record ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (record.isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.recordedProblem.ifBlank { record.notes ?: record.recordType },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (record.isResolved) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (record.isResolved) "Resolved" else "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (record.isResolved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!record.symptoms.isNullOrBlank()) {
                            Text(
                                text = "Symptoms: ${record.symptoms}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!record.medicationName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Medication: ${record.medicationName} (${record.dosage ?: "Std Dose"})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Frequency: ${record.frequency ?: "Daily"} | Duration: ${record.treatmentDurationDays} days",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        if (!record.supplements.isNullOrBlank()) {
                            Text(
                                text = "Supplements: ${record.supplements}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${dateFormat.format(Date(record.startDate))} ${record.endDate?.let { "- ${dateFormat.format(Date(it))}" } ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (!record.veterinarianName.isNullOrBlank()) {
                                Text(
                                    text = "Vet: ${record.veterinarianName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun NutritionSection(
    records: List<NutritionRecordEntity>,
    onAddNutrition: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrition & Feeding History / جیره غذایی (${records.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddNutrition,
                    modifier = Modifier.testTag("add_nutrition_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Feed")
                }
            }
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No individual feeding logs recorded for this bird.\nClick 'Log Feed' to record daily diets and supplements.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(records.sortedByDescending { it.recordDate }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.foodType,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = item.amount,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Consumption: ${item.consumptionRate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (item.consumptionRate) {
                                    "FINISHED_ALL", "HIGH" -> Color(0xFF2E7D32)
                                    "LOW", "REFUSED" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(text = "•", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Water: ${item.waterType}", style = MaterialTheme.typography.bodySmall)
                        }

                        if (item.supplements.isNotBlank()) {
                            Text(
                                text = "Supplements: ${item.supplements}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${dateFormat.format(Date(item.recordDate))} (${item.feedingSchedule})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (item.cost > 0) {
                                Text(
                                    text = "$%.2f".format(Locale.US, item.cost),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
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
private fun BreedingSection(
    bird: BirdEntity,
    pairs: List<PairEntity>,
    children: com.example.data.database.relation.BirdWithChildren?,
    onSelectOtherBird: (String) -> Unit,
    onNavigateToPair: (Long) -> Unit = {},
    onNavigateToCage: (String) -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Pairs", style = MaterialTheme.typography.labelSmall)
                        Text(text = "${pairs.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Offspring", style = MaterialTheme.typography.labelSmall)
                        Text(text = "${children?.allChildren?.size ?: 0}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val activeCount = pairs.count { it.isActive }
                        Text(text = "Active Pair", style = MaterialTheme.typography.labelSmall)
                        Text(text = if (activeCount > 0) "YES" else "NO", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (activeCount > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Text(
                text = "Breeding Pairs History / تاریخچه جفت‌گیری‌ها (${pairs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (pairs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This bird has not been paired yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(pairs) { pair ->
                val isSire = pair.maleRingNumber == bird.ringNumber
                val mateRing = if (isSire) pair.femaleRingNumber else pair.maleRingNumber
                val mateRole = if (isSire) "Dam (Hen / مادر)" else "Sire (Cock / پدر)"

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pair.isActive) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable { onNavigateToPair(pair.id) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pair #${pair.id}: ${pair.maleRingNumber} x ${pair.femaleRingNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (pair.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                            ) {
                                Text(
                                    text = if (pair.isActive) "ACTIVE PAIR" else pair.status,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Mate Info & Direct Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onSelectOtherBird(mateRing) }
                            ) {
                                Text(text = "$mateRole: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = mateRing,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            pair.cageCode?.let { code ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { onNavigateToCage(code) }
                                ) {
                                    Text(
                                        text = "Cage: $code",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Paired on: ${dateFormat.format(Date(pair.pairingDate))}${pair.endDate?.let { " ~ " + dateFormat.format(Date(it)) } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!pair.results.isNullOrBlank()) {
                            Text(
                                text = "Results: ${pair.results}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneticsSection(
    bird: BirdEntity,
    genetics: com.example.data.database.entity.BirdGeneticsEntity?,
    onEdit: () -> Unit,
    onOpenSimulator: () -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Genetics & Alleles / ویژگی‌های ژنتیکی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_genetics_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    OutlinedButton(onClick = onOpenSimulator) {
                        Icon(Icons.Filled.Biotech, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulator")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "Visual Mutations / جهش‌های ظاهری", value = genetics?.visualMutations ?: bird.mutation)
                    DetailRow(label = "Split (Carrier) / حامل جهش", value = genetics?.splitMutations?.ifBlank { "None" } ?: "None")
                    DetailRow(label = "Base Color Series / سری رنگ", value = genetics?.baseSeries ?: "BLUE/GREEN")
                    DetailRow(label = "Dark Factors (تیره)", value = "${genetics?.darkFactors ?: 0} Factors")
                    DetailRow(label = "Violet Factor (بنفش)", value = if (genetics?.violetFactor == true) "YES / دارد" else "NO")
                    DetailRow(label = "Grey Factor (خاکستری)", value = if (genetics?.greyFactor == true) "YES / دارد" else "NO")
                    DetailRow(label = "Cinnamon Factor (دارچینی)", value = if (genetics?.cinnamonFactor == true) "YES / دارد" else "NO")
                    DetailRow(label = "Ino Factor (آلبینو/لوتینو)", value = if (genetics?.inoFactor == true) "YES / دارد" else "NO")
                    DetailRow(label = "YellowFace Type (صورت زرد)", value = genetics?.yellowFaceType ?: "Standard")
                    DetailRow(label = "Piebald Type (ابلق)", value = genetics?.piebaldType ?: "NONE")
                    if (!genetics?.notes.isNullOrBlank()) {
                        DetailRow(label = "Genetics Notes", value = genetics!!.notes!!)
                    }
                }
            }
        }
    }
}

@Composable
private fun PedigreeSection(
    bird: BirdEntity,
    pedigree: com.example.data.database.entity.PedigreeRecordEntity?,
    parents: com.example.data.database.relation.BirdWithParents?,
    onEdit: () -> Unit,
    onSelectOtherBird: (String) -> Unit,
    onOpenPedigreeTree: () -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedigree Tree / شجره‌نامه و نسب ۳ نسل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_pedigree_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    OutlinedButton(onClick = onOpenPedigreeTree) {
                        Icon(Icons.Filled.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Full Tree")
                    }
                }
            }
        }

        // Breeder & Inbreeding info
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Breeder: ${pedigree?.breederName ?: "Aviary Facility"}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Breeder Code: ${pedigree?.breederCode ?: "IR-AV-01"}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "COI (Inbreeding):", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format(Locale.US, "%.2f%%", (pedigree?.inbreedingCoefficient ?: 0.0) * 100),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3-Generation Tree View
        item {
            Text(text = "GEN 1 & 2: PARENTS & GRANDPARENTS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Paternal Line
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Paternal Line (پدر و اجداد پدری)", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    PedigreeNode(
                        role = "Sire (Father / پدر)",
                        ring = pedigree?.sireRing ?: bird.fatherRing ?: "Not Recorded",
                        onClick = { ring -> if (ring != "Not Recorded") onSelectOtherBird(ring) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PedigreeNode(
                            role = "Paternal Grandsire (پدربزرگ)",
                            ring = pedigree?.paternalGrandsire ?: "-",
                            modifier = Modifier.weight(1f),
                            onClick = { ring -> if (ring != "-") onSelectOtherBird(ring) }
                        )
                        PedigreeNode(
                            role = "Paternal Granddam (مادربزرگ)",
                            ring = pedigree?.paternalGranddam ?: "-",
                            modifier = Modifier.weight(1f),
                            onClick = { ring -> if (ring != "-") onSelectOtherBird(ring) }
                        )
                    }
                }
            }
        }

        // Maternal Line
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Maternal Line (مادر و اجداد مادری)", fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                    PedigreeNode(
                        role = "Dam (Mother / مادر)",
                        ring = pedigree?.damRing ?: bird.motherRing ?: "Not Recorded",
                        onClick = { ring -> if (ring != "Not Recorded") onSelectOtherBird(ring) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PedigreeNode(
                            role = "Maternal Grandsire (پدربزرگ)",
                            ring = pedigree?.maternalGrandsire ?: "-",
                            modifier = Modifier.weight(1f),
                            onClick = { ring -> if (ring != "-") onSelectOtherBird(ring) }
                        )
                        PedigreeNode(
                            role = "Maternal Granddam (مادربزرگ)",
                            ring = pedigree?.maternalGranddam ?: "-",
                            modifier = Modifier.weight(1f),
                            onClick = { ring -> if (ring != "-") onSelectOtherBird(ring) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PedigreeNode(
    role: String,
    ring: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {}
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick(ring) },
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = ring,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun CompetitionsSection(scores: List<com.example.data.database.entity.CompetitionScoreEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Competition & Show History / سوابق و کارنامه مسابقات (${scores.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (scores.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "This bird has not entered any show competitions yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(scores) { score ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = score.showClass,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (score.isDigitallyConfirmed) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "Official Certified",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (!score.awardTitle.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🏆 ${score.awardTitle.replace("_", " ")}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "%.1f pts".format(java.util.Locale.US, score.totalScore),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (!score.judgeName.isNullOrBlank() || !score.judgingCode.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Judge: ${score.judgeName ?: "Official"} ${score.judgingCode?.let { "($it)" } ?: ""} • Cage: ${score.cageNumberInShow ?: "-"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (!score.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Judge Comment: \"${score.notes}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!score.visualAiSuggestion.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "AI Visual Analysis: ${score.visualAiSuggestion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
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
private fun CageSection(
    bird: BirdEntity,
    cage: com.example.data.database.entity.CageEntity?,
    onNavigateToCage: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cage & Housing History / وضعیت قفس و جایگاه",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(label = "Assigned Cage Code", value = bird.cageCode ?: "Not Assigned", isMonospace = true)
                DetailRow(label = "Cage Type", value = cage?.type?.name?.replace("_", " ") ?: "Standard Aviary Cage")
                DetailRow(label = "Room / Section", value = cage?.location ?: "Breeding Block A")
                DetailRow(label = "Capacity", value = "${cage?.capacity ?: 2} birds")
                DetailRow(label = "Sanitization Status", value = if (cage?.isClean == true) "Clean & Sanitized" else "Scheduled for cleaning")

                bird.cageCode?.let { code ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onNavigateToCage(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Cage Record / مشاهده مشخصات کامل قفس")
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancesSection(incomeRecords: List<com.example.data.database.entity.IncomeEntity>) {
    val totalIncome = incomeRecords.sumOf { it.amount }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total Financial Earnings", style = MaterialTheme.typography.labelSmall)
                        Text(text = "$${String.format(Locale.US, "%.2f", totalIncome)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = "${incomeRecords.size} Transactions", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text(
                text = "Transaction Records / تراکنش‌های مالی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (incomeRecords.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No sales or prize revenues associated with this bird yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(incomeRecords) { inc ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = inc.title, fontWeight = FontWeight.Bold)
                            Text(text = "${inc.category} • ${inc.notes ?: ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(text = "+$${inc.amount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfExportSection(
    bird: BirdEntity,
    onExport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.testTag("pdf_export_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Official Pedigree Certificate Export",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "صدور شناسنامه رسمی و شجره‌نامه معتبر سالن به صورت فایل استاندارد PDF شامل مشخصات کامل، حلقه، اجداد ۳ نسل، وزن‌ها و کد اعتبارسنجی.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth().testTag("export_pdf_button")
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Share Official PDF Certificate")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
