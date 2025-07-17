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
        val json = Json { ignoreUnknownKeys = true }
        val parsed = json.decodeFromString<TransactionResponse>(jsonString)
        Transaction(parsed.type, parsed.amount, parsed.category, parsed.date)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
