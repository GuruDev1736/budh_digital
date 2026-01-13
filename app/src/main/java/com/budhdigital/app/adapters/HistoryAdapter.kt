package com.budhdigital.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.budhdigital.app.databinding.ItemHistoryBinding
import com.budhdigital.app.models.HistoryItem

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onActionClick: (HistoryItem, String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.apply {
                tvTitle.text = item.title
                tvStatus.text = item.status
                tvDate.text = item.date
                tvRequestId.text = "#${item.id}"

                ivIcon.setImageResource(item.icon)
                val iconBgColor = ContextCompat.getColor(root.context, item.iconColor)
                iconBackground.setCardBackgroundColor(iconBgColor)

                val statusColor = ContextCompat.getColor(root.context, item.statusColor)
                tvStatus.setTextColor(statusColor)


                root.setOnClickListener {
                    onActionClick(item, "View Details")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

