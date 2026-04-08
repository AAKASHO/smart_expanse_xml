package com.smartexpense.ai.ui.addexpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R
import com.smartexpense.ai.data.ExpenseCategory

/**
 * Adapter for the category grid in Add Expense.
 * Driven entirely by the [categories] list — swap data source
 * (e.g. from a remote config) without touching this adapter.
 */
class CategoryAdapter(
    private val categories: List<ExpenseCategory>,
    private val onCategorySelected: (ExpenseCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedKey: String = categories.firstOrNull()?.key ?: ""

    fun setSelected(key: String) {
        val oldIndex = categories.indexOfFirst { it.key == selectedKey }
        selectedKey = key
        val newIndex = categories.indexOfFirst { it.key == selectedKey }
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    fun getSelectedKey(): String = selectedKey

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.category_item_root)
        val iconCircle: FrameLayout = itemView.findViewById(R.id.icon_circle)
        val icon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        val label: TextView = itemView.findViewById(R.id.tv_category_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context
        val isSelected = category.key == selectedKey

        holder.label.text = category.label
        holder.icon.setImageResource(category.iconRes)

        if (isSelected) {
            // Selected: deep navy card, white icon circle, white icon & label
            holder.root.background = ContextCompat.getDrawable(context, R.drawable.bg_category_selected_grid)
            holder.iconCircle.background = ContextCompat.getDrawable(context, R.drawable.bg_icon_circle_selected)
            holder.icon.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            holder.label.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            holder.label.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            // Unselected: light surface card, tinted icon circle, navy icon & grey label
            holder.root.background = ContextCompat.getDrawable(context, R.drawable.bg_category_unselected)
            holder.iconCircle.background = ContextCompat.getDrawable(context, R.drawable.bg_icon_circle)
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            holder.label.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
            holder.label.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        holder.root.setOnClickListener {
            setSelected(category.key)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}
