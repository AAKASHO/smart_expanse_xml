package com.smartexpense.ai.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.data.ExpenseCategory
import com.smartexpense.ai.data.model.CustomCategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Unified category item shown in the Manage Categories list. */
data class DisplayCategory(
    val key: String,
    val label: String,
    val emoji: String,          // emoji icon (custom) or "" (built-in uses drawable)
    val iconRes: Int = 0,       // drawable icon (built-in)
    val isBuiltIn: Boolean,
    val entity: CustomCategoryEntity? = null  // non-null for custom; needed for deletion
)

class ManageCategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val useCases = (application as SmartExpenseApp).useCases

    /** Live merged list: built-in (non-deletable) + custom categories from DB. */
    val displayCategories = useCases.getCustomCategories()
        .map { customList ->
            val builtIn = ExpenseCategories.ALL.map { cat ->
                DisplayCategory(
                    key = cat.key,
                    label = cat.label,
                    emoji = "",
                    iconRes = cat.iconRes,
                    isBuiltIn = true
                )
            }
            val custom = customList.map { entity ->
                DisplayCategory(
                    key = entity.key,
                    label = entity.label,
                    emoji = entity.emoji,
                    isBuiltIn = false,
                    entity = entity
                )
            }
            builtIn + custom
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(label: String, emoji: String) {
        if (label.isBlank()) return
        val key = label.trim().replaceFirstChar { it.uppercase() }
        viewModelScope.launch {
            useCases.addCustomCategory(
                CustomCategoryEntity(key = key, label = key, emoji = emoji)
            )
        }
    }

    fun deleteCategory(entity: CustomCategoryEntity) {
        viewModelScope.launch {
            useCases.deleteCustomCategory(entity)
        }
    }
}
