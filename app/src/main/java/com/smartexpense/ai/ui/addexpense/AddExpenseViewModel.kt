package com.smartexpense.ai.ui.addexpense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.db.Expense
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }
}
