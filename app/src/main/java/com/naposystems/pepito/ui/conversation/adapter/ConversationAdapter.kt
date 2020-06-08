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
import com.naposystems.pepito.utility.DownloadAttachmentResult
import com.naposystems.pepito.utility.UploadResult
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import timber.log.Timber

class ConversationAdapter constructor(
    private val clickListener: ClickListener,
    private val mediaPlayerManager: MediaPlayerManager,
    private var timeFormat: Int?
) :
    ListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val PROGRESS = "progress"
        const val UPLOAD_COMPLETE = "upload_complete"
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
        const val TYPE_MISSED_CALL = 13
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
                oldItem.message.id == newItem.message.id &&
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

    fun setStartDownload(itemPosition: Int, job: ProducerScope<DownloadAttachmentResult>) {
        try {
            notifyItemChanged(itemPosition, job)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setProgress(position: Int, progress: Long) {
        try {
            notifyItemChanged(position, Bundle().apply { putLong(PROGRESS, progress) })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setUploadStart(attachment: Attachment, job: ProducerScope<UploadResult>) {
        try {
            notifyItemChanged(getPositionByItem(attachment), job)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setUploadProgress(attachment: Attachment, progress: Long, job: ProducerScope<UploadResult>) {
        try {
            notifyItemChanged(
                getPositionByItem(attachment),
                listOf(Bundle().apply { putLong(PROGRESS, progress) }, job)
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setUploadComplete(attachment: Attachment) {
        try {
            notifyItemChanged(
                getPositionByItem(attachment),
                Bundle().apply { putBoolean(UPLOAD_COMPLETE, true) }
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun getPositionByItem(attachment: Attachment) =
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
            return when (conversation.message.messageType) {
                Constants.MessageType.MESSAGE.type -> {
                    if (conversation.attachmentList.isNotEmpty()) {
                        when (conversation.attachmentList[0].type) {
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
                        if (conversation.message.isMine == Constants.IsMine.YES.value)
                            TYPE_MY_MESSAGE
                        else
                            TYPE_INCOMING_MESSAGE
                    }
                }
                Constants.MessageType.MISSED_CALL.type,
                Constants.MessageType.MISSED_VIDEO_CALL.type -> TYPE_MISSED_CALL
                else -> {
                    if (conversation.message.isMine == Constants.IsMine.YES.value) {
                        TYPE_MY_MESSAGE
                    } else {
                        TYPE_INCOMING_MESSAGE
                    }
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
            TYPE_MISSED_CALL -> MessageMissedCallViewHolder.from(parent)
            else -> MyMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        try {
            isFirst = if (position == 0) {
                true
            } else {
                (position - 1 == itemCount ||
                        (position - 1 < itemCount && item?.message?.isMine != getItem(position - 1)?.message?.isMine))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        item?.let {
            when (getItemViewType(position)) {
                TYPE_MY_MESSAGE,
                TYPE_MY_MESSAGE_GIF,
                TYPE_MY_MESSAGE_VIDEO,
                TYPE_MY_MESSAGE_DOCUMENT,
                TYPE_MY_MESSAGE_GIF_NN,
                TYPE_INCOMING_MESSAGE,
                TYPE_INCOMING_MESSAGE_GIF,
                TYPE_INCOMING_MESSAGE_VIDEO,
                TYPE_INCOMING_MESSAGE_GIF_NN,
                TYPE_INCOMING_MESSAGE_DOCUMENT ->
                    (holder as ConversationViewHolder)
                        .bind(item, clickListener, isFirst, timeFormat)
                TYPE_MY_MESSAGE_AUDIO,
                TYPE_INCOMING_MESSAGE_AUDIO -> {
                    (holder as ConversationViewHolder)
                        .bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
                }
                TYPE_MISSED_CALL -> (holder as MessageMissedCallViewHolder)
                    .bind(item, clickListener, timeFormat)
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

            Timber.d("onBindViewHolder with payload: ${payloads.first()}")

            item?.let {
                try {
                    when (val any = payloads.first()) {
                        is Bundle -> handleBundlePayload(any, position, holder)
                        is ProducerScope<*> -> handleProducerScopePayload(any, position, holder)
                        is List<*> -> {
                            if (any.isNotEmpty()) {
                                handleBundleAndJobPayload(
                                    any[0] as Bundle,
                                    any[1] as ProducerScope<*>,
                                    position,
                                    holder
                                )
                                /*any.forEach { anyItem: Any? ->
                                    when (anyItem) {
                                        is Bundle -> handleBundlePayload(anyItem, position, holder)
                                        is Job -> handleJobPayload(anyItem, position, holder)
                                    }
                                }*/
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Que mierda pasa")
                }
            }
        }
    }

    private fun handleProducerScopePayload(job: ProducerScope<*>, position: Int, holder: RecyclerView.ViewHolder) {
        Timber.d("handleProducerScopePayload: ${getItemViewType(position)}")
        when (getItemViewType(position)) {
            TYPE_INCOMING_MESSAGE,
            TYPE_INCOMING_MESSAGE_VIDEO,
            TYPE_INCOMING_MESSAGE_GIF,
            TYPE_INCOMING_MESSAGE_GIF_NN,
            TYPE_INCOMING_MESSAGE_AUDIO,
            TYPE_INCOMING_MESSAGE_DOCUMENT -> {
                (holder as ConversationViewHolder).apply {
                    setDownloadStart(job)
                }
            }
            TYPE_MY_MESSAGE,
            TYPE_MY_MESSAGE_VIDEO,
            TYPE_MY_MESSAGE_GIF,
            TYPE_MY_MESSAGE_GIF_NN,
            TYPE_MY_MESSAGE_AUDIO,
            TYPE_MY_MESSAGE_DOCUMENT -> {
                (holder as ConversationViewHolder).apply {
                    setUploadStart(job)
                }
            }
        }
    }

    private fun handleBundlePayload(
        bundle: Bundle,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val progress = bundle.getLong(PROGRESS)
        val uploadComplete = bundle.getBoolean(UPLOAD_COMPLETE, false)
        when (getItemViewType(position)) {
            TYPE_MY_MESSAGE,
            TYPE_MY_MESSAGE_GIF,
            TYPE_MY_MESSAGE_VIDEO,
            TYPE_MY_MESSAGE_GIF_NN,
            TYPE_MY_MESSAGE_AUDIO,
            TYPE_MY_MESSAGE_DOCUMENT -> {
                (holder as ConversationViewHolder).apply {
                    setProgress(progress)
                    setUploadComplete(uploadComplete)
                }
            }
            TYPE_INCOMING_MESSAGE,
            TYPE_INCOMING_MESSAGE_GIF,
            TYPE_INCOMING_MESSAGE_VIDEO,
            TYPE_INCOMING_MESSAGE_GIF_NN,
            TYPE_INCOMING_MESSAGE_DOCUMENT,
            TYPE_INCOMING_MESSAGE_AUDIO -> {
                (holder as ConversationViewHolder).setProgress(progress)
            }
        }
    }

    private fun handleBundleAndJobPayload(
        bundle: Bundle,
        job: ProducerScope<*>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val progress = bundle.getLong(PROGRESS)
        val uploadComplete = bundle.getBoolean(UPLOAD_COMPLETE, false)
        when (getItemViewType(position)) {
            TYPE_MY_MESSAGE,
            TYPE_MY_MESSAGE_GIF,
            TYPE_MY_MESSAGE_VIDEO,
            TYPE_MY_MESSAGE_GIF_NN,
            TYPE_MY_MESSAGE_AUDIO,
            TYPE_MY_MESSAGE_DOCUMENT -> {
                (holder as ConversationViewHolder).apply {
                    setUploadProgressAndJob(progress, job)
                    setUploadComplete(uploadComplete)
                }
            }
            TYPE_INCOMING_MESSAGE,
            TYPE_INCOMING_MESSAGE_GIF,
            TYPE_INCOMING_MESSAGE_VIDEO,
            TYPE_INCOMING_MESSAGE_GIF_NN,
            TYPE_INCOMING_MESSAGE_DOCUMENT,
            TYPE_INCOMING_MESSAGE_AUDIO -> {
                (holder as ConversationViewHolder).setDownloadProgressAndJob(progress, job)
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
        fun downloadAttachment(messageAndAttachment: MessageAndAttachment, itemPosition: Int?)
        fun uploadAttachment(attachment: Attachment, message: Message)
        fun updateAttachmentState(messageAndAttachment: Attachment)
        fun sendMessageRead(messageAndAttachment: MessageAndAttachment)
    }
}