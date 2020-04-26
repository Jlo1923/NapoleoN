package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.Constants

class IncomingMessageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageBinding
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
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean
    ) {
        binding.itemPosition = adapterPosition
        binding.conversation = item
        binding.clickListener = clickListener
        binding.imageViewAttachment.visibility = View.GONE
        binding.isFirst = isFirst

        val firstAttachment: Attachment? = item.getFirstAttachment()

        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            })

        if (item.message.isSelected) {
            binding.containerIncomingMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
        } else {
            binding.containerIncomingMessage.setBackgroundColor(Color.TRANSPARENT)
        }

        binding.containerIncomingMessage.setOnLongClickListener {
            clickListener.onLongClick(item.message)
            true
        }

        binding.containerIncomingMessage.setOnClickListener {
            clickListener.onClick(item)
        }

        when(firstAttachment?.status){
            Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                binding.progressBar.visibility = View.GONE
                binding.progressBar.setProgress(0f)
                binding.imageViewAttachment.setImageDrawable(null)
            }
            Constants.AttachmentStatus.DOWNLOADING.status -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            else -> binding.progressBar.visibility = View.GONE
        }

        binding.imageViewAttachment.setOnClickListener {
            if (firstAttachment?.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status) {
                clickListener.onPreviewClick(item)
            }
        }

        item.quote?.let {
            binding.containerQuote.visibility = View.VISIBLE
            binding.quote.setupMessageAndAttachment(item)
        } ?: run {
            binding.containerQuote.visibility = View.GONE
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): IncomingMessageViewHolder {
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