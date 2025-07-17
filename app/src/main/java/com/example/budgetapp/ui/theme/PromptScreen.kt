package com.example.budgetapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetapp.model.Transaction

@Composable
fun PromptScreen(
    transactions: List<Transaction>,
    onPromptSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Voer een prompt in", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Bijv: Ik gaf €15 uit aan snacks") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            onPromptSubmit(prompt)
            prompt = ""
        }) {
            Text("Verstuur")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Transacties", style = MaterialTheme.typography.titleSmall)
        LazyColumn {
            items(transactions) { t ->
                Text("${t.date} - ${t.category} - ${if (t.type == "income") "+" else "-"}€${t.amount}")
            }
        }
    }
}
