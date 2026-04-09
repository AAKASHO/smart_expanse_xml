package com.smartexpense.ai.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smartexpense.ai.databinding.FragmentBudgetLimitsBinding
import com.smartexpense.ai.util.CurrencyFormatter
import kotlinx.coroutines.launch

class BudgetLimitsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBudgetLimitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BudgetLimitsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetLimitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[BudgetLimitsViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentBudget.collect { budget ->
                val limit = budget?.monthlyLimit ?: 30000.0
                binding.etBudgetAmount.setText(limit.toInt().toString())
                
                val prefs = requireContext().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                val savedDailyLimit = prefs.getFloat("daily_budget_limit", -1f)
                val dailyLimit = if (savedDailyLimit > 0) savedDailyLimit.toInt() else (limit / 30).toInt().coerceIn(100, 5000)
                
                binding.sliderDailyLimit.value = dailyLimit.toFloat()
                binding.tvDailyLimitValue.text = "₹${CurrencyFormatter.format(dailyLimit.toDouble())}"
            }
        }
    }

    private fun setupClickListeners() {
        binding.sliderDailyLimit.addOnChangeListener { _, value, _ ->
            binding.tvDailyLimitValue.text = "₹${CurrencyFormatter.format(value.toDouble())}"
        }

        binding.btnSaveBudget.setOnClickListener {
            val newAmountStr = binding.etBudgetAmount.text.toString()
            if (newAmountStr.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = newAmountStr.toDoubleOrNull()
            if (amount != null && amount > 0) {
                viewModel.updateBudget(amount)
                
                // Save the daily limit choice
                val prefs = requireContext().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
                prefs.edit().putFloat("daily_budget_limit", binding.sliderDailyLimit.value).apply()
                
                Toast.makeText(requireContext(), "Budget Updated!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
