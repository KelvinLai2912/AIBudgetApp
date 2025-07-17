package com.example.budgetapp.util

import com.example.budgetapp.model.Transaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class TransactionResponse(
    val type: String,
    val amount: Double,
    val category: String,
    val date: String
)

fun parseToTransactions(jsonString: String): List<Transaction> {
    return try {
        val cleanedJson = jsonString
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = Json { ignoreUnknownKeys = true }

        val parsedList = json.decodeFromString<List<TransactionResponse>>(cleanedJson)

        parsedList.map {
            Transaction(it.type, it.amount, it.category, it.date)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
