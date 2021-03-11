package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AddContactTitleItemBinding
import com.naposystems.napoleonchat.model.addContact.AddContactTitle

class AddContactTitleViewHolder constructor( val binding: AddContactTitleItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AddContactTitle ) {
        binding.textViewTitle.text = item.title
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