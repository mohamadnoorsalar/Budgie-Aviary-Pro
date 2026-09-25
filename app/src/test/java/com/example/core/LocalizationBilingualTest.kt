package com.example.core

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizationBilingualTest {

    @Test
    fun testPersianLayoutDirectionIsRTL() {
        val persian = AppLanguage.PERSIAN
        assertTrue("Persian must have isRtl=true", persian.isRtl)
        assertEquals(LayoutDirection.Rtl, persian.layoutDirection)
        assertEquals("fa", persian.code)
    }

    @Test
    fun testEnglishLayoutDirectionIsLTR() {
        val english = AppLanguage.ENGLISH
        assertFalse("English must have isRtl=false", english.isRtl)
        assertEquals(LayoutDirection.Ltr, english.layoutDirection)
        assertEquals("en", english.code)
    }

    @Test
    fun testContextLocaleUpdateForPersian() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val faContext = LocalizationManager.updateContextLocale(baseContext, AppLanguage.PERSIAN)

        val appNameFa = faContext.getString(R.string.app_name)
        val navDashboardFa = faContext.getString(R.string.nav_dashboard)
        val navBirdsFa = faContext.getString(R.string.nav_birds)

        assertEquals("مدیریت سالن مرغ عشق", appNameFa)
        assertEquals("داشبورد", navDashboardFa)
        assertEquals("پرنده‌ها", navBirdsFa)
    }

    @Test
    fun testContextLocaleUpdateForEnglish() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val enContext = LocalizationManager.updateContextLocale(baseContext, AppLanguage.ENGLISH)

        val navDashboardEn = enContext.getString(R.string.nav_dashboard)
        val navBirdsEn = enContext.getString(R.string.nav_birds)
        val navReproductionEn = enContext.getString(R.string.nav_reproduction)

        assertEquals("Dashboard", navDashboardEn)
        assertEquals("Birds", navBirdsEn)
        assertEquals("Reproduction", navReproductionEn)
    }

    @Test
    fun testLanguageSwitchDoesNotAffectLocaleIntegrity() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()

        // Switch to Persian
        val faContext = LocalizationManager.updateContextLocale(baseContext, AppLanguage.PERSIAN)
        assertEquals("فارسی (RTL)", AppLanguage.PERSIAN.titleFa)
        assertEquals("ذخیره", faContext.getString(R.string.action_save))

        // Switch to English
        val enContext = LocalizationManager.updateContextLocale(baseContext, AppLanguage.ENGLISH)
        assertEquals("Save", enContext.getString(R.string.action_save))
        assertEquals("Active", enContext.getString(R.string.status_active))

        // Switch back to Persian
        val faContext2 = LocalizationManager.updateContextLocale(baseContext, AppLanguage.PERSIAN)
        assertEquals("فعال", faContext2.getString(R.string.status_active))
    }
}
