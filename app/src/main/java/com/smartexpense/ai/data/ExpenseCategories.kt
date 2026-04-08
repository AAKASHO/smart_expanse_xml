package com.smartexpense.ai.data

import androidx.annotation.DrawableRes
import com.smartexpense.ai.R

/**
 * Data class representing an expense category.
 * Used by the Add Expense screen to display available categories.
 *
 * To add/remove categories, simply edit the [ExpenseCategories.ALL] list.
 * The UI (RecyclerView + GridLayoutManager) inflates dynamically from this list,
 * so no layout changes are required.
 */
data class ExpenseCategory(
    val key: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

object ExpenseCategories {

    /** Default selected category key */
    const val DEFAULT = "Food"

    /** Master list of expense categories. Edit this to add/remove categories. */
    val ALL: List<ExpenseCategory> = listOf(
        ExpenseCategory("Food",     "Food",     R.drawable.ic_food),
        ExpenseCategory("Travel",   "Travel",   R.drawable.ic_travel),
        ExpenseCategory("Bills",    "Bills",    R.drawable.ic_bills),
        ExpenseCategory("Shopping", "Shopping", R.drawable.ic_shopping),
        ExpenseCategory("Other",    "Other",    R.drawable.ic_other)
    )
}
