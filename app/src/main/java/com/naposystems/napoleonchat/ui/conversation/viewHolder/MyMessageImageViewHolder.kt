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
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageWithImageBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber

class MyMessageImageViewHolder constructor(
    private val binding: ConversationItemMyMessageWithImageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

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
        binding.imageViewAttachment.visibility = View.GONE
        binding.isFirst = isFirst
        binding.timeFormat = timeFormat
        binding.itemPosition = adapterPosition
        binding.imageViewAttachment.clipToOutline = true

        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): MyMessageImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageWithImageBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMessageImageViewHolder(binding)
        }
    }
}