package com.naposystems.pepito.ui.status.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.StatusFragmentItemBinding
import com.naposystems.pepito.entity.Status

class StatusAdapter(
    private val status: List<Status>,
    private val clickListener: StatusSelectionListener
) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = status.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status = status[position]
        holder.bind(status, clickListener)
    }

    class ViewHolder private constructor(private val binding: StatusFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: Status, clickListener: StatusSelectionListener) {
            binding.status = status
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StatusFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }
    }

    class StatusSelectionListener(val clickListener: (status: Status) -> Unit) {
        fun onClick(status: Status) = clickListener(status)
    }
}