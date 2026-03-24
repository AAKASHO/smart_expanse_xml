package com.smartexpense.ai.service.insights

import com.smartexpense.ai.data.db.CategoryTotal
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.util.CurrencyFormatter

data class InsightCard(
    val title: String,
    val description: String,
    val type: InsightType,
    val severity: InsightSeverity = InsightSeverity.INFO
)

enum class InsightType {
    BUDGET_WARNING,
    TREND_DETECTION,
    TOP_CATEGORY,
    SAVING_OPPORTUNITY,
    DAILY_HIGH
}

enum class InsightSeverity {
    CRITICAL, WARNING, INFO
}

class InsightsEngine {

    /**
     * Generate all insights based on current data
     */
    fun generateInsights(
        currentMonthTotal: Double,
        budgetLimit: Double,
        categoryTotals: List<CategoryTotal>,
        previousMonthTotal: Double?,
        expenses: List<Expense>
    ): List<InsightCard> {
        val insights = mutableListOf<InsightCard>()

        // 1. Budget Warning
        getBudgetWarning(currentMonthTotal, budgetLimit)?.let { insights.add(it) }

        // 2. Trend Detection (month-over-month)
        getTrendInsight(currentMonthTotal, previousMonthTotal)?.let { insights.add(it) }

        // 3. Top Category
        getTopCategoryInsight(categoryTotals, currentMonthTotal)?.let { insights.add(it) }

        // 4. Smart Saving Opportunity
        getSavingOpportunity(categoryTotals, budgetLimit, currentMonthTotal)?.let { insights.add(it) }

        // 5. Highest Daily Spend
        getHighestDailySpend(expenses)?.let { insights.add(it) }

        return insights
    }

    private fun getBudgetWarning(total: Double, limit: Double): InsightCard? {
        if (limit <= 0) return null
        val percentage = (total / limit * 100).toInt()

        return when {
            percentage >= 100 -> InsightCard(
                title = "Budget Exceeded!",
                description = "You've spent ₹${CurrencyFormatter.format(total)} — exceeding your ₹${CurrencyFormatter.format(limit)} budget by ₹${CurrencyFormatter.format(total - limit)}.",
                type = InsightType.BUDGET_WARNING,
                severity = InsightSeverity.CRITICAL
            )
            percentage >= 80 -> InsightCard(
                title = "Budget Warning",
                description = "You've used $percentage% of your monthly budget. Only ₹${CurrencyFormatter.format(limit - total)} remaining.",
                type = InsightType.BUDGET_WARNING,
                severity = InsightSeverity.WARNING
            )
            else -> null
        }
    }

    private fun getTrendInsight(currentTotal: Double, previousTotal: Double?): InsightCard? {
        if (previousTotal == null || previousTotal == 0.0) return null

        val change = ((currentTotal - previousTotal) / previousTotal * 100).toInt()

        return when {
            change > 10 -> InsightCard(
                title = "Trend Detection",
                description = "Spending increased by $change% vs last month. Consider reviewing your expenses.",
                type = InsightType.TREND_DETECTION,
                severity = if (change > 30) InsightSeverity.WARNING else InsightSeverity.INFO
            )
            change < -10 -> InsightCard(
                title = "Great Savings!",
                description = "You're spending ${-change}% less than last month. Keep it up! 🎉",
                type = InsightType.TREND_DETECTION,
                severity = InsightSeverity.INFO
            )
            else -> InsightCard(
                title = "Steady Spending",
                description = "Your spending is consistent with last month. Good financial discipline!",
                type = InsightType.TREND_DETECTION,
                severity = InsightSeverity.INFO
            )
        }
    }

    private fun getTopCategoryInsight(
        categoryTotals: List<CategoryTotal>,
        totalSpending: Double
    ): InsightCard? {
        if (categoryTotals.isEmpty() || totalSpending == 0.0) return null

        val top = categoryTotals.first()
        val percentage = (top.total / totalSpending * 100).toInt()

        return InsightCard(
            title = "Top Category",
            description = "Most spent on ${top.category} ($percentage%). ₹${CurrencyFormatter.format(top.total)} this month.",
            type = InsightType.TOP_CATEGORY,
            severity = if (percentage > 50) InsightSeverity.WARNING else InsightSeverity.INFO
        )
    }

    private fun getSavingOpportunity(
        categoryTotals: List<CategoryTotal>,
        budgetLimit: Double,
        currentTotal: Double
    ): InsightCard? {
        if (categoryTotals.isEmpty() || budgetLimit <= 0) return null

        val top = categoryTotals.first()
        val tenPercent = top.total * 0.1
        val potentialSaving = CurrencyFormatter.format(tenPercent)

        return InsightCard(
            title = "Smart Saving Opportunity",
            description = "Cut ${top.category} spending by 10% to save ₹$potentialSaving/month. That's ₹${CurrencyFormatter.format(tenPercent * 12)}/year!",
            type = InsightType.SAVING_OPPORTUNITY,
            severity = InsightSeverity.INFO
        )
    }

    private fun getHighestDailySpend(expenses: List<Expense>): InsightCard? {
        if (expenses.isEmpty()) return null

        // Group by day and find the highest
        val dailyTotals = expenses.groupBy { it.date / 86400000 }
            .mapValues { (_, exps) -> exps.sumOf { it.amount } }

        val highestDay = dailyTotals.maxByOrNull { it.value } ?: return null
        val highestAmount = CurrencyFormatter.format(highestDay.value)

        val dayTimestamp = highestDay.key * 86400000
        val dayStr = com.smartexpense.ai.util.DateFormatter.formatDate(dayTimestamp)

        return InsightCard(
            title = "Highest Daily Spend",
            description = "₹$highestAmount on $dayStr was your biggest spending day this month.",
            type = InsightType.DAILY_HIGH,
            severity = InsightSeverity.INFO
        )
    }
}
