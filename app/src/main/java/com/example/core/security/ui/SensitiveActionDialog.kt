package com.example.core.security.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.security.LockType
import com.example.core.security.SecurityManager

@Composable
fun SensitiveActionDialog(
    securityManager: SecurityManager,
    actionTitle: String,
    actionMessage: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isFa = currentLanguage == AppLanguage.PERSIAN
    val secState = securityManager.state.value

    var confirmationInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val requiresSecret = secState.isLockConfigured

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = actionTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = actionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (requiresSecret) {
                    Text(
                        text = if (isFa) {
                            "این یک اقدام حساس است. برای تایید، ${if (secState.lockType == LockType.PIN) "پین کد" else "رمز عبور"} خود را وارد نمایید:"
                        } else {
                            "This is a sensitive action. Please enter your ${if (secState.lockType == LockType.PIN) "PIN" else "Password"} to authorize:"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = {
                            confirmationInput = it
                            errorMessage = null
                        },
                        label = { Text(if (secState.lockType == LockType.PIN) "PIN" else "Password") },
                        singleLine = true,
                        keyboardOptions = if (secState.lockType == LockType.PIN) {
                            KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        } else {
                            KeyboardOptions(keyboardType = KeyboardType.Password)
                        },
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
                            .testTag("sensitive_action_secret_input")
                    )
                } else {
                    Text(
                        text = if (isFa) {
                            "برای تایید این عملیات غیرقابل بازگشت، کلمه 'تایید' یا 'CONFIRM' را تایپ کنید:"
                        } else {
                            "To confirm this irreversible action, type 'CONFIRM':"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = {
                            confirmationInput = it
                            errorMessage = null
                        },
                        label = { Text("CONFIRM") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sensitive_action_confirm_input")
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (securityManager.verifySensitiveAction(confirmationInput)) {
                        securityManager.logAudit(
                            action = "SENSITIVE_ACTION_CONFIRMED",
                            summary = "Authorized sensitive operation: $actionTitle",
                            entityType = "SENSITIVE_OP",
                            entityId = "CONFIRM"
                        )
                        onConfirm()
                    } else {
                        errorMessage = if (isFa) {
                            if (requiresSecret) "رمز عبور یا پین اشتباه است." else "لطفاً عبارت 'تایید' یا 'CONFIRM' را درست تایپ کنید."
                        } else {
                            if (requiresSecret) "Incorrect PIN or password." else "Please type 'CONFIRM' to proceed."
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("sensitive_action_confirm_button")
            ) {
                Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFa) "تایید و اجرا" else "Confirm & Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "انصراف" else "Cancel")
            }
        }
    )
}
