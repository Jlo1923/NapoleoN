package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithGifNnBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class MyMessageGifNNViewHolder constructor(
    private val binding: ConversationItemMyMessageWithGifNnBinding
) :
    ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.containerMessage = binding.containerMyMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonState
//        super.containerQuote = binding.containerQuote
        super.textViewCountDown = binding.textViewCountDown
//        super.quote = binding.quote
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
        binding.clickListener = clickListener
        binding.conversation = item
        binding.imageViewAttachment.visibility = View.GONE
        binding.timeFormat = timeFormat

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): MyMessageGifNNViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageWithGifNnBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMessageGifNNViewHolder(binding)
        }
    }
}