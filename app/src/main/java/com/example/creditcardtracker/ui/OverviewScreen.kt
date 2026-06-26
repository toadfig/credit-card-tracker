package com.example.creditcardtracker.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Account
import com.example.creditcardtracker.data.AccountType
import com.example.creditcardtracker.data.Transaction
import com.example.creditcardtracker.data.TransactionType
import com.example.creditcardtracker.data.LoungeVisit
import com.example.creditcardtracker.data.EmiPlan
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.vaultGlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverviewScreen(
    viewModel: TrackerViewModel,
    onManageCardsClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onPaymentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = viewModel.accounts
    val selectedIndex by viewModel.selectedIndex
    val context = LocalContext.current

    // Calculation of Net Worth
    val totalAssets = accounts.filter { it.accountType != AccountType.CREDIT_CARD }.sumOf { it.balance }
    val totalLiabilities = accounts.filter { it.accountType == AccountType.CREDIT_CARD }.sumOf { it.balance }
    val netWorth = totalAssets - totalLiabilities

    val inLocale = Locale("en", "IN")
    val bdtFormatter = remember { 
        val formatter = NumberFormat.getCurrencyInstance(inLocale)
        formatter.currency = Currency.getInstance("BDT")
        formatter
    }
    fun formatBdt(amount: Double): String {
        return bdtFormatter.format(amount)
    }

    var showTxDialog by remember { mutableStateOf(false) }
    var txDialogType by remember { mutableStateOf(TransactionType.EXPENSE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (accounts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .vaultGlass(borderRadius = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "No Accounts",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Secure Money Manager",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure bank accounts, credit cards, bKash/Nagad wallets, or cash ledger in one secure, fully offline space.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onManageCardsClick,
                        shape = VaultUiTokens.ShapeFullPill
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Account")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add First Account")
                    }
                }
            }
        } else {
            val currentAccount = accounts.getOrNull(selectedIndex) ?: accounts[0]
            val lazyListState = rememberLazyListState()

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Net Worth Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vaultGlass(borderRadius = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Net Worth",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatBdt(netWorth),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (netWorth >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Assets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatBdt(totalAssets), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Liabilities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatBdt(totalLiabilities), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Swipable Accounts Carousel
                LazyRow(
                    state = lazyListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(items = accounts, key = { _, account -> account.id }) { index, account ->
                        AccountWidget(
                            account = account,
                            isSelected = index == selectedIndex,
                            viewModel = viewModel,
                            onClick = { viewModel.selectedIndex.value = index }
                        )
                    }
                }

                // Page indicator dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    accounts.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (index == selectedIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == selectedIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                // Quick Action Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            txDialogType = TransactionType.EXPENSE
                            showTxDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.TrendingDown, contentDescription = "Log Expense")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Expense", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            txDialogType = TransactionType.INCOME
                            showTxDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.TrendingUp, contentDescription = "Log Income")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Income", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            txDialogType = TransactionType.TRANSFER
                            showTxDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = "Transfer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transfer", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSubscriptionsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.Autorenew, contentDescription = "Bills")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bills & Subs", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPaymentsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.Payments, contentDescription = "Payments")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Payments Log", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Selected Account Details View
                SelectedAccountDetailsView(
                    account = currentAccount,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }

            FloatingActionButton(
                onClick = onManageCardsClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Manage Accounts")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Accounts", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showTxDialog) {
        QuickLogTransactionDialog(
            type = txDialogType,
            preSelectedAccountId = accounts.getOrNull(selectedIndex)?.id ?: "",
            accounts = accounts,
            viewModel = viewModel,
            onDismiss = { showTxDialog = false }
        )
    }
}

