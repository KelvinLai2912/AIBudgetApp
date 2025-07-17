package com.example.budgetapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val type: String, // "income" of "expense"
    val amount: Double,
    val category: String,
    val date: String // "YYYY-MM-DD"
)
