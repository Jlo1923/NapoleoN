package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemMyMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder

class MyMessageViewHolder constructor(
    private val binding: ConversationItemMyMessageBinding
) : ConversationViewHolder(binding.root) {

    fun setProgress(
        progress: Long
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.setProgress(progress.toFloat())
    }

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat : Int?
    ) {
        binding.itemPosition = adapterPosition
        binding.conversation = item
        binding.clickListener = clickListener
        binding.imageViewAttachment.visibility = View.GONE
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat

        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            })

        if (item.message.isSelected) {
            binding.containerMyMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
        } else {
            binding.containerMyMessage.setBackgroundColor(Color.TRANSPARENT)
        }

        binding.containerMyMessage.setOnLongClickListener {
            clickListener.onLongClick(item.message)
            true
        }

        binding.containerMyMessage.setOnClickListener {
            clickListener.onClick(item)
        }

        item.quote?.let {
            binding.quote.setupMessageAndAttachment(item)
            binding.containerQuote.visibility = View.VISIBLE
        } ?: run {
            binding.containerQuote.visibility = View.GONE
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