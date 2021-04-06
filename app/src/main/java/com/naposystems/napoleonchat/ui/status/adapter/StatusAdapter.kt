package com.naposystems.napoleonchat.ui.status.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.StatusFragmentItemBinding
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener

class StatusAdapter(
    private val statusEntities: List<StatusEntity>,
    private val clickListener: StatusSelectionListener
) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder.from(parent)
    }

    override fun getItemCount() = statusEntities.size

    override fun onBindViewHolder(holderStatus: StatusViewHolder, position: Int) {
        val status = statusEntities[position]
        holderStatus.bind(status, clickListener)
    }

    class StatusViewHolder private constructor(private val binding: StatusFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(statusEntity: StatusEntity, clickListener: StatusSelectionListener) {
            binding.status = statusEntity
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.buttonMore.setSafeOnClickListener {
                clickListener.onMoreClick(statusEntity, it)
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
        val clickListener: (statusEntity: StatusEntity) -> Unit,
        val clickDelete: (statusEntity: StatusEntity, view: View) -> Unit
    ) {
        fun onClick(statusEntity: StatusEntity) = clickListener(statusEntity)
        fun onMoreClick(statusEntity: StatusEntity, view: View) = clickDelete(statusEntity, view)
    }
}