package com.smartexpense.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.smartexpense.ai.data.db.AppDatabase
import com.smartexpense.ai.data.repository.ExpenseRepository

class SmartExpenseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        ExpenseRepository(database.expenseDao(), database.budgetDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to log expenses"
        }

        val budgetChannel = NotificationChannel(
            CHANNEL_BUDGET,
            getString(R.string.notification_channel_budget),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Budget alerts when nearing limits"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(budgetChannel)
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_BUDGET = "budget_alerts"
    }
}
