package com.smartexpense.ai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val merchant: String = "",
    val note: String = "",
    val date: Long, // timestamp in millis
    val paymentMethod: String = "UPI", // UPI, Cash, Card
    val isAutoSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
