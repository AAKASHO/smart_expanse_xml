package com.smartexpense.ai.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.domain.model.Expense

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val useCases = (application as SmartExpenseApp).useCases

    private val _searchQuery = MutableLiveData("")
    private val _categoryFilter = MutableLiveData<String?>(null)

    val allExpenses = useCases.getAllExpenses().asLiveData()

    val filteredExpenses: LiveData<List<Expense>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            _categoryFilter.switchMap { category ->
                if (category.isNullOrBlank()) {
                    useCases.getAllExpenses().asLiveData()
                } else {
                    useCases.getExpensesByCategory(category).asLiveData()
                }
            }
        } else {
            useCases.searchExpenses(query).asLiveData()
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
