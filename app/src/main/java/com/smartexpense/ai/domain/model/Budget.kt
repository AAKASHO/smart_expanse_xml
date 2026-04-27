package com.smartexpense.ai.domain.model

data class Budget(
    val id: Long = 0,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
)
