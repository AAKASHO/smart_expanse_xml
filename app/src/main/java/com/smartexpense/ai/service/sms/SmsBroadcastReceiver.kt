package com.smartexpense.ai.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.db.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sms_parsing_enabled", false)) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullMessage = messages.joinToString("") { it.messageBody }
        val sender = messages.firstOrNull()?.originatingAddress ?: ""

        // Only process bank/UPI messages
        if (!isBankMessage(sender, fullMessage)) return

        val parsedExpense = parseSmsToExpense(fullMessage) ?: return

        val app = context.applicationContext as SmartExpenseApp
        CoroutineScope(Dispatchers.IO).launch {
            app.repository.addExpense(parsedExpense)
        }
    }

    private fun isBankMessage(sender: String, body: String): Boolean {
        val bankSenders = listOf(
            "SBI", "HDFC", "ICICI", "AXIS", "KOTAK", "PNB", "BOB", "CANARA",
            "PAYTM", "GPAY", "PHONEPE", "AMAZON", "UPIID"
        )
        val bankKeywords = listOf(
            "debited", "credited", "spent", "paid", "received",
            "transaction", "txn", "a/c", "account", "upi", "neft", "imps"
        )

        val senderUpper = sender.uppercase()
        val bodyLower = body.lowercase()

        return bankSenders.any { senderUpper.contains(it) } ||
                bankKeywords.any { bodyLower.contains(it) }
    }

    fun parseSmsToExpense(message: String): Expense? {
        val amount = extractAmount(message) ?: return null
        val merchant = extractMerchant(message)
        val category = guessCategory(merchant, message)
        val paymentMethod = guessPaymentMethod(message)

        return Expense(
            amount = amount,
            category = category,
            merchant = merchant,
            note = "Auto-parsed from SMS",
            date = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            isAutoSynced = true
        )
    }

    private fun extractAmount(message: String): Double? {
        // Match patterns like "Rs.500", "Rs 500", "INR 500", "₹500", "Rs.1,200.50"
        val patterns = listOf(
            Pattern.compile("(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.\\d{1,2}|[\\d,]+)"),
            Pattern.compile("(?i)(?:debited|spent|paid|amount)\\s*(?:of)?\\s*(?:rs\\.?|inr|₹)?\\s*([\\d,]+\\.\\d{1,2}|[\\d,]+)"),
            Pattern.compile("([\\d,]+\\.\\d{1,2}|[\\d,]+)\\s*(?:debited|credited)", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                return amountStr.toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractMerchant(message: String): String {
        // Try to extract merchant name from common patterns
        val patterns = listOf(
            Pattern.compile("(?:at|to|from|merchant|paid to)\\s+([A-Za-z][A-Za-z0-9\\s]{2,20})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:VPA|UPI)\\s*[:\\-]?\\s*([A-Za-z][A-Za-z0-9@.\\-]{2,30})", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)?.trim()?.take(30) ?: "Unknown"
            }
        }
        return "Unknown Merchant"
    }

    private fun guessCategory(merchant: String, message: String): String {
        val text = "$merchant $message".lowercase()
        return when {
            text.containsAny("zomato", "swiggy", "food", "restaurant", "cafe", "pizza", "burger", "kitchen", "lunch", "dinner", "breakfast") -> "Food"
            text.containsAny("uber", "ola", "taxi", "cab", "petrol", "fuel", "travel", "flight", "train", "bus", "metro") -> "Travel"
            text.containsAny("electricity", "water", "gas", "bill", "recharge", "airtel", "jio", "vodafone", "broadband", "rent") -> "Bills"
            text.containsAny("amazon", "flipkart", "myntra", "shopping", "mall", "store", "shop", "purchase") -> "Shopping"
            else -> "Other"
        }
    }

    private fun guessPaymentMethod(message: String): String {
        val text = message.lowercase()
        return when {
            text.contains("upi") || text.contains("vpa") -> "UPI"
            text.contains("card") || text.contains("credit") || text.contains("debit") -> "Card"
            text.contains("cash") -> "Cash"
            else -> "UPI"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
