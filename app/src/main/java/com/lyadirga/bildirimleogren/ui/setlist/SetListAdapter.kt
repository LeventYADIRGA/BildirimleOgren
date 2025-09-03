package com.lyadirga.bildirimleogren.ui.setlist

import com.lyadirga.bildirimleogren.databinding.ListItemSetBinding
import com.lyadirga.bildirimleogren.model.LanguageSetSummary
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SetListAdapter(
    private val onClick: (LanguageSetSummary) -> Unit
) : ListAdapter<LanguageSetSummary, SetListAdapter.MyViewHolder>(DiffCallback) {

    inner class MyViewHolder(val binding: ListItemSetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemSetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            title.text = item.title
            url.text = item.url ?: ""

            root.setOnClickListener {
                onClick(item)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<LanguageSetSummary>() {
        override fun areItemsTheSame(oldItem: LanguageSetSummary, newItem: LanguageSetSummary): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LanguageSetSummary, newItem: LanguageSetSummary): Boolean {
            return oldItem == newItem
        }
    }
}
