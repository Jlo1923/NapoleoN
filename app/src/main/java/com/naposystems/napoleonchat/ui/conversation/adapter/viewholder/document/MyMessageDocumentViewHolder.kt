package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.document

import android.view.LayoutInflater
import android.view.ViewGroup
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageWithDocumentBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

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
        item: MessageAttachmentRelation,
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