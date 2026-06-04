package com.example.creditcardtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = viewModel.cards
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Cards", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Outlined.AddCard, contentDescription = "Add Card")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cards added yet. Click + to add.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cards) { card ->
                        ManageCardItem(
                            card = card,
                            onDelete = { viewModel.deleteCard(card.id) }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddCardDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, bank, number, expiry, cvv, limit, statementDay, dueDay, fee, isRedeemable, redLimit, redUnit, colorIdx ->
                        viewModel.addCard(
                            name, bank, number, expiry, cvv, limit, statementDay, dueDay, fee, isRedeemable, redLimit, redUnit, colorIdx
                        )
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ManageCardItem(
    card: CreditCard,
    onDelete: () -> Unit
) {
    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF00B4D8), Color(0xFF0077B6))),
        Brush.linearGradient(listOf(Color(0xFFFB8500), Color(0xFFFFB703))),
        Brush.linearGradient(listOf(Color(0xFFEF476F), Color(0xFFFF8A80))),
        Brush.linearGradient(listOf(Color(0xFF7209B7), Color(0xFF3F37C9))),
        Brush.linearGradient(listOf(Color(0xFF2B2D42), Color(0xFF8D99AE)))
    )
    val brush = gradients[card.cardColorIndex % gradients.size]

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
                // Color swatch
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )

                Column {
                    Text(
                        text = "${card.bank.uppercase()} - ${card.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "•••• ${card.cardNumber.takeLast(4)} | Limit: $${card.creditLimit.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Statement: Day ${card.statementDay} | Due: Day ${card.dueDay}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Card",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, bank: String, number: String, expiry: String, cvv: String,
        limit: Double, statementDay: Int, dueDay: Int, fee: Double,
        isRedeemable: Boolean, redLimit: Double, redUnit: String, colorIdx: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var statementDay by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }
    var annualFee by remember { mutableStateOf("") }
    
    var isFeeRedeemable by remember { mutableStateOf(false) }
    var feeRedemptionLimit by remember { mutableStateOf("") }
    var feeRedemptionUnit by remember { mutableStateOf("Spend") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credit Card", fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (errorText.isNotEmpty()) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        label = { Text("Issuer Bank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Card Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it.filter { char -> char.isDigit() }.take(16) },
                        label = { Text("Card Number (16 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { expiry = it.take(5) },
                            label = { Text("Expiry (MM/YY)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it.filter { char -> char.isDigit() }.take(4) },
                            label = { Text("CVV") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = limit,
                        onValueChange = { limit = it },
                        label = { Text("Credit Limit ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = statementDay,
                            onValueChange = { statementDay = it.filter { char -> char.isDigit() }.take(2) },
                            label = { Text("Statement Day (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dueDay,
                            onValueChange = { dueDay = it.filter { char -> char.isDigit() }.take(2) },
                            label = { Text("Due Day (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = annualFee,
                        onValueChange = { annualFee = it },
                        label = { Text("Annual Fee ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    val feeVal = annualFee.toDoubleOrNull() ?: 0.0
                    if (feeVal > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isFeeRedeemable,
                                onCheckedChange = { isFeeRedeemable = it }
                            )
                            Text("Annual fee is waivable/redeemable")
                        }

                        if (isFeeRedeemable) {
                            OutlinedTextField(
                                value = feeRedemptionLimit,
                                onValueChange = { feeRedemptionLimit = it },
                                label = { Text("Redemption Target Value") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Redeem Unit:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = feeRedemptionUnit == "Spend",
                                        onClick = { feeRedemptionUnit = "Spend" }
                                    )
                                    Text("Spend ($)")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = feeRedemptionUnit == "Points",
                                        onClick = { feeRedemptionUnit = "Points" }
                                    )
                                    Text("Points")
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Select Card Color Theme:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color(0xFF00B4D8), // Theme 0
                            Color(0xFFFB8500), // Theme 1
                            Color(0xFFEF476F), // Theme 2
                            Color(0xFF7209B7), // Theme 3
                            Color(0xFF2B2D42)  // Theme 4
                        )
                        colors.forEachIndexed { idx, col ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(col)
                                    .clickable { selectedColorIndex = idx }
                                    .border(
                                        width = if (selectedColorIndex == idx) 3.dp else 0.dp,
                                        color = if (selectedColorIndex == idx) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val stmtDay = statementDay.toIntOrNull() ?: 0
                val dDay = dueDay.toIntOrNull() ?: 0
                val limVal = limit.toDoubleOrNull() ?: 0.0
                val feeVal = annualFee.toDoubleOrNull() ?: 0.0
                val redLim = feeRedemptionLimit.toDoubleOrNull() ?: 0.0

                if (bank.isBlank() || name.isBlank() || number.isBlank() || expiry.isBlank() || cvv.isBlank()) {
                    errorText = "Please fill in all card credentials."
                } else if (limVal <= 0.0) {
                    errorText = "Credit limit must be greater than 0."
                } else if (stmtDay !in 1..31 || dDay !in 1..31) {
                    errorText = "Statement and Due days must be between 1 and 31."
                } else if (expiry.length != 5 || !expiry.contains("/")) {
                    errorText = "Expiry date must be in MM/YY format."
                } else {
                    onConfirm(
                        name, bank, number, expiry, cvv, limVal, stmtDay, dDay, feeVal,
                        isFeeRedeemable, redLim, feeRedemptionUnit, selectedColorIndex
                    )
                }
            }) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
