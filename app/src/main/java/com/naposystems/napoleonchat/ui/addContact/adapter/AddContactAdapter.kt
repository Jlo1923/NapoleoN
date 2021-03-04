package com.naposystems.napoleonchat.ui.addContact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactItemBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

class AddContactAdapter constructor(private val clickListener: ClickListener) :
    ListAdapter<ContactEntity, AddContactAdapter.AddContactViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
        override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem.nickname == newItem.nickname && oldItem.haveFriendshipRequest == newItem.haveFriendshipRequest
        }

        override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddContactViewHolder {
        return AddContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AddContactViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class AddContactViewHolder constructor(private val binding: AddContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContactEntity, clickListener: ClickListener) {

            binding.contact = item
            binding.clickListener = clickListener
            binding.buttonAdd.isEnabled = true

            binding.textViewUserName.isSelected = true

            binding.buttonAdd.setOnClickListener {
                if (!item.haveFriendshipRequest) {
                    it.isEnabled = false
                    clickListener.onAddClick(item)
                }
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AddContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddContactItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return AddContactViewHolder(binding)
            }
        }
    }

    fun updateContact(index: Int) {
        currentList[index].haveFriendshipRequest = true
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onAddClick(contact: ContactEntity)
    }
}