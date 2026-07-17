package com.smartexpense.ai.ui.addexpense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.R
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.data.ExpenseCategory
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.service.notification.NotificationHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val useCases = (application as SmartExpenseApp).useCases

    /** Merged list: built-in categories first, then user-created ones. */
    val allCategories = useCases.getCustomCategories().map { customList ->
            val builtIn = ExpenseCategories.ALL
            val custom = customList.map { entity ->
                ExpenseCategory(
                    key = entity.key,
                    label = entity.label,
                    iconRes = R.drawable.ic_other  // emoji shown by label; uses ic_other as icon slot
                )
            }
            builtIn + custom
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseCategories.ALL)

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            val d = async {
                useCases.addExpense(expense)
            }
            val ans = d.await()
            useCases.addExpense(expense)
            NotificationHelper(getApplication()).checkBudgetAndNotify(useCases)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            useCases.updateExpense(expense)
            NotificationHelper(getApplication()).checkBudgetAndNotify(useCases)
        }
    }

    fun loadExpense(id: Long): LiveData<Expense?> =
        useCases.getExpenseById(id).asLiveData()
}
