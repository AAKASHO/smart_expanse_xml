package com.smartexpense.ai.ui.addexpense

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.smartexpense.ai.R
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.databinding.FragmentAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var viewModel: AddExpenseViewModel
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // Edit mode — set if an expenseId was passed via arguments
    private var editingExpenseId: Long = -1L
    private var isEditMode = false
    private var editingExpense: com.smartexpense.ai.data.db.Expense? = null

    private var selectedCategory = ExpenseCategories.DEFAULT
    private var selectedPaymentMethod = "UPI"
    private var selectedDate = Calendar.getInstance()

    private lateinit var categoryAdapter: CategoryAdapter
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

        // Determine mode from nav argument
        editingExpenseId = arguments?.getLong("expenseId", -1L) ?: -1L
        isEditMode = editingExpenseId != -1L

        setupAmountField()
        setupCategoryGrid()
        setupPaymentMethodSelection()
        setupDatePicker()
        setupDoneButton()

        if (isEditMode) {
            // Pre-fill all fields once the expense loads
            binding.tvScreenTitle.text = "Edit Transaction"
            binding.btnDone.text = "Save Changes"
            binding.btnDone.setIconResource(R.drawable.ic_edit)
            viewModel.loadExpense(editingExpenseId).observe(viewLifecycleOwner) { expense ->
                expense ?: return@observe
                editingExpense = expense
                prefillFields(expense)
            }
        } else {
            binding.tvScreenTitle.text = "Add Expense"
        }
    }

    private fun prefillFields(expense: Expense) {
        // Amount
        val amountText = "₹ ${String.format("%.2f", expense.amount)}"
        isFormatting = true
        binding.etAmount.setText(amountText)
        binding.etAmount.setSelection(amountText.length)
        isFormatting = false

        // Category
        selectedCategory = expense.category
        categoryAdapter.setSelected(expense.category)

        // Date
        selectedDate.timeInMillis = expense.date
        updateDateDisplay()

        // Payment method
        selectedPaymentMethod = expense.paymentMethod
        updatePaymentSelection()

        // Note
        binding.etNote.setText(expense.note)
    }

    // ─────────────────────────────────────────────────────────────────────
    private val PLACEHOLDER = "₹ 0.00"
    private var isFormatting = false

    private fun formatAmount(input: String): String {
        var cleanString = input.replace(Regex("[^\\d.]"), "")

        val firstDotIndex = cleanString.indexOf('.')
        if (firstDotIndex != -1) {
            val afterDot = cleanString.substring(firstDotIndex + 1).replace(".", "")
            cleanString = cleanString.substring(0, firstDotIndex + 1) + afterDot
        }

        val dotIndex = cleanString.indexOf('.')
        if (dotIndex != -1 && cleanString.length - dotIndex - 1 > 2) {
            cleanString = cleanString.substring(0, dotIndex + 3)
        }

        if (cleanString.startsWith("0") && cleanString.length > 1 && !cleanString.startsWith("0.")) {
            cleanString = cleanString.trimStart('0')
            if (cleanString.isEmpty() || cleanString.startsWith(".")) {
                cleanString = "0" + cleanString
            }
        } else if (cleanString == ".") {
            cleanString = "0."
        }

        if (cleanString.isEmpty()) return ""

        val value = cleanString.toDoubleOrNull() ?: 0.0
        if (value > 10000000.0) {
            cleanString = "10000000"
        }

        return "₹ $cleanString"
    }

    private fun setupAmountField() {
        val colorEntered = ContextCompat.getColor(requireContext(), R.color.primary)

        binding.etAmount.setText(PLACEHOLDER)
        binding.etAmount.setTextColor(colorEntered)

        binding.etAmount.setOnFocusChangeListener { _, hasFocus ->
            val current = binding.etAmount.text?.toString() ?: ""
            if (hasFocus) {
                if (current == PLACEHOLDER) {
                    binding.etAmount.setSelection(PLACEHOLDER.length)
                }
            } else {
                if (current.isBlank() || current == PLACEHOLDER || current == "₹" || current == "₹ ") {
                    isFormatting = true
                    binding.etAmount.setText(PLACEHOLDER)
                    isFormatting = false
                }
            }
        }

        var oldText = ""
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isFormatting) oldText = s?.toString() ?: ""
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                val rawText = s?.toString() ?: ""

                if (rawText == oldText) return

                var textToFormat = rawText
                if (oldText == PLACEHOLDER) {
                    if (rawText.length > oldText.length) {
                        textToFormat = rawText.removePrefix(PLACEHOLDER).removeSuffix(PLACEHOLDER)
                    } else if (rawText.length < oldText.length) {
                        if (PLACEHOLDER.contains(rawText)) {
                            textToFormat = ""
                        }
                    }
                }

                if (rawText.isEmpty() || rawText == "₹" || rawText == "₹ " || textToFormat.isEmpty()) {
                    isFormatting = true
                    binding.etAmount.setText(PLACEHOLDER)
                    binding.etAmount.setSelection(PLACEHOLDER.length)
                    isFormatting = false
                    return
                }

                val formatted = formatAmount(textToFormat)

                if (formatted.isEmpty()) {
                    isFormatting = true
                    binding.etAmount.setText(PLACEHOLDER)
                    binding.etAmount.setSelection(PLACEHOLDER.length)
                    isFormatting = false
                    return
                }

                isFormatting = true
                binding.etAmount.setText(formatted)
                binding.etAmount.setSelection(formatted.length)
                isFormatting = false
            }
        })
    }

    // ── Category grid (RecyclerView + GridLayoutManager) ─────────────
    private fun setupCategoryGrid() {
        categoryAdapter = CategoryAdapter(ExpenseCategories.ALL) { category ->
            selectedCategory = category.key
        }
        categoryAdapter.setSelected(selectedCategory)

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
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
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.surface_variant)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)
        val grey = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
        val radius = resources.getDimension(R.dimen.size12)

        paymentViews.forEach { (method, chip) ->
            if (method == selectedPaymentMethod) {
                com.smartexpense.ai.util.BindingAdapterUtils.setBackground(chip, radius, selectedColor)
                chip.setTextColor(white)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                com.smartexpense.ai.util.BindingAdapterUtils.setBackground(chip, radius, unselectedColor)
                chip.setTextColor(grey)
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
            val amountText = binding.etAmount.text.toString().trim()

            if (amountText.isBlank() || amountText == PLACEHOLDER) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cleanAmountText = amountText.replace(Regex("[^\\d.]"), "")
            val amount = cleanAmountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = binding.etNote.text.toString()

            if (isEditMode) {
                // Use the already-loaded expense — no new observer needed
                val original = editingExpense
                if (original == null) {
                    Toast.makeText(requireContext(), "Loading transaction…", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val updated = original.copy(
                    amount = amount,
                    category = selectedCategory,
                    note = note,
                    date = selectedDate.timeInMillis,
                    paymentMethod = selectedPaymentMethod
                )
                viewModel.updateExpense(updated)
                Toast.makeText(requireContext(), "Transaction updated ✅", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_transactions)
            } else {
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
                findNavController().navigate(R.id.nav_dashboard)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
