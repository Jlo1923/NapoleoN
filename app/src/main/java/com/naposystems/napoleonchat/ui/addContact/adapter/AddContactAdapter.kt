package com.naposystems.napoleonchat.ui.addContact.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.model.addContact.AddContactTitle
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.ui.addContact.viewHolder.*
import com.naposystems.napoleonchat.utility.Constants

class AddContactAdapter constructor(
    private val context: Context,
    private val clickListener: ClickListener
) :
    ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback) {

    private lateinit var objectTitle: AddContactTitle

    companion object {
        const val TYPE_NO_FRIEND = 1
        const val TYPE_FRIEND = 2
        const val TYPE_REQUEST_SENT = 3
        const val TYPE_REQUEST_RECEIVED = 4
        const val TYPE_TITLE = 5

    }

    object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            if (oldItem is Contact && newItem is Contact)
                return oldItem.nickname == newItem.nickname && oldItem.receiver == newItem.receiver

            return if (oldItem is AddContactTitle && newItem is AddContactTitle)
                oldItem.type == newItem.type
            else false
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item is Contact) {
            when {
                item.statusFriend -> TYPE_FRIEND
                item.receiver == true -> TYPE_REQUEST_SENT
                item.offer == true -> TYPE_REQUEST_RECEIVED
                else ->
                    TYPE_NO_FRIEND
            }
        } else TYPE_TITLE

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NO_FRIEND ->
                AddContactViewHolder.from(parent)
            TYPE_REQUEST_SENT -> {
                RequestContactSentViewHolder.from(parent)
            }
            TYPE_REQUEST_RECEIVED -> {
                RequestContactReceivedHolder.from(parent)
            }
            TYPE_TITLE -> {
                AddContactTitleViewHolder.from(parent)
            }
            else -> {
                //friend or friend blocked
                FriendContactViewHolder.from(parent)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AddContactViewHolder -> holder.bind(item as Contact, clickListener)
            is RequestContactSentViewHolder -> holder.bind(item as Contact)
            is RequestContactReceivedHolder -> holder.bind(item as Contact, clickListener)
            is AddContactTitleViewHolder -> {
                objectTitle = item as AddContactTitle
                holder.bind(item, context)
            }
            else -> {
                (holder as FriendContactViewHolder).bind(item as Contact, clickListener)
            }
        }
    }


    fun updateContact(contact: Contact) {
        contact.receiver = true
        val index = currentList.indexOf(contact)
        notifyItemChanged(index)
    }

    fun updateContactRequest(contact: Contact) {
        //update Title
        if (contact.statusFriend) {
            objectTitle.type = Constants.AddContactTitleType.TITLE_MY_CONTACTS.type
            val index = currentList.indexOf(objectTitle)
            notifyItemChanged(index)
        }

        //update Contact
        val index = currentList.indexOf(contact)
        notifyItemChanged(index)
    }

    interface ClickListener {
        fun onAddClick(contact: Contact)
        fun onOpenChat(contact: Contact)
        fun onAcceptRequest(contact: Contact, state: Boolean)
    }
}