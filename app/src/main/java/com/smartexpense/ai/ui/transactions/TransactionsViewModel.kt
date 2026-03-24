package com.smartexpense.ai.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.db.Expense

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository

    private val _searchQuery = MutableLiveData("")
    private val _categoryFilter = MutableLiveData<String?>(null)

    val allExpenses = repository.getAllExpenses().asLiveData()

    val filteredExpenses: LiveData<List<Expense>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            _categoryFilter.switchMap { category ->
                if (category.isNullOrBlank()) {
                    repository.getAllExpenses().asLiveData()
                } else {
                    repository.getByCategory(category).asLiveData()
                }
            }
        } else {
            repository.searchExpenses(query).asLiveData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
        _searchQuery.value = ""
    }
}
