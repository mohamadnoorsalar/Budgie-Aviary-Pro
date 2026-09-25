package com.example.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

val LocalAppLanguage = compositionLocalOf { AppLanguage.PERSIAN }

object LocalizationManager {

    fun updateContextLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}

@Composable
fun AviaryLocalizationProvider(
    currentLanguage: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val localizedContext = remember(currentLanguage, context) {
        LocalizationManager.updateContextLocale(context, currentLanguage)
    }

    val config = remember(currentLanguage) {
        val locale = Locale(currentLanguage.code)
        val c = Configuration(localizedContext.resources.configuration)
        c.setLocale(locale)
        c.setLayoutDirection(locale)
        c
    }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalLayoutDirection provides currentLanguage.layoutDirection,
        LocalContext provides localizedContext,
        LocalConfiguration provides config
    ) {
        content()
    }
}
