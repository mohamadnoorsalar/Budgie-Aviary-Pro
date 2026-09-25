package com.example.feature.aiassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.ai.AviaryDataRetriever
import com.example.core.ai.GeminiClient
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFactualDbResponse: Boolean = false,
    val isError: Boolean = false
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class AiAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val hasApiKeyConfigured: Boolean = false,
    val currentUserName: String = "مدیر سالن / Aviary Admin",
    val currentUserRole: String = "ADMIN", // ADMIN, BREEDER, VET, VIEWER
    val isAiAccessPermitted: Boolean = true,
    val permissionNotice: String? = null
)

class AiAssistantViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val retriever = AviaryDataRetriever(repository)

    private val _uiState = MutableStateFlow(
        AiAssistantUiState(
            hasApiKeyConfigured = GeminiClient.hasValidApiKey(),
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "سلام! من دستیار هوشمند سالن پرورش مرغ عشق شما هستم. می‌توانم به سوالات شما بر اساس اطلاعات واقعی و زنده‌ی ثبت‌شده در پایگاه داده پاسخ دهم.\n\n" +
                           "نمونه سوالات:\n" +
                           "• «امروز چه کارهایی دارم؟»\n" +
                           "• «پرنده‌های قفس ۲۵ را نشان بده»\n" +
                           "• «جفت ۱۲ در سه دوره اخیر چه عملکردی داشته؟»\n" +
                           "• «آخرین وزن این پرنده چقدر است؟»\n" +
                           "• «کدام تخم‌ها نزدیک هچ هستند؟»",
                    isFactualDbResponse = true
                )
            )
        )
    )
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    init {
        checkUserPermissions()
    }

    private fun checkUserPermissions() {
        viewModelScope.launch {
            try {
                val users = repository.activeUsers.first()
                val currentUser = users.firstOrNull { it.isActive }
                if (currentUser != null) {
                    val permitted = currentUser.role in listOf("ADMIN", "BREEDER", "VET", "OPERATOR")
                    _uiState.update {
                        it.copy(
                            currentUserName = currentUser.displayName,
                            currentUserRole = currentUser.role,
                            isAiAccessPermitted = permitted,
                            permissionNotice = if (!permitted) "دسترسی شما محدود است. تنها نقش‌های مجاز امکان استفاده از دستیار داده‌ها را دارند." else null
                        )
                    }
                }
            } catch (_: Exception) {
                // Default admin access
            }
        }
    }

    fun sendMessage(userPrompt: String) {
        val prompt = userPrompt.trim()
        if (prompt.isBlank() || _uiState.value.isLoading) return

        if (!_uiState.value.isAiAccessPermitted) {
            _uiState.update {
                it.copy(
                    messages = it.messages + ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = "خطای دسترسی: حساب کاربری شما مجوز پرس‌وجو از دستیار هوشمند داده‌ها را ندارد.",
                        isError = true
                    )
                )
            }
            return
        }

        // Add user message to chat
        val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                // 1. Retrieve factual database snapshot
                val contextData = retriever.retrieveContextForQuery(prompt)

                // Check if Gemini API key is available
                val hasKey = GeminiClient.hasValidApiKey()

                val answerText = if (hasKey) {
                    // System prompt strictly binds model to retrieved facts and prevents hallucination
                    val systemContextPrompt = retriever.buildContextPrompt(contextData)
                    val result = GeminiClient.queryGemini(systemPrompt = systemContextPrompt, userQuery = prompt)

                    result.getOrElse {
                        // Fall back to deterministic local answer if remote call fails or quota reached
                        retriever.answerLocallyWithoutApi(contextData)
                    }
                } else {
                    // Fast, 100% deterministic local retrieval directly answering from the database
                    retriever.answerLocallyWithoutApi(contextData)
                }

                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            text = answerText,
                            isFactualDbResponse = true
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            text = "خطا در پردازش اطلاعات پایگاه داده: ${e.message ?: "خطای ناشناخته"}",
                            isError = true
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearChat() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = "گفتگو پاکسازی شد. سوال بعدی خود را از اطلاعات سالن بپرسید."
                    )
                )
            )
        }
    }

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AiAssistantViewModel::class.java)) {
                return AiAssistantViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
