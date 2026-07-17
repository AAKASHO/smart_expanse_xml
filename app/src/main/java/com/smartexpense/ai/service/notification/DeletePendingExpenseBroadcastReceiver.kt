package com.smartexpense.ai.service.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.domain.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Receives the "Not a Transaction" action from the SMS transaction notification.
 * Deletes the pending expense and dismisses the notification.
 */
class DeletePendingExpenseBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DELETE_PENDING = "com.smartexpense.ai.ACTION_DELETE_PENDING"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DELETE_PENDING) return

        val expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (expenseId == -1L) return

        val app = context.applicationContext as SmartExpenseApp
        CoroutineScope(Dispatchers.IO).launch {
            // Load and delete the pending expense
            val expense = app.useCases.getExpenseById(expenseId).firstOrNull()
            if (expense != null && expense.isPending) {
                app.useCases.deleteExpense(expense)
            }
        }

        // Dismiss the notification immediately
        if (notificationId != -1) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notificationId)
        }
    }
}
