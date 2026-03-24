package com.smartexpense.ai.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.db.Budget
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetLimitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository
    
    val currentBudget = repository.getLatestBudget().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateBudget(amount: Double) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val existing = currentBudget.value
            val newBudget = if (existing != null && existing.month == cal.get(Calendar.MONTH) + 1 && existing.year == cal.get(Calendar.YEAR)) {
                existing.copy(monthlyLimit = amount)
            } else {
                Budget(
                    monthlyLimit = amount,
                    month = cal.get(Calendar.MONTH) + 1,
                    year = cal.get(Calendar.YEAR)
                )
            }
            if (existing != null && existing.id != 0 && newBudget.id != 0) {
                 repository.updateBudget(newBudget)
            } else {
                 repository.setBudget(newBudget)
            }
        }
    }
}
