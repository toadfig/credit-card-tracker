package com.example.creditcardtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.Expense
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val expenses = viewModel.expenses
    val cards = viewModel.cards
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Expenses Log",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded. Tap + to add.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sort latest first
                    val sortedExpenses = expenses.sortedByDescending { it.date }
                    items(sortedExpenses) { expense ->
                        val linkedCard = cards.find { it.id == expense.cardId }
                        ExpenseItem(
                            expense = expense,
                            card = linkedCard,
                            onDelete = { viewModel.deleteExpense(expense.id) }
                        )
                    }
                }
            }
        }

        if (cards.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }

        if (showAddDialog) {
            AddExpenseDialog(
                cards = cards,
                onDismiss = { showAddDialog = false },
                onConfirm = { cardId, amount, category, description, date ->
                    viewModel.addExpense(cardId, amount, category, description, date)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    card: CreditCard?,
    onDelete: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    val categoryIcon = when (expense.category) {
        "Food" -> Icons.Outlined.Restaurant
        "Transit" -> Icons.Outlined.DirectionsCar
        "Bills" -> Icons.Outlined.ReceiptLong
        "Shopping" -> Icons.Outlined.ShoppingBag
        "Entertainment" -> Icons.Outlined.LocalPlay
        else -> Icons.Outlined.Category
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vaultGlass(borderRadius = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(VaultUiTokens.ShapeChipSmall)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = expense.category,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${card?.bank?.uppercase() ?: "Unknown Card"} • ${dateFormat.format(expense.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currencyFormat.format(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: String, amount: Double, category: String, description: String, date: Long) -> Unit
) {
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var errorText by remember { mutableStateOf("") }

    val categories = listOf("Food", "Transit", "Bills", "Shopping", "Entertainment", "Others")
    var cardDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Card Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = cardDropdownExpanded,
                    onExpandedChange = { cardDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCard?.let { "${it.bank.uppercase()} (${it.name})" } ?: "Select Card",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Card") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cardDropdownExpanded,
                        onDismissRequest = { cardDropdownExpanded = false }
                    ) {
                        cards.forEach { card ->
                            DropdownMenuItem(
                                text = { Text("${card.bank.uppercase()} - ${card.name} (•••• ${card.cardNumber.takeLast(4)})") },
                                onClick = {
                                    selectedCard = card
                                    cardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amtVal = amount.toDoubleOrNull() ?: 0.0
                val activeCard = selectedCard

                if (activeCard == null) {
                    errorText = "Please select a credit card."
                } else if (amtVal <= 0.0) {
                    errorText = "Please enter an expense amount greater than 0."
                } else if (description.isBlank()) {
                    errorText = "Please write a brief description."
                } else {
                    onConfirm(activeCard.id, amtVal, selectedCategory, description, System.currentTimeMillis())
                }
            }) {
                Text("Log", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
