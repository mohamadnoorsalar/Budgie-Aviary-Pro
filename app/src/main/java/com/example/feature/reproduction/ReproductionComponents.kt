package com.example.feature.reproduction

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.relation.ChickWithBreedingDetails
import com.example.data.database.relation.PairWithBreedingDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

@Composable
fun BreedingTimelineCard(
    pair: PairWithBreedingDetails?,
    onSyncReminders: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("breeding_timeline_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Budgerigar Breeding Lifecycle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (pair != null) {
                            "Pair #${pair.pair.id} (♂ ${pair.pair.maleRingNumber} × ♀ ${pair.pair.femaleRingNumber})"
                        } else {
                            "Standard Species Incubation & Weaning Protocol"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onSyncReminders,
                    modifier = Modifier.testTag("sync_reminders_button")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Reminders", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Timeline Steps
            val milestones = listOf(
                "Mating" to "Day 0",
                "Laying" to "Day 8-12",
                "Candling" to "Day 5-7 inc.",
                "Hatching" to "Day 18 inc.",
                "Banding" to "Day 6-8 age",
                "Weaning" to "Day 30-35"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                milestones.forEachIndexed { index, (name, timing) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = timing,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingHatchesCard(
    upcomingEggs: List<EggEntity>,
    onHatchEgg: (EggEntity) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_hatches_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Egg, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Hatches Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${upcomingEggs.size} due soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (upcomingEggs.isEmpty()) {
                Text(
                    text = "No eggs currently within expected hatch window.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                upcomingEggs.take(4).forEach { egg ->
                    val now = System.currentTimeMillis()
                    val expectedDate = egg.expectedHatchDate ?: (egg.layDate + 18L * 86400000L)
                    val daysLeft = ((expectedDate - now) / 86400000L)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Egg #${egg.eggNumber} (Pair #${egg.pairId})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Due: ${dateFormat.format(Date(expectedDate))} (${if (daysLeft <= 0) "Today/Overdue" else "in $daysLeft days"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (daysLeft <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onHatchEgg(egg) },
                                modifier = Modifier.testTag("quick_hatch_button_${egg.id}")
                            ) {
                                Text("Hatch", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EggItemCard(
    egg: EggEntity,
    onCandle: () -> Unit,
    onHatch: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("egg_item_card_${egg.eggNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            egg.isHatched -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            egg.fertilityStatus == "FERTILE" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            egg.fertilityStatus == "INFERTILE" -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (egg.isHatched) Icons.Default.ChildCare else Icons.Default.Egg,
                                contentDescription = null,
                                tint = when {
                                    egg.isHatched -> Color(0xFF2E7D32)
                                    egg.fertilityStatus == "FERTILE" -> Color(0xFF1976D2)
                                    egg.fertilityStatus == "INFERTILE" -> Color(0xFFD32F2F)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Egg #${egg.eggNumber} (Pair #${egg.pairId})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Laid: ${dateFormat.format(Date(egg.layDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip
                val statusColor = when {
                    egg.isHatched -> Color(0xFF2E7D32)
                    egg.fertilityStatus == "FERTILE" -> Color(0xFF1976D2)
                    egg.fertilityStatus == "INFERTILE" -> Color(0xFFD32F2F)
                    egg.fertilityStatus == "DEAD_IN_SHELL" -> Color(0xFF795548)
                    else -> MaterialTheme.colorScheme.outline
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (egg.isHatched) "HATCHED" else egg.fertilityStatus,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Hatch details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Expected Hatch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = egg.expectedHatchDate?.let { dateFormat.format(Date(it)) } ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text("Actual Hatch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = egg.actualHatchDate?.let { dateFormat.format(Date(it)) } ?: if (egg.isHatched) "Yes" else "Pending",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text("Result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = egg.eggResult ?: if (egg.isHatched) "HATCHED" else "INCUBATING",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!egg.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${egg.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_egg_${egg.eggNumber}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Egg", tint = MaterialTheme.colorScheme.error)
                }

                OutlinedButton(
                    onClick = onCandle,
                    modifier = Modifier.testTag("candle_button_${egg.eggNumber}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Candle")
                }

                if (!egg.isHatched && egg.fertilityStatus != "INFERTILE") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onHatch,
                        modifier = Modifier.testTag("hatch_button_${egg.eggNumber}")
                    ) {
                        Icon(Icons.Default.ChildCare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hatch")
                    }
                }
            }
        }
    }
}

@Composable
fun ChickItemCard(
    chickDetails: ChickWithBreedingDetails,
    onLogWeight: () -> Unit,
    onTransfer: () -> Unit,
    onRegisterBird: () -> Unit,
    onMortality: () -> Unit,
    onDelete: () -> Unit
) {
    val chick = chickDetails.chick
    val pair = chickDetails.pair
    val ageDays = ((System.currentTimeMillis() - chick.hatchDate) / 86400000L).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chick_card_${chick.hatchOrder}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chick #${chick.hatchOrder} ${chick.bandedRingNumber?.let { "• $it" } ?: "(Unbanded)"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hatched: ${dateFormat.format(Date(chick.hatchDate))} ($ageDays days old)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (chick.status) {
                        "IN_NEST" -> MaterialTheme.colorScheme.primaryContainer
                        "WEANED", "TRANSFERRED" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        "DECEASED" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = chick.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (chick.status == "DECEASED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Parents & Cage lineage bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Parents: ♂ ${pair?.maleRingNumber ?: "Unknown"} × ♀ ${pair?.femaleRingNumber ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cage: ${chick.cageCode ?: pair?.cageCode ?: "In Nest"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Weight & Stage Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = chick.weightGrams?.let { "${it}g" } ?: "Not logged",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text("Growth Stage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = chick.growthStage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("Egg Origin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "Egg #${chickDetails.egg?.eggNumber ?: chick.hatchOrder}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (chick.status == "DECEASED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Mortality Date: ${chick.mortalityDate?.let { dateFormat.format(Date(it)) } ?: "Recorded"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Cause: ${chick.mortalityReason ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Chick", tint = MaterialTheme.colorScheme.error)
                }

                OutlinedButton(onClick = onLogWeight, modifier = Modifier.weight(1f).testTag("chick_weight_btn_${chick.hatchOrder}")) {
                    Text("Weight", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(onClick = onTransfer, modifier = Modifier.weight(1f).testTag("chick_transfer_btn_${chick.hatchOrder}")) {
                    Text("Transfer", style = MaterialTheme.typography.labelSmall)
                }

                if (chick.status != "DECEASED") {
                    Button(onClick = onRegisterBird, modifier = Modifier.weight(1.2f).testTag("chick_register_btn_${chick.hatchOrder}")) {
                        Text("Register Bird", style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onMortality, modifier = Modifier.size(36.dp).testTag("chick_mortality_btn_${chick.hatchOrder}")) {
                        Icon(Icons.Default.Warning, contentDescription = "Mortality", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun NestItemCard(
    nest: NestEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nest_card_${nest.boxNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Box ${nest.boxNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Bedding: ${nest.nestMaterial}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Cage: ${nest.cageCode ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (nest.isClean) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (nest.isClean) "Clean" else "Dirty",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (nest.isClean) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Nest", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
