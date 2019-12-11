package com.naposystems.pepito.ui.blockedContacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.BlockedContactsItemBinding
import com.naposystems.pepito.entity.BlockedContact

class BlockedContactsAdapter constructor(
    private val blockedContacts: List<BlockedContact>,
    private val clickListener: BlockedContactSelectionListener
) :
    RecyclerView.Adapter<BlockedContactsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = blockedContacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val blockedContact = blockedContacts[position]
        holder.bind(blockedContact, clickListener)
    }

    class ViewHolder constructor(private val binding: BlockedContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blockedContact: BlockedContact, clickListener: BlockedContactSelectionListener) {
            binding.contact = blockedContact
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = BlockedContactsItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return ViewHolder(binding)
            }
        }

    }

    class BlockedContactSelectionListener(val clickListener: (blockedContact: BlockedContact) -> Unit) {
        fun onClick(blockedContact: BlockedContact) = clickListener(blockedContact)
    }
}