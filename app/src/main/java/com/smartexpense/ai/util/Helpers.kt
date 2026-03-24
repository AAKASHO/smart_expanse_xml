package com.smartexpense.ai.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object CurrencyFormatter {
    private val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun format(amount: Double): String {
        return indianFormat.format(amount)
    }

    fun formatWithSymbol(amount: Double): String {
        return "₹${format(amount)}"
    }
}

object DateFormatter {
    private val fullFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    fun formatDate(timestamp: Long): String = fullFormat.format(Date(timestamp))

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Date(timestamp))

    fun getRelativeDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, then) -> "Today"
            isYesterday(now, then) -> "Yesterday"
            isSameWeek(now, then) -> dayFormat.format(Date(timestamp))
            else -> fullFormat.format(Date(timestamp))
        }
    }

    fun getGroupHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, then) -> "Today"
            isYesterday(now, then) -> "Yesterday"
            else -> fullFormat.format(Date(timestamp)).uppercase()
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, then: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, then)
    }

    private fun isSameWeek(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)
    }
}

object CategoryHelper {
    data class CategoryInfo(
        val name: String,
        val icon: Int,
        val colorResId: Int
    )

    val categories = listOf("Food", "Travel", "Bills", "Shopping", "Other")

    fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "food" -> "restaurant"
            "travel" -> "commute"
            "bills" -> "receipt_long"
            "shopping" -> "shopping_bag"
            "other" -> "more_horiz"
            else -> "attach_money"
        }
    }
}
