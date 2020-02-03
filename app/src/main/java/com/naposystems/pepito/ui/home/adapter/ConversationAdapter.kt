package com.naposystems.pepito.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.HomeFragmentItemBinding
import com.naposystems.pepito.entity.conversation.ConversationAndContact

class ConversationAdapter(private val clickListener: ClickListener): ListAdapter<ConversationAndContact, ConversationAdapter.ConversationAndContactViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ConversationAndContact>() {
        override fun areItemsTheSame(
            oldItem: ConversationAndContact,
            newItem: ConversationAndContact
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: ConversationAndContact,
            newItem: ConversationAndContact
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationAndContactViewHolder {
        return ConversationAndContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ConversationAndContactViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat, clickListener)
    }

    class ConversationAndContactViewHolder private constructor(private val binding: HomeFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: ConversationAndContact, clickListener: ClickListener) {
            binding.clickListener = clickListener
            binding.conversation = conversation
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ConversationAndContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HomeFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return ConversationAndContactViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onClick(item: ConversationAndContact)
    }
}