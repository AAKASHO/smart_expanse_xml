package com.smartexpense.ai.data.model

data class CategoryTotalEntity(
    val category: String,
    val total: Double
)

data class DailyTotalEntity(
    val date: Long,
    val total: Double
)
