package com.smartexpense.ai.data.repository

import com.smartexpense.ai.data.db.BudgetDao
import com.smartexpense.ai.data.db.CustomCategoryDao
import com.smartexpense.ai.data.db.ExpenseDao
import com.smartexpense.ai.data.mapper.toDomain
import com.smartexpense.ai.data.mapper.toEntity
import com.smartexpense.ai.data.model.CustomCategoryEntity
import com.smartexpense.ai.domain.model.Budget
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.DailyTotal
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val customCategoryDao: CustomCategoryDao
) : ExpenseRepository {

    // ── Custom Categories ──────────────────────────────────────────────────
    fun getCustomCategories(): Flow<List<CustomCategoryEntity>> = customCategoryDao.getAllCategories()
    suspend fun addCustomCategory(entity: CustomCategoryEntity) = customCategoryDao.insert(entity)
    suspend fun deleteCustomCategory(entity: CustomCategoryEntity) = customCategoryDao.delete(entity)

    override suspend fun addExpense(expense: Expense): Long = expenseDao.insert(expense.toEntity())

    override suspend fun updateExpense(expense: Expense) = expenseDao.update(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense.toEntity())

    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getRecentExpenses(limit: Int): Flow<List<Expense>> = expenseDao.getRecentExpenses(limit).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(category: String): Flow<List<Expense>> = expenseDao.getByCategory(category).map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Expense?> = expenseDao.getById(id).map { it?.toDomain() }

    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.search(query).map { list -> list.map { it.toDomain() } }

    override fun getExpenseCount(): Flow<Int> = expenseDao.getExpenseCount()

    override fun getMonthlySpending(month: Int, year: Int): Flow<Double?> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getTotalSpending(start, end)
    }

    override fun getCurrentMonthSpending(): Flow<Double?> {
        val cal = Calendar.getInstance()
        return getMonthlySpending(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    override fun getCurrentDaySpending(): Flow<Double?> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return expenseDao.getTotalSpending(startCal.timeInMillis, endCal.timeInMillis)
    }

    override fun getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getCategoryTotals(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getCurrentMonthCategoryTotals(): Flow<List<CategoryTotal>> {
        val cal = Calendar.getInstance()
        return getCategoryTotals(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    override fun getDailyTotals(month: Int, year: Int): Flow<List<DailyTotal>> {
        val (start, end) = getMonthRange(month, year)
        return expenseDao.getDailyTotals(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getCurrentMonthDailyTotals(): Flow<List<DailyTotal>> {
        val cal = Calendar.getInstance()
        return getDailyTotals(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    override fun getCurrentMonthExpenses(): Flow<List<Expense>> {
        val cal = Calendar.getInstance()
        val (start, end) = getMonthRange(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        return expenseDao.getByDateRange(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getPreviousMonthSpending(): Flow<Double?> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return getMonthlySpending(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    override fun getWeeklySpending(): Flow<Double?> {
        val (start, end) = getWeekRange()
        return expenseDao.getTotalSpending(start, end)
    }

    override fun getWeeklyCategoryTotals(): Flow<List<CategoryTotal>> {
        val (start, end) = getWeekRange()
        return expenseDao.getCategoryTotals(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getWeeklyDailyTotals(): Flow<List<DailyTotal>> {
        val (start, end) = getWeekRange()
        return expenseDao.getDailyTotals(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getWeeklyExpenses(): Flow<List<Expense>> {
        val (start, end) = getWeekRange()
        return expenseDao.getByDateRange(start, end).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun setBudget(budget: Budget): Long = budgetDao.insert(budget.toEntity())

    override suspend fun updateBudget(budget: Budget) = budgetDao.update(budget.toEntity())

    override fun getBudget(month: Int, year: Int): Flow<Budget?> = budgetDao.getBudget(month, year).map { it?.toDomain() }

    override fun getCurrentBudget(): Flow<Budget?> {
        val cal = Calendar.getInstance()
        return budgetDao.getBudget(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)).map { it?.toDomain() }
    }

    override fun getLatestBudget(): Flow<Budget?> = budgetDao.getLatestBudget().map { it?.toDomain() }

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

    private fun getWeekRange(): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }
}
