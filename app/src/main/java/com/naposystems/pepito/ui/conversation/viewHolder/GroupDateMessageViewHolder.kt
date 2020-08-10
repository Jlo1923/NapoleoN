package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemGroupDateMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Utils

class GroupDateMessageViewHolder constructor(
    private val binding: ConversationItemGroupDateMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment
    ) {
        binding.conversation = item
        binding.textViewBody.text = item.message.body
        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): GroupDateMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemGroupDateMessageBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            return GroupDateMessageViewHolder(binding)
        }
    }
}