@Composable
fun AccountWidget(
    account: Account,
    isSelected: Boolean,
    viewModel: TrackerViewModel,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CardScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (account.accountType == AccountType.CREDIT_CARD) {
            val activeSpend = viewModel.getSpendInCurrentCycle(account)
            CreditCardDesign(
                bank = account.bank,
                name = account.name,
                cardNumber = account.cardNumber,
                expiryDate = account.expiryDate,
                cvv = account.cvv,
                cardType = account.safeCardType,
                cardTier = account.safeCardTier,
                activeSpend = activeSpend,
                creditLimit = account.creditLimit,
                isSelected = isSelected,
                onClick = onClick
            )
        } else {
            val inLocale = Locale("en", "IN")
            val balanceFormatter = remember { 
                val formatter = NumberFormat.getCurrencyInstance(inLocale)
                formatter.currency = Currency.getInstance("BDT")
                formatter
            }
            val formattedBalance = balanceFormatter.format(account.balance)
            
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(160.dp)
                    .clickable { onClick() }
                    .vaultGlass(borderRadius = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = account.bank,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            val icon = when (account.accountType) {
                                AccountType.BANK_ACCOUNT -> Icons.Outlined.AccountBalance
                                AccountType.MFS -> Icons.Outlined.Smartphone
                                AccountType.CASH -> Icons.Outlined.Payments
                                else -> Icons.Outlined.Wallet
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = account.accountType.name,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = formattedBalance,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedAccountDetailsView(
    account: Account,
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
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

    if (account.accountType == AccountType.CREDIT_CARD) {
        val activeSpend = viewModel.getSpendInCurrentCycle(account)
        val activePayments = viewModel.getPaymentsInCurrentCycle(account)
        val remainingLimit = (account.creditLimit - activeSpend).coerceAtLeast(0.0)
        val unpaidStatementBalance = (activeSpend - activePayments).coerceAtLeast(0.0)

        val cycleRange = viewModel.getBillingCycleRange(account.statementDay)
        val dueDateMillis = viewModel.getDueDateForCycle(account.statementDay, account.dueDay)
        
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val daysRemaining = ((dueDateMillis - today) / (1000 * 60 * 60 * 24)).toInt()

        var showLoungeLogDialog by remember { mutableStateOf(false) }
        var showEmiLogDialog by remember { mutableStateOf(false) }
        var showScanDialog by remember { mutableStateOf(false) }

        var simulatedPaymentFraction by remember { mutableStateOf(1f) }
        val simulatedPaymentAmount = unpaidStatementBalance * simulatedPaymentFraction
        val simulatedUnpaidAmount = (unpaidStatementBalance - simulatedPaymentAmount).coerceAtLeast(0.0)
        val simulatedInterestCharge = simulatedUnpaidAmount * 0.025 // 2.5% finance charge estimate

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showScanDialog = true
            } else {
                Toast.makeText(context, "SMS read permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .vaultGlass(borderRadius = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Due Date Banner
                Surface(
                    color = when {
                        unpaidStatementBalance <= 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        daysRemaining < 0 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        daysRemaining <= 5 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val statusIcon = when {
                            unpaidStatementBalance <= 0 -> Icons.Outlined.CheckCircle
                            daysRemaining < 0 -> Icons.Outlined.Error
                            daysRemaining <= 5 -> Icons.Outlined.Warning
                            else -> Icons.Outlined.Schedule
                        }
                        val statusText = when {
                            unpaidStatementBalance <= 0 -> "Fully Paid for this statement cycle!"
                            daysRemaining < 0 -> "Overdue by ${-daysRemaining} days!"
                            daysRemaining == 0 -> "Payment is due TODAY!"
                            daysRemaining == 1 -> "Due Tomorrow! Pay immediately."
                            else -> "Payment due in $daysRemaining days"
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Status",
                            tint = when {
                                unpaidStatementBalance <= 0 -> MaterialTheme.colorScheme.primary
                                daysRemaining <= 5 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                        Column {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (unpaidStatementBalance > 0) {
                                Text(
                                    text = "Due Date: ${dateFormat.format(Date(dueDateMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Balance Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Limit Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBdt(remainingLimit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current Cycle Spend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBdt(activeSpend), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Payments & Unpaid Outstanding stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Payments Logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBdt(activePayments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Remaining Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatBdt(unpaidStatementBalance), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = if (unpaidStatementBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // SMS Inbox Scanner Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sms,
                                contentDescription = "SMS Scan",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text("SMS Inbox Scanner", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Scan historical texts from ${account.smsSender}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = {
                                val hasReadSms = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.READ_SMS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasReadSms) {
                                    showScanDialog = true
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.READ_SMS)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Scan Inbox", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Interest Payment Simulator
                if (unpaidStatementBalance > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.Calculate, contentDescription = "Calculator", tint = MaterialTheme.colorScheme.primary)
                                Text("Interest Payment Simulator", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Simulate payment fraction to see estimated future finance charges (2.5% monthly estimate on unpaid balances). Dispute claims must be filed within 45 days.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = simulatedPaymentFraction,
                                onValueChange = { simulatedPaymentFraction = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Paying: ${(simulatedPaymentFraction * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatBdt(simulatedPaymentAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Est. Finance Fee", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatBdt(simulatedInterestCharge), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (simulatedInterestCharge > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Bangladesh Bank June 2026 Circular compliance card
                var isGuidelinesExpanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isGuidelinesExpanded = !isGuidelinesExpanded }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Gavel,
                                    contentDescription = "Regulation",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "June 2026 BB Regulations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isGuidelinesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = "Toggle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isGuidelinesExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Text(
                                text = "Bangladesh Bank Credit Card Directive (June 2026):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Unsecured Credit Ceiling: The combined limit for unsecured credit card loans across all banking institutions is capped strictly at BDT 40 Lakh (৳40,00,000) per individual customer.\n" +
                                       "• Unsecured Exposure Parameters: Banking and Non-Banking Financial Institutions (NBFIs) are required to monitor customer debt-to-income ratios before issuing additional lines of credit.\n" +
                                       "• Billing Dispute Terms: Customers have a 45-day window to file dispute claims. No interest, finance fees, or late payment penalties can be assessed on disputed transactions during the investigation period.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Fees & Annual redemption criteria
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Fees & Redemptions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CardMembership,
                                contentDescription = "Fee",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Annual Fee",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (account.annualFee > 0) formatBdt(account.annualFee) else "No Fee",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (account.annualFee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Redeem,
                                    contentDescription = "Waive",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Waive Option",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (account.isFeeRedeemable) "Redeemable" else "Non-Redeemable",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (account.isFeeRedeemable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (account.isFeeRedeemable) {
                            val target = account.feeRedemptionLimit
                            val progressPercent = if (account.feeRedemptionUnit == "Spend") {
                                val activeYearSpend = activeSpend
                                ((activeYearSpend / target) * 100).coerceIn(0.0, 100.0)
                            } else {
                                0.0
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (account.feeRedemptionUnit == "Spend") {
                                            "Spend requirement: Spend ${formatBdt(target)} to waive fee. Current cycle progress: ${progressPercent.toInt()}%."
                                        } else {
                                            "Points requirement: Accumulate ${target.toInt()} points to waive fee."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Bespoke Benefits & Partner Offers Expandable Panel
                CardBenefitsSection(account = account)

                // Add installment/lounge visit triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showLoungeLogDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.FlightTakeoff, contentDescription = "Log Lounge")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Lounge")
                    }

                    OutlinedButton(
                        onClick = { showEmiLogDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.CreditCard, contentDescription = "Add EMI")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add EMI")
                    }
                }
            }
        }

        if (showLoungeLogDialog) {
            var loungeName by remember { mutableStateOf("") }
            var airport by remember { mutableStateOf("") }
            var guestsCount by remember { mutableStateOf("0") }

            AlertDialog(
                onDismissRequest = { showLoungeLogDialog = false },
                title = { Text("Log Lounge Visit", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = loungeName,
                            onValueChange = { loungeName = it },
                            label = { Text("Lounge Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = airport,
                            onValueChange = { airport = it },
                            label = { Text("Airport Code (e.g. DAC)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = guestsCount,
                            onValueChange = { guestsCount = it.filter { char -> char.isDigit() } },
                            label = { Text("Accompanying Guests") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (loungeName.isBlank() || airport.isBlank()) {
                            Toast.makeText(context, "Fill in all fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            val guests = guestsCount.toIntOrNull() ?: 0
                            viewModel.addLoungeVisit(account.id, loungeName.trim(), airport.uppercase().trim(), System.currentTimeMillis(), guests)
                            showLoungeLogDialog = false
                        }
                    }) {
                        Text("Log Visit", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoungeLogDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEmiLogDialog) {
            var merchant by remember { mutableStateOf("") }
            var totalAmount by remember { mutableStateOf("") }
            var durationMonths by remember { mutableStateOf("6") }

            AlertDialog(
                onDismissRequest = { showEmiLogDialog = false },
                title = { Text("Add Installment Plan", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            label = { Text("Merchant/Vendor") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { totalAmount = it },
                            label = { Text("Total Purchase Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = durationMonths,
                            onValueChange = { durationMonths = it.filter { char -> char.isDigit() } },
                            label = { Text("Duration (Months)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val total = totalAmount.toDoubleOrNull() ?: 0.0
                        val duration = durationMonths.toIntOrNull() ?: 0
                        if (merchant.isBlank() || total <= 0.0 || duration <= 0) {
                            Toast.makeText(context, "Invalid input details.", Toast.LENGTH_SHORT).show()
                        } else {
                            val monthly = total / duration
                            viewModel.addEmiPlan(account.id, merchant.trim(), total, monthly, duration, System.currentTimeMillis())
                            showEmiLogDialog = false
                        }
                    }) {
                        Text("Create Plan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmiLogDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showScanDialog) {
            ScanSmsDialog(
                account = account,
                viewModel = viewModel,
                onDismiss = { showScanDialog = false }
            )
        }
    } else {
        // Assets Account details: bank/MFS/cash
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis

        val monthIncome = viewModel.transactions
            .filter { it.sourceAccountId == account.id && it.type == TransactionType.INCOME && it.date >= startOfMonth }
            .sumOf { it.amount * it.exchangeRate }

        val monthExpense = viewModel.transactions
            .filter { it.sourceAccountId == account.id && it.type == TransactionType.EXPENSE && it.date >= startOfMonth }
            .sumOf { it.amount * it.exchangeRate }

        val accountTransactions = viewModel.transactions
            .filter { it.sourceAccountId == account.id || it.destinationAccountId == account.id }
            .sortedByDescending { it.date }
            .take(5)

        var showScanDialog by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showScanDialog = true
            } else {
                Toast.makeText(context, "SMS read permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .vaultGlass(borderRadius = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly flow widget
                Text(
                    text = "This Month's Cash Flow",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBdt(monthIncome), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Outflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBdt(monthExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // SMS Scanning for bKash/Bank
                if (account.smsSender.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sms,
                                    contentDescription = "SMS Scan",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Column {
                                    Text("SMS Inbox Scanner", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Scan alerts from ${account.smsSender}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Button(
                                onClick = {
                                    val hasReadSms = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.READ_SMS
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (hasReadSms) {
                                        showScanDialog = true
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.READ_SMS)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Scan", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Recent Transactions list
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (accountTransactions.isEmpty()) {
                    Text(
                        text = "No recent transactions logged.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        accountTransactions.forEach { tx ->
                            val isSource = tx.sourceAccountId == account.id
                            val isIncome = tx.type == TransactionType.INCOME || (!isSource && tx.type == TransactionType.TRANSFER)
                            val displayAmount = if (isIncome) "+${formatBdt(tx.amount)}" else "-${formatBdt(tx.amount)}"
                            val displayColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            
                            val dateStr = SimpleDateFormat("MMM dd", Locale.US).format(Date(tx.date))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.description.ifEmpty { tx.category },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$dateStr • ${tx.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = displayAmount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = displayColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showScanDialog) {
            ScanSmsDialog(
                account = account,
                viewModel = viewModel,
                onDismiss = { showScanDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSmsDialog(
    account: Account,
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val smsList = remember(account) {
        val allSms = viewModel.scanInboxForAccountSms(context, account)
        val existingDates = viewModel.transactions.filter { it.sourceAccountId == account.id }.map { it.date }.toSet()
        allSms.filter { it.date !in existingDates }
    }

    val selectedSmsList = remember(smsList) {
        smsList.map { true }.toMutableStateList()
    }

    val bdtFormatter = remember { NumberFormat.getNumberInstance(Locale("en", "IN")) }
    fun formatBdt(amount: Double): String {
        return "৳" + bdtFormatter.format(amount)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan SMS Inbox", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Found ${smsList.size} new transaction alerts from sender \"${account.smsSender}\" that have not been logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (smsList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No new transaction alerts found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                            itemsIndexed(smsList) { idx, sms ->
                                val dateStr = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US).format(Date(sms.date))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSmsList[idx] = !selectedSmsList[idx]
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedSmsList[idx],
                                        onCheckedChange = { checked ->
                                            selectedSmsList[idx] = checked
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${sms.merchant}: ${formatBdt(sms.amount)}",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "$dateStr • ${sms.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var importCount = 0
                    smsList.forEachIndexed { idx, sms ->
                        if (selectedSmsList[idx]) {
                            val bodyLower = sms.body.lowercase(Locale.US)
                            val transactionType = if (bodyLower.contains("received") || 
                                                     bodyLower.contains("credited") || 
                                                     bodyLower.contains("deposit") || 
                                                     bodyLower.contains("cash in") ||
                                                     bodyLower.contains("ref:")) {
                                TransactionType.INCOME
                            } else {
                                TransactionType.EXPENSE
                            }

                            viewModel.addTransaction(
                                type = transactionType,
                                sourceAccountId = account.id,
                                destinationAccountId = null,
                                amount = sms.amount,
                                category = sms.category,
                                description = "Auto SMS: ${sms.merchant}",
                                date = sms.date,
                                currency = "BDT",
                                exchangeRate = 1.0
                            )
                            importCount++
                        }
                    }
                    Toast.makeText(context, "Imported $importCount transaction(s).", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                enabled = smsList.isNotEmpty() && selectedSmsList.any { it }
            ) {
                Text("Import Selected", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogTransactionDialog(
    type: TransactionType,
    preSelectedAccountId: String,
    accounts: List<Account>,
    viewModel: TrackerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSourceId by remember { mutableStateOf(preSelectedAccountId) }
    var selectedDestId by remember { mutableStateOf(accounts.firstOrNull { it.id != preSelectedAccountId }?.id ?: "") }
    
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (type == TransactionType.INCOME) "Salary" else "Food & Dining") }

    val categories = if (type == TransactionType.INCOME) {
        listOf("Salary", "Freelance", "Investment", "Gift", "Others")
    } else {
        listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Healthcare", "Education", "Entertainment", "Others")
    }

    var showSourceDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }
    var showCatDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    TransactionType.EXPENSE -> "Log Expense"
                    TransactionType.INCOME -> "Log Income"
                    TransactionType.TRANSFER -> "Transfer Funds"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Source account selection
                val srcAccount = accounts.find { it.id == selectedSourceId } ?: accounts.firstOrNull()
                selectedSourceId = srcAccount?.id ?: ""
                
                Text(
                    text = when (type) {
                        TransactionType.EXPENSE -> "Charged Account"
                        TransactionType.INCOME -> "Destination Account"
                        TransactionType.TRANSFER -> "From Account"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showSourceDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(srcAccount?.let { "${it.bank} - ${it.name}" } ?: "Select Account", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }
                    DropdownMenu(
                        expanded = showSourceDropdown,
                        onDismissRequest = { showSourceDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.bank} - ${account.name}") },
                                onClick = {
                                    selectedSourceId = account.id
                                    showSourceDropdown = false
                                }
                            )
                        }
                    }
                }

                // Destination account selection (only for transfer)
                if (type == TransactionType.TRANSFER) {
                    val destAccount = accounts.find { it.id == selectedDestId } ?: accounts.firstOrNull { it.id != selectedSourceId }
                    selectedDestId = destAccount?.id ?: ""

                    Text(
                        text = "To Account",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showDestDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(destAccount?.let { "${it.bank} - ${it.name}" } ?: "Select Account", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = showDestDropdown,
                            onDismissRequest = { showDestDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            accounts.filter { it.id != selectedSourceId }.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text("${account.bank} - ${account.name}") },
                                    onClick = {
                                        selectedDestId = account.id
                                        showDestDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection (only for expense/income)
                if (type != TransactionType.TRANSFER) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Vendor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    } else if (selectedSourceId.isEmpty()) {
                        Toast.makeText(context, "Please select an account.", Toast.LENGTH_SHORT).show()
                    } else if (type == TransactionType.TRANSFER && selectedDestId.isEmpty()) {
                        Toast.makeText(context, "Please select a destination account.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addTransaction(
                            type = type,
                            sourceAccountId = selectedSourceId,
                            destinationAccountId = if (type == TransactionType.TRANSFER) selectedDestId else null,
                            amount = amount,
                            category = if (type == TransactionType.TRANSFER) "Transfer" else category,
                            description = description.trim(),
                            date = System.currentTimeMillis(),
                            currency = "BDT",
                            exchangeRate = 1.0
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Log Transaction", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun detectPresetCard(bank: String, name: String, tier: String, type: String): String? {
    val b = bank.lowercase()
    val n = name.lowercase()
    val t = tier.lowercase()
    val ty = type.lowercase()
    return when {
        b.contains("brac") && (n.contains("flexi") || (t.contains("platinum") && ty.contains("visa"))) -> "brac_flexi"
        b.contains("brac") && (n.contains("diners") || t.contains("emerald") || ty.contains("diners")) -> "brac_diners"
        (b.contains("eastern") || b.contains("ebl")) && (n.contains("sharetrip") || (t.contains("titanium") && ty.contains("mastercard"))) -> "ebl_sharetrip"
        (b.contains("eastern") || b.contains("ebl")) && (n.contains("stellar") || t.contains("stellar") || (t.contains("platinum") && ty.contains("visa"))) -> "ebl_stellar"
        else -> null
    }
}

@Composable
fun CardBenefitsSection(account: Account) {
    val presetKey = detectPresetCard(account.bank, account.name, account.safeCardTier, account.safeCardType) ?: return
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CardMembership,
                        contentDescription = "Benefits",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Bespoke Benefits & Partner Offers",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Toggle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                
                when (presetKey) {
                    "brac_flexi" -> {
                        BenefitItem(
                            icon = Icons.Outlined.FlightTakeoff,
                            title = "Airport Lounge Privileges",
                            description = "• Unlimited complimentary access to Balaka Executive Lounge at Hazrat Shahjalal International Airport, Dhaka (for cardholder).\n• LoungeKey international access (chargeable at USD 29 per person per visit)."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Redeem,
                            title = "Reward System",
                            description = "• 1 reward point per BDT 80 spent.\n• 2x Reward Points on all local & international POS and e-commerce transactions on Fridays."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Restaurant,
                            title = "Dining & Hotels",
                            description = "• Buy-1-Get-1 (BOGO) buffet offers at Six Seasons, Amari Dhaka, and other partner hotels.\n• 25% off dining at Six Seasons Hotel, Dera Resort BOGO, and 10% off at Gourmet & Grace."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Travel & Welcome Offers",
                            description = "• Welcome gift: BDT 2,000 ShareTrip voucher.\n• Convert retail transactions of BDT 15,000+ to 6 months 0% EMI via Super P@yflex."
                        )
                    }
                    "brac_diners" -> {
                        BenefitItem(
                            icon = Icons.Outlined.FlightTakeoff,
                            title = "Airport Lounge Privileges",
                            description = "• Unlimited complimentary access to Balaka Executive Lounge, Dhaka for cardholder + 1 guest.\n• 3 complimentary visits per year to over 1,500+ Diners Club international lounges globally (USD 29/visit thereafter)."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Redeem,
                            title = "Reward System & Cashback",
                            description = "• 2 reward points per BDT 80 spent.\n• 2x reward points on travel (ShareTrip, GoZayaan, Bdtickets, Shohoz) and international e-commerce on Thursdays.\n• 5% cashback (up to BDT 300 monthly) on Cooper's, Gloria Jean's, and Herfy."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Restaurant,
                            title = "Dining & Hotels",
                            description = "• Premium BOGO, Buy-1-Get-2 (BOG2), and Buy-1-Get-3 (BOG3) buffet offers at Renaissance, Amari, Le Meridien, etc.\n• Cashback and discounts at Chef’s Table, foodpanda, and foodi."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Travel & Welcome Offers",
                            description = "• Welcome vouchers: Artisan, GoZayaan, and Square Hospitals.\n• Smart P@yflex: 12-month EMI at 7% interest for BDT 15,000+ purchases; 0% EMI at 1,400+ partner merchants."
                        )
                    }
                    "ebl_sharetrip" -> {
                        BenefitItem(
                            icon = Icons.Outlined.FlightTakeoff,
                            title = "Airport Lounge Privileges",
                            description = "• Unlimited complimentary access to EBL SKYLOUNGE at Dhaka and Chattogram (Domestic terminal).\n• LoungeKey access to 1,100+ global lounges."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Redeem,
                            title = "Travel Coins & Points",
                            description = "• 2x TripCoins on ShareTrip transactions.\n• 2x SkyCoins on cross-border transactions."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Restaurant,
                            title = "Dining Perks",
                            description = "• Mastercard campaign BOGO dining deals (e.g. during Ramadan & festive campaigns).\n• EBL Advantage discounts (up to 20% off) at participating local restaurants."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Travel Discounts & Vouchers",
                            description = "• 15% discount on base fares of Biman Bangladesh Airlines, US-Bangla, NOVOAIR, and Air Astra booked via ShareTrip.\n• BDT 5,000 worth of holiday vouchers (Maldives, Singapore, Europe, etc.).\n• Zero annual renewal fee with 24 transactions in a year."
                        )
                    }
                    "ebl_stellar" -> {
                        BenefitItem(
                            icon = Icons.Outlined.FlightTakeoff,
                            title = "Airport Lounge Privileges",
                            description = "• 6 complimentary visits per year to EBL SKYLOUNGE (Dhaka & Chattogram) for cardholder + 2 children under 12.\n• Priority Pass international access to 1,300+ global lounges."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Redeem,
                            title = "Rewards Program",
                            description = "• 1.5 points per BDT 50 spend (redeemable for Visa SkyCoins)."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.Restaurant,
                            title = "Dining & Hotels",
                            description = "• Visa Platinum dining privileges and up to 20% off at premium restaurants via EBL Discount Partners."
                        )
                        BenefitItem(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Travel & VIP Services",
                            description = "• Meet & Greet Service: Personal fast-track assistance at Hazrat Shahjalal International Airport (Dhaka) check-in & arrival.\n• ShareTrip offers: 15% off international flight base fares, extra 6% off (up to BDT 5,000), and 10% off hotels.\n• BDT 1,500 lifestyle vouchers on card issuance.\n• Free travel insurance & baggage protection."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
