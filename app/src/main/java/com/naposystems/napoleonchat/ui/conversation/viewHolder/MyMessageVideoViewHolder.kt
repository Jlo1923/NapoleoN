package com.naposystems.napoleonchat.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageWithVideoBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
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
                    .thumbnail(0.1f)
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