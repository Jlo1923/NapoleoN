package com.naposystems.pepito.ui.conversation.adapter

import android.graphics.Color
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.ui.custom.circleProgressBar.CircleProgressBar
import com.naposystems.pepito.ui.custom.inputPanel.InputPanelQuote
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class ConversationViewHolder constructor(
    view: View
) : RecyclerView.ViewHolder(view) {

    private var uploadJob: Job? = null
    private var downloadJob: Job? = null
    private var countDownTimer: CountDownTimer? = null


    var containerMessage: ConstraintLayout? = null
    var containerQuote: ConstraintLayout? = null
    var textViewCountDown: TextView? = null
    var quote: InputPanelQuote? = null
    var progressBarIndeterminate: ProgressBar? = null
    var progressBar: CircleProgressBar? = null
    var imageButtonState: AppCompatImageButton? = null
    var audioPlayer: AudioPlayerCustomView? = null
    var imageViewAttachment: ImageView? = null

    fun countDown(
        item: MessageAndAttachment,
        textView: TextView?,
        itemToEliminate: (MessageAndAttachment) -> Unit
    ) {

        countDownTimer?.cancel()
        val endTime = item.message.totalSelfDestructionAt.toLong()
        if (endTime > 0) {
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let { time ->
                val timeInDays = TimeUnit.SECONDS.toDays(time).toInt()
                when {
                    TimeUnit.SECONDS.toDays(time) >= 1 -> {
                        textView?.text = textView?.resources?.getQuantityString(
                            R.plurals.text_self_destruct_time_days, timeInDays, timeInDays
                        )
                    }
                    else -> {
                        countDownTimer = object : CountDownTimer(
                            TimeUnit.SECONDS.toMillis(endTime) - System.currentTimeMillis(),
                            1
                        ) {
                            override fun onFinish() {
                                itemToEliminate(item)
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                if (textView?.isVisible == false) {
                                    textView.visibility = View.VISIBLE
                                }
                                textView?.text = Utils.getDuration(
                                    millisUntilFinished,
                                    showHours = false
                                )
                            }
                        }
                        countDownTimer?.start()
                    }
                }
            }
        } else {
            textView?.text = ""
        }

    }

    fun setProgress(
        progress: Long
    ) {
        Timber.d("setProgressP: $progress")
        progressBar?.visibility = View.VISIBLE
        progressBar?.setProgress(progress.toFloat())
        imageButtonState?.visibility = View.VISIBLE
        progressBarIndeterminate?.visibility = View.GONE

        if (progress == 100L) {
            progressBar?.visibility = View.GONE
        }
    }

    fun setUploadComplete(boolean: Boolean) {
        if (boolean) {
            progressBar?.visibility = View.GONE
            audioPlayer?.enablePlayButton(true)
        }
    }

    fun setUploadStart(job: Job) {
        Timber.d("setUploadStart")
        this.uploadJob = job
        imageButtonState?.visibility = View.VISIBLE
        progressBarIndeterminate?.visibility = View.VISIBLE
    }

    fun setDownloadStart(job: Job) {
        Timber.d("setDownloadStart")
        this.downloadJob = job
        imageButtonState?.visibility = View.VISIBLE
        progressBarIndeterminate?.visibility = View.VISIBLE
    }

    open fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean = false,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager? = null
    ) {
        countDown(
            item,
            textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            })

        if (item.message.isSelected) {
            containerMessage?.setBackgroundColor(Color.parseColor("#BBCCCCCC"))
        } else {
            containerMessage?.setBackgroundColor(Color.TRANSPARENT)
        }

        containerMessage?.setOnLongClickListener {
            clickListener.onLongClick(item.message)
            true
        }

        item.quote?.let {
            quote?.setupMessageAndAttachment(item)
            containerQuote?.visibility = View.VISIBLE
        } ?: run {
            containerQuote?.visibility = View.GONE
        }

        val firstAttachment: Attachment? = item.getFirstAttachment()

        firstAttachment?.let { attachment ->
            Timber.d("attachment.status ${attachment.status}")

            if (item.message.status == Constants.MessageStatus.READED.status &&
                attachment.status == Constants.AttachmentStatus.NOT_DOWNLOADED.status
            ) {
                val fileName = "${attachment.webId}.${attachment.extension}"
                attachment.status = Constants.AttachmentStatus.DOWNLOADING.status
                attachment.uri = fileName
                clickListener.downloadAttachment(attachment, adapterPosition)
            }

            when (attachment.status) {
                Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.INVISIBLE
                    progressBarIndeterminate?.visibility = View.GONE

                    imageButtonState?.setImageResource(R.drawable.ic_file_upload_black)
                    imageButtonState?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.SENDING.status -> {
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    imageButtonState?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.SENT.status -> {
                    if (audioPlayer != null) {
                        audioPlayer?.enablePlayButton(true)
                    }
                }
                Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                    progressBar?.visibility = View.INVISIBLE
                    progressBar?.setProgress(0f)
                    progressBarIndeterminate?.visibility = View.VISIBLE
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    imageButtonState?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.DOWNLOADING.status -> {
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    imageButtonState?.visibility = View.VISIBLE
                    progressBarIndeterminate?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                    progressBar?.visibility = View.GONE
                    progressBar?.setProgress(0f)
                    progressBarIndeterminate?.visibility = View.GONE
                    audioPlayer?.enablePlayButton(true)
                    imageButtonState?.visibility = View.GONE
                    if (audioPlayer != null && mediaPlayerManager != null) {
                        audioPlayer?.enablePlayButton(true)
                        loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
                    }
                }
                Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
                Constants.AttachmentStatus.DOWNLOAD_ERROR.status -> {
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.INVISIBLE
                    progressBarIndeterminate?.visibility = View.INVISIBLE

                    imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                    imageButtonState?.visibility = View.VISIBLE
                }
            }

            imageButtonState?.setOnClickListener(
                imageButtonStateClickListener(
                    attachment,
                    clickListener,
                    item
                )
            )

            imageViewAttachment?.setOnClickListener {
                if (attachment.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status) {
                    clickListener.onPreviewClick(item)
                }
            }

            if (item.message.isMine == Constants.IsMine.YES.value && audioPlayer != null && mediaPlayerManager != null) {
                loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
            }
        }
    }

    private fun loadMediaPlayer(
        mediaPlayerManager: MediaPlayerManager,
        attachment: Attachment,
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener
    ) {
        with(audioPlayer!!) {
            setMediaPlayerManager(mediaPlayerManager)
            isEncryptedFile(false)
            setAudioFileUri(
                Utils.getFileUri(
                    context = context,
                    fileName = attachment.uri,
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
    }

    private fun imageButtonStateClickListener(
        attachment: Attachment,
        clickListener: ConversationAdapter.ClickListener,
        item: MessageAndAttachment
    ) = View.OnClickListener {
        when (attachment.status) {
            Constants.AttachmentStatus.SENDING.status -> {
                if (this.uploadJob?.isActive == true) {
                    this.uploadJob?.cancel()
                } else {
                    attachment.status = Constants.AttachmentStatus.UPLOAD_CANCEL.status
                    clickListener.updateAttachmentState(attachment)
                }
                progressBar?.setProgress(0.0f)
                audioPlayer?.setProgress(0)
                progressBarIndeterminate?.visibility = View.GONE
                audioPlayer?.hideIndeterminateProgress()
                progressBar?.visibility = View.INVISIBLE
                audioPlayer?.hideProgressBar()
            }
            Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                attachment.status = Constants.AttachmentStatus.SENDING.status
                clickListener.uploadAttachment(attachment, item.message)
            }
            Constants.AttachmentStatus.DOWNLOADING.status -> {
                this.downloadJob?.cancel()
                progressBar?.setProgress(0.0f)
                progressBarIndeterminate?.visibility = View.GONE
                progressBar?.visibility = View.GONE
            }
            Constants.AttachmentStatus.DOWNLOAD_ERROR.status,
            Constants.AttachmentStatus.DOWNLOAD_CANCEL.status -> {
                attachment.status = Constants.AttachmentStatus.DOWNLOADING.status
                progressBarIndeterminate?.visibility = View.VISIBLE
                clickListener.downloadAttachment(attachment, adapterPosition)
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                clickListener.onPreviewClick(item)
            }
        }
    }
}