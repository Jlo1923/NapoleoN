package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithVideoBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.Constants
import timber.log.Timber

class IncomingMessageVideoViewHolder constructor(private val binding: ConversationItemIncomingMessageWithVideoBinding) :
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
        isFirst: Boolean,
        timeFormat : Int?
    ) {
        Timber.d("Bind")
        binding.itemPosition = adapterPosition
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

        val firstAttachment: Attachment? = item.getFirstAttachment()

        if (item.message.isSelected) {
            binding.containerIncomingMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
        } else {
            binding.containerIncomingMessage.setBackgroundColor(Color.TRANSPARENT)
        }

        item.quote?.let {
            binding.quote.setupMessageAndAttachment(item)
            binding.containerQuote.visibility = View.VISIBLE
        } ?: run {
            binding.containerQuote.visibility = View.GONE
        }

        when (firstAttachment?.status) {
            Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                binding.progressBar.visibility = View.GONE
                binding.imageViewPlay.visibility = View.GONE
                binding.progressBar.setProgress(0f)
//                binding.imageViewAttachment.setImageDrawable(null)
            }
            Constants.AttachmentStatus.DOWNLOADING.status -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status ->{
                binding.progressBar.visibility = View.GONE
                binding.imageViewPlay.visibility = View.VISIBLE
            }
        }

        binding.imageViewAttachment.setOnClickListener {
            if (firstAttachment?.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status) {
                binding.imageViewPlay.visibility = View.VISIBLE
                clickListener.onPreviewClick(item)
            }
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): IncomingMessageVideoViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageWithVideoBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMessageVideoViewHolder(binding)
        }
    }
}