package com.smartexpense.ai.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartexpense.ai.R
import com.smartexpense.ai.databinding.ItemInsightCardBinding
import com.smartexpense.ai.service.insights.InsightCard
import com.smartexpense.ai.service.insights.InsightSeverity
import com.smartexpense.ai.service.insights.InsightType

class InsightAdapter : ListAdapter<InsightCard, InsightAdapter.InsightViewHolder>(InsightDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val binding = ItemInsightCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InsightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InsightViewHolder(private val binding: ItemInsightCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(insight: InsightCard) {
            binding.insight = insight
            val context = binding.root.context
            
            // Format badge text
            binding.tvInsightBadge.text = insight.type.name.replace("_", " ")
            
            // Create circular background for icon
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * context.resources.displayMetrics.density
            }
            
            // Apply dynamic styling based on severity and type
            when (insight.severity) {
                InsightSeverity.CRITICAL -> {
                    binding.ivInsightIcon.setImageResource(R.drawable.ic_notification)
                    binding.ivInsightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error))
                    bgDrawable.setColor(ContextCompat.getColor(context, R.color.error_container))
                    binding.tvInsightBadge.setTextColor(Color.parseColor("#99454652"))
                }
                InsightSeverity.WARNING -> {
                    binding.ivInsightIcon.setImageResource(R.drawable.ic_notification)
                    binding.ivInsightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.tertiary))
                    bgDrawable.setColor(ContextCompat.getColor(context, R.color.tertiary_container))
                    binding.tvInsightBadge.setTextColor(Color.parseColor("#99454652"))
                }
                InsightSeverity.INFO -> {
                    if (insight.type == InsightType.TOP_CATEGORY) {
                        binding.ivInsightIcon.setImageResource(R.drawable.ic_home)
                        binding.ivInsightIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        bgDrawable.setColor(ContextCompat.getColor(context, R.color.on_secondary_container))
                        binding.tvInsightBadge.setTextColor(Color.parseColor("#99006E6E"))
                    } else {
                        binding.ivInsightIcon.setImageResource(R.drawable.ic_insights)
                        binding.ivInsightIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        bgDrawable.setColor(ContextCompat.getColor(context, R.color.primary_container))
                        binding.tvInsightBadge.setTextColor(Color.parseColor("#99454652"))
                    }
                }
            }
            
            binding.ivInsightIcon.background = bgDrawable
            binding.executePendingBindings()
        }
    }

    class InsightDiffCallback : DiffUtil.ItemCallback<InsightCard>() {
        override fun areItemsTheSame(oldItem: InsightCard, newItem: InsightCard): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: InsightCard, newItem: InsightCard): Boolean {
            return oldItem == newItem
        }
    }
}
