package com.naposystems.napoleonchat.ui.conversation.viewHolder

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemIncomingMessageWithImageBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
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
        super.imageButtonShow = binding.imageViewIconShow
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

                when (attachment.type) {
                    Constants.AttachmentType.IMAGE.type,
                    Constants.AttachmentType.VIDEO.type -> {
                        transformationList.add(BlurTransformation(context))
                    }
                    Constants.AttachmentType.GIF.type -> {
                        binding.imageViewIconShow.apply {
                            setImageDrawable(context.getDrawable(R.drawable.ic_gif_black))
                            setColorFilter(ContextCompat.getColor(context, R.color.white))
                        }
                        binding.containerBrandGiphy.visibility = View.VISIBLE
                    }
                }

                transformationList.add(RoundedCorners(8))

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