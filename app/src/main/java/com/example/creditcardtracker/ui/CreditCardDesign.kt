package com.example.creditcardtracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcardtracker.theme.vaultGlow
import java.text.NumberFormat
import java.util.Locale

object CardDesignHelper {
    fun detectCardType(number: String): String {
        val clean = number.filter { it.isDigit() }
        return when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") ||
                    (clean.length >= 4 && clean.take(4).toIntOrNull() in 2221..2720) -> "Mastercard"
            clean.startsWith("34") || clean.startsWith("37") -> "American Express"
            clean.startsWith("62") -> "UnionPay"
            clean.startsWith("35") && clean.length >= 4 && clean.take(4).toIntOrNull() in 3528..3589 -> "JCB"
            clean.startsWith("36") || clean.startsWith("38") || (clean.length >= 3 && clean.take(3).toIntOrNull() in 300..305) -> "Diners Club"
            clean.startsWith("6011") || clean.startsWith("65") || (clean.length >= 3 && clean.take(3).toIntOrNull() in 644..649) -> "Discover"
            else -> "Visa"
        }
    }

    fun getCardNumberLengthRange(type: String): IntRange {
        return when (type) {
            "American Express" -> 15..15
            "Diners Club" -> 14..14
            "UnionPay" -> 16..19
            else -> 16..16
        }
    }

    fun formatCardNumber(number: String, type: String): String {
        val clean = number.filter { it.isDigit() }
        val sb = StringBuilder()
        when (type) {
            "American Express" -> {
                // 4-6-5
                for (i in clean.indices) {
                    sb.append(clean[i])
                    if (i == 3 || i == 9) {
                        if (i != clean.lastIndex) sb.append(" ")
                    }
                }
            }
            "Diners Club" -> {
                // 4-6-4
                for (i in clean.indices) {
                    sb.append(clean[i])
                    if (i == 3 || i == 9) {
                        if (i != clean.lastIndex) sb.append(" ")
                    }
                }
            }
            else -> {
                // 4-4-4-4
                for (i in clean.indices) {
                    sb.append(clean[i])
                    if (i % 4 == 3 && i != clean.lastIndex) {
                        sb.append(" ")
                    }
                }
            }
        }
        return sb.toString()
    }

    fun getTierBrush(tier: String): Brush {
        return when (tier) {
            "Gold" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE5A93B),
                    Color(0xFFFFF6BD),
                    Color(0xFFB8860B),
                    Color(0xFFE5A93B)
                )
            )
            "Titanium" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF435058),
                    Color(0xFF8F9FA9),
                    Color(0xFF283035)
                )
            )
            "Platinum" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFCFD9DF),
                    Color(0xFFE2EBF0),
                    Color(0xFFA1B0BC)
                )
            )
            "Signature" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF334155),
                    Color(0xFF020617)
                )
            )
            "Infinite" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF000000),
                    Color(0xFF2E2E2E),
                    Color(0xFF080808)
                )
            )
            "World" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1E2F),
                    Color(0xFF3B1E54),
                    Color(0xFF12121A)
                )
            )
            "World Elite" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF2D0938),
                    Color(0xFF5E2B6D),
                    Color(0xFF1D0624)
                )
            )
            "Emerald" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0B5D34),
                    Color(0xFF16A34A),
                    Color(0xFF052E16)
                )
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E3C72),
                    Color(0xFF2A5298)
                )
            )
        }
    }

    fun getTierTextColor(tier: String): Color {
        return when (tier) {
            "Gold" -> Color(0xFF5C4017)
            "Platinum" -> Color(0xFF374151)
            "Titanium" -> Color(0xFFF3F4F6)
            else -> Color.White
        }
    }
}

