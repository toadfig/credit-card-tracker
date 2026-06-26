package com.example.creditcardtracker.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.BdtText
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
                        shape = RoundedCornerShape(50)
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
                            style = MaterialTheme.typography.displayMedium,
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
                    // Bills & Subs Bento Card
                    val activeBillsCount = viewModel.subscriptions.size
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clickable { onSubscriptionsClick() }
                            .vaultGlass(borderRadius = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Bills & Subs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Outlined.Autorenew, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text("$activeBillsCount Active", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tap to view logs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Payments Log Bento Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clickable { onPaymentsClick() }
                            .vaultGlass(borderRadius = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Outlined.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text("Log History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tap to view", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
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

        val dueDateMillis = viewModel.getDueDateForCycle(account.statementDay, account.dueDay)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val daysRemaining = ((dueDateMillis - today) / (1000 * 60 * 60 * 24)).toInt()

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
            }
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
    }
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
    var categoryText by remember { mutableStateOf("Food") }
    var descriptionText by remember { mutableStateOf("") }

    val categories = listOf("Food", "Shopping", "Utilities", "Salary", "Entertainment", "Bills", "Rent", "Other")

    var expandedSource by remember { mutableStateOf(false) }
    var expandedDest by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log ${type.name.lowercase().replaceFirstChar { it.titlecase() }}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Source Account
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = accounts.firstOrNull { it.id == selectedSourceId }?.let { "${it.bank} - ${it.name}" } ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (type == TransactionType.TRANSFER) "From Account" else "Account") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedSource = true }) {
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedSource,
                        onDismissRequest = { expandedSource = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.bank} - ${acc.name} (${acc.accountType.name.replace("_", " ")})") },
                                onClick = {
                                    selectedSourceId = acc.id
                                    expandedSource = false
                                }
                            )
                        }
                    }
                }

                // Destination Account (for transfers)
                if (type == TransactionType.TRANSFER) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = accounts.firstOrNull { it.id == selectedDestId }?.let { "${it.bank} - ${it.name}" } ?: "Select Destination",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Account") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedDest = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedDest,
                            onDismissRequest = { expandedDest = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            accounts.filter { it.id != selectedSourceId }.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.bank} - ${acc.name}") },
                                    onClick = {
                                        selectedDestId = acc.id
                                        expandedDest = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedCategory = true }) {
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoryText = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0 || selectedSourceId.isEmpty()) {
                        Toast.makeText(context, "Please enter valid amount.", Toast.LENGTH_SHORT).show()
                    } else if (type == TransactionType.TRANSFER && selectedSourceId == selectedDestId) {
                        Toast.makeText(context, "Source and destination cannot be same.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addTransaction(
                            type = type,
                            sourceAccountId = selectedSourceId,
                            destinationAccountId = if (type == TransactionType.TRANSFER) selectedDestId else null,
                            amount = amount,
                            category = categoryText,
                            description = descriptionText.trim(),
                            date = System.currentTimeMillis()
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
