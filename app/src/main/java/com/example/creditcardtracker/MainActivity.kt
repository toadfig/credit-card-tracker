package com.example.creditcardtracker

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.creditcardtracker.security.BiometricHelper
import com.example.creditcardtracker.theme.CreditCardTrackerTheme
import com.example.creditcardtracker.theme.VaultUiTokens
import com.example.creditcardtracker.theme.animatedVaultGradient
import com.example.creditcardtracker.ui.*

enum class AppTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Overview("Home", Icons.Default.Home),
    Expenses("Ledger", Icons.AutoMirrored.Filled.List),
    Budgets("Budgets", Icons.Default.PieChart),
    Portfolio("Portfolio", Icons.Outlined.Analytics),
    Tax("Tax", Icons.Outlined.CalendarToday)
}

enum class OverlayScreen {
    None,
    ManageCards,
    Settings,
    Subscriptions,
    Payments,
    CreditCommand
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : FragmentActivity() {
    private lateinit var viewModel: TrackerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots / recordings for security
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[TrackerViewModel::class.java]

        setContent {
            val dynamicColorEnabled by viewModel.isDynamicColorEnabled

            CreditCardTrackerTheme(dynamicColor = dynamicColorEnabled) {
                var currentTab by remember { mutableStateOf(AppTab.Overview) }
                var overlayScreen by remember { mutableStateOf(OverlayScreen.None) }
                
                // Security / Unlock states
                val biometricEnabled by viewModel.isBiometricEnabled
                val isUnlocked by viewModel.isUnlocked

                // Automatically trigger biometric unlock on startup if enabled
                LaunchedEffect(biometricEnabled, isUnlocked) {
                    if (biometricEnabled && !isUnlocked) {
                        triggerUnlock(
                            onSuccess = { viewModel.isUnlocked.value = true },
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
                                    onSuccess = { viewModel.isUnlocked.value = true },
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
                                                    AppTab.Overview -> "Money Manager"
                                                    AppTab.Expenses -> "Transactions Ledger"
                                                    AppTab.Budgets -> "Budgets & Savings"
                                                    AppTab.Portfolio -> "Investment Portfolio"
                                                    AppTab.Tax -> "Tax Planning"
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
                                        colors = TopAppBarDefaults.topAppBarColors(
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
                                                alwaysShowLabel = true,
                                                label = {
                                                    Text(
                                                        text = tab.label,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
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
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(innerPadding)
                                ) {
                                    AnimatedContent(
                                        targetState = currentTab,
                                        transitionSpec = {
                                            fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) + 
                                            slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow), initialOffsetY = { it / 10 }) togetherWith 
                                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                                        },
                                        label = "TabTransition"
                                    ) { targetTab ->
                                        when (targetTab) {
                                            AppTab.Overview -> OverviewScreen(
                                                viewModel = viewModel,
                                                onManageCardsClick = { overlayScreen = OverlayScreen.ManageCards },
                                                onSubscriptionsClick = { overlayScreen = OverlayScreen.Subscriptions },
                                                onPaymentsClick = { overlayScreen = OverlayScreen.Payments },
                                                onCreditCommandClick = { overlayScreen = OverlayScreen.CreditCommand }
                                            )
                                            AppTab.Expenses -> ExpensesScreen(viewModel = viewModel)
                                            AppTab.Budgets -> BudgetsScreen(viewModel = viewModel)
                                            AppTab.Portfolio -> InvestmentsScreen(viewModel = viewModel)
                                            AppTab.Tax -> TaxScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }

                            // Sliding overlay screens
                            AnimatedVisibility(
                                visible = overlayScreen != OverlayScreen.None,
                                enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), initialOffsetY = { it }),
                                exit = slideOutVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium), targetOffsetY = { it })
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
                                    OverlayScreen.Subscriptions -> {
                                        Scaffold(
                                            topBar = {
                                                CenterAlignedTopAppBar(
                                                    title = { Text("Subscriptions & Bills", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                                                    navigationIcon = {
                                                        IconButton(onClick = { overlayScreen = OverlayScreen.None }) {
                                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                                        }
                                                    }
                                                )
                                            }
                                        ) { paddingValues ->
                                            SubscriptionsScreen(
                                                viewModel = viewModel,
                                                modifier = Modifier.padding(paddingValues)
                                            )
                                        }
                                    }
                                    OverlayScreen.Payments -> {
                                        Scaffold(
                                            topBar = {
                                                CenterAlignedTopAppBar(
                                                    title = { Text("Payments Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                                                    navigationIcon = {
                                                        IconButton(onClick = { overlayScreen = OverlayScreen.None }) {
                                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                                        }
                                                    }
                                                )
                                            }
                                        ) { paddingValues ->
                                            PaymentsScreen(
                                                viewModel = viewModel,
                                                modifier = Modifier.padding(paddingValues)
                                            )
                                        }
                                    }
                                    OverlayScreen.CreditCommand -> {
                                        CreditCommandCenter(
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

    override fun onStop() {
        super.onStop()
        if (::viewModel.isInitialized && viewModel.isBiometricEnabled.value) {
            viewModel.isUnlocked.value = false
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
