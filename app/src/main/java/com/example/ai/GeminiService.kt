package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GroqMessage(val role: String, val content: String)

@JsonClass(generateAdapter = true)
data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>
)

@JsonClass(generateAdapter = true)
data class GroqChoice(val message: GroqMessage)

@JsonClass(generateAdapter = true)
data class GroqResponse(val choices: List<GroqChoice> = emptyList())

interface GroqApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): GroqResponse
}

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GroqApiService::class.java)
    }

    suspend fun analyzeSpending(expensesJson: String): String = withContext(Dispatchers.IO) {
        val prompt = "Analyze these expenses and identify money leaks, subscriptions that can be cut, and where the user is overspending. Be concise and formatted in a helpful response: $expensesJson"
        callGroq(prompt, "You are a Senior Financial Advisor AI.")
    }

    suspend fun askFinancialCoach(question: String, contextData: String): String = withContext(Dispatchers.IO) {
        val prompt = "User context data (expenses): \n$contextData\n\nUser Question: $question\n\nProvide tailored financial advice."
        callGroq(prompt, "You are SpendSense AI, an expert, empathetic, elite financial coach.")
    }

    private suspend fun callGroq(prompt: String, systemInstruction: String): String {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isEmpty() || apiKey.startsWith("MY_GROQ_API_KEY")) {
             return "API Key is missing or invalid. Please configure GROQ_API_KEY in AI Studio Secrets."
        }
        
        val request = GroqRequest(
            messages = listOf(
                GroqMessage("system", systemInstruction),
                GroqMessage("user", prompt)
            )
        )
        
        return try {
            val response = api.generateContent("Bearer $apiKey", request)
            var text = response.choices.firstOrNull()?.message?.content ?: "No insight received from AI."
            text = text.replace("\\*\\*|\\*".toRegex(), "")
            text
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: e.message()
            "API Error (HTTP ${e.code()}): $errorBody\n\nPlease check your Groq API quota and keys in settings."
        } catch (e: Exception) {
            "Analysis failed: ${e.localizedMessage ?: e.toString()}\n\nPlease check your connection."
        }
    }
}

