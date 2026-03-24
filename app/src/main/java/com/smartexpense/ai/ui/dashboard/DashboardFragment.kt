package com.smartexpense.ai.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smartexpense.ai.R
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.service.insights.InsightType
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupObservers(view)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<View>(R.id.btn_view_all)?.setOnClickListener {
            findNavController().navigate(R.id.nav_transactions)
        }
        view.findViewById<View>(R.id.tv_view_all_insights)?.setOnClickListener {
            findNavController().navigate(R.id.nav_analytics)
        }
    }

    private fun setupObservers(view: View) {
        val tvSpending = view.findViewById<TextView>(R.id.tv_spending_amount)
        val tvBudgetDivider = view.findViewById<TextView>(R.id.tv_budget_divider)
        val tvPercentage = view.findViewById<TextView>(R.id.tv_budget_percentage)
        val tvRemaining = view.findViewById<TextView>(R.id.tv_remaining)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_budget)
        val recentContainer = view.findViewById<LinearLayout>(R.id.recent_transactions_container)
        val tvNoTransactions = view.findViewById<TextView>(R.id.tv_no_transactions)
        val btnViewAll = view.findViewById<View>(R.id.btn_view_all)

        // Insight cards
        val cardBudgetWarning = view.findViewById<View>(R.id.card_budget_warning)
        val cardTrend = view.findViewById<View>(R.id.card_trend)
        val cardTopCategory = view.findViewById<View>(R.id.card_top_category)
        val tvBudgetWarningDesc = view.findViewById<TextView>(R.id.tv_budget_warning_desc)
        val tvTrendDesc = view.findViewById<TextView>(R.id.tv_trend_desc)
        val tvTopCategoryDesc = view.findViewById<TextView>(R.id.tv_top_category_desc)
        val tvNoInsights = view.findViewById<TextView>(R.id.tv_no_insights)

        var currentSpending = 0.0
        var budgetLimit = 30000.0
        var prevSpending: Double? = null

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            budgetLimit = budget?.monthlyLimit ?: 30000.0
            tvBudgetDivider.text = " / ₹${CurrencyFormatter.format(budgetLimit)}"
            updateBudgetUI(tvSpending, tvPercentage, tvRemaining, progressBar, currentSpending, budgetLimit)
        }

        viewModel.currentMonthSpending.observe(viewLifecycleOwner) { total ->
            currentSpending = total ?: 0.0
            tvSpending.text = "₹${CurrencyFormatter.format(currentSpending)}"
            updateBudgetUI(tvSpending, tvPercentage, tvRemaining, progressBar, currentSpending, budgetLimit)
        }

        viewModel.previousMonthSpending.observe(viewLifecycleOwner) { total ->
            prevSpending = total
        }

        viewModel.recentExpenses.observe(viewLifecycleOwner) { expenses ->
            recentContainer.removeAllViews()
            if (expenses.isNullOrEmpty()) {
                tvNoTransactions.visibility = View.VISIBLE
                btnViewAll.visibility = View.GONE
                recentContainer.visibility = View.GONE
            } else {
                tvNoTransactions.visibility = View.GONE
                btnViewAll.visibility = View.VISIBLE
                recentContainer.visibility = View.VISIBLE
                expenses.take(3).forEach { expense ->
                    recentContainer.addView(createTransactionRow(expense))
                }
            }
        }

        // AI Insights
        viewModel.categoryTotals.observe(viewLifecycleOwner) { categories ->
            val insights = viewModel.insightsEngine.generateInsights(
                currentSpending, budgetLimit, categories ?: emptyList(),
                prevSpending, emptyList()
            )

            // Reset visibility
            cardBudgetWarning.visibility = View.GONE
            cardTrend.visibility = View.GONE
            cardTopCategory.visibility = View.GONE

            var hasInsights = false
            insights.forEach { insight ->
                when (insight.type) {
                    InsightType.BUDGET_WARNING -> {
                        cardBudgetWarning.visibility = View.VISIBLE
                        tvBudgetWarningDesc.text = insight.description
                        hasInsights = true
                    }
                    InsightType.TREND_DETECTION -> {
                        cardTrend.visibility = View.VISIBLE
                        tvTrendDesc.text = insight.description
                        hasInsights = true
                    }
                    InsightType.TOP_CATEGORY -> {
                        cardTopCategory.visibility = View.VISIBLE
                        tvTopCategoryDesc.text = insight.description
                        hasInsights = true
                    }
                    else -> {}
                }
            }

            tvNoInsights.visibility = if (hasInsights) View.GONE else View.VISIBLE
        }
    }

    private fun updateBudgetUI(
        tvSpending: TextView, tvPercentage: TextView, tvRemaining: TextView,
        progressBar: ProgressBar, spending: Double, budget: Double
    ) {
        val percentage = if (budget > 0) ((spending / budget) * 100).toInt().coerceAtMost(100) else 0
        val remaining = (budget - spending).coerceAtLeast(0.0)

        tvSpending.text = "₹${CurrencyFormatter.format(spending)}"
        tvPercentage.text = "$percentage% of budget used"
        tvRemaining.text = "₹${CurrencyFormatter.format(remaining)} Remaining"
        progressBar.progress = percentage
    }

    private fun createTransactionRow(expense: Expense): View {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_transaction_simple, null)

        row.findViewById<TextView>(R.id.tv_merchant).text =
            expense.merchant.ifEmpty { expense.category }
        row.findViewById<TextView>(R.id.tv_details).text =
            "${DateFormatter.getRelativeDate(expense.date)} • ${expense.category}"
        row.findViewById<TextView>(R.id.tv_amount).text =
            "- ₹${CurrencyFormatter.format(expense.amount)}"

        return row
    }
}
