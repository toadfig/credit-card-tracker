package com.example.creditcardtracker.security

import java.util.regex.Pattern

object SmsParser {
    
    fun parseAmount(body: String): Double? {
        // Look for currency prefixes: BDT 500, USD 4.50, $12.30, etc.
        val amountRegex = Pattern.compile(
            """(?i)\b(?:BDT|USD|Tk|Tk\.|EUR|GBP|Rs\.|Rs|\$)\s*([\d,]+\.?\d*)""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = amountRegex.matcher(body)
        if (matcher.find()) {
            val numberStr = matcher.group(1)?.replace(",", "")
            return numberStr?.toDoubleOrNull()
        }
        
        // Fallback: look for general decimal numbers near transaction words
        val fallbackRegex = Pattern.compile(
            """(?:spent|charged|debited|txn|payment\s+of)\s*(?:of\s+)?([\d,]+\.?\d*)""",
            Pattern.CASE_INSENSITIVE
        )
        val fallbackMatcher = fallbackRegex.matcher(body)
        if (fallbackMatcher.find()) {
            val numberStr = fallbackMatcher.group(1)?.replace(",", "")
            return numberStr?.toDoubleOrNull()
        }
        return null
    }

    fun parseMerchant(body: String): String {
        // Patterns like "at MERCHANT on" or "at MERCHANT at" or "in MERCHANT"
        val merchantPatterns = listOf(
            Pattern.compile("""(?i)\bat\s+([^,.\n]+?)\s+(?:on|at|for|date|with)\b"""),
            Pattern.compile("""(?i)\bat\s+([^,.\n]{1,30})"""),
            Pattern.compile("""(?i)\bto\s+([^,.\n]+?)\s+(?:on|at|for|date)\b"""),
            Pattern.compile("""(?i)\bto\s+([^,.\n]{1,30})""")
        )

        for (pattern in merchantPatterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val rawMerchant = matcher.group(1)?.trim() ?: ""
                if (rawMerchant.isNotEmpty() && rawMerchant.length < 30) {
                    return rawMerchant
                }
            }
        }
        return "Unknown Merchant"
    }
}
