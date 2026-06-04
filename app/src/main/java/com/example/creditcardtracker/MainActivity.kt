package com.example.creditcardtracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.creditcardtracker.security.BiometricHelper
import com.example.creditcardtracker.theme.CreditCardTrackerTheme
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.animatedVaultGradient
import com.example.creditcardtracker.ui.*

enum class AppTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Expenses("Expenses", Icons.Default.List),
    Overview("Overview", Icons.Default.Home),
    Payments("Payments", Icons.Default.Payments)
}

enum class OverlayScreen {
    None,
    ManageCards,
    Settings
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CreditCardTrackerTheme {
                val viewModel: TrackerViewModel = viewModel()
                var currentTab by remember { mutableStateOf(AppTab.Overview) }
                var overlayScreen by remember { mutableStateOf(OverlayScreen.None) }
                
                // Security / Unlock states
                val biometricEnabled by viewModel.isBiometricEnabled
                var isUnlocked by remember { mutableStateOf(!biometricEnabled) }

                // Automatically trigger biometric unlock on startup if enabled
                LaunchedEffect(biometricEnabled) {
                    if (biometricEnabled && !isUnlocked) {
                        triggerUnlock(
                            onSuccess = { isUnlocked = true },
                            onFailure = { msg ->
                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isUnlocked) {
                        // Vault Lock Screen Overlay
                        LockScreen(
                            onUnlockClick = {
                                triggerUnlock(
                                    onSuccess = { isUnlocked = true },
                                    onFailure = { msg ->
                                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    } else {
                        // Main Application View
                        Box(modifier = Modifier.fillMaxSize()) {
                            Scaffold(
                                topBar = {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Text(
                                                text = when (currentTab) {
                                                    AppTab.Expenses -> "Expenses"
                                                    AppTab.Overview -> "Secure Vault"
                                                    AppTab.Payments -> "Payments"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        },
                                        actions = {
                                            IconButton(onClick = { overlayScreen = OverlayScreen.Settings }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Settings,
                                                    contentDescription = "Settings"
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = Color.Transparent,
                                            titleContentColor = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                },
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp
                                    ) {
                                        AppTab.entries.forEach { tab ->
                                            val isSelected = currentTab == tab
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = { currentTab = tab },
                                                label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
                                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(animatedVaultGradient())
                                        .padding(innerPadding)
                                ) {
                                    AnimatedContent(
                                        targetState = currentTab,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(250)) + slideInVertically(animationSpec = tween(250), initialOffsetY = { it / 10 }) togetherWith 
                                            fadeOut(animationSpec = tween(120))
                                        },
                                        label = "TabTransition"
                                    ) { targetTab ->
                                        when (targetTab) {
                                            AppTab.Expenses -> ExpensesScreen(viewModel = viewModel)
                                            AppTab.Overview -> OverviewScreen(
                                                viewModel = viewModel,
                                                onManageCardsClick = { overlayScreen = OverlayScreen.ManageCards }
                                            )
                                            AppTab.Payments -> PaymentsScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }

                            // Sliding overlay screens
                            AnimatedVisibility(
                                visible = overlayScreen != OverlayScreen.None,
                                enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
                                exit = slideOutVertically(animationSpec = tween(250), targetOffsetY = { it })
                            ) {
                                when (overlayScreen) {
                                    OverlayScreen.ManageCards -> {
                                        ManageCardsScreen(
                                            viewModel = viewModel,
                                            onBackClick = { overlayScreen = OverlayScreen.None }
                                        )
                                    }
                                    OverlayScreen.Settings -> {
                                        SettingsScreen(
                                            viewModel = viewModel,
                                            onBackClick = { overlayScreen = OverlayScreen.None }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerUnlock(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (BiometricHelper.isBiometricsAvailable(this)) {
            BiometricHelper.showBiometricPrompt(
                activity = this,
                title = "Vault Secure Access",
                subtitle = "Authenticate to decrypt local records",
                onSuccess = onSuccess,
                onError = onFailure
            )
        } else {
            // Backup fallback (always unlock if biometric hardware is not setup / configured on system)
            onSuccess()
        }
    }
}

@Composable
fun LockScreen(
    onUnlockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0F1D), Color(0xFF1B263B)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked",
                tint = VaultUiTokens.VaultEmerald,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vault Locked",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Biometric credentials are required to authorize decryption of transaction records.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            // Large fingerprint click target
            Button(
                onClick = onUnlockClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.size(90.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Scan fingerprint",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to Authenticate",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
