package com.example.creditcardtracker.data

import java.util.UUID

enum class AccountType {
    CREDIT_CARD, BANK_ACCOUNT, MFS, CASH
}

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bank: String,                  // E.g. "BRAC Bank", "EBL", "bKash", "Cash"
    val accountType: AccountType,
    val balance: Double,               // Asset balance or outstanding balance depending on type
    val creditLimit: Double = 0.0,      // Credit card only
    val cardNumber: String = "",       // Last 4 or fully stored (encrypted)
    val expiryDate: String = "",       // MM/YY
    val cvv: String = "",              // Encrypted CVV
    val statementDay: Int = 1,         // Statement date (day of month)
    val dueDay: Int = 1,               // Due date (day of month)
    val annualFee: Double = 0.0,
    val isFeeRedeemable: Boolean = false,
    val feeRedemptionLimit: Double = 0.0, // Criteria limit (e.g. 5000 spend)
    val feeRedemptionUnit: String = "Spend",  // Spend / Points
    val accountColorIndex: Int = 0,    // Design index for UI
    val isSmsTrackingEnabled: Boolean = false,
    val smsSender: String = "",
    val cardType: String? = "Visa",    // Visa, Mastercard, Diners Club, etc.
    val cardTier: String? = "Classic", // Classic, Gold, Platinum, Emerald, Titanium, Stellar, etc.
    val annualLoungeQuota: Int = 0,
    val cashbackRate: Double = 0.0,
    val rewardPointsRate: Double = 0.0, // Points per unit spend
    val bankHelpline: String = ""
) {
    val safeCardType: String get() = cardType ?: "Visa"
    val safeCardTier: String get() = cardTier ?: "Classic"
}

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType = TransactionType.EXPENSE,
    val sourceAccountId: String,               // The account debited (for expense/transfer)
    val destinationAccountId: String? = null,  // The account credited (only for transfer)
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long,
    val currency: String = "BDT",
    val exchangeRate: Double = 1.0
)

// Kept for backward compatibility parsing old database backups
data class CreditCard(
    val id: String,
    val name: String,
    val bank: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val creditLimit: Double,
    val statementDay: Int,
    val dueDay: Int,
    val annualFee: Double,
    val isFeeRedeemable: Boolean,
    val feeRedemptionLimit: Double,
    val feeRedemptionUnit: String,
    val cardColorIndex: Int = 0,
    val isSmsTrackingEnabled: Boolean = false,
    val smsSender: String = "",
    val cardType: String? = "Visa",
    val cardTier: String? = "Classic",
    val annualLoungeQuota: Int = 0,
    val cashbackRate: Double = 0.0,
    val rewardPointsRate: Double = 0.0,
    val bankHelpline: String = ""
)

data class Expense(
    val id: String,
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
    val cardId: String,          // Reused as accountId for credit card account
    val amount: Double,
    val date: Long,
    val notes: String
)

data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,       // Rename cardId to accountId
    val name: String,
    val amount: Double,
    val billingDay: Int,
    val category: String,
    val isActive: Boolean = true
)

data class LoungeVisit(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,       // Rename cardId to accountId
    val loungeName: String,
    val airport: String,
    val date: Long,
    val guestsCount: Int
)

data class EmiPlan(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,       // Rename cardId to accountId
    val merchant: String,
    val totalAmount: Double,
    val monthlyInstallment: Double,
    val monthsDuration: Int,
    val startDate: Long,
    val isActive: Boolean = true
)

data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val category: String,       // Category name or "Total"
    val limitAmount: Double,
    val spentAmount: Double = 0.0,
    val period: String = "Monthly",
    val startDate: Long = System.currentTimeMillis(),
    val isRolloverEnabled: Boolean = false,
    val rolloverAmount: Double = 0.0
)

data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long
)

data class AppBackupData(
    val cards: List<CreditCard>? = emptyList(),
    val expenses: List<Expense>? = emptyList(),
    val accounts: List<Account>? = emptyList(),
    val transactions: List<Transaction>? = emptyList(),
    val payments: List<Payment>? = emptyList(),
    val subscriptions: List<Subscription>? = emptyList(),
    val loungeVisits: List<LoungeVisit>? = emptyList(),
    val emiPlans: List<EmiPlan>? = emptyList(),
    val budgets: List<Budget>? = emptyList(),
    val savingsGoals: List<SavingsGoal>? = emptyList()
)
