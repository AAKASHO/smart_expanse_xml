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
import com.smartexpense.ai.data.db.CategoryTotal
import com.smartexpense.ai.data.db.DailyTotal
import com.smartexpense.ai.service.insights.InsightType
import com.smartexpense.ai.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var viewModel: AnalyticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_analytics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]

        setupCharts(view)
        observeData(view)
    }

    private fun setupCharts(view: View) {
        // Bar Chart Setup
        val barChart = view.findViewById<BarChart>(R.id.bar_chart)
        barChart.apply {
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

        // Pie Chart Setup
        val pieChart = view.findViewById<PieChart>(R.id.pie_chart)
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 55f
            transparentCircleRadius = 60f
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.parseColor("#1B1B21"))
            setEntryLabelTextSize(11f)
            setTouchEnabled(true)
            legend.isEnabled = false
            setNoDataText("No category data yet")
            setNoDataTextColor(Color.parseColor("#767683"))
        }
    }

    private fun observeData(view: View) {
        val tvTotal = view.findViewById<TextView>(R.id.tv_total_spending)

        var totalSpending = 0.0
        var budgetLimit = 30000.0
        var prevSpending: Double? = null

        viewModel.currentMonthSpending.observe(viewLifecycleOwner) { total ->
            totalSpending = total ?: 0.0
            tvTotal.text = "₹${CurrencyFormatter.format(totalSpending)}"
        }

        viewModel.currentBudget.observe(viewLifecycleOwner) { budget ->
            budgetLimit = budget?.monthlyLimit ?: 30000.0
        }

        viewModel.previousMonthSpending.observe(viewLifecycleOwner) { total ->
            prevSpending = total
        }

        // Bar Chart Data
        viewModel.dailyTotals.observe(viewLifecycleOwner) { dailyTotals ->
            updateBarChart(view, dailyTotals ?: emptyList())
        }

        // Pie Chart + Insights
        viewModel.categoryTotals.observe(viewLifecycleOwner) { categories ->
            updatePieChart(view, categories ?: emptyList())
        }

        viewModel.currentMonthExpenses.observe(viewLifecycleOwner) { expenses ->
            val categories = viewModel.categoryTotals.value ?: emptyList()
            val insights = viewModel.insightsEngine.generateInsights(
                totalSpending, budgetLimit, categories, prevSpending, expenses ?: emptyList()
            )

            val cardDailyHigh = view.findViewById<View>(R.id.card_daily_high)
            val cardMostExpensive = view.findViewById<View>(R.id.card_most_expensive)
            val cardSaving = view.findViewById<View>(R.id.card_saving)

            cardDailyHigh.visibility = View.GONE
            cardMostExpensive.visibility = View.GONE
            cardSaving.visibility = View.GONE

            insights.forEach { insight ->
                when (insight.type) {
                    InsightType.DAILY_HIGH -> {
                        cardDailyHigh.visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.tv_daily_high_desc).text = insight.description
                    }
                    InsightType.TOP_CATEGORY -> {
                        cardMostExpensive.visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.tv_most_expensive_desc).text = insight.description
                    }
                    InsightType.SAVING_OPPORTUNITY -> {
                        cardSaving.visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.tv_saving_desc).text = insight.description
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateBarChart(view: View, dailyTotals: List<DailyTotal>) {
        val barChart = view.findViewById<BarChart>(R.id.bar_chart)
        if (dailyTotals.isEmpty()) {
            barChart.clear()
            return
        }

        val entries = dailyTotals.mapIndexed { index, daily ->
            BarEntry(index.toFloat(), daily.total.toFloat())
        }

        val labels = dailyTotals.map { daily ->
            SimpleDateFormat("dd", Locale.getDefault()).format(Date(daily.date))
        }

        val dataSet = BarDataSet(entries, "Daily Spending").apply {
            color = Color.parseColor("#000666")
            valueTextColor = Color.parseColor("#767683")
            valueTextSize = 9f
            setDrawValues(false)
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.animateY(800)
        barChart.invalidate()
    }

    private fun updatePieChart(view: View, categories: List<CategoryTotal>) {
        val pieChart = view.findViewById<PieChart>(R.id.pie_chart)
        if (categories.isEmpty()) {
            pieChart.clear()
            return
        }

        val entries = categories.map { cat ->
            PieEntry(cat.total.toFloat(), cat.category)
        }

        val colors = listOf(
            Color.parseColor("#000666"),
            Color.parseColor("#006A6A"),
            Color.parseColor("#38DEBB"),
            Color.parseColor("#4C56AF"),
            Color.parseColor("#BDC2FF")
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        pieChart.data = PieData(dataSet)
        pieChart.animateXY(800, 800)
        pieChart.invalidate()
    }
}
