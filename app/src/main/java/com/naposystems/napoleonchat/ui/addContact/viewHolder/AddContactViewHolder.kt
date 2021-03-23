package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactItemBinding
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter

class AddContactViewHolder constructor(private val binding: AddContactItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Contact, clickListener: AddContactAdapter.ClickListener) {

        binding.contact = item

        binding.textViewUserName.isSelected = true

        binding.buttonAdd.setOnClickListener {
            item.receiver = true
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