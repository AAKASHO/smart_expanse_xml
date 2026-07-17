package com.smartexpense.ai.data.mapper

import com.smartexpense.ai.data.model.BudgetEntity
import com.smartexpense.ai.data.model.CategoryTotalEntity
import com.smartexpense.ai.data.model.DailyTotalEntity
import com.smartexpense.ai.data.model.ExpenseEntity
import com.smartexpense.ai.domain.model.Budget
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.DailyTotal
import com.smartexpense.ai.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        amount = amount,
        category = category,
        merchant = merchant,
        note = note,
        date = date,
        paymentMethod = paymentMethod,
        isAutoSynced = isAutoSynced,
        isPending = isPending,
        createdAt = createdAt
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        amount = amount,
        category = category,
        merchant = merchant,
        note = note,
        date = date,
        paymentMethod = paymentMethod,
        isAutoSynced = isAutoSynced,
        isPending = isPending,
        createdAt = createdAt
    )
}

fun BudgetEntity.toDomain(): Budget {
    return Budget(
        id = id,
        monthlyLimit = monthlyLimit,
        month = month,
        year = year,
        createdAt = createdAt
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        monthlyLimit = monthlyLimit,
        month = month,
        year = year,
        createdAt = createdAt
    )
}

fun CategoryTotalEntity.toDomain(): CategoryTotal {
    return CategoryTotal(
        category = category,
        total = total
    )
}

fun DailyTotalEntity.toDomain(): DailyTotal {
    return DailyTotal(
        date = date,
        total = total
    )
}
