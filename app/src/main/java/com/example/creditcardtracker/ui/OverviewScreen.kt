package com.example.creditcardtracker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.*
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
import com.example.creditcardtracker.data.CreditCard
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
    modifier: Modifier = Modifier
) {
    val cards = viewModel.cards
    val selectedIndex by viewModel.selectedIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cards.isEmpty()) {
            // Empty State
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
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = "No Cards",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Secure Credit Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track your billing cycles, statement balances, fees, and payments in one fully secure, offline space.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onManageCardsClick,
                        shape = VaultUiTokens.ShapeFullPill
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Card")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add First Card")
                    }
                }
            }
        } else {
            // Cards Dashboard
            val currentCard = cards.getOrNull(selectedIndex) ?: cards[0]
            val lazyListState = rememberLazyListState()

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title and Quick Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Card Overview",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Swipe to view cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Horizontal snap scrolling card list
                LazyRow(
                    state = lazyListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(cards) { index, card ->
                        CreditCardWidget(
                            card = card,
                            isSelected = index == selectedIndex,
                            viewModel = viewModel,
                            onClick = { viewModel.selectedIndex.value = index }
                        )
                    }
                }

                // Indicator dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    cards.forEachIndexed { index, _ ->
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

                // Selected Card Details Card
                SelectedCardDetailsView(
                    card = currentCard,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }

            // Floating action button inside Overview
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
                    Icon(Icons.Outlined.Settings, contentDescription = "Manage Cards")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Cards", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreditCardWidget(
    card: CreditCard,
    isSelected: Boolean,
    viewModel: TrackerViewModel,
    onClick: () -> Unit
) {
    val activeSpend = viewModel.getSpendInCurrentCycle(card)

    CreditCardDesign(
        bank = card.bank,
        name = card.name,
        cardNumber = card.cardNumber,
        expiryDate = card.expiryDate,
        cvv = card.cvv,
        cardType = card.safeCardType,
        cardTier = card.safeCardTier,
        activeSpend = activeSpend,
        creditLimit = card.creditLimit,
        isSelected = isSelected,
        onClick = onClick
    )
}

@Composable
fun SelectedCardDetailsView(
    card: CreditCard,
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val activeSpend = viewModel.getSpendInCurrentCycle(card)
    val activePayments = viewModel.getPaymentsInCurrentCycle(card)
    val remainingLimit = (card.creditLimit - activeSpend).coerceAtLeast(0.0)
    val unpaidStatementBalance = (activeSpend - activePayments).coerceAtLeast(0.0)

    val cycleRange = viewModel.getBillingCycleRange(card.statementDay)
    val dueDateMillis = viewModel.getDueDateForCycle(card.statementDay, card.dueDay)
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    // Calculate days remaining
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
                .padding(20.dp),
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
                        daysRemaining == 1 -> "Due tomorrow!"
                        else -> "Due in $daysRemaining days (on ${dateFormat.format(dueDateMillis)})"
                    }
                    val statusColor = when {
                        unpaidStatementBalance <= 0 -> MaterialTheme.colorScheme.primary
                        daysRemaining < 0 -> MaterialTheme.colorScheme.error
                        daysRemaining <= 5 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Status",
                        tint = statusColor
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Billing Cycle Progress Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Billing Cycle",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${dateFormat.format(cycleRange.first)} - ${dateFormat.format(cycleRange.second)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Light
                    )
                }
                
                val limitFraction = (activeSpend / card.creditLimit).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { limitFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (limitFraction > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spend: ${currencyFormat.format(activeSpend)} (${(limitFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Available: ${currencyFormat.format(remainingLimit)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

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
                        text = if (card.annualFee > 0) currencyFormat.format(card.annualFee) else "No Fee",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (card.annualFee > 0) {
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
                            text = if (card.isFeeRedeemable) "Redeemable" else "Non-Redeemable",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (card.isFeeRedeemable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (card.isFeeRedeemable) {
                        // Progress to redemption
                        val target = card.feeRedemptionLimit
                        val progressPercent = if (card.feeRedemptionUnit == "Spend") {
                            val activeYearSpend = activeSpend // Standard estimate based on active cycle or full db (simplicity uses activeSpend)
                            ((activeYearSpend / target) * 100).coerceIn(0.0, 100.0)
                        } else {
                            0.0 // Points entry could be standalone (simply show target info)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (card.feeRedemptionUnit == "Spend") {
                                        "Spend requirement: Spend ${currencyFormat.format(target)} to waive fee. Current cycle progress: ${progressPercent.toInt()}%."
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

            // Summary stats (unpaid / payments)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Payments Made", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currencyFormat.format(activePayments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Remaining Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currencyFormat.format(unpaidStatementBalance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (unpaidStatementBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
