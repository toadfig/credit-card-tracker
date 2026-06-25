package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Budget
import com.example.creditcardtracker.data.SavingsGoal
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val budgets = viewModel.budgets
    val savingsGoals = viewModel.savingsGoals
    val context = LocalContext.current

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val inLocale = Locale("en", "IN")
    val bdtFormatter = remember { 
        val formatter = NumberFormat.getCurrencyInstance(inLocale)
        formatter.currency = Currency.getInstance("BDT")
        formatter
    }
    fun formatBdt(amount: Double): String {
        return bdtFormatter.format(amount)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budgets Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set limit caps by spending category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showAddBudgetDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Budget")
                }
            }

            if (budgets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vaultGlass(borderRadius = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "No Budgets",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No budgets configured. Tap + to set one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                budgets.forEach { budget ->
                    val spent = viewModel.getSpentForCategoryThisMonth(budget.category)
                    BudgetProgressCard(
                        budget = budget,
                        spent = spent,
                        formatBdt = ::formatBdt,
                        onDelete = { viewModel.deleteBudget(budget.id) },
                        onToggleRollover = { viewModel.toggleBudgetRollover(budget.id) }
                    )
                }
            }
        }

        // Savings Goals Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track progress for long-term targets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showAddGoalDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Goal")
                }
            }

            if (savingsGoals.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vaultGlass(borderRadius = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Redeem,
                            contentDescription = "No Goals",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No savings goals logged. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                savingsGoals.forEach { goal ->
                    var showUpdateDialog by remember { mutableStateOf(false) }
                    SavingsGoalProgressCard(
                        goal = goal,
                        formatBdt = ::formatBdt,
                        onDelete = { viewModel.deleteSavingsGoal(goal.id) },
                        onUpdateClick = { showUpdateDialog = true }
                    )

                    if (showUpdateDialog) {
                        UpdateGoalProgressDialog(
                            goal = goal,
                            onDismiss = { showUpdateDialog = false },
                            onConfirm = { amt ->
                                viewModel.updateSavingsGoalProgress(goal.id, amt)
                                showUpdateDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddBudgetDialog) {
        var limitText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Food & Dining") }
        var isRolloverEnabled by remember { mutableStateOf(false) }

        val categories = listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Healthcare", "Education", "Entertainment", "Others")
        var showCatDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            title = { Text("Create Category Budget", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCatDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category)
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = showCatDropdown,
                            onDismissRequest = { showCatDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        showCatDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Limit Cap Amount (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Monthly Rollover", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Unspent budget carries over into the next month's limit.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isRolloverEnabled,
                            onCheckedChange = { isRolloverEnabled = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        if (limit <= 0.0) {
                            Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                        } else if (budgets.any { it.category == category }) {
                            Toast.makeText(context, "A budget for $category already exists.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addBudget(category, limit, isRolloverEnabled)
                            showAddBudgetDialog = false
                        }
                    }
                ) {
                    Text("Create Budget", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddGoalDialog) {
        var name by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var currentText by remember { mutableStateOf("0") }
        var targetDays by remember { mutableStateOf("365") } // Default target 1 year

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Create Savings Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Goal Name (e.g. Travel, Emergency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Amount (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        label = { Text("Initial Saved Amount (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetDays,
                        onValueChange = { targetDays = it.filter { char -> char.isDigit() } },
                        label = { Text("Time Target (Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        val current = currentText.toDoubleOrNull() ?: 0.0
                        val days = targetDays.toLongOrNull() ?: 365
                        
                        if (name.isBlank() || target <= 0.0 || current < 0.0 || days <= 0) {
                            Toast.makeText(context, "Invalid input details.", Toast.LENGTH_SHORT).show()
                        } else {
                            val targetDateMillis = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
                            viewModel.addSavingsGoal(name.trim(), target, current, targetDateMillis)
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("Add Goal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BudgetProgressCard(
    budget: Budget,
    spent: Double,
    formatBdt: (Double) -> String,
    onDelete: () -> Unit,
    onToggleRollover: () -> Unit
) {
    // Total available limit includes rollover
    val totalLimit = budget.limitAmount + budget.rolloverAmount
    val progress = if (totalLimit > 0) (spent / totalLimit).coerceIn(0.0, 1.0).toFloat() else 0f
    
    val color = when {
        progress >= 0.9f -> MaterialTheme.colorScheme.error
        progress >= 0.7f -> Color(0xFFFFD166) // Orange
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vaultGlass(borderRadius = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (budget.rolloverAmount != 0.0) {
                        Text(
                            text = "Rollover: ${formatBdt(budget.rolloverAmount)} (Total Limit: ${formatBdt(totalLimit)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleRollover) {
                        Icon(
                            imageVector = if (budget.isRolloverEnabled) Icons.Outlined.Autorenew else Icons.Outlined.Block,
                            contentDescription = "Rollover Status",
                            tint = if (budget.isRolloverEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spent ${formatBdt(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Limit ${formatBdt(totalLimit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SavingsGoalProgressCard(
    goal: SavingsGoal,
    formatBdt: (Double) -> String,
    onDelete: () -> Unit,
    onUpdateClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(goal.targetDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vaultGlass(borderRadius = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target Date: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onUpdateClick) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Update Progress", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved ${formatBdt(goal.currentAmount)} (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Target ${formatBdt(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpdateGoalProgressDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf(goal.currentAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Goal Progress", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Update current saved amount for \"${goal.name}\":", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Saved Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt < 0.0) {
                        Toast.makeText(context, "Amount cannot be negative.", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(amt)
                    }
                }
            ) {
                Text("Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
