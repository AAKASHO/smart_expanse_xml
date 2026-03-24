package com.smartexpense.ai.ui.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R
import com.smartexpense.ai.data.db.Expense
import com.smartexpense.ai.util.CurrencyFormatter
import com.smartexpense.ai.util.DateFormatter

sealed class TransactionListItem {
    data class Header(val title: String) : TransactionListItem()
    data class Transaction(val expense: Expense) : TransactionListItem()
}

class TransactionAdapter : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Header -> VIEW_TYPE_HEADER
            is TransactionListItem.Transaction -> VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.Transaction -> (holder as TransactionViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tv_header)
        fun bind(item: TransactionListItem.Header) {
            tvHeader.text = item.title
        }
    }

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMerchant: TextView = view.findViewById(R.id.tv_merchant)
        private val tvDetails: TextView = view.findViewById(R.id.tv_details)
        private val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        private val tvAutoSynced: TextView = view.findViewById(R.id.tv_auto_synced)
        private val tvAutoSyncBadge: TextView = view.findViewById(R.id.tv_auto_sync_badge)

        fun bind(item: TransactionListItem.Transaction) {
            val expense = item.expense
            tvMerchant.text = expense.merchant.ifEmpty { expense.category }
            tvDetails.text = "${expense.paymentMethod} • ${expense.category}"
            tvAmount.text = "₹${CurrencyFormatter.format(expense.amount)}"

            if (expense.isAutoSynced) {
                tvAutoSynced.visibility = View.VISIBLE
                tvAutoSyncBadge.visibility = View.VISIBLE
            } else {
                tvAutoSynced.visibility = View.GONE
                tvAutoSyncBadge.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionListItem>() {
            override fun areItemsTheSame(
                oldItem: TransactionListItem, newItem: TransactionListItem
            ): Boolean {
                return when {
                    oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header ->
                        oldItem.title == newItem.title
                    oldItem is TransactionListItem.Transaction && newItem is TransactionListItem.Transaction ->
                        oldItem.expense.id == newItem.expense.id
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: TransactionListItem, newItem: TransactionListItem
            ): Boolean = oldItem == newItem
        }

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

            return items
        }
    }
}
