package com.example.feature.birds

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.core.ai.BirdPhotoAnalyzer
import com.example.core.ai.BirdVisualSuggestion
import com.example.core.ai.GeminiClient
import com.example.core.ai.SuggestionConfidence
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.data.database.entity.BirdEntity
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdFormDialog(
    initialBird: BirdEntity? = null,
    onDismiss: () -> Unit,
    onSave: (BirdEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEditing = initialBird != null

    // Main Bird Data Fields
    var ringNumber by remember { mutableStateOf(initialBird?.ringNumber ?: "") }
    var name by remember { mutableStateOf(initialBird?.name ?: "") }
    var gender by remember { mutableStateOf(initialBird?.gender ?: BirdGender.UNKNOWN) }
    var variety by remember { mutableStateOf(initialBird?.variety ?: BudgieVariety.ENGLISH_SHOW) }
    var mutation by remember { mutableStateOf(initialBird?.mutation ?: "Normal") }
    var color by remember { mutableStateOf(initialBird?.color ?: "Green") }
    var placeOfBirth by remember { mutableStateOf(initialBird?.placeOfBirth ?: "") }
    var generation by remember { mutableStateOf(initialBird?.generation ?: "F1") }
    var cageCode by remember { mutableStateOf(initialBird?.cageCode ?: "") }
    var status by remember { mutableStateOf(initialBird?.status ?: BirdStatus.ACTIVE) }
    var fatherRing by remember { mutableStateOf(initialBird?.fatherRing ?: "") }
    var motherRing by remember { mutableStateOf(initialBird?.motherRing ?: "") }
    var photoUri by remember { mutableStateOf(initialBird?.photoUri ?: "") }
    var notes by remember { mutableStateOf(initialBird?.notes ?: "") }

    var ringError by remember { mutableStateOf(false) }
    var varietyExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    // AI Vision State
    var isAnalyzingPhoto by remember { mutableStateOf(false) }
    var visualSuggestion by remember { mutableStateOf<BirdVisualSuggestion?>(null) }
    var aiErrorMessage by remember { mutableStateOf<String?>(null) }
    var showSuggestionPanel by remember { mutableStateOf(false) }

    // Flags tracking whether user has explicitly confirmed/applied suggestions
    var colorConfirmedByUser by remember { mutableStateOf(false) }
    var patternConfirmedByUser by remember { mutableStateOf(false) }
    var varietyConfirmedByUser by remember { mutableStateOf(false) }

    // Camera Capture Uri setup
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            photoUri = tempCameraUri.toString()
            // Automatically trigger AI analysis proposal
            analyzePhotoWithAi(context, tempCameraUri!!, coroutineScope) { analyzing, suggestion, error ->
                isAnalyzingPhoto = analyzing
                visualSuggestion = suggestion
                aiErrorMessage = error
                if (suggestion != null) showSuggestionPanel = true
            }
        }
    }

    // Photo Gallery Picker (Photo Picker zero-permission contract)
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
            analyzePhotoWithAi(context, uri, coroutineScope) { analyzing, suggestion, error ->
                isAnalyzingPhoto = analyzing
                visualSuggestion = suggestion
                aiErrorMessage = error
                if (suggestion != null) showSuggestionPanel = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("bird_form_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.bird_edit_title) else stringResource(R.string.action_add_bird),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Vision Assist",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Photo Acquisition & AI Analysis Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Bird Photo & AI Visual Suggestion / عکس و پیشنهاد هوشمند",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عکس بگیرید یا انتخاب کنید تا ویژگی‌های ظاهری (رنگ، جهش، نژاد) توسط هوش مصنوعی پیشنهاد داده شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Camera Button
                            Button(
                                onClick = {
                                    try {
                                        val photoFile = File(context.cacheDir, "bird_photo_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                        tempCameraUri = uri
                                        takePictureLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("bird_camera_button")
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontSize = 12.sp)
                            }

                            // Gallery Picker Button
                            OutlinedButton(
                                onClick = {
                                    pickPhotoLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("bird_gallery_button")
                            ) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", fontSize = 12.sp)
                            }
                        }

                        // Preview of selected photo if available
                        if (photoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Bird Photo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Photo Selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = photoUri.takeLast(35),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                if (!isAnalyzingPhoto) {
                                    IconButton(
                                        onClick = {
                                            val uri = Uri.parse(photoUri)
                                            analyzePhotoWithAi(context, uri, coroutineScope) { analyzing, suggestion, error ->
                                                isAnalyzingPhoto = analyzing
                                                visualSuggestion = suggestion
                                                aiErrorMessage = error
                                                if (suggestion != null) showSuggestionPanel = true
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Re-analyze", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Loading Indicator
                        if (isAnalyzingPhoto) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "در حال تحلیل تصویر و استخراج ویژگی‌های فنوتیپ...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Error Notice if AI fails
                        if (aiErrorMessage != null && !isAnalyzingPhoto) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "سرویس هوش مصنوعی در دسترس نبود. ثبت دستی عادی فعال است.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Suggested Characteristics Review Card
                if (visualSuggestion != null) {
                    val sug = visualSuggestion!!
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("ai_suggestion_review_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "پیشنهادات بصری هوش مصنوعی (بازبینی)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "پیشنهادات تا قبل از تایید دستی شما رسمی نمی‌شوند و داده‌های کاربر را به شکل خودکار تغییر نمی‌دهند.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 1. Suggested Color
                            if (!sug.suggestedColor.isNullOrBlank()) {
                                SuggestionItemRow(
                                    label = "رنگ پیشنهادی / Color:",
                                    value = sug.suggestedColor,
                                    confidence = sug.colorConfidence,
                                    isConfirmed = colorConfirmedByUser,
                                    onConfirm = {
                                        color = sug.suggestedColor
                                        colorConfirmedByUser = true
                                    }
                                )
                            }

                            // 2. Suggested Pattern / Mutation
                            if (!sug.suggestedPattern.isNullOrBlank()) {
                                SuggestionItemRow(
                                    label = "طرح و جهش / Pattern:",
                                    value = sug.suggestedPattern,
                                    confidence = sug.patternConfidence,
                                    isConfirmed = patternConfirmedByUser,
                                    onConfirm = {
                                        mutation = sug.suggestedPattern
                                        patternConfirmedByUser = true
                                    }
                                )
                            }

                            // 3. Suggested Variety
                            if (sug.suggestedVariety != null) {
                                SuggestionItemRow(
                                    label = "نژاد / Variety:",
                                    value = sug.suggestedVariety.name.replace("_", " "),
                                    confidence = sug.varietyConfidence,
                                    isConfirmed = varietyConfirmedByUser,
                                    onConfirm = {
                                        variety = sug.suggestedVariety
                                        varietyConfirmedByUser = true
                                    }
                                )
                            }

                            // Button to apply all suggested visual traits
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    sug.suggestedColor?.let { color = it; colorConfirmedByUser = true }
                                    sug.suggestedPattern?.let { mutation = it; patternConfirmedByUser = true }
                                    sug.suggestedVariety?.let { variety = it; varietyConfirmedByUser = true }
                                    Toast.makeText(context, "ویژگی‌های بصری تایید و به فرم اضافه شدند", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().testTag("ai_confirm_all_suggestions_button")
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تایید همه پیشنهادات بصری / Confirm All Visual Traits", fontSize = 12.sp)
                            }

                            // Mandatory Disclaimer for uncertain traits (Sex, Age, Hidden Genetics)
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ملاحظات علمی و زیستی (غیرقطعی):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sug.sexDisclaimer,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (sug.suggestedSexGuess != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "• مشاهده سیربینی: ${sug.suggestedSexGuess} (وضعیت: نیازمند تایید دستی پرورش‌دهنده)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Standard Registration Form Fields (Always functional manual flow)
                Text(
                    text = "اطلاعات شناسنامه‌ای پرنده / Bird Identification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Ring Number
                OutlinedTextField(
                    value = ringNumber,
                    onValueChange = {
                        ringNumber = it
                        if (it.isNotBlank()) ringError = false
                    },
                    label = { Text("Ring Number / شماره حلقه *") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    enabled = !isEditing,
                    isError = ringError,
                    supportingText = if (ringError) { { Text("Ring number is required") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bird_form_ring_input")
                )

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name / نام پرنده (اختیاری)") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bird_form_name_input")
                )

                // Gender selector (Explicit human confirmation required)
                Text(
                    text = "Sex / جنسیت (تایید دستی پرورش‌دهنده):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = gender == BirdGender.MALE,
                        onClick = { gender = BirdGender.MALE },
                        label = { Text(stringResource(R.string.bird_filter_male)) },
                        leadingIcon = { Icon(Icons.Filled.Male, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = gender == BirdGender.FEMALE,
                        onClick = { gender = BirdGender.FEMALE },
                        label = { Text(stringResource(R.string.bird_filter_female)) },
                        leadingIcon = { Icon(Icons.Filled.Female, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = gender == BirdGender.UNKNOWN,
                        onClick = { gender = BirdGender.UNKNOWN },
                        label = { Text("نامشخص") },
                        leadingIcon = { Icon(Icons.Filled.QuestionMark, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Variety Dropdown
                ExposedDropdownMenuBox(
                    expanded = varietyExpanded,
                    onExpandedChange = { varietyExpanded = !varietyExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = variety.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Variety / نژاد") },
                        leadingIcon = { Icon(Icons.Filled.Pets, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = varietyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = varietyExpanded,
                        onDismissRequest = { varietyExpanded = false }
                    ) {
                        BudgieVariety.values().forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v.name.replace("_", " ")) },
                                onClick = {
                                    variety = v
                                    varietyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mutation & Color in row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mutation,
                        onValueChange = { mutation = it },
                        label = { Text("Mutation / جهش") },
                        modifier = Modifier.weight(1f).testTag("bird_form_mutation_input")
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color / رنگ") },
                        modifier = Modifier.weight(1f).testTag("bird_form_color_input")
                    )
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status / وضعیت") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        BirdStatus.values().forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Cage & Generation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cageCode,
                        onValueChange = { cageCode = it },
                        label = { Text("Cage / قفس") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = generation,
                        onValueChange = { generation = it },
                        label = { Text("Generation / نسل") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Place of Birth
                OutlinedTextField(
                    value = placeOfBirth,
                    onValueChange = { placeOfBirth = it },
                    label = { Text("Place of Birth / محل تولد") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Parents (Father Ring & Mother Ring)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fatherRing,
                        onValueChange = { fatherRing = it },
                        label = { Text("Father Ring / حلقه پدر") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = motherRing,
                        onValueChange = { motherRing = it },
                        label = { Text("Mother Ring / حلقه مادر") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / یادداشت‌ها") },
                    leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ringNumber.isBlank()) {
                        ringError = true
                        return@Button
                    }
                    val updated = initialBird?.copy(
                        name = name.takeIf { it.isNotBlank() },
                        gender = gender,
                        variety = variety,
                        mutation = mutation.ifBlank { "Normal" },
                        color = color.ifBlank { "Green" },
                        placeOfBirth = placeOfBirth.takeIf { it.isNotBlank() },
                        generation = generation.ifBlank { "F1" },
                        cageCode = cageCode.takeIf { it.isNotBlank() },
                        status = status,
                        fatherRing = fatherRing.takeIf { it.isNotBlank() }?.trim()?.uppercase(),
                        motherRing = motherRing.takeIf { it.isNotBlank() }?.trim()?.uppercase(),
                        photoUri = photoUri.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() },
                        updatedAt = System.currentTimeMillis()
                    ) ?: BirdEntity(
                        ringNumber = ringNumber.trim().uppercase(),
                        name = name.takeIf { it.isNotBlank() },
                        gender = gender,
                        variety = variety,
                        mutation = mutation.ifBlank { "Normal" },
                        color = color.ifBlank { "Green" },
                        birthDate = System.currentTimeMillis(),
                        placeOfBirth = placeOfBirth.takeIf { it.isNotBlank() },
                        generation = generation.ifBlank { "F1" },
                        cageCode = cageCode.takeIf { it.isNotBlank() },
                        status = status,
                        fatherRing = fatherRing.takeIf { it.isNotBlank() }?.trim()?.uppercase(),
                        motherRing = motherRing.takeIf { it.isNotBlank() }?.trim()?.uppercase(),
                        photoUri = photoUri.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("bird_form_save_button")
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
private fun SuggestionItemRow(
    label: String,
    value: String,
    confidence: SuggestionConfidence,
    isConfirmed: Boolean,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                // Confidence / Confirmation badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (confidence) {
                        SuggestionConfidence.HIGH -> MaterialTheme.colorScheme.primaryContainer
                        SuggestionConfidence.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                        SuggestionConfidence.UNCERTAIN -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = when (confidence) {
                            SuggestionConfidence.HIGH -> "مطمئن (High)"
                            SuggestionConfidence.MEDIUM -> "نیازمند تایید (Needs confirmation)"
                            SuggestionConfidence.UNCERTAIN -> "حدس اولیه (Uncertain)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (isConfirmed) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Confirmed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(6.dp)
                )
            }
        } else {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("apply_suggestion_button")
            ) {
                Text("تایید و اعمال", fontSize = 11.sp)
            }
        }
    }
}

private fun analyzePhotoWithAi(
    context: Context,
    uri: Uri,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStateChange: (Boolean, BirdVisualSuggestion?, String?) -> Unit
) {
    onStateChange(true, null, null)

    coroutineScope.launch {
        try {
            val base64 = GeminiClient.uriToBase64(context, uri)
            if (base64 == null) {
                onStateChange(false, BirdPhotoAnalyzer.generateLocalFallbackSuggestion(), "Unable to decode image.")
                return@launch
            }

            if (GeminiClient.hasValidApiKey()) {
                val result = GeminiClient.queryGeminiVision(
                    systemPrompt = BirdPhotoAnalyzer.SYSTEM_PROMPT,
                    userPrompt = "Analyze this budgerigar photo. Identify primary body color, feather pattern/mutation, variety, and visible phenotype features. Remember: do not assume sex, age, or hidden genetics as certain.",
                    imageBase64 = base64
                )

                result.onSuccess { responseText ->
                    val parsed = BirdPhotoAnalyzer.parseAiJsonResponse(responseText)
                    onStateChange(false, parsed, null)
                }.onFailure { err ->
                    // Service unavailable / quota exceeded: fallback to structured inspection template
                    val fallback = BirdPhotoAnalyzer.generateLocalFallbackSuggestion()
                    onStateChange(false, fallback, err.message)
                }
            } else {
                // No API key configured: provide transparent visual suggestion template
                val fallback = BirdPhotoAnalyzer.generateLocalFallbackSuggestion()
                onStateChange(false, fallback, null)
            }
        } catch (e: Exception) {
            val fallback = BirdPhotoAnalyzer.generateLocalFallbackSuggestion()
            onStateChange(false, fallback, e.message)
        }
    }
}
