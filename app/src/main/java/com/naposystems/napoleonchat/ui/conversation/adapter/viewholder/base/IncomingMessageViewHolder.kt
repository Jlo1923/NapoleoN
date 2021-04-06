package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.base

import android.view.LayoutInflater
import android.view.ViewGroup
import com.naposystems.napoleonchat.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
        super.textViewMessage = binding.textViewMessage
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
        binding.conversation = item
        binding.clickListener = clickListener
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat
        binding.itemPosition = adapterPosition

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