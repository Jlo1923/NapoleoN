package com.naposystems.pepito.ui.conversation.viewHolder

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithImageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.utility.BlurTransformation
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber

class IncomingMessageImageViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithImageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonState
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
        binding.imageViewAttachment.visibility = View.VISIBLE
        binding.imageViewAttachment.clipToOutline = true

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

                val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()

                transformationList.add(CenterCrop())
                transformationList.add(RoundedCorners(8))

                if (attachment.type != Constants.AttachmentType.LOCATION.type) {
                    transformationList.add(BlurTransformation(context))
                }

                Glide.with(binding.imageViewAttachment)
                    .load(attachment.body)
                    .transform(
                        *transformationList.toTypedArray()
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