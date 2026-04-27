package com.smartexpense.ai.ui.addexpense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.service.notification.NotificationHelper
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val useCases = (application as SmartExpenseApp).useCases

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
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
