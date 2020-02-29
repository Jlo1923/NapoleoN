package com.naposystems.pepito.ui.conversation.adapter

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageBinding
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithAudioBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageBinding
import com.naposystems.pepito.databinding.ConversationItemMyMessageWithAudioBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.GlideManager
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

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
    }

    private var isFirst = false

    object DiffCallback : DiffUtil.ItemCallback<MessageAndAttachment>() {
        override fun areItemsTheSame(
            oldItem: MessageAndAttachment,
            newItem: MessageAndAttachment
        ): Boolean {
            return oldItem.message.id == newItem.message.id && oldItem.message.status == newItem.message.status
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MY_MESSAGE -> MyMessageViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE -> IncomingMessageViewHolder.from(parent)
            TYPE_MY_MESSAGE_AUDIO -> MyMessageAudioViewHolder.from(parent)
            TYPE_INCOMING_MESSAGE_AUDIO -> IncomingMessageAudioViewHolder.from(parent)
            else -> MyMessageViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)

        isFirst = (position + 1 == itemCount ||
                (position + 1 < itemCount && item?.message?.isMine != getItem(position + 1)?.message?.isMine))

        when (getItemViewType(position)) {
            TYPE_MY_MESSAGE -> (holder as MyMessageViewHolder)
                .bind(item!!, clickListener, isFirst)
            TYPE_INCOMING_MESSAGE -> (holder as IncomingMessageViewHolder)
                .bind(item!!, clickListener, isFirst)
            TYPE_MY_MESSAGE_AUDIO -> (holder as MyMessageAudioViewHolder)
                .bind(item!!, clickListener, isFirst, mediaPlayerManager)
            TYPE_INCOMING_MESSAGE_AUDIO -> (holder as IncomingMessageAudioViewHolder)
                .bind(item!!, clickListener, isFirst, mediaPlayerManager)
        }
    }

    class MyMessageViewHolder constructor(private val binding: ConversationItemMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

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

    class IncomingMessageViewHolder constructor(private val binding: ConversationItemIncomingMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean
        ) {

            binding.conversation = item
            binding.imageViewAttachment.visibility = View.GONE
            binding.isFirst = isFirst

            val context = binding.containerMessage.context

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
            fun from(parent: ViewGroup): IncomingMessageViewHolder {
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

    class MyMessageAudioViewHolder constructor(private val binding: ConversationItemMyMessageWithAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

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

    class IncomingMessageAudioViewHolder constructor(private val binding: ConversationItemIncomingMessageWithAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageAndAttachment,
            clickListener: ClickListener,
            isFirst: Boolean,
            mediaPlayerManager: MediaPlayerManager
        ) {
            binding.conversation = item
            binding.clickListener = clickListener
            binding.isFirst = isFirst

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
            fun from(parent: ViewGroup): IncomingMessageAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationItemIncomingMessageWithAudioBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return IncomingMessageAudioViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun clickListener(item: MessageAndAttachment)
        fun errorPlayingAudio()
    }
}