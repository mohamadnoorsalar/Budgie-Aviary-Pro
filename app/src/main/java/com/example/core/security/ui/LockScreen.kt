package com.example.core.security.ui

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.security.BiometricAuthHelper
import com.example.core.security.LockType
import com.example.core.security.SecurityManager
import com.example.core.security.SecurityState

@Composable
fun LockScreen(
    securityManager: SecurityManager,
    securityState: SecurityState,
    currentLanguage: AppLanguage,
    onUnlocked: () -> Unit
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val context = LocalContext.current
    val activity = context as? Activity

    var enteredSecret by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isLockedOut = securityState.lockoutRemainingSeconds > 0

    // Auto-prompt biometric on launch if enabled and not locked out
    LaunchedEffect(securityState.isBiometricEnabled, isLockedOut) {
        if (securityState.isBiometricEnabled && !isLockedOut && activity != null) {
            BiometricAuthHelper.promptBiometric(
                activity = activity,
                title = if (isFa) "احراز هویت بیومتریک" else "Biometric Unlock",
                subtitle = if (isFa) "برای ورود اثر انگشت خود را اسکن کنید" else "Scan your fingerprint to unlock",
                negativeButtonText = if (isFa) "استفاده از رمز عبور" else "Use Password/PIN",
                onSuccess = {
                    securityManager.unlockWithBiometric()
                    onUnlocked()
                },
                onError = { /* Keep PIN fallback active */ }
            )
        }
    }

    fun submitUnlock(input: String) {
        if (input.isBlank() || isLockedOut) return
        when (val res = securityManager.unlockWithSecret(input)) {
            is SecurityManager.UnlockResult.Success -> {
                errorMessage = null
                onUnlocked()
            }
            is SecurityManager.UnlockResult.InvalidSecret -> {
                enteredSecret = ""
                errorMessage = if (isFa) {
                    "رمز عبور یا پین اشتباه است. (${res.attemptsRemaining} تلاش باقی‌مانده)"
                } else {
                    "Incorrect PIN or password. (${res.attemptsRemaining} attempts left)"
                }
            }
            is SecurityManager.UnlockResult.LockedOut -> {
                enteredSecret = ""
                errorMessage = if (isFa) {
                    "تعداد تلاش‌های ناموفق بیش از حد مجاز. ${res.waitSeconds} ثانیه صبر کنید."
                } else {
                    "Too many failed attempts. Locked for ${res.waitSeconds}s."
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Shield Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFa) "پرورشگاه مرغ عشق - قفل امنیتی" else "Budgie Aviary Security Lock",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFa) {
                    "کاربر فعال: ${securityState.currentUserName} (${securityState.currentRole.titleFa})"
                } else {
                    "Active User: ${securityState.currentUserName} (${securityState.currentRole.titleEn})"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lockout Banner
            AnimatedVisibility(visible = isLockedOut) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isFa) {
                                "قفل امنیتی فعال شد! لطفاً ${securityState.lockoutRemainingSeconds} ثانیه صبر کنید."
                            } else {
                                "Security cooldown active! Please wait ${securityState.lockoutRemainingSeconds}s."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Error Message
            if (errorMessage != null && !isLockedOut) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (securityState.lockType == LockType.PIN) {
                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    val maxPins = 4
                    for (i in 0 until maxPins) {
                        val isFilled = i < enteredSecret.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad
                PinKeypad(
                    onNumberClick = { digit ->
                        if (!isLockedOut && enteredSecret.length < 6) {
                            val next = enteredSecret + digit
                            enteredSecret = next
                            if (next.length == 4) {
                                submitUnlock(next)
                            }
                        }
                    },
                    onBackspace = {
                        if (!isLockedOut && enteredSecret.isNotEmpty()) {
                            enteredSecret = enteredSecret.dropLast(1)
                        }
                    },
                    onBiometricClick = {
                        if (activity != null && securityState.isBiometricEnabled && !isLockedOut) {
                            BiometricAuthHelper.promptBiometric(
                                activity = activity,
                                title = if (isFa) "احراز هویت بیومتریک" else "Biometric Unlock",
                                subtitle = if (isFa) "برای ورود اثر انگشت خود را اسکن کنید" else "Scan your fingerprint to unlock",
                                negativeButtonText = if (isFa) "لغو" else "Cancel",
                                onSuccess = {
                                    securityManager.unlockWithBiometric()
                                    onUnlocked()
                                },
                                onError = { msg -> errorMessage = msg }
                            )
                        }
                    },
                    showBiometric = securityState.isBiometricEnabled,
                    enabled = !isLockedOut
                )
            } else {
                // Password Text Mode
                OutlinedTextField(
                    value = enteredSecret,
                    onValueChange = { enteredSecret = it },
                    label = { Text(if (isFa) "رمز عبور" else "Password") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_password_input"),
                    enabled = !isLockedOut
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { submitUnlock(enteredSecret) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("unlock_button"),
                    enabled = enteredSecret.isNotBlank() && !isLockedOut
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFa) "ورود به برنامه" else "Unlock Application")
                }

                if (securityState.isBiometricEnabled && activity != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            BiometricAuthHelper.promptBiometric(
                                activity = activity,
                                title = if (isFa) "احراز هویت بیومتریک" else "Biometric Unlock",
                                subtitle = if (isFa) "برای ورود اثر انگشت خود را اسکن کنید" else "Scan your fingerprint",
                                negativeButtonText = if (isFa) "لغو" else "Cancel",
                                onSuccess = {
                                    securityManager.unlockWithBiometric()
                                    onUnlocked()
                                },
                                onError = { msg -> errorMessage = msg }
                            )
                        },
                        enabled = !isLockedOut
                    ) {
                        Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFa) "بازگشایی با اثر انگشت" else "Unlock with Fingerprint")
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit,
    showBiometric: Boolean,
    enabled: Boolean
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                for (key in row) {
                    when (key) {
                        "BIO" -> {
                            if (showBiometric) {
                                IconButton(
                                    onClick = onBiometricClick,
                                    enabled = enabled,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Biometric",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(68.dp))
                            }
                        }
                        "DEL" -> {
                            IconButton(
                                onClick = onBackspace,
                                enabled = enabled,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = enabled) { onNumberClick(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
