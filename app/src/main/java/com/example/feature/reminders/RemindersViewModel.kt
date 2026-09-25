package com.example.feature.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import com.example.core.localization.AppLanguage
import com.example.core.notification.AviaryNotificationManager
import com.example.core.notification.NotificationPreferences
import com.example.core.notification.NotificationPreferencesStore
import com.example.data.database.entity.ReminderEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReminderTab {
    TODAY,
    UPCOMING,
    OVERDUE,
    COMPLETED,
    NOTIFICATION_SETTINGS
}

data class RemindersUiFilterState(
    val tab: ReminderTab = ReminderTab.TODAY,
    val typeFilter: ReminderType? = null,
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val isSyncing: Boolean = false,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val statusMessage: String? = null
)

data class RemindersUiState(
    val selectedTab: ReminderTab = ReminderTab.TODAY,
    val selectedTypeFilter: ReminderType? = null,
    val searchQuery: String = "",
    val allReminders: List<ReminderEntity> = emptyList(),
    val todayTasks: List<ReminderEntity> = emptyList(),
    val upcomingTasks: List<ReminderEntity> = emptyList(),
    val overdueTasks: List<ReminderEntity> = emptyList(),
    val completedTasks: List<ReminderEntity> = emptyList(),
    val displayedTasks: List<ReminderEntity> = emptyList(),
    val todayCount: Int = 0,
    val upcomingCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val isAddDialogOpen: Boolean = false,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

class RemindersViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ReminderTab.TODAY)
    private val _selectedTypeFilter = MutableStateFlow<ReminderType?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _isSyncing = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())

    private val _filterState: StateFlow<RemindersUiFilterState> = combine(
        combine(_selectedTab, _selectedTypeFilter, _searchQuery) { tab, type, query ->
            Triple(tab, type, query)
        },
        combine(_isAddDialogOpen, _isSyncing, _statusMessage) { isAdd, isSync, status ->
            Triple(isAdd, isSync, status)
        },
        _notificationPreferences
    ) { (tab, type, query), (isAdd, isSync, status), notifPrefs ->
        RemindersUiFilterState(
            tab = tab,
            typeFilter = type,
            searchQuery = query,
            isAddDialogOpen = isAdd,
            isSyncing = isSync,
            notificationPreferences = notifPrefs,
            statusMessage = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiFilterState()
    )

    val uiState: StateFlow<RemindersUiState> = combine(
        repository.allReminders,
        _filterState
    ) { allReminders: List<ReminderEntity>, filters: RemindersUiFilterState ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Start of today (00:00:00)
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        // End of today (23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        val pending = allReminders.filter { !it.isCompleted }
        val completed = allReminders.filter { it.isCompleted }

        val todayTasks = pending.filter { it.dueDate in startOfToday..endOfToday }
        val upcomingTasks = pending.filter { it.dueDate > endOfToday }
        val overdueTasks = pending.filter { it.dueDate < startOfToday }

        val baseList = when (filters.tab) {
            ReminderTab.TODAY -> todayTasks
            ReminderTab.UPCOMING -> upcomingTasks
            ReminderTab.OVERDUE -> overdueTasks
            ReminderTab.COMPLETED -> completed
            ReminderTab.NOTIFICATION_SETTINGS -> emptyList()
        }

        val filteredList = baseList.filter { item ->
            val matchesType = filters.typeFilter == null || item.type == filters.typeFilter
            val matchesSearch = filters.searchQuery.isBlank() ||
                    item.title.contains(filters.searchQuery, ignoreCase = true) ||
                    (item.description?.contains(filters.searchQuery, ignoreCase = true) == true) ||
                    (item.relatedRingNumber?.contains(filters.searchQuery, ignoreCase = true) == true) ||
                    (item.cageCode?.contains(filters.searchQuery, ignoreCase = true) == true)
            matchesType && matchesSearch
        }

        RemindersUiState(
            selectedTab = filters.tab,
            selectedTypeFilter = filters.typeFilter,
            searchQuery = filters.searchQuery,
            allReminders = allReminders,
            todayTasks = todayTasks,
            upcomingTasks = upcomingTasks,
            overdueTasks = overdueTasks,
            completedTasks = completed,
            displayedTasks = filteredList,
            todayCount = todayTasks.size,
            upcomingCount = upcomingTasks.size,
            overdueCount = overdueTasks.size,
            completedCount = completed.size,
            notificationPreferences = filters.notificationPreferences,
            isAddDialogOpen = filters.isAddDialogOpen,
            isSyncing = filters.isSyncing,
            statusMessage = filters.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiState()
    )

    fun loadPreferences(context: Context) {
        _notificationPreferences.value = NotificationPreferencesStore.load(context)
    }

    fun setSelectedTab(tab: ReminderTab) {
        _selectedTab.value = tab
    }

    fun setTypeFilter(type: ReminderType?) {
        _selectedTypeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAddDialogOpen(open: Boolean) {
        _isAddDialogOpen.value = open
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun addReminder(
        title: String,
        description: String?,
        dueDate: Long,
        priority: ReminderPriority,
        type: ReminderType,
        relatedRingNumber: String? = null,
        cageCode: String? = null,
        pairId: Long? = null,
        repeatIntervalDays: Int = 0
    ) {
        viewModelScope.launch {
            repository.saveReminder(
                ReminderEntity(
                    title = title.trim(),
                    description = description?.takeIf { it.isNotBlank() },
                    dueDate = dueDate,
                    priority = priority,
                    type = type,
                    relatedRingNumber = relatedRingNumber?.takeIf { it.isNotBlank() },
                    cageCode = cageCode?.takeIf { it.isNotBlank() },
                    pairId = pairId,
                    repeatIntervalDays = repeatIntervalDays
                )
            )
            _isAddDialogOpen.value = false
            _statusMessage.value = "Reminder saved successfully"
        }
    }

    fun toggleComplete(reminder: ReminderEntity) {
        viewModelScope.launch {
            val newStatus = !reminder.isCompleted
            val completedTime = if (newStatus) System.currentTimeMillis() else null
            repository.updateReminder(
                reminder.copy(
                    isCompleted = newStatus,
                    completedAt = completedTime
                )
            )

            // If it had a repeat interval and was marked complete, schedule the next recurring instance!
            if (newStatus && reminder.repeatIntervalDays > 0) {
                val nextDueDate = reminder.dueDate + (reminder.repeatIntervalDays * 86400000L)
                repository.saveReminder(
                    reminder.copy(
                        id = 0,
                        dueDate = nextDueDate,
                        isCompleted = false,
                        completedAt = null,
                        syncId = java.util.UUID.randomUUID().toString()
                    )
                )
            }
        }
    }

    fun rescheduleReminder(reminder: ReminderEntity, addedDays: Int) {
        viewModelScope.launch {
            val newDueDate = reminder.dueDate + (addedDays * 86400000L)
            repository.updateReminder(reminder.copy(dueDate = newDueDate))
            _statusMessage.value = "Rescheduled for ${addedDays} day(s) later"
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _statusMessage.value = "Reminder deleted"
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompletedReminders()
            _statusMessage.value = "Completed reminders cleared"
        }
    }

    fun syncSmartReminders(context: Context, isPersian: Boolean = false) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val count = repository.syncSmartReminders()
                _statusMessage.value = if (isPersian) {
                    if (count > 0) "$count یادآور هوشمند با موفقیت تولید و ثبت شد" else "همه یادآورها به‌روز هستند (هیچ مورد تکراری افزوده نشد)"
                } else {
                    if (count > 0) "$count smart reminders generated" else "All reminders are up to date (no duplicates added)"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Sync error: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun toggleNotificationType(context: Context, type: ReminderType) {
        val current = _notificationPreferences.value
        val updated = if (current.enabledTypes.contains(type)) {
            current.copy(enabledTypes = current.enabledTypes - type)
        } else {
            current.copy(enabledTypes = current.enabledTypes + type)
        }
        _notificationPreferences.value = updated
        NotificationPreferencesStore.save(context, updated)
    }

    fun updateGlobalNotification(context: Context, enabled: Boolean) {
        val updated = _notificationPreferences.value.copy(isGloballyEnabled = enabled)
        _notificationPreferences.value = updated
        NotificationPreferencesStore.save(context, updated)
    }

    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        val updated = _notificationPreferences.value.copy(reminderHour = hour, reminderMinute = minute)
        _notificationPreferences.value = updated
        NotificationPreferencesStore.save(context, updated)
    }

    fun sendTestNotification(context: Context, type: ReminderType, language: AppLanguage) {
        val sent = AviaryNotificationManager.sendTestNotification(context, type, language)
        _statusMessage.value = if (sent) {
            if (language == AppLanguage.PERSIAN) "اعلان تستی ارسال شد" else "Test notification sent"
        } else {
            if (language == AppLanguage.PERSIAN) "ارسال اعلان غیرفعال است یا مجوز لازم داده نشده است" else "Notification permission disabled or type not active"
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(repository) as T
        }
    }
}
