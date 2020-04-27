package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithDocumentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.utility.Constants

class IncomingMessageDocumentViewHolder constructor(private val binding: ConversationItemIncomingMessageWithDocumentBinding) :
    RecyclerView.ViewHolder(binding.root) {

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
        binding.conversation = item
        binding.clickListener = clickListener
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat

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

        val firstAttachment = item.attachmentList[0]

        when (firstAttachment.status) {
            Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                val fileName = "${firstAttachment.webId}.${firstAttachment.extension}"
                firstAttachment.status = Constants.AttachmentStatus.DOWNLOADING.status
                firstAttachment.uri = fileName
                clickListener.downloadAttachment(firstAttachment, adapterPosition)
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): IncomingMessageDocumentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageWithDocumentBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMessageDocumentViewHolder(binding)
        }
    }
}