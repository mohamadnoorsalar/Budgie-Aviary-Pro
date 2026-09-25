package com.example.core.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import android.util.Base64
import androidx.core.content.edit
import com.example.data.database.dao.AuditLogDao
import com.example.data.database.entity.AuditLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SecurityManager private constructor(
    private val context: Context,
    private val auditLogDao: AuditLogDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L
    private var lockoutJob: Job? = null

    init {
        // If app lock is enabled, start locked
        if (_state.value.isLockConfigured) {
            _state.update { it.copy(isAppLocked = true) }
        }
        checkLockoutTimer()
    }

    private fun loadInitialState(): SecurityState {
        val isConfigured = prefs.getBoolean(KEY_LOCK_CONFIGURED, false)
        val lockTypeStr = prefs.getString(KEY_LOCK_TYPE, LockType.NONE.name) ?: LockType.NONE.name
        val lockType = try { LockType.valueOf(lockTypeStr) } catch (_: Exception) { LockType.NONE }
        val isBiometric = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        val timeoutSec = prefs.getLong(KEY_AUTOLOCK_TIMEOUT, AutoLockTimeout.ONE_MIN.seconds)
        val roleId = prefs.getString(KEY_CURRENT_ROLE, AppRole.MANAGER.id)
        val role = AppRole.fromId(roleId)
        val userName = prefs.getString(KEY_USER_NAME, "Manager") ?: "Manager"
        val hasApiKey = prefs.contains(KEY_ENCRYPTED_API_KEY)

        // Custom permissions
        val customMap = mutableMapOf<AviaryModule, PermissionLevel>()
        for (module in AviaryModule.entries) {
            val levelStr = prefs.getString(KEY_CUSTOM_PERM_PREFIX + module.key, null)
            if (levelStr != null) {
                try {
                    customMap[module] = PermissionLevel.valueOf(levelStr)
                } catch (_: Exception) {}
            }
        }

        return SecurityState(
            isLockConfigured = isConfigured,
            lockType = lockType,
            isAppLocked = isConfigured,
            isBiometricEnabled = isBiometric,
            autoLockTimeout = AutoLockTimeout.fromSeconds(timeoutSec),
            currentRole = role,
            currentUserName = userName,
            failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0),
            hasEncryptedApiKey = hasApiKey,
            customPermissions = customMap
        )
    }

    /**
     * Set or change App Lock (PIN or Password).
     */
    fun setupLock(type: LockType, secret: String): Boolean {
        if (type == LockType.NONE || secret.isBlank()) return false
        val salt = CryptoManager.generateSalt()
        val hash = CryptoManager.hashPassword(secret, salt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)

        prefs.edit {
            putBoolean(KEY_LOCK_CONFIGURED, true)
            putString(KEY_LOCK_TYPE, type.name)
            putString(KEY_SECRET_HASH, hash)
            putString(KEY_SECRET_SALT, saltBase64)
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putLong(KEY_LOCKOUT_UNTIL, 0L)
        }

        _state.update {
            it.copy(
                isLockConfigured = true,
                lockType = type,
                failedAttempts = 0,
                lockoutRemainingSeconds = 0
            )
        }

        logAudit("SECURITY_SETUP", "Configured app lock with ${type.name}", "SECURITY", "APP_LOCK")
        return true
    }

    /**
     * Disables app lock after verifying current secret.
     */
    fun removeLock(currentSecret: String): Boolean {
        if (!verifySecret(currentSecret)) return false
        prefs.edit {
            putBoolean(KEY_LOCK_CONFIGURED, false)
            putString(KEY_LOCK_TYPE, LockType.NONE.name)
            remove(KEY_SECRET_HASH)
            remove(KEY_SECRET_SALT)
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putLong(KEY_LOCKOUT_UNTIL, 0L)
        }

        _state.update {
            it.copy(
                isLockConfigured = false,
                lockType = LockType.NONE,
                isAppLocked = false,
                isBiometricEnabled = false,
                failedAttempts = 0,
                lockoutRemainingSeconds = 0
            )
        }

        logAudit("SECURITY_DISABLED", "App lock disabled by authenticated user", "SECURITY", "APP_LOCK")
        return true
    }

    /**
     * Validates secret against stored hash and salt.
     */
    private fun verifySecret(secret: String): Boolean {
        val storedHash = prefs.getString(KEY_SECRET_HASH, null) ?: return false
        val saltBase64 = prefs.getString(KEY_SECRET_SALT, null) ?: return false
        val salt = try { Base64.decode(saltBase64, Base64.NO_WRAP) } catch (_: Exception) { return false }
        val computedHash = CryptoManager.hashPassword(secret, salt)
        return storedHash == computedHash
    }

    /**
     * Unlock attempt using PIN/Password. Protects against brute force.
     */
    fun unlockWithSecret(secret: String): UnlockResult {
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (now < lockoutUntil) {
            val remainSec = ((lockoutUntil - now) / 1000).toInt() + 1
            return UnlockResult.LockedOut(remainSec)
        }

        val isValid = verifySecret(secret)
        if (isValid) {
            // Reset failed attempts
            prefs.edit {
                putInt(KEY_FAILED_ATTEMPTS, 0)
                putLong(KEY_LOCKOUT_UNTIL, 0L)
            }
            _state.update {
                it.copy(
                    isAppLocked = false,
                    failedAttempts = 0,
                    lockoutRemainingSeconds = 0
                )
            }
            logAudit("UNLOCK_SUCCESS", "App unlocked successfully via ${_state.value.lockType}", "SECURITY", "APP_LOCK")
            return UnlockResult.Success
        } else {
            val newAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            prefs.edit { putInt(KEY_FAILED_ATTEMPTS, newAttempts) }

            logAudit("UNLOCK_FAILED", "Failed unlock attempt ($newAttempts)", "SECURITY", "APP_LOCK")

            if (newAttempts >= MAX_ATTEMPTS) {
                val delayMs = calculateLockoutDurationMs(newAttempts)
                val lockUntil = System.currentTimeMillis() + delayMs
                prefs.edit { putLong(KEY_LOCKOUT_UNTIL, lockUntil) }
                startLockoutCountdown(delayMs / 1000)
                return UnlockResult.LockedOut((delayMs / 1000).toInt())
            }

            _state.update { it.copy(failedAttempts = newAttempts) }
            return UnlockResult.InvalidSecret(MAX_ATTEMPTS - newAttempts)
        }
    }

    /**
     * Unlock via Biometric authentication.
     */
    fun unlockWithBiometric(): Boolean {
        if (!_state.value.isBiometricEnabled) return false
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (now < lockoutUntil) return false

        prefs.edit {
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putLong(KEY_LOCKOUT_UNTIL, 0L)
        }
        _state.update {
            it.copy(
                isAppLocked = false,
                failedAttempts = 0,
                lockoutRemainingSeconds = 0
            )
        }
        logAudit("UNLOCK_BIOMETRIC", "App unlocked via Biometric authentication", "SECURITY", "APP_LOCK")
        return true
    }

    fun lockApp() {
        if (_state.value.isLockConfigured) {
            _state.update { it.copy(isAppLocked = true) }
            logAudit("LOCK_APP", "App manually locked", "SECURITY", "APP_LOCK")
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
        _state.update { it.copy(isBiometricEnabled = enabled) }
        logAudit("SECURITY_CONFIG", "Biometric unlock set to $enabled", "SECURITY", "BIOMETRIC")
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        prefs.edit { putLong(KEY_AUTOLOCK_TIMEOUT, timeout.seconds) }
        _state.update { it.copy(autoLockTimeout = timeout) }
        logAudit("SECURITY_CONFIG", "Auto-lock timeout set to ${timeout.titleEn}", "SECURITY", "AUTO_LOCK")
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = SystemClock.elapsedRealtime()
    }

    fun onAppForegrounded() {
        if (!_state.value.isLockConfigured || _state.value.isAppLocked) return
        val timeout = _state.value.autoLockTimeout
        if (timeout == AutoLockTimeout.NEVER) return

        val elapsedSec = (SystemClock.elapsedRealtime() - lastBackgroundTimestamp) / 1000
        if (elapsedSec >= timeout.seconds) {
            _state.update { it.copy(isAppLocked = true) }
            logAudit("AUTO_LOCK", "App locked automatically after background idle ($elapsedSec s)", "SECURITY", "AUTO_LOCK")
        }
    }

    // Role and Permissions
    fun switchRole(newRole: AppRole, userName: String = newRole.titleEn) {
        prefs.edit {
            putString(KEY_CURRENT_ROLE, newRole.id)
            putString(KEY_USER_NAME, userName)
        }
        _state.update {
            it.copy(
                currentRole = newRole,
                currentUserName = userName
            )
        }
        logAudit("ROLE_SWITCH", "User switched role to ${newRole.name} ($userName)", "SECURITY", "ROLES")
    }

    fun updateCustomPermission(module: AviaryModule, level: PermissionLevel) {
        prefs.edit {
            putString(KEY_CUSTOM_PERM_PREFIX + module.key, level.name)
        }
        val updated = _state.value.customPermissions.toMutableMap()
        updated[module] = level
        _state.update { it.copy(customPermissions = updated) }
    }

    fun getEffectivePermissions(): RolePermissions {
        val currentRole = _state.value.currentRole
        if (currentRole == AppRole.CUSTOM) {
            val merged = AviaryModule.entries.associateWith { module ->
                _state.value.customPermissions[module] ?: PermissionLevel.READ_ONLY
            }
            return RolePermissions(AppRole.CUSTOM, merged)
        }
        return RolePermissions.defaultPermissions(currentRole)
    }

    // Secure API Key Vault (Encrypted using Keystore AES)
    fun saveEncryptedApiKey(key: String) {
        if (key.isBlank()) {
            prefs.edit { remove(KEY_ENCRYPTED_API_KEY) }
            _state.update { it.copy(hasEncryptedApiKey = false) }
        } else {
            val encrypted = CryptoManager.encrypt(key.trim())
            prefs.edit { putString(KEY_ENCRYPTED_API_KEY, encrypted) }
            _state.update { it.copy(hasEncryptedApiKey = true) }
        }
        logAudit("API_KEY_UPDATE", "Gemini API Key securely updated in Keystore vault", "SECURITY", "VAULT")
    }

    fun getEncryptedApiKey(): String {
        val encrypted = prefs.getString(KEY_ENCRYPTED_API_KEY, null) ?: return ""
        return CryptoManager.decrypt(encrypted)
    }

    // Sensitive Action Confirmation Challenge
    fun verifySensitiveAction(confirmationInput: String): Boolean {
        // If app lock is configured, must match PIN/Password
        if (_state.value.isLockConfigured) {
            return verifySecret(confirmationInput)
        }
        // If no lock configured, requiring explicit word "CONFIRM" or "تایید"
        return confirmationInput.equals("CONFIRM", ignoreCase = true) ||
                confirmationInput.trim() == "تایید"
    }

    // Audit Logging Helper
    fun logAudit(action: String, summary: String, entityType: String, entityId: String) {
        scope.launch {
            try {
                // Strictly sanitize to avoid logging secrets, passwords, or keys
                val cleanSummary = CryptoManager.sanitizeAuditText(summary)
                auditLogDao.insertLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "${_state.value.currentUserName} (${_state.value.currentRole.name})",
                        actionType = action,
                        entityType = entityType,
                        entityId = entityId,
                        timestamp = System.currentTimeMillis(),
                        summary = cleanSummary
                    )
                )
            } catch (_: Exception) {}
        }
    }

    private fun calculateLockoutDurationMs(attempts: Int): Long {
        return when {
            attempts <= 5 -> 30_000L // 30 sec
            attempts <= 7 -> 60_000L // 1 min
            attempts <= 9 -> 300_000L // 5 min
            else -> 900_000L // 15 min
        }
    }

    private fun checkLockoutTimer() {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            startLockoutCountdown((lockoutUntil - now) / 1000)
        }
    }

    private fun startLockoutCountdown(seconds: Long) {
        lockoutJob?.cancel()
        lockoutJob = scope.launch {
            var remain = seconds.toInt()
            while (remain > 0) {
                _state.update { it.copy(lockoutRemainingSeconds = remain) }
                delay(1000)
                remain--
            }
            _state.update { it.copy(lockoutRemainingSeconds = 0) }
            prefs.edit { putLong(KEY_LOCKOUT_UNTIL, 0L) }
        }
    }

    sealed interface UnlockResult {
        data object Success : UnlockResult
        data class InvalidSecret(val attemptsRemaining: Int) : UnlockResult
        data class LockedOut(val waitSeconds: Int) : UnlockResult
    }

    companion object {
        private const val PREFS_NAME = "aviary_security_prefs"
        private const val KEY_LOCK_CONFIGURED = "lock_configured"
        private const val KEY_LOCK_TYPE = "lock_type"
        private const val KEY_SECRET_HASH = "secret_hash"
        private const val KEY_SECRET_SALT = "secret_salt"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTOLOCK_TIMEOUT = "autolock_timeout"
        private const val KEY_CURRENT_ROLE = "current_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_ENCRYPTED_API_KEY = "encrypted_api_key"
        private const val KEY_CUSTOM_PERM_PREFIX = "perm_"

        const val MAX_ATTEMPTS = 5

        @Volatile
        private var INSTANCE: SecurityManager? = null

        fun getInstance(context: Context, auditLogDao: AuditLogDao): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityManager(context.applicationContext, auditLogDao)
                INSTANCE = instance
                instance
            }
        }
    }
}
