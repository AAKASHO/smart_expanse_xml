package com.smartexpense.ai.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.service.insights.InsightsEngine

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val useCases = (application as SmartExpenseApp).useCases
    val insightsEngine = InsightsEngine()

    val currentMonthSpending = useCases.getCurrentMonthSpending().asLiveData()
    val currentBudget = useCases.getLatestBudget().asLiveData()
    val recentExpenses = useCases.getRecentExpenses(5).asLiveData()
    val categoryTotals = useCases.getCurrentMonthCategoryTotals().asLiveData()
    val previousMonthSpending = useCases.getPreviousMonthSpending().asLiveData()
    val currentMonthExpenses = useCases.getCurrentMonthExpenses().asLiveData()
}
