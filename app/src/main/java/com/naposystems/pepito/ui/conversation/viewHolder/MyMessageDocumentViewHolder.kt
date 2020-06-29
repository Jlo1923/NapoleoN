package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithDocumentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class MyMessageDocumentViewHolder constructor(private val binding: ConversationItemMyMessageWithDocumentBinding) :
    ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerMyMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonState
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
    }

    override fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        binding.conversation = item
        binding.clickListener = clickListener
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat
        binding.itemPosition = adapterPosition

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