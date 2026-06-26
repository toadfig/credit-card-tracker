package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AccountBalance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Account
import com.example.creditcardtracker.data.AccountType
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.util.Locale
import java.util.Currency

val BANGLADESH_ISSUERS = listOf(
    "BRAC Bank",
    "Eastern Bank (EBL)",
    "City Bank",
    "Standard Chartered (SCB)",
    "Mutual Trust Bank (MTB)",
    "Dutch-Bangla Bank (DBBL)",
    "HSBC Bangladesh",
    "Prime Bank",
    "Dhaka Bank",
    "Bank Asia",
    "Trust Bank",
    "United Commercial Bank (UCB)",
    "LankaBangla Finance",
    "IDLC Finance",
    "Janata Bank",
    "Sonali Bank",
    "Agrani Bank"
)

val CARD_TYPES = listOf("Visa", "Mastercard", "Diners Club", "American Express", "UnionPay", "JCB")
val CARD_TIERS = listOf("Classic", "Gold", "Titanium", "Platinum", "Signature", "Infinite", "Emerald")

data class CardPreset(
    val presetName: String,
    val name: String,
    val bank: String,
    val cardType: String,
    val cardTier: String,
    val annualFee: Double,
    val isFeeRedeemable: Boolean,
    val feeRedemptionLimit: Double,
    val feeRedemptionUnit: String,
    val annualLoungeQuota: Int,
    val cashbackRate: Double,
    val rewardPointsRate: Double,
    val bankHelpline: String,
    val smsSender: String
)

