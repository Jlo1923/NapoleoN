package com.naposystems.pepito.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import java.io.File

class ConversationAdapter constructor(
    private val clickListener: ConversationClickListener
) :
    PagedListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
    }

    private var isFirst = false

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

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)
        return if (conversation?.message?.isMine == Constants.IsMine.YES.value) {
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
                (position + 1 < itemCount && item?.message?.isMine != getItem(position + 1)?.message?.isMine))

        if (getItemViewType(position) == TYPE_MY_MESSAGE) {
            item?.let { (holder as MyMessageViewHolder).bind(it, clickListener, isFirst) }
        } else {
            item?.let { (holder as IncomingMessageViewHolder).bind(it, clickListener, isFirst) }
        }
    }

    class MyMessageViewHolder constructor(private val binding: ConversationItemMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ConversationClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE

            val context = binding.containerMessage.context

            binding.containerMessage.background = if (isFirst) {
                context.getDrawable(R.drawable.bg_my_message)
            } else {
                context.getDrawable(R.drawable.bg_my_message_rounded)
            }

            if (item.attachmentList.isNotEmpty()) {
                binding.imageViewAttachment.visibility = View.VISIBLE
                val firstAttachment = item.attachmentList[0]

                Glide.with(context)
                    .load(File(firstAttachment.uri))
                    .into(binding.imageViewAttachment)
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

        fun bind(
            item: MessageAndAttachment,
            clickListener: ConversationClickListener,
            isFirst: Boolean
        ) {

            binding.conversation = item
            binding.imageViewAttachment.visibility = View.GONE

            val context = binding.containerMessage.context

            binding.containerMessage.background = if (isFirst) {
                context.getDrawable(R.drawable.bg_incoming_message)
            } else {
                context.getDrawable(R.drawable.bg_incoming_message_rounded)
            }

            binding.clickListener = clickListener

            if (item.attachmentList.isNotEmpty()) {
                binding.imageViewAttachment.visibility = View.VISIBLE
                val firstAttachment = item.attachmentList[0]

                Glide.with(context)
                    .load(if (firstAttachment.uri.isNotEmpty()) File(firstAttachment.uri) else firstAttachment.body)
                    .into(binding.imageViewAttachment)
            }

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

    class ConversationClickListener(val clickListener: (item: MessageAndAttachment) -> Unit) {
        fun onClick(item: MessageAndAttachment) = clickListener(item)
    }
}