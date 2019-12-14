package com.naposystems.pepito.ui.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.ContactsItemBinding
import com.naposystems.pepito.entity.Contact

class ContactsAdapter constructor(private val clickListener: ContactClickListener) :
    ListAdapter<Contact, ContactsAdapter.ContactsViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.nickname == newItem.nickname
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactsViewHolder {
        return ContactsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ContactsViewHolder constructor(private val binding: ContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Contact, clickListener: ContactClickListener) {
            binding.contact = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ContactsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContactsItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ContactsViewHolder(binding)
            }
        }
    }

    class ContactClickListener(val clickListener: (item: Contact) -> Unit) {
        fun onClick(item: Contact) = clickListener(item)
    }
}