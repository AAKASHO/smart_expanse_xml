package com.smartexpense.ai.domain.usecase

import com.smartexpense.ai.domain.model.Budget
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.DailyTotal
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

data class ExpenseUseCases(
    val addExpense: AddExpenseUseCase,
    val updateExpense: UpdateExpenseUseCase,
    val deleteExpense: DeleteExpenseUseCase,
    val getAllExpenses: GetAllExpensesUseCase,
    val getRecentExpenses: GetRecentExpensesUseCase,
    val getExpensesByCategory: GetExpensesByCategoryUseCase,
    val searchExpenses: SearchExpensesUseCase,
    val getCurrentMonthSpending: GetCurrentMonthSpendingUseCase,
    val getCurrentBudget: GetCurrentBudgetUseCase,
    val getLatestBudget: GetLatestBudgetUseCase,
    val getCurrentMonthCategoryTotals: GetCurrentMonthCategoryTotalsUseCase,
    val getPreviousMonthSpending: GetPreviousMonthSpendingUseCase,
    val getCurrentMonthExpenses: GetCurrentMonthExpensesUseCase,
    val getExpenseById: GetExpenseByIdUseCase,
    val getWeeklySpending: GetWeeklySpendingUseCase,
    val getWeeklyCategoryTotals: GetWeeklyCategoryTotalsUseCase,
    val getWeeklyDailyTotals: GetWeeklyDailyTotalsUseCase,
    val getWeeklyExpenses: GetWeeklyExpensesUseCase,
    val getCurrentMonthDailyTotals: GetCurrentMonthDailyTotalsUseCase,
    val getCurrentDaySpending: GetCurrentDaySpendingUseCase,
    val setBudget: SetBudgetUseCase,
    val updateBudget: UpdateBudgetUseCase
)

class AddExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense): Long {
        if (expense.amount <= 0) throw IllegalArgumentException("Expense amount must be positive")
        return repository.addExpense(expense)
    }
}

class UpdateExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) = repository.updateExpense(expense)
}

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) = repository.deleteExpense(expense)
}

class GetAllExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<Expense>> = repository.getAllExpenses()
}

class GetRecentExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(limit: Int = 5): Flow<List<Expense>> = repository.getRecentExpenses(limit)
}

class GetExpensesByCategoryUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(category: String): Flow<List<Expense>> = repository.getByCategory(category)
}

class SearchExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(query: String): Flow<List<Expense>> = repository.searchExpenses(query)
}

class GetCurrentMonthSpendingUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Double?> = repository.getCurrentMonthSpending()
}

class GetCurrentBudgetUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Budget?> = repository.getCurrentBudget()
}

class GetLatestBudgetUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Budget?> = repository.getLatestBudget()
}

class GetCurrentMonthCategoryTotalsUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<CategoryTotal>> = repository.getCurrentMonthCategoryTotals()
}

class GetPreviousMonthSpendingUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Double?> = repository.getPreviousMonthSpending()
}

class GetCurrentMonthExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<Expense>> = repository.getCurrentMonthExpenses()
}

class GetExpenseByIdUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(id: Long): Flow<Expense?> = repository.getById(id)
}

class GetWeeklySpendingUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Double?> = repository.getWeeklySpending()
}

class GetWeeklyCategoryTotalsUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<CategoryTotal>> = repository.getWeeklyCategoryTotals()
}

class GetWeeklyDailyTotalsUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<DailyTotal>> = repository.getWeeklyDailyTotals()
}

class GetWeeklyExpensesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<Expense>> = repository.getWeeklyExpenses()
}

class GetCurrentMonthDailyTotalsUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<List<DailyTotal>> = repository.getCurrentMonthDailyTotals()
}

class GetCurrentDaySpendingUseCase(private val repository: ExpenseRepository) {
    operator fun invoke(): Flow<Double?> = repository.getCurrentDaySpending()
}

class SetBudgetUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(budget: Budget): Long = repository.setBudget(budget)
}

class UpdateBudgetUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(budget: Budget) = repository.updateBudget(budget)
}
