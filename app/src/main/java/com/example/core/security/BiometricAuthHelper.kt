package com.example.core.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors

object BiometricAuthHelper {

    fun isBiometricSupported(context: Context): Boolean {
        return try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val biometricManager = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
                biometricManager != null && keyguardManager?.isDeviceSecure == true
            } else {
                keyguardManager?.isKeyguardSecure == true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun promptBiometric(
        activity: Activity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val executor = Executors.newSingleThreadExecutor()
                val cancellationSignal = CancellationSignal()

                val prompt = BiometricPrompt.Builder(activity)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButton(
                        negativeButtonText,
                        executor,
                        DialogInterface.OnClickListener { _, _ ->
                            activity.runOnUiThread { onError("Cancelled by user") }
                        }
                    )
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            activity.runOnUiThread { onSuccess() }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            activity.runOnUiThread { onError(errString?.toString() ?: "Authentication error") }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            activity.runOnUiThread { onError("Authentication failed") }
                        }
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Biometric prompt error")
            }
        } else {
            onError("Biometric prompt requires Android 9+")
        }
    }
}
