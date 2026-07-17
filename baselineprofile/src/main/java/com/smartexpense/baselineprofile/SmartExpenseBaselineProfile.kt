package com.smartexpense.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SmartExpenseBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = "com.smartexpense.ai") {

            // ── 1. App startup ────────────────────────────────────────────────
            pressHome()
            startActivityAndWait()  // records all code run during cold start

            // ── 2. Navigate to Dashboard (already on it, just wait for load) ─
            device.waitForIdle()

            // ── 3. Navigate to Transactions tab ──────────────────────────────
            device.findObject(
                androidx.test.uiautomator.By.desc("Transactions")
            )?.click()
            device.waitForIdle()

            // ── 4. Navigate to Analytics tab ─────────────────────────────────
            device.findObject(
                androidx.test.uiautomator.By.desc("Analytics")
            )?.click()
            device.waitForIdle()

            // ── 5. Navigate to Settings tab ───────────────────────────────────
            device.findObject(
                androidx.test.uiautomator.By.desc("Settings")
            )?.click()
            device.waitForIdle()
        }
    }
}
