package com.smartexpense.ai.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.service.insights.InsightsEngine

enum class DateFilter { MONTHLY, WEEKLY }

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val useCases = (application as SmartExpenseApp).useCases
    val insightsEngine = InsightsEngine()

    private val _dateFilter = MutableLiveData(DateFilter.MONTHLY)
    val dateFilter: LiveData<DateFilter> = _dateFilter

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    val spendingTotal = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) useCases.getCurrentMonthSpending().asLiveData()
        else useCases.getWeeklySpending().asLiveData()
    }

    val categoryTotals = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) useCases.getCurrentMonthCategoryTotals().asLiveData()
        else useCases.getWeeklyCategoryTotals().asLiveData()
    }

    val dailyTotals = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) useCases.getCurrentMonthDailyTotals().asLiveData()
        else useCases.getWeeklyDailyTotals().asLiveData()
    }

    val expenses = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) useCases.getCurrentMonthExpenses().asLiveData()
        else useCases.getWeeklyExpenses().asLiveData()
    }

    val previousMonthSpending = useCases.getPreviousMonthSpending().asLiveData()
    val currentBudget = useCases.getLatestBudget().asLiveData()
}
