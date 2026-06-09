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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
    "Agrani Bank",
    "ONE Bank",
    "AB Bank",
    "IFIC Bank",
    "Mercantile Bank",
    "Al-Arafah Islami Bank (AIBL)",
    "Islami Bank Bangladesh (IBBL)",
    "National Bank (NBL)",
    "Jamuna Bank",
    "Premier Bank",
    "NRB Bank",
    "SBAC Bank",
    "Midland Bank",
    "Community Bank Bangladesh",
    "Exim Bank",
    "Shahjalal Islami Bank (SJIBL)"
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

fun getBankHelplineDefault(bankName: String): String {
    val b = bankName.uppercase()
    return when {
        b.contains("BRAC") -> "16221"
        b.contains("CITY") -> "16234"
        b.contains("EASTERN") || b.contains("EBL") -> "16230"
        b.contains("CHARTERED") || b.contains("SCB") -> "16233"
        b.contains("MUTUAL") || b.contains("MTB") -> "16219"
        b.contains("DUTCH") || b.contains("DBBL") -> "16216"
        b.contains("HSBC") -> "16196"
        b.contains("LANKABANGLA") -> "16273"
        b.contains("SONALI") -> "16639"
        b.contains("JANATA") -> "16256"
        b.contains("PRIME") -> "16218"
        b.contains("DHAKA") -> "16203"
        b.contains("BANK ASIA") -> "16205"
        b.contains("TRUST") -> "16201"
        b.contains("UNITED") || b.contains("UCB") -> "16236"
        b.contains("SOUTHEAST") -> "16206"
        b.contains("IPDC") -> "16519"
        b.contains("IDLC") -> "16409"
        b.contains("ONE BANK") -> "16269"
        b.contains("AB BANK") -> "16207"
        b.contains("IFIC") -> "16255"
        b.contains("MERCANTILE") -> "16225"
        b.contains("AL-ARAFAH") || b.contains("AIBL") -> "16102"
        b.contains("ISLAMI BANK") || b.contains("IBBL") -> "16259"
        b.contains("NATIONAL BANK") || b.contains("NBL") -> "16224"
        b.contains("JAMUNA") -> "16742"
        b.contains("PREMIER") -> "16411"
        b.contains("NRB BANK") -> "16568"
        b.contains("SBAC") -> "16414"
        b.contains("MIDLAND") -> "16596"
        b.contains("COMMUNITY") -> "16607"
        b.contains("EXIM") -> "16246"
        b.contains("SHAHJALAL") || b.contains("SJIBL") -> "16237"
        else -> ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = viewModel.cards
    var showAddDialog by remember { mutableStateOf(false) }
    var configCard by remember { mutableStateOf<CreditCard?>(null) }

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
                    items(items = cards, key = { it.id }) { card ->
                        ManageCardItem(
                            card = card,
                            onConfigure = { configCard = card },
                            onDelete = { viewModel.deleteCard(card.id) }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddCardDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, bank, number, expiry, cvv, limit, statementDay, dueDay, fee, isRedeemable, redLimit, redUnit, colorIdx, isSmsTrackingEnabled, smsSender, cardType, cardTier, lounge, cashback, points, helpline ->
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
                            cardTier = cardTier,
                            annualLoungeQuota = lounge,
                            cashbackRate = cashback,
                            rewardPointsRate = points,
                            bankHelpline = helpline
                        )
                        showAddDialog = false
                    }
                )
            }

            configCard?.let { card ->
                CardConfigurationDialog(
                    card = card,
                    viewModel = viewModel,
                    onDismiss = { configCard = null }
                )
            }
        }
    }
}

