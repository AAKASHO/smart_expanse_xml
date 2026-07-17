package com.smartexpense.ai.ui.transactions

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartexpense.ai.R
import com.smartexpense.ai.databinding.FragmentTransactionsBinding
import com.smartexpense.ai.util.BindingAdapterUtils
import com.smartexpense.ai.util.gone
import com.smartexpense.ai.util.visible
import com.smartexpense.ai.data.ExpenseCategories
import androidx.navigation.fragment.findNavController

class TransactionsFragment : Fragment() {

    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var binding: FragmentTransactionsBinding

    private var selectedFilter: String? = null
    private val chipViews = mutableMapOf<String?, TextView>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("LifeCycleTest", "TransactionsFragment onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LifeCycleTest", "TransactionsFragment onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        Log.d("LifeCycleTest", "TransactionsFragment onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        setupFilters()
        observeData()
        updateFilterUI()
        Log.d("LifeCycleTest", "TransactionsFragment onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d("LifeCycleTest", "TransactionsFragment onStart")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d("LifeCycleTest", "TransactionsFragment onViewStateRestored")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LifeCycleTest", "TransactionsFragment onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LifeCycleTest", "TransactionsFragment onDestroyView")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifeCycleTest", "TransactionsFragment onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("LifeCycleTest", "TransactionsFragment onDetach")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifeCycleTest", "TransactionsFragment onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifeCycleTest", "TransactionsFragment onStop")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("LifeCycleTest", "TransactionsFragment onSaveInstanceState")
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { expense ->
            val bundle = android.os.Bundle().apply {
                putLong("expenseId", expense.id)
            }
            findNavController().navigate(R.id.action_transactions_to_add_expense, bundle)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        val filterChipsLayout = binding.filterChips

        chipViews.clear()

        val allChip = createFilterChip("All")
        filterChipsLayout.addView(allChip)
        chipViews[null] = allChip

        ExpenseCategories.ALL.forEach { category ->
            val chip = createFilterChip(category.label)
            filterChipsLayout.addView(chip)
            chipViews[category.key] = chip
        }

        chipViews.forEach { (category, chip) ->
            chip.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                selectedFilter = category
                viewModel.setCategoryFilter(category)
                updateFilterUI()
            }
        }
    }

    private fun createFilterChip(text: String): TextView {
        val chip = TextView(requireContext())
        val density = resources.displayMetrics.density
        
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            (40 * density).toInt()
        )
        params.marginEnd = (10 * density).toInt()
        chip.layoutParams = params
        
        chip.text = text
        chip.textSize = 13f
        chip.gravity = android.view.Gravity.CENTER
        
        val padding = (20 * density).toInt()
        chip.setPadding(padding, 0, padding, 0)
        
        return chip
    }

    private fun updateFilterUI() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.secondary_container)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.on_secondary_container)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.surface_container_highest)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
        val radius = resources.getDimension(R.dimen.size24)

        chipViews.forEach { (category, chip) ->
            if (category == selectedFilter) {
                BindingAdapterUtils.setBackground(chip, radius, selectedColor)
                chip.setTextColor(selectedTextColor)
                chip.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD))
            } else {
                BindingAdapterUtils.setBackground(chip, radius, unselectedColor)
                chip.setTextColor(unselectedTextColor)
                chip.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL))
            }
        }
    }

    private fun observeData() {
        viewModel.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses.isNullOrEmpty()) {
                binding.tvEmpty.visible()
                binding.rvTransactions.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvTransactions.visible()
                adapter.submitList(TransactionAdapter.groupByDate(expenses))
            }
        }
    }
}
