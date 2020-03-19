package com.naposystems.pepito.ui.status.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.StatusFragmentItemBinding
import com.naposystems.pepito.entity.Status

class StatusAdapter(
    private val status: List<Status>,
    private val clickListener: StatusSelectionListener
) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder.from(parent)
    }

    override fun getItemCount() = status.size

    override fun onBindViewHolder(holderStatus: StatusViewHolder, position: Int) {
        val status = status[position]
        holderStatus.bind(status, clickListener)
    }

    class StatusViewHolder private constructor(private val binding: StatusFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: Status, clickListener: StatusSelectionListener) {
            binding.status = status
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.buttonMore.setOnClickListener {
                clickListener.onMoreClick(status, it)
            }
        }

        companion object {
            fun from(parent: ViewGroup): StatusViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = StatusFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return StatusViewHolder(binding)
            }
        }
    }

    class StatusSelectionListener(
        val clickListener: (status: Status) -> Unit,
        val clickDelete: (status: Status, view: View) -> Unit
    ) {
        fun onClick(status: Status) = clickListener(status)
        fun onMoreClick(status: Status, view: View) = clickDelete(status, view)
    }
}