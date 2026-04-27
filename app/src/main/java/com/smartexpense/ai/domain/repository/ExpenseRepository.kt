package com.smartexpense.ai.domain.repository

import com.smartexpense.ai.domain.model.Budget
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.DailyTotal
import com.smartexpense.ai.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    // --- Expense operations ---
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getRecentExpenses(limit: Int = 5): Flow<List<Expense>>
    fun getByCategory(category: String): Flow<List<Expense>>
    fun getById(id: Long): Flow<Expense?>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    fun searchExpenses(query: String): Flow<List<Expense>>
    fun getExpenseCount(): Flow<Int>

    // --- Monthly totals ---
    fun getMonthlySpending(month: Int, year: Int): Flow<Double?>
    fun getCurrentMonthSpending(): Flow<Double?>
    fun getCurrentDaySpending(): Flow<Double?>
    fun getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>>
    fun getCurrentMonthCategoryTotals(): Flow<List<CategoryTotal>>
    fun getDailyTotals(month: Int, year: Int): Flow<List<DailyTotal>>
    fun getCurrentMonthDailyTotals(): Flow<List<DailyTotal>>
    fun getCurrentMonthExpenses(): Flow<List<Expense>>
    fun getPreviousMonthSpending(): Flow<Double?>

    // --- Weekly totals ---
    fun getWeeklySpending(): Flow<Double?>
    fun getWeeklyCategoryTotals(): Flow<List<CategoryTotal>>
    fun getWeeklyDailyTotals(): Flow<List<DailyTotal>>
    fun getWeeklyExpenses(): Flow<List<Expense>>

    // --- Budget operations ---
    suspend fun setBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    fun getBudget(month: Int, year: Int): Flow<Budget?>
    fun getCurrentBudget(): Flow<Budget?>
    fun getLatestBudget(): Flow<Budget?>
}
