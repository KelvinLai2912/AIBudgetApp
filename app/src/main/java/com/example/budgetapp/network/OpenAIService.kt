package com.example.budgetapp.network

import com.example.budgetapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

object OpenAIService {
    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        val bodyString = buildRequestBody(prompt)
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(bodyString.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ Fout: ${response.code} – ${response.message}")
                    return@use null
                }
                val respBody = response.body?.string()
                parseResponse(respBody)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildRequestBody(prompt: String): String {
        val systemPrompt = """
                                Zet Nederlandse tekst om in een JSON-array met transacties.  
                                Gebruik **alleen** wat letterlijk genoemd is. Voeg **nooit** extra transacties of bedragen toe.
                        
                                Herken en parseer expliciete datums, zowel:
                                - “YYYY MMMM D” (bijv. “2024 juli 1” → “2024-07-01”)
                                - “D MMMM YYYY” (bijv. “1 juli 2024” → “2024-07-01”)
                                én relatieve datums:
                                  - gisteren → ${LocalDate.now().minusDays(1)}
                                  - vandaag   → ${LocalDate.now()}
                                  - morgen    → ${LocalDate.now().plusDays(1)}
                                  - overmorgen→ ${LocalDate.now().plusDays(2)}
                        
                                Formaat per object:
                                {
                                  "type":     "income" of "expense",
                                  "amount":   exact bedrag uit prompt (float),
                                  "category": letterlijk woord of zin uit prompt,
                                  "date":     YYYY-MM-DD
                                }
                        
                                Geef **alleen** de JSON-array terug, zonder extra uitleg of markdown.
                            """.trimIndent()

        val body = buildJsonObject {
            put("model", "gpt-4o")
            put("temperature", 0)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", systemPrompt)
                }
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
        }
        return json.encodeToString(JsonObject.serializer(), body)
    }

    private fun parseResponse(responseBody: String?): String? {
        if (responseBody == null) return null
        val root = json.parseToJsonElement(responseBody).jsonObject
        return root["choices"]
            ?.jsonArray
            ?.getOrNull(0)
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
    }
}
