package com.smartexpense.ai.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smartexpense.ai.R
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.databinding.FragmentDashboardBinding
import com.smartexpense.ai.service.insights.InsightType
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var insightAdapter: InsightAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupInsightsRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupInsightsRecyclerView() {
        insightAdapter = InsightAdapter()
        binding.rvInsights.apply {
            adapter = insightAdapter
            val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun setupClickListeners() {
        binding.btnViewAll.setOnClickListener {
            (requireActivity() as com.smartexpense.ai.MainActivity).navigateToTab(R.id.nav_transactions)
        }
        binding.heroCard.setOnClickListener {
            findNavController().navigate(R.id.nav_budget_limits)
        }
        binding.tvViewAllInsights.setOnClickListener {
            (requireActivity() as com.smartexpense.ai.MainActivity).navigateToTab(R.id.nav_analytics)
        }
        binding.ivProfile.setOnClickListener {
            (requireActivity() as com.smartexpense.ai.MainActivity).navigateToTab(R.id.nav_settings)
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

            insightAdapter.submitList(insights)
            
            val hasInsights = insights.isNotEmpty()
            binding.rvInsights.visibility = if (hasInsights) View.VISIBLE else View.GONE
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

        // Update Bento Grid Category Budgets
        updateCategoryBentoGrid()
    }

    private fun updateCategoryBentoGrid() {
        val categories = viewModel.categoryTotals.value ?: emptyList()
        
        // Define limits (these could be fetched from DB later, but for now we follow the design)
        val limits = mapOf(
            "Food" to 5000.0,
            "Travel" to 3000.0,
            "Bills" to 10000.0,
            "Shopping" to 7000.0
        )

        val foodSpent = categories.find { it.category == "Food" }?.total ?: 0.0
        val travelSpent = categories.find { it.category == "Travel" }?.total ?: 0.0
        val billsSpent = categories.find { it.category == "Bills" }?.total ?: 0.0
        val shoppingSpent = categories.find { it.category == "Shopping" }?.total ?: 0.0

        updateCategoryCard(binding.tvFoodBudgetStatus, binding.progressFoodBudget, foodSpent, limits["Food"]!!)
        updateCategoryCard(binding.tvTravelBudgetStatus, binding.progressTravelBudget, travelSpent, limits["Travel"]!!)
        updateCategoryCard(binding.tvBillsBudgetStatus, binding.progressBillsBudget, billsSpent, limits["Bills"]!!)
        updateCategoryCard(binding.tvShoppingBudgetStatus, binding.progressShoppingBudget, shoppingSpent, limits["Shopping"]!!)
    }

    private fun updateCategoryCard(tvStatus: TextView, progressBar: ProgressBar, spent: Double, limit: Double) {
        tvStatus.text = "₹${CurrencyFormatter.format(spent)} / ₹${CurrencyFormatter.format(limit)}"
        progressBar.progress = if (limit > 0) ((spent / limit) * 100).toInt().coerceAtMost(100) else 0
    }

    private fun createTransactionRow(expense: Expense): View {
        val rowBinding = com.smartexpense.ai.databinding.ItemTransactionSimpleBinding.inflate(LayoutInflater.from(requireContext()), null, false)

        val iconRes = com.smartexpense.ai.data.ExpenseCategories.ALL.find { it.label == expense.category }?.iconRes ?: com.smartexpense.ai.R.drawable.ic_other
        rowBinding.ivCategoryIcon.setImageResource(iconRes)

        rowBinding.tvMerchant.text = expense.merchant.ifEmpty { expense.category }
        rowBinding.tvDetails.text = "${DateFormatter.getRelativeDate(expense.date)} • ${expense.category}"
        rowBinding.tvAmount.text = "- ₹${CurrencyFormatter.format(expense.amount)}"

        return rowBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
