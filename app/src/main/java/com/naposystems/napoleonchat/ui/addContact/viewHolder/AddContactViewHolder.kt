package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactItemBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter

class AddContactViewHolder constructor(private val binding: AddContactItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ContactEntity, clickListener: AddContactAdapter.ClickListener) {

        binding.contact = item

        binding.textViewUserName.isSelected = true

        if (item.haveFriendshipRequest) {
            binding.buttonAdd.isVisible = false
            binding.textSent.isVisible = true
        } else {
            binding.buttonAdd.isVisible = true
            binding.textSent.isVisible = false
        }

        binding.buttonAdd.setOnClickListener {
            clickListener.onAddClick(item)
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