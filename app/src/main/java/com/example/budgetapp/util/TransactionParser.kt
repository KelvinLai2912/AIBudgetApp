package com.example.budgetapp.util

import com.example.budgetapp.model.Transaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class TransactionResponse(
    val type: String,
    val amount: Double,
    val category: String,
    val date: String
)

fun parseToTransaction(jsonString: String): Transaction? {
    return try {
        val cleanedJson = jsonString
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = Json { ignoreUnknownKeys = true }

        val parsedList = json.decodeFromString<List<TransactionResponse>>(cleanedJson)
        val parsed = parsedList.firstOrNull() ?: return null

        Transaction(parsed.type, parsed.amount, parsed.category, parsed.date)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
