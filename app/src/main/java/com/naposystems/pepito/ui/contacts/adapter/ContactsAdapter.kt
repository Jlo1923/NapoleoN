package com.naposystems.pepito.ui.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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

            binding.buttonMore.setOnClickListener {
                clickListener.onMoreClick(item, it)
            }

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

    interface ContactClickListener {
        fun onClick(item: Contact)
        fun onMoreClick(item: Contact, view: View)
    }
}