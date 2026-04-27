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
import com.smartexpense.ai.databinding.ItemCategoryGridBinding

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

    inner class ViewHolder(val binding: ItemCategoryGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context
        val isSelected = category.key == selectedKey
        
        val binding = holder.binding

        binding.tvCategoryLabel.text = category.label
        binding.ivCategoryIcon.setImageResource(category.iconRes)

        if (isSelected) {
            // Selected: deep navy card, white icon circle, white icon & label
            binding.categoryItemRoot.background = ContextCompat.getDrawable(context, R.drawable.bg_category_selected_grid)
            binding.iconCircle.background = ContextCompat.getDrawable(context, R.drawable.bg_icon_circle_selected)
            binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            binding.tvCategoryLabel.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            binding.tvCategoryLabel.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            // Unselected: light surface card, tinted icon circle, navy icon & grey label
            binding.categoryItemRoot.background = ContextCompat.getDrawable(context, R.drawable.bg_category_unselected)
            binding.iconCircle.background = ContextCompat.getDrawable(context, R.drawable.bg_icon_circle)
            binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            binding.tvCategoryLabel.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
            binding.tvCategoryLabel.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        binding.categoryItemRoot.setOnClickListener {
            setSelected(category.key)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}
