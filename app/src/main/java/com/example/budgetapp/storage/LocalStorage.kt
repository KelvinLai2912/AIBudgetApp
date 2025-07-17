package com.example.budgetapp.storage

import android.content.Context
import com.example.budgetapp.model.Transaction
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

object LocalStorage {
    private const val FILE_NAME = "transactions.json"
    private val json = Json { prettyPrint = true }

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val file = File(context.filesDir, FILE_NAME)
        val jsonString = json.encodeToString(transactions)
        file.writeText(jsonString)
    }

    fun loadTransactions(context: Context): List<Transaction> {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) {
            try {
                val content = file.readText()
                json.decodeFromString(content)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
