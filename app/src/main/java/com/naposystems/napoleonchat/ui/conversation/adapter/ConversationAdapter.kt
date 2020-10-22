package com.naposystems.napoleonchat.ui.conversation.adapter

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.ui.conversation.viewHolder.*
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import timber.log.Timber

class ConversationAdapter constructor(
    private val clickListener: ClickListener,
    private val mediaPlayerManager: MediaPlayerManager,
    private var timeFormat: Int?
) :
    ListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val IS_UPLOAD = "is_upload"
        const val COMPRESS_PROGRESS = "compress_progress"
        const val PROGRESS = "progress"
        const val DOWNLOAD_START = "download_start"
        const val DOWNLOAD_COMPLETE = "download_complete"
        const val DOWNLOAD_CANCEL = "download_cancel"
        const val UPLOAD_COMPLETE = "upload_complete"
        const val FOCUS_MESSAGE = "focus_message"
        const val PLAY_AUDIO = "play_audio"
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
        const val TYPE_MY_MESSAGE_IMAGE = 3
        const val TYPE_INCOMING_MESSAGE_IMAGE = 4
        const val TYPE_MY_MESSAGE_AUDIO = 5
        const val TYPE_INCOMING_MESSAGE_AUDIO = 6
        const val TYPE_MY_MESSAGE_VIDEO = 7
        const val TYPE_INCOMING_MESSAGE_VIDEO = 8
        const val TYPE_MY_MESSAGE_DOCUMENT = 9
        const val TYPE_INCOMING_MESSAGE_DOCUMENT = 10
        const val TYPE_MY_MESSAGE_GIF = 11
        const val TYPE_INCOMING_MESSAGE_GIF = 12
        const val TYPE_MY_MESSAGE_GIF_NN = 13
        const val TYPE_INCOMING_MESSAGE_GIF_NN = 14
        const val TYPE_MY_MESSAGE_LOCATION = 15
        const val TYPE_INCOMING_MESSAGE_LOCATION = 16
        const val TYPE_MISSED_CALL = 17
        const val TYPE_SYSTEM_MESSAGE = 18
        const val TYPE_GROUP_DATE_MESSAGES = 19
    }

    private var isFirst = false
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

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

    fun setStartDownload(itemPosition: Int, job: Job) {
        try {
            Timber.d("setStartDownload")
            notifyItemChanged(itemPosition, job)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setDownloadProgress(position: Int, progress: Float) {
        try {
            notifyItemChanged(position, Bundle().apply { putFloat(PROGRESS, progress) })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setDownloadComplete(position: Int) {
        try {
            notifyItemChanged(position, Bundle().apply { putBoolean(DOWNLOAD_COMPLETE, true) })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setDownloadCancel(position: Int) {
        try {
            notifyItemChanged(position, Bundle().apply { putBoolean(DOWNLOAD_CANCEL, true) })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setUploadStart(attachment: Attachment) {
        try {
            notifyItemChanged(
                getPositionByItem(attachment),
                Bundle().apply { putBoolean(IS_UPLOAD, true) })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setCompressProgress(
        attachment: Attachment,
        progress: Float
    ) {
        try {
            notifyItemChanged(
                getPositionByItem(attachment),
                listOf(Bundle().apply { putFloat(COMPRESS_PROGRESS, progress) })
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setUploadProgress(
        attachment: Attachment,
        progress: Float
    ) {
        try {
            notifyItemChanged(
                getPositionByItem(attachment),
                Bundle().apply {
                    putFloat(PROGRESS, progress)
                    putBoolean(IS_UPLOAD, true)
                }
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

    fun startFocusAnimation(mQuotedMessage: Int?) {
        if (mQuotedMessage != null) {
            notifyItemChanged(
                mQuotedMessage,
                Bundle().apply { putBoolean(FOCUS_MESSAGE, true) }
            )
        }
    }

    fun checkIfNextIsAudio(messageId: String) {
        val positionActualAudio = getPositionByMessageId(messageId)
        if (positionActualAudio >= 0) {
            val actualMessageAndAttachment = getItem(positionActualAudio)
            val nextPosition = positionActualAudio + 1

            if (nextPosition < currentList.size) {
                val nextItem = getItem(nextPosition)

                nextItem.getFirstAttachment()?.let { attachment ->
                    if (attachment.type == Constants.AttachmentType.AUDIO.type &&
                        nextItem.message.isMine == actualMessageAndAttachment.message.isMine
                    ) {
                        if (nextItem.message.isMine == Constants.IsMine.NO.value) {
                            if (attachment.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status) {
                                clickListener.scrollToNextAudio(nextPosition)
                            } else {
                                mediaPlayerManager.resetMediaPlayer()
                            }
                        } else {
                            clickListener.scrollToNextAudio(nextPosition)
                        }
                    } else {
                        mediaPlayerManager.resetMediaPlayer()
                    }
                } ?: run {
                    mediaPlayerManager.resetMediaPlayer()
                }
            } else {
                mediaPlayerManager.resetMediaPlayer()
            }
        }
    }

    fun notifyPlayAudio(position: Int) {
        notifyItemChanged(
            position,
            Bundle().apply { putBoolean(PLAY_AUDIO, true) }
        )
    }

    private fun getPositionByMessageId(id: String) =
        currentList.indexOfFirst { messageAndAttachment ->
            messageAndAttachment.message.id.toString() == id
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
            return when (conversation.message.messageType) {
                Constants.MessageType.MESSAGE.type -> {
                    if (conversation.attachmentList.isNotEmpty()) {
                        when (conversation.attachmentList[0].type) {
                            Constants.AttachmentType.IMAGE.type -> {
                                if (conversation.message.isMine == Constants.IsMine.YES.value)
                                    TYPE_MY_MESSAGE_IMAGE
                                else
                                    TYPE_INCOMING_MESSAGE_IMAGE
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
                            Constants.AttachmentType.LOCATION.type -> {
                                if (conversation.message.isMine == Constants.IsMine.YES.value) {
                                    TYPE_MY_MESSAGE_LOCATION
                                } else {
                                    TYPE_INCOMING_MESSAGE_LOCATION
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
                Constants.MessageType.NEW_CONTACT.type -> TYPE_SYSTEM_MESSAGE
                Constants.MessageType.MESSAGES_GROUP_DATE.type -> TYPE_GROUP_DATE_MESSAGES
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
            TYPE_MY_MESSAGE_IMAGE -> MyMessageImageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_IMAGE -> IncomingMessageImageViewHolder.from(parent)
            TYPE_MY_MESSAGE_AUDIO -> MyMessageAudioViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_AUDIO -> IncomingMessageAudioViewHolder.from(parent)
            TYPE_MY_MESSAGE_VIDEO -> MyMessageVideoViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_VIDEO -> IncomingMessageVideoViewHolder.from(parent)
            TYPE_MY_MESSAGE_DOCUMENT -> MyMessageDocumentViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_DOCUMENT -> IncomingMessageDocumentViewHolder.from(parent)
            TYPE_MY_MESSAGE_GIF -> MyMessageImageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_GIF -> IncomingMessageImageViewHolder.from(parent)
            TYPE_MY_MESSAGE_GIF_NN -> MyMessageGifNNViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_GIF_NN -> IncomingMessageGifNNViewHolder.from(parent)
            TYPE_MY_MESSAGE_LOCATION -> MyMessageImageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_LOCATION -> IncomingMessageImageViewHolder.from(parent)
            TYPE_MISSED_CALL -> MessageMissedCallViewHolder.from(parent)
            TYPE_SYSTEM_MESSAGE -> SystemMessageViewHolder.from(parent)
            TYPE_GROUP_DATE_MESSAGES -> GroupDateMessageViewHolder.from(parent)
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
                TYPE_MY_MESSAGE_IMAGE,
                TYPE_MY_MESSAGE_GIF,
                TYPE_MY_MESSAGE_VIDEO,
                TYPE_MY_MESSAGE_DOCUMENT,
                TYPE_MY_MESSAGE_GIF_NN,
                TYPE_MY_MESSAGE_LOCATION,
                TYPE_INCOMING_MESSAGE,
                TYPE_INCOMING_MESSAGE_IMAGE,
                TYPE_INCOMING_MESSAGE_GIF,
                TYPE_INCOMING_MESSAGE_VIDEO,
                TYPE_INCOMING_MESSAGE_GIF_NN,
                TYPE_INCOMING_MESSAGE_DOCUMENT,
                TYPE_INCOMING_MESSAGE_LOCATION,
                TYPE_MISSED_CALL,
                TYPE_SYSTEM_MESSAGE ->
                    (holder as ConversationViewHolder)
                        .bind(item, clickListener, isFirst, timeFormat)
                TYPE_MY_MESSAGE_AUDIO,
                TYPE_INCOMING_MESSAGE_AUDIO -> {
                    (holder as ConversationViewHolder)
                        .bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
                }
                TYPE_GROUP_DATE_MESSAGES -> (holder as GroupDateMessageViewHolder)
                    .bind(item)
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
                        is Job -> handleJobPayload(any, position, holder)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun handleBundlePayload(
        bundle: Bundle,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val isUpload = bundle.getBoolean(IS_UPLOAD, false)
        val progress = bundle.getFloat(PROGRESS, -1f)
        val uploadComplete = bundle.getBoolean(UPLOAD_COMPLETE, false)
        val downloadCancel = bundle.getBoolean(DOWNLOAD_CANCEL, false)
        val focusMessage = bundle.getBoolean(FOCUS_MESSAGE, false)
        val playAudio = bundle.getBoolean(PLAY_AUDIO, false)
        val downloadComplete = bundle.getBoolean(DOWNLOAD_COMPLETE, false)
        val compressProgress = bundle.getFloat(COMPRESS_PROGRESS, -1f)
        when (getItemViewType(position)) {
            TYPE_MY_MESSAGE_IMAGE,
            TYPE_MY_MESSAGE_GIF,
            TYPE_MY_MESSAGE_VIDEO,
            TYPE_MY_MESSAGE_GIF_NN,
            TYPE_MY_MESSAGE_AUDIO,
            TYPE_MY_MESSAGE_DOCUMENT,
            TYPE_MY_MESSAGE_LOCATION -> {
                (holder as ConversationViewHolder).apply {
                    if (isUpload) {
                        if (progress > -1) {
                            setUploadProgressAndJob(progress)
                        }
                        if (compressProgress > -1) {
                            setCompressProgressAndJob(progress)
                        }
                        setUploadComplete(uploadComplete)
                    } else {
                        setProgress(progress)
                        setUploadComplete(uploadComplete)
                        startFocusAnim(focusMessage)
                        playAudio(playAudio)
                    }
                }
            }
            TYPE_INCOMING_MESSAGE_IMAGE,
            TYPE_INCOMING_MESSAGE_GIF,
            TYPE_INCOMING_MESSAGE_VIDEO,
            TYPE_INCOMING_MESSAGE_GIF_NN,
            TYPE_INCOMING_MESSAGE_DOCUMENT,
            TYPE_INCOMING_MESSAGE_AUDIO,
            TYPE_INCOMING_MESSAGE_LOCATION -> {
                (holder as ConversationViewHolder).apply {
                    setProgress(progress)
                    setDownloadCancel(downloadCancel)
                    startFocusAnim(focusMessage)
                    playAudio(playAudio)
                    setDownloadComplete(downloadComplete)
                }
            }
            TYPE_MY_MESSAGE,
            TYPE_INCOMING_MESSAGE -> {
                (holder as ConversationViewHolder).apply {
                    startFocusAnim(focusMessage)
                }
            }
        }
    }

    private fun handleJobPayload(
        job: Job,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        when (getItemViewType(position)) {
            TYPE_INCOMING_MESSAGE_IMAGE,
            TYPE_INCOMING_MESSAGE_GIF,
            TYPE_INCOMING_MESSAGE_VIDEO,
            TYPE_INCOMING_MESSAGE_GIF_NN,
            TYPE_INCOMING_MESSAGE_DOCUMENT,
            TYPE_INCOMING_MESSAGE_AUDIO,
            TYPE_INCOMING_MESSAGE_LOCATION -> {
                (holder as ConversationViewHolder).apply {
                    setDownloadStart(job)
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
        fun goToQuote(messageAndAttachment: MessageAndAttachment, itemPosition: Int?)
        fun downloadAttachment(messageAndAttachment: MessageAndAttachment, itemPosition: Int?)
        fun uploadAttachment(attachment: Attachment, message: Message)
        fun updateAttachmentState(messageAndAttachment: Attachment)
        fun sendMessageRead(messageAndAttachment: MessageAndAttachment)
        fun sendMessageRead(
            messageId: String,
            messageWebId: String,
            isComplete: Boolean,
            position: Int = -1
        )

        fun reSendMessage(message: Message)
        fun scrollToNextAudio(nextPosition: Int)
        fun updateMessageState(message: Message)
    }
}