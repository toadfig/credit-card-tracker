package com.example.creditcardtracker.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.creditcardtracker.data.DatabaseHelper
import com.example.creditcardtracker.data.AccountType
import com.example.creditcardtracker.data.Transaction
import com.example.creditcardtracker.data.TransactionType
import java.text.NumberFormat
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    private val channelId = "cctracker_transactions"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: ""
        val body = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }
        val timestamp = messages[0].timestampMillis

        Log.d("SmsReceiver", "Received SMS from: $sender")

        val dbHelper = DatabaseHelper(context)
        
        // Find matching accounts with SMS tracking enabled
        val incomingNormalized = SmsParser.normalizeSender(sender)
        val matchedAccounts = dbHelper.accounts.filter { account ->
            if (!account.isSmsTrackingEnabled) return@filter false
            
            val senderMatch = if (account.smsSender.isNotEmpty()) {
                val accountSenderNormalized = SmsParser.normalizeSender(account.smsSender)
                incomingNormalized.contains(accountSenderNormalized) || accountSenderNormalized.contains(incomingNormalized)
            } else false
            
            val bodyMatch = if (account.cardNumber.length >= 4) {
                body.contains(account.cardNumber.takeLast(4))
            } else false
            
            val mfsMatch = if (account.accountType == AccountType.MFS) {
                val bankName = account.bank.lowercase(Locale.US)
                val accountName = account.name.lowercase(Locale.US)
                (bankName.contains("bkash") && incomingNormalized.contains("BKASH")) ||
                (bankName.contains("nagad") && incomingNormalized.contains("NAGAD")) ||
                (accountName.contains("bkash") && incomingNormalized.contains("BKASH")) ||
                (accountName.contains("nagad") && incomingNormalized.contains("NAGAD"))
            } else false

            senderMatch || bodyMatch || mfsMatch
        }

        if (matchedAccounts.isEmpty()) {
            return
        }

        val amount = SmsParser.parseAmount(body)
        if (amount == null) {
            Log.d("SmsReceiver", "Could not parse amount from SMS: $body")
            return
        }

        val merchant = SmsParser.parseMerchant(body)
        val category = SmsParser.parseCategory(merchant, body)

        // Insert Transaction for each matching account
        for (account in matchedAccounts) {
            val bodyLower = body.lowercase(Locale.US)
            val transactionType = if (bodyLower.contains("received") || 
                                     bodyLower.contains("credited") || 
                                     bodyLower.contains("deposit") || 
                                     bodyLower.contains("cash in") ||
                                     bodyLower.contains("ref:")) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }

            val newTx = Transaction(
                type = transactionType,
                sourceAccountId = account.id,
                amount = amount,
                category = category,
                description = "Auto SMS: $merchant",
                date = timestamp
            )
            dbHelper.transactions.add(newTx)

            // Update balance dynamically on the account object
            val balanceDiff = if (transactionType == TransactionType.INCOME) amount else -amount
            val accountIndex = dbHelper.accounts.indexOfFirst { it.id == account.id }
            if (accountIndex != -1) {
                val targetAccount = dbHelper.accounts[accountIndex]
                val newBalance = if (targetAccount.accountType == AccountType.CREDIT_CARD) {
                    if (transactionType == TransactionType.INCOME) targetAccount.balance - amount else targetAccount.balance + amount
                } else {
                    targetAccount.balance + balanceDiff
                }
                dbHelper.accounts[accountIndex] = targetAccount.copy(balance = newBalance)
            }
        }

        dbHelper.saveData()
        
        // Notify user
        showNotification(context, amount, merchant, matchedAccounts[0].name)
    }

    private fun showNotification(context: Context, amount: Double, merchant: String, cardName: String) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val title = "Expense Logged Automatically"
        val text = "Detected ${currencyFormat.format(amount)} at $merchant on your $cardName card."

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Automatic Transaction Logs",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when transactions are parsed from SMS"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("SmsReceiver", "Post notification permission not granted: ${e.message}")
        }
    }
}
