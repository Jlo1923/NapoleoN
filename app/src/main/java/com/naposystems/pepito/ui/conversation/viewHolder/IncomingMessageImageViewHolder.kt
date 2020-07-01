package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithImageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.BlurTransformation
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber

class IncomingMessageImageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithImageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonCancel
        super.textViewCountDown = binding.textViewCountDown
        super.quote = binding.quote
        super.imageViewAttachment = binding.imageViewAttachment
        super.textViewMessage = binding.textViewMessage
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
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat
        binding.itemPosition = adapterPosition

        bindImageAttachment(item)

        binding.executePendingBindings()
    }

    private fun bindImageAttachment(
        messageAndAttachment: MessageAndAttachment
    ) {
        try {
            val context = binding.imageViewAttachment.context
            messageAndAttachment.getFirstAttachment()?.let { attachment ->

                binding.imageViewAttachment.visibility = View.VISIBLE

                Glide.with(binding.imageViewAttachment)
                    .load(attachment.body)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(8),
                        BlurTransformation(context)
                    )
                    .into(binding.imageViewAttachment)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
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