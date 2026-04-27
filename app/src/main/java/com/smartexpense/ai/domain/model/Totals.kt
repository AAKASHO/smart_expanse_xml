package com.smartexpense.ai.domain.model

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class DailyTotal(
    val date: Long,
    val total: Double
)
