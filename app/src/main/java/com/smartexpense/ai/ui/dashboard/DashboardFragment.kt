package com.smartexpense.ai.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smartexpense.ai.R
import com.smartexpense.ai.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var dashboardAdapter: DashboardAdapter

    // Accumulated state — all observers write here, then trigger a list rebuild
    private var currentSpending = 0.0
    private var budgetLimit     = 30_000.0
    private var prevSpending: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupRecyclerView()
        setupObservers()
    }

    // ─── Setup ─────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            onProfileClick = {
                (requireActivity() as com.smartexpense.ai.MainActivity)
                    .navigateToTab(R.id.nav_settings)
            },
            onHeroClick = {
                findNavController().navigate(R.id.nav_budget_limits)
            }
        )
        binding.rvDashboard.adapter = dashboardAdapter
    }

    // ─── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            budgetLimit = budget?.monthlyLimit ?: 30_000.0
            rebuildList()
        }

        viewModel.currentMonthSpending.observe(viewLifecycleOwner) { total ->
            currentSpending = total ?: 0.0
            rebuildList()
        }

        viewModel.previousMonthSpending.observe(viewLifecycleOwner) { total ->
            prevSpending = total
        }

        viewModel.recentExpenses.observe(viewLifecycleOwner) {
            rebuildList()
        }

        viewModel.categoryTotals.observe(viewLifecycleOwner) {
            rebuildList()
        }
    }

    // ─── List builder ──────────────────────────────────────────────────────────

    private fun rebuildList() {
        val categoryTotals  = viewModel.categoryTotals.value  ?: emptyList()
        val recentExpenses  = viewModel.recentExpenses.value  ?: emptyList()

        val insights = viewModel.insightsEngine.generateInsights(
            currentSpending, budgetLimit, categoryTotals, prevSpending, emptyList()
        )

        val items = buildDashboardItems(
            spending              = currentSpending,
            budget                = budgetLimit,
            insights              = insights,
            categoryTotals        = categoryTotals,
            recentExpenses        = recentExpenses,
            onViewAllInsights     = {
                (requireActivity() as com.smartexpense.ai.MainActivity)
                    .navigateToTab(R.id.nav_analytics)
            },
            onViewAllTransactions = {
                (requireActivity() as com.smartexpense.ai.MainActivity)
                    .navigateToTab(R.id.nav_transactions)
            }
        )

        dashboardAdapter.submitList(items)
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
