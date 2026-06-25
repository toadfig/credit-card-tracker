package com.example.creditcardtracker.data

import android.content.Context
import android.util.Log
import com.example.creditcardtracker.security.CryptoManager
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DatabaseHelper(private val context: Context) {
    private val gson = Gson()
    private val dbFileName = "cctracker_db.json.enc"
    private val dbFile = File(context.filesDir, dbFileName)

    var accounts: MutableList<Account> = mutableListOf()
        private set
    var transactions: MutableList<Transaction> = mutableListOf()
        private set
    var payments: MutableList<Payment> = mutableListOf()
        private set
    var subscriptions: MutableList<Subscription> = mutableListOf()
        private set
    var loungeVisits: MutableList<LoungeVisit> = mutableListOf()
        private set
    var emiPlans: MutableList<EmiPlan> = mutableListOf()
        private set
    var budgets: MutableList<Budget> = mutableListOf()
        private set
    var savingsGoals: MutableList<SavingsGoal> = mutableListOf()
        private set

    init {
        loadData()
    }

    fun loadData() {
        if (!dbFile.exists()) {
            accounts = mutableListOf()
            transactions = mutableListOf()
            payments = mutableListOf()
            subscriptions = mutableListOf()
            loungeVisits = mutableListOf()
            emiPlans = mutableListOf()
            budgets = mutableListOf()
            savingsGoals = mutableListOf()
            return
        }
        try {
            val encryptedBytes = dbFile.readBytes()
            val decryptedBytes = CryptoManager.decryptLocal(encryptedBytes)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val data = gson.fromJson(jsonString, AppBackupData::class.java)
            
            val loadedAccounts = (data.accounts ?: emptyList()).toMutableList()
            val loadedTransactions = (data.transactions ?: emptyList()).toMutableList()
            
            // Backward-compatibility migrations
            if (loadedAccounts.isEmpty() && data.cards != null && data.cards.isNotEmpty()) {
                data.cards.forEach { card ->
                    loadedAccounts.add(
                        Account(
                            id = card.id,
                            name = card.name,
                            bank = card.bank,
                            accountType = AccountType.CREDIT_CARD,
                            balance = 0.0,
                            creditLimit = card.creditLimit,
                            cardNumber = card.cardNumber,
                            expiryDate = card.expiryDate,
                            cvv = card.cvv,
                            statementDay = card.statementDay,
                            dueDay = card.dueDay,
                            annualFee = card.annualFee,
                            isFeeRedeemable = card.isFeeRedeemable,
                            feeRedemptionLimit = card.feeRedemptionLimit,
                            feeRedemptionUnit = card.feeRedemptionUnit,
                            accountColorIndex = card.cardColorIndex,
                            isSmsTrackingEnabled = card.isSmsTrackingEnabled,
                            smsSender = card.smsSender,
                            cardType = card.cardType,
                            cardTier = card.cardTier,
                            annualLoungeQuota = card.annualLoungeQuota,
                            cashbackRate = card.cashbackRate,
                            rewardPointsRate = card.rewardPointsRate,
                            bankHelpline = card.bankHelpline
                        )
                    )
                }
            }
            
            if (loadedTransactions.isEmpty() && data.expenses != null && data.expenses.isNotEmpty()) {
                data.expenses.forEach { exp ->
                    loadedTransactions.add(
                        Transaction(
                            id = exp.id,
                            type = TransactionType.EXPENSE,
                            sourceAccountId = exp.cardId,
                            destinationAccountId = null,
                            amount = exp.amount,
                            category = exp.category,
                            description = exp.description,
                            date = exp.date,
                            currency = exp.currency,
                            exchangeRate = exp.exchangeRate
                        )
                    )
                }
            }

            accounts = loadedAccounts
            transactions = loadedTransactions
            payments = (data.payments ?: emptyList()).toMutableList()
            subscriptions = (data.subscriptions ?: emptyList()).toMutableList()
            loungeVisits = (data.loungeVisits ?: emptyList()).toMutableList()
            emiPlans = (data.emiPlans ?: emptyList()).toMutableList()
            budgets = (data.budgets ?: emptyList()).toMutableList()
            savingsGoals = (data.savingsGoals ?: emptyList()).toMutableList()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to load database: ${e.message}")
            accounts = mutableListOf()
            transactions = mutableListOf()
            payments = mutableListOf()
            subscriptions = mutableListOf()
            loungeVisits = mutableListOf()
            emiPlans = mutableListOf()
            budgets = mutableListOf()
            savingsGoals = mutableListOf()
        }
    }

    fun saveData() {
        try {
            val data = AppBackupData(
                cards = emptyList(),
                expenses = emptyList(),
                accounts = accounts,
                transactions = transactions,
                payments = payments,
                subscriptions = subscriptions,
                loungeVisits = loungeVisits,
                emiPlans = emiPlans,
                budgets = budgets,
                savingsGoals = savingsGoals
            )
            val jsonString = gson.toJson(data)
            val rawBytes = jsonString.toByteArray(Charsets.UTF_8)
            val encryptedBytes = CryptoManager.encryptLocal(rawBytes)
            dbFile.writeBytes(encryptedBytes)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to save database: ${e.message}")
        }
    }

    fun exportBackup(outputStream: FileOutputStream, password: CharArray): Boolean {
        return try {
            val data = AppBackupData(
                cards = emptyList(),
                expenses = emptyList(),
                accounts = accounts,
                transactions = transactions,
                payments = payments,
                subscriptions = subscriptions,
                loungeVisits = loungeVisits,
                emiPlans = emiPlans,
                budgets = budgets,
                savingsGoals = savingsGoals
            )
            val jsonString = gson.toJson(data)
            val rawBytes = jsonString.toByteArray(Charsets.UTF_8)
            val encryptedBytes = CryptoManager.encryptWithPassword(rawBytes, password)
            outputStream.write(encryptedBytes)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to export backup: ${e.message}")
            false
        }
    }

    fun importBackup(inputStream: FileInputStream, password: CharArray): Boolean {
        return try {
            val encryptedBytes = inputStream.readBytes()
            inputStream.close()
            val decryptedBytes = CryptoManager.decryptWithPassword(encryptedBytes, password)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val data = gson.fromJson(jsonString, AppBackupData::class.java)
            
            accounts = (data.accounts ?: emptyList()).toMutableList()
            transactions = (data.transactions ?: emptyList()).toMutableList()
            
            // Also fall back for imported backups of old versions
            if (accounts.isEmpty() && data.cards != null && data.cards.isNotEmpty()) {
                data.cards.forEach { card ->
                    accounts.add(
                        Account(
                            id = card.id,
                            name = card.name,
                            bank = card.bank,
                            accountType = AccountType.CREDIT_CARD,
                            balance = 0.0,
                            creditLimit = card.creditLimit,
                            cardNumber = card.cardNumber,
                            expiryDate = card.expiryDate,
                            cvv = card.cvv,
                            statementDay = card.statementDay,
                            dueDay = card.dueDay,
                            annualFee = card.annualFee,
                            isFeeRedeemable = card.isFeeRedeemable,
                            feeRedemptionLimit = card.feeRedemptionLimit,
                            feeRedemptionUnit = card.feeRedemptionUnit,
                            accountColorIndex = card.cardColorIndex,
                            isSmsTrackingEnabled = card.isSmsTrackingEnabled,
                            smsSender = card.smsSender,
                            cardType = card.cardType,
                            cardTier = card.cardTier,
                            annualLoungeQuota = card.annualLoungeQuota,
                            cashbackRate = card.cashbackRate,
                            rewardPointsRate = card.rewardPointsRate,
                            bankHelpline = card.bankHelpline
                        )
                    )
                }
            }
            if (transactions.isEmpty() && data.expenses != null && data.expenses.isNotEmpty()) {
                data.expenses.forEach { exp ->
                    transactions.add(
                        Transaction(
                            id = exp.id,
                            type = TransactionType.EXPENSE,
                            sourceAccountId = exp.cardId,
                            destinationAccountId = null,
                            amount = exp.amount,
                            category = exp.category,
                            description = exp.description,
                            date = exp.date,
                            currency = exp.currency,
                            exchangeRate = exp.exchangeRate
                        )
                    )
                }
            }

            payments = (data.payments ?: emptyList()).toMutableList()
            subscriptions = (data.subscriptions ?: emptyList()).toMutableList()
            loungeVisits = (data.loungeVisits ?: emptyList()).toMutableList()
            emiPlans = (data.emiPlans ?: emptyList()).toMutableList()
            budgets = (data.budgets ?: emptyList()).toMutableList()
            savingsGoals = (data.savingsGoals ?: emptyList()).toMutableList()
            saveData()
            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to import database: ${e.message}")
            false
        }
    }
}
