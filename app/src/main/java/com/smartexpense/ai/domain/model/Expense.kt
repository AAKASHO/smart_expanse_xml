package com.smartexpense.ai.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val merchant: String = "",
    val note: String = "",
    val date: Long,
    val paymentMethod: String = "UPI",
    val isAutoSynced: Boolean = false,
    val isPending: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
