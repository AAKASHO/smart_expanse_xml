package com.smartexpense.ai.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "amount")
    val monthlyLimit: Double,
    val month: Int,
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
)
