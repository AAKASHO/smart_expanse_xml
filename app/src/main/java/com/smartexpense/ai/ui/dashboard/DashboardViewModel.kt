package com.smartexpense.ai.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.service.insights.InsightsEngine

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository
    val insightsEngine = InsightsEngine()

    val currentMonthSpending = repository.getCurrentMonthSpending().asLiveData()
    val currentBudget = repository.getLatestBudget().asLiveData()
    val recentExpenses = repository.getRecentExpenses(5).asLiveData()
    val categoryTotals = repository.getCurrentMonthCategoryTotals().asLiveData()
    val previousMonthSpending = repository.getPreviousMonthSpending().asLiveData()
    val currentMonthExpenses = repository.getCurrentMonthExpenses().asLiveData()
}
