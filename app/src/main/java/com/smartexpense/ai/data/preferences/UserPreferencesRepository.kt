package com.smartexpense.ai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single DataStore instance tied to the app Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "smart_expense_prefs"
)

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    // ─── Keys ──────────────────────────────────────────────────────────────────

    private object Keys {
        val SMS_PARSING_ENABLED    = booleanPreferencesKey("sms_parsing_enabled")
        val BUDGET_ALERTS_ENABLED  = booleanPreferencesKey("budget_alerts_enabled")
        val DAILY_REMINDERS_ENABLED = booleanPreferencesKey("daily_reminders_enabled")
    }

    // ─── Reads (Flow — never blocks main thread) ────────────────────────────────

    val isSmsParsingEnabled: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[Keys.SMS_PARSING_ENABLED] ?: false }

    val isBudgetAlertsEnabled: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[Keys.BUDGET_ALERTS_ENABLED] ?: true }

    val isDailyRemindersEnabled: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[Keys.DAILY_REMINDERS_ENABLED] ?: false }

    // ─── Writes (suspend — runs on IO thread automatically) ────────────────────

    suspend fun setSmsParsingEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.SMS_PARSING_ENABLED] = enabled }
    }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.BUDGET_ALERTS_ENABLED] = enabled }
    }

    suspend fun setDailyRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DAILY_REMINDERS_ENABLED] = enabled }
    }
}
