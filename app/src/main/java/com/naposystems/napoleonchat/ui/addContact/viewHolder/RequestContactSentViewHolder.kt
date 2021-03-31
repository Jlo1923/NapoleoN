package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactRequestSentItemBinding
import com.naposystems.napoleonchat.model.addContact.Contact

class RequestContactSentViewHolder constructor(private val binding: AddContactRequestSentItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Contact) {
        binding.apply {
            contact = item
            executePendingBindings()
        }

    }

    companion object {
        fun from(parent: ViewGroup): RequestContactSentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AddContactRequestSentItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return RequestContactSentViewHolder(binding)
        }
    }
}
