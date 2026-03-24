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
import com.smartexpense.ai.databinding.FragmentDashboardBinding
import com.smartexpense.ai.service.insights.InsightType
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnViewAll.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_transactions
        }
        binding.tvViewAllInsights.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_analytics
        }
    }

    private fun setupObservers() {
        var currentSpending = 0.0
        var budgetLimit = 30000.0
        var prevSpending: Double? = null

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            budgetLimit = budget?.monthlyLimit ?: 30000.0
            binding.tvBudgetDivider.text = " / ₹${CurrencyFormatter.format(budgetLimit)}"
            updateBudgetUI(currentSpending, budgetLimit)
        }

        viewModel.currentMonthSpending.observe(viewLifecycleOwner) { total ->
            currentSpending = total ?: 0.0
            binding.tvSpendingAmount.text = "₹${CurrencyFormatter.format(currentSpending)}"
            updateBudgetUI(currentSpending, budgetLimit)
        }

        viewModel.previousMonthSpending.observe(viewLifecycleOwner) { total ->
            prevSpending = total
        }

        viewModel.recentExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.recentTransactionsContainer.removeAllViews()
            if (expenses.isNullOrEmpty()) {
                binding.tvNoTransactions.visibility = View.VISIBLE
                binding.btnViewAll.visibility = View.GONE
                binding.recentTransactionsContainer.visibility = View.GONE
            } else {
                binding.tvNoTransactions.visibility = View.GONE
                binding.btnViewAll.visibility = View.VISIBLE
                binding.recentTransactionsContainer.visibility = View.VISIBLE
                expenses.take(3).forEach { expense ->
                    binding.recentTransactionsContainer.addView(createTransactionRow(expense))
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
            binding.cardBudgetWarning.visibility = View.GONE
            binding.cardTrend.visibility = View.GONE
            binding.cardTopCategory.visibility = View.GONE

            var hasInsights = false
            insights.forEach { insight ->
                when (insight.type) {
                    InsightType.BUDGET_WARNING -> {
                        binding.cardBudgetWarning.visibility = View.VISIBLE
                        binding.tvBudgetWarningDesc.text = insight.description
                        hasInsights = true
                    }
                    InsightType.TREND_DETECTION -> {
                        binding.cardTrend.visibility = View.VISIBLE
                        binding.tvTrendDesc.text = insight.description
                        hasInsights = true
                    }
                    InsightType.TOP_CATEGORY -> {
                        binding.cardTopCategory.visibility = View.VISIBLE
                        binding.tvTopCategoryDesc.text = insight.description
                        hasInsights = true
                    }
                    else -> {}
                }
            }

            binding.tvNoInsights.visibility = if (hasInsights) View.GONE else View.VISIBLE
        }
    }

    private fun updateBudgetUI(spending: Double, budget: Double) {
        val percentage = if (budget > 0) ((spending / budget) * 100).toInt().coerceAtMost(100) else 0
        val remaining = (budget - spending).coerceAtLeast(0.0)

        binding.tvSpendingAmount.text = "₹${CurrencyFormatter.format(spending)}"
        binding.tvBudgetPercentage.text = "$percentage% of budget used"
        binding.tvRemaining.text = "₹${CurrencyFormatter.format(remaining)} Remaining"
        binding.progressBudget.progress = percentage
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
