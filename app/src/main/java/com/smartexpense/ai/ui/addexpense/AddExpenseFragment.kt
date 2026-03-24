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
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var viewModel: AddExpenseViewModel
    private var selectedCategory = "Food"
    private var selectedPaymentMethod = "UPI"
    private var selectedDate = Calendar.getInstance()

    private val categoryViews = mutableMapOf<String, LinearLayout>()
    private val paymentViews = mutableMapOf<String, TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_expense, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        setupCategorySelection(view)
        setupPaymentMethodSelection(view)
        setupDatePicker(view)
        setupDoneButton(view)
    }

    private fun setupCategorySelection(view: View) {
        categoryViews["Food"] = view.findViewById(R.id.cat_food)
        categoryViews["Travel"] = view.findViewById(R.id.cat_travel)
        categoryViews["Bills"] = view.findViewById(R.id.cat_bills)
        categoryViews["Shopping"] = view.findViewById(R.id.cat_shopping)
        categoryViews["Other"] = view.findViewById(R.id.cat_other)

        categoryViews.forEach { (name, layout) ->
            layout.setOnClickListener {
                selectedCategory = name
                updateCategorySelection()
            }
        }

        // Default selection
        selectedCategory = "Food"
        updateCategorySelection()
    }

    private fun updateCategorySelection() {
        categoryViews.forEach { (name, layout) ->
            if (name == selectedCategory) {
                layout.setBackgroundResource(R.drawable.bg_category_selected)
            } else {
                layout.setBackgroundResource(R.drawable.bg_category_unselected)
            }
        }
    }

    private fun setupPaymentMethodSelection(view: View) {
        paymentViews["UPI"] = view.findViewById(R.id.chip_upi)
        paymentViews["Cash"] = view.findViewById(R.id.chip_cash)
        paymentViews["Card"] = view.findViewById(R.id.chip_card)

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
                chip.setTextColor(resources.getColor(R.color.primary, null))
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_unselected)
                chip.setTextColor(resources.getColor(R.color.on_surface_variant, null))
            }
        }
    }

    private fun setupDatePicker(view: View) {
        val tvDate = view.findViewById<TextView>(R.id.tv_selected_date)
        updateDateDisplay(tvDate)

        view.findViewById<View>(R.id.date_picker_container).setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateDisplay(tvDate)
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay(tvDate: TextView) {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvDate.text = format.format(selectedDate.time)
    }

    private fun setupDoneButton(view: View) {
        view.findViewById<View>(R.id.btn_done).setOnClickListener {
            val amountText = view.findViewById<EditText>(R.id.et_amount).text.toString()
            val note = view.findViewById<EditText>(R.id.et_note).text.toString()

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
}
