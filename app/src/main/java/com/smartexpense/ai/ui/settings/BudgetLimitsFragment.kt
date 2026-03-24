package com.smartexpense.ai.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.smartexpense.ai.databinding.FragmentBudgetLimitsBinding
import com.smartexpense.ai.util.CurrencyFormatter
import kotlinx.coroutines.launch

class BudgetLimitsFragment : Fragment() {

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
                binding.tvCurrentBudget.text = "₹${CurrencyFormatter.format(limit)}"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveBudget.setOnClickListener {
            val newAmountStr = binding.etBudgetAmount.text.toString()
            if (newAmountStr.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = newAmountStr.toDoubleOrNull()
            if (amount != null && amount > 0) {
                viewModel.updateBudget(amount)
                Toast.makeText(requireContext(), "Budget Limits Updated!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