@Composable
fun ManageCardItem(
    card: CreditCard,
    onConfigure: () -> Unit,
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${card.safeCardTier.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (card.annualLoungeQuota > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Lounge: ${card.annualLoungeQuota}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        if (card.isSmsTrackingEnabled) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SMS: ${card.smsSender}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.primary
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
                IconButton(onClick = onConfigure) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configure Features",
                        tint = MaterialTheme.colorScheme.primary
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
        isSmsTrackingEnabled: Boolean, smsSender: String, cardType: String, cardTier: String,
        annualLoungeQuota: Int, cashbackRate: Double, rewardPointsRate: Double, bankHelpline: String
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
    var showExpiryPicker by remember { mutableStateOf(false) }

    // Advanced fields
    var annualLoungeQuota by remember { mutableStateOf("") }
    var cashbackRate by remember { mutableStateOf("") }
    var rewardPointsRate by remember { mutableStateOf("") }
    var bankHelpline by remember { mutableStateOf("") }

    // SMS Tracking states
    var isSmsTrackingEnabled by remember { mutableStateOf(false) }
    var smsSender by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }

    // Auto-update default helpline when bank changes
    LaunchedEffect(bank) {
        if (bank.isNotEmpty()) {
            val defaultHelp = getBankHelplineDefault(bank)
            if (defaultHelp.isNotEmpty() && bankHelpline.isEmpty()) {
                bankHelpline = defaultHelp
            }
        }
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
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expiry (MM/YY)") },
                            trailingIcon = {
                                IconButton(onClick = { showExpiryPicker = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Event,
                                        contentDescription = "Select Expiry MM/YY"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showExpiryPicker = true }
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

                // Advanced limits/helper features inputs
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Complimentary Lounges & Helplines", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = annualLoungeQuota,
                            onValueChange = { annualLoungeQuota = it.filter { char -> char.isDigit() }.take(2) },
                            label = { Text("Annual Lounge Visits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bankHelpline,
                            onValueChange = { bankHelpline = it },
                            label = { Text("Helpline Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }

                item {
                    Text("Cashbacks & Reward Rates", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cashbackRate,
                            onValueChange = { cashbackRate = it },
                            label = { Text("Cashback (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rewardPointsRate,
                            onValueChange = { rewardPointsRate = it },
                            label = { Text("Reward Points Rate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
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
                
                val lQuota = annualLoungeQuota.toIntOrNull() ?: 0
                val cbRate = cashbackRate.toDoubleOrNull() ?: 0.0
                val rwPoints = rewardPointsRate.toDoubleOrNull() ?: 0.0

                if (bank.isBlank() || name.isBlank() || number.isBlank() || expiry.isBlank() || cvv.isBlank()) {
                    errorText = "Please fill in all card credentials."
                } else if (limVal <= 0.0) {
                    errorText = "Credit limit must be greater than 0."
                } else if (limVal > 4000000.0) {
                    errorText = "Credit limit cannot exceed the BB circular ceiling of BDT 40 Lakh (৳40,00,000)."
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
                        isSmsTrackingEnabled, smsSender.trim(), cardType, cardTier,
                        lQuota, cbRate, rwPoints, bankHelpline.trim()
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

    if (showExpiryPicker) {
        ExpiryPickerDialog(
            onDismiss = { showExpiryPicker = false },
            onConfirm = { m, y ->
                expiry = "$m/$y"
                showExpiryPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardConfigurationDialog(
    card: CreditCard,
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var isSmsEnabled by remember { mutableStateOf(card.isSmsTrackingEnabled) }
    var smsSender by remember { mutableStateOf(card.smsSender) }
    var annualLoungeQuota by remember { mutableStateOf(card.annualLoungeQuota.toString()) }
    var bankHelpline by remember { mutableStateOf(card.bankHelpline) }
    var cashbackRate by remember { mutableStateOf(card.cashbackRate.toString()) }
    var rewardPointsRate by remember { mutableStateOf(card.rewardPointsRate.toString()) }

    val recentSenders = remember(isSmsEnabled) {
        if (isSmsEnabled) viewModel.getRecentSmsSenders(context) else emptyList()
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

    LaunchedEffect(isSmsEnabled) {
        if (isSmsEnabled) {
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
        title = { Text("Configure Features", fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Customize features for ${card.bank} - ${card.name}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // SMS Auto-Tracking Section
                item {
                    Text("SMS Auto-Tracking", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable SMS Tracking")
                        Switch(
                            checked = isSmsEnabled,
                            onCheckedChange = { isSmsEnabled = it }
                        )
                    }
                }

                if (isSmsEnabled) {
                    item {
                        OutlinedTextField(
                            value = smsSender,
                            onValueChange = { smsSender = it },
                            label = { Text("SMS Sender ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
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
                            Text("Common bank formats:", style = MaterialTheme.typography.labelSmall)
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

                // Lounges & Helpline Section
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Airport Lounge & Helplines", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = annualLoungeQuota,
                            onValueChange = { annualLoungeQuota = it.filter { char -> char.isDigit() }.take(2) },
                            label = { Text("Annual Lounge Visits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bankHelpline,
                            onValueChange = { bankHelpline = it },
                            label = { Text("Helpline Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }

                // Rewards & Cashbacks
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cashbacks & Reward Rates", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cashbackRate,
                            onValueChange = { cashbackRate = it },
                            label = { Text("Cashback (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rewardPointsRate,
                            onValueChange = { rewardPointsRate = it },
                            label = { Text("Reward Points Rate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isSmsEnabled && smsSender.isBlank()) {
                        Toast.makeText(context, "Please specify an SMS Sender ID.", Toast.LENGTH_SHORT).show()
                    } else {
                        val quotaVal = annualLoungeQuota.toIntOrNull() ?: 0
                        val cbVal = cashbackRate.toDoubleOrNull() ?: 0.0
                        val rwVal = rewardPointsRate.toDoubleOrNull() ?: 0.0
                        
                        viewModel.updateCardSettings(
                            cardId = card.id,
                            isSmsEnabled = isSmsEnabled,
                            smsSender = smsSender.trim(),
                            loungeQuota = quotaVal,
                            cashbackRate = cbVal,
                            rewardPointsRate = rwVal,
                            helpline = bankHelpline.trim()
                        )
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

@Composable
fun ExpiryPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (month: String, year: String) -> Unit
) {
    val months = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    val years = (26..40).map { it.toString() } // 2026-2040

    var selectedMonth by remember { mutableStateOf("06") }
    var selectedYear by remember { mutableStateOf("26") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Expiry Date", fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Months Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Month", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Box(modifier = Modifier.height(200.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(months) { m ->
                                val isSelected = m == selectedMonth
                                Text(
                                    text = m,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMonth = m }
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .padding(vertical = 12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Years Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Year", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Box(modifier = Modifier.height(200.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(years) { y ->
                                val isSelected = y == selectedYear
                                Text(
                                    text = "'$y",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedYear = y }
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .padding(vertical = 12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedMonth, selectedYear) }
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

