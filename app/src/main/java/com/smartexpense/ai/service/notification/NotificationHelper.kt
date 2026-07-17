package com.smartexpense.ai.service.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.smartexpense.ai.MainActivity
import com.smartexpense.ai.R
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.domain.usecase.ExpenseUseCases
import com.smartexpense.ai.service.transaction.VerifyTransactionActivity
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {

    fun scheduleDailyReminder() {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
    }

    /**
     * Shows a rich notification prompting the user to confirm an auto-parsed SMS transaction.
     * Action 1: Opens [VerifyTransactionActivity] to categorise and confirm.
     * Action 2: Fires [DeletePendingExpenseBroadcastReceiver] to silently delete the pending row.
     */
    fun showTransactionVerificationNotification(
        expenseId: Long,
        amount: Double,
        merchant: String
    ) {
        val notificationId = (expenseId % Int.MAX_VALUE).toInt() + 3000

        // Action 1: Yes, Categorize → open VerifyTransactionActivity
        val verifyIntent = Intent(context, VerifyTransactionActivity::class.java).apply {
            putExtra(VerifyTransactionActivity.EXTRA_EXPENSE_ID, expenseId)
            putExtra(VerifyTransactionActivity.EXTRA_NOTIFICATION_ID, notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val verifyPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            verifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Not a Transaction → delete pending row
        val deleteIntent = Intent(context, DeletePendingExpenseBroadcastReceiver::class.java).apply {
            action = DeletePendingExpenseBroadcastReceiver.ACTION_DELETE_PENDING
            putExtra(DeletePendingExpenseBroadcastReceiver.EXTRA_EXPENSE_ID, expenseId)
            putExtra(DeletePendingExpenseBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountFormatted = "₹%.2f".format(amount)

        val notification = NotificationCompat.Builder(context, SmartExpenseApp.CHANNEL_TRANSACTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.txn_notification_title))
            .setContentText(context.getString(R.string.txn_notification_body, amountFormatted, merchant))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.txn_notification_body, amountFormatted, merchant))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(verifyPendingIntent)
            .addAction(
                R.drawable.ic_add_circle,
                context.getString(R.string.action_yes_categorize),
                verifyPendingIntent
            )
            .addAction(
                R.drawable.ic_other,
                context.getString(R.string.action_not_transaction),
                deletePendingIntent
            )
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun showBudgetAlert(percentageUsed: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SmartExpenseApp.CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.budget_alert_title))
            .setContentText(context.getString(R.string.budget_alert_text, percentageUsed))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2001, notification)
    }

    suspend fun checkBudgetAndNotify(useCases: ExpenseUseCases) {
        val prefs = context.getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("budget_alerts_enabled", true)) return

        // Check Monthly Limit
        val currentMonthSpending = useCases.getCurrentMonthSpending().firstOrNull() ?: 0.0
        val currentBudget = useCases.getCurrentBudget().firstOrNull()?.monthlyLimit ?: 0.0

        if (currentBudget > 0) {
            val percentageUsed = ((currentMonthSpending / currentBudget) * 100).toInt()
            if (percentageUsed >= 80) {
                showBudgetAlert(percentageUsed)
            }
        }

        // Check Daily Limit
        val dailyLimit = prefs.getFloat("daily_budget_limit", -1f)
        if (dailyLimit > 0) {
            val currentDaySpending = useCases.getCurrentDaySpending().firstOrNull() ?: 0.0
            if (currentDaySpending >= dailyLimit) {
                showDailyBudgetAlert(dailyLimit.toInt())
            }
        }
    }

    private fun showDailyBudgetAlert(limit: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SmartExpenseApp.CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.daily_budget_alert_title))
            .setContentText(context.getString(R.string.daily_budget_alert_text, limit))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2002, notification)
    }
}

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            SmartExpenseApp.CHANNEL_REMINDERS
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(applicationContext.getString(R.string.reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)

        return Result.success()
    }
}

