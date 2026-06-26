package com.example.creditcardtracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.data.Transaction
import com.example.creditcardtracker.data.TransactionType
import com.example.creditcardtracker.theme.vaultGlass
import com.example.creditcardtracker.theme.BdtText
import com.example.creditcardtracker.theme.formatBdtValue
import java.text.SimpleDateFormat
import java.util.*

data class MonthlyCashflow(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

@Composable
fun AnalyticsScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val transactions = viewModel.transactions
    val scrollState = rememberScrollState()

    // 1. Calculate cashflow of the last 6 months
    val monthlyCashflowList = remember(transactions) {
        val list = mutableListOf<MonthlyCashflow>()
        val cal = Calendar.getInstance()
        for (i in 5 downTo 0) {
            val checkCal = Calendar.getInstance()
            checkCal.add(Calendar.MONTH, -i)
            val year = checkCal.get(Calendar.YEAR)
            val month = checkCal.get(Calendar.MONTH)

            val income = transactions.filter { tx ->
                tx.type == TransactionType.INCOME && Calendar.getInstance().apply { timeInMillis = tx.date }.let {
                    it.get(Calendar.YEAR) == year && it.get(Calendar.MONTH) == month
                }
            }.sumOf { it.amount * it.exchangeRate }

            val expense = transactions.filter { tx ->
                tx.type == TransactionType.EXPENSE && Calendar.getInstance().apply { timeInMillis = tx.date }.let {
                    it.get(Calendar.YEAR) == year && it.get(Calendar.MONTH) == month
                }
            }.sumOf { it.amount * it.exchangeRate }

            val sdf = SimpleDateFormat("MMM", Locale.US)
            list.add(MonthlyCashflow(sdf.format(checkCal.time), income, expense))
        }
        list
    }

    // 2. Spending breakdown by category for the current month
    val currentMonthExpenses = remember(transactions) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && Calendar.getInstance().apply { timeInMillis = tx.date }.let {
                it.get(Calendar.YEAR) == year && it.get(Calendar.MONTH) == month
            }
        }
    }

    val totalSpending = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount * it.exchangeRate }
    }

    val categories = listOf("Food & Dining", "Groceries", "Transportation", "Shopping", "Utilities", "Healthcare", "Education", "Entertainment", "Others")
    val categoryColors = listOf(
        MaterialTheme.colorScheme.primary,                    // Food & Dining
        MaterialTheme.colorScheme.secondary,                  // Groceries
        MaterialTheme.colorScheme.tertiary,                   // Transportation
        MaterialTheme.colorScheme.error,                      // Shopping
        MaterialTheme.colorScheme.outline,                    // Utilities
        MaterialTheme.colorScheme.secondaryContainer,         // Healthcare
        MaterialTheme.colorScheme.primaryContainer,           // Education
        Color(0xFFFF8A65),                                    // Entertainment
        Color(0xFF90A4AE)                                     // Others
    )

    val spendByCategory = remember(currentMonthExpenses) {
        categories.map { category ->
            currentMonthExpenses.filter { it.category == category }.sumOf { it.amount * it.exchangeRate }
        }
    }

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Cashflow Analytics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Tab Card 1: Monthly Flow (Income vs Expense)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .vaultGlass(borderRadius = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Weekly Flow (Last 6 Months)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Cashflow Bars
                val maxVal = remember(monthlyCashflowList) {
                    val maxInList = monthlyCashflowList.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 100.0
                    maxInList.coerceAtLeast(100.0)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyCashflowList.forEach { flow ->
                        val incFraction = (flow.income / maxVal).toFloat().coerceIn(0.02f, 1f)
                        val expFraction = (flow.expense / maxVal).toFloat().coerceIn(0.02f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Income bar (Primary / Cyan-Teal)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(incFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                // Expense bar (Error / Coral-Red)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(expFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                )
                            }
                            Text(
                                text = flow.monthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Legend indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
                        Text("Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.error))
                        Text("Expense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Tab Card 2: Spending Breakdown Donut Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .vaultGlass(borderRadius = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Spending Breakdown (Current Month)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (totalSpending > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Drawn Donut Chart
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(spendByCategory, totalSpending) {
                                        detectTapGestures { offset ->
                                            val cx = size.width / 2f
                                            val cy = size.height / 2f
                                            val dx = offset.x - cx
                                            val dy = offset.y - cy
                                            val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                                            val outerRadius = size.width / 2f
                                            val innerRadius = outerRadius * 0.6f

                                            if (distance in innerRadius..outerRadius) {
                                                val angleDeg = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                                val clockwiseAngle = (angleDeg + 90.0 + 360.0) % 360.0

                                                var currentStart = 0.0
                                                spendByCategory.forEachIndexed { index, amount ->
                                                    val sweep = (amount / totalSpending) * 360.0
                                                    if (sweep > 0.0) {
                                                        val currentEnd = currentStart + sweep
                                                        if (clockwiseAngle >= currentStart && clockwiseAngle < currentEnd) {
                                                            val clickedCat = categories[index]
                                                            selectedCategory = if (selectedCategory == clickedCat) null else clickedCat
                                                        }
                                                        currentStart = currentEnd
                                                    }
                                                }
                                            } else {
                                                selectedCategory = null
                                            }
                                        }
                                    }
                            ) {
                                var startAngle = -90f
                                spendByCategory.forEachIndexed { idx, amount ->
                                    val sweepAngle = (amount / totalSpending).toFloat() * 360f
                                    if (sweepAngle > 0f) {
                                        val isSelected = categories[idx] == selectedCategory
                                        val color = categoryColors[idx]
                                        drawArc(
                                            color = if (selectedCategory == null || isSelected) color else color.copy(alpha = 0.3f),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            size = size,
                                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedCategory != null) selectedCategory!!.take(6) + ".." else "Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val displayAmt = if (selectedCategory != null) {
                                    val catIdx = categories.indexOf(selectedCategory)
                                    spendByCategory.getOrElse(catIdx) { 0.0 }
                                } else {
                                    totalSpending
                                }
                                Text(
                                    text = "৳" + formatBdtValue(displayAmt).substringBefore("."),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Legends Sidebar
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.forEachIndexed { idx, category ->
                                val amount = spendByCategory[idx]
                                val percent = if (totalSpending > 0) ((amount / totalSpending) * 100).toInt() else 0
                                if (amount > 0) {
                                    val isSelected = category == selectedCategory
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                            .clickable { selectedCategory = if (isSelected) null else category }
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No spending logged this month.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
