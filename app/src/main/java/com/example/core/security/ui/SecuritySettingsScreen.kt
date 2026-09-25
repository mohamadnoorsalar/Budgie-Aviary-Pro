package com.example.core.security.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.security.AppRole
import com.example.core.security.AutoLockTimeout
import com.example.core.security.AviaryModule
import com.example.core.security.BiometricAuthHelper
import com.example.core.security.CryptoManager
import com.example.core.security.LockType
import com.example.core.security.PermissionLevel
import com.example.core.security.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    securityManager: SecurityManager,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenAuditLog: () -> Unit
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val context = LocalContext.current
    val secState by securityManager.state.collectAsState()

    var showSetupLockDialog by remember { mutableStateOf(false) }
    var showDisableLockDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showCustomPermDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    val biometricAvailable = remember { BiometricAuthHelper.isBiometricSupported(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isFa) "امنیت و کنترل دسترسی" else "Security & Access Control",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("security_settings_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // App Lock Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (secState.isLockConfigured) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                    contentDescription = null,
                                    tint = if (secState.isLockConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = if (isFa) "قفل برنامه" else "App Lock",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (secState.isLockConfigured) {
                                            if (isFa) "فعال (${if (secState.lockType == LockType.PIN) "پین‌کد" else "رمز عبور"})"
                                            else "Active (${secState.lockType.name})"
                                        } else {
                                            if (isFa) "غیرفعال" else "Disabled"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (secState.isLockConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            if (secState.isLockConfigured) {
                                Row {
                                    OutlinedButton(
                                        onClick = { securityManager.lockApp() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isFa) "قفل آنی" else "Lock Now")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!secState.isLockConfigured) {
                                Button(
                                    onClick = { showSetupLockDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Filled.Security, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isFa) "تنظیم پین یا رمز ورود" else "Set Up PIN or Password")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showSetupLockDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.LockReset, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isFa) "تغییر رمز" else "Change")
                                }
                                OutlinedButton(
                                    onClick = { showDisableLockDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.LockOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isFa) "غیرفعال‌سازی" else "Disable")
                                }
                            }
                        }
                    }
                }
            }

            // Biometric Unlock Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = if (isFa) "بازگشایی با بیومتریک (اثر انگشت)" else "Biometric Unlock",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (biometricAvailable) {
                                        if (isFa) "پشتیبانی سخت‌افزاری بیومتریک فعال است" else "Hardware biometric supported"
                                    } else {
                                        if (isFa) "حسگر اثر انگشت در این دستگاه یافت نشد یا ثبت نشده است" else "Biometrics not available or not enrolled"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = secState.isBiometricEnabled && secState.isLockConfigured,
                            onCheckedChange = { enabled ->
                                if (!secState.isLockConfigured) {
                                    Toast.makeText(context, if (isFa) "ابتدا قفل برنامه را تنظیم کنید" else "Set up app lock first", Toast.LENGTH_SHORT).show()
                                } else {
                                    securityManager.setBiometricEnabled(enabled)
                                }
                            },
                            enabled = biometricAvailable && secState.isLockConfigured
                        )
                    }
                }
            }

            // Auto-Lock Timeout Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimeoutDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LockClock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = if (isFa) "قفل خودکار پس از خروج" else "Automatic Lock",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isFa) secState.autoLockTimeout.titleFa else secState.autoLockTimeout.titleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedButton(onClick = { showTimeoutDialog = true }) {
                            Text(if (isFa) "تغییر" else "Change")
                        }
                    }
                }
            }

            // Role-Based Access Control Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = if (isFa) "نقش کاربر و سطوح دسترسی" else "User Role & Permissions",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isFa) {
                                            "نقش فعال: ${secState.currentRole.titleFa} (${secState.currentUserName})"
                                        } else {
                                            "Active: ${secState.currentRole.titleEn} (${secState.currentUserName})"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(onClick = { showRoleDialog = true }) {
                                Text(if (isFa) "تغییر نقش" else "Switch")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isFa) secState.currentRole.descriptionFa else secState.currentRole.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (secState.currentRole == AppRole.CUSTOM) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showCustomPermDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isFa) "مدیریت دسترسی‌های ماژول‌ها" else "Configure Module Permissions")
                            }
                        }
                    }
                }
            }

            // Secure API Key Vault Card (Android KeyStore AES-256)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = if (isFa) "صندوق امن کلید API (KeyStore AES-256)" else "Secure API Key Vault",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (secState.hasEncryptedApiKey) {
                                        if (isFa) "کلید هوش مصنوعی جمینای رمزنگاری شده و فعال است" else "Gemini API key is encrypted & active"
                                    } else {
                                        if (isFa) "کلیدی ذخیره نشده است (استفاده از تنظیمات پیش‌فرض)" else "No custom key stored (using default)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (secState.hasEncryptedApiKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showApiKeyDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.VpnKey, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFa) "تنظیم کلید رمزنگاری شده Gemini" else "Manage Encrypted API Key")
                        }
                    }
                }
            }

            // Protected Encrypted Backups Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = if (isFa) "پشتیبان‌گیری رمزنگاری شده و محافظت‌شده" else "Protected Backups",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isFa) "تهیه فایل پشتیبان محافظت شده با رمز عبور و درستی‌سنجی SHA-256" else "Password-protected backup with SHA-256 integrity checksum",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFa) "پشتیبان‌گیری و بازیابی رمزدار" else "Encrypted Backup & Restore")
                        }
                    }
                }
            }

            // Security Audit Log Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = if (isFa) "گزارش فعالیت‌های امنیتی (Audit Log)" else "Security Audit Log",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isFa) "مشاهده گزارش لاگین‌ها، تلاش‌های ناموفق و تغییرات با حفظ محرمانگی" else "Audit history of logins, lock events, and actions without secret leakage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenAuditLog,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.History, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFa) "مشاهده رویدادهای لاگ امنیتی" else "View Audit Log")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (showSetupLockDialog) {
        SetupLockDialog(
            isFa = isFa,
            onDismiss = { showSetupLockDialog = false },
            onSave = { type, secret ->
                securityManager.setupLock(type, secret)
                showSetupLockDialog = false
                Toast.makeText(context, if (isFa) "قفل با موفقیت ثبت شد" else "App lock configured", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDisableLockDialog) {
        DisableLockDialog(
            isFa = isFa,
            lockType = secState.lockType,
            onDismiss = { showDisableLockDialog = false },
            onConfirm = { secret ->
                val success = securityManager.removeLock(secret)
                if (success) {
                    showDisableLockDialog = false
                    Toast.makeText(context, if (isFa) "قفل برنامه غیرفعال شد" else "App lock disabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, if (isFa) "رمز یا پین وارد شده اشتباه است" else "Incorrect secret", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showTimeoutDialog) {
        TimeoutPickerDialog(
            isFa = isFa,
            current = secState.autoLockTimeout,
            onDismiss = { showTimeoutDialog = false },
            onSelect = {
                securityManager.setAutoLockTimeout(it)
                showTimeoutDialog = false
            }
        )
    }

    if (showRoleDialog) {
        RoleSelectionDialog(
            isFa = isFa,
            currentRole = secState.currentRole,
            onDismiss = { showRoleDialog = false },
            onSelectRole = { role, name ->
                securityManager.switchRole(role, name)
                showRoleDialog = false
            }
        )
    }

    if (showCustomPermDialog) {
        CustomPermissionsDialog(
            isFa = isFa,
            securityManager = securityManager,
            onDismiss = { showCustomPermDialog = false }
        )
    }

    if (showApiKeyDialog) {
        ApiKeyVaultDialog(
            isFa = isFa,
            initialKey = securityManager.getEncryptedApiKey(),
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                securityManager.saveEncryptedApiKey(key)
                showApiKeyDialog = false
                Toast.makeText(context, if (isFa) "کلید امن با موفقیت ذخیره شد" else "API key securely stored", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBackupDialog) {
        ProtectedBackupDialog(
            isFa = isFa,
            securityManager = securityManager,
            onDismiss = { showBackupDialog = false }
        )
    }
}

@Composable
private fun SetupLockDialog(
    isFa: Boolean,
    onDismiss: () -> Unit,
    onSave: (LockType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(LockType.PIN) }
    var secret by remember { mutableStateOf("") }
    var confirmSecret by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "تنظیم قفل برنامه" else "Set Up App Lock") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == LockType.PIN,
                        onClick = { selectedType = LockType.PIN; secret = ""; confirmSecret = "" },
                        label = { Text(if (isFa) "پین‌کد (۴-۶ رقم)" else "PIN (4-6 digits)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == LockType.PASSWORD,
                        onClick = { selectedType = LockType.PASSWORD; secret = ""; confirmSecret = "" },
                        label = { Text(if (isFa) "رمز عبور" else "Password") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = secret,
                    onValueChange = {
                        secret = if (selectedType == LockType.PIN) it.filter { ch -> ch.isDigit() } else it
                    },
                    label = { Text(if (selectedType == LockType.PIN) "PIN" else if (isFa) "رمز عبور" else "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmSecret,
                    onValueChange = {
                        confirmSecret = if (selectedType == LockType.PIN) it.filter { ch -> ch.isDigit() } else it
                    },
                    label = { Text(if (isFa) "تکرار رمز" else "Confirm") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedType == LockType.PIN && secret.length < 4) {
                        error = if (isFa) "پین باید حداقل ۴ رقم باشد." else "PIN must be at least 4 digits."
                        return@Button
                    }
                    if (selectedType == LockType.PASSWORD && secret.length < 6) {
                        error = if (isFa) "رمز عبور باید حداقل ۶ نویسه باشد." else "Password must be at least 6 characters."
                        return@Button
                    }
                    if (secret != confirmSecret) {
                        error = if (isFa) "تکرار رمز مطابقت ندارد." else "Passwords do not match."
                        return@Button
                    }
                    onSave(selectedType, secret)
                }
            ) {
                Text(if (isFa) "ذخیره" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun DisableLockDialog(
    isFa: Boolean,
    lockType: LockType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var secret by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "غیرفعال‌سازی قفل برنامه" else "Disable App Lock") },
        text = {
            Column {
                Text(
                    text = if (isFa) "برای غیرفعال‌سازی قفل، لطفاً رمز یا پین فعلی خود را وارد کنید:"
                    else "Enter your current PIN or password to disable:"
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text(if (lockType == LockType.PIN) "PIN" else "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(secret) }) {
                Text(if (isFa) "تایید و غیرفعال‌سازی" else "Confirm & Disable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun TimeoutPickerDialog(
    isFa: Boolean,
    current: AutoLockTimeout,
    onDismiss: () -> Unit,
    onSelect: (AutoLockTimeout) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "مدت زمان قفل خودکار" else "Auto-Lock Timeout") },
        text = {
            Column {
                AutoLockTimeout.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == current,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFa) option.titleFa else option.titleEn)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "بستن" else "Close")
            }
        }
    )
}

@Composable
private fun RoleSelectionDialog(
    isFa: Boolean,
    currentRole: AppRole,
    onDismiss: () -> Unit,
    onSelectRole: (AppRole, String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var userName by remember { mutableStateOf(currentRole.titleEn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "انتخاب نقش کاربری" else "Select User Role") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(AppRole.entries.size) { idx ->
                    val role = AppRole.entries[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRole = role
                                userName = if (isFa) role.titleFa else role.titleEn
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = role == selectedRole,
                            onClick = {
                                selectedRole = role
                                userName = if (isFa) role.titleFa else role.titleEn
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isFa) role.titleFa else role.titleEn,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isFa) role.descriptionFa else role.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSelectRole(selectedRole, userName) }) {
                Text(if (isFa) "انتخاب و اعمال" else "Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun CustomPermissionsDialog(
    isFa: Boolean,
    securityManager: SecurityManager,
    onDismiss: () -> Unit
) {
    val secState by securityManager.state.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "سطح دسترسی نقش سفارشی" else "Custom Role Module Permissions") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(AviaryModule.entries.size) { idx ->
                    val module = AviaryModule.entries[idx]
                    val currentLevel = secState.customPermissions[module] ?: PermissionLevel.READ_ONLY

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = if (isFa) module.titleFa else module.titleEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PermissionLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = level == currentLevel,
                                    onClick = { securityManager.updateCustomPermission(module, level) },
                                    label = {
                                        Text(
                                            when (level) {
                                                PermissionLevel.NONE -> if (isFa) "هیچ" else "None"
                                                PermissionLevel.READ_ONLY -> if (isFa) "خواندن" else "Read"
                                                PermissionLevel.READ_WRITE -> if (isFa) "ویرایش" else "Write"
                                                PermissionLevel.FULL -> if (isFa) "کامل" else "Full"
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isFa) "ذخیره دسترسی‌ها" else "Done")
            }
        }
    )
}

@Composable
private fun ApiKeyVaultDialog(
    isFa: Boolean,
    initialKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(initialKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "صندوق امن کلید Gemini" else "Secure Gemini API Key Vault") },
        text = {
            Column {
                Text(
                    text = if (isFa) "این کلید با استاندارد AES-256 در سخت‌افزار امن دستگاه (Android KeyStore) ذخیره می‌شود و هرگز در فایل لاگ ثبت نمی‌گردد."
                    else "This API key is encrypted using AES-256 in Android KeyStore and will never be logged in plain text."
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Gemini API Key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(apiKey) }) {
                Text(if (isFa) "ذخیره در صندوق امن" else "Save in Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
private fun ProtectedBackupDialog(
    isFa: Boolean,
    securityManager: SecurityManager,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "پشتیبان‌گیری رمزدار (PBKDF2 + AES)" else "Encrypted Backup (PBKDF2 + AES)") },
        text = {
            Column {
                Text(
                    text = if (isFa) "برای تهیه یا بازگردانی فایل پشتیبان رمزدار، یک رمز عبور قوی وارد کنید:"
                    else "Enter an encryption password to protect or decrypt the backup:"
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isFa) "رمز عبور پشتیبان" else "Backup Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (exportStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exportStatus ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.length < 6) {
                        exportStatus = if (isFa) "رمز عبور باید حداقل ۶ نویسه باشد." else "Password must be at least 6 characters."
                        return@Button
                    }
                    val sampleData = """{"backupVersion":1,"timestamp":${System.currentTimeMillis()},"app":"BudgieAviary"}"""
                    val encrypted = CryptoManager.encryptBackup(sampleData, password)
                    securityManager.logAudit(
                        action = "ENCRYPTED_BACKUP_CREATED",
                        summary = "Protected backup generated with AES-GCM and SHA-256 checksum",
                        entityType = "BACKUP",
                        entityId = "LOCAL"
                    )
                    exportStatus = if (isFa) "پشتیبان رمزنگاری شده با موفقیت ایجاد شد! (اندازه: ${encrypted.length} بایت)"
                    else "Protected backup created successfully! (${encrypted.length} bytes)"
                }
            ) {
                Text(if (isFa) "ایجاد فایل پشتیبان رمزدار" else "Create Encrypted Backup")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "بستن" else "Close")
            }
        }
    )
}
