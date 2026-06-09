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
import com.example.creditcardtracker.data.Subscription
import com.example.creditcardtracker.data.LoungeVisit
import com.example.creditcardtracker.data.EmiPlan
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val sharedPrefs = application.getSharedPreferences("cctracker_prefs", Context.MODE_PRIVATE)

    val cards = mutableStateListOf<CreditCard>()
    val expenses = mutableStateListOf<Expense>()
    val payments = mutableStateListOf<Payment>()
    val subscriptions = mutableStateListOf<Subscription>()
    val loungeVisits = mutableStateListOf<LoungeVisit>()
    val emiPlans = mutableStateListOf<EmiPlan>()

    val isBiometricEnabled = mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
    val isDynamicColorEnabled = mutableStateOf(sharedPrefs.getBoolean("dynamic_color_enabled", true))
    val isUnlocked = mutableStateOf(!isBiometricEnabled.value)
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
        subscriptions.clear()
        subscriptions.addAll(dbHelper.subscriptions)
        loungeVisits.clear()
        loungeVisits.addAll(dbHelper.loungeVisits)
        emiPlans.clear()
        emiPlans.addAll(dbHelper.emiPlans)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        isBiometricEnabled.value = enabled
        if (!enabled) {
            isUnlocked.value = true
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("dynamic_color_enabled", enabled).apply()
        isDynamicColorEnabled.value = enabled
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
        cardColorIndex: Int,
        isSmsTrackingEnabled: Boolean = false,
        smsSender: String = "",
        cardType: String = "Visa",
        cardTier: String = "Classic",
        annualLoungeQuota: Int = 0,
        cashbackRate: Double = 0.0,
        rewardPointsRate: Double = 0.0,
        bankHelpline: String = ""
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
            cardColorIndex = cardColorIndex,
            isSmsTrackingEnabled = isSmsTrackingEnabled,
            smsSender = smsSender,
            cardType = cardType,
            cardTier = cardTier,
            annualLoungeQuota = annualLoungeQuota,
            cashbackRate = cashbackRate,
            rewardPointsRate = rewardPointsRate,
            bankHelpline = bankHelpline
        )
        dbHelper.cards.add(newCard)
        dbHelper.saveData()
        syncState()
    }

    fun deleteCard(cardId: String) {
        dbHelper.cards.removeAll { it.id == cardId }
        dbHelper.expenses.removeAll { it.cardId == cardId }
        dbHelper.payments.removeAll { it.cardId == cardId }
        dbHelper.subscriptions.removeAll { it.cardId == cardId }
        dbHelper.loungeVisits.removeAll { it.cardId == cardId }
        dbHelper.emiPlans.removeAll { it.cardId == cardId }
        dbHelper.saveData()
        syncState()
        if (selectedIndex.value >= cards.size && cards.isNotEmpty()) {
            selectedIndex.value = cards.size - 1
        }
    }

    fun updateCardSettings(
        cardId: String,
        isSmsEnabled: Boolean,
        smsSender: String,
        loungeQuota: Int,
        cashbackRate: Double,
        rewardPointsRate: Double,
        helpline: String
    ) {
        val index = dbHelper.cards.indexOfFirst { it.id == cardId }
        if (index != -1) {
            val oldCard = dbHelper.cards[index]
            dbHelper.cards[index] = oldCard.copy(
                isSmsTrackingEnabled = isSmsEnabled,
                smsSender = smsSender,
                annualLoungeQuota = loungeQuota,
                cashbackRate = cashbackRate,
                rewardPointsRate = rewardPointsRate,
                bankHelpline = helpline
            )
            dbHelper.saveData()
            syncState()
        }
    }

    fun addExpense(
        cardId: String,
        amount: Double,
        category: String,
        description: String,
        date: Long,
        currency: String = "BDT",
        exchangeRate: Double = 1.0
    ) {
        val newExpense = Expense(
            cardId = cardId,
            amount = amount,
            category = category,
            description = description,
            date = date,
            currency = currency,
            exchangeRate = exchangeRate
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

    fun addSubscription(cardId: String, name: String, amount: Double, billingDay: Int, category: String) {
        val newSub = Subscription(cardId = cardId, name = name, amount = amount, billingDay = billingDay, category = category)
        dbHelper.subscriptions.add(newSub)
        dbHelper.saveData()
        syncState()
    }

    fun deleteSubscription(id: String) {
        dbHelper.subscriptions.removeAll { it.id == id }
        dbHelper.saveData()
        syncState()
    }

    fun addLoungeVisit(cardId: String, loungeName: String, airport: String, date: Long, guestsCount: Int) {
        val newVisit = LoungeVisit(cardId = cardId, loungeName = loungeName, airport = airport, date = date, guestsCount = guestsCount)
        dbHelper.loungeVisits.add(newVisit)
        dbHelper.saveData()
        syncState()
    }

    fun deleteLoungeVisit(id: String) {
        dbHelper.loungeVisits.removeAll { it.id == id }
        dbHelper.saveData()
        syncState()
    }

    fun addEmiPlan(cardId: String, merchant: String, totalAmount: Double, monthlyInstallment: Double, monthsDuration: Int, startDate: Long) {
        val newEmi = EmiPlan(
            cardId = cardId,
            merchant = merchant,
            totalAmount = totalAmount,
            monthlyInstallment = monthlyInstallment,
            monthsDuration = monthsDuration,
            startDate = startDate
        )
        dbHelper.emiPlans.add(newEmi)
        dbHelper.saveData()
        syncState()
    }

    fun deleteEmiPlan(id: String) {
        dbHelper.emiPlans.removeAll { it.id == id }
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

    fun getActiveEmiInstallmentsForCard(cardId: String): Double {
        val currentRange = getBillingCycleRange(cards.firstOrNull { it.id == cardId }?.statementDay ?: 1)
        val cycleStart = currentRange.first

        return emiPlans.filter { it.cardId == cardId && it.isActive }.sumOf { emi ->
            val emiStartCal = Calendar.getInstance().apply { timeInMillis = emi.startDate }
            val cycleStartCal = Calendar.getInstance().apply { timeInMillis = cycleStart }

            val yearsDiff = cycleStartCal.get(Calendar.YEAR) - emiStartCal.get(Calendar.YEAR)
            val monthsDiff = cycleStartCal.get(Calendar.MONTH) - emiStartCal.get(Calendar.MONTH) + (yearsDiff * 12)

            if (monthsDiff in 0 until emi.monthsDuration) {
                emi.monthlyInstallment
            } else {
                0.0
            }
        }
    }

    fun getSpendInCurrentCycle(card: CreditCard): Double {
        val range = getBillingCycleRange(card.statementDay)
        val standardSpend = expenses
            .filter { it.cardId == card.id && it.date in range.first..range.second }
            .sumOf { it.amount * it.exchangeRate }
        val emiSpend = getActiveEmiInstallmentsForCard(card.id)
        return standardSpend + emiSpend
    }

    fun getPaymentsInCurrentCycle(card: CreditCard): Double {
        val range = getBillingCycleRange(card.statementDay)
        return payments
            .filter { it.cardId == card.id && it.date in range.first..range.second }
            .sumOf { it.amount }
    }

    fun exportToCsv(context: Context): String? {
        val filename = "cctracker_export_${System.currentTimeMillis()}.csv"
        try {
            val directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = java.io.File(directory, filename)

            java.io.FileWriter(file).use { writer ->
                // Cards section
                writer.append("--- CARDS ---\n")
                writer.append("ID,Bank,Name,Limit,CardType,CardTier,Helpline,LoungeQuota,CashbackRate,PointsRate\n")
                cards.forEach { card ->
                    writer.append("${card.id},${card.bank},${card.name},${card.creditLimit},${card.safeCardType},${card.safeCardTier},${card.bankHelpline},${card.annualLoungeQuota},${card.cashbackRate},${card.rewardPointsRate}\n")
                }
                writer.append("\n")

                // Expenses section
                writer.append("--- EXPENSES ---\n")
                writer.append("ID,CardID,Amount,Currency,ExchangeRate,Category,Description,Date\n")
                expenses.forEach { exp ->
                    writer.append("${exp.id},${exp.cardId},${exp.amount},${exp.currency},${exp.exchangeRate},${exp.category},${exp.description},${exp.date}\n")
                }
                writer.append("\n")

                // Payments section
                writer.append("--- PAYMENTS ---\n")
                writer.append("ID,CardID,Amount,Date,Notes\n")
                payments.forEach { pay ->
                    writer.append("${pay.id},${pay.cardId},${pay.amount},${pay.date},${pay.notes}\n")
                }
                writer.append("\n")

                // Subscriptions section
                writer.append("--- SUBSCRIPTIONS ---\n")
                writer.append("ID,CardID,Name,Amount,BillingDay,Category,Active\n")
                subscriptions.forEach { sub ->
                    writer.append("${sub.id},${sub.cardId},${sub.name},${sub.amount},${sub.billingDay},${sub.category},${sub.isActive}\n")
                }
                writer.append("\n")

                // Lounge Visits section
                writer.append("--- LOUNGE VISITS ---\n")
                writer.append("ID,CardID,LoungeName,Airport,Date,Guests\n")
                loungeVisits.forEach { visit ->
                    writer.append("${visit.id},${visit.cardId},${visit.loungeName},${visit.airport},${visit.date},${visit.guestsCount}\n")
                }
                writer.append("\n")

                // EMI Plans section
                writer.append("--- EMI PLANS ---\n")
                writer.append("ID,CardID,Merchant,TotalAmount,Installment,Months,StartDate,Active\n")
                emiPlans.forEach { emi ->
                    writer.append("${emi.id},${emi.cardId},${emi.merchant},${emi.totalAmount},${emi.monthlyInstallment},${emi.monthsDuration},${emi.startDate},${emi.isActive}\n")
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("TrackerViewModel", "Failed to export CSV: ${e.message}")
            return null
        }
    }

    fun getRecentSmsSenders(context: Context): List<String> {
        val senders = mutableSetOf<String>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        try {
            val uri = android.net.Uri.parse("content://sms/inbox")
            val projection = arrayOf("address")
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")
            cursor?.use {
                val addressIndex = it.getColumnIndexOrThrow("address")
                var count = 0
                while (it.moveToNext() && count < 150) {
                    val address = it.getString(addressIndex)
                    if (!address.isNullOrBlank()) {
                        senders.add(address)
                    }
                    count++
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TrackerViewModel", "Failed to query SMS: ${e.message}")
        }
        return senders.toList().sorted()
    }

    fun scanInboxForCardSms(context: Context, card: CreditCard): List<TempSmsExpense> {
        val list = mutableListOf<TempSmsExpense>()
        if (card.smsSender.isBlank()) return list

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return list
        }

        try {
            val uri = android.net.Uri.parse("content://sms/inbox")
            val cursor = context.contentResolver.query(
                uri,
                arrayOf("address", "body", "date"),
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndexOrThrow("address")
                val bodyIdx = it.getColumnIndexOrThrow("body")
                val dateIdx = it.getColumnIndexOrThrow("date")

                val targetSenderNormalized = com.example.creditcardtracker.security.SmsParser.normalizeSender(card.smsSender)

                while (it.moveToNext()) {
                    val rawSender = it.getString(addressIdx) ?: ""
                    val body = it.getString(bodyIdx) ?: ""
                    val date = it.getLong(dateIdx)

                    val normalizedIncoming = com.example.creditcardtracker.security.SmsParser.normalizeSender(rawSender)

                    if (normalizedIncoming.contains(targetSenderNormalized) || 
                        targetSenderNormalized.contains(normalizedIncoming)) {
                        val amount = com.example.creditcardtracker.security.SmsParser.parseAmount(body)
                        if (amount != null && amount > 0.0) {
                            val merchant = com.example.creditcardtracker.security.SmsParser.parseMerchant(body)
                            val category = com.example.creditcardtracker.security.SmsParser.parseCategory(merchant, body)
                            list.add(
                                TempSmsExpense(
                                    amount = amount,
                                    merchant = merchant,
                                    category = category,
                                    date = date,
                                    body = body
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TrackerViewModel", "Failed to scan SMS inbox: ${e.message}")
        }
        return list
    }
}

data class TempSmsExpense(
    val amount: Double,
    val merchant: String,
    val category: String,
    val date: Long,
    val body: String
)
