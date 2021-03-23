package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AddContactTitleItemBinding
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.utility.Constants

class AddContactTitleViewHolder constructor(val binding: AddContactTitleItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Contact, context: Context) {

        if (item.type == Constants.AddContactTitleType.TITLE_MY_CONTACTS.type) {
            binding.textViewTitle.text = context.getString(R.string.text_my_contacts_added)
        } else binding.textViewTitle.text = context.getString(R.string.text_coincidence)

    }

    companion object {
        fun from(parent: ViewGroup): AddContactTitleViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AddContactTitleItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            return AddContactTitleViewHolder(binding)
        }
    }

}