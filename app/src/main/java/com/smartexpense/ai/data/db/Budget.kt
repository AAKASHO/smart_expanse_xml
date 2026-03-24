package com.smartexpense.ai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthlyLimit: Double = 30000.0,
    val month: Int, // 1-12
    val year: Int
)
