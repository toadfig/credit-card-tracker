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

    var cards: MutableList<CreditCard> = mutableListOf()
        private set
    var expenses: MutableList<Expense> = mutableListOf()
        private set
    var payments: MutableList<Payment> = mutableListOf()
        private set

    init {
        loadData()
    }

    fun loadData() {
        if (!dbFile.exists()) {
            cards = mutableListOf()
            expenses = mutableListOf()
            payments = mutableListOf()
            return
        }
        try {
            val encryptedBytes = dbFile.readBytes()
            val decryptedBytes = CryptoManager.decryptLocal(encryptedBytes)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val data = gson.fromJson(jsonString, AppBackupData::class.java)
            cards = data.cards?.toMutableList() ?: mutableListOf()
            expenses = data.expenses?.toMutableList() ?: mutableListOf()
            payments = data.payments?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to load database: ${e.message}")
            cards = mutableListOf()
            expenses = mutableListOf()
            payments = mutableListOf()
        }
    }

    fun saveData() {
        try {
            val data = AppBackupData(cards, expenses, payments)
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
            val data = AppBackupData(cards, expenses, payments)
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
            
            cards = data.cards?.toMutableList() ?: mutableListOf()
            expenses = data.expenses?.toMutableList() ?: mutableListOf()
            payments = data.payments?.toMutableList() ?: mutableListOf()
            saveData()
            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Failed to import backup: ${e.message}")
            false
        }
    }
}
