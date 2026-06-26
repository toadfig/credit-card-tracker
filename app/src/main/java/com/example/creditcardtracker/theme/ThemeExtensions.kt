package com.example.creditcardtracker.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Format helper for South Asian currency digit grouping (lakhs & crores).
 */
fun formatBdtValue(amount: Double): String {
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    val formattedNumber = formatter.format(absAmount)
    return if (isNegative) "-$formattedNumber" else formattedNumber
}

/**
 * Premium BDT Currency Composable that renders the TAKA (৳) symbol 20% smaller
 * and lighter weight than the numerical amount, ensuring focus on the value.
 */
@Composable
fun BdtText(
    amount: Double,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val numberText = formatBdtValue(amount)
    val textColor = if (color != Color.Unspecified) color else style.color
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = style.fontSize * 0.8f,
            fontWeight = FontWeight.Normal,
            color = textColor
        )) {
            append("৳ ")
        }
        withStyle(style = SpanStyle(
            fontSize = style.fontSize,
            fontWeight = fontWeight ?: style.fontWeight ?: FontWeight.Normal,
            color = textColor
        )) {
            append(numberText)
        }
    }
    Text(text = annotatedString, style = style, modifier = modifier)
}

object VaultUiTokens {
    // Symmetrical Shape Tokens
    val ShapeFullPill = RoundedCornerShape(50)
    val ShapeCardLarge = RoundedCornerShape(24.dp)
    val ShapeChipSmall = RoundedCornerShape(12.dp)

    // Secure Theme Colors
    val VaultDeepBlue = Color(0xFF0D1B2A)
    val VaultEmerald = Color(0xFF00E676)
    val VaultTeal = Color(0xFF00B4D8)
    val VaultGold = Color(0xFFFFB703)
    val VaultCoral = Color(0xFFFB8500)
    
    // Night Mode Abyss Tints
    val AbyssBlack = Color(0xFF0A0F1D)
    val AbyssCharcoal = Color(0xFF1B263B)
    val AbyssNeonCyan = Color(0xFF00F5FF)

    // Glassmorphic Colors
    val GlassWhiteDay = Color(0x33FFFFFF)       // 20% transparent white for Day Mode
    val GlassCharcoalNight = Color(0x660B0E14)   // 40% transparent smoky slate for Night Mode
}

/**
 * Animated liquid-gradient backdrop that shifts colors organically.
 * Adapts automatically to Day and Night themes.
 */
@Composable
fun animatedVaultGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "vaultFlow")

    val startColorA = if (isDark) VaultUiTokens.AbyssBlack else Color(0xFFE8F1F2)
    val endColorA = if (isDark) VaultUiTokens.AbyssCharcoal else Color(0xFFB5E2FA)

    val startColorB = if (isDark) Color(0xFF0B1425) else Color(0xFFEDF2F4)
    val endColorB = if (isDark) Color(0xFF1E2D4A) else Color(0xFFD8F3DC)

    val colorShiftA by infiniteTransition.animateColor(
        initialValue = startColorA,
        targetValue = endColorA,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vaultColorA"
    )

    val colorShiftB by infiniteTransition.animateColor(
        initialValue = startColorB,
        targetValue = endColorB,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vaultColorB"
    )

    return Brush.linearGradient(
        colors = listOf(colorShiftA, colorShiftB)
    )
}

/**
 * Hardware-accelerated Gaussian Blur and Glassmorphic Modifier for lists & panels.
 */
@Composable
fun Modifier.vaultGlass(
    borderRadius: Dp = 24.dp,
    glowColor: Color = Color.Transparent
): Modifier {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    return this
        .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(borderRadius))
        .clip(RoundedCornerShape(borderRadius))
        .background(surfaceColor)
}

/**
 * Custom glow shadow shader.
 */
fun Modifier.vaultGlow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 24.dp,
    shadowRadius: Dp = 16.dp,
    offsetY: Dp = 4.dp
): Modifier = this.drawBehind {
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.color = color.copy(alpha = alpha).toArgb()
    frameworkPaint.setShadowLayer(
        shadowRadius.toPx(),
        0f,
        offsetY.toPx(),
        color.copy(alpha = alpha).toArgb()
    )
    drawIntoCanvas { canvas ->
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}
