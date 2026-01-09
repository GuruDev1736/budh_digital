package com.naturexpresscargo.pressapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.naturexpresscargo.pressapp.databinding.ItemServiceBinding
import com.naturexpresscargo.pressapp.models.ServiceItem

class ServiceAdapter(
    private val services: List<ServiceItem>,
    private val onItemClick: (ServiceItem) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: ServiceItem) {
            binding.apply {
                tvServiceTitle.text = service.title
                tvServiceDescription.text = service.description
                ivServiceIcon.setImageResource(service.icon)

                val backgroundColor = ContextCompat.getColor(root.context, service.iconColor)
                iconBackground.setCardBackgroundColor(backgroundColor)

                root.setOnClickListener {
                    onItemClick(service)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size
}

