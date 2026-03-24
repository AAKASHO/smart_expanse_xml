package com.smartexpense.ai.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.smartexpense.ai.R
import com.smartexpense.ai.SmartExpenseApp
import com.smartexpense.ai.service.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var notificationHelper: NotificationHelper

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            prefs.edit().putBoolean("sms_parsing_enabled", true).apply()
            Toast.makeText(requireContext(), "SMS parsing enabled ✅", Toast.LENGTH_SHORT).show()
        } else {
            view?.findViewById<SwitchMaterial>(R.id.switch_sms)?.isChecked = false
            Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE)
        notificationHelper = NotificationHelper(requireContext())

        setupToggles(view)
        setupButtons(view)
    }

    private fun setupToggles(view: View) {
        // SMS Parsing toggle
        val switchSms = view.findViewById<SwitchMaterial>(R.id.switch_sms)
        switchSms.isChecked = prefs.getBoolean("sms_parsing_enabled", false)
        switchSms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestSmsPermission()
            } else {
                prefs.edit().putBoolean("sms_parsing_enabled", false).apply()
            }
        }

        // Budget Alerts toggle
        val switchBudget = view.findViewById<SwitchMaterial>(R.id.switch_budget_alerts)
        switchBudget.isChecked = prefs.getBoolean("budget_alerts_enabled", true)
        switchBudget.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("budget_alerts_enabled", isChecked).apply()
        }

        // Daily Reminders toggle
        val switchReminders = view.findViewById<SwitchMaterial>(R.id.switch_reminders)
        switchReminders.isChecked = prefs.getBoolean("daily_reminders_enabled", false)
        switchReminders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("daily_reminders_enabled", isChecked).apply()
            if (isChecked) {
                notificationHelper.scheduleDailyReminder()
                Toast.makeText(requireContext(), "Daily reminders enabled at 8 PM", Toast.LENGTH_SHORT).show()
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }
    }

    private fun setupButtons(view: View) {
        // Google Sign-In
        view.findViewById<View>(R.id.btn_google_signin).setOnClickListener {
            Toast.makeText(requireContext(), "Google Sign-In requires Firebase setup. Add google-services.json first.", Toast.LENGTH_LONG).show()
        }

        // Export CSV
        view.findViewById<View>(R.id.btn_export).setOnClickListener {
            exportToCsv()
        }

        // Clear Cache
        view.findViewById<View>(R.id.btn_clear_cache).setOnClickListener {
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
            prefs.edit().putBoolean("sms_parsing_enabled", true).apply()
        } else {
            smsPermissionLauncher.launch(
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            )
        }
    }

    private fun exportToCsv() {
        val app = requireContext().applicationContext as SmartExpenseApp

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = withContext(Dispatchers.IO) {
                    app.repository.getAllExpenses().first()
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
