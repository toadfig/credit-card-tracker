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
import com.example.creditcardtracker.data.Expense
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
        
        // Find matching cards with SMS tracking enabled
        val matchedCards = dbHelper.cards.filter {
            it.isSmsTrackingEnabled && it.smsSender.equals(sender, ignoreCase = true)
        }

        if (matchedCards.isEmpty()) {
            return
        }

        val amount = SmsParser.parseAmount(body)
        if (amount == null) {
            Log.d("SmsReceiver", "Could not parse amount from SMS: $body")
            return
        }

        val merchant = SmsParser.parseMerchant(body)
        val category = SmsParser.parseCategory(merchant, body)

        // Insert Expense for each matching card
        for (card in matchedCards) {
            val newExpense = Expense(
                cardId = card.id,
                amount = amount,
                category = category,
                description = "Auto SMS: $merchant",
                date = timestamp
            )
            dbHelper.expenses.add(newExpense)
        }

        dbHelper.saveData()
        
        // Notify user
        showNotification(context, amount, merchant, matchedCards[0].name)
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
