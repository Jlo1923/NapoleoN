package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithGifNnBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder

class MyMessageGifNNViewHolder constructor(
    private val binding: ConversationItemMyMessageWithGifNnBinding
) :
    ConversationViewHolder(binding.root) {

    fun setProgress(
        progress: Long
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.setProgress(progress.toFloat())
    }

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener
    ) {
        binding.itemPosition = adapterPosition
        binding.clickListener = clickListener
        binding.conversation = item
        binding.imageViewAttachment.visibility = View.GONE

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
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): MyMessageGifNNViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageWithGifNnBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMessageGifNNViewHolder(binding)
        }
    }
}