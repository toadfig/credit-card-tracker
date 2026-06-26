package com.example.creditcardtracker.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Holding
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.BdtText
import com.example.creditcardtracker.theme.formatBdtValue
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val holdings = viewModel.holdings
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    // Standard Currency Formatting (BDT)
    fun formatBdt(amount: Double): String {
        return "৳ " + formatBdtValue(amount)
    }

    // Calculations (Multiplying by 120x to convert USD to BDT)
    val totalPortfolioValue = holdings.sumOf { it.shares * it.currentPrice } * 120.0
    val totalCostBasis = holdings.sumOf { it.shares * it.averageCost } * 120.0
    val totalGainLoss = totalPortfolioValue - totalCostBasis
    val gainLossPercent = if (totalCostBasis > 0) (totalGainLoss / totalCostBasis) * 100 else 0.0

    // Categorization logic for Donut Chart
    var stocksVal = 0.0
    var bondsVal = 0.0
    var cryptoVal = 0.0
    var cashVal = 0.0

    holdings.forEach { holding ->
        val value = holding.shares * holding.currentPrice
        when (holding.ticker) {
            "NVDA", "AAPL", "MSFT" -> stocksVal += value
            "BND" -> bondsVal += value
            "BTC" -> cryptoVal += value
            "USD" -> cashVal += value
            else -> {
                if (holding.ticker.endsWith("USD") || holding.ticker == "ETH" || holding.ticker == "SOL") {
                    cryptoVal += value
                } else {
                    stocksVal += value
                }
            }
        }
    }

    val sum = stocksVal + bondsVal + cryptoVal + cashVal
    val (stocksPct, bondsPct, cryptoPct, cashPct) = if (sum > 0) {
        listOf(stocksVal / sum, bondsVal / sum, cryptoVal / sum, cashVal / sum)
    } else {
        listOf(0.60, 0.20, 0.15, 0.05) // fallback default allocation
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
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Portfolio",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    BdtText(
                        amount = totalPortfolioValue,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // High-End Gain/Loss Capsule
                Surface(
                    color = if (totalGainLoss >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (totalGainLoss >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                            contentDescription = "Trending",
                            tint = if (totalGainLoss >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%+.2f%% (৳ %s)", gainLossPercent, formatBdtValue(totalGainLoss)),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalGainLoss >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Allocation Donut Chart and Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .vaultGlass(borderRadius = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Donut Canvas
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val stocksColor = MaterialTheme.colorScheme.primary
                        val bondsColor = MaterialTheme.colorScheme.secondary
                        val cryptoColor = MaterialTheme.colorScheme.tertiary
                        val cashColor = MaterialTheme.colorScheme.outline

                        Canvas(modifier = Modifier.size(110.dp)) {
                            var startAngle = -90f
                            val sweepStocks = (stocksPct * 360f).toFloat()
                            val sweepBonds = (bondsPct * 360f).toFloat()
                            val sweepCrypto = (cryptoPct * 360f).toFloat()
                            val sweepCash = (cashPct * 360f).toFloat()

                            // Stocks
                            if (sweepStocks > 0f) {
                                drawArc(
                                    color = stocksColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepStocks,
                                    useCenter = false,
                                    size = size,
                                    style = Stroke(width = 10.dp.toPx())
                                )
                                startAngle += sweepStocks
                            }

                            // Bonds
                            if (sweepBonds > 0f) {
                                drawArc(
                                    color = bondsColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepBonds,
                                    useCenter = false,
                                    size = size,
                                    style = Stroke(width = 10.dp.toPx())
                                )
                                startAngle += sweepBonds
                            }

                            // Crypto
                            if (sweepCrypto > 0f) {
                                drawArc(
                                    color = cryptoColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepCrypto,
                                    useCenter = false,
                                    size = size,
                                    style = Stroke(width = 10.dp.toPx())
                                )
                                startAngle += sweepCrypto
                            }

                            // Cash
                            if (sweepCash > 0f) {
                                drawArc(
                                    color = cashColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepCash,
                                    useCenter = false,
                                    size = size,
                                    style = Stroke(width = 10.dp.toPx())
                                )
                                startAngle += sweepCash
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Diversified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Good", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Legend Panel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LegendRow(name = "Stocks", pct = stocksPct, color = MaterialTheme.colorScheme.primary)
                        LegendRow(name = "Bonds", pct = bondsPct, color = MaterialTheme.colorScheme.secondary)
                        LegendRow(name = "Crypto", pct = cryptoPct, color = MaterialTheme.colorScheme.tertiary)
                        LegendRow(name = "Cash", pct = cashPct, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Holdings Section Title
            Text(
                text = "Your Holdings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Holdings High-Density List
            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No holdings tracked yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = holdings, key = { it.id }) { holding ->
                        HoldingItem(
                            holding = holding,
                            onDelete = { viewModel.deleteHolding(holding.id) }
                        )
                    }
                }
            }
        }

        // Add Holding Extended Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Asset")
        }

        if (showAddDialog) {
            AddHoldingDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { ticker, name, shares, cost, price, daily ->
                    viewModel.addHolding(ticker, name, shares, cost, price, daily)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun LegendRow(name: String, pct: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(text = name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = String.format(Locale.US, "%.0f%%", pct * 100),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HoldingItem(holding: Holding, onDelete: () -> Unit) {
    val value = (holding.shares * holding.currentPrice) * 120.0
    val totalCost = (holding.shares * holding.averageCost) * 120.0
    val gain = value - totalCost
    val isPositive = gain >= 0

    // Fake sparkline points based on ticker hash to keep it consistent yet dynamic
    val sparkPoints = remember(holding.ticker, holding.currentPrice) {
        val hash = holding.ticker.hashCode()
        val list = mutableListOf<Float>()
        var curr = holding.currentPrice.toFloat() * 0.95f
        list.add(curr)
        val rand = java.util.Random(hash.toLong())
        for (i in 1..8) {
            val change = (rand.nextFloat() - 0.48f) * 0.03f // slight bias upwards/downwards
            curr *= (1f + change)
            list.add(curr)
        }
        list.add(holding.currentPrice.toFloat())
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.2f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when (holding.ticker) {
                            "BTC", "ETH" -> Icons.Outlined.CurrencyBitcoin
                            "USD" -> Icons.Outlined.AttachMoney
                            "BND" -> Icons.Outlined.Receipt
                            else -> Icons.Outlined.Analytics
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = holding.ticker,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = holding.ticker,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = holding.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Custom Sparkline Paths
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .height(30.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Sparkline(
                    points = sparkPoints,
                    color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Value + Change %
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                BdtText(
                    amount = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = String.format(Locale.US, "%+.1f%%", holding.dailyChangePercent),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (holding.dailyChangePercent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete holding",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val delta = (maxVal - minVal).let { if (it == 0f) 1f else it }

        val path = Path()
        points.forEachIndexed { index, value ->
            val x = (index.toFloat() / (points.size - 1)) * width
            val y = height - ((value - minVal) / delta) * height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Double, Double) -> Unit
) {
    val context = LocalContext.current
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var sharesText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var dailyText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Asset", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Ticker (e.g. NVDA, BTC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. NVIDIA Corp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sharesText,
                        onValueChange = { sharesText = it },
                        label = { Text("Shares") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Avg Cost (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Current Price (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dailyText,
                        onValueChange = { dailyText = it },
                        label = { Text("Daily Change %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val shares = sharesText.toDoubleOrNull() ?: 0.0
                    val costBdt = costText.toDoubleOrNull() ?: 0.0
                    val priceBdt = priceText.toDoubleOrNull() ?: 0.0
                    val daily = dailyText.toDoubleOrNull() ?: 0.0

                    if (ticker.isBlank() || name.isBlank() || shares <= 0.0 || costBdt <= 0.0 || priceBdt <= 0.0) {
                        Toast.makeText(context, "Please fill in all details.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Store in native USD values
                        onConfirm(ticker.trim(), name.trim(), shares, costBdt / 120.0, priceBdt / 120.0, daily)
                    }
                }
            ) {
                Text("Add Asset", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
