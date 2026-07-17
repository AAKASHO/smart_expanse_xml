package com.smartexpense.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode

import com.smartexpense.ai.BuildConfig
import com.smartexpense.ai.data.db.AppDatabase
import com.smartexpense.ai.data.preferences.UserPreferencesRepository
import com.smartexpense.ai.data.repository.ExpenseRepositoryImpl
import com.smartexpense.ai.domain.usecase.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class SmartExpenseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val userPreferences by lazy { UserPreferencesRepository(this) }

    val repository by lazy {
        ExpenseRepositoryImpl(
            database.expenseDao(),
            database.budgetDao(),
            database.customCategoryDao()
        )
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
            updateBudget = UpdateBudgetUseCase(repository),
            getCustomCategories = GetCustomCategoriesUseCase(repository),
            addCustomCategory = AddCustomCategoryUseCase(repository),
            deleteCustomCategory = DeleteCustomCategoryUseCase(repository)
        )
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            setupStrictMode()
        }
        createNotificationChannels()
    }

    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()          // catch disk reads on main thread
                .detectDiskWrites()         // catch disk writes on main thread
                .detectNetwork()            // catch network calls on main thread
                .detectCustomSlowCalls()    // catch slow code tagged with StrictMode.noteSlowCall()
                .penaltyLog()               // log violations to Logcat
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()       // catch unclosed SQLite cursors
                .detectLeakedClosableObjects()      // catch unclosed streams, cursors, etc.
                .detectActivityLeaks()              // catch leaked Activity instances
                .penaltyLog()
                .build()
        )

    }

    private fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to log expenses"
        }

        val ch = Channel<Int>()
        val budgetChannel = NotificationChannel(
            CHANNEL_BUDGET,
            getString(R.string.notification_channel_budget),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Budget alerts when nearing limits"
        }

        flow {
            emit(true)
            delay(200)
        }

        val transactionChannel = NotificationChannel(
            CHANNEL_TRANSACTIONS,
            getString(R.string.notification_channel_transactions),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Prompts to verify auto-detected SMS transactions"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(budgetChannel)
        notificationManager.createNotificationChannel(transactionChannel)
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_BUDGET = "budget_alerts"
        const val CHANNEL_TRANSACTIONS = "transactions"
    }
}
