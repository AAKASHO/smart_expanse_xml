package com.smartexpense.ai.data.repository

import com.smartexpense.ai.data.db.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {

    // --- Expense operations ---

    suspend fun addExpense(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getRecentExpenses(limit: Int = 5): Flow<List<Expense>> = expenseDao.getRecentExpenses(limit)

    fun getByCategory(category: String): Flow<List<Expense>> = expenseDao.getByCategory(category)

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getByDateRange(startDate, endDate)

    fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.search(query)

    fun getExpenseCount(): Flow<Int> = expenseDao.getExpenseCount()

    // --- Monthly totals ---

    fun getMonthlySpending(month: Int, year: Int): Flow<Double?> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getTotalSpending(start, end)
    }

    fun getCurrentMonthSpending(): Flow<Double?> {
        val cal = Calendar.getInstance()
        return getMonthlySpending(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getCategoryTotals(start, end)
    }

    fun getCurrentMonthCategoryTotals(): Flow<List<CategoryTotal>> {
        val cal = Calendar.getInstance()
        return getCategoryTotals(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun getDailyTotals(month: Int, year: Int): Flow<List<DailyTotal>> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getDailyTotals(start, end)
    }

    fun getCurrentMonthDailyTotals(): Flow<List<DailyTotal>> {
        val cal = Calendar.getInstance()
        return getDailyTotals(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun getCurrentMonthExpenses(): Flow<List<Expense>> {
        val cal = Calendar.getInstance()
        val (start, end) = getMonthRange(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        return expenseDao.getByDateRange(start, end)
    }

    fun getPreviousMonthSpending(): Flow<Double?> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return getMonthlySpending(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    // --- Budget operations ---

    suspend fun setBudget(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun updateBudget(budget: Budget) = budgetDao.update(budget)

    fun getBudget(month: Int, year: Int): Flow<Budget?> = budgetDao.getBudget(month, year)

    fun getCurrentBudget(): Flow<Budget?> {
        val cal = Calendar.getInstance()
        return budgetDao.getBudget(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun getLatestBudget(): Flow<Budget?> = budgetDao.getLatestBudget()

    // --- Helpers ---

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }
}
