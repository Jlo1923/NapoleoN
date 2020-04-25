package com.naposystems.pepito.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.HomeFragmentItemBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel

class ConversationAdapter(
    private val clickListener: ClickListener,
    private val userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel
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
        holder.bind(chat, clickListener, userDisplayFormatShareViewModel)
    }

    class ConversationAndContactViewHolder private constructor(
        private val binding: HomeFragmentItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: MessageAndAttachment, clickListener: ClickListener,
                 userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel
        ) {
            binding.conversation = conversation
            binding.clickListener = clickListener
            binding.viewModel = userDisplayFormatShareViewModel

            binding.constrainsLayoutConversation.setOnLongClickListener {
                clickListener.onLongClick(conversation, binding.textViewHora)
                true
            }

            binding.imageViewUserImage.setOnClickListener {
                clickListener.onClickAvatar(conversation)
            }

            binding.executePendingBindings()
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