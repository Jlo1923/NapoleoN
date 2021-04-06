package com.naposystems.napoleonchat.ui.addContact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AddContactFriendshipRequestOfferBinding
import com.naposystems.napoleonchat.databinding.AddContactFriendshipRequestReceivedBinding
import com.naposystems.napoleonchat.databinding.AddContactFriendshipRequestTitleBinding
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.model.FriendShipRequestAdapterType
import com.naposystems.napoleonchat.model.FriendshipRequestTitle
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog

class FriendshipRequestAdapter constructor(
    private val clickListener: ClickListener,
    private val fragmentManager: FragmentManager,
    private val handlerDialog: HandlerDialog,
    private val context: Context
) : ListAdapter<FriendShipRequestAdapterType, RecyclerView.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<FriendShipRequestAdapterType>() {
        override fun areItemsTheSame(
            oldItem: FriendShipRequestAdapterType,
            newItem: FriendShipRequestAdapterType
        ): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(
            oldItem: FriendShipRequestAdapterType,
            newItem: FriendShipRequestAdapterType
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.FriendShipRequestType.TITLE.type -> {
                TitleViewHolder.from(parent)
            }
            Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type -> {
                FriendshipRequestViewHolder.from(parent)
            }
            Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_OFFER.type -> {
                FriendshipOfferViewHolder.from(parent)
            }
            else -> {
                TitleViewHolder.from(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (getItemViewType(position) == Constants.FriendShipRequestType.TITLE.type) {
            (holder as TitleViewHolder).bind(item as FriendshipRequestTitle)
        } else {
            val friendshipRequest = item as FriendShipRequest

            if (friendshipRequest.isReceived) {
                //TODO: Remover el paso de context por parametro
                (holder as FriendshipRequestViewHolder).bind(
                    item,
                    clickListener,
                    fragmentManager,
                    handlerDialog,
                    context
                )
            } else {
                //TODO: Remover el paso de context por parametro
                (holder as FriendshipOfferViewHolder).bind(
                    item,
                    clickListener,
                    fragmentManager,
                    handlerDialog,
                    context
                )
            }
        }
    }

    class FriendshipRequestViewHolder constructor(private val binding: AddContactFriendshipRequestReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FriendShipRequest,
            clickListener: ClickListener,
            fragmentManager: FragmentManager,
            handlerDialog: HandlerDialog,
            context: Context
        ) {

            binding.friendshipRequest = item
            binding.progressbar.isVisible = false
            binding.containerButtons.isVisible = true
            binding.textViewUserName.isSelected = true

            binding.buttonRefuse.setOnClickListener {

                handlerDialog.generalDialog(
                    context.getString(R.string.text_friend_title_reject_request),
                    context.getString(
                        R.string.text_friend_text_reject_request,
                        item.contact.nickname
                    ),
                    true,
                    fragmentManager,
                    context.getString(
                        R.string.text_confirm
                    )
                ) {
                    hideButtons()
                    clickListener.onRefuse(item)
                }

            }

            binding.buttonAccept.setOnClickListener {
                hideButtons()
                clickListener.onAccept(item)
            }

            binding.executePendingBindings()
        }

        fun hideButtons() {
            binding.progressbar.isVisible = true
            binding.containerButtons.isVisible = false
        }

        companion object {
            fun from(parent: ViewGroup): FriendshipRequestViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddContactFriendshipRequestReceivedBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return FriendshipRequestViewHolder(binding)
            }
        }

    }

    class FriendshipOfferViewHolder constructor(private val binding: AddContactFriendshipRequestOfferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FriendShipRequest, clickListener: ClickListener,
            fragmentManager: FragmentManager,
            handlerDialog: HandlerDialog,
            context: Context
        ) {
            binding.progressbar.isVisible = false
            binding.buttonCancel.setIconTintResource(R.color.red)
            binding.friendshipRequest = item
            binding.clickListener = clickListener

            binding.buttonCancel.setOnClickListener {
                handlerDialog.generalDialog(
                    context.getString(R.string.text_friend_title_cancel_request),
                    context.getString(
                        R.string.text_friend_text_cancel_request,
                        item.contact.nickname
                    ),
                    true,
                    fragmentManager,
                    context.getString(
                        R.string.text_confirm
                    )
                ) {

                    it.isEnabled = false
                    binding.buttonCancel.setIconTintResource(R.color.white)
                    binding.progressbar.isVisible = true

                    clickListener.onCancel(item)
                }
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): FriendshipOfferViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddContactFriendshipRequestOfferBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return FriendshipOfferViewHolder(binding)
            }
        }
    }

    class TitleViewHolder constructor(private val binding: AddContactFriendshipRequestTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendshipRequestTitle) {
            binding.title = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddContactFriendshipRequestTitleBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return TitleViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onRefuse(friendshipRequest: FriendShipRequest)
        fun onAccept(friendshipRequest: FriendShipRequest)
        fun onCancel(friendshipRequest: FriendShipRequest)
    }
}