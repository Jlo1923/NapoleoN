package com.naposystems.pepito.ui.conversation.adapter;

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageBinding
import com.naposystems.pepito.entity.Conversation
import com.naposystems.pepito.utility.Constants

class ConversationAdapter constructor(
    private val clickListener: ConversationClickListener
) :
    PagedListAdapter<Conversation, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
    }

    private var isFirst = false

    object DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)
        return if (conversation?.isMine == Constants.IsMine.YES.value) {
            TYPE_MY_MESSAGE
        } else {
            TYPE_INCOMING_MESSAGE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_MY_MESSAGE) {
            MyMessageViewHolder.from(parent)
        } else {
            IncomingMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)

        isFirst = (position + 1 == itemCount ||
                (position + 1 < itemCount && item?.isMine != getItem(position + 1)?.isMine))

        if (getItemViewType(position) == TYPE_MY_MESSAGE) {
            (holder as MyMessageViewHolder).bind(item!!, clickListener, isFirst)
        } else {
            (holder as IncomingMessageViewHolder).bind(item!!, clickListener, isFirst)
        }
    }

    class MyMessageViewHolder constructor(private val binding: ConversationItemMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Conversation, clickListener: ConversationClickListener, isFirst: Boolean) {
            binding.conversation = item
            binding.clickListener = clickListener

            val context = binding.containerMessage.context

            binding.containerMessage.background = if (isFirst) {
                context.getDrawable(R.drawable.bg_my_message)
            } else {
                context.getDrawable(R.drawable.bg_my_message_rounded)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageViewHolder(binding)
            }
        }
    }

    class IncomingMessageViewHolder constructor(private val binding: ConversationItemIncomingMessageBinding) :

        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Conversation, clickListener: ConversationClickListener, isFirst: Boolean) {

            binding.conversation = item

            val context = binding.containerMessage.context

            binding.containerMessage.background = if (isFirst) {
                context.getDrawable(R.drawable.bg_incoming_message)
            } else {
                context.getDrawable(R.drawable.bg_incoming_message_rounded)
            }

            binding.clickListener = clickListener
            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup): IncomingMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageViewHolder(binding)
            }
        }
    }

    class ConversationClickListener(val clickListener: (item: Conversation) -> Unit) {
        fun onClick(item: Conversation) = clickListener(item)
    }
}