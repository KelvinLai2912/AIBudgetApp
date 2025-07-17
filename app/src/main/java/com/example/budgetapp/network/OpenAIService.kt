package com.example.budgetapp.network

import com.example.budgetapp.BuildConfig
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OpenAIService {
    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }


    suspend fun fetchResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        val body = buildRequestBody(prompt)

        val request = Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            .header("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), body))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyText = response.body?.string()
                parseResponse(bodyText)
            } else {
                println("❌ Fout: ${response.code} - ${response.message}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildRequestBody(prompt: String): String {
        val jsonBody = buildJsonObject {
            put("model", "gpt-4o")
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", """
                        Je bent een slimme assistent die Nederlandse financiële prompts omzet in transacties.
                        Je output is een JSON-array van transacties.
                        Elke transactie heeft: type ("inkomen" of "uitgaven"), amount (float, in euro’s), description (korte tekst).
                        Geef alleen een geldige JSON-array terug met objecten in dit formaat:
                        {
                          "type": "income | expense",
                          "amount": <getal>,
                          "category": "<beschrijving>",
                          "date": "<YYYY-MM-DD>"
                        }
                    """.trimIndent())
                }
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }

        }
        return json.encodeToString(JsonObject.serializer(), jsonBody)
    }

    private fun parseResponse(responseBody: String?): String? {
        if (responseBody == null) return null
        val root = json.parseToJsonElement(responseBody).jsonObject
        return root["choices"]
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
    }
}
