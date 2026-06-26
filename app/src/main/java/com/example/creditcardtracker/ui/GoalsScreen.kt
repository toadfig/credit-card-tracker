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
import com.example.creditcardtracker.data.SavingsGoal
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.BdtText
import com.example.creditcardtracker.theme.formatBdtValue
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.*

enum class GoalTermTab(val title: String) {
    ShortTerm("Short-Term (<3M)"),
    MidTerm("Mid-Term (6-11M)"),
    LongTerm("Long-Term (1Y+)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val savingsGoals = viewModel.savingsGoals
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeTab by remember { mutableStateOf(GoalTermTab.ShortTerm) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    fun formatBdt(amount: Double): String {
        return "৳ " + formatBdtValue(amount)
    }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Filter goals based on duration from today to target date
    val filteredGoals = remember(savingsGoals, activeTab, today) {
        savingsGoals.filter { goal ->
            val durationDays = ((goal.targetDate - today) / (1000 * 60 * 60 * 24)).toInt()
            when (activeTab) {
                GoalTermTab.ShortTerm -> durationDays <= 90
                GoalTermTab.MidTerm -> durationDays in 91..364
                GoalTermTab.LongTerm -> durationDays >= 365
            }
        }
    }

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
                text = "Financial Goals",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Track short, mid, and long term savings milestones to hit your future targets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Term Selector Toggles (Segmented Tab Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GoalTermTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scrollable List of Filtered Goals
            if (filteredGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
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
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = "No Goals",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No ${activeTab.title.substringBefore(" ")} goals found. Tap + to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredGoals.forEach { goal ->
                        var showUpdateDialog by remember { mutableStateOf(false) }

                        GoalProgressCard(
                            goal = goal,
                            today = today,
                            formatBdt = ::formatBdt,
                            onDelete = { viewModel.deleteSavingsGoal(goal.id) },
                            onAdjustClick = { showUpdateDialog = true }
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

        // Floating Action Button to add goal
        FloatingActionButton(
            onClick = { showAddGoalDialog = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Goal")
        }
    }

    if (showAddGoalDialog) {
        var name by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var currentText by remember { mutableStateOf("0") }
        var durationMonthsText by remember { mutableStateOf("") }

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
                        label = { Text("Goal Title (e.g. New Car, Laptop)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        label = { Text("Initial Saved Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = durationMonthsText,
                        onValueChange = { durationMonthsText = it },
                        label = { Text("Target Duration (Months)") },
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
                        val months = durationMonthsText.toIntOrNull() ?: 0

                        if (name.isBlank() || target <= 0.0 || current < 0.0 || months <= 0) {
                            Toast.makeText(context, "Please fill in all details correctly.", Toast.LENGTH_SHORT).show()
                        } else {
                            val computedTargetDate = System.currentTimeMillis() + (months * 30L * 24 * 60 * 60 * 1000)
                            viewModel.addSavingsGoal(name.trim(), target, current, computedTargetDate)
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
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
fun GoalProgressCard(
    goal: SavingsGoal,
    today: Long,
    formatBdt: (Double) -> String,
    onDelete: () -> Unit,
    onAdjustClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "GoalProgress"
    )

    val durationDays = ((goal.targetDate - today) / (1000 * 60 * 60 * 24)).toInt()
    val dateStr = SimpleDateFormat("MMM yyyy", Locale.US).format(Date(goal.targetDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target Date: $dateStr • ${if (durationDays > 0) "$durationDays days left" else "Overdue"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onAdjustClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Adjust", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatBdt(goal.currentAmount)} (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${formatBdt(goal.targetAmount)}",
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
                    label = { Text("Saved Amount (৳)") },
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
