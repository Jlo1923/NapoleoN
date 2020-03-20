package com.naposystems.pepito.ui.conversation.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.*
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class ConversationAdapter constructor(
    private val clickListener: ClickListener,
    private val mediaPlayerManager: MediaPlayerManager
) :
    PagedListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
        const val TYPE_MY_MESSAGE_AUDIO = 3
        const val TYPE_INCOMING_MESSAGE_AUDIO = 4
        const val TYPE_MY_MESSAGE_VIDEO = 5
        const val TYPE_INCOMING_MESSAGE_VIDEO = 6
        const val TYPE_MY_MESSAGE_DOCUMENT = 7
        const val TYPE_INCOMING_MESSAGE_DOCUMENT = 8
    }

    private var isFirst = false

    object DiffCallback : DiffUtil.ItemCallback<MessageAndAttachment>() {
        override fun areItemsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem.message.id == newItem.message.id &&
                    oldItem.message.status == newItem.message.status &&
                    oldItem.message.isSelected == newItem.message.isSelected
        }

        override fun areContentsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem == newItem
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
            TYPE_INCOMING_MESSAGE_AUDIO -> IncomingMessageAudioViewHolder.from(
                parent
            )
            TYPE_MY_MESSAGE_VIDEO -> MyMessageVideoViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_VIDEO -> IncomingMessageVideoViewHolder.from(parent)
            TYPE_MY_MESSAGE_DOCUMENT -> MyMessageDocumentViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_DOCUMENT -> IncomingMessageDocumentViewHolder.from(parent)
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
            }
        }
    }

    class MyMessageViewHolder constructor(
        private val binding: ConversationItemMyMessageBinding
    ) :
        ConversationViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

            countDown(
                item,
                binding.textViewCountDown,
                itemToEliminate = { messageAndAttachment ->
                    clickListener.messageToEliminate(messageAndAttachment)
                })

            if (item.message.isSelected) {
                binding.containerMyMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerMyMessage.setBackgroundColor(Color.TRANSPARENT)
            }

            binding.containerMyMessage.setOnLongClickListener {
                clickListener.onLongClick(item.message)
                true
            }

            binding.containerMyMessage.setOnClickListener {
                clickListener.onClick(item)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageViewHolder(binding)
            }
        }
    }

    class IncomingMessageViewHolder constructor(
        private val binding: ConversationItemIncomingMessageBinding
    ) :
        ConversationViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

            countDown(
                item,
                binding.textViewCountDown,
                itemToEliminate = { messageAndAttachment ->
                    clickListener.messageToEliminate(messageAndAttachment)
                })

            if (item.message.isSelected) {
                binding.containerIncomingMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerIncomingMessage.setBackgroundColor(Color.TRANSPARENT)
            }

            binding.containerIncomingMessage.setOnLongClickListener {
                clickListener.onLongClick(item.message)
                true
            }

            binding.containerIncomingMessage.setOnClickListener {
                clickListener.onClick(item)
            }

            binding.clickListener = clickListener

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

    class MyMessageAudioViewHolder constructor(
        private val binding: ConversationItemMyMessageWithAudioBinding
    ) :
        ConversationViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            countDown(
                item,
                binding.textViewCountDown,
                itemToEliminate = { messageAndAttachment ->
                    clickListener.messageToEliminate(messageAndAttachment)
                })

            with(binding.audioPlayer) {

                val firstAttachment = item.attachmentList[0]

                setMediaPlayerManager(mediaPlayerManager)
                isEncryptedFile(false)
                setAudioFileUri(
                    Utils.getFileUri(
                        context = context,
                        fileName = firstAttachment.uri,
                        subFolder = Constants.NapoleonCacheDirectories.AUDIOS.folder
                    )
                )
                setAudioId(item.attachmentList[0].id)
                setListener(object : AudioPlayerCustomView.Listener {
                    override fun onErrorPlayingAudio() {
                        clickListener.errorPlayingAudio()
                    }
                })
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyMessageAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageWithAudioBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageAudioViewHolder(binding)
            }
        }
    }

    class IncomingMessageAudioViewHolder constructor(
        private val binding: ConversationItemIncomingMessageWithAudioBinding
    ) :
        ConversationViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            countDown(
                item,
                binding.textViewCountDown,
                itemToEliminate = { messageAndAttachment ->
                    clickListener.messageToEliminate(messageAndAttachment)
                })

            with(binding.audioPlayer) {
                val firstAttachment = item.attachmentList[0]

                setMediaPlayerManager(mediaPlayerManager)
                isEncryptedFile(false)
                setAudioFileUri(
                    Utils.getFileUri(
                        context = context,
                        fileName = firstAttachment.uri,
                        subFolder = Constants.NapoleonCacheDirectories.AUDIOS.folder
                    )
                )
                setAudioId(item.attachmentList[0].id)
                setListener(object : AudioPlayerCustomView.Listener {
                    override fun onErrorPlayingAudio() {
                        clickListener.errorPlayingAudio()
                    }
                })
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(
                parent: ViewGroup
            ): IncomingMessageAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageWithAudioBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageAudioViewHolder(binding/*, countDownTimer*/)
            }
        }
    }

    class MyMessageVideoViewHolder constructor(private val binding: ConversationItemMyMessageWithVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            if (item.message.isSelected) {
                binding.containerMyMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerMyMessage.setBackgroundColor(Color.TRANSPARENT)
            }
            binding.executePendingBindings()
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

    class IncomingMessageVideoViewHolder constructor(private val binding: ConversationItemIncomingMessageWithVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            if (item.message.isSelected) {
                binding.containerIncomingMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerIncomingMessage.setBackgroundColor(Color.TRANSPARENT)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): IncomingMessageVideoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageWithVideoBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageVideoViewHolder(binding)
            }
        }
    }

    class MyMessageDocumentViewHolder constructor(private val binding: ConversationItemMyMessageWithDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            if (item.message.isSelected) {
                binding.containerMyMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerMyMessage.setBackgroundColor(Color.TRANSPARENT)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyMessageDocumentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageWithDocumentBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageDocumentViewHolder(binding)
            }
        }
    }

    class IncomingMessageDocumentViewHolder constructor(private val binding: ConversationItemIncomingMessageWithDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            if (item.message.isSelected) {
                binding.containerIncomingMessage.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
            } else {
                binding.containerIncomingMessage.setBackgroundColor(Color.TRANSPARENT)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): IncomingMessageDocumentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageWithDocumentBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageDocumentViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onClick(item: MessageAndAttachment)
        fun onLongClick(item: Message)
        fun messageToEliminate(item: MessageAndAttachment)
        fun errorPlayingAudio()
        fun onPreviewClick(item: MessageAndAttachment)
    }
}