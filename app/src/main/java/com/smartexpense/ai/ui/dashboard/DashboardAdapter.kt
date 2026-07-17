package com.smartexpense.ai.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.data.ExpenseCategory
import com.smartexpense.ai.databinding.ItemDashboardCategoryCardBinding
import com.smartexpense.ai.databinding.ItemDashboardCategoryRowBinding
import com.smartexpense.ai.databinding.ItemDashboardEmptyBinding
import com.smartexpense.ai.databinding.ItemDashboardHeaderBinding
import com.smartexpense.ai.databinding.ItemDashboardHeroBinding
import com.smartexpense.ai.databinding.ItemDashboardInsightsBinding
import com.smartexpense.ai.databinding.ItemDashboardSectionTitleBinding
import com.smartexpense.ai.databinding.ItemDashboardViewAllBinding
import com.smartexpense.ai.databinding.ItemTransactionSimpleBinding
import com.smartexpense.ai.domain.model.CategoryTotal
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.service.insights.InsightCard
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter

// ─── Sealed item model ────────────────────────────────────────────────────────

sealed class DashboardItem {
    /** App header bar (logo + settings) */
    object Header : DashboardItem()

    /** Monthly spending hero card */
    data class HeroCard(
        val spending: Double,
        val budget: Double
    ) : DashboardItem()

    /** Section title label, with an optional clickable action */
    data class SectionTitle(
        val title: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : DashboardItem()

    /** Horizontal insights carousel */
    data class InsightsCarousel(val insights: List<InsightCard>) : DashboardItem()

    /**
     * A TWO-COLUMN category budget row.
     * [left] is always present; [right] may be null for odd-count lists.
     */
    data class CategoryRow(
        val left: ExpenseCategory,
        val right: ExpenseCategory?,
        val categoryTotals: List<CategoryTotal>,
        val limits: Map<String, Double>
    ) : DashboardItem()

    /** Single recent-transaction row */
    data class TransactionRow(val expense: Expense) : DashboardItem()

    /** "View All Transactions" button */
    data class ViewAll(val onClick: () -> Unit) : DashboardItem()

    /** No-transactions placeholder */
    object EmptyState : DashboardItem()
}

// ─── View-type constants ───────────────────────────────────────────────────────

private const val TYPE_HEADER          = 0
private const val TYPE_HERO            = 1
private const val TYPE_SECTION_TITLE   = 2
private const val TYPE_INSIGHTS        = 3
private const val TYPE_CATEGORY_ROW    = 4
private const val TYPE_TRANSACTION_ROW = 5
private const val TYPE_VIEW_ALL        = 6
private const val TYPE_EMPTY_STATE     = 7

// ─── DiffUtil callback ────────────────────────────────────────────────────────

class DashboardDiffCallback : DiffUtil.ItemCallback<DashboardItem>() {

    override fun areItemsTheSame(old: DashboardItem, new: DashboardItem): Boolean = when {
        old is DashboardItem.Header        && new is DashboardItem.Header        -> true
        old is DashboardItem.HeroCard      && new is DashboardItem.HeroCard      -> true
        old is DashboardItem.SectionTitle  && new is DashboardItem.SectionTitle  -> old.title == new.title
        old is DashboardItem.InsightsCarousel && new is DashboardItem.InsightsCarousel -> true
        old is DashboardItem.CategoryRow   && new is DashboardItem.CategoryRow   -> old.left.key == new.left.key
        old is DashboardItem.TransactionRow && new is DashboardItem.TransactionRow -> old.expense.id == new.expense.id
        old is DashboardItem.ViewAll       && new is DashboardItem.ViewAll       -> true
        old is DashboardItem.EmptyState    && new is DashboardItem.EmptyState    -> true
        else -> false
    }

