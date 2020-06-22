package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithImageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageImageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithImageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.containerMessage = binding.containerIncomingMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonCancel
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
        super.imageViewAttachment = binding.imageViewAttachment
    }

    override fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        binding.itemPosition = adapterPosition
        binding.conversation = item
        binding.clickListener = clickListener
        binding.imageViewAttachment.visibility = View.GONE
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): IncomingMessageImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageWithImageBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMessageImageViewHolder(binding)
        }
    }
}