package com.example.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import com.example.core.common.displayNameEn
import com.example.core.common.displayNameFa
import com.example.core.localization.AppLanguage
import com.example.data.database.entity.ReminderEntity

data class NotificationPreferences(
    val isGloballyEnabled: Boolean = true,
    val enabledTypes: Set<ReminderType> = ReminderType.entries.toSet(),
    val reminderHour: Int = 8,
    val reminderMinute: Int = 30,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true
)

object NotificationPreferencesStore {
    private const val PREFS_NAME = "aviary_notification_prefs"
    private const val KEY_GLOBAL_ENABLED = "key_global_enabled"
    private const val KEY_ENABLED_TYPES = "key_enabled_types"
    private const val KEY_REMINDER_HOUR = "key_reminder_hour"
    private const val KEY_REMINDER_MIN = "key_reminder_min"
    private const val KEY_SOUND = "key_sound"
    private const val KEY_VIBRATION = "key_vibration"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): NotificationPreferences {
        val prefs = getPrefs(context)
        val global = prefs.getBoolean(KEY_GLOBAL_ENABLED, true)
        val typesStringSet = prefs.getStringSet(KEY_ENABLED_TYPES, null)
        val enabledTypes = if (typesStringSet == null) {
            ReminderType.entries.toSet()
        } else {
            typesStringSet.mapNotNull {
                try { ReminderType.valueOf(it) } catch (_: Exception) { null }
            }.toSet()
        }
        val hour = prefs.getInt(KEY_REMINDER_HOUR, 8)
        val min = prefs.getInt(KEY_REMINDER_MIN, 30)
        val sound = prefs.getBoolean(KEY_SOUND, true)
        val vibration = prefs.getBoolean(KEY_VIBRATION, true)

        return NotificationPreferences(
            isGloballyEnabled = global,
            enabledTypes = enabledTypes,
            reminderHour = hour,
            reminderMinute = min,
            isSoundEnabled = sound,
            isVibrationEnabled = vibration
        )
    }

    fun save(context: Context, preferences: NotificationPreferences) {
        getPrefs(context).edit()
            .putBoolean(KEY_GLOBAL_ENABLED, preferences.isGloballyEnabled)
            .putStringSet(KEY_ENABLED_TYPES, preferences.enabledTypes.map { it.name }.toSet())
            .putInt(KEY_REMINDER_HOUR, preferences.reminderHour)
            .putInt(KEY_REMINDER_MIN, preferences.reminderMinute)
            .putBoolean(KEY_SOUND, preferences.isSoundEnabled)
            .putBoolean(KEY_VIBRATION, preferences.isVibrationEnabled)
            .apply()
    }
}

object AviaryNotificationManager {

    const val CHANNEL_ID = "aviary_reminders_channel"
    private const val CHANNEL_NAME_EN = "Aviary Tasks & Breeding Alerts"
    private const val CHANNEL_NAME_FA = "یادآورها و هشدارهای تکثیر سالن"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME_EN
            val descriptionText = "Notifications for budgerigar hatching, egg candling, medication, feeding, and aviary care."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canSendNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showReminderNotification(
        context: Context,
        reminder: ReminderEntity,
        language: AppLanguage
    ): Boolean {
        val prefs = NotificationPreferencesStore.load(context)
        if (!prefs.isGloballyEnabled || !prefs.enabledTypes.contains(reminder.type)) {
            return false
        }

        initNotificationChannel(context)

        val isPersian = language == AppLanguage.PERSIAN
        val typeTitle = if (isPersian) reminder.type.displayNameFa else reminder.type.displayNameEn
        val title = "$typeTitle: ${reminder.title}"
        val content = reminder.description ?: if (isPersian) "موعد اقدام در سالن پرورش مرغ عشق" else "Aviary management action required."

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityLevel = when (reminder.priority) {
            ReminderPriority.URGENT -> NotificationCompat.PRIORITY_MAX
            ReminderPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            ReminderPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            ReminderPriority.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(priorityLevel)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (prefs.isVibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (canSendNotifications(context)) {
                val notifId = (1000 + (reminder.id % 9000)).toInt()
                notificationManager.notify(notifId, builder.build())
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun sendTestNotification(
        context: Context,
        type: ReminderType,
        language: AppLanguage
    ): Boolean {
        val isPersian = language == AppLanguage.PERSIAN
        val dummy = ReminderEntity(
            id = (System.currentTimeMillis() % 10000),
            title = if (isPersian) "تست اعلان سیستم یادآور سالن" else "Aviary Notification Test",
            description = if (isPersian)
                "اعلان‌های مربوط به ${type.displayNameFa} با موفقیت در دستگاه فعال و تنظیم شدند."
            else
                "Notifications for ${type.displayNameEn} are successfully active and configured.",
            dueDate = System.currentTimeMillis(),
            priority = ReminderPriority.HIGH,
            type = type
        )
        return showReminderNotification(context, dummy, language)
    }
}
