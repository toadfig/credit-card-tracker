package com.example.creditcardtracker.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.Subscription
import com.example.creditcardtracker.theme.vaultGlass
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cards = viewModel.cards
    val subscriptions = viewModel.subscriptions

    var isBiometricEnabled by viewModel.isBiometricEnabled
    var isDynamicColorEnabled by viewModel.isDynamicColorEnabled

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddSubDialog by remember { mutableStateOf(false) }
    
    var alertText by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.SemiBold) },
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
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Security & Theme Section
                item {
                    Text(
                        text = "Security & Visuals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .vaultGlass(borderRadius = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Biometric row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Outlined.Fingerprint, contentDescription = "Biometrics")
                                    Column {
                                        Text("Biometric Authentication", fontWeight = FontWeight.Medium)
                                        Text(
                                            "Unlock with fingerprint or face.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isBiometricEnabled,
                                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                            // Dynamic Color row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Outlined.Palette, contentDescription = "Dynamic Color")
                                    Column {
                                        Text("Dynamic Color Theme", fontWeight = FontWeight.Medium)
                                        Text(
                                            "Match app colors with your system wallpaper.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isDynamicColorEnabled,
                                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                                )
                            }
                        }
                    }
                }

                // Subscriptions Manager Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subscriptions Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (cards.isNotEmpty()) {
                            TextButton(onClick = { showAddSubDialog = true }) {
                                Text("Add", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (subscriptions.isEmpty()) {
                    item {
                        Text(
                            text = "No recurring subscriptions configured yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(subscriptions) { sub ->
                        val linkedCard = cards.find { it.id == sub.cardId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${linkedCard?.bank?.uppercase() ?: "Unknown Card"} • Day ${sub.billingDay} of Month",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = currencyFormat.format(sub.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(onClick = { viewModel.deleteSubscription(sub.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Subscription",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Data Management Section
                item {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .vaultGlass(borderRadius = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Secure, offline database operations. Exports are fully encrypted with a passphrase.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { showExportDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Save, contentDescription = "Export")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Encrypted Backup")
                            }

                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Restore, contentDescription = "Import")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Encrypted Backup")
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                            // CSV Export button
                            OutlinedButton(
                                onClick = {
                                    val csvPath = viewModel.exportToCsv(context)
                                    if (csvPath != null) {
                                        alertText = "CSV spreadsheet exported successfully to Downloads folder:\n\n$csvPath"
                                    } else {
                                        alertText = "Failed to export CSV file. Verify storage permissions."
                                    }
                                    showAlert = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.TableChart, contentDescription = "Export CSV")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export CSV Transactions")
                            }
                        }
                    }
                }
            }

            // Export Dialog
            if (showExportDialog) {
                PasswordPromptDialog(
                    title = "Export Backup",
                    description = "Choose a strong password to encrypt the backup file. You will need this password to restore the data.",
                    onDismiss = { showExportDialog = false },
                    onConfirm = { password ->
                        showExportDialog = false
                        val backupFile = File(context.getExternalFilesDir(null), "cctracker_backup.enc")
                        try {
                            val fos = FileOutputStream(backupFile)
                            val success = viewModel.exportData(fos, password.toCharArray())
                            if (success) {
                                alertText = "Backup file successfully exported to:\n\n${backupFile.absolutePath}"
                            } else {
                                alertText = "Failed to write backup file. Try again."
                            }
                        } catch (e: Exception) {
                            alertText = "Error exporting backup: ${e.message}"
                        }
                        showAlert = true
                    }
                )
            }

            // Import Dialog
            if (showImportDialog) {
                PasswordPromptDialog(
                    title = "Import Backup",
                    description = "Enter the password that was used to encrypt the backup. The backup file must be named 'cctracker_backup.enc' and located in the app's external files directory.",
                    onDismiss = { showImportDialog = false },
                    onConfirm = { password ->
                        showImportDialog = false
                        val backupFile = File(context.getExternalFilesDir(null), "cctracker_backup.enc")
                        if (!backupFile.exists()) {
                            alertText = "Backup file not found!\n\nPlease place your backup file named 'cctracker_backup.enc' in this folder first:\n\n${context.getExternalFilesDir(null)?.absolutePath}"
                            showAlert = true
                            return@PasswordPromptDialog
                        }
                        try {
                            val fis = FileInputStream(backupFile)
                            val success = viewModel.importData(fis, password.toCharArray())
                            alertText = if (success) {
                                "Database successfully imported and restored!"
                            } else {
                                "Failed to decrypt backup. Please check your password."
                            }
                        } catch (e: Exception) {
                            alertText = "Error restoring database: ${e.message}"
                        }
                        showAlert = true
                    }
                )
            }

            // Add Subscription Dialog
            if (showAddSubDialog) {
                var name by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var billingDay by remember { mutableStateOf("") }
                var selectedCategory by remember { mutableStateOf("Utilities") }
                var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
                var errorText by remember { mutableStateOf("") }

                var cardDropdownExpanded by remember { mutableStateOf(false) }
                var categoryDropdownExpanded by remember { mutableStateOf(false) }

                val categories = listOf("Utilities", "Food & Dining", "Groceries", "Shopping", "Transportation", "Others")

                AlertDialog(
                    onDismissRequest = { showAddSubDialog = false },
                    title = { Text("Log Recurring Subscription", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            if (errorText.isNotEmpty()) {
                                Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            ExposedDropdownMenuBox(
                                expanded = cardDropdownExpanded,
                                onExpandedChange = { cardDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedCard?.let { "${it.bank.uppercase()} (${it.name})" } ?: "Select Card",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Charge Card") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = cardDropdownExpanded,
                                    onDismissRequest = { cardDropdownExpanded = false }
                                ) {
                                    cards.forEach { card ->
                                        DropdownMenuItem(
                                            text = { Text("${card.bank.uppercase()} - ${card.name}") },
                                            onClick = {
                                                selectedCard = card
                                                cardDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Subscription Name (e.g. Netflix)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = { Text("Monthly Cost ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
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
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
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
                            val cost = amount.toDoubleOrNull() ?: 0.0
                            val day = billingDay.toIntOrNull() ?: 0
                            val activeCard = selectedCard
                            
                            if (activeCard == null) {
                                errorText = "Please select a card."
                            } else if (name.isBlank()) {
                                errorText = "Please specify a subscription name."
                            } else if (cost <= 0.0) {
                                errorText = "Monthly cost must be greater than 0."
                            } else if (day !in 1..31) {
                                errorText = "Billing day must be between 1 and 31."
                            } else {
                                viewModel.addSubscription(activeCard.id, name.trim(), cost, day, selectedCategory)
                                showAddSubDialog = false
                            }
                        }) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddSubDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Alert Dialog for results
            if (showAlert) {
                AlertDialog(
                    onDismissRequest = { showAlert = false },
                    title = { Text("Operation Status", fontWeight = FontWeight.SemiBold) },
                    text = { Text(alertText) },
                    confirmButton = {
                        TextButton(onClick = { showAlert = false }) {
                            Text("OK", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PasswordPromptDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (title == "Export Backup") {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (password.isBlank()) {
                    errorText = "Password cannot be empty."
                } else if (title == "Export Backup" && password != confirmPassword) {
                    errorText = "Passwords do not match."
                } else {
                    onConfirm(password)
                }
            }) {
                Text("Proceed", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
