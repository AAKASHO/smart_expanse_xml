package com.smartexpense.ai.ui.transactions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R

class TransactionsFragment : Fragment() {

    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionAdapter

    private var selectedFilter: String? = null
    private val chipViews = mutableMapOf<String?, TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_transactions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        setupRecyclerView(view)
        setupSearch(view)
        setupFilters(view)
        observeData(view)
    }

    private fun setupRecyclerView(view: View) {
        adapter = TransactionAdapter()
        val rv = view.findViewById<RecyclerView>(R.id.rv_transactions)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun setupSearch(view: View) {
        view.findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters(view: View) {
        chipViews[null] = view.findViewById(R.id.chip_all)
        chipViews["Food"] = view.findViewById(R.id.chip_food)
        chipViews["Travel"] = view.findViewById(R.id.chip_travel_filter)
        chipViews["Bills"] = view.findViewById(R.id.chip_bills_filter)
        chipViews["Shopping"] = view.findViewById(R.id.chip_shopping_filter)

        chipViews.forEach { (category, chip) ->
            chip.setOnClickListener {
                selectedFilter = category
                viewModel.setCategoryFilter(category)
                updateFilterUI()
            }
        }
    }

    private fun updateFilterUI() {
        chipViews.forEach { (category, chip) ->
            if (category == selectedFilter) {
                chip.setBackgroundResource(R.drawable.bg_category_selected)
                chip.setTextColor(resources.getColor(R.color.primary, null))
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_unselected)
                chip.setTextColor(resources.getColor(R.color.on_surface_variant, null))
            }
        }
    }

    private fun observeData(view: View) {
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        val rvTransactions = view.findViewById<RecyclerView>(R.id.rv_transactions)

        viewModel.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvTransactions.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvTransactions.visibility = View.VISIBLE
                adapter.submitList(TransactionAdapter.groupByDate(expenses))
            }
        }
    }
}
