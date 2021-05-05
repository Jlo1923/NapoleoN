package com.naposystems.napoleonchat.ui.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ContactsItemBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogViewModel

class ContactsAdapter constructor(
        private val clickListener: ContactClickListener,
        private val userDisplayFormatDialogViewModel: UserDisplayFormatDialogViewModel
    ) :
    ListAdapter<ContactEntity, ContactsAdapter.ContactsViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
        override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem.nicknameFake == newItem.nicknameFake && oldItem.displayNameFake == newItem.displayNameFake
        }

        override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
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
        holder.bind(item, clickListener, userDisplayFormatDialogViewModel)
    }

    class ContactsViewHolder constructor(private val binding: ContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactEntity,
                 clickListener: ContactClickListener,
                 userDisplayFormatDialogViewModel: UserDisplayFormatDialogViewModel
        ) {
            binding.clickListener = clickListener

            binding.apply {
                contact = item
                userDisplayFormat = userDisplayFormatDialogViewModel.getUserDisplayFormat()

                buttonMore.setOnClickListener {
                    clickListener.onMoreClick(item, it)
                }

                val context = container.context

                if (item.id == 0) {
                    container.background = context.resources.getDrawable(
                        R.drawable.bg_add_contact_item, context.theme
                    )
                    buttonMore.visibility = View.GONE
                } else {
                    container.background = context.resources.getDrawable(
                        R.drawable.bg_home_item, context.theme
                    )
                    buttonMore.visibility = View.VISIBLE
                }

                executePendingBindings()
            }
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
        fun onClick(item: ContactEntity)
        fun onMoreClick(item: ContactEntity, view: View)
    }
}
