package com.example.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.budgetapp.model.Transaction
import com.example.budgetapp.storage.LocalStorage
import com.example.budgetapp.ui.DashboardScreen
import com.example.budgetapp.ui.PromptScreen
import com.example.budgetapp.ui.theme.BudgetAppTheme
import com.example.budgetapp.network.OpenAIService
import com.example.budgetapp.util.parseToTransactions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = applicationContext

        setContent {
            BudgetAppTheme {
                var selectedIndex by remember { mutableStateOf(0) }
                var transactions by remember {
                    mutableStateOf(LocalStorage.loadTransactions(appContext))
                }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                selected = selectedIndex == 0,
                                onClick = { selectedIndex = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Prompt") },
                                label = { Text("Prompt") },
                                selected = selectedIndex == 1,
                                onClick = { selectedIndex = 1 }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedIndex) {
                        0 -> DashboardScreen(
                            transactions = transactions,
                            onClearAll = {
                                transactions = emptyList()
                                LocalStorage.saveTransactions(appContext, transactions)
                                println("🗑️ Alle transacties gewist.")
                            }
                        )
                        1 -> PromptScreen(
                            transactions = transactions,
                            onPromptSubmit = { prompt ->
                                coroutineScope.launch {
                                    try {
                                        val result = OpenAIService.fetchResponse(
                                            "Zet dit om in JSON met de velden: type (\"income\" of \"expense\"), amount (getal), category, en date (YYYY-MM-DD). Geef *alleen* het JSON-object terug. Prompt: $prompt"
                                        )
                                        println("GPT antwoord: $result")

                                        val newTransactions = result?.let { parseToTransactions(it) } ?: emptyList()

                                        if (newTransactions.isNotEmpty()) {
                                            transactions = transactions + newTransactions
                                            LocalStorage.saveTransactions(appContext, transactions)
                                            newTransactions.forEach {
                                                println("✅ Transactie toegevoegd: $it")
                                            }
                                        } else {
                                            println("⚠️ Kan resultaat niet omzetten naar transacties.")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        println("🔥 Fout tijdens GPT-verwerking: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
