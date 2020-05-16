package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithAudioBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageAudioViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithAudioBinding
) :
    ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.containerMessage = binding.containerIncomingMessage
        super.progressBar = binding.audioPlayer.getProgressBar()
        super.progressBarIndeterminate = binding.audioPlayer.getIndeterminateProgress()
        super.imageButtonState = binding.audioPlayer.getImageButtonState()
        super.containerQuote = binding.containerQuote
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
        super.audioPlayer = binding.audioPlayer
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

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): IncomingMessageAudioViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageWithAudioBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMessageAudioViewHolder(binding)
        }
    }
}