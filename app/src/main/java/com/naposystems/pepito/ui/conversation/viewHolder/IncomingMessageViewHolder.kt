package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageBinding
) : ConversationViewHolder(binding.root) {

    init {
        super.containerMessage = binding.containerIncomingMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonCancel
        super.containerQuote = binding.containerQuote
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
    }

    /*private var job: Job? = null

    fun setStart(job: Job) {
        this.job = job
        binding.imageButtonCancel.visibility = View.VISIBLE
    }

    fun setProgress(
        progress: Long
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.setProgress(progress.toFloat())

        if (progress == 100L){
            binding.progressBar.visibility = View.GONE
        }
    }*/

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