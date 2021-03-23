package com.naposystems.napoleonchat.ui.addContact.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.ui.addContact.viewHolder.*
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

class AddContactAdapter constructor(
    private val context: Context,
    private val clickListener: ClickListener
) :
    ListAdapter<Contact, RecyclerView.ViewHolder>(DiffCallback) {


    companion object {
        const val TYPE_NO_FRIEND = 1
        const val TYPE_FRIEND = 2
        const val TYPE_REQUEST_SENT = 3
        const val TYPE_REQUEST_RECEIVED = 4
        const val TYPE_TITLE = 5

    }

    object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {

        val item = getItem(position)
        return if (item.type == 0) {
            when {
                item.statusFriend -> TYPE_FRIEND
                item.receiver -> TYPE_REQUEST_SENT
                item.offer -> TYPE_REQUEST_RECEIVED
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
            is RequestContactSentViewHolder -> holder.bind(item)
            is AddContactViewHolder -> holder.bind(item, clickListener)
            is RequestContactReceivedHolder -> holder.bind(item, clickListener)
            is AddContactTitleViewHolder -> {
                holder.bind(item, context)
            }
            else -> {
                (holder as FriendContactViewHolder).bind(item, clickListener)
            }
        }
    }


    fun updateContact(contact: Contact) {
        val index = currentList.indexOf(contact)
        notifyItemChanged(index)
    }

    fun updateContactRequest(contact: Contact) {
        try {
            //update Title

            if (contact.statusFriend) {
                val firstPosition = 0
                val secondPosition = 1

                val list: MutableList<Contact> = mutableListOf()
                list.addAll(currentList)

                val element = currentList.findLast { it.statusFriend }
                if (element != null) {
                    list.remove(contact)
                    list.add(secondPosition, contact)
                    submitList(list)
                    notifyItemChanged(secondPosition)
                } else {
                    list.add(
                        firstPosition, Contact(
                            id = -1,
                            type = Constants.AddContactTitleType.TITLE_MY_CONTACTS.type
                        )
                    )
                    list.add(secondPosition, contact)
                    submitList(list)
                    notifyItemRangeChanged(firstPosition, secondPosition)
                }
            } else updateContact(contact)

        } catch (exception: Exception) {
            Timber.d("exception ${exception.message}")
        }

    }

    interface ClickListener {
        fun onAddClick(contact: Contact)
        fun onOpenChat(contact: Contact)
        fun onAcceptRequest(contact: Contact, state: Boolean)
    }
}