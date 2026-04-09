package com.smartexpense.ai.ui.addexpense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.service.notification.NotificationHelper
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            NotificationHelper(getApplication()).checkBudgetAndNotify(repository)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            NotificationHelper(getApplication()).checkBudgetAndNotify(repository)
        }
    }

    fun loadExpense(id: Long): LiveData<Expense?> =
        repository.getById(id).asLiveData()
}
