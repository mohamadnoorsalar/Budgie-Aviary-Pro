package com.example.feature.backup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.backup.AutoBackupSnapshot
import com.example.core.backup.BackupInspection
import com.example.core.backup.BackupManager
import com.example.core.backup.RestoreMode
import com.example.core.localization.AppLanguage
import com.example.core.sync.DeviceTransferManager
import com.example.core.sync.NetworkMonitor
import com.example.core.sync.SyncManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    backupManager: BackupManager,
    syncManager: SyncManager,
    deviceTransferManager: DeviceTransferManager,
    networkMonitor: NetworkMonitor,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onNavigateToConflicts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val networkState by networkMonitor.networkState.collectAsStateWithLifecycle()
    val syncStatus by syncManager.syncStatus.collectAsStateWithLifecycle()
    val unresolvedConflictsCount by syncManager.getUnresolvedConflictCount().collectAsStateWithLifecycle(initialValue = 0)
    val pendingOperationsCount by syncManager.getPendingCount().collectAsStateWithLifecycle(initialValue = 0)

    val tabs = listOf(
        if (isFa) "پشتیبان‌گیری رمزدار" else "Encrypted Backup",
        if (isFa) "بازیابی امن" else "Safe Restore",
        if (isFa) "پشتیبان خودکار" else "Auto-Backup",
        if (isFa) "انتقال بین دستگاه‌ها" else "Device Sync"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("backup_restore_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isFa) "پشتیبان‌گیری، بازیابی و همگام‌سازی" else "Backup, Restore & Sync",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (networkState.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (networkState.isOnline)
                                    (if (isFa) "آنلاین - آماده همگام‌سازی" else "Online - Ready to sync")
                                else
                                    (if (isFa) "حالت آفلاین - ذخیره محلی امن" else "Offline Mode - Operating Locally"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (unresolvedConflictsCount > 0) {
                        IconButton(onClick = onNavigateToConflicts) {
                            BadgedBox(
                                badge = {
                                    Badge { Text(unresolvedConflictsCount.toString()) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Difference,
                                    contentDescription = "Conflicts",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ManualBackupTab(
                    backupManager = backupManager,
                    isFa = isFa,
                    context = context,
                    onShowMessage = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                1 -> RestoreTab(
                    backupManager = backupManager,
                    isFa = isFa,
                    onShowMessage = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                2 -> AutoBackupTab(
                    backupManager = backupManager,
                    isFa = isFa,
                    onShowMessage = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                3 -> DeviceSyncTab(
                    syncManager = syncManager,
                    deviceTransferManager = deviceTransferManager,
                    networkState = networkState,
                    syncStatus = syncStatus,
                    pendingCount = pendingOperationsCount,
                    unresolvedConflictsCount = unresolvedConflictsCount,
                    isFa = isFa,
                    context = context,
                    onNavigateToConflicts = onNavigateToConflicts,
                    onShowMessage = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            }
        }
    }
}

@Composable
fun ManualBackupTab(
    backupManager: BackupManager,
    isFa: Boolean,
    context: Context,
    onShowMessage: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedBackupPayload by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFa) "رمزگذاری استاندارد AES-256-GCM" else "AES-256-GCM Backup Encryption",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFa)
                            "تمامی پرونده‌های پرندگان، جفت‌ها، جوجه‌کشی، درمان‌ها و امور مالی در قالب یک بسته رمزدار امن با هش PBKDF2 و بررسی یکپارچگی SHA-256 صادر می‌شود."
                        else
                            "All birds, pairs, clutches, treatments, and finances will be exported into an encrypted bundle with PBKDF2 key derivation and SHA-256 integrity checksum.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFa) "تنظیم گذرواژه پشتیبان" else "Set Backup Password",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isFa) "گذرواژه فایل پشتیبان (حداقل ۴ کاراکتر)" else "Backup Password (min 4 chars)") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(if (isFa) "تکرار گذرواژه" else "Confirm Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (password.length < 4) {
                                onShowMessage(if (isFa) "گذرواژه باید حداقل ۴ کاراکتر باشد" else "Password must be at least 4 characters")
                                return@Button
                            }
                            if (password != confirmPassword) {
                                onShowMessage(if (isFa) "گذرواژه‌ها یکسان نیستند" else "Passwords do not match")
                                return@Button
                            }

                            coroutineScope.launch {
                                isGenerating = true
                                try {
                                    val result = backupManager.createEncryptedBackup(password)
                                    generatedBackupPayload = result
                                    onShowMessage(if (isFa) "پشتیبان رمزدار با موفقیت ایجاد شد" else "Encrypted backup created successfully")
                                } catch (e: Exception) {
                                    onShowMessage("Error: ${e.localizedMessage}")
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating && password.isNotEmpty() && confirmPassword.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFa) "ایجاد و ذخیره پشتیبان رمزدار" else "Generate Encrypted Backup")
                    }
                }
            }
        }

        if (generatedBackupPayload != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFa) "فایل پشتیبان آماده است" else "Encrypted Backup Ready",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFa)
                                "این متن یا فایل حاوی تمامی داده‌های رمزگذاری‌شده سالن شماست. آن را در جای امن نگهداری کنید."
                            else
                                "This encrypted payload contains all facility records. Store it securely.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Aviary Backup", generatedBackupPayload))
                                    onShowMessage(if (isFa) "کد رمزدار در کلیپ‌بورد کپی شد" else "Encrypted payload copied to clipboard")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFa) "کپی کد" else "Copy")
                            }

                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, generatedBackupPayload)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Export Aviary Backup")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFa) "اشتراک‌گذاری" else "Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestoreTab(
    backupManager: BackupManager,
    isFa: Boolean,
    onShowMessage: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var backupDataInput by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var restorePasswordVisible by remember { mutableStateOf(false) }

    var isInspecting by remember { mutableStateOf(false) }
    var inspectionResult by remember { mutableStateOf<BackupInspection?>(null) }
    var selectedRestoreMode by remember { mutableStateOf(RestoreMode.SMART_MERGE) }
    var isRestoring by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFa) "ورود اطلاعات پشتیبان رمزدار" else "Enter Encrypted Backup Data",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = backupDataInput,
                        onValueChange = {
                            backupDataInput = it
                            inspectionResult = null
                        },
                        label = { Text(if (isFa) "متن رمزدار پشتیبان (.aviary یا Base64)" else "Encrypted Backup Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = {
                            restorePassword = it
                            inspectionResult = null
                        },
                        label = { Text(if (isFa) "گذرواژه رمزگشایی" else "Decryption Password") },
                        visualTransformation = if (restorePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { restorePasswordVisible = !restorePasswordVisible }) {
                                Icon(
                                    imageVector = if (restorePasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (backupDataInput.isBlank() || restorePassword.isBlank()) {
                                onShowMessage(if (isFa) "لطفاً اطلاعات و گذرواژه را وارد کنید" else "Please enter backup data and password")
                                return@Button
                            }

                            coroutineScope.launch {
                                isInspecting = true
                                val result = backupManager.inspectEncryptedBackup(backupDataInput.trim(), restorePassword)
                                isInspecting = false
                                inspectionResult = result

                                if (!result.isValid) {
                                    onShowMessage(result.errorMessage ?: (if (isFa) "گذرواژه اشتباه است یا فایل مخدوش است" else "Invalid password or corrupted file"))
                                }
                            }
                        },
                        enabled = !isInspecting && backupDataInput.isNotBlank() && restorePassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isInspecting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFa) "بررسی صحت و محتوای پشتیبان" else "Inspect & Validate Backup")
                    }
                }
            }
        }

        if (inspectionResult != null && inspectionResult!!.isValid) {
            val meta = inspectionResult!!.metadata!!

            // Older Data Overwrite Warning Card
            if (inspectionResult!!.isOlderThanCurrent) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isFa) "هشدار مهم: تاریخ این پشتیبان قدیمی‌تر است!" else "Warning: Older Backup Detected!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isFa)
                                        "اطلاعات موجود روی دستگاه جدیدتر از این فایل پشتیبان است. بازنویسی کامل ممکن است تغییرات اخیر را حذف کند. استفاده از «ادغام هوشمند» توصیه اکید می‌شود."
                                    else
                                        "Records on this device are newer than this backup. A full replacement will overwrite newer entries. Smart Merge is strongly recommended.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFa) "خلاصه محتوای فایل پشتیبان" else "Backup Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "تاریخ پشتیبان‌گیری:" else "Backup Date:")
                            Text(dateFormatter.format(Date(meta.backupTimestamp)), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "تعداد پرندگان:" else "Total Birds:")
                            Text("${meta.totalBirds}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "تعداد جفت‌ها:" else "Total Pairs:")
                            Text("${meta.totalPairs}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isFa) "دستگاه مبدا:" else "Source Device:")
                            Text(meta.deviceName.ifEmpty { meta.deviceId }, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isFa) "روش بازیابی:" else "Restore Mode:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedRestoreMode == RestoreMode.SMART_MERGE,
                                onClick = { selectedRestoreMode = RestoreMode.SMART_MERGE },
                                label = { Text(if (isFa) "ادغام هوشمند (امن)" else "Smart Merge (Safe)") },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = selectedRestoreMode == RestoreMode.REPLACE_ALL,
                                onClick = { selectedRestoreMode = RestoreMode.REPLACE_ALL },
                                label = { Text(if (isFa) "جایگزینی کامل" else "Replace All") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isRestoring = true
                                    val res = backupManager.restoreBackup(
                                        encryptedData = backupDataInput.trim(),
                                        password = restorePassword,
                                        mode = selectedRestoreMode
                                    )
                                    isRestoring = false
                                    if (res.isSuccess) {
                                        onShowMessage(
                                            if (isFa)
                                                "بازیابی با موفقیت انجام شد (${res.insertedCount} رکورد جدید، ${res.updatedCount} به‌روزرسانی)"
                                            else
                                                "Restore successful (${res.insertedCount} inserted, ${res.updatedCount} updated)"
                                        )
                                        backupDataInput = ""
                                        restorePassword = ""
                                        inspectionResult = null
                                    } else {
                                        onShowMessage(res.message)
                                    }
                                }
                            },
                            enabled = !isRestoring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRestoreMode == RestoreMode.REPLACE_ALL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                if (selectedRestoreMode == RestoreMode.REPLACE_ALL)
                                    (if (isFa) "تایید و جایگزینی کامل کل پایگاه داده" else "Confirm Full Replace")
                                else
                                    (if (isFa) "ادغام امن داده‌ها" else "Start Smart Merge")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoBackupTab(
    backupManager: BackupManager,
    isFa: Boolean,
    onShowMessage: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(backupManager.isAutoBackupEnabled) }
    var freqDays by remember { mutableIntStateOf(backupManager.autoBackupFrequencyDays) }
    var snapshots by remember { mutableStateOf(backupManager.listAutoSnapshots()) }
    var isCreatingSnapshot by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isFa) "پشتیبان‌گیری خودکار روزانه" else "Automatic Daily Backups",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isFa) "ایجاد خودکار نسخه‌های پشتیبان در حافظه محلی دستگاه با نگهداری ۷ نسخه اخیر." else "Creates automatic encrypted snapshots with 7-day retention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                backupManager.isAutoBackupEnabled = it
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isCreatingSnapshot = true
                                val file = backupManager.createAutoSnapshot()
                                isCreatingSnapshot = false
                                if (file != null) {
                                    snapshots = backupManager.listAutoSnapshots()
                                    onShowMessage(if (isFa) "نسخه فوری با موفقیت ایجاد شد" else "Snapshot created successfully")
                                }
                            }
                        },
                        enabled = !isCreatingSnapshot && isEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCreatingSnapshot) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFa) "ایجاد نسخه پشتیبان فوری" else "Create Instant Snapshot")
                    }
                }
            }
        }

        item {
            Text(
                text = if (isFa) "نسخه‌های پشتیبان محلی ذخیره شده (${snapshots.size})" else "Local Auto-Snapshots (${snapshots.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (snapshots.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isFa) "هنوز هیچ نسخه خودکاری ایجاد نشده است" else "No automatic snapshots created yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(snapshots, key = { it.fileName }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.formattedDate,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${item.sizeBytes / 1024} KB • ${item.fileName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val content = item.file.readText()
                                    val res = backupManager.restoreBackup(content, "AviaryAutoBackupPass_2026", RestoreMode.SMART_MERGE)
                                    if (res.isSuccess) {
                                        onShowMessage(if (isFa) "نسخه پشتیبان با موفقیت ادغام شد" else "Snapshot merged successfully")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isFa) "بازیابی" else "Restore", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceSyncTab(
    syncManager: SyncManager,
    deviceTransferManager: DeviceTransferManager,
    networkState: com.example.core.sync.NetworkState,
    syncStatus: com.example.core.sync.SyncStatus,
    pendingCount: Int,
    unresolvedConflictsCount: Int,
    isFa: Boolean,
    context: Context,
    onNavigateToConflicts: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var generatedTransferCode by remember { mutableStateOf<String?>(null) }
    var generatedPackageData by remember { mutableStateOf<String?>(null) }
    var isGeneratingTransfer by remember { mutableStateOf(false) }

    var inputPairingCode by remember { mutableStateOf("") }
    var inputPackageData by remember { mutableStateOf("") }
    var isImportingTransfer by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Network Card
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
                                imageVector = if (networkState.isOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                                contentDescription = null,
                                tint = if (networkState.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (networkState.isOnline)
                                        (if (isFa) "اتصال اینترنت فعال (${networkState.connectionType})" else "Online (${networkState.connectionType})")
                                    else
                                        (if (isFa) "آفلاین - عملکرد بدون اینترنت" else "Offline Mode"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isFa) "شناسه دستگاه: ${syncManager.deviceId}" else "Device ID: ${syncManager.deviceId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (unresolvedConflictsCount > 0) {
                            OutlinedButton(
                                onClick = onNavigateToConflicts,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(if (isFa) "تداخل‌ها ($unresolvedConflictsCount)" else "Conflicts ($unresolvedConflictsCount)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFa) "تغییرات در صف همگام‌سازی: $pendingCount" else "Pending Sync Queue: $pendingCount",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = syncManager.syncPendingQueue()
                                    if (ok) {
                                        onShowMessage(if (isFa) "همگام‌سازی با موفقیت انجام شد" else "Sync completed successfully")
                                    } else {
                                        onShowMessage(syncStatus.syncMessage ?: (if (isFa) "در حالت آفلاین همگام‌سازی مقدور نیست" else "Cannot sync while offline"))
                                    }
                                }
                            },
                            enabled = !syncStatus.isSyncing,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (syncStatus.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(imageVector = Icons.Filled.Sync, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(if (isFa) "همگام‌سازی اکنون" else "Sync Now")
                        }
                    }
                }
            }
        }

        // Direct Device-to-Device Send Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFa) "ارسال مستقیم به دستگاه دیگر" else "Send to Another Device",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFa)
                            "ایجاد بسته انتقال رمزگذاری‌شده با کد جفت‌سازی یک‌بارمصرف ۶ رقمی جهت انتقال به گوشی یا تبلت دیگر در سالن."
                        else
                            "Generate a one-time 6-digit pairing code package to securely transfer aviary records to a phone or tablet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingTransfer = true
                                val pairingCode = deviceTransferManager.generatePairingCode()
                                val pkg = deviceTransferManager.createTransferPackage(pairingCode)
                                generatedTransferCode = pairingCode
                                generatedPackageData = pkg
                                isGeneratingTransfer = false
                            }
                        },
                        enabled = !isGeneratingTransfer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingTransfer) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.Devices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFa) "تولید بسته انتقال با کد جفت‌سازی" else "Generate Transfer Package")
                    }

                    if (generatedTransferCode != null && generatedPackageData != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isFa) "کد جفت‌سازی ۶ رقمی دستگاه:" else "6-Digit Pairing Code:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = generatedTransferCode!!,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Device Transfer", generatedPackageData))
                                        onShowMessage(if (isFa) "بسته انتقال در کلیپ‌بورد کپی شد" else "Transfer package copied")
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isFa) "کپی بسته انتقال" else "Copy Transfer Data")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Direct Device-to-Device Receive Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFa) "دریافت از دستگاه دیگر" else "Receive from Another Device",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputPairingCode,
                        onValueChange = { inputPairingCode = it },
                        label = { Text(if (isFa) "کد جفت‌سازی ۶ رقمی" else "6-Digit Pairing Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputPackageData,
                        onValueChange = { inputPackageData = it },
                        label = { Text(if (isFa) "بسته انتقال دریافتی (JSON)" else "Transfer Package Payload") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (inputPairingCode.isBlank() || inputPackageData.isBlank()) {
                                onShowMessage(if (isFa) "کد جفت‌سازی و بسته انتقال را وارد کنید" else "Please enter code and package")
                                return@Button
                            }

                            coroutineScope.launch {
                                isImportingTransfer = true
                                val res = deviceTransferManager.importTransferPackage(
                                    packageJson = inputPackageData.trim(),
                                    pairingCode = inputPairingCode.trim(),
                                    mode = RestoreMode.SMART_MERGE
                                )
                                isImportingTransfer = false
                                if (res.isSuccess) {
                                    onShowMessage(
                                        if (isFa)
                                            "انتقال داده‌ها با موفقیت انجام شد (${res.insertedCount} جدید، ${res.updatedCount} به‌روزرسانی)"
                                        else
                                            "Transfer imported successfully (${res.insertedCount} inserted, ${res.updatedCount} updated)"
                                    )
                                    inputPairingCode = ""
                                    inputPackageData = ""
                                } else {
                                    onShowMessage(res.message)
                                }
                            }
                        },
                        enabled = !isImportingTransfer && inputPairingCode.isNotBlank() && inputPackageData.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isImportingTransfer) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isFa) "رمزگشایی و ادغام اطلاعات" else "Decrypt & Merge Data")
                    }
                }
            }
        }
    }
}
