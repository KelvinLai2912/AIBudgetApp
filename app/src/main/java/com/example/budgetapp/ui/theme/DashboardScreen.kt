package com.example.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetapp.model.Transaction

@Composable
fun DashboardScreen(transactions: List<Transaction>) {
    val totalIncome = transactions
        .filter { it.type == "income" }
        .sumOf { it.amount }

    val totalExpenses = transactions
        .filter { it.type == "expense" }
        .sumOf { it.amount }

    val saldo = totalIncome - totalExpenses

    val categoryTotals = transactions
        .groupBy { it.category }
        .mapValues { entry ->
            entry.value.sumOf {
                if (it.type == "income") it.amount else -it.amount
            }
        }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Huidig saldo: €%.2f".format(saldo),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Categorie-overzicht", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(categoryTotals.entries.toList()) { (category, total) ->
                Text("$category: €%.2f".format(total))
            }
        }
    }
}
