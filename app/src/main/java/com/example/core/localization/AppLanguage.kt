package com.example.core.localization

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val titleEn: String, val titleFa: String, val isRtl: Boolean) {
    PERSIAN("fa", "Persian (فارسی)", "فارسی (RTL)", true),
    ENGLISH("en", "English", "انگلیسی (LTR)", false);

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}
