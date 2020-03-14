package com.naposystems.pepito.ui.conversation.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithAudioBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithAudioBinding
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import java.io.File

class ConversationAdapter constructor(
    private val clickListener: ClickListener,
    private val mediaPlayerManager: MediaPlayerManager,
    private val resources: Resources
) :
    PagedListAdapter<MessageAndAttachment, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val TYPE_MY_MESSAGE = 1
        const val TYPE_INCOMING_MESSAGE = 2
        const val TYPE_MY_MESSAGE_AUDIO = 3
        const val TYPE_INCOMING_MESSAGE_AUDIO = 4
    }

    private var isFirst = false

    object DiffCallback : DiffUtil.ItemCallback<MessageAndAttachment>() {
        override fun areItemsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem.message.id == newItem.message.id && oldItem.message.status == newItem.message.status && oldItem.message.isSelected == newItem.message.isSelected
        }

        override fun areContentsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)

        conversation.let {
            return if (conversation?.attachmentList!!.isNotEmpty()) {
                when (conversation.attachmentList[0].type) {
                    Constants.AttachmentType.IMAGE.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value) {
                            TYPE_MY_MESSAGE
                        } else {
                            TYPE_INCOMING_MESSAGE
                        }
                    }
                    Constants.AttachmentType.AUDIO.type -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value) {
                            TYPE_MY_MESSAGE_AUDIO
                        } else {
                            TYPE_INCOMING_MESSAGE_AUDIO
                        }
                    }
                    else -> {
                        if (conversation.message.isMine == Constants.IsMine.YES.value) {
                            TYPE_MY_MESSAGE
                        } else {
                            TYPE_INCOMING_MESSAGE
                        }
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
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val countDownTimer: CountDownTimer? = null

        return when (viewType) {
            TYPE_MY_MESSAGE -> MyMessageViewHolder.from(parent, countDownTimer)
            TYPE_INCOMING_MESSAGE -> IncomingMessageViewHolder.from(parent, countDownTimer)
            TYPE_MY_MESSAGE_AUDIO -> MyMessageAudioViewHolder.from(parent, countDownTimer)
            TYPE_INCOMING_MESSAGE_AUDIO -> IncomingMessageAudioViewHolder.from(parent, countDownTimer)
            else -> MyMessageViewHolder.from(parent, countDownTimer)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)

        isFirst = (position + 1 == itemCount ||
                (position + 1 < itemCount && item?.message?.isMine != getItem(position + 1)?.message?.isMine))

        item?.let {
            when (getItemViewType(position)) {
                TYPE_MY_MESSAGE -> (holder as MyMessageViewHolder)
                    .bind(item, clickListener, isFirst, resources)
                TYPE_INCOMING_MESSAGE -> (holder as IncomingMessageViewHolder)
                    .bind(item, clickListener, isFirst, resources)
                TYPE_MY_MESSAGE_AUDIO -> (holder as MyMessageAudioViewHolder)
                    .bind(item, clickListener, isFirst, mediaPlayerManager, resources)
                TYPE_INCOMING_MESSAGE_AUDIO -> (holder as IncomingMessageAudioViewHolder)
                    .bind(item, clickListener, isFirst, mediaPlayerManager, resources)
            }
        }
    }

    class MyMessageViewHolder constructor(private val binding: ConversationItemMyMessageBinding, countDownTimer: CountDownTimer?) :
        ConversationViewHolder(binding.root, countDownTimer) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            resources: Resources
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

            countDown(item, item.message.totalSelfDestructionAt.toLong(), binding.textViewCountDown, resources, itemToEliminate = {messageAndAttachment ->
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
                clickListener.onClick(item.message)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, countDownTimer: CountDownTimer?): MyMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageViewHolder(binding, countDownTimer)
            }
        }
    }

    class IncomingMessageViewHolder constructor(private val binding: ConversationItemIncomingMessageBinding, countDownTimer: CountDownTimer?) :
        ConversationViewHolder(binding.root, countDownTimer) {

        @SuppressLint("ResourceAsColor")
        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            resources: Resources
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

            val context = binding.containerMessage.context

            countDown(item, item.message.totalSelfDestructionAt.toLong(), binding.textViewCountDown, resources, itemToEliminate = {messageAndAttachment ->
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
                clickListener.onClick(item.message)
            }

            binding.containerMessage.background = if (isFirst) {
                context.getDrawable(R.drawable.bg_incoming_message)
            } else {
                context.getDrawable(R.drawable.bg_incoming_message_rounded)
            }

            binding.clickListener = clickListener

            if (item.attachmentList.isNotEmpty()) {
                binding.imageViewAttachment.visibility = View.VISIBLE
                val firstAttachment = item.attachmentList[0]

                Glide.with(context)
                    .load(if (firstAttachment.uri.isNotEmpty()) File(firstAttachment.uri) else firstAttachment.body)
                    .into(binding.imageViewAttachment)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, countDownTimer: CountDownTimer?): IncomingMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageViewHolder(binding, countDownTimer)
            }
        }
    }

    class MyMessageAudioViewHolder constructor(private val binding: ConversationItemMyMessageWithAudioBinding, countDownTimer: CountDownTimer?) :
        ConversationViewHolder(binding.root, countDownTimer) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager,
            resources: Resources
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            countDown(item, item.message.totalSelfDestructionAt.toLong(), binding.textViewCountDown, resources, itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            })

            with(binding.audioPlayer) {
                setMediaPlayerManager(mediaPlayerManager)
                isEncryptedFile(true)
                setAbsolutePath(item.attachmentList[0].uri)
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
            fun from(parent: ViewGroup, countDownTimer: CountDownTimer?): MyMessageAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemMyMessageWithAudioBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return MyMessageAudioViewHolder(binding, countDownTimer)
            }
        }
    }

    class IncomingMessageAudioViewHolder constructor(private val binding: ConversationItemIncomingMessageWithAudioBinding, countDownTimer: CountDownTimer?) :
        ConversationViewHolder(binding.root, countDownTimer){

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager,
            resources: Resources
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

            countDown(item, item.message.totalSelfDestructionAt.toLong(), binding.textViewCountDown, resources, itemToEliminate = {messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            })

            with(binding.audioPlayer) {
                setMediaPlayerManager(mediaPlayerManager)
                isEncryptedFile(true)
                setAbsolutePath(item.attachmentList[0].uri)
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
            fun from(parent: ViewGroup, countDownTimer: CountDownTimer?): IncomingMessageAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageWithAudioBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageAudioViewHolder(binding, countDownTimer)
            }
        }
    }

    interface ClickListener {
        fun onClick(item: Message)
        fun onLongClick(item: Message)
        fun messageToEliminate(item: MessageAndAttachment)
        fun errorPlayingAudio()
    }
}