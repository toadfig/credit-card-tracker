package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.Subscription
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.util.*

@Composable
fun SubscriptionsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val subscriptions = viewModel.subscriptions
    val cards = viewModel.cards
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Formatting for Lakh/Crore in Bangladesh
    val bdtFormatter = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN"))
    }
    fun formatBdt(amount: Double): String {
        return "৳" + bdtFormatter.format(amount)
    }

    // Calculate projected outflow for the next 30 days (sum of active subscriptions)
    val activeSubs = subscriptions.filter { it.isActive }
    val projectedMonthlyOutflow = activeSubs.sumOf { it.amount }

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
                text = "Subscriptions",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Projected Outflow Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .vaultGlass(borderRadius = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Autorenew,
                                contentDescription = "Outflow",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Projected 30-Day Outflow",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = formatBdt(projectedMonthlyOutflow),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Estimated recurring payments across ${activeSubs.size} active subscription${if (activeSubs.size == 1) "" else "s"}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Subscriptions,
                            contentDescription = "No Subscriptions",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Subscriptions Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keep track of your active memberships, utilities, and SaaS billing schedules in one clean space.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = subscriptions, key = { it.id }) { sub ->
                        val linkedCard = cards.find { it.id == sub.cardId }
                        SubscriptionItem(
                            sub = sub,
                            card = linkedCard,
                            formatBdt = ::formatBdt,
                            onDelete = { viewModel.deleteSubscription(sub.id) }
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
                Icon(Icons.Filled.Add, contentDescription = "Add Subscription")
            }
        }

        if (showAddDialog) {
            AddSubscriptionDialog(
                cards = cards,
                onDismiss = { showAddDialog = false },
                onConfirm = { cardId, name, amount, billingDay, category ->
                    viewModel.addSubscription(cardId, name, amount, billingDay, category)
                    showAddDialog = false
                    Toast.makeText(context, "Subscription logged successfully.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun SubscriptionItem(
    sub: Subscription,
    card: CreditCard?,
    formatBdt: (Double) -> String,
    onDelete: () -> Unit
) {
    val categoryIcon = when (sub.category) {
        "Entertainment" -> Icons.Outlined.PlayArrow
        "Utilities" -> Icons.Outlined.Lightbulb
        "SaaS" -> Icons.Outlined.Cloud
        "Retail" -> Icons.Outlined.ShoppingBag
        "Education" -> Icons.Outlined.School
        else -> Icons.Outlined.Category
    }

    val categoryColor = when (sub.category) {
        "Entertainment" -> Color(0xFFEF476F)
        "Utilities" -> Color(0xFF06D6A0)
        "SaaS" -> Color(0xFF118AB2)
        "Retail" -> Color(0xFFFFD166)
        "Education" -> Color(0xFF7209B7)
        else -> Color(0xFF708090)
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
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = sub.category,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${card?.bank?.uppercase() ?: "Unknown Card"} • Billed Day ${sub.billingDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${formatBdt(sub.amount)}/mo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Subscription",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: String, name: String, amount: Double, billingDay: Int, category: String) -> Unit
) {
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("SaaS") }

    var cardDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    val categories = listOf("SaaS", "Entertainment", "Utilities", "Retail", "Education", "Others")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Subscription", fontWeight = FontWeight.Bold) },
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
                        label = { Text("Charge Credit Card") },
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

                // Subscription Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subscription Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cost and Billing Day Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Cost (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = billingDay,
                        onValueChange = { billingDay = it.filter { char -> char.isDigit() }.take(2) },
                        label = { Text("Billing Day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Dropdown
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amtVal = amount.toDoubleOrNull() ?: 0.0
                val dayVal = billingDay.toIntOrNull() ?: 0
                val activeCard = selectedCard

                if (activeCard == null) {
                    errorText = "Please select a card."
                } else if (name.isBlank()) {
                    errorText = "Please enter a subscription name."
                } else if (amtVal <= 0.0) {
                    errorText = "Please enter a cost greater than 0."
                } else if (dayVal !in 1..31) {
                    errorText = "Billing day must be between 1 and 31."
                } else {
                    onConfirm(activeCard.id, name.trim(), amtVal, dayVal, selectedCategory)
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