    override fun areContentsTheSame(old: DashboardItem, new: DashboardItem) = old == new
}

// ─── Adapter ──────────────────────────────────────────────────────────────────

class DashboardAdapter(
    private val onProfileClick: () -> Unit,
    private val onHeroClick: () -> Unit
) : androidx.recyclerview.widget.ListAdapter<DashboardItem, RecyclerView.ViewHolder>(DashboardDiffCallback()) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is DashboardItem.Header          -> TYPE_HEADER
        is DashboardItem.HeroCard        -> TYPE_HERO
        is DashboardItem.SectionTitle    -> TYPE_SECTION_TITLE
        is DashboardItem.InsightsCarousel -> TYPE_INSIGHTS
        is DashboardItem.CategoryRow     -> TYPE_CATEGORY_ROW
        is DashboardItem.TransactionRow  -> TYPE_TRANSACTION_ROW
        is DashboardItem.ViewAll         -> TYPE_VIEW_ALL
        is DashboardItem.EmptyState      -> TYPE_EMPTY_STATE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER          -> HeaderVH(ItemDashboardHeaderBinding.inflate(inflater, parent, false))
            TYPE_HERO            -> HeroVH(ItemDashboardHeroBinding.inflate(inflater, parent, false))
            TYPE_SECTION_TITLE   -> SectionTitleVH(ItemDashboardSectionTitleBinding.inflate(inflater, parent, false))
            TYPE_INSIGHTS        -> InsightsVH(ItemDashboardInsightsBinding.inflate(inflater, parent, false))
            TYPE_CATEGORY_ROW    -> CategoryRowVH(ItemDashboardCategoryRowBinding.inflate(inflater, parent, false))
            TYPE_TRANSACTION_ROW -> TransactionVH(ItemTransactionSimpleBinding.inflate(inflater, parent, false))
            TYPE_VIEW_ALL        -> ViewAllVH(ItemDashboardViewAllBinding.inflate(inflater, parent, false))
            TYPE_EMPTY_STATE     -> EmptyVH(ItemDashboardEmptyBinding.inflate(inflater, parent, false))
            else -> throw IllegalStateException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DashboardItem.Header          -> (holder as HeaderVH).bind()
            is DashboardItem.HeroCard        -> (holder as HeroVH).bind(item)
            is DashboardItem.SectionTitle    -> (holder as SectionTitleVH).bind(item)
            is DashboardItem.InsightsCarousel -> (holder as InsightsVH).bind(item)
            is DashboardItem.CategoryRow     -> (holder as CategoryRowVH).bind(item)
            is DashboardItem.TransactionRow  -> (holder as TransactionVH).bind(item.expense)
            is DashboardItem.ViewAll         -> (holder as ViewAllVH).bind(item)
            is DashboardItem.EmptyState      -> Unit // static, no binding needed
        }
    }

    // ── ViewHolders ────────────────────────────────────────────────────────────

    inner class HeaderVH(private val b: ItemDashboardHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind() {
            b.ivProfile.setOnClickListener { onProfileClick() }
        }
    }

    inner class HeroVH(private val b: ItemDashboardHeroBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DashboardItem.HeroCard) {
            val spending = item.spending
            val budget   = item.budget
            val pct      = if (budget > 0) ((spending / budget) * 100).toInt().coerceAtMost(100) else 0
            val remaining = (budget - spending).coerceAtLeast(0.0)

            b.tvSpendingAmount.text   = "₹${CurrencyFormatter.format(spending)}"
            b.tvBudgetDivider.text    = " / ₹${CurrencyFormatter.format(budget)}"
            b.tvBudgetPercentage.text = "$pct% of budget used"
            b.tvRemaining.text        = "₹${CurrencyFormatter.format(remaining)} Remaining"
            b.progressBudget.progress = pct

            b.heroCard.setOnClickListener { onHeroClick() }
        }
    }

    inner class SectionTitleVH(private val b: ItemDashboardSectionTitleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DashboardItem.SectionTitle) {
            b.tvSectionTitle.text = item.title
            if (item.actionLabel != null && item.onAction != null) {
                b.tvSectionAction.visibility = View.VISIBLE
                b.tvSectionAction.text       = item.actionLabel
                b.tvSectionAction.setOnClickListener { item.onAction.invoke() }
            } else {
                b.tvSectionAction.visibility = View.GONE
            }
        }
    }

    inner class InsightsVH(private val b: ItemDashboardInsightsBinding) : RecyclerView.ViewHolder(b.root) {
        private val insightAdapter = InsightAdapter()
        private var snapAttached = false

        init {
            b.rvInsightsInner.adapter = insightAdapter
        }

        fun bind(item: DashboardItem.InsightsCarousel) {
            // Attach snap helper only once
            if (!snapAttached) {
                LinearSnapHelper().attachToRecyclerView(b.rvInsightsInner)
                snapAttached = true
            }
            insightAdapter.submitList(item.insights)
            val hasInsights = item.insights.isNotEmpty()
            b.rvInsightsInner.visibility = if (hasInsights) View.VISIBLE else View.GONE
            b.tvNoInsights.visibility    = if (hasInsights) View.GONE   else View.VISIBLE
        }
    }

    inner class CategoryRowVH(private val b: ItemDashboardCategoryRowBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DashboardItem.CategoryRow) {
            bindCard(b.cardLeft, item.left, item.categoryTotals, item.limits)

            if (item.right != null) {
                b.cardRight.root.visibility = View.VISIBLE
                bindCard(b.cardRight, item.right, item.categoryTotals, item.limits)
            } else {
                b.cardRight.root.visibility = View.INVISIBLE
            }
        }

        private fun bindCard(
            card: ItemDashboardCategoryCardBinding,
            category: ExpenseCategory,
            categoryTotals: List<CategoryTotal>,
            limits: Map<String, Double>
        ) {
            val ctx   = card.root.context
            val spent = categoryTotals.find { it.category == category.label }?.total ?: 0.0
            val limit = limits[category.label]

            // Icon drawable
            card.ivCatIcon.setImageResource(category.iconRes)

            // Icon background colour derived from category
            val (bgColor, tintColor) = categoryColorPair(category.key)
            val bgDrawable = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = 10f * ctx.resources.displayMetrics.density
                setColor(Color.parseColor(bgColor))
            }
            card.ivCatIcon.background      = bgDrawable
            card.ivCatIcon.imageTintList   = ColorStateList.valueOf(Color.parseColor(tintColor))

            // Name + status
            card.tvCatName.text = category.label
            if (limit != null && limit > 0) {
                card.tvCatStatus.text      = "₹${CurrencyFormatter.format(spent)} / ₹${CurrencyFormatter.format(limit)}"
                card.progressCat.visibility = View.VISIBLE
                card.progressCat.progress   = ((spent / limit) * 100).toInt().coerceAtMost(100)
            } else {
                card.tvCatStatus.text       = "₹${CurrencyFormatter.format(spent)} spent"
                card.progressCat.visibility = View.INVISIBLE
            }
        }

        /** Returns (backgroundHex, iconTintHex) per category key */
        private fun categoryColorPair(key: String): Pair<String, String> = when (key) {
            "Food"     -> "#FFF4E6" to "#FF9800"
            "Travel"   -> "#E1F5FE" to "#03A9F4"
            "Bills"    -> "#E8F5E9" to "#4CAF50"
            "Shopping" -> "#F3E5F5" to "#9C27B0"
            else       -> "#F5F5F5" to "#757575"
        }
    }

    inner class TransactionVH(private val b: ItemTransactionSimpleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(expense: Expense) {
            val iconRes = ExpenseCategories.ALL.find { it.label == expense.category }?.iconRes
                ?: R.drawable.ic_other
            b.ivCategoryIcon.setImageResource(iconRes)
            b.tvMerchant.text = expense.merchant.ifEmpty { expense.category }
            b.tvDetails.text  = "${DateFormatter.getRelativeDate(expense.date)} • ${expense.category}"
            b.tvAmount.text   = "- ₹${CurrencyFormatter.format(expense.amount)}"
        }
    }

    inner class ViewAllVH(private val b: ItemDashboardViewAllBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DashboardItem.ViewAll) {
            b.tvViewAll.setOnClickListener { item.onClick() }
        }
    }

    inner class EmptyVH(b: ItemDashboardEmptyBinding) : RecyclerView.ViewHolder(b.root)
}

