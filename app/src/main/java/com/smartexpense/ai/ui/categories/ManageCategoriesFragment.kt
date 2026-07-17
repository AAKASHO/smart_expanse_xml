package com.smartexpense.ai.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartexpense.ai.R
import com.smartexpense.ai.databinding.FragmentManageCategoriesBinding
import kotlinx.coroutines.launch

class ManageCategoriesFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentManageCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ManageCategoriesViewModel
    private lateinit var adapter: ManageCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Expand the bottom sheet to a comfortable height
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            peekHeight = resources.displayMetrics.heightPixels / 2
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ManageCategoriesViewModel::class.java]

        adapter = ManageCategoryAdapter(
            onDelete = { displayCategory ->
                displayCategory.entity?.let { viewModel.deleteCategory(it) }
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ManageCategoriesFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.displayCategories.collect { categories ->
                adapter.submitList(categories)
            }
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_category, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_category))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val emoji = dialogView.findViewById<EditText>(R.id.et_emoji).text
                    .toString().trim().ifEmpty { "📦" }
                val name = dialogView.findViewById<EditText>(R.id.et_category_name).text
                    .toString().trim()
                if (name.isNotBlank()) {
                    viewModel.addCategory(name, emoji)
                }
            }
            .setNegativeButton(getString(R.string.discard), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Inner Adapter ────────────────────────────────────────────────────────

    class ManageCategoryAdapter(
        private val onDelete: (DisplayCategory) -> Unit
    ) : ListAdapter<DisplayCategory, ManageCategoryAdapter.ViewHolder>(DiffCallback) {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvEmoji: TextView = itemView.findViewById(R.id.tv_emoji)
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
            val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
            val tvBadge: TextView = itemView.findViewById(R.id.tv_built_in_badge)
            val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_manage_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            val ctx = holder.itemView.context

            holder.tvLabel.text = item.label

            if (item.isBuiltIn) {
                // Show drawable icon
                holder.tvEmoji.visibility = View.GONE
                holder.ivIcon.visibility = View.VISIBLE
                holder.ivIcon.setImageResource(item.iconRes)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.primary))
                holder.tvBadge.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.GONE
            } else {
                // Show emoji icon
                holder.tvEmoji.visibility = View.VISIBLE
                holder.ivIcon.visibility = View.GONE
                holder.tvEmoji.text = item.emoji
                holder.tvBadge.visibility = View.GONE
                holder.btnDelete.visibility = View.VISIBLE
                holder.btnDelete.setOnClickListener { onDelete(item) }
            }
        }

        private object DiffCallback : DiffUtil.ItemCallback<DisplayCategory>() {
            override fun areItemsTheSame(a: DisplayCategory, b: DisplayCategory) = a.key == b.key
            override fun areContentsTheSame(a: DisplayCategory, b: DisplayCategory) = a == b
        }
    }
}
