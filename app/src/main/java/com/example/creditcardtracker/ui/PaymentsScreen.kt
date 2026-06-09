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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.Payment as PaymentModel
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val payments = viewModel.payments
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
                text = "Payments History",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (payments.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payments logged yet. Tap + to record a payment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val sortedPayments = payments.sortedByDescending { it.date }
                    items(sortedPayments) { payment ->
                        val linkedCard = cards.find { it.id == payment.cardId }
                        PaymentItem(
                            payment = payment,
                            card = linkedCard,
                            onDelete = { viewModel.deletePayment(payment.id) }
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
                Icon(Icons.Filled.Add, contentDescription = "Log Payment")
            }
        }

        if (showAddDialog) {
            AddPaymentDialog(
                cards = cards,
                onDismiss = { showAddDialog = false },
                onConfirm = { cardId, amount, date, notes ->
                    viewModel.addPayment(cardId, amount, date, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun PaymentItem(
    payment: PaymentModel,
    card: CreditCard?,
    onDelete: () -> Unit
) {
    val bdtFormatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

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
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Paid",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = if (payment.notes.isBlank()) "Statement Payment" else payment.notes,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${card?.bank?.uppercase() ?: "Unknown Card"} • ${dateFormat.format(payment.date)}",
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
                    text = "৳" + bdtFormatter.format(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    cards: List<CreditCard>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: String, amount: Double, date: Long, notes: String) -> Unit
) {
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    var cardDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Payment", fontWeight = FontWeight.Bold) },
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

                // Card Selector
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
                    label = { Text("Payment Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payment Notes / Reference") },
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
                    errorText = "Please select a card."
                } else if (amtVal <= 0.0) {
                    errorText = "Please enter a payment amount greater than 0."
                } else {
                    onConfirm(activeCard.id, amtVal, System.currentTimeMillis(), notes)
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
