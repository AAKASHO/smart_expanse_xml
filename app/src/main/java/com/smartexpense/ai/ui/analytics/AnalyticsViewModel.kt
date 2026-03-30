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
    private val repository = (application as SmartExpenseApp).repository
    val insightsEngine = InsightsEngine()

    private val _dateFilter = MutableLiveData(DateFilter.MONTHLY)
    val dateFilter: LiveData<DateFilter> = _dateFilter

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    val spendingTotal = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) repository.getCurrentMonthSpending().asLiveData()
        else repository.getWeeklySpending().asLiveData()
    }

    val categoryTotals = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) repository.getCurrentMonthCategoryTotals().asLiveData()
        else repository.getWeeklyCategoryTotals().asLiveData()
    }

    val dailyTotals = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) repository.getCurrentMonthDailyTotals().asLiveData()
        else repository.getWeeklyDailyTotals().asLiveData()
    }

    val expenses = _dateFilter.switchMap { filter ->
        if (filter == DateFilter.MONTHLY) repository.getCurrentMonthExpenses().asLiveData()
        else repository.getWeeklyExpenses().asLiveData()
    }

    val previousMonthSpending = repository.getPreviousMonthSpending().asLiveData()
    val currentBudget = repository.getLatestBudget().asLiveData()
}
