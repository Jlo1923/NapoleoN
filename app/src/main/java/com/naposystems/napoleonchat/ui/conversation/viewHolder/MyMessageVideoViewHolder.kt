package com.naposystems.napoleonchat.ui.conversation.viewHolder

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageWithVideoBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber

class MyMessageVideoViewHolder constructor(private val binding: ConversationItemMyMessageWithVideoBinding) :
    ConversationViewHolder(binding.root, binding.root.context) {

//    private var job: Job? = null

    init {
        super.parentContainerMessage = binding.containerMyMessage
        super.progressBar = binding.progressBar
        super.progressBarIndeterminate = binding.progressBarIndeterminate
        super.imageButtonState = binding.imageButtonState
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

        bindImageAttachment(item)

        binding.executePendingBindings()
    }

    private fun bindImageAttachment(
        messageAndAttachmentRelation: MessageAttachmentRelation
    ) {
        try {
            val context = binding.imageViewAttachment.context
            messageAndAttachmentRelation.getFirstAttachment()?.let { attachment ->

                val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()

                transformationList.add(CenterCrop())
                transformationList.add(BlurTransformation(context))
                transformationList.add(RoundedCorners(8))

                Glide.with(binding.imageViewAttachment)
                    .load(attachment.body)
                    .transform(
                        *transformationList.toTypedArray()
                    )
                    .into(binding.imageViewAttachment)

                binding.imageViewAttachment.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun from(parent: ViewGroup): MyMessageVideoViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageWithVideoBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMessageVideoViewHolder(binding)
        }
    }
}