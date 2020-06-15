package com.naposystems.pepito.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.HomeFragmentItemBinding
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants

class ConversationAdapter(
    private val clickListener: ClickListener,
    private val displayFormat: Int?,
    private val timeFormat: Int?
) :
    ListAdapter<MessageAndAttachment,
            ConversationAdapter.ConversationAndContactViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<MessageAndAttachment>() {
        override fun areItemsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationAndContactViewHolder {
        return ConversationAndContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ConversationAndContactViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat, clickListener, displayFormat, timeFormat)
    }

    class ConversationAndContactViewHolder private constructor(
        private val binding: HomeFragmentItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            conversation: MessageAndAttachment, clickListener: ClickListener,
            displayFormat: Int?,
            timeFormat: Int?
        ) {
            binding.conversation = conversation
            binding.clickListener = clickListener
            binding.displayFormat = displayFormat
            binding.timeFormat = timeFormat

            binding.constrainsLayoutConversation.setOnLongClickListener {
                clickListener.onLongClick(conversation, binding.textViewHora)
                true
            }

            binding.imageViewUserImage.setOnClickListener {
                clickListener.onClickAvatar(conversation)
            }

            binding.executePendingBindings()

            if (conversation.message.isMine == Constants.IsMine.YES.value) {
                binding.textViewUnreadMessages.visibility = View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup): ConversationAndContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HomeFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return ConversationAndContactViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onClick(item: MessageAndAttachment)
        fun onClickAvatar(item: MessageAndAttachment)
        fun onLongClick(item: MessageAndAttachment, view: View)
    }
}