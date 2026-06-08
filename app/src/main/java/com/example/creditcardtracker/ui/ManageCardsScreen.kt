package com.example.creditcardtracker.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Sms
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
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.theme.vaultGlass

val BANGLADESH_ISSUERS = listOf(
    "BRAC Bank",
    "City Bank",
    "Eastern Bank (EBL)",
    "Standard Chartered (SCB)",
    "Mutual Trust Bank (MTB)",
    "Dutch-Bangla Bank (DBBL)",
    "HSBC Bangladesh",
    "Prime Bank",
    "Dhaka Bank",
    "Bank Asia",
    "Trust Bank",
    "United Commercial Bank (UCB)",
    "Southeast Bank",
    "LankaBangla Finance",
    "IDLC Finance",
    "IPDC Finance",
    "Janata Bank",
    "Sonali Bank",
    "Agrani Bank"
)

val CARD_TYPES = listOf(
    "Visa",
    "Mastercard",
    "American Express",
    "UnionPay",
    "JCB",
    "Diners Club",
    "Discover"
)

val CARD_TIERS = listOf(
    "Classic",
    "Gold",
    "Titanium",
    "Platinum",
    "Signature",
    "Infinite",
    "World",
    "World Elite",
    "Emerald"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = viewModel.cards
    var showAddDialog by remember { mutableStateOf(false) }
    var smsConfigCard by remember { mutableStateOf<CreditCard?>(null) }

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
                            onConfigureSms = { smsConfigCard = card },
                            onDelete = { viewModel.deleteCard(card.id) }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddCardDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, bank, number, expiry, cvv, limit, statementDay, dueDay, fee, isRedeemable, redLimit, redUnit, colorIdx, isSmsTrackingEnabled, smsSender, cardType, cardTier ->
                        viewModel.addCard(
                            name = name,
                            bank = bank,
                            cardNumber = number,
                            expiryDate = expiry,
                            cvv = cvv,
                            creditLimit = limit,
                            statementDay = statementDay,
                            dueDay = dueDay,
                            annualFee = fee,
                            isFeeRedeemable = isRedeemable,
                            feeRedemptionLimit = redLimit,
                            feeRedemptionUnit = redUnit,
                            cardColorIndex = colorIdx,
                            isSmsTrackingEnabled = isSmsTrackingEnabled,
                            smsSender = smsSender,
                            cardType = cardType,
                            cardTier = cardTier
                        )
                        showAddDialog = false
                    }
                )
            }

            smsConfigCard?.let { card ->
                SmsTrackingConfigDialog(
                    card = card,
                    viewModel = viewModel,
                    onDismiss = { smsConfigCard = null }
                )
            }
        }
    }
}