// ─── Helper: build item list from raw data ─────────────────────────────────────

/**
 * Assembles the ordered list of [DashboardItem]s to submit to [DashboardAdapter].
 *
 * Category limits are kept here for now; move to ViewModel/DB when budget-by-category
 * becomes user-configurable.
 */
fun buildDashboardItems(
    spending: Double,
    budget: Double,
    insights: List<InsightCard>,
    categoryTotals: List<CategoryTotal>,
    recentExpenses: List<Expense>,
    onViewAllInsights: () -> Unit,
    onViewAllTransactions: () -> Unit
): List<DashboardItem> {

    val limits = mapOf(
        "Food"     to 5000.0,
        "Travel"   to 3000.0,
        "Bills"    to 10000.0,
        "Shopping" to 7000.0
        // "Other" intentionally has no limit — renders with just spent amount
    )

    val items = mutableListOf<DashboardItem>()

    // 1. Header
    items += DashboardItem.Header

    // 2. Hero card
    items += DashboardItem.HeroCard(spending, budget)

    // 3. AI Insights section
    items += DashboardItem.SectionTitle(
        title       = "AI Insights",
        actionLabel = "View All",
        onAction    = onViewAllInsights
    )
    items += DashboardItem.InsightsCarousel(insights)

    // 4. Recent Activity section header
    items += DashboardItem.SectionTitle(title = "Recent Activity")

    // 5. Category budget rows — driven by ExpenseCategories.ALL in pairs
    val categories = ExpenseCategories.ALL
    val pairs = categories.chunked(2)
    pairs.forEach { pair ->
        items += DashboardItem.CategoryRow(
            left           = pair[0],
            right          = pair.getOrNull(1),
            categoryTotals = categoryTotals,
            limits         = limits
        )
    }

    // 6. Recent transactions (or empty state)
    if (recentExpenses.isEmpty()) {
        items += DashboardItem.EmptyState
    } else {
        recentExpenses.take(3).forEach { items += DashboardItem.TransactionRow(it) }
        items += DashboardItem.ViewAll(onViewAllTransactions)
    }

    return items
}
