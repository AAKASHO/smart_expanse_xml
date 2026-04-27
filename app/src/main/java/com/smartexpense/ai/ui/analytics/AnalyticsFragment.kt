package com.smartexpense.ai.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.smartexpense.ai.R
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.DailyTotal
import com.smartexpense.ai.service.insights.InsightType
import com.smartexpense.ai.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var viewModel: AnalyticsViewModel
    private var _binding: com.smartexpense.ai.databinding.FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = com.smartexpense.ai.databinding.FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupCharts()
        setupClickListeners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.toggleMonthly.setOnClickListener {
            viewModel.setDateFilter(DateFilter.MONTHLY)
        }
        binding.toggleWeekly.setOnClickListener {
            viewModel.setDateFilter(DateFilter.WEEKLY)
        }
    }

    private fun setupCharts() {
        // Bar Chart Setup
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            setTouchEnabled(false)
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1AC6C5D4")
                textColor = Color.parseColor("#767683")
                textSize = 10f
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#767683")
                textSize = 10f
                granularity = 1f
            }
            setNoDataText("No spending data yet")
            setNoDataTextColor(Color.parseColor("#767683"))
        }

        // Pie Chart Setup (Donut style)
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 75f
            transparentCircleRadius = 0f
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(false)
            setTouchEnabled(true)
            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                yOffset = 5f
            }
            setNoDataText("No category data yet")
            setNoDataTextColor(Color.WHITE)
        }
    }

    private fun observeData() {
        viewModel.spendingTotal.observe(viewLifecycleOwner) { total ->
            binding.tvTotalSpending.text = "₹${CurrencyFormatter.format(total ?: 0.0)}"
        }

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            // Budget can be used for insights or comparison if needed
        }

        // Bar Chart Data
        viewModel.dailyTotals.observe(viewLifecycleOwner) { dailyTotals ->
            updateBarChart(dailyTotals ?: emptyList())
        }

        // Pie Chart + Insights
        viewModel.categoryTotals.observe(viewLifecycleOwner) { categories ->
            updatePieChart(categories ?: emptyList())
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            val categories = viewModel.categoryTotals.value ?: emptyList()
            val totalSpending = viewModel.spendingTotal.value ?: 0.0
            val budgetLimit = viewModel.currentBudget.value?.monthlyLimit ?: 30000.0
            val prevSpending = viewModel.previousMonthSpending.value

            val insights = viewModel.insightsEngine.generateInsights(
                totalSpending, budgetLimit, categories, prevSpending, expenses ?: emptyList()
            )

            binding.cardDailyHigh.visibility = View.GONE
            binding.cardMostExpensive.visibility = View.GONE
            binding.cardSaving.visibility = View.GONE

            insights.forEach { insight ->
                when (insight.type) {
                    InsightType.DAILY_HIGH -> {
                        binding.cardDailyHigh.visibility = View.VISIBLE
                        binding.tvDailyHighDesc.text = insight.description
                    }
                    InsightType.TOP_CATEGORY -> {
                        binding.cardMostExpensive.visibility = View.VISIBLE
                        binding.tvMostExpensiveDesc.text = insight.description
                    }
                    InsightType.SAVING_OPPORTUNITY -> {
                        binding.cardSaving.visibility = View.VISIBLE
                        binding.tvSavingDesc.text = insight.description
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateBarChart(dailyTotals: List<DailyTotal>) {
        if (dailyTotals.isEmpty()) {
            binding.barChart.clear()
            return
        }

        val entries = dailyTotals.mapIndexed { index, daily ->
            BarEntry(index.toFloat(), daily.total.toFloat())
        }

        val labels = dailyTotals.map { daily ->
            SimpleDateFormat("dd", Locale.getDefault()).format(Date(daily.date))
        }

        val dataSet = BarDataSet(entries, "Spending").apply {
            color = Color.parseColor("#000666")
            setDrawValues(false)
        }

        binding.barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.animateY(800)
        binding.barChart.invalidate()
    }

    private fun updatePieChart(categories: List<CategoryTotal>) {
        if (categories.isEmpty()) {
            binding.pieChart.clear()
            return
        }

        val entries = categories.map { cat ->
            PieEntry(cat.total.toFloat(), cat.category)
        }

        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.category_food),
            ContextCompat.getColor(requireContext(), R.color.category_travel),
            ContextCompat.getColor(requireContext(), R.color.category_bills),
            ContextCompat.getColor(requireContext(), R.color.category_shopping),
            ContextCompat.getColor(requireContext(), R.color.category_other)
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            setDrawValues(false)
            sliceSpace = 4f
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.animateXY(800, 800)
        binding.pieChart.invalidate()
        
        // Add total in center
        val total = categories.sumOf { it.total }
        binding.pieChart.centerText = "₹${CurrencyFormatter.format(total)}"
        binding.pieChart.setCenterTextColor(Color.WHITE)
        binding.pieChart.setCenterTextSize(18f)
    }
}