@Composable
fun ManageCardItem(
    card: CreditCard,
    onConfigureSms: () -> Unit,
    onDelete: () -> Unit
) {
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
                // Mini Styled card theme block
                Box(
                    modifier = Modifier
                        .size(56.dp, 36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardDesignHelper.getTierBrush(card.safeCardTier))
                ) {
                    CardBackgroundWaves(tier = card.safeCardTier)
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp)) {
                        CardBrandLogo(
                            type = card.safeCardType,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp, 12.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${card.safeCardTier.uppercase()} | Day ${card.statementDay}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (card.isSmsTrackingEnabled) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SMS: ${card.smsSender}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(onClick = onConfigureSms) {
                    Icon(
                        imageVector = Icons.Outlined.Sms,
                        contentDescription = "SMS Setup",
                        tint = if (card.isSmsTrackingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDropdownSelector(
    label: String,
    options: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        val filteredOptions = remember(value) {
            if (value.isEmpty()) options
            else options.filter { it.contains(value, ignoreCase = true) }
        }

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
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
        isRedeemable: Boolean, redLimit: Double, redUnit: String, colorIdx: Int,
        isSmsTrackingEnabled: Boolean, smsSender: String, cardType: String, cardTier: String
    ) -> Unit
) {
    val context = LocalContext.current

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

    var cardType by remember { mutableStateOf("Visa") }
    var cardTier by remember { mutableStateOf("Classic") }

    // SMS Tracking states
    var isSmsTrackingEnabled by remember { mutableStateOf(false) }
    var smsSender by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }

    // Fetch suggested SMS senders
    val recentSmsSenders = remember(isSmsTrackingEnabled) {
        if (isSmsTrackingEnabled) {
            // Check permission first (handled in LaunchedEffect, but fetch what we can)
            try {
                BANGLADESH_ISSUERS // fallback hints or query
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[android.Manifest.permission.RECEIVE_SMS] ?: false
        val readGranted = permissions[android.Manifest.permission.READ_SMS] ?: false
        if (!receiveGranted || !readGranted) {
            Toast.makeText(context, "Permissions required for auto SMS tracking.", Toast.LENGTH_SHORT).show()
        }
    }

    val hasReceive = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasPost = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else true

    LaunchedEffect(isSmsTrackingEnabled) {
        if (isSmsTrackingEnabled) {
            val permissionsNeeded = mutableListOf<String>()
            if (!hasReceive) permissionsNeeded.add(android.Manifest.permission.RECEIVE_SMS)
            if (!hasRead) permissionsNeeded.add(android.Manifest.permission.READ_SMS)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !hasPost) {
                permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            if (permissionsNeeded.isNotEmpty()) {
                permissionLauncher.launch(permissionsNeeded.toTypedArray())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credit Card", fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
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

                // Live Preview Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CreditCardDesign(
                            bank = if (bank.isEmpty()) "ISSUER BANK" else bank,
                            name = if (name.isEmpty()) "CARDHOLDER NAME" else name,
                            cardNumber = number,
                            expiryDate = expiry,
                            cvv = cvv,
                            cardType = cardType,
                            cardTier = cardTier
                        )
                    }
                }

                item {
                    BankDropdownSelector(
                        label = "Issuer Bank / FI",
                        options = BANGLADESH_ISSUERS,
                        value = bank,
                        onValueChange = { bank = it }
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
                        onValueChange = { input ->
                            val clean = input.filter { char -> char.isDigit() }
                            val maxLen = CardDesignHelper.getCardNumberLengthRange(cardType).last
                            val limited = clean.take(maxLen)
                            number = limited

                            if (limited.isNotEmpty()) {
                                cardType = CardDesignHelper.detectCardType(limited)
                            }
                        },
                        label = { Text("Card Number (${CardDesignHelper.getCardNumberLengthRange(cardType).last} digits)") },
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
                        DropdownSelector(
                            label = "Card Type",
                            options = CARD_TYPES,
                            selectedOption = cardType,
                            onOptionSelected = { type ->
                                cardType = type
                                val maxLen = CardDesignHelper.getCardNumberLengthRange(type).last
                                if (number.length > maxLen) {
                                    number = number.take(maxLen)
                                }
                            },
                            modifier = Modifier.weight(1.5f)
                        )

                        DropdownSelector(
                            label = "Card Tier",
                            options = CARD_TIERS,
                            selectedOption = cardTier,
                            onOptionSelected = { cardTier = it },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
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

                // SMS Configuration Section
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable SMS Auto-Tracking", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isSmsTrackingEnabled,
                            onCheckedChange = { isSmsTrackingEnabled = it }
                        )
                    }
                }

                if (isSmsTrackingEnabled) {
                    item {
                        OutlinedTextField(
                            value = smsSender,
                            onValueChange = { smsSender = it },
                            label = { Text("SMS Sender ID (e.g. BRACBANK)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Suggested Senders:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BRACBANK", "CityBank", "SCB").forEach { sender ->
                                SuggestionChip(
                                    onClick = { smsSender = sender },
                                    label = { Text(sender) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Select Color Theme (Fallback):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color(0xFF00B4D8),
                            Color(0xFFFB8500),
                            Color(0xFFEF476F),
                            Color(0xFF7209B7),
                            Color(0xFF2B2D42)
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
                } else if (isSmsTrackingEnabled && smsSender.isBlank()) {
                    errorText = "Please specify an SMS Sender ID."
                } else {
                    onConfirm(
                        name, bank, number, expiry, cvv, limVal, stmtDay, dDay, feeVal,
                        isFeeRedeemable, redLim, feeRedemptionUnit, selectedColorIndex,
                        isSmsTrackingEnabled, smsSender.trim(), cardType, cardTier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTrackingConfigDialog(
    card: CreditCard,
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(card.isSmsTrackingEnabled) }
    var smsSender by remember { mutableStateOf(card.smsSender) }

    val recentSenders = remember(isEnabled) {
        if (isEnabled) viewModel.getRecentSmsSenders(context) else emptyList()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[android.Manifest.permission.RECEIVE_SMS] ?: false
        val readGranted = permissions[android.Manifest.permission.READ_SMS] ?: false
        if (!receiveGranted || !readGranted) {
            Toast.makeText(context, "Permissions required for auto SMS tracking.", Toast.LENGTH_SHORT).show()
        }
    }

    val hasReceive = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasPost = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else true

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            val permissionsNeeded = mutableListOf<String>()
            if (!hasReceive) permissionsNeeded.add(android.Manifest.permission.RECEIVE_SMS)
            if (!hasRead) permissionsNeeded.add(android.Manifest.permission.READ_SMS)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !hasPost) {
                permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            if (permissionsNeeded.isNotEmpty()) {
                permissionLauncher.launch(permissionsNeeded.toTypedArray())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SMS Auto-Tracking Setup", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure auto expense extraction for ${card.bank} - ${card.name}.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable SMS Tracking")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }

                if (isEnabled) {
                    Text(
                        text = "Identify the sender address of your bank alerts (e.g. BRACBANK, CityBank).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = smsSender,
                        onValueChange = { smsSender = it },
                        label = { Text("SMS Sender ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recentSenders.isNotEmpty()) {
                        Text("Suggested from your inbox:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentSenders.take(3).forEach { sender ->
                                SuggestionChip(
                                    onClick = { smsSender = sender },
                                    label = { Text(sender) }
                                )
                            }
                        }
                    } else {
                        // Fallback common bank sender formats
                        Text("Common formats:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BRACBANK", "CityBank", "SCB").forEach { sender ->
                                SuggestionChip(
                                    onClick = { smsSender = sender },
                                    label = { Text(sender) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isEnabled && smsSender.isBlank()) {
                        Toast.makeText(context, "Please specify an SMS Sender ID.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateCardSmsTracking(card.id, isEnabled, smsSender.trim())
                        onDismiss()
                    }
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
