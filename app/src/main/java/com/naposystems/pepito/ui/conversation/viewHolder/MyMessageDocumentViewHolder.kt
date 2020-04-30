package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithDocumentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder

class MyMessageDocumentViewHolder constructor(private val binding: ConversationItemMyMessageWithDocumentBinding) :
    ConversationViewHolder(binding.root) {

    fun setProgress(
        progress: Long
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.setProgress(progress.toFloat())
    }

    fun setUploadComplete(boolean: Boolean) {
        if (boolean) {
            binding.progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat : Int?
    ) {
        binding.conversation = item
        binding.clickListener = clickListener
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

        item.quote?.let {
            binding.quote.setupMessageAndAttachment(item)
            binding.containerQuote.visibility = View.VISIBLE
        } ?: run {
            binding.containerQuote.visibility = View.GONE
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): MyMessageDocumentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageWithDocumentBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMessageDocumentViewHolder(binding)
        }
    }
}