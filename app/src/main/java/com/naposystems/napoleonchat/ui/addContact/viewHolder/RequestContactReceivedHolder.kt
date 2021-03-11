package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactRequestReceivedItemBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter
import timber.log.Timber

class RequestContactReceivedHolder constructor(private val binding: AddContactRequestReceivedItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ContactEntity, clickListener: AddContactAdapter.ClickListener) {
        binding.contact = item
        binding.buttonRefuse.setOnClickListener {
            item.statusFriend = false
            item.offer = false
            clickListener.onAcceptRequest(item, false)
        }
        binding.buttonAccept.setOnClickListener {
            item.statusFriend = true
            item.offer = false
            clickListener.onAcceptRequest(item, true)
        }
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): RequestContactReceivedHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AddContactRequestReceivedItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return RequestContactReceivedHolder(binding)
        }
    }
}