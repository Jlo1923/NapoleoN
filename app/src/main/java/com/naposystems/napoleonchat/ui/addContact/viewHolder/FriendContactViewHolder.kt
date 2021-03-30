package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactFriendItemBinding
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter


class FriendContactViewHolder constructor(private val binding: AddContactFriendItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Contact, clickListener: AddContactAdapter.ClickListener) {

        binding.apply {
            contact = item
            root.setOnClickListener {
                if (!item.statusBlocked) clickListener.onOpenChat(item)
            }
            executePendingBindings()
        }

    }

    companion object {
        fun from(parent: ViewGroup): FriendContactViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AddContactFriendItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return FriendContactViewHolder(binding)
        }
    }
}