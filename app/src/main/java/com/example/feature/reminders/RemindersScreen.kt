package com.example.feature.reminders

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import com.example.core.common.displayNameEn
import com.example.core.common.displayNameFa
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.entity.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    LaunchedEffect(Unit) {
        viewModel.loadPreferences(context)
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("reminders_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Action Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reminder_search_input"),
                    placeholder = {
                        Text(
                            if (isFa) "جستجوی یادآور، حلقه، قفس..." else "Search reminders, rings, cages...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Smart Sync Button
                OutlinedButton(
                    onClick = { viewModel.syncSmartReminders(context, isFa) },
                    enabled = !state.isSyncing,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("sync_reminders_button")
                ) {
                    if (state.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFa) "همگام‌سازی" else "Auto Sync", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main State Tabs (Today, Upcoming, Overdue, Completed, Notification Settings)
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("reminders_tab_row")
            ) {
                Tab(
                    selected = state.selectedTab == ReminderTab.TODAY,
                    onClick = { viewModel.setSelectedTab(ReminderTab.TODAY) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isFa) "وظایف امروز" else "Today")
                            if (state.todayCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${state.todayCount}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_today")
                )
                Tab(
                    selected = state.selectedTab == ReminderTab.UPCOMING,
                    onClick = { viewModel.setSelectedTab(ReminderTab.UPCOMING) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isFa) "پیش‌رو" else "Upcoming")
                            if (state.upcomingCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                    Text("${state.upcomingCount}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_upcoming")
                )
                Tab(
                    selected = state.selectedTab == ReminderTab.OVERDUE,
                    onClick = { viewModel.setSelectedTab(ReminderTab.OVERDUE) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isFa) "معوق / عقب‌افتاده" else "Overdue")
                            if (state.overdueCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("${state.overdueCount}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_overdue")
                )
                Tab(
                    selected = state.selectedTab == ReminderTab.COMPLETED,
                    onClick = { viewModel.setSelectedTab(ReminderTab.COMPLETED) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (isFa) "تکمیل شده" else "Completed")
                            if (state.completedCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.outline) {
                                    Text("${state.completedCount}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_completed")
                )
                Tab(
                    selected = state.selectedTab == ReminderTab.NOTIFICATION_SETTINGS,
                    onClick = { viewModel.setSelectedTab(ReminderTab.NOTIFICATION_SETTINGS) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(18.dp)) },
                    text = { Text(if (isFa) "تنظیمات اعلان" else "Alerts") },
                    modifier = Modifier.testTag("tab_alerts_settings")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body based on active tab
            if (state.selectedTab == ReminderTab.NOTIFICATION_SETTINGS) {
                NotificationSettingsView(
                    state = state,
                    viewModel = viewModel,
                    isFa = isFa,
                    context = context
                )
            } else {
                // Type Filter Chips for all 12 types
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = state.selectedTypeFilter == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text(if (isFa) "همه دسته‌ها" else "All Types") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("filter_all_types")
                    )

                    ReminderType.entries.forEach { type ->
                        val isSelected = state.selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setTypeFilter(if (isSelected) null else type)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = getReminderTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(if (isFa) type.displayNameFa else type.displayNameEn, fontSize = 12.sp)
                            },
                            modifier = Modifier.testTag("filter_type_${type.name}")
                        )
                    }
                }

                // If on completed tab, provide Clear Completed button
                if (state.selectedTab == ReminderTab.COMPLETED && state.displayedTasks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.clearCompleted() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("clear_completed_button")
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFa) "پاکسازی همه موارد تکمیل‌شده" else "Clear All Completed", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Tasks or Empty State
                if (state.displayedTasks.isEmpty()) {
                    val emptyTitle = when (state.selectedTab) {
                        ReminderTab.TODAY -> if (isFa) "هیچ وظیفه‌ای برای امروز ثبت نشده است" else "No Tasks Due Today"
                        ReminderTab.UPCOMING -> if (isFa) "هیچ وظیفه پیش‌رویی وجود ندارد" else "No Upcoming Tasks"
                        ReminderTab.OVERDUE -> if (isFa) "عالی! هیچ وظیفه معوق یا عقب‌افتاده‌ای ندارید" else "Great! No Overdue Tasks"
                        ReminderTab.COMPLETED -> if (isFa) "هیچ مورد تکمیل‌شده‌ای موجود نیست" else "No Completed Tasks"
                        else -> ""
                    }
                    val emptyDesc = when (state.selectedTab) {
                        ReminderTab.TODAY -> if (isFa) "از دکمه «+» برای افزودن یادآور یا «همگام‌سازی» برای بررسی خودکار تخم‌ها، داروها و نظافت استفاده کنید." else "Use '+' to add a reminder or 'Auto Sync' to inspect eggs, meds, and cage care."
                        ReminderTab.UPCOMING -> if (isFa) "یادآورهای آینده برای هچ تخم‌ها، نظافت هفتگی و معاینات دوره‌ای در اینجا نمایش داده می‌شوند." else "Future hatch dates, weekly cleaning, and scheduled care will appear here."
                        ReminderTab.OVERDUE -> if (isFa) "تمامی اقدامات سالن طبق زمان‌بندی انجام شده است." else "All aviary activities are currently on schedule."
                        ReminderTab.COMPLETED -> if (isFa) "وظایفی که علامت تکمیل می‌خورند در این بخش آرشیو می‌شوند." else "Tasks marked as completed will be archived here."
                        else -> ""
                    }

                    EmptyStateView(
                        icon = if (state.selectedTab == ReminderTab.OVERDUE) Icons.Filled.CheckCircle else Icons.Filled.Notifications,
                        title = emptyTitle,
                        description = emptyDesc,
                        actionLabel = if (isFa) "ثبت یادآور جدید" else "Add Reminder",
                        onActionClick = { viewModel.setAddDialogOpen(true) },
                        testTag = "reminders_empty_view"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.displayedTasks, key = { it.id }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                isFa = isFa,
                                onToggleComplete = { viewModel.toggleComplete(reminder) },
                                onReschedule = { days -> viewModel.rescheduleReminder(reminder, days) },
                                onDelete = { viewModel.deleteReminder(reminder) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.setAddDialogOpen(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_reminder_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
        }

        // Add Reminder Dialog
        if (state.isAddDialogOpen) {
            AddReminderDialog(
                isFa = isFa,
                onDismiss = { viewModel.setAddDialogOpen(false) },
                onAdd = { title, desc, dueDate, priority, type, ring, cage, pair, repeat ->
                    viewModel.addReminder(
                        title = title,
                        description = desc,
                        dueDate = dueDate,
                        priority = priority,
                        type = type,
                        relatedRingNumber = ring,
                        cageCode = cage,
                        pairId = pair,
                        repeatIntervalDays = repeat
                    )
                }
            )
        }
    }
}

@Composable
private fun ReminderItemCard(
    reminder: ReminderEntity,
    isFa: Boolean,
    onToggleComplete: () -> Unit,
    onReschedule: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val priorityColor = when (reminder.priority) {
        ReminderPriority.URGENT -> MaterialTheme.colorScheme.error
        ReminderPriority.HIGH -> Color(0xFFE65100)
        ReminderPriority.NORMAL -> MaterialTheme.colorScheme.primary
        ReminderPriority.LOW -> Color(0xFF546E7A)
    }

    val now = System.currentTimeMillis()
    val isOverdue = !reminder.isCompleted && reminder.dueDate < now - 3600000L
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val dueDateStr = sdf.format(Date(reminder.dueDate))

    val daysDiff = ((reminder.dueDate - now) / 86400000.0).toInt()
    val timeLabel = when {
        reminder.isCompleted -> if (isFa) "تکمیل شده" else "Completed"
        isOverdue -> if (isFa) "عقب‌افتاده (${-daysDiff} روز)" else "Overdue by ${-daysDiff}d"
        daysDiff == 0 -> if (isFa) "موعد امروز" else "Due Today"
        daysDiff == 1 -> if (isFa) "فردا" else "Tomorrow"
        else -> if (isFa) "تا $daysDiff روز دیگر" else "In $daysDiff days"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_item_${reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Priority Indicator Strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (reminder.isCompleted) Color.Gray else priorityColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Completion Toggle Button
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_complete_${reminder.id}")
            ) {
                if (reminder.isCompleted) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Mark Complete",
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Content & Tags
            Column(modifier = Modifier.weight(1f)) {
                // Header tags: Type + Timing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = getReminderTypeIcon(reminder.type),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isFa) reminder.type.displayNameFa else reminder.type.displayNameEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Due Tag
                    Surface(
                        color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )

                // Description
                reminder.description?.let { desc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata Chips: Due Date & Related Entities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = dueDateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    reminder.relatedRingNumber?.let { ring ->
                        Text(
                            text = "• Ring: $ring",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    reminder.cageCode?.let { cage ->
                        Text(
                            text = "• Cage: $cage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    reminder.pairId?.let { pair ->
                        Text(
                            text = "• Pair: #$pair",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    if (reminder.repeatIntervalDays > 0) {
                        Text(
                            text = "• ⟳ ${reminder.repeatIntervalDays}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Action Menu
            Box {
                IconButton(
                    onClick = { isMenuOpen = true },
                    modifier = Modifier.testTag("reminder_menu_${reminder.id}")
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = { isMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isFa) "به‌تعویق انداختن (+۱ روز)" else "Snooze (+1 Day)") },
                        leadingIcon = { Icon(Icons.Filled.Alarm, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onReschedule(1)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isFa) "به‌تعویق انداختن (+۳ روز)" else "Snooze (+3 Days)") },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onReschedule(3)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isFa) "به‌تعویق انداختن (+۱ هفته)" else "Snooze (+1 Week)") },
                        leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                        onClick = {
                            isMenuOpen = false
                            onReschedule(7)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (isFa) "حذف یادآور" else "Delete Reminder", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            isMenuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingsView(
    state: RemindersUiState,
    viewModel: RemindersViewModel,
    isFa: Boolean,
    context: android.content.Context
) {
    val prefs = state.notificationPreferences
    val currentLang = LocalAppLanguage.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notification_settings_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Master Toggle Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (prefs.isGloballyEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = if (isFa) "اعلان‌های سیستم سالن" else "Aviary Push Alerts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFa) "ارسال اعلان‌ها به زبان فعلی برنامه (${if (isFa) "فارسی" else "English"})"
                                    else "Sends alerts in the active app language (${if (isFa) "Persian" else "English"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = prefs.isGloballyEnabled,
                            onCheckedChange = { viewModel.updateGlobalNotification(context, it) },
                            modifier = Modifier.testTag("master_notification_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Schedule Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isFa) "ساعت ارسال اعلان‌های روزانه" else "Daily Scheduled Alert Time",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", prefs.reminderHour, prefs.reminderMinute),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.updateReminderTime(context, 8, 0) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("08:00")
                            }
                            OutlinedButton(
                                onClick = { viewModel.updateReminderTime(context, 9, 30) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("09:30")
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Notification Types
        item {
            Text(
                text = if (isFa) "مدیریت دسته‌های اعلان (فعال/غیرفعال)" else "Notification Categories (Enable/Disable)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // All 12 Types
        items(ReminderType.entries) { type ->
            val isEnabled = prefs.enabledTypes.contains(type)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notif_type_card_${type.name}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getReminderTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isFa) type.displayNameFa else type.displayNameEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = getReminderTypeDescription(type, isFa),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Send Test Notification Button
                        IconButton(
                            onClick = { viewModel.sendTestNotification(context, type, currentLang) },
                            modifier = Modifier.testTag("test_notif_${type.name}")
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Test Notification",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.toggleNotificationType(context, type) },
                            modifier = Modifier.testTag("switch_notif_${type.name}")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    isFa: Boolean,
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        description: String?,
        dueDate: Long,
        priority: ReminderPriority,
        type: ReminderType,
        ringNumber: String?,
        cageCode: String?,
        pairId: Long?,
        repeatDays: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.DAILY_TASK) }
    var selectedPriority by remember { mutableStateOf(ReminderPriority.NORMAL) }
    var ringNumber by remember { mutableStateOf("") }
    var cageCode by remember { mutableStateOf("") }
    var pairIdStr by remember { mutableStateOf("") }
    var repeatDays by remember { mutableIntStateOf(0) }
    var dueDaysOffset by remember { mutableIntStateOf(0) } // 0 = today, 1 = tomorrow, 6 = candling, 18 = hatch

    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val calculatedDueDate = now + (dueDaysOffset * 86400000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isFa) "ثبت یادآور و وظیفه جدید" else "New Aviary Task & Reminder",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isFa) "عنوان وظیفه / اقدام *" else "Task Title *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_reminder_title"),
                    singleLine = true
                )

                // Category / Type Dropdown
                Text(
                    text = if (isFa) "دسته وظیفه" else "Task Category",
                    style = MaterialTheme.typography.labelMedium
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isTypeMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_reminder_type_btn")
                    ) {
                        Icon(getReminderTypeIcon(selectedType), contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFa) selectedType.displayNameFa else selectedType.displayNameEn)
                    }

                    DropdownMenu(
                        expanded = isTypeMenuExpanded,
                        onDismissRequest = { isTypeMenuExpanded = false }
                    ) {
                        ReminderType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(if (isFa) type.displayNameFa else type.displayNameEn) },
                                leadingIcon = { Icon(getReminderTypeIcon(type), contentDescription = null) },
                                onClick = {
                                    selectedType = type
                                    isTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority Selector
                Text(
                    text = if (isFa) "اولویت" else "Priority",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReminderPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Due Date Presets
                Text(
                    text = if (isFa) "زمان سررسید" else "Due Timing",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = dueDaysOffset == 0,
                        onClick = { dueDaysOffset = 0 },
                        label = { Text(if (isFa) "امروز" else "Today") }
                    )
                    FilterChip(
                        selected = dueDaysOffset == 1,
                        onClick = { dueDaysOffset = 1 },
                        label = { Text(if (isFa) "فردا" else "Tomorrow") }
                    )
                    FilterChip(
                        selected = dueDaysOffset == 3,
                        onClick = { dueDaysOffset = 3 },
                        label = { Text(if (isFa) "۳ روز دیگر" else "+3 Days") }
                    )
                    FilterChip(
                        selected = dueDaysOffset == 6,
                        onClick = { dueDaysOffset = 6 },
                        label = { Text(if (isFa) "کندلینگ (۶ روز)" else "Candle (6d)") }
                    )
                    FilterChip(
                        selected = dueDaysOffset == 7,
                        onClick = { dueDaysOffset = 7 },
                        label = { Text(if (isFa) "۱ هفته دیگر" else "+1 Week") }
                    )
                    FilterChip(
                        selected = dueDaysOffset == 18,
                        onClick = { dueDaysOffset = 18 },
                        label = { Text(if (isFa) "هچ تخم (۱۸ روز)" else "Hatch (18d)") }
                    )
                }

                // Repeat Schedule
                Text(
                    text = if (isFa) "تکرار دوره‌ای" else "Recurring Schedule",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = repeatDays == 0,
                        onClick = { repeatDays = 0 },
                        label = { Text(if (isFa) "یک‌بار" else "Once", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = repeatDays == 1,
                        onClick = { repeatDays = 1 },
                        label = { Text(if (isFa) "روزانه" else "Daily", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = repeatDays == 7,
                        onClick = { repeatDays = 7 },
                        label = { Text(if (isFa) "هفتگی" else "Weekly", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = repeatDays == 30,
                        onClick = { repeatDays = 30 },
                        label = { Text(if (isFa) "ماهانه" else "Monthly", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Optional Reference Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = ringNumber,
                        onValueChange = { ringNumber = it },
                        label = { Text(if (isFa) "شماره حلقه (اختیاری)" else "Ring #", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cageCode,
                        onValueChange = { cageCode = it },
                        label = { Text(if (isFa) "کد قفس (اختیاری)" else "Cage Code", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isFa) "توضیحات و یادداشت" else "Description / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val parsedPair = pairIdStr.toLongOrNull()
                        onAdd(
                            title,
                            description,
                            calculatedDueDate,
                            selectedPriority,
                            selectedType,
                            ringNumber,
                            cageCode,
                            parsedPair,
                            repeatDays
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("dialog_submit_reminder")
            ) {
                Text(if (isFa) "ثبت یادآور" else "Save Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

fun getReminderTypeIcon(type: ReminderType): ImageVector {
    return when (type) {
        ReminderType.EGG_LAYING -> Icons.Filled.Egg
        ReminderType.EXPECTED_HATCH -> Icons.Filled.Event
        ReminderType.MATING -> Icons.Filled.Favorite
        ReminderType.MEDICATION -> Icons.Filled.Medication
        ReminderType.SUPPLEMENTS -> Icons.Filled.Vaccines
        ReminderType.WEIGHT_MEASUREMENT -> Icons.Filled.FitnessCenter
        ReminderType.FEEDING -> Icons.Filled.Restaurant
        ReminderType.CLEANING -> Icons.Filled.CleaningServices
        ReminderType.INVENTORY -> Icons.Filled.Inventory2
        ReminderType.DAILY_TASK -> Icons.Filled.CheckCircle
        ReminderType.IMPORTANT_EVENT -> Icons.Filled.Warning
        ReminderType.GENETIC_CALENDAR -> Icons.Filled.Science
    }
}

fun getReminderTypeDescription(type: ReminderType, isFa: Boolean): String {
    return if (isFa) {
        when (type) {
            ReminderType.EGG_LAYING -> "هشدارهای کندلینگ روز ۶ و بررسی فواصل تخم‌گذاری"
            ReminderType.EXPECTED_HATCH -> "پیش‌بینی روز ۱۸ و آماده‌سازی لانه جهت خروج جوجه"
            ReminderType.MATING -> "معرفی جفت‌ها و بررسی تخم‌ریزی اولیه"
            ReminderType.MEDICATION -> "دوزهای دارویی، زمان اتمام درمان و ایزولاسیون"
            ReminderType.SUPPLEMENTS -> "ویتامین E، کلسیم مایع و پروبیوتیک‌های هفتگی"
            ReminderType.WEIGHT_MEASUREMENT -> "توزین دوره‌ای جوجه‌های در حال رشد و مولدین"
            ReminderType.FEEDING -> "توزیع غذای نرم، سبزیجات تازه و دان اصلی"
            ReminderType.CLEANING -> "شستشوی کفی‌ها، ضدعفونی قفس و تعویض پوشال"
            ReminderType.INVENTORY -> "هشدار اتمام دان، دارو، مکمل و حلقه‌ها"
            ReminderType.DAILY_TASK -> "تعویض آب تازه و چک عمومی وضعیت سالن"
            ReminderType.IMPORTANT_EVENT -> "سفارش حلقه، شروع فصل و تاریخ مسابقات"
            ReminderType.GENETIC_CALENDAR -> "برنامه‌ریزی نسل‌ها و جفت‌اندازی‌های اصلاح نژادی"
        }
    } else {
        when (type) {
            ReminderType.EGG_LAYING -> "Candling at day 6 and lay interval tracking"
            ReminderType.EXPECTED_HATCH -> "18-day incubation due date alerts"
            ReminderType.MATING -> "Pairing introduction and nest box inspection"
            ReminderType.MEDICATION -> "Active antibiotic and antifungal treatment doses"
            ReminderType.SUPPLEMENTS -> "Weekly liquid calcium + D3 & probiotics"
            ReminderType.WEIGHT_MEASUREMENT -> "Weight progress for nursery chicks and breeders"
            ReminderType.FEEDING -> "Soft eggfood, seed replenishment & greens"
            ReminderType.CLEANING -> "Tray cleaning, disinfection and nest liner changes"
            ReminderType.INVENTORY -> "Low stock warnings for seed, meds, and rings"
            ReminderType.DAILY_TASK -> "Daily drinker wash and aviary inspection"
            ReminderType.IMPORTANT_EVENT -> "Ring orders, show deadlines, breeding seasons"
            ReminderType.GENETIC_CALENDAR -> "Test pairings and mutation cycle milestones"
        }
    }
}
