package com.example.feature.birds

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.entity.BirdEntity

@Composable
fun BirdsScreen(
    viewModel: BirdsViewModel,
    onNavigateToCage: (cageCode: String) -> Unit = {},
    onNavigateToPair: (pairId: Long) -> Unit = {},
    onNavigateToPedigree: (ringNumber: String) -> Unit = {},
    onNavigateToGenetics: (ringNumber: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    // If a bird is selected, show the central Bird Detail profile screen!
    if (state.selectedBirdRing != null) {
        val detailViewModel: BirdDetailViewModel = viewModel(
            key = "detail_${state.selectedBirdRing}",
            factory = BirdDetailViewModel.Factory(
                repository = viewModel.repository,
                initialRingNumber = state.selectedBirdRing!!
            )
        )
        BirdDetailScreen(
            viewModel = detailViewModel,
            onBack = { viewModel.selectBird(null) },
            onSelectOtherBird = { newRing -> viewModel.selectBird(newRing) },
            onNavigateToCage = onNavigateToCage,
            onNavigateToPair = onNavigateToPair,
            onNavigateToPedigree = onNavigateToPedigree,
            onNavigateToGenetics = onNavigateToGenetics,
            modifier = modifier
        )
        return
    }

    var birdToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("birds_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("birds_search_input"),
                placeholder = { Text("Search ring, name, color, mutation, cage...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row: Gender & Status
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                // Gender: All
                item {
                    FilterChip(
                        selected = state.filterGender == null,
                        onClick = { viewModel.setFilterGender(null) },
                        label = { Text(stringResource(R.string.bird_filter_all)) },
                        modifier = Modifier.testTag("filter_gender_all")
                    )
                }
                // Gender: Male
                item {
                    FilterChip(
                        selected = state.filterGender == BirdGender.MALE,
                        onClick = { viewModel.setFilterGender(if (state.filterGender == BirdGender.MALE) null else BirdGender.MALE) },
                        label = { Text(stringResource(R.string.bird_filter_male)) },
                        leadingIcon = { Icon(Icons.Filled.Male, contentDescription = null, tint = Color(0xFF1976D2)) },
                        modifier = Modifier.testTag("filter_gender_male")
                    )
                }
                // Gender: Female
                item {
                    FilterChip(
                        selected = state.filterGender == BirdGender.FEMALE,
                        onClick = { viewModel.setFilterGender(if (state.filterGender == BirdGender.FEMALE) null else BirdGender.FEMALE) },
                        label = { Text(stringResource(R.string.bird_filter_female)) },
                        leadingIcon = { Icon(Icons.Filled.Female, contentDescription = null, tint = Color(0xFFC2185B)) },
                        modifier = Modifier.testTag("filter_gender_female")
                    )
                }
                // Status: Active
                item {
                    FilterChip(
                        selected = state.filterStatus == BirdStatus.ACTIVE,
                        onClick = { viewModel.setFilterStatus(if (state.filterStatus == BirdStatus.ACTIVE) null else BirdStatus.ACTIVE) },
                        label = { Text("Active") },
                        modifier = Modifier.testTag("filter_status_active")
                    )
                }
                // Status: In Breeding
                item {
                    FilterChip(
                        selected = state.filterStatus == BirdStatus.BREEDING,
                        onClick = { viewModel.setFilterStatus(if (state.filterStatus == BirdStatus.BREEDING) null else BirdStatus.BREEDING) },
                        label = { Text("Breeding") },
                        modifier = Modifier.testTag("filter_status_breeding")
                    )
                }
                // Status: Sold
                item {
                    FilterChip(
                        selected = state.filterStatus == BirdStatus.SOLD,
                        onClick = { viewModel.setFilterStatus(if (state.filterStatus == BirdStatus.SOLD) null else BirdStatus.SOLD) },
                        label = { Text("Sold") },
                        modifier = Modifier.testTag("filter_status_sold")
                    )
                }
            }

            // Results count badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.birds.size} of ${state.totalCount} birds shown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.filterGender != null || state.filterStatus != null || state.searchQuery.isNotBlank()) {
                    TextButton(
                        onClick = {
                            viewModel.setFilterGender(null)
                            viewModel.setFilterStatus(null)
                            viewModel.setFilterVariety(null)
                            viewModel.setSearchQuery("")
                        }
                    ) {
                        Text("Reset Filters", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Birds List
            if (state.birds.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.Pets,
                    title = stringResource(R.string.empty_birds_title),
                    description = stringResource(R.string.empty_birds_desc)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.birds, key = { it.ringNumber }) { bird ->
                        BirdCardItem(
                            bird = bird,
                            onCardClick = { viewModel.selectBird(bird.ringNumber) },
                            onDeleteClick = { birdToDelete = bird.ringNumber }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Bird
        FloatingActionButton(
            onClick = { viewModel.setAddDialogOpen(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("birds_fab_add"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.action_add_bird)
            )
        }
    }

    // Add Bird Dialog
    if (state.isAddDialogOpen) {
        BirdFormDialog(
            initialBird = null,
            onDismiss = { viewModel.setAddDialogOpen(false) },
            onSave = { bird -> viewModel.saveBird(bird) }
        )
    }

    // Delete Confirmation Dialog
    if (birdToDelete != null) {
        AlertDialog(
            onDismissRequest = { birdToDelete = null },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.bird_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        val ring = birdToDelete!!
                        birdToDelete = null
                        viewModel.deleteBird(ring)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { birdToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun BirdCardItem(
    bird: BirdEntity,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("bird_item_${bird.ringNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sex Icon Circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        when (bird.gender) {
                            BirdGender.MALE -> Color(0xFF1976D2).copy(alpha = 0.15f)
                            BirdGender.FEMALE -> Color(0xFFC2185B).copy(alpha = 0.15f)
                            BirdGender.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        1.5.dp,
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
                    tint = when (bird.gender) {
                        BirdGender.MALE -> Color(0xFF1976D2)
                        BirdGender.FEMALE -> Color(0xFFC2185B)
                        BirdGender.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = bird.ringNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!bird.name.isNullOrBlank()) {
                        Text(
                            text = "(${bird.name})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${bird.variety.name.replace("_", " ")} • ${bird.mutation} • ${bird.color}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!bird.cageCode.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = bird.cageCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (bird.status) {
                            BirdStatus.ACTIVE -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                            BirdStatus.BREEDING -> Color(0xFFE65100).copy(alpha = 0.15f)
                            BirdStatus.SOLD -> Color(0xFF455A64).copy(alpha = 0.15f)
                            BirdStatus.DECEASED -> Color(0xFFB71C1C).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = bird.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (bird.status) {
                                BirdStatus.ACTIVE -> Color(0xFF2E7D32)
                                BirdStatus.BREEDING -> Color(0xFFE65100)
                                BirdStatus.SOLD -> Color(0xFF455A64)
                                BirdStatus.DECEASED -> Color(0xFFB71C1C)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("bird_delete_${bird.ringNumber}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
