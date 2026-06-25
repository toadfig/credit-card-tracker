package com.example.creditcardtracker.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.creditcardtracker.data.Account
import com.example.creditcardtracker.data.AccountType
import com.example.creditcardtracker.data.DatabaseHelper
import com.example.creditcardtracker.data.Transaction
import com.example.creditcardtracker.data.TransactionType
import com.example.creditcardtracker.data.Payment
import com.example.creditcardtracker.data.Subscription
import com.example.creditcardtracker.data.LoungeVisit
import com.example.creditcardtracker.data.EmiPlan
import com.example.creditcardtracker.data.Budget
import com.example.creditcardtracker.data.SavingsGoal
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val sharedPrefs = application.getSharedPreferences("cctracker_prefs", Context.MODE_PRIVATE)

    val accounts = mutableStateListOf<Account>()
    val transactions = mutableStateListOf<Transaction>()
    val payments = mutableStateListOf<Payment>()
    val subscriptions = mutableStateListOf<Subscription>()
    val loungeVisits = mutableStateListOf<LoungeVisit>()
    val emiPlans = mutableStateListOf<EmiPlan>()
    val budgets = mutableStateListOf<Budget>()
    val savingsGoals = mutableStateListOf<SavingsGoal>()

    val isBiometricEnabled = mutableStateOf(sharedPrefs.getBoolean("biometric_enabled", false))
    val isDynamicColorEnabled = mutableStateOf(sharedPrefs.getBoolean("dynamic_color_enabled", true))
    val isUnlocked = mutableStateOf(!isBiometricEnabled.value)
    val selectedIndex = mutableStateOf(0)

    init {
        syncState()
        checkAndProcessBudgetRollover()
    }

    private fun syncState() {
        accounts.clear()
        accounts.addAll(dbHelper.accounts)
        transactions.clear()
        transactions.addAll(dbHelper.transactions)
        payments.clear()
        payments.addAll(dbHelper.payments)
        subscriptions.clear()
        subscriptions.addAll(dbHelper.subscriptions)
        loungeVisits.clear()
        loungeVisits.addAll(dbHelper.loungeVisits)
        emiPlans.clear()
        emiPlans.addAll(dbHelper.emiPlans)
        budgets.clear()
        budgets.addAll(dbHelper.budgets)
        savingsGoals.clear()
        savingsGoals.addAll(dbHelper.savingsGoals)
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

    fun addAccount(
        name: String,
        bank: String,
        accountType: AccountType,
        balance: Double,
        creditLimit: Double = 0.0,
        cardNumber: String = "",
        expiryDate: String = "",
        cvv: String = "",
        statementDay: Int = 1,
        dueDay: Int = 1,
        annualFee: Double = 0.0,
        isFeeRedeemable: Boolean = false,
        feeRedemptionLimit: Double = 0.0,
        feeRedemptionUnit: String = "Spend",
        accountColorIndex: Int = 0,
        isSmsTrackingEnabled: Boolean = false,
        smsSender: String = "",
        cardType: String? = "Visa",
        cardTier: String? = "Classic",
        annualLoungeQuota: Int = 0,
        cashbackRate: Double = 0.0,
        rewardPointsRate: Double = 0.0,
        bankHelpline: String = ""
    ) {
        val newAccount = Account(
            name = name,
            bank = bank,
            accountType = accountType,
            balance = balance,
            creditLimit = creditLimit,
            cardNumber = cardNumber,
            expiryDate = expiryDate,
            cvv = cvv,
            statementDay = statementDay,
            dueDay = dueDay,
            annualFee = annualFee,
            isFeeRedeemable = isFeeRedeemable,
            feeRedemptionLimit = feeRedemptionLimit,
            feeRedemptionUnit = feeRedemptionUnit,
            accountColorIndex = accountColorIndex,
            isSmsTrackingEnabled = isSmsTrackingEnabled,
            smsSender = smsSender,
            cardType = cardType,
            cardTier = cardTier,
            annualLoungeQuota = annualLoungeQuota,
            cashbackRate = cashbackRate,
            rewardPointsRate = rewardPointsRate,
            bankHelpline = bankHelpline
        )
        dbHelper.accounts.add(newAccount)
        dbHelper.saveData()
        syncState()
    }

    fun deleteAccount(accountId: String) {
        dbHelper.accounts.removeAll { it.id == accountId }
        dbHelper.transactions.removeAll { it.sourceAccountId == accountId || it.destinationAccountId == accountId }
        dbHelper.payments.removeAll { it.cardId == accountId }
        dbHelper.subscriptions.removeAll { it.accountId == accountId }
        dbHelper.loungeVisits.removeAll { it.accountId == accountId }
        dbHelper.emiPlans.removeAll { it.accountId == accountId }
        dbHelper.saveData()
        syncState()
        if (selectedIndex.value >= accounts.size && accounts.isNotEmpty()) {
            selectedIndex.value = accounts.size - 1
        }
    }

    fun updateAccountSettings(
        accountId: String,
        isSmsEnabled: Boolean,
        smsSender: String,
        loungeQuota: Int,
        cashbackRate: Double,
        rewardPointsRate: Double,
        helpline: String
    ) {
        val index = dbHelper.accounts.indexOfFirst { it.id == accountId }
        if (index != -1) {
            val oldAccount = dbHelper.accounts[index]
            dbHelper.accounts[index] = oldAccount.copy(
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

    fun addTransaction(
        type: TransactionType,
        sourceAccountId: String,
        destinationAccountId: String? = null,
        amount: Double,
        category: String,
        description: String,
        date: Long,
        currency: String = "BDT",
        exchangeRate: Double = 1.0
    ) {
        val newTx = Transaction(
            type = type,
            sourceAccountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            amount = amount,
            category = category,
            description = description,
            date = date,
            currency = currency,
            exchangeRate = exchangeRate
        )
        dbHelper.transactions.add(newTx)
        updateBalanceForTx(newTx, isAddition = true)
        dbHelper.saveData()
        syncState()
    }

    fun deleteTransaction(txId: String) {
        val tx = dbHelper.transactions.find { it.id == txId }
        if (tx != null) {
            updateBalanceForTx(tx, isAddition = false)
            dbHelper.transactions.remove(tx)
            dbHelper.saveData()
            syncState()
        }
    }

    private fun updateBalanceForTx(tx: Transaction, isAddition: Boolean) {
        val multiplier = if (isAddition) 1 else -1
        
        val srcIndex = dbHelper.accounts.indexOfFirst { it.id == tx.sourceAccountId }
        if (srcIndex != -1) {
            val account = dbHelper.accounts[srcIndex]
            val amountDiff = tx.amount * multiplier
            val newBalance = when (tx.type) {
                TransactionType.INCOME -> {
                    if (account.accountType == AccountType.CREDIT_CARD) {
                        account.balance - amountDiff
                    } else {
                        account.balance + amountDiff
                    }
                }
                TransactionType.EXPENSE -> {
                    if (account.accountType == AccountType.CREDIT_CARD) {
                        account.balance + amountDiff
                    } else {
                        account.balance - amountDiff
                    }
                }
                TransactionType.TRANSFER -> {
                    if (account.accountType == AccountType.CREDIT_CARD) {
                        account.balance + amountDiff
                    } else {
                        account.balance - amountDiff
                    }
                }
            }
            dbHelper.accounts[srcIndex] = account.copy(balance = newBalance)
        }
        
        if (tx.type == TransactionType.TRANSFER && tx.destinationAccountId != null) {
            val destIndex = dbHelper.accounts.indexOfFirst { it.id == tx.destinationAccountId }
            if (destIndex != -1) {
                val account = dbHelper.accounts[destIndex]
                val amountDiff = tx.amount * multiplier
                val newBalance = if (account.accountType == AccountType.CREDIT_CARD) {
                    account.balance - amountDiff
                } else {
                    account.balance + amountDiff
                }
                dbHelper.accounts[destIndex] = account.copy(balance = newBalance)
            }
        }
    }

    fun addPayment(cardId: String, amount: Double, date: Long, notes: String) {
        val newPayment = Payment(
            cardId = cardId,
            amount = amount,
            date = date,
            notes = notes
        )
        dbHelper.payments.add(newPayment)
        
        // Paying off credit card reduces outstanding balance
        val index = dbHelper.accounts.indexOfFirst { it.id == cardId }
        if (index != -1) {
            val account = dbHelper.accounts[index]
            dbHelper.accounts[index] = account.copy(balance = account.balance - amount)
        }
        
        dbHelper.saveData()
        syncState()
    }

    fun deletePayment(paymentId: String) {
        val payment = dbHelper.payments.find { it.id == paymentId }
        if (payment != null) {
            val index = dbHelper.accounts.indexOfFirst { it.id == payment.cardId }
            if (index != -1) {
                val account = dbHelper.accounts[index]
                dbHelper.accounts[index] = account.copy(balance = account.balance + payment.amount)
            }
            dbHelper.payments.remove(payment)
            dbHelper.saveData()
            syncState()
        }
    }

    fun addSubscription(accountId: String, name: String, amount: Double, billingDay: Int, category: String) {
        val newSub = Subscription(accountId = accountId, name = name, amount = amount, billingDay = billingDay, category = category)
        dbHelper.subscriptions.add(newSub)
        dbHelper.saveData()
        syncState()
    }

    fun deleteSubscription(id: String) {
        dbHelper.subscriptions.removeAll { it.id == id }
        dbHelper.saveData()
        syncState()
    }

    fun addLoungeVisit(accountId: String, loungeName: String, airport: String, date: Long, guestsCount: Int) {
        val newVisit = LoungeVisit(accountId = accountId, loungeName = loungeName, airport = airport, date = date, guestsCount = guestsCount)
        dbHelper.loungeVisits.add(newVisit)
        dbHelper.saveData()
        syncState()
    }

    fun deleteLoungeVisit(id: String) {
        dbHelper.loungeVisits.removeAll { it.id == id }
        dbHelper.saveData()
        syncState()
    }

    fun addEmiPlan(accountId: String, merchant: String, totalAmount: Double, monthlyInstallment: Double, monthsDuration: Int, startDate: Long) {
        val newEmi = EmiPlan(
            accountId = accountId,
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

    // Budgets CRUD
    fun addBudget(category: String, limitAmount: Double, isRolloverEnabled: Boolean = false) {
        val newBudget = Budget(category = category, limitAmount = limitAmount, isRolloverEnabled = isRolloverEnabled)
        dbHelper.budgets.add(newBudget)
        dbHelper.saveData()
        syncState()
    }

    fun deleteBudget(budgetId: String) {
        dbHelper.budgets.removeAll { it.id == budgetId }
        dbHelper.saveData()
        syncState()
    }

    fun toggleBudgetRollover(budgetId: String) {
        val index = dbHelper.budgets.indexOfFirst { it.id == budgetId }
        if (index != -1) {
            val budget = dbHelper.budgets[index]
            dbHelper.budgets[index] = budget.copy(isRolloverEnabled = !budget.isRolloverEnabled)
            dbHelper.saveData()
            syncState()
        }
    }

    // Savings Goals CRUD
    fun addSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: Long) {
        val newGoal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = currentAmount, targetDate = targetDate)
        dbHelper.savingsGoals.add(newGoal)
        dbHelper.saveData()
        syncState()
    }

    fun updateSavingsGoalProgress(goalId: String, currentAmount: Double) {
        val index = dbHelper.savingsGoals.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val goal = dbHelper.savingsGoals[index]
            dbHelper.savingsGoals[index] = goal.copy(currentAmount = currentAmount)
            dbHelper.saveData()
            syncState()
        }
    }

    fun deleteSavingsGoal(goalId: String) {
        dbHelper.savingsGoals.removeAll { it.id == goalId }
        dbHelper.saveData()
        syncState()
    }

    // Rollover check
    fun checkAndProcessBudgetRollover() {
        val now = System.currentTimeMillis()
        val currentMonthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var updated = false
        dbHelper.budgets.forEachIndexed { index, budget ->
            val budgetCalendar = Calendar.getInstance().apply { timeInMillis = budget.startDate }
            val currentCalendar = Calendar.getInstance().apply { timeInMillis = now }
            
            val isSameMonth = budgetCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    budgetCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
            
            if (!isSameMonth) {
                val startOfBudgetMonth = Calendar.getInstance().apply {
                    timeInMillis = budget.startDate
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val endOfBudgetMonth = Calendar.getInstance().apply {
                    timeInMillis = budget.startDate
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                val spentInBudgetMonth = dbHelper.transactions
                    .filter { it.type == TransactionType.EXPENSE && it.category == budget.category && it.date in startOfBudgetMonth..endOfBudgetMonth }
                    .sumOf { it.amount * it.exchangeRate }
                
                val leftover = budget.limitAmount + budget.rolloverAmount - spentInBudgetMonth
                val nextRollover = if (budget.isRolloverEnabled) leftover else 0.0
                
                dbHelper.budgets[index] = budget.copy(
                    rolloverAmount = nextRollover,
                    startDate = currentMonthStart,
                    spentAmount = 0.0
                )
                updated = true
            }
        }
        if (updated) {
            dbHelper.saveData()
            syncState()
        }
    }

    fun getSpentForCategoryThisMonth(category: String): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        return transactions
            .filter { it.type == TransactionType.EXPENSE && it.category == category && it.date >= startOfMonth }
            .sumOf { it.amount * it.exchangeRate }
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

    fun getActiveEmiInstallmentsForAccount(accountId: String): Double {
        val account = accounts.firstOrNull { it.id == accountId } ?: return 0.0
        val currentRange = getBillingCycleRange(account.statementDay)
        val cycleStart = currentRange.first

        return emiPlans.filter { it.accountId == accountId && it.isActive }.sumOf { emi ->
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

    fun getSpendInCurrentCycle(account: Account): Double {
        val range = getBillingCycleRange(account.statementDay)
        val standardSpend = transactions
            .filter { it.sourceAccountId == account.id && it.type == TransactionType.EXPENSE && it.date in range.first..range.second }
            .sumOf { it.amount * it.exchangeRate }
        val emiSpend = getActiveEmiInstallmentsForAccount(account.id)
        return standardSpend + emiSpend
    }

    fun getPaymentsInCurrentCycle(account: Account): Double {
        val range = getBillingCycleRange(account.statementDay)
        return payments
            .filter { it.cardId == account.id && it.date in range.first..range.second }
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
                // Accounts section
                writer.append("--- ACCOUNTS ---\n")
                writer.append("ID,Type,Bank,Name,Balance,Limit,CardType,CardTier,Helpline,LoungeQuota,CashbackRate,PointsRate\n")
                accounts.forEach { account ->
                    writer.append("${account.id},${account.accountType},${account.bank},${account.name},${account.balance},${account.creditLimit},${account.safeCardType},${account.safeCardTier},${account.bankHelpline},${account.annualLoungeQuota},${account.cashbackRate},${account.rewardPointsRate}\n")
                }
                writer.append("\n")

                // Transactions section
                writer.append("--- TRANSACTIONS ---\n")
                writer.append("ID,Type,SourceAccount,DestinationAccount,Amount,Currency,ExchangeRate,Category,Description,Date\n")
                transactions.forEach { tx ->
                    writer.append("${tx.id},${tx.type},${tx.sourceAccountId},${tx.destinationAccountId ?: ""},${tx.amount},${tx.currency},${tx.exchangeRate},${tx.category},${tx.description},${tx.date}\n")
                }
                writer.append("\n")

                // Payments section
                writer.append("--- PAYMENTS ---\n")
                writer.append("ID,AccountID,Amount,Date,Notes\n")
                payments.forEach { pay ->
                    writer.append("${pay.id},${pay.cardId},${pay.amount},${pay.date},${pay.notes}\n")
                }
                writer.append("\n")

                // Subscriptions section
                writer.append("--- SUBSCRIPTIONS ---\n")
                writer.append("ID,AccountID,Name,Amount,BillingDay,Category,Active\n")
                subscriptions.forEach { sub ->
                    writer.append("${sub.id},${sub.accountId},${sub.name},${sub.amount},${sub.billingDay},${sub.category},${sub.isActive}\n")
                }
                writer.append("\n")

                // Lounge Visits section
                writer.append("--- LOUNGE VISITS ---\n")
                writer.append("ID,AccountID,LoungeName,Airport,Date,Guests\n")
                loungeVisits.forEach { visit ->
                    writer.append("${visit.id},${visit.accountId},${visit.loungeName},${visit.airport},${visit.date},${visit.guestsCount}\n")
                }
                writer.append("\n")

                // EMI Plans section
                writer.append("--- EMI PLANS ---\n")
                writer.append("ID,AccountID,Merchant,TotalAmount,Installment,Months,StartDate,Active\n")
                emiPlans.forEach { emi ->
                    writer.append("${emi.id},${emi.accountId},${emi.merchant},${emi.totalAmount},${emi.monthlyInstallment},${emi.monthsDuration},${emi.startDate},${emi.isActive}\n")
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("TrackerViewModel", "Failed to export CSV: ${e.message}")
            return null
        }
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

    fun scanInboxForAccountSms(context: Context, account: Account): List<TempSmsExpense> {
        val list = mutableListOf<TempSmsExpense>()
        if (account.smsSender.isBlank()) return list

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

                val targetSenderNormalized = com.example.creditcardtracker.security.SmsParser.normalizeSender(account.smsSender)

                while (it.moveToNext()) {
                    val rawSender = it.getString(addressIdx) ?: ""
                    val body = it.getString(bodyIdx) ?: ""
                    val date = it.getLong(dateIdx)

                    val normalizedIncoming = com.example.creditcardtracker.security.SmsParser.normalizeSender(rawSender)

                    val senderMatch = normalizedIncoming.contains(targetSenderNormalized) || 
                                     targetSenderNormalized.contains(normalizedIncoming)
                    
                    val digitMatch = if (account.cardNumber.length >= 4) {
                        body.contains(account.cardNumber.takeLast(4))
                    } else false

                    if (senderMatch || digitMatch) {
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
