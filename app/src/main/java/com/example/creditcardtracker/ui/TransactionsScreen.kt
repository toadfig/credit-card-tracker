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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val transactions = viewModel.transactions
    val accounts = viewModel.accounts
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogType by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedAccountFilterId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedAccountFilterId, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchesQuery = tx.description.contains(searchQuery, ignoreCase = true) || 
                               tx.category.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesAccount = selectedAccountFilterId == null || 
                                 tx.sourceAccountId == selectedAccountFilterId || 
                                 tx.destinationAccountId == selectedAccountFilterId
            val matchesCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
            matchesQuery && matchesType && matchesAccount && matchesCategory
        }.sortedByDescending { it.date }
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
                text = "Transactions Ledger",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search description / vendor...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
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

            // Filter status indicators
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
                            label = { Text(cat) },
                            trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Remove") }
                        )
                    }
                    selectedAccountFilterId?.let { accId ->
                        val accName = accounts.find { it.id == accId }?.name ?: "Account"
                        InputChip(
                            selected = true,
                            onClick = { selectedAccountFilterId = null },
                            label = { Text(accName) },
                            trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Remove") }
                        )
                    }
                }
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
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

        // Floating Action Button to add transaction
        FloatingActionButton(
            onClick = {
                if (accounts.isEmpty()) {
                    Toast.makeText(context, "Please configure an account first.", Toast.LENGTH_SHORT).show()
                } else {
                    addDialogType = TransactionType.EXPENSE
                    showAddDialog = true
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            type = addDialogType,
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, srcId, destId, amt, cat, desc, date ->
                viewModel.addTransaction(
                    type = type,
                    sourceAccountId = srcId,
                    destinationAccountId = destId,
                    amount = amt,
                    category = cat,
                    description = desc,
                    date = date
                )
                showAddDialog = false
            }
        )
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
