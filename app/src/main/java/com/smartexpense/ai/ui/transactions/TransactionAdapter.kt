package com.smartexpense.ai.ui.transactions

import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R
import com.smartexpense.ai.data.ExpenseCategories
import com.smartexpense.ai.domain.model.Expense
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter
import com.smartexpense.ai.databinding.ItemTransactionBinding
import com.smartexpense.ai.databinding.ItemTransactionHeaderBinding
import com.smartexpense.ai.databinding.ItemAiFooterBinding
import com.smartexpense.ai.util.BindingAdapterUtils

class TransactionAdapter(private val onEditClicked: (Expense) -> Unit) : ListAdapter<TransactionAdapter.TransactionListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var expandedExpenseId: Long? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Header      -> VIEW_TYPE_HEADER
            is TransactionListItem.AiFooter    -> VIEW_TYPE_FOOTER
            is TransactionListItem.Transaction -> VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER  -> HeaderViewHolder(
                ItemTransactionHeaderBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_FOOTER  -> FooterViewHolder(
                ItemAiFooterBinding.inflate(inflater, parent, false)
            )
            else              -> TransactionViewHolder(
                ItemTransactionBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header      -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.Transaction -> (holder as TransactionViewHolder).bind(item)
            is TransactionListItem.AiFooter    -> { /* static card, nothing to bind */ }
        }
    }

    // ── Header ─────────────────────────────────────────────────────────
    inner class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionListItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    // ── AI footer (static card) ────────────────────────────────────────
    inner class FooterViewHolder(binding: ItemAiFooterBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ── Transaction ────────────────────────────────────────────────────
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionListItem.Transaction) {
            val expense = item.expense
            val context = binding.root.context
            val radius = context.resources.getDimension(com.smartexpense.ai.R.dimen.size16)

            // ── Basic info ──────────────────────────────────────────
            val iconRes = ExpenseCategories.ALL.find { it.label == expense.category }?.iconRes ?: R.drawable.ic_other
            binding.ivIcon.setImageResource(iconRes)

            binding.tvMerchant.text = expense.merchant.ifEmpty { expense.category }
            binding.tvDetails.text = "${expense.paymentMethod} • ${expense.category}"
            binding.tvAmount.text = "₹${CurrencyFormatter.format(expense.amount)}"

            // ── Auto-sync badge ─────────────────────────────────────
            binding.tvAutoSynced.visibility =
                if (expense.isAutoSynced) android.view.View.VISIBLE else android.view.View.GONE
            binding.ivAutoSyncBadge.visibility =
                if (expense.isAutoSynced) android.view.View.VISIBLE else android.view.View.GONE

            // ── Expand state ────────────────────────────────────────
            val isExpanded = expense.id == expandedExpenseId
            val hasNote = expense.note.isNotBlank()

            // Chevron is always visible — rotated when expanded
            binding.ivExpandIcon.rotation = if (isExpanded) 180f else 0f

            // ── Note card ───────────────────────────────────────────
            if (hasNote) {
                binding.layoutNoteCard.visibility = android.view.View.VISIBLE
                binding.tvNoteText.text = expense.note
                binding.tvNoteText.maxLines = if (isExpanded) Int.MAX_VALUE else 1
            } else {
                binding.layoutNoteCard.visibility = android.view.View.GONE
            }

            // ── Meta grid (expanded only) ───────────────────────────
            binding.layoutMetaGrid.visibility =
                if (isExpanded) android.view.View.VISIBLE else android.view.View.GONE

            binding.tvDetailCategory.text = expense.category
            binding.tvDetailTime.text = DateFormatter.getRelativeDate(expense.date)
            binding.tvDetailPaymentMethod.text = expense.paymentMethod
            binding.tvDetailSyncStatus.text = if (expense.isAutoSynced) "AUTO-SYNCED" else "MANUAL"

            // ── Edit button ─────────────────────────────────────────
            binding.btnEditTransaction.setOnClickListener {
                onEditClicked(expense)
            }

            // ── Card background ─────────────────────────────────────
            val bgColor = if (isExpanded) {
                ContextCompat.getColor(context, R.color.surface_container_high)
            } else {
                ContextCompat.getColor(context, R.color.surface_container_lowest)
            }
            val strokeColor = if (isExpanded)
                ContextCompat.getColor(context, R.color.primary)
            else null
            val strokeWidth = if (isExpanded)
                context.resources.getDimension(R.dimen.size1)
            else null
            BindingAdapterUtils.setBackground(
                binding.root, radius, bgColor,
                strokeColor = strokeColor, strokeWidth = strokeWidth
            )

            // ── Click to expand/collapse ────────────────────────────
            binding.root.setOnClickListener {
                val previousExpandedId = expandedExpenseId
                expandedExpenseId = if (isExpanded) null else expense.id

                val parent = binding.root.parent as? ViewGroup
                if (parent != null) {
                    TransitionManager.beginDelayedTransition(
                        parent,
                        android.transition.AutoTransition().apply { duration = 220 }
                    )
                }

                notifyItemChanged(adapterPosition)
                if (previousExpandedId != null && previousExpandedId != expense.id) {
                    val prevIdx = currentList.indexOfFirst {
                        it is TransactionListItem.Transaction && it.expense.id == previousExpandedId
                    }
                    if (prevIdx != -1) notifyItemChanged(prevIdx)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER      = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_FOOTER      = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionListItem>() {
            override fun areItemsTheSame(
                oldItem: TransactionListItem, newItem: TransactionListItem
            ): Boolean = when {
                oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header ->
                    oldItem.title == newItem.title
                oldItem is TransactionListItem.Transaction && newItem is TransactionListItem.Transaction ->
                    oldItem.expense.id == newItem.expense.id
                oldItem is TransactionListItem.AiFooter && newItem is TransactionListItem.AiFooter -> true
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: TransactionListItem, newItem: TransactionListItem
            ): Boolean = oldItem == newItem
        }

        /**
         * Groups expenses by date header and appends the AI footer card.
         */
        fun groupByDate(expenses: List<Expense>): List<TransactionListItem> {
            val items = mutableListOf<TransactionListItem>()
            var lastHeader = ""
            expenses.sortedByDescending { it.date }.forEach { expense ->
                val header = DateFormatter.getGroupHeader(expense.date)
                if (header != lastHeader) {
                    items.add(TransactionListItem.Header(header))
                    lastHeader = header
                }
                items.add(TransactionListItem.Transaction(expense))
            }
            // AI footer always appended at the end
            items.add(TransactionListItem.AiFooter)
            return items
        }
    }

    sealed class TransactionListItem {
        data class Header(val title: String) : TransactionListItem()
        data class Transaction(val expense: Expense) : TransactionListItem()
        /** Singleton footer injected at the end of a non-empty list */
        object AiFooter : TransactionListItem()
    }
}
