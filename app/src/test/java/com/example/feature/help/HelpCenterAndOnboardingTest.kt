package com.example.feature.help

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.feature.onboarding.OnboardingManager
import com.example.feature.onboarding.OnboardingStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HelpCenterAndOnboardingTest {

    @Test
    fun testAllRequiredTopicsAreCovered() {
        val requiredTopicIds = listOf(
            "getting_started", "birds", "cages", "pairs", "reproduction",
            "eggs", "chicks", "genetics", "pedigree", "health",
            "nutrition", "inventory", "finance", "competitions",
            "ai_assistant", "ai_photo_registration", "reports",
            "pdf_excel", "reminders", "security", "backup",
            "api_keys", "settings"
        )

        val topics = HelpContentRepository.topics
        assertTrue("Must contain all 23 required help guides", topics.size >= 23)

        for (topicId in requiredTopicIds) {
            val topic = HelpContentRepository.getTopicById(topicId)
            assertNotNull("Missing required guide for topic: $topicId", topic)
            assertTrue("Topic $topicId must have English title", topic!!.titleEn.isNotBlank())
            assertTrue("Topic $topicId must have Persian title", topic.titleFa.isNotBlank())
            assertTrue("Topic $topicId must have English content", topic.contentEn.isNotBlank())
            assertTrue("Topic $topicId must have Persian content", topic.contentFa.isNotBlank())
        }
    }

    @Test
    fun testHelpSearchInEnglishAndPersian() {
        // Search in English
        val searchGeneticsEn = HelpContentRepository.searchTopics("genetics", isPersian = false)
        assertTrue(searchGeneticsEn.any { it.id == "genetics" })

        val searchCandlingEn = HelpContentRepository.searchTopics("candling", isPersian = false)
        assertTrue(searchCandlingEn.any { it.id == "eggs" || it.id == "reproduction" })

        // Search in Persian
        val searchGeneticsFa = HelpContentRepository.searchTopics("ژنتیک", isPersian = true)
        assertTrue(searchGeneticsFa.any { it.id == "genetics" })

        val searchRingFa = HelpContentRepository.searchTopics("حلقه", isPersian = true)
        assertTrue(searchRingFa.any { it.id == "chicks" || it.id == "birds" })
    }

    @Test
    fun testHelpCategoryFiltering() {
        val breedingTopics = HelpContentRepository.topics.filter { it.category == HelpCategory.BREEDING }
        assertTrue("Breeding category should include pairs, genetics, eggs, chicks, pedigree", breedingTopics.size >= 5)

        val systemTopics = HelpContentRepository.topics.filter { it.category == HelpCategory.SYSTEM }
        assertTrue("System category should include security, backup, ai, api keys, settings", systemTopics.size >= 5)
    }

    @Test
    fun testOnboardingStepsAndSkipBehavior() {
        val steps = OnboardingStep.entries
        assertEquals("Onboarding should feature 8 comprehensive setup steps", 8, steps.size)

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Initially onboarding is not marked completed
        OnboardingManager.setOnboardingCompleted(context, false)
        assertFalse(OnboardingManager.isOnboardingCompleted(context))

        // Skipping onboarding marks it completed so user is not forced
        OnboardingManager.setOnboardingCompleted(context, true)
        assertTrue(OnboardingManager.isOnboardingCompleted(context))
    }
}
