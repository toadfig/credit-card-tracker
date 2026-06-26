package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Budget
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.BdtText
import com.example.creditcardtracker.theme.formatBdtValue
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val budgets = viewModel.budgets
    val context = LocalContext.current

    var showAddBudgetDialog by remember { mutableStateOf(false) }

    fun formatBdt(amount: Double): String {
        return "৳ " + formatBdtValue(amount)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Category Budgets",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Set and manage monthly limits for spending categories to stay on track.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (budgets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vaultGlass(borderRadius = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PieChart,
                            contentDescription = "No Budgets",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
        }

        // Floating Action Button to add budget
        FloatingActionButton(
            onClick = { showAddBudgetDialog = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Budget")
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
                        label = { Text("Limit Cap Amount (৳)") },
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
}

@Composable
fun BudgetProgressCard(
    budget: Budget,
    spent: Double,
    formatBdt: (Double) -> String,
    onDelete: () -> Unit,
    onToggleRollover: () -> Unit
) {
    val totalLimit = budget.limitAmount + budget.rolloverAmount
    val progress = if (totalLimit > 0) (spent / totalLimit).coerceIn(0.0, 1.0).toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "BudgetProgress"
    )

    val color = when {
        progress >= 0.9f -> MaterialTheme.colorScheme.error
        progress >= 0.7f -> Color(0xFFFFD166)
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
                progress = { animatedProgress },
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
