package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.giftnn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.napoleonchat.databinding.ConversationItemIncomingMessageWithGifNnBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageGifNNViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithGifNnBinding
) :
    ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonState
//        super.containerQuote = binding.containerQuote
        super.textViewCountDown = binding.textViewCountDown
//        super.quote = binding.quote
    }

    override fun bind(
        item: MessageAttachmentRelation,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        binding.itemPosition = adapterPosition
        binding.clickListener = clickListener
        binding.conversation = item
        binding.imageViewAttachment.visibility = View.GONE
        binding.timeFormat = timeFormat
        binding.itemPosition = adapterPosition

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): IncomingMessageGifNNViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageWithGifNnBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMessageGifNNViewHolder(binding)
        }
    }
}