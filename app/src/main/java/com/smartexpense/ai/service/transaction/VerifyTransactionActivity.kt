package com.smartexpense.ai.service.transaction

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartexpense.ai.R
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.data.ExpenseCategory
import com.smartexpense.ai.data.model.CustomCategoryEntity
import com.smartexpense.ai.databinding.ActivityVerifyTransactionBinding
import com.smartexpense.ai.service.notification.DeletePendingExpenseBroadcastReceiver
import com.smartexpense.ai.service.notification.NotificationHelper
import com.smartexpense.ai.ui.addexpense.CategoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Transparent overlay Activity launched when the user taps
 * "Yes, Categorize" in the SMS transaction notification.
 *
 * Loads the pending expense, lets the user pick/create a category,
 * then confirms (saves) or discards (deletes) it.
 */
class VerifyTransactionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    private lateinit var binding: ActivityVerifyTransactionBinding
    private var expenseId: Long = -1L
    private var notificationId: Int = -1
    private var selectedCategory = ExpenseCategories.DEFAULT

    private lateinit var categoryAdapter: CategoryAdapter
    private val allCategories = mutableListOf<ExpenseCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L)
        notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (expenseId == -1L) {
            finish()
            return
        }

        // Dismiss the notification if launched from it
        if (notificationId != -1) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)
        }

        setupCategoryGrid()
        loadExpenseDetails()
        setupButtons()

        // Tap outside the panel to dismiss
        binding.root.setOnClickListener { finish() }
        binding.verifyPanel.setOnClickListener { /* consume touch, don't dismiss */ }
    }

    private fun setupCategoryGrid() {
        val app = application as SmartExpenseApp
        // Start with built-in categories
        allCategories.addAll(ExpenseCategories.ALL)
        selectedCategory = ExpenseCategories.DEFAULT

        categoryAdapter = CategoryAdapter(allCategories) { category ->
            selectedCategory = category.key
        }
        categoryAdapter.setSelected(selectedCategory)

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@VerifyTransactionActivity, 3)
            adapter = categoryAdapter
        }

        // Observe custom categories and merge dynamically
        lifecycleScope.launch {
            app.useCases.getCustomCategories().collect { customList ->
                val merged = mutableListOf<ExpenseCategory>()
                merged.addAll(ExpenseCategories.ALL)
                customList.forEach { entity ->
                    merged.add(
                        ExpenseCategory(
                            key = entity.key,
                            label = entity.label,
                            iconRes = R.drawable.ic_other // fallback icon for custom
                        )
                    )
                }
                allCategories.clear()
                allCategories.addAll(merged)
                categoryAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadExpenseDetails() {
        val app = application as SmartExpenseApp
        lifecycleScope.launch {
            val expense = withContext(Dispatchers.IO) {
                app.useCases.getExpenseById(expenseId).firstOrNull()
            }
            if (expense == null || !expense.isPending) {
                Toast.makeText(this@VerifyTransactionActivity, "Transaction not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            binding.tvAmount.text = "₹%.2f".format(expense.amount)
            binding.tvMerchant.text = if (expense.merchant.isNotBlank()) expense.merchant else "Unknown Merchant"
            binding.tvPaymentMethod.text = expense.paymentMethod

            // Pre-select the auto-guessed category if it exists in our list
            if (allCategories.any { it.key == expense.category }) {
                selectedCategory = expense.category
                categoryAdapter.setSelected(selectedCategory)
            }
        }
    }

    private fun setupButtons() {
        val app = application as SmartExpenseApp

        // + New Category
        binding.chipAddCategory.setOnClickListener {
            showAddCategoryDialog(app)
        }

        // Confirm
        binding.btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                val expense = withContext(Dispatchers.IO) {
                    app.useCases.getExpenseById(expenseId).firstOrNull()
                } ?: return@launch

                val confirmed = expense.copy(
                    category = selectedCategory,
                    isPending = false
                )
                withContext(Dispatchers.IO) {
                    app.useCases.updateExpense(confirmed)
                    NotificationHelper(this@VerifyTransactionActivity)
                        .checkBudgetAndNotify(app.useCases)
                }

                Toast.makeText(this@VerifyTransactionActivity, "Transaction saved ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Discard
        binding.btnDiscard.setOnClickListener {
            lifecycleScope.launch {
                val expense = withContext(Dispatchers.IO) {
                    app.useCases.getExpenseById(expenseId).firstOrNull()
                }
                if (expense != null) {
                    withContext(Dispatchers.IO) { app.useCases.deleteExpense(expense) }
                }
                Toast.makeText(this@VerifyTransactionActivity, "Transaction discarded", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showAddCategoryDialog(app: SmartExpenseApp) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_category, null)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_category))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val emoji = dialogView.findViewById<EditText>(R.id.et_emoji).text
                    .toString().trim().ifEmpty { "📦" }
                val name = dialogView.findViewById<EditText>(R.id.et_category_name).text
                    .toString().trim()
                if (name.isNotBlank()) {
                    lifecycleScope.launch {
                        app.useCases.addCustomCategory(
                            CustomCategoryEntity(key = name, label = name, emoji = emoji)
                        )
                    }
                }
            }
            .setNegativeButton(getString(R.string.discard), null)
            .show()
    }
}
