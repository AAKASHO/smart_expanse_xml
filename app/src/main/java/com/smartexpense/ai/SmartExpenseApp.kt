package com.smartexpense.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.smartexpense.ai.data.db.AppDatabase
import com.smartexpense.ai.data.repository.ExpenseRepositoryImpl
import com.smartexpense.ai.domain.usecase.*

class SmartExpenseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    
    val repository by lazy {
        ExpenseRepositoryImpl(database.expenseDao(), database.budgetDao())
    }

    val useCases by lazy {
        ExpenseUseCases(
            addExpense = AddExpenseUseCase(repository),
            updateExpense = UpdateExpenseUseCase(repository),
            deleteExpense = DeleteExpenseUseCase(repository),
            getAllExpenses = GetAllExpensesUseCase(repository),
            getRecentExpenses = GetRecentExpensesUseCase(repository),
            getExpensesByCategory = GetExpensesByCategoryUseCase(repository),
            searchExpenses = SearchExpensesUseCase(repository),
            getCurrentMonthSpending = GetCurrentMonthSpendingUseCase(repository),
            getCurrentBudget = GetCurrentBudgetUseCase(repository),
            getLatestBudget = GetLatestBudgetUseCase(repository),
            getCurrentMonthCategoryTotals = GetCurrentMonthCategoryTotalsUseCase(repository),
            getPreviousMonthSpending = GetPreviousMonthSpendingUseCase(repository),
            getCurrentMonthExpenses = GetCurrentMonthExpensesUseCase(repository),
            getExpenseById = GetExpenseByIdUseCase(repository),
            getWeeklySpending = GetWeeklySpendingUseCase(repository),
            getWeeklyCategoryTotals = GetWeeklyCategoryTotalsUseCase(repository),
            getWeeklyDailyTotals = GetWeeklyDailyTotalsUseCase(repository),
            getWeeklyExpenses = GetWeeklyExpensesUseCase(repository),
            getCurrentMonthDailyTotals = GetCurrentMonthDailyTotalsUseCase(repository),
            getCurrentDaySpending = GetCurrentDaySpendingUseCase(repository),
            setBudget = SetBudgetUseCase(repository),
            updateBudget = UpdateBudgetUseCase(repository)
        )
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
