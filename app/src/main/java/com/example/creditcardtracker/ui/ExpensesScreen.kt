package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.vaultGlass
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val transactions = viewModel.transactions
    val accounts = viewModel.accounts
    
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogType by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedAccountFilterId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showAnalytics by remember { mutableStateOf(true) }

    val inLocale = Locale("en", "IN")
    val bdtFormatter = remember { 
        val formatter = NumberFormat.getCurrencyInstance(inLocale)
        formatter.currency = Currency.getInstance("BDT")
        formatter
    }
    fun formatBdt(amount: Double): String {
        return bdtFormatter.format(amount)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions Ledger",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (transactions.isNotEmpty()) {
                    TextButton(onClick = { showAnalytics = !showAnalytics }) {
                        Text(
                            text = if (showAnalytics) "Hide Charts" else "Show Charts",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search description / vendor...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Filtering Row (Expense / Income / Transfer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                    label = { Text("Expenses") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.INCOME,
                    onClick = { selectedTypeFilter = TransactionType.INCOME },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.TRANSFER,
                    onClick = { selectedTypeFilter = TransactionType.TRANSFER },
                    label = { Text("Transfers") }
                )
            }

            // Filter status indicator
            if (selectedCategoryFilter != null || selectedAccountFilterId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    selectedCategoryFilter?.let { cat ->
                        InputChip(
                            selected = true,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("Category: $cat") },
                            trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    selectedAccountFilterId?.let { accId ->
                        val acc = accounts.find { it.id == accId }
                        InputChip(
                            selected = true,
                            onClick = { selectedAccountFilterId = null },
                            label = { Text("Account: ${acc?.name ?: "Unknown"}") },
                            trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Spending Analytics Card
            val expensesOnly = remember(transactions) {
                transactions.filter { it.type == TransactionType.EXPENSE }
            }
            if (showAnalytics && expensesOnly.isNotEmpty() && (selectedTypeFilter == null || selectedTypeFilter == TransactionType.EXPENSE)) {
                SpendingAnalyticsCard(
                    expenses = expensesOnly,
                    selectedCategory = selectedCategoryFilter,
                    onCategoryClick = { selectedCategoryFilter = it }
                )
            }

            // Ledger List
            val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedAccountFilterId, selectedCategoryFilter) {
                var list = transactions.toList()
                if (searchQuery.isNotBlank()) {
                    list = list.filter { it.description.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
                }
                if (selectedTypeFilter != null) {
                    list = list.filter { it.type == selectedTypeFilter }
                }
                if (selectedCategoryFilter != null) {
                    list = list.filter { it.category == selectedCategoryFilter }
                }
                if (selectedAccountFilterId != null) {
                    list = list.filter { it.sourceAccountId == selectedAccountFilterId || it.destinationAccountId == selectedAccountFilterId }
                }
                list.sortedByDescending { it.date }
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching records found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = filteredTransactions, key = { it.id }) { tx ->
                        val srcAcc = accounts.find { it.id == tx.sourceAccountId }
                        val destAcc = accounts.find { it.id == tx.destinationAccountId }
                        TransactionItem(
                            transaction = tx,
                            sourceAccount = srcAcc,
                            destinationAccount = destAcc,
                            onDelete = { viewModel.deleteTransaction(tx.id) },
                            onAccountClick = { selectedAccountFilterId = it }
                        )
                    }
                }
            }
        }

        if (accounts.isNotEmpty()) {
            FloatingActionButton(
                onClick = { 
                    addDialogType = selectedTypeFilter ?: TransactionType.EXPENSE
                    showAddDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }

        if (showAddDialog) {
            AddTransactionDialog(
                type = addDialogType,
                accounts = accounts,
                onDismiss = { showAddDialog = false },
                onConfirm = { type, sourceId, destId, amount, category, desc, date ->
                    viewModel.addTransaction(type, sourceId, destId, amount, category, desc, date)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun SpendingAnalyticsCard(
    expenses: List<Transaction>,
    selectedCategory: String?,
    onCategoryClick: (String?) -> Unit
) {
    val categories = listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Others")
    val categoryColors = listOf(
        Color(0xFFEF476F),
        Color(0xFF118AB2),
        Color(0xFF06D6A0),
        Color(0xFFFFD166),
        Color(0xFF072AC8),
        Color(0xFF708090)
    )

    // Calculate sum per category
    val spendByCategory = categories.map { category ->
        expenses.filter { it.category == category }.sumOf { it.amount * it.exchangeRate }
    }
    val totalSpend = spendByCategory.sum()

    val inLocale = Locale("en", "IN")
    val bdtFormatter = remember { 
        val formatter = NumberFormat.getCurrencyInstance(inLocale)
        formatter.currency = Currency.getInstance("BDT")
        formatter
    }
    fun formatBdt(amount: Double): String {
        return bdtFormatter.format(amount)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vaultGlass(borderRadius = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Monthly Spending Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (totalSpend > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Pie Chart
                    Canvas(
                        modifier = Modifier
                            .size(110.dp)
                            .pointerInput(expenses, totalSpend) {
                                detectTapGestures { offset ->
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val dx = offset.x - cx
                                    val dy = offset.y - cy
                                    val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                                    val outerRadius = size.width / 2f
                                    val innerRadius = outerRadius * 0.56f

                                    if (distance in innerRadius..outerRadius) {
                                        val angleDeg = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                        val clockwiseAngle = (angleDeg + 90.0 + 360.0) % 360.0

                                        var currentStart = 0.0
                                        spendByCategory.forEachIndexed { index, amount ->
                                            val sweep = (amount / totalSpend) * 360.0
                                            if (sweep > 0.0) {
                                                val currentEnd = currentStart + sweep
                                                if (clockwiseAngle >= currentStart && clockwiseAngle < currentEnd) {
                                                    val clickedCat = categories[index]
                                                    onCategoryClick(if (selectedCategory == clickedCat) null else clickedCat)
                                                }
                                                currentStart = currentEnd
                                            }
                                        }
                                    } else {
                                        onCategoryClick(null)
                                    }
                                }
                            }
                    ) {
                        var startAngle = -90f
                        spendByCategory.forEachIndexed { idx, amount ->
                            val sweepAngle = (amount / totalSpend).toFloat() * 360f
                            if (sweepAngle > 0f) {
                                val isSelected = categories[idx] == selectedCategory
                                val color = categoryColors[idx]
                                drawArc(
                                    color = if (selectedCategory == null || isSelected) color else color.copy(alpha = 0.3f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = size
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // Draw hollow center for donut look
                        drawCircle(
                            color = Color(0xFF151922),
                            radius = size.width * 0.28f,
                            center = center
                        )
                    }

                    // Legends Sidebar
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEachIndexed { idx, category ->
                            val amount = spendByCategory[idx]
                            val percent = if (totalSpend > 0) ((amount / totalSpend) * 100).toInt() else 0
                            if (amount > 0) {
                                val isSelected = category == selectedCategory
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { onCategoryClick(if (isSelected) null else category) }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(categoryColors[idx])
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$category ($percent%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No data to display.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    sourceAccount: Account?,
    destinationAccount: Account?,
    onDelete: () -> Unit,
    onAccountClick: (String) -> Unit
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

    val isExpense = transaction.type == TransactionType.EXPENSE
    val isIncome = transaction.type == TransactionType.INCOME
    
    val displayAmount = when (transaction.type) {
        TransactionType.INCOME -> "+${formatBdt(transaction.amount)}"
        TransactionType.EXPENSE -> "-${formatBdt(transaction.amount)}"
        TransactionType.TRANSFER -> formatBdt(transaction.amount)
    }

    val displayColor = when (transaction.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
    }

    val icon = when (transaction.category) {
        "Food & Dining" -> Icons.Outlined.Restaurant
        "Groceries" -> Icons.Outlined.LocalGroceryStore
        "Transportation" -> Icons.Outlined.DirectionsCar
        "Shopping" -> Icons.Outlined.ShoppingBag
        "Utilities" -> Icons.Outlined.FlashOn
        "Healthcare" -> Icons.Outlined.MedicalServices
        "Education" -> Icons.Outlined.School
        "Salary" -> Icons.Outlined.WorkOutline
        "Freelance" -> Icons.Outlined.Devices
        "Transfer" -> Icons.Outlined.SwapHoriz
        else -> Icons.AutoMirrored.Outlined.ReceiptLong
    }

    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(transaction.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = displayColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(44.dp),
                    contentColor = displayColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = transaction.category)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description.ifEmpty { transaction.category },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Routing tag
                    val routingText = when (transaction.type) {
                        TransactionType.TRANSFER -> "${sourceAccount?.name ?: "Unknown"} ➔ ${destinationAccount?.name ?: "Unknown"}"
                        else -> sourceAccount?.name ?: "Unknown"
                    }
                    Text(
                        text = routingText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { sourceAccount?.let { onAccountClick(it.id) } }
                            .padding(top = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = displayColor
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, String, String?, Double, String, String, Long) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(type) }
    var selectedSourceId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedDestId by remember { mutableStateOf(accounts.firstOrNull { it.id != selectedSourceId }?.id ?: "") }
    
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (selectedType == TransactionType.INCOME) "Salary" else "Food & Dining") }

    val categories = if (selectedType == TransactionType.INCOME) {
        listOf("Salary", "Freelance", "Investment", "Gift", "Others")
    } else {
        listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Healthcare", "Education", "Entertainment", "Others")
    }

    var showSourceDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }
    var showCatDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER).forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { 
                                selectedType = t 
                                category = if (t == TransactionType.INCOME) "Salary" else "Food & Dining"
                            },
                            label = { Text(t.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val srcAccount = accounts.find { it.id == selectedSourceId } ?: accounts.firstOrNull()
                selectedSourceId = srcAccount?.id ?: ""

                Text(
                    text = when (selectedType) {
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

                if (selectedType == TransactionType.TRANSFER) {
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

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedType != TransactionType.TRANSFER) {
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
                    } else if (selectedType == TransactionType.TRANSFER && selectedDestId.isEmpty()) {
                        Toast.makeText(context, "Please select a destination account.", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(
                            selectedType,
                            selectedSourceId,
                            if (selectedType == TransactionType.TRANSFER) selectedDestId else null,
                            amount,
                            if (selectedType == TransactionType.TRANSFER) "Transfer" else category,
                            description.trim(),
                            System.currentTimeMillis()
                        )
                    }
                }
            ) {
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
