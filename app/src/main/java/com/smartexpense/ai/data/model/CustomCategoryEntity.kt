package com.smartexpense.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists user-created expense categories.
 * Built-in categories live in [com.smartexpense.ai.data.ExpenseCategories].
 */
@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,           // unique identifier, used as category string in expenses
    val label: String,         // display name
    val emoji: String = "📦"   // emoji displayed as icon (no drawable needed)
)
