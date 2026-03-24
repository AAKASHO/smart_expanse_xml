package com.smartexpense.ai.ui.addexpense

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smartexpense.ai.R
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.databinding.FragmentAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var viewModel: AddExpenseViewModel
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory = "Food"
    private var selectedPaymentMethod = "UPI"
    private var selectedDate = Calendar.getInstance()

    private val categoryViews = mutableMapOf<String, LinearLayout>()
    private val paymentViews = mutableMapOf<String, TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        setupCategorySelection()
        setupPaymentMethodSelection()
        setupDatePicker()
        setupDoneButton()
    }

    private fun setupCategorySelection() {
        categoryViews["Food"] = binding.catFood
        categoryViews["Travel"] = binding.catTravel
        categoryViews["Bills"] = binding.catBills
        categoryViews["Shopping"] = binding.catShopping
        categoryViews["Other"] = binding.catOther

        categoryViews.forEach { (name, layout) ->
            layout.setOnClickListener {
                selectedCategory = name
                updateCategorySelection()
            }
        }

        // Default selection
        updateCategorySelection()
    }

    private fun updateCategorySelection() {
        categoryViews.forEach { (name, layout) ->
            if (name == selectedCategory) {
                layout.setBackgroundResource(R.drawable.bg_category_selected)
                // Need to update child views tint/color manually as before
                (layout.getChildAt(0) as? android.widget.ImageView)?.setColorFilter(resources.getColor(android.R.color.white, null))
                (layout.getChildAt(1) as? TextView)?.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                layout.setBackgroundResource(R.drawable.bg_category_unselected)
                (layout.getChildAt(0) as? android.widget.ImageView)?.setColorFilter(resources.getColor(R.color.primary, null))
                (layout.getChildAt(1) as? TextView)?.setTextColor(resources.getColor(R.color.on_surface_variant, null))
            }
        }
    }

    private fun setupPaymentMethodSelection() {
        paymentViews["UPI"] = binding.chipUpi
        paymentViews["Cash"] = binding.chipCash
        paymentViews["Card"] = binding.chipCard

        paymentViews.forEach { (method, chip) ->
            chip.setOnClickListener {
                selectedPaymentMethod = method
                updatePaymentSelection()
            }
        }
        updatePaymentSelection()
    }

    private fun updatePaymentSelection() {
        paymentViews.forEach { (method, chip) ->
            if (method == selectedPaymentMethod) {
                chip.setBackgroundResource(R.drawable.bg_category_selected)
                chip.setTextColor(resources.getColor(android.R.color.white, null))
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_unselected)
                chip.setTextColor(resources.getColor(R.color.on_surface_variant, null))
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun setupDatePicker() {
        updateDateDisplay()

        binding.datePickerContainer.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateDisplay()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = format.format(selectedDate.time)
    }

    private fun setupDoneButton() {
        binding.btnDone.setOnClickListener {
            val amountText = binding.etAmount.text.toString()
            val note = binding.etNote.text.toString()

            if (amountText.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                amount = amount,
                category = selectedCategory,
                merchant = "",
                note = note,
                date = selectedDate.timeInMillis,
                paymentMethod = selectedPaymentMethod,
                isAutoSynced = false
            )

            viewModel.addExpense(expense)
            Toast.makeText(requireContext(), "Expense added! ✅", Toast.LENGTH_SHORT).show()

            // Navigate back to dashboard
            findNavController().navigate(R.id.nav_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
