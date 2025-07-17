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
                        0 -> DashboardScreen(transactions = transactions)
                        1 -> PromptScreen(
                            transactions = transactions,
                            onPromptSubmit = { prompt ->
                                // Later: GPT verwerken
                                val newTransaction = Transaction(
                                    type = "expense",
                                    amount = 10.0,
                                    category = "test",
                                    date = "2025-07-17"
                                )
                                transactions = transactions + newTransaction
                                LocalStorage.saveTransactions(appContext, transactions)
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
