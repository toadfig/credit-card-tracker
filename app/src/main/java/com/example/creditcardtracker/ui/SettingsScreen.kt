package com.example.creditcardtracker.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.creditcardtracker.theme.vaultGlass
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBiometricEnabled by viewModel.isBiometricEnabled

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    var alertText by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                // Biometrics Section
                item {
                    Text(
                        text = "Security & Privacy",
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Fingerprint, contentDescription = "Biometrics")
                                Column {
                                    Text("Biometric Authentication", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Use fingerprint or face recognition to unlock.",
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
                    }
                }

                // Backup Section
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Backup Info
                            Text(
                                text = "Encrypt and export or import data. Backups are encrypted with a password and saved to your device's external storage folder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Export Button
                            Button(
                                onClick = { showExportDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Save, contentDescription = "Export")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Encrypted Backup")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Import Button
                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Restore, contentDescription = "Import")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Encrypted Backup")
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
                    description = "Enter the password that was used to encrypt the backup file. The backup file must be named 'cctracker_backup.enc' and located in the app's files directory.",
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

            // Alert Dialog for results
            if (showAlert) {
                AlertDialog(
                    onDismissRequest = { showAlert = false },
                    title = { Text("Backup Status", fontWeight = FontWeight.SemiBold) },
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
