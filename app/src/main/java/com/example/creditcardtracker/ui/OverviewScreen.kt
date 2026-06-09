package com.example.creditcardtracker.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.EmiPlan
import com.example.creditcardtracker.data.LoungeVisit
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
            val currentCard = cards.getOrNull(selectedIndex) ?: cards[0]
            val lazyListState = rememberLazyListState()

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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

                LazyRow(
                    state = lazyListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(items = cards, key = { _, card -> card.id }) { index, card ->
                        CreditCardWidget(
                            card = card,
                            isSelected = index == selectedIndex,
                            viewModel = viewModel,
                            onClick = { viewModel.selectedIndex.value = index }
                        )
                    }
                }

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

                SelectedCardDetailsView(
                    card = currentCard,
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
    val context = LocalContext.current
    val activeSpend = viewModel.getSpendInCurrentCycle(card)
    val activePayments = viewModel.getPaymentsInCurrentCycle(card)
    val remainingLimit = (card.creditLimit - activeSpend).coerceAtLeast(0.0)
    val unpaidStatementBalance = (activeSpend - activePayments).coerceAtLeast(0.0)

    val cycleRange = viewModel.getBillingCycleRange(card.statementDay)
    val dueDateMillis = viewModel.getDueDateForCycle(card.statementDay, card.dueDay)
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val daysRemaining = ((dueDateMillis - today) / (1000 * 60 * 60 * 24)).toInt()

    var showLoungeLogDialog by remember { mutableStateOf(false) }
    var showEmiLogDialog by remember { mutableStateOf(false) }

    // Interest payment simulator state
    var simulatedPaymentFraction by remember { mutableStateOf(1f) }
    val simulatedPaymentAmount = unpaidStatementBalance * simulatedPaymentFraction
    val simulatedUnpaidAmount = (unpaidStatementBalance - simulatedPaymentAmount).coerceAtLeast(0.0)
    val simulatedInterestCharge = simulatedUnpaidAmount * 0.025 // 2.5% finance charge estimate

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

            // Airport Lounge visits
            if (card.annualLoungeQuota > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                val visitsForCard = viewModel.loungeVisits.filter { it.cardId == card.id }
                val totalGuests = visitsForCard.sumOf { it.guestsCount }
                val visitsUsed = visitsForCard.size + totalGuests
                val visitsLeft = (card.annualLoungeQuota - visitsUsed).coerceAtLeast(0)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Airport Lounge Access",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showLoungeLogDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.FlightTakeoff,
                                contentDescription = "Log Lounge Visit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Quota left: $visitsLeft / ${card.annualLoungeQuota} complimentary visits",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (visitsForCard.isNotEmpty()) {
                                Text(
                                    text = "Last logged visit on ${dateFormat.format(visitsForCard.last().date)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Active EMIs Section
            val emiForCard = viewModel.emiPlans.filter { it.cardId == card.id && it.isActive }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Installments (EMI)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showEmiLogDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add EMI Plan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (emiForCard.isEmpty()) {
                    Text(
                        text = "No active installment plans on this card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    emiForCard.forEach { emi ->
                        // Calculate installment progress
                        val emiStartCal = Calendar.getInstance().apply { timeInMillis = emi.startDate }
                        val cycleStartCal = Calendar.getInstance().apply { timeInMillis = cycleRange.first }
                        val yearsDiff = cycleStartCal.get(Calendar.YEAR) - emiStartCal.get(Calendar.YEAR)
                        val monthsDiff = cycleStartCal.get(Calendar.MONTH) - emiStartCal.get(Calendar.MONTH) + (yearsDiff * 12)
                        val currentMonth = (monthsDiff + 1).coerceIn(1, emi.monthsDuration)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = emi.merchant,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Month $currentMonth of ${emi.monthsDuration} | Total: ${currencyFormat.format(emi.totalAmount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${currencyFormat.format(emi.monthlyInstallment)}/mo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Interest & Minimum Payment Simulator
            if (unpaidStatementBalance > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Interest & Minimum Simulator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Slide to simulate partial payments and preview finance interest charges.",
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Simulated Payment", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(simulatedPaymentAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. Finance Interest (2.5%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (simulatedInterestCharge > 0) currencyFormat.format(simulatedInterestCharge) else "None",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (simulatedInterestCharge > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Helpline shortcut
            if (card.bankHelpline.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
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
                                imageVector = Icons.Outlined.Call,
                                contentDescription = "Call Helpline",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Emergency Helpline", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(card.bankHelpline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${card.bankHelpline}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Helpline unavailable.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Call", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Fees & Annual redemption criteria
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        val target = card.feeRedemptionLimit
                        val progressPercent = if (card.feeRedemptionUnit == "Spend") {
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
                        viewModel.addLoungeVisit(card.id, loungeName.trim(), airport.uppercase().trim(), System.currentTimeMillis(), guests)
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
                        Toast.makeText(context, "Invalid input credentials.", Toast.LENGTH_SHORT).show()
                    } else {
                        val monthly = total / duration
                        viewModel.addEmiPlan(card.id, merchant.trim(), total, monthly, duration, System.currentTimeMillis())
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
}
