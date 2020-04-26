package com.naposystems.pepito.ui.conversation.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naposystems.pepito.databinding.ConversationItemIncomingMessageWithAudioBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

class IncomingMessageAudioViewHolder constructor(
    private val binding: ConversationItemIncomingMessageWithAudioBinding
) :
    ConversationViewHolder(binding.root) {

    fun setProgress(
        progress: Long
    ) {
        binding.audioPlayer.enablePlayButton(false)
        binding.audioPlayer.setProgress(progress)
    }

    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
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

            when (firstAttachment.status) {
                Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                    val fileName = "${firstAttachment.webId}.${firstAttachment.extension}"
                    firstAttachment.status = Constants.AttachmentStatus.DOWNLOADING.status
                    firstAttachment.uri = fileName
                    clickListener.downloadAttachment(firstAttachment, adapterPosition)
                }
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                    setMediaPlayerManager(mediaPlayerManager)
                    isEncryptedFile(false)
                    setAudioFileUri(
                        Utils.getFileUri(
                            context = context,
                            fileName = firstAttachment.uri,
                            subFolder = Constants.NapoleonCacheDirectories.AUDIOS.folder
                        )
                    )
                    setAudioId(firstAttachment.id)
                    setListener(object : AudioPlayerCustomView.Listener {
                        override fun onErrorPlayingAudio() {
                            clickListener.errorPlayingAudio()
                        }
                    })

                    binding.audioPlayer.enablePlayButton(true)
                }
            }
        }

        item.quote?.let {
            binding.quote.setupMessageAndAttachment(item)
            binding.containerQuote.visibility = View.VISIBLE
        } ?: run {
            binding.containerQuote.visibility = View.GONE
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
            return IncomingMessageAudioViewHolder(binding)
        }
    }
}