package com.smartexpense.ai.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.dataStoreFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartexpense.ai.R
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.data.preferences.UserPreferencesRepository
import com.smartexpense.ai.service.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var userPreferences: UserPreferencesRepository
    private lateinit var notificationHelper: NotificationHelper

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            lifecycleScope.launch { userPreferences.setSmsParsingEnabled(true) }
            Toast.makeText(requireContext(), "SMS parsing enabled ✅", Toast.LENGTH_SHORT).show()
        } else {
            binding.switchSms.isChecked = false
            Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private var _binding: com.smartexpense.ai.databinding.FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = com.smartexpense.ai.databinding.FragmentSettingsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireContext().applicationContext as SmartExpenseApp
        userPreferences = app.userPreferences
        notificationHelper = NotificationHelper(requireContext())

        setupToggles()
        setupButtons()
    }

    private fun setupToggles() {
        // ── Observe preferences from DataStore (Flow → never blocks main thread) ──
        lifecycleScope.launch {
            userPreferences.isSmsParsingEnabled.collectLatest { enabled ->
                binding.switchSms.isChecked = enabled
            }
        }
        lifecycleScope.launch {
            userPreferences.isBudgetAlertsEnabled.collectLatest { enabled ->
                binding.switchBudgetAlerts.isChecked = enabled
            }
        }
        lifecycleScope.launch {
            userPreferences.isDailyRemindersEnabled.collectLatest { enabled ->
                binding.switchReminders.isChecked = enabled
            }
        }

        // SMS Parsing toggle
        binding.switchSms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestSmsPermission()
            } else {
                lifecycleScope.launch { userPreferences.setSmsParsingEnabled(false) }
            }
        }

        // Budget Alerts toggle
        binding.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { userPreferences.setBudgetAlertsEnabled(isChecked) }
            if (isChecked) requestNotificationPermission()
        }

        // Daily Reminders toggle
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { userPreferences.setDailyRemindersEnabled(isChecked) }
            if (isChecked) {
                requestNotificationPermission()
                notificationHelper.scheduleDailyReminder()
                Toast.makeText(requireContext(), "Daily reminders enabled at 8 PM", Toast.LENGTH_SHORT).show()
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }

    }

    private fun setupButtons() {
        // Google Sign-In
        binding.btnGoogleSignin.setOnClickListener {
            Toast.makeText(requireContext(), "Google Sign-In requires Firebase setup. Add google-services.json first.", Toast.LENGTH_LONG).show()
        }

        // Manage Categories
        binding.btnManageCategories.setOnClickListener {
            findNavController().navigate(R.id.nav_manage_categories)
        }

        // Manage Budget Limit
        binding.btnManageBudget.setOnClickListener {
            findNavController().navigate(R.id.nav_budget_limits)
        }

        // Export CSV
        binding.btnExport.setOnClickListener {
            exportToCsv()
        }

        // Clear Cache
        binding.btnClearCache.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All Data?")
                .setMessage("This will delete all your expense records. This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear") { _, _ ->
                    // Clear database
                    Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        ) {
            lifecycleScope.launch { userPreferences.setSmsParsingEnabled(true) }
        } else {
            smsPermissionLauncher.launch(
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun exportToCsv() {
        val app = requireContext().applicationContext as SmartExpenseApp

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = withContext(Dispatchers.IO) {
                    app.useCases.getAllExpenses().first()
                }

                if (expenses.isEmpty()) {
                    Toast.makeText(requireContext(), "No expenses to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val csv = buildString {
                    appendLine("Date,Category,Merchant,Amount,PaymentMethod,Note,AutoSynced")
                    expenses.forEach { exp ->
                        appendLine("${dateFormat.format(Date(exp.date))},${exp.category},${exp.merchant},${exp.amount},${exp.paymentMethod},\"${exp.note}\",${exp.isAutoSynced}")
                    }
                }

                val fileName = "smart_expense_export_${System.currentTimeMillis()}.csv"
                val file = File(requireContext().getExternalFilesDir(null), fileName)
                withContext(Dispatchers.IO) {
                    file.writeText(csv)
                }

                Toast.makeText(requireContext(), "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()

                // Share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Smart Expense Export")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                startActivity(Intent.createChooser(shareIntent, "Share CSV"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
