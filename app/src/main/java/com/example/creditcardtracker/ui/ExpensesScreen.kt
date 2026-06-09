package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showAnalytics by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expenses Log",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (expenses.isNotEmpty()) {
                    TextButton(onClick = { showAnalytics = !showAnalytics }) {
                        Text(
                            text = if (showAnalytics) "Hide Analytics" else "Show Analytics",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Spending Analytics Card
            if (showAnalytics && expenses.isNotEmpty()) {
                SpendingAnalyticsCard(expenses = expenses)
            }

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
                val sortedExpenses = remember(expenses.size, expenses) { 
                    expenses.sortedByDescending { it.date } 
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = sortedExpenses, key = { it.id }) { expense ->
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
                onConfirm = { cardId, amount, category, description, date, currency, exchangeRate ->
                    viewModel.addExpense(cardId, amount, category, description, date, currency, exchangeRate)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun SpendingAnalyticsCard(expenses: List<Expense>) {
    val categories = listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Others")
    val categoryColors = listOf(
        Color(0xFFEF476F),
        Color(0xFF118AB2),
        Color(0xFF06D6A0),
        Color(0xFFFFD166),
        Color(0xFF072AC8),
        Color(0xFF708090)
    )

    // Calculate sum of expenses per category (converting to BDT)
    val spendByCategory = categories.map { category ->
        expenses.filter { it.category == category }.sumOf { it.amount * it.exchangeRate }
    }
    val totalSpend = spendByCategory.sum()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vaultGlass(borderRadius = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Analytics Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (totalSpend > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Pie Chart Drawn in Canvas
                    Canvas(modifier = Modifier.size(110.dp)) {
                        var startAngle = -90f
                        spendByCategory.forEachIndexed { index, amount ->
                            val sweepAngle = ((amount / totalSpend) * 360f).toFloat()
                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = categoryColors[index],
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        // Draw central circle to make it a donut
                        drawCircle(
                            color = Color(0xFF111827), // Vault dark background
                            radius = size.width * 0.28f,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }

                    // Legend Layout
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEachIndexed { index, name ->
                            val amount = spendByCategory[index]
                            if (amount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(categoryColors[index], RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        text = "$name: BDT ${amount.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No data to display charts.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    card: CreditCard?,
    onDelete: () -> Unit
) {
    val bdtFormat = NumberFormat.getCurrencyInstance(Locale.US).apply { 
        currency = java.util.Currency.getInstance("BDT") 
    }
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    val categoryIcon = when (expense.category) {
        "Food & Dining" -> Icons.Outlined.Restaurant
        "Groceries" -> Icons.Outlined.LocalGroceryStore
        "Transportation" -> Icons.Outlined.DirectionsCar
        "Shopping" -> Icons.Outlined.ShoppingBag
        "Utilities" -> Icons.AutoMirrored.Outlined.ReceiptLong
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = expense.category,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    // Show original currency
                    val formattedOrig = if (expense.currency == "USD") {
                        "$${String.format("%.2f", expense.amount)}"
                    } else {
                        "৳${expense.amount.toInt()}"
                    }
                    Text(
                        text = formattedOrig,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    // Show conversion to BDT if foreign currency
                    if (expense.currency == "USD") {
                        Text(
                            text = "৳${(expense.amount * expense.exchangeRate).toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
    onConfirm: (
        cardId: String, amount: Double, category: String, description: String,
        date: Long, currency: String, exchangeRate: Double
    ) -> Unit
) {
    val context = LocalContext.current

    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    
    // Multi-currency details
    var currency by remember { mutableStateOf("BDT") }
    var exchangeRate by remember { mutableStateOf("117.5") } // Default USD rate estimate

    var errorText by remember { mutableStateOf("") }

    val categories = listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Others")
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

                // Currency Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Currency:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = currency == "BDT", onClick = { currency = "BDT" })
                        Text("BDT (৳)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = currency == "USD", onClick = { currency = "USD" })
                        Text("USD ($)")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    if (currency == "USD") {
                        OutlinedTextField(
                            value = exchangeRate,
                            onValueChange = { exchangeRate = it },
                            label = { Text("Rate (to BDT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

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
                val rateVal = exchangeRate.toDoubleOrNull() ?: 1.0
                val activeCard = selectedCard

                if (activeCard == null) {
                    errorText = "Please select a credit card."
                } else if (amtVal <= 0.0) {
                    errorText = "Please enter an expense amount greater than 0."
                } else if (currency == "USD" && rateVal <= 0.0) {
                    errorText = "Please enter a valid exchange rate."
                } else if (description.isBlank()) {
                    errorText = "Please write a brief description."
                } else {
                    val finalRate = if (currency == "BDT") 1.0 else rateVal
                    onConfirm(
                        activeCard.id, amtVal, selectedCategory, description,
                        System.currentTimeMillis(), currency, finalRate
                    )
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