val CARD_PRESETS = listOf(
    CardPreset(
        presetName = "BRAC Bank Visa Platinum Flexi",
        name = "Visa Platinum Flexi",
        bank = "BRAC Bank",
        cardType = "Visa",
        cardTier = "Platinum",
        annualFee = 5000.0,
        isFeeRedeemable = true,
        feeRedemptionLimit = 2500.0,
        feeRedemptionUnit = "Points",
        annualLoungeQuota = 99, // unlimited Balaka
        cashbackRate = 0.0,
        rewardPointsRate = 1.0 / 80.0,
        bankHelpline = "16221",
        smsSender = "BRACBANK"
    ),
    CardPreset(
        presetName = "BRAC Bank Diners Club Emerald",
        name = "Diners Club Emerald",
        bank = "BRAC Bank",
        cardType = "Diners Club",
        cardTier = "Emerald",
        annualFee = 6000.0,
        isFeeRedeemable = true,
        feeRedemptionLimit = 3000.0,
        feeRedemptionUnit = "Points",
        annualLoungeQuota = 3,
        cashbackRate = 0.015,
        rewardPointsRate = 2.0 / 80.0,
        bankHelpline = "16221",
        smsSender = "BRACBANK"
    ),
    CardPreset(
        presetName = "EBL ShareTrip Mastercard Titanium",
        name = "ShareTrip Titanium Mastercard",
        bank = "Eastern Bank (EBL)",
        cardType = "Mastercard",
        cardTier = "Titanium",
        annualFee = 5000.0,
        isFeeRedeemable = true,
        feeRedemptionLimit = 24.0,
        feeRedemptionUnit = "Transactions",
        annualLoungeQuota = 6,
        cashbackRate = 0.0,
        rewardPointsRate = 1.0 / 50.0,
        bankHelpline = "16230",
        smsSender = "EBL"
    ),
    CardPreset(
        presetName = "EBL Stellar Platinum Visa",
        name = "Stellar Platinum Visa",
        bank = "Eastern Bank (EBL)",
        cardType = "Visa",
        cardTier = "Platinum",
        annualFee = 8000.0,
        isFeeRedeemable = true,
        feeRedemptionLimit = 24.0,
        feeRedemptionUnit = "Transactions",
        annualLoungeQuota = 99,
        cashbackRate = 0.0,
        rewardPointsRate = 1.5 / 50.0,
        bankHelpline = "16230",
        smsSender = "EBL"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = viewModel.accounts
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAccountForConfig by remember { mutableStateOf<Account?>(null) }
    val context = LocalContext.current

    val inLocale = Locale("en", "IN")
    val bdtFormatter = remember { 
        val formatter = NumberFormat.getCurrencyInstance(inLocale)
        formatter.currency = Currency.getInstance("BDT")
        formatter
    }
    fun formatBdt(amount: Double): String {
        return bdtFormatter.format(amount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Accounts & Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Outlined.AddCard, contentDescription = "Add Account")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F131E))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (accounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No accounts configured yet. Tap + to add.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts) { account ->
                        AccountItemRow(
                            account = account,
                            formatBdt = ::formatBdt,
                            onClick = { selectedAccountForConfig = account },
                            onDelete = { viewModel.deleteAccount(account.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    selectedAccountForConfig?.let { account ->
        ConfigureSmsLoungeDialog(
            account = account,
            viewModel = viewModel,
            onDismiss = { selectedAccountForConfig = null }
        )
    }
}

@Composable
fun AccountItemRow(
    account: Account,
    formatBdt: (Double) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .vaultGlass(borderRadius = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${account.bank} - ${account.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Type: ${account.accountType.name} • ${if (account.accountType == AccountType.CREDIT_CARD) "Outstanding" else "Balance"}: ${formatBdt(account.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                if (account.isSmsTrackingEnabled) {
                    Text(
                        text = "SMS: Enabled (${account.smsSender})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(1) } // 1: Bank Selection Grid, 2: Authorization, 3: Manual Form
    var selectedBank by remember { mutableStateOf("") }
    
    // Authorization state
    var isConnecting by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableStateOf(0) } // 0: Savings, 1: Credit Card
    
    // Manual form state
    var accountType by remember { mutableStateOf(AccountType.CREDIT_CARD) }
    var bank by remember { mutableStateOf(BANGLADESH_ISSUERS[0]) }
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("0") }
    var creditLimitText by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var statementDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("1") }
    var annualFeeText by remember { mutableStateOf("0") }
    var isFeeRedeemable by remember { mutableStateOf(false) }
    var feeRedemptionLimitText by remember { mutableStateOf("") }
    var feeRedemptionUnit by remember { mutableStateOf("Spend") }
    var cardType by remember { mutableStateOf("Visa") }
    var cardTier by remember { mutableStateOf("Classic") }
    var annualLoungeQuotaText by remember { mutableStateOf("0") }
    var cashbackRateText by remember { mutableStateOf("0") }
    var rewardPointsRateText by remember { mutableStateOf("0") }
    var helpline by remember { mutableStateOf("") }
    var isSmsEnabled by remember { mutableStateOf(false) }
    var smsSender by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableStateOf(-1) }

    var showTypeDropdown by remember { mutableStateOf(false) }
    var showBankDropdown by remember { mutableStateOf(false) }
    var showPresetDropdown by remember { mutableStateOf(false) }
    var showCardTypeDropdown by remember { mutableStateOf(false) }
    var showCardTierDropdown by remember { mutableStateOf(false) }
    var showRedeemUnitDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (step) {
                    1 -> "Select Your Bank"
                    2 -> "Authorize Access"
                    else -> "Add Account / Card"
                },
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (step) {
                    1 -> {
                        // Search bar (visual only)
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Search for your bank...", color = Color.White.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledTextColor = Color.White
                            )
                        )
                        
                        Text("Popular Institutions", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        
                        // 2x3 grid of popular options
                        val popularOptions = listOf(
                            "BRAC Bank" to Color(0xFF0D5C75),
                            "City Bank" to Color(0xFF5C0D75),
                            "DBBL" to Color(0xFF0D752E),
                            "StanChart" to Color(0xFF75500D),
                            "Eastern Bank (EBL)" to Color(0xFF2C0D75),
                            "bKash" to Color(0xFFE2125B)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in popularOptions.indices step 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (j in i..i+1) {
                                        if (j < popularOptions.size) {
                                            val (bName, bColor) = popularOptions[j]
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(80.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(bColor.copy(alpha = 0.3f))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        selectedBank = bName
                                                        step = 2
                                                        selectedOptionIndex = 0
                                                    }
                                                    .padding(12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.AccountBalance,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.9f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = bName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Custom setup button
                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(Icons.Outlined.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Other Account (Manual Setup)", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    2 -> {
                        // Authorization Flow
                        if (isConnecting) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Establishing secure connection to $selectedBank...", color = Color.White)
                                }
                            }
                        } else {
                            Text(
                                text = "Money Manager needs permission to securely sync your transaction history and account balances from $selectedBank.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Select Account to Link", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            
                            // Let the user select the account type
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val options = if (selectedBank == "bKash") {
                                    listOf("bKash MFS Wallet (৳ 45,000.00 Balance)")
                                } else {
                                    listOf(
                                        "Checking / Savings Account (৳ 2,50,000.00 Balance)",
                                        "Credit Card (Visa Platinum - ৳ 3,00,000.00 Limit)"
                                    )
                                }
                                
                                options.forEachIndexed { idx, opt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selectedOptionIndex == idx) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                            .clickable { selectedOptionIndex = idx }
                                            .border(1.dp, if (selectedOptionIndex == idx) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedOptionIndex == idx,
                                            onClick = { selectedOptionIndex = idx },
                                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Permissions Requested", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            
                            // Permission items
                            val permissions = listOf(
                                Icons.Outlined.Receipt to "Transaction History" to "Up to 24 months of historical data to categorize your spending patterns.",
                                Icons.Outlined.AccountBalanceWallet to "Account Balances" to "Real-time balance updates to help keep your budget calculations accurate.",
                                Icons.Outlined.Person to "Identity Verification" to "Basic profile information including name and account type for verification."
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                permissions.forEach { perm ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Icon(
                                            imageVector = perm.first.first,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(perm.first.second, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text(perm.second, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Privacy disclaimer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Your credentials are never stored. We use 256-bit bank-grade encryption to secure your data transfer.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                    3 -> {
                        // Account Type Selector
                        Text("Account Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { showTypeDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(accountType.name, color = Color.White)
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                }
                            }
                            DropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                                AccountType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            accountType = type
                                            showTypeDropdown = false
                                            if (type == AccountType.MFS) {
                                                bank = "bKash"
                                                name = "bKash Wallet"
                                            } else if (type == AccountType.CASH) {
                                                bank = "Cash"
                                                name = "Cash Ledger"
                                            } else {
                                                bank = BANGLADESH_ISSUERS[0]
                                                name = ""
                                            }
                                            selectedPresetIndex = -1
                                        }
                                    )
                                }
                            }
                        }

                        // Preset Selector for Credit Cards
                        if (accountType == AccountType.CREDIT_CARD) {
                            Text("Select Card Preset (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { showPresetDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(if (selectedPresetIndex >= 0) CARD_PRESETS[selectedPresetIndex].presetName else "Custom / Manual", color = Color.White)
                                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                    }
                                }
                                DropdownMenu(expanded = showPresetDropdown, onDismissRequest = { showPresetDropdown = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                                    DropdownMenuItem(
                                        text = { Text("Custom / Manual") },
                                        onClick = {
                                            selectedPresetIndex = -1
                                            showPresetDropdown = false
                                        }
                                    )
                                    CARD_PRESETS.forEachIndexed { index, preset ->
                                        DropdownMenuItem(
                                            text = { Text(preset.presetName) },
                                            onClick = {
                                                selectedPresetIndex = index
                                                showPresetDropdown = false
                                                bank = preset.bank
                                                name = preset.name
                                                cardType = preset.cardType
                                                cardTier = preset.cardTier
                                                annualFeeText = preset.annualFee.toString()
                                                isFeeRedeemable = preset.isFeeRedeemable
                                                feeRedemptionLimitText = preset.feeRedemptionLimit.toString()
                                                feeRedemptionUnit = preset.feeRedemptionUnit
                                                annualLoungeQuotaText = preset.annualLoungeQuota.toString()
                                                cashbackRateText = (preset.cashbackRate * 100).toString()
                                                rewardPointsRateText = preset.rewardPointsRate.toString()
                                                helpline = preset.bankHelpline
                                                smsSender = preset.smsSender
                                                isSmsEnabled = preset.smsSender.isNotEmpty()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Bank/Issuer Selector
                        if (accountType != AccountType.CASH) {
                            Text(if (accountType == AccountType.MFS) "MFS Provider" else "Bank / Issuer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                            if (accountType == AccountType.MFS) {
                                var mfsDropdown by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(onClick = { mfsDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(bank, color = Color.White)
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                        }
                                    }
                                    DropdownMenu(expanded = mfsDropdown, onDismissRequest = { mfsDropdown = false }) {
                                        listOf("bKash", "Nagad", "Rocket", "Upay", "CellFin", "Others").forEach { mfs ->
                                            DropdownMenuItem(text = { Text(mfs) }, onClick = {
                                                bank = mfs
                                                name = "$mfs Wallet"
                                                mfsDropdown = false
                                            })
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(onClick = { showBankDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(bank, color = Color.White)
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                        }
                                    }
                                    DropdownMenu(expanded = showBankDropdown, onDismissRequest = { showBankDropdown = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                                        BANGLADESH_ISSUERS.forEach { issuer ->
                                            DropdownMenuItem(text = { Text(issuer) }, onClick = {
                                                bank = issuer
                                                showBankDropdown = false
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Account Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Starting Balance (or outstanding)
                        OutlinedTextField(
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            label = { Text(if (accountType == AccountType.CREDIT_CARD) "Starting Outstanding Balance" else "Starting Available Balance") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Credit Card Specifics
                        if (accountType == AccountType.CREDIT_CARD) {
                            OutlinedTextField(
                                value = creditLimitText,
                                onValueChange = { creditLimitText = it },
                                label = { Text("Credit Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it.filter { char -> char.isDigit() } },
                                label = { Text("Card Number (digits)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = expiryDate,
                                    onValueChange = { expiryDate = it },
                                    label = { Text("Expiry (MM/YY)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = { cvv = it },
                                    label = { Text("CVV") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = statementDay,
                                    onValueChange = { statementDay = it.filter { char -> char.isDigit() } },
                                    label = { Text("Statement Day (1-31)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                OutlinedTextField(
                                    value = dueDay,
                                    onValueChange = { dueDay = it.filter { char -> char.isDigit() } },
                                    label = { Text("Due Day (1-31)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }

                            // Card type and tier
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { showCardTypeDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cardType, color = Color.White)
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                        }
                                    }
                                    DropdownMenu(expanded = showCardTypeDropdown, onDismissRequest = { showCardTypeDropdown = false }) {
                                        CARD_TYPES.forEach { ct ->
                                            DropdownMenuItem(text = { Text(ct) }, onClick = {
                                                cardType = ct
                                                showCardTypeDropdown = false
                                            })
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { showCardTierDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cardTier, color = Color.White)
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                        }
                                    }
                                    DropdownMenu(expanded = showCardTierDropdown, onDismissRequest = { showCardTierDropdown = false }) {
                                        CARD_TIERS.forEach { tier ->
                                            DropdownMenuItem(text = { Text(tier) }, onClick = {
                                                cardTier = tier
                                                showCardTierDropdown = false
                                            })
                                        }
                                    }
                                }
                            }

                            // Annual Fee
                            OutlinedTextField(
                                value = annualFeeText,
                                onValueChange = { annualFeeText = it },
                                label = { Text("Annual Fee") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fee Waiver Redeemable", fontWeight = FontWeight.Bold, color = Color.White)
                                Switch(checked = isFeeRedeemable, onCheckedChange = { isFeeRedeemable = it })
                            }

                            if (isFeeRedeemable) {
                                OutlinedTextField(
                                    value = feeRedemptionLimitText,
                                    onValueChange = { feeRedemptionLimitText = it },
                                    label = { Text("Redemption Cap Limit") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(onClick = { showRedeemUnitDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Redeem Unit: $feeRedemptionUnit", color = Color.White)
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                        }
                                    }
                                    DropdownMenu(expanded = showRedeemUnitDropdown, onDismissRequest = { showRedeemUnitDropdown = false }) {
                                        listOf("Spend", "Points", "Transactions").forEach { unit ->
                                            DropdownMenuItem(text = { Text(unit) }, onClick = {
                                                feeRedemptionUnit = unit
                                                showRedeemUnitDropdown = false
                                            })
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = annualLoungeQuotaText,
                                onValueChange = { annualLoungeQuotaText = it.filter { char -> char.isDigit() } },
                                label = { Text("Annual Lounge Quota (Visits)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = cashbackRateText,
                                onValueChange = { cashbackRateText = it },
                                label = { Text("Cashback Rate (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = rewardPointsRateText,
                                onValueChange = { rewardPointsRateText = it },
                                label = { Text("Reward Points Rate (Points per spent)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = helpline,
                                onValueChange = { helpline = it },
                                label = { Text("Bank Helpline") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // SMS Configuration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable SMS Parsing", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Extract transaction amounts dynamically from SMS inbox alerts.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                            }
                            Switch(checked = isSmsEnabled, onCheckedChange = { isSmsEnabled = it })
                        }

                        if (isSmsEnabled) {
                            OutlinedTextField(
                                value = smsSender,
                                onValueChange = { smsSender = it },
                                label = { Text("SMS Sender Name (e.g. BRACBANK, EBL)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (step) {
                1 -> {
                    // No confirm button needed for grid select
                }
                2 -> {
                    if (!isConnecting) {
                        TextButton(
                            onClick = {
                                isConnecting = true
                                scope.launch {
                                    delay(1500) // Simulate connecting progress
                                    
                                    // Add the selected account to database
                                    if (selectedBank == "bKash") {
                                        viewModel.addAccount(
                                            name = "bKash Wallet",
                                            bank = "bKash",
                                            accountType = AccountType.MFS,
                                            balance = 45000.0,
                                            creditLimit = 0.0,
                                            cardNumber = "",
                                            expiryDate = "",
                                            cvv = "",
                                            statementDay = 1,
                                            dueDay = 1,
                                            annualFee = 0.0,
                                            isFeeRedeemable = false,
                                            feeRedemptionLimit = 0.0,
                                            feeRedemptionUnit = "Spend",
                                            accountColorIndex = 4,
                                            isSmsTrackingEnabled = true,
                                            smsSender = "bKash",
                                            cardType = "",
                                            cardTier = "",
                                            annualLoungeQuota = 0,
                                            cashbackRate = 0.0,
                                            rewardPointsRate = 0.0,
                                            bankHelpline = "16247"
                                        )
                                    } else {
                                        val actualBank = when (selectedBank) {
                                            "DBBL" -> "Dutch-Bangla Bank (DBBL)"
                                            "StanChart" -> "Standard Chartered (SCB)"
                                            "Eastern Bank (EBL)" -> "Eastern Bank (EBL)"
                                            else -> selectedBank
                                        }
                                        
                                        if (selectedOptionIndex == 0) {
                                            // Savings account
                                            viewModel.addAccount(
                                                name = "Checking / Savings",
                                                bank = actualBank,
                                                accountType = AccountType.BANK_ACCOUNT,
                                                balance = 250000.0,
                                                creditLimit = 0.0,
                                                cardNumber = "",
                                                expiryDate = "",
                                                cvv = "",
                                                statementDay = 1,
                                                dueDay = 1,
                                                annualFee = 0.0,
                                                isFeeRedeemable = false,
                                                feeRedemptionLimit = 0.0,
                                                feeRedemptionUnit = "Spend",
                                                accountColorIndex = 1,
                                                isSmsTrackingEnabled = false,
                                                smsSender = "",
                                                cardType = "",
                                                cardTier = "",
                                                annualLoungeQuota = 0,
                                                cashbackRate = 0.0,
                                                rewardPointsRate = 0.0,
                                                bankHelpline = ""
                                            )
                                        } else {
                                            // Credit Card preset based on bank
                                            val limit = 300000.0
                                            val cTier = if (selectedBank == "StanChart") "Signature" else "Platinum"
                                            val cType = if (selectedBank == "City Bank") "American Express" else "Visa"
                                            val fee = if (selectedBank == "City Bank") 10000.0 else 5000.0
                                            val sender = when (selectedBank) {
                                                "BRAC Bank" -> "BRACBANK"
                                                "City Bank" -> "CITYBANK"
                                                "DBBL" -> "DBBL"
                                                "StanChart" -> "SCB"
                                                "Eastern Bank (EBL)" -> "EBL"
                                                else -> "BANK"
                                            }
                                            val namePreset = "$cType $cTier"
                                            
                                            viewModel.addAccount(
                                                name = namePreset,
                                                bank = actualBank,
                                                accountType = AccountType.CREDIT_CARD,
                                                balance = 12450.0,
                                                creditLimit = limit,
                                                cardNumber = "4321",
                                                expiryDate = "12/29",
                                                cvv = "999",
                                                statementDay = 15,
                                                dueDay = 2,
                                                annualFee = fee,
                                                isFeeRedeemable = true,
                                                feeRedemptionLimit = 2500.0,
                                                feeRedemptionUnit = "Points",
                                                accountColorIndex = 2,
                                                isSmsTrackingEnabled = true,
                                                smsSender = sender,
                                                cardType = cType,
                                                cardTier = cTier,
                                                annualLoungeQuota = 8,
                                                cashbackRate = 0.01,
                                                rewardPointsRate = 1.0,
                                                bankHelpline = "16221"
                                            )
                                        }
                                    }
                                    isConnecting = false
                                    Toast.makeText(context, "$selectedBank successfully linked!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Authorize Access", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                3 -> {
                    TextButton(
                        onClick = {
                            val bal = balanceText.toDoubleOrNull() ?: 0.0
                            val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                            val fee = annualFeeText.toDoubleOrNull() ?: 0.0
                            val waiveLimit = feeRedemptionLimitText.toDoubleOrNull() ?: 0.0
                            val lounge = annualLoungeQuotaText.toIntOrNull() ?: 0
                            val cashback = (cashbackRateText.toDoubleOrNull() ?: 0.0) / 100.0
                            val pointsRate = rewardPointsRateText.toDoubleOrNull() ?: 0.0
                            val stDay = statementDay.toIntOrNull() ?: 1
                            val dDay = dueDay.toIntOrNull() ?: 1

                            if (name.isBlank() || (accountType != AccountType.CASH && bank.isBlank())) {
                                Toast.makeText(context, "Fill in required credentials.", Toast.LENGTH_SHORT).show()
                            } else if (accountType == AccountType.CREDIT_CARD && limit <= 0.0) {
                                Toast.makeText(context, "Please enter a valid credit limit.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addAccount(
                                    name = name.trim(),
                                    bank = bank.trim(),
                                    accountType = accountType,
                                    balance = bal,
                                    creditLimit = limit,
                                    cardNumber = cardNumber.trim(),
                                    expiryDate = expiryDate.trim(),
                                    cvv = cvv.trim(),
                                    statementDay = stDay,
                                    dueDay = dDay,
                                    annualFee = fee,
                                    isFeeRedeemable = isFeeRedeemable,
                                    feeRedemptionLimit = waiveLimit,
                                    feeRedemptionUnit = feeRedemptionUnit,
                                    accountColorIndex = if (accountType == AccountType.CREDIT_CARD) (selectedPresetIndex.coerceAtLeast(0) % 5) else 0,
                                    isSmsTrackingEnabled = isSmsEnabled,
                                    smsSender = smsSender.trim(),
                                    cardType = cardType,
                                    cardTier = cardTier,
                                    annualLoungeQuota = lounge,
                                    cashbackRate = cashback,
                                    rewardPointsRate = pointsRate,
                                    bankHelpline = helpline.trim()
                                )
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Save Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step > 1 && !isConnecting) {
                        step = 1
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (step > 1 && !isConnecting) "Back" else "Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureSmsLoungeDialog(
    account: Account,
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSmsEnabled by remember { mutableStateOf(account.isSmsTrackingEnabled) }
    var smsSender by remember { mutableStateOf(account.smsSender) }
    var loungeQuotaText by remember { mutableStateOf(account.annualLoungeQuota.toString()) }
    var cashbackRateText by remember { mutableStateOf((account.cashbackRate * 100).toString()) }
    var pointsRateText by remember { mutableStateOf(account.rewardPointsRate.toString()) }
    var helpline by remember { mutableStateOf(account.bankHelpline) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Settings: ${account.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable SMS Tracking", fontWeight = FontWeight.Bold)
                    Switch(checked = isSmsEnabled, onCheckedChange = { isSmsEnabled = it })
                }

                if (isSmsEnabled) {
                    OutlinedTextField(
                        value = smsSender,
                        onValueChange = { smsSender = it },
                        label = { Text("SMS Sender Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (account.accountType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = loungeQuotaText,
                        onValueChange = { loungeQuotaText = it.filter { char -> char.isDigit() } },
                        label = { Text("Annual Lounge Quota (Visits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cashbackRateText,
                        onValueChange = { cashbackRateText = it },
                        label = { Text("Cashback Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pointsRateText,
                        onValueChange = { pointsRateText = it },
                        label = { Text("Points Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = helpline,
                        onValueChange = { helpline = it },
                        label = { Text("Helpline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lounge = loungeQuotaText.toIntOrNull() ?: 0
                    val cashback = (cashbackRateText.toDoubleOrNull() ?: 0.0) / 100.0
                    val points = pointsRateText.toDoubleOrNull() ?: 0.0
                    
                    viewModel.updateAccountSettings(
                        accountId = account.id,
                        isSmsEnabled = isSmsEnabled,
                        smsSender = smsSender.trim(),
                        loungeQuota = lounge,
                        cashbackRate = cashback,
                        rewardPointsRate = points,
                        helpline = helpline.trim()
                    )
                    Toast.makeText(context, "Configurations updated.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Save Details", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
