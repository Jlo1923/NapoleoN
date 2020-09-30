package com.naposystems.napoleonchat.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactFriendshipRequestReceivedBinding
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest

class FriendShipRequestReceivedAdapter constructor(private val clickListener: ClickListener) :
    ListAdapter<FriendShipRequest, FriendShipRequestReceivedAdapter.FriendShipRequestReceivedViewHolder>(
        FriendShipRequest.DiffCallback
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendShipRequestReceivedViewHolder {
        return FriendShipRequestReceivedViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FriendShipRequestReceivedViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class FriendShipRequestReceivedViewHolder constructor(
        private val binding: AddContactFriendshipRequestReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendShipRequest, clickListener: ClickListener) {
            binding.friendshipRequest = item
            binding.buttonRefuse.isEnabled = true
            binding.buttonAccept.isEnabled = true

            binding.textViewUserName.isSelected = true

            binding.buttonRefuse.setOnClickListener {
                it.isEnabled = false
                binding.buttonAccept.isEnabled = false
                clickListener.onRefuse(item)
            }

            binding.buttonAccept.setOnClickListener {
                it.isEnabled = false
                binding.buttonRefuse.isEnabled = false
                clickListener.onAccept(item)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): FriendShipRequestReceivedViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddContactFriendshipRequestReceivedBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return FriendShipRequestReceivedViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onRefuse(friendshipRequest: FriendShipRequest)
        fun onAccept(friendshipRequest: FriendShipRequest)
    }
}