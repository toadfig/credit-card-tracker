package com.example.creditcardtracker.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.creditcardtracker.data.CreditCard
import com.example.creditcardtracker.data.DatabaseHelper
import com.example.creditcardtracker.data.Expense
import com.example.creditcardtracker.data.Payment
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val sharedPrefs = application.getSharedPreferences("cctracker_prefs", Context.MODE_PRIVATE)

    val cards = mutableStateListOf<CreditCard>()
    val expenses = mutableStateListOf<Expense>()
    val payments = mutableStateListOf<Payment>()

    val isBiometricEnabled = mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
    val selectedIndex = mutableStateOf(0)

    init {
        syncState()
    }

    private fun syncState() {
        cards.clear()
        cards.addAll(dbHelper.cards)
        expenses.clear()
        expenses.addAll(dbHelper.expenses)
        payments.clear()
        payments.addAll(dbHelper.payments)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        isBiometricEnabled.value = enabled
    }

    fun addCard(
        name: String,
        bank: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        creditLimit: Double,
        statementDay: Int,
        dueDay: Int,
        annualFee: Double,
        isFeeRedeemable: Boolean,
        feeRedemptionLimit: Double,
        feeRedemptionUnit: String,
        cardColorIndex: Int
    ) {
        val newCard = CreditCard(
            name = name,
            bank = bank,
            cardNumber = cardNumber,
            expiryDate = expiryDate,
            cvv = cvv,
            creditLimit = creditLimit,
            statementDay = statementDay,
            dueDay = dueDay,
            annualFee = annualFee,
            isFeeRedeemable = isFeeRedeemable,
            feeRedemptionLimit = feeRedemptionLimit,
            feeRedemptionUnit = feeRedemptionUnit,
            cardColorIndex = cardColorIndex
        )
        dbHelper.cards.add(newCard)
        dbHelper.saveData()
        syncState()
    }

    fun deleteCard(cardId: String) {
        dbHelper.cards.removeAll { it.id == cardId }
        dbHelper.expenses.removeAll { it.cardId == cardId }
        dbHelper.payments.removeAll { it.cardId == cardId }
        dbHelper.saveData()
        syncState()
        if (selectedIndex.value >= cards.size && cards.isNotEmpty()) {
            selectedIndex.value = cards.size - 1
        }
    }

    fun addExpense(cardId: String, amount: Double, category: String, description: String, date: Long) {
        val newExpense = Expense(
            cardId = cardId,
            amount = amount,
            category = category,
            description = description,
            date = date
        )
        dbHelper.expenses.add(newExpense)
        dbHelper.saveData()
        syncState()
    }

    fun deleteExpense(expenseId: String) {
        dbHelper.expenses.removeAll { it.id == expenseId }
        dbHelper.saveData()
        syncState()
    }

    fun addPayment(cardId: String, amount: Double, date: Long, notes: String) {
        val newPayment = Payment(
            cardId = cardId,
            amount = amount,
            date = date,
            notes = notes
        )
        dbHelper.payments.add(newPayment)
        dbHelper.saveData()
        syncState()
    }

    fun deletePayment(paymentId: String) {
        dbHelper.payments.removeAll { it.id == paymentId }
        dbHelper.saveData()
        syncState()
    }

    fun exportData(outputStream: FileOutputStream, password: CharArray): Boolean {
        return dbHelper.exportBackup(outputStream, password)
    }

    fun importData(inputStream: FileInputStream, password: CharArray): Boolean {
        val success = dbHelper.importBackup(inputStream, password)
        if (success) {
            syncState()
        }
        return success
    }

    // Calculations
    fun getBillingCycleRange(statementDay: Int, targetTimeMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = targetTimeMillis }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = targetTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = targetTimeMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        if (currentDay >= statementDay) {
            startCalendar.set(Calendar.DAY_OF_MONTH, statementDay)
            endCalendar.add(Calendar.MONTH, 1)
            endCalendar.set(Calendar.DAY_OF_MONTH, statementDay)
            endCalendar.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            startCalendar.add(Calendar.MONTH, -1)
            startCalendar.set(Calendar.DAY_OF_MONTH, statementDay)
            endCalendar.set(Calendar.DAY_OF_MONTH, statementDay)
            endCalendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    fun getDueDateForCycle(statementDay: Int, dueDay: Int, targetTimeMillis: Long = System.currentTimeMillis()): Long {
        val range = getBillingCycleRange(statementDay, targetTimeMillis)
        val cycleEndMillis = range.second
        
        val dueCalendar = Calendar.getInstance().apply {
            timeInMillis = cycleEndMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        
        if (dueDay < statementDay) {
            dueCalendar.add(Calendar.MONTH, 1)
        }
        dueCalendar.set(Calendar.DAY_OF_MONTH, dueDay)
        return dueCalendar.timeInMillis
    }

    fun getSpendInCurrentCycle(card: CreditCard): Double {
        val range = getBillingCycleRange(card.statementDay)
        return expenses
            .filter { it.cardId == card.id && it.date in range.first..range.second }
            .sumOf { it.amount }
    }

    fun getPaymentsInCurrentCycle(card: CreditCard): Double {
        val range = getBillingCycleRange(card.statementDay)
        return payments
            .filter { it.cardId == card.id && it.date in range.first..range.second }
            .sumOf { it.amount }
    }
}
