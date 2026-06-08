package com.example.creditcardtracker.data

import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bank: String,
    val cardNumber: String,      // Last 4 or fully stored (encrypted)
    val expiryDate: String,      // MM/YY
    val cvv: String,            // Encrypted CVV
    val creditLimit: Double,
    val statementDay: Int,       // Statement date (day of month)
    val dueDay: Int,             // Due date (day of month)
    val annualFee: Double,
    val isFeeRedeemable: Boolean,
    val feeRedemptionLimit: Double, // Criteria limit (e.g. 5000 spend)
    val feeRedemptionUnit: String,  // Spend / Points
    val cardColorIndex: Int = 0,  // Design index for UI
    val isSmsTrackingEnabled: Boolean = false,
    val smsSender: String = "",
    val cardType: String? = "Visa",
    val cardTier: String? = "Classic",
    val annualLoungeQuota: Int = 0,
    val cashbackRate: Double = 0.0,
    val rewardPointsRate: Double = 0.0, // Points per unit spend
    val bankHelpline: String = ""
) {
    val safeCardType: String get() = cardType ?: "Visa"
    val safeCardTier: String get() = cardTier ?: "Classic"
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long,
    val currency: String = "BDT",
    val exchangeRate: Double = 1.0
)

data class Payment(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val amount: Double,
    val date: Long,
    val notes: String
)

data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val name: String,
    val amount: Double,
    val billingDay: Int,
    val category: String,
    val isActive: Boolean = true
)

data class LoungeVisit(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val loungeName: String,
    val airport: String,
    val date: Long,
    val guestsCount: Int
)

data class EmiPlan(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val merchant: String,
    val totalAmount: Double,
    val monthlyInstallment: Double,
    val monthsDuration: Int,
    val startDate: Long,
    val isActive: Boolean = true
)

data class AppBackupData(
    val cards: List<CreditCard> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val loungeVisits: List<LoungeVisit> = emptyList(),
    val emiPlans: List<EmiPlan> = emptyList()
)
