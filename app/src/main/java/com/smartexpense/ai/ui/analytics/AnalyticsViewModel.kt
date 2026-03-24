package com.smartexpense.ai.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.service.insights.InsightsEngine

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SmartExpenseApp).repository
    val insightsEngine = InsightsEngine()

    val currentMonthSpending = repository.getCurrentMonthSpending().asLiveData()
    val categoryTotals = repository.getCurrentMonthCategoryTotals().asLiveData()
    val dailyTotals = repository.getCurrentMonthDailyTotals().asLiveData()
    val previousMonthSpending = repository.getPreviousMonthSpending().asLiveData()
    val currentMonthExpenses = repository.getCurrentMonthExpenses().asLiveData()
    val currentBudget = repository.getLatestBudget().asLiveData()
}
