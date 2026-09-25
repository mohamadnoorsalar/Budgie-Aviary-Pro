package com.example.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

// Model recommended in gemini-api skill
const val GEMINI_MODEL = "gemini-3.5-flash"

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiGenerationConfig(
    val temperature: Float? = 0.2f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 1500
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/$GEMINI_MODEL:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun hasValidApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("placeholder", ignoreCase = true)
    }

    suspend fun queryGemini(
        systemPrompt: String,
        userQuery: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!hasValidApiKey()) {
            return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY is not configured in the Secrets panel."))
        }

        try {
            val request = GeminiRequest(
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userQuery))
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.2f)
            )

            val response = service.generateContent(apiKey = apiKey, request = request)
            if (response.error != null) {
                return@withContext Result.failure(Exception("Gemini API Error: ${response.error.message ?: "Unknown"}"))
            }

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (replyText != null) {
                Result.success(replyText.trim())
            } else {
                Result.failure(Exception("Empty response from AI."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun queryGeminiVision(
        systemPrompt: String,
        userPrompt: String,
        imageBase64: String,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!hasValidApiKey()) {
            return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY is not configured in the Secrets panel."))
        }

        try {
            val parts = listOf(
                GeminiPart(text = userPrompt),
                GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = imageBase64))
            )

            val request = GeminiRequest(
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = parts
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.1f)
            )

            val response = service.generateContent(apiKey = apiKey, request = request)
            if (response.error != null) {
                return@withContext Result.failure(Exception("Gemini Vision Error: ${response.error.message ?: "Unknown"}"))
            }

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (replyText != null) {
                Result.success(replyText.trim())
            } else {
                Result.failure(Exception("Empty vision response from AI."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Utility to compress and encode a Uri to Base64 (max 1024px to preserve bandwidth).
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            // Scale down if larger than 1024px
            val maxDim = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val ratio = width.toFloat() / height.toFloat()
                val targetW: Int
                val targetH: Int
                if (width > height) {
                    targetW = maxDim
                    targetH = (maxDim / ratio).toInt()
                } else {
                    targetH = maxDim
                    targetW = (maxDim * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