@Composable
fun CardChip(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(38.dp, 28.dp)) {
        val width = size.width
        val height = size.height
        
        // Chip base
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFAD961), Color(0xFFF76B1C))
            ),
            size = size,
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        
        // Horizontal middle grid line
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = 1.dp.toPx()
        )
        
        // Vertical lines
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(width / 3, 0f),
            end = Offset(width / 3, height),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(width * 2 / 3, 0f),
            end = Offset(width * 2 / 3, height),
            strokeWidth = 1.dp.toPx()
        )
        
        // Inner detail curves
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.15f),
            topLeft = Offset(width * 0.15f, height * 0.15f),
            size = Size(width * 0.7f, height * 0.7f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun CardBackgroundWaves(tier: String, modifier: Modifier = Modifier) {
    val strokeColor = when (tier) {
        "Gold" -> Color.White.copy(alpha = 0.15f)
        "Platinum" -> Color(0xFF6B7280).copy(alpha = 0.12f)
        "Titanium" -> Color.White.copy(alpha = 0.10f)
        "Infinite", "Signature" -> Color(0xFFD4AF37).copy(alpha = 0.10f) // Gold lines on black
        else -> Color.White.copy(alpha = 0.10f)
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw elegant holographic wavy curves
        val path1 = Path().apply {
            moveTo(0f, height * 0.8f)
            cubicTo(
                width * 0.25f, height * 0.9f,
                width * 0.6f, height * 0.3f,
                width, height * 0.4f
            )
        }
        drawPath(
            path = path1,
            color = strokeColor,
            style = Stroke(width = 1.5.dp.toPx())
        )
        
        val path2 = Path().apply {
            moveTo(0f, height * 0.65f)
            cubicTo(
                width * 0.3f, height * 0.85f,
                width * 0.5f, height * 0.2f,
                width, height * 0.25f
            )
        }
        drawPath(
            path = path2,
            color = strokeColor,
            style = Stroke(width = 1.dp.toPx())
        )

        val path3 = Path().apply {
            moveTo(0f, height * 0.5f)
            cubicTo(
                width * 0.4f, height * 0.7f,
                width * 0.7f, height * 0.15f,
                width, height * 0.1f
            )
        }
        drawPath(
            path = path3,
            color = strokeColor,
            style = Stroke(width = 0.8.dp.toPx())
        )
    }
}

@Composable
fun CardBrandLogo(type: String, modifier: Modifier = Modifier) {
    when (type) {
        "Mastercard" -> {
            Canvas(modifier = modifier.size(38.dp, 24.dp)) {
                val r = 11.dp.toPx()
                val cy = size.height / 2
                
                // Left Red Circle
                drawCircle(
                    color = Color(0xFFEB001B),
                    radius = r,
                    center = Offset(size.width * 0.36f, cy)
                )
                // Right Orange/Yellow Circle
                drawCircle(
                    color = Color(0xFFF79E1B).copy(alpha = 0.85f),
                    radius = r,
                    center = Offset(size.width * 0.64f, cy)
                )
            }
        }
        "Visa" -> {
            Box(
                modifier = modifier
                    .width(46.dp)
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        "American Express" -> {
            Box(
                modifier = modifier
                    .background(Color(0xFF0173C2), RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AMEX",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        "UnionPay" -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFD81B60))
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF1E3C72))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "UnionPay",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
        }
        "JCB" -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(12.dp)
                        .background(Color(0xFF0038A8))
                )
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(12.dp)
                        .background(Color(0xFFD11C1C))
                )
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(12.dp)
                        .background(Color(0xFF008A4B))
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "JCB",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }
        "Discover" -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISC",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = Color(0xFFFF6600))
                }
                Text(
                    text = "VER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        "Diners Club" -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(color = Color.White, style = Stroke(1.dp.toPx()))
                    drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 5.dp.toPx(), style = Stroke(1.dp.toPx()))
                }
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Diners",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp
                )
            }
        }
        else -> {
            Box(modifier = modifier.size(1.dp))
        }
    }
}

@Composable
fun CreditCardDesign(
    bank: String,
    name: String,
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    cardType: String,
    cardTier: String,
    activeSpend: Double? = null,
    creditLimit: Double? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brush = CardDesignHelper.getTierBrush(cardTier)
    val textColor = CardDesignHelper.getTierTextColor(cardTier)
    fun formatBdt(amount: Double): String {
        return "৳ " + com.example.creditcardtracker.theme.formatBdtValue(amount)
    }
    
    var isRevealed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val formattedNum = if (cardNumber.length >= 4) {
        CardDesignHelper.formatCardNumber(cardNumber, cardType)
    } else {
        "•••• •••• •••• ••••"
    }

    Card(
        modifier = modifier
            .width(280.dp)
            .height(160.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        ) {
            // Background waves pattern
            CardBackgroundWaves(tier = cardTier)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Bank Name and Tier Badge + Visibility eye
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = bank.uppercase(),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = name,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Eye Visibility Toggle
                        Icon(
                            imageVector = if (isRevealed) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { isRevealed = !isRevealed }
                        )

                        // Premium Tier Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = textColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = cardTier.uppercase(),
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Middle Row: Chip and Brand Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CardChip()
                    
                    CardBrandLogo(type = cardType)
                }

                // Bottom Section: Card Number and Details
                Column {
                    val displayNum = if (isRevealed) {
                        formattedNum
                    } else {
                        if (cardNumber.length >= 4) {
                            val last4 = cardNumber.takeLast(4)
                            when (cardType) {
                                "American Express" -> "•••• •••••• •$last4"
                                "Diners Club" -> "•••• •••••• •$last4"
                                else -> "•••• •••• •••• $last4"
                            }
                        } else {
                            "•••• •••• •••• ••••"
                        }
                    }

                    Text(
                        text = displayNum,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(
                                    text = "EXPIRY",
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = expiryDate.ifEmpty { "MM/YY" },
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "CVV",
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = if (isRevealed) cvv.ifEmpty { "•••" } else "•••",
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Cycle spend indicator if available
                        if (activeSpend != null && creditLimit != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "CYCLE SPEND",
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = formatBdt(activeSpend),
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
