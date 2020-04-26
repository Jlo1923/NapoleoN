package com.naposystems.pepito.ui.conversation.adapter

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.conversation.viewHolder.*
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class ConversationAdapter constructor(
    private val clickListener: ClickListener,
    private val mediaPlayerManager: MediaPlayerManager
) :
    ListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val PROGRESS = "progress"
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
        const val TYPE_MY_MESSAGE_AUDIO = 3
        const val TYPE_INCOMING_MESSAGE_AUDIO = 4
        const val TYPE_MY_MESSAGE_VIDEO = 5
        const val TYPE_INCOMING_MESSAGE_VIDEO = 6
        const val TYPE_MY_MESSAGE_DOCUMENT = 7
        const val TYPE_INCOMING_MESSAGE_DOCUMENT = 8
        const val TYPE_MY_MESSAGE_GIF = 9
        const val TYPE_INCOMING_MESSAGE_GIF = 10
        const val TYPE_MY_MESSAGE_GIF_NN = 11
        const val TYPE_INCOMING_MESSAGE_GIF_NN = 12
    }

    private var isFirst = false

    object DiffCallback : DiffUtil.ItemCallback<MessageAndAttachment>() {
        override fun areItemsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            val hasAttachments: Boolean
            var oldFirstAttachment: Attachment? = null
            var newFirstAttachment: Attachment? = null
            if (oldItem.attachmentList.isNotEmpty() && newItem.attachmentList.isNotEmpty()) {
                oldFirstAttachment = oldItem.attachmentList.first()
                newFirstAttachment = newItem.attachmentList.first()
                hasAttachments = true
            } else {
                hasAttachments = false
            }
            return if (!hasAttachments) {
                oldItem.message.id == newItem.message.id &&
                        oldItem.message.status == newItem.message.status &&
                        oldItem.message.isSelected == newItem.message.isSelected
            } else {
                return oldItem.message.id == newItem.message.id &&
                        oldItem.message.status == newItem.message.status &&
                        oldItem.message.isSelected == newItem.message.isSelected &&
                        oldFirstAttachment?.status == newFirstAttachment?.status
            }
        }

        override fun areContentsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem == newItem
        }
    }

    fun setProgress(position: Int, progress: Long) {
        notifyItemChanged(position, Bundle().apply { putLong(PROGRESS, progress) })
    }

    fun setUploadProgress(attachment: Attachment, progress: Long) {
        notifyItemChanged(
            getPositionByItem(attachment),
            Bundle().apply { putLong(PROGRESS, progress) })
    }

    private fun getPositionByItem(attachment: Attachment) =
        currentList.indexOfFirst { messageAndAttachment ->
            if (messageAndAttachment.attachmentList.isNotEmpty()) {
                messageAndAttachment.attachmentList.first().id == attachment.id
            } else {
                false
            }
        }

    fun getMessageAndAttachment(position: Int): MessageAndAttachment? {
        return getItem(position)
    }

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)

        conversation?.let {
            return if (conversation.attachmentList.isNotEmpty()) {
                when (conversation.attachmentList[0].type) {
                    Constants.AttachmentType.IMAGE.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE
                        else
                            TYPE_INCOMING_MESSAGE
                    }
                    Constants.AttachmentType.AUDIO.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE_AUDIO
                        else
                            TYPE_INCOMING_MESSAGE_AUDIO
                    }
                    Constants.AttachmentType.VIDEO.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE_VIDEO
                        else
                            TYPE_INCOMING_MESSAGE_VIDEO
                    }
                    Constants.AttachmentType.DOCUMENT.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE_DOCUMENT
                        else
                            TYPE_INCOMING_MESSAGE_DOCUMENT
                    }
                    Constants.AttachmentType.GIF.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value) {
                            TYPE_MY_MESSAGE_GIF
                        } else {
                            TYPE_INCOMING_MESSAGE_GIF
                        }
                    }
                    Constants.AttachmentType.GIF_NN.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value) {
                            TYPE_MY_MESSAGE_GIF_NN
                        } else {
                            TYPE_INCOMING_MESSAGE_GIF_NN
                        }
                    }
                    else -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE
                        else
                            TYPE_INCOMING_MESSAGE
                    }
                }
            } else {
                if (conversation.message.isMine == Constants.IsMine.YES.value) {
                    TYPE_MY_MESSAGE
                } else {
                    TYPE_INCOMING_MESSAGE
                }
            }
        }

        return -1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MY_MESSAGE -> MyMessageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE -> IncomingMessageViewHolder.from(parent)
            TYPE_MY_MESSAGE_AUDIO -> MyMessageAudioViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_AUDIO -> IncomingMessageAudioViewHolder.from(parent)
            TYPE_MY_MESSAGE_VIDEO -> MyMessageVideoViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_VIDEO -> IncomingMessageVideoViewHolder.from(parent)
            TYPE_MY_MESSAGE_DOCUMENT -> MyMessageDocumentViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_DOCUMENT -> IncomingMessageDocumentViewHolder.from(parent)
            TYPE_MY_MESSAGE_GIF -> MyMessageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_GIF -> IncomingMessageViewHolder.from(parent)
            TYPE_MY_MESSAGE_GIF_NN -> MyMessageGifNNViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_GIF_NN -> IncomingMessageGifNNViewHolder.from(parent)
            else -> MyMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)

        isFirst = (position + 1 == itemCount ||
                (position + 1 < itemCount && item?.message?.isMine != getItem(position + 1)?.message?.isMine))

        item?.let {
            when (getItemViewType(position)) {
                TYPE_MY_MESSAGE -> (holder as MyMessageViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_INCOMING_MESSAGE -> (holder as IncomingMessageViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_MY_MESSAGE_AUDIO -> (holder as MyMessageAudioViewHolder)
                    .bind(item, clickListener, isFirst, mediaPlayerManager)
                TYPE_INCOMING_MESSAGE_AUDIO -> (holder as IncomingMessageAudioViewHolder)
                    .bind(item, clickListener, isFirst, mediaPlayerManager)
                TYPE_MY_MESSAGE_VIDEO -> (holder as MyMessageVideoViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_INCOMING_MESSAGE_VIDEO -> (holder as IncomingMessageVideoViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_MY_MESSAGE_DOCUMENT -> (holder as MyMessageDocumentViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_INCOMING_MESSAGE_DOCUMENT -> (holder as IncomingMessageDocumentViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_MY_MESSAGE_GIF -> (holder as MyMessageViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_INCOMING_MESSAGE_GIF -> (holder as IncomingMessageViewHolder)
                    .bind(item, clickListener, isFirst)
                TYPE_MY_MESSAGE_GIF_NN -> (holder as MyMessageGifNNViewHolder)
                    .bind(item, clickListener)
                TYPE_INCOMING_MESSAGE_GIF_NN -> (holder as IncomingMessageGifNNViewHolder)
                    .bind(item, clickListener)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.firstOrNull() != null) {
            val item = getItem(position)

            item?.let {
                val bundle = payloads.first() as Bundle
                val progress = bundle.getLong(PROGRESS)
                when (getItemViewType(position)) {
                    TYPE_MY_MESSAGE -> {
                        (holder as MyMessageViewHolder).setProgress(progress)
                    }
                    TYPE_MY_MESSAGE_VIDEO -> {
                        (holder as MyMessageVideoViewHolder).setProgress(progress)
                    }
                    TYPE_MY_MESSAGE_GIF -> {
                        (holder as MyMessageViewHolder).setProgress(progress)
                    }
                    TYPE_MY_MESSAGE_GIF_NN -> {
                        (holder as MyMessageGifNNViewHolder).setProgress(progress)
                    }
                    TYPE_MY_MESSAGE_AUDIO -> {
                        (holder as MyMessageAudioViewHolder).setProgress(progress)
                    }
                    TYPE_MY_MESSAGE_DOCUMENT -> {
                        (holder as MyMessageDocumentViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE -> {
                        (holder as IncomingMessageViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE_VIDEO -> {
                        (holder as IncomingMessageVideoViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE_GIF -> {
                        (holder as IncomingMessageViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE_GIF_NN -> {
                        (holder as IncomingMessageGifNNViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE_AUDIO -> {
                        (holder as IncomingMessageAudioViewHolder).setProgress(progress)
                    }
                    TYPE_INCOMING_MESSAGE_DOCUMENT -> {
                        (holder as IncomingMessageDocumentViewHolder).setProgress(progress)
                    }
                    else -> {
                        // Intentionally empty
                    }
                }
            }
        }
    }

    interface ClickListener {
        fun onClick(item: MessageAndAttachment)
        fun onLongClick(item: Message)
        fun messageToEliminate(item: MessageAndAttachment)
        fun errorPlayingAudio()
        fun onPreviewClick(item: MessageAndAttachment)
        fun goToQuote(messageAndAttachment: MessageAndAttachment)
        fun downloadAttachment(attachment: Attachment, itemPosition: Int?)
    }
}