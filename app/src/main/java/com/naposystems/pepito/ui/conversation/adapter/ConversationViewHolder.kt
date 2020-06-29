package com.naposystems.pepito.ui.conversation.adapter

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.pepito.ui.custom.circleProgressBar.CircleProgressBar
import com.naposystems.pepito.ui.custom.inputPanel.InputPanelQuote
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class ConversationViewHolder constructor(
    view: View,
    private val context: Context
) : RecyclerView.ViewHolder(view) {

    private var uploadJob: ProducerScope<*>? = null
    private var downloadJob: ProducerScope<*>? = null
    private var countDownTimer: CountDownTimer? = null

    var parentContainerMessage: ConstraintLayout? = null
    var textViewCountDown: TextView? = null
    var quote: InputPanelQuote? = null
    var progressBarIndeterminate: ProgressBar? = null
    var progressBar: CircleProgressBar? = null
    var imageButtonState: AppCompatImageButton? = null
    var audioPlayer: AudioPlayerCustomView? = null
    var imageViewAttachment: ImageView? = null
    var imageButtonSend: AppCompatImageButton? = null
    var textViewMessage: TextView? = null

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
            showDestructionTime(item)
        }
    }

    fun setProgress(
        progress: Long
    ) {
        try {
            if (this.downloadJob?.isActive == true) {
                Timber.d(" setProgress this.uploadjob: ${this.uploadJob}, this.downloadJob: ${this.downloadJob}")

                if (progress > 0) {
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.setProgress(progress.toFloat())
                    imageButtonState?.visibility = View.VISIBLE
                    progressBarIndeterminate?.visibility = View.GONE
                }

                if (progress == 100L) {
                    progressBar?.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Timber.d(e)
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
            progressBarIndeterminate?.visibility = View.GONE
        }
    }

    fun setUploadProgressAndJob(
        progress: Long,
        job: ProducerScope<*>
    ) {
        Timber.d("progress: $progress, job: $job")
        progressBar?.visibility = View.VISIBLE
        progressBar?.setProgress(progress.toFloat())

        if (progress > 0) {
            progressBarIndeterminate?.visibility = View.GONE
        }

        if (progress == 100L) {
            progressBar?.visibility = View.GONE
//            imageButtonState?.visibility = View.GONE
        }
        this.uploadJob = job

        if (job.isClosedForSend) {
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
        }
    }

    fun setDownloadProgressAndJob(
        progress: Long,
        job: ProducerScope<*>
    ) {
        Timber.d("progress: $progress, job: $job")
        progressBar?.visibility = View.VISIBLE
        progressBar?.setProgress(progress.toFloat())

        if (progress > 0) {
            progressBarIndeterminate?.visibility = View.GONE
        }

        if (progress == 100L) {
            progressBar?.visibility = View.GONE
//            imageButtonState?.visibility = View.GONE
        }

        this.downloadJob = job
    }

    fun setUploadComplete(boolean: Boolean) {
        if (boolean) {
            progressBar?.visibility = View.GONE
            imageButtonState?.visibility = View.GONE
            Timber.d("enablePlayButton setUploadComplete: true")
            audioPlayer?.enablePlayButton(true)
        }
    }

    fun setUploadStart(job: ProducerScope<*>) {
        Timber.d("setUploadStart: $job")
        this.uploadJob = job
//        imageButtonState?.visibility = View.VISIBLE
        progressBarIndeterminate?.visibility = View.VISIBLE
    }

    fun setDownloadStart(job: ProducerScope<*>) {
        Timber.d("setDownloadStart: $job")
        this.downloadJob = job
        imageButtonState?.visibility = View.GONE
        progressBar?.setProgress(0f)
        progressBar?.visibility = View.INVISIBLE
        progressBarIndeterminate?.visibility = View.VISIBLE
    }

    fun setDownloadCancel(isCancel: Boolean) {
        if (isCancel) {
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
            progressBarIndeterminate?.visibility = View.GONE
            imageButtonState?.visibility = View.VISIBLE
            imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
        }
    }

    fun startFocusAnim(focusMessage: Boolean) {
        if (focusMessage) {
            /*val objectAnimatorMic = AnimatorInflater.loadAnimator(
                context,
                R.animator.animator_focus_message
            ) as ObjectAnimator

            objectAnimatorMic.target = containerMessage
            objectAnimatorMic.start()*/
            val colorAnim =
                ObjectAnimator.ofInt(
                    parentContainerMessage,
                    "backgroundColor",
                    Color.parseColor("#59CCCCCC"),
                    Color.TRANSPARENT
                )
            colorAnim.duration = 300
            colorAnim.setEvaluator(ArgbEvaluator())
            colorAnim.interpolator = FastOutSlowInInterpolator()
            colorAnim.repeatCount = 3
            colorAnim.repeatMode = ValueAnimator.REVERSE
            colorAnim.start()

            colorAnim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit

                override fun onAnimationEnd(animation: Animator) {
                    parentContainerMessage?.setBackgroundColor(Color.TRANSPARENT)
                }

                override fun onAnimationCancel(animation: Animator) = Unit

                override fun onAnimationStart(animation: Animator) = Unit
            })
        }
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
            }
        )

        if (item.message.isSelected) {
            parentContainerMessage?.setBackgroundColor(Color.parseColor("#59CCCCCC"))
        } else {
            parentContainerMessage?.setBackgroundColor(Color.TRANSPARENT)
        }

        parentContainerMessage?.setOnLongClickListener {
            clickListener.onLongClick(item.message)
            true
        }

        item.quote?.let {
            quote?.setupMessageAndAttachment(item)
            quote?.visibility = View.VISIBLE
        } ?: run {
            quote?.visibility = View.GONE
        }

        audioPlayer?.enablePlayButton(false)
        imageViewAttachment?.visibility = View.GONE
        imageButtonState?.visibility = View.GONE
        progressBar?.setProgress(0.0f)
        progressBar?.visibility = View.GONE
        progressBarIndeterminate?.visibility = View.GONE

        val firstAttachment: Attachment? = item.getFirstAttachment()

        firstAttachment?.let { attachment ->
//            Timber.d("message.id: ${item.message.id}, attachment.id: ${attachment.id}, message.status ${item.message.status}, attachment.status ${attachment.status}, job: ${this.downloadJob}")
//            Timber.d("hasUploadComplete: $hasUploadComplete")

            audioPlayer?.setDuration(attachment.duration)

            if (item.message.status == Constants.MessageStatus.UNREAD.status &&
                attachment.status == Constants.AttachmentStatus.NOT_DOWNLOADED.status
            ) {
                Timber.d("Attachment status: ${attachment.status}, uri: ${attachment.uri}")
                clickListener.downloadAttachment(item, adapterPosition)
            }

            when (attachment.status) {
                Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE

//                    imageButtonState?.setImageResource(R.drawable.ic_file_upload_black)
//                    imageButtonState?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.SENDING.status -> {
//                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
//                    imageButtonState?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.SENT.status -> {
//                    imageButtonState?.visibility = View.INVISIBLE
                    progressBarIndeterminate?.visibility = View.GONE
                    progressBar?.visibility = View.INVISIBLE
                    progressBar?.setProgress(0f)
                    Timber.d("enablePlayButton SENT: true")
                    audioPlayer?.enablePlayButton(true)
                }
                Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                    progressBar?.setProgress(0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    imageButtonState?.visibility = View.GONE
                }
                Constants.AttachmentStatus.DOWNLOADING.status -> {
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    progressBarIndeterminate?.visibility = View.VISIBLE
                }
                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                    progressBar?.visibility = View.GONE
                    progressBar?.setProgress(0f)
                    progressBarIndeterminate?.visibility = View.GONE
                    imageButtonState?.visibility = View.GONE
                    if (audioPlayer != null && mediaPlayerManager != null) {
                        Timber.d("enablePlayButton DOWNLOAD_COMPLETE: true")
                        audioPlayer?.enablePlayButton(true)
                        loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
                        progressBar?.visibility = View.GONE
                        imageButtonState?.visibility = View.GONE
                    }

                    if (attachment.type == Constants.AttachmentType.GIF_NN.type && item.message.status == Constants.MessageStatus.UNREAD.status) {
                        clickListener.sendMessageRead(item)
                    }
                }
                Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
                Constants.AttachmentStatus.DOWNLOAD_ERROR.status -> {
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE

                    Timber.d("imageButtonState?.visibility = ${imageButtonState?.visibility}")
                    imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                    imageButtonState?.visibility = View.VISIBLE
                    Timber.d("imageButtonState?.visibility = ${imageButtonState?.visibility}")
                }
                Constants.AttachmentStatus.ERROR.status -> {
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE

                    if (item.message.isMine == Constants.IsMine.YES.value) {
//                        imageButtonState?.setImageResource(R.drawable.ic_file_upload_black)
                    } else {
//                        imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                    }

//                    imageButtonState?.visibility = View.VISIBLE
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
                if (attachment.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status ||
                    attachment.status == Constants.AttachmentStatus.SENT.status
                ) {
                    clickListener.onPreviewClick(item)
                }
            }

            if (item.message.isMine == Constants.IsMine.YES.value && audioPlayer != null && mediaPlayerManager != null) {
                loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
            }
        }

        imageButtonSend?.setOnClickListener {
            if (item.message.status == Constants.MessageStatus.ERROR.status) {
                clickListener.reSendMessage(item.message)
            }
        }
    }

    private fun showDestructionTime(messageAndAttachment: MessageAndAttachment) {
        val message = messageAndAttachment.message
        val stringId = when (message.selfDestructionAt) {
            Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time -> R.string.text_every_five_seconds
            Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time -> R.string.text_every_fifteen_seconds
            Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time -> R.string.text_every_thirty_seconds
            Constants.SelfDestructTime.EVERY_ONE_MINUTE.time -> R.string.text_every_one_minute
            Constants.SelfDestructTime.EVERY_TEN_MINUTES.time -> R.string.text_every_ten_minutes
            Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time -> R.string.text_every_thirty_minutes
            Constants.SelfDestructTime.EVERY_ONE_HOUR.time -> R.string.text_every_one_hour
            Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time -> R.string.text_every_twelve_hours
            Constants.SelfDestructTime.EVERY_ONE_DAY.time -> R.string.text_every_one_day
            Constants.SelfDestructTime.EVERY_SEVEN_DAY.time -> R.string.text_every_seven_days
            else -> -1
        }
        if (stringId != -1) {
            textViewCountDown?.visibility = View.VISIBLE
            textViewCountDown?.text = context.resources.getString(stringId)
        }
    }

    private fun loadMediaPlayer(
        mediaPlayerManager: MediaPlayerManager,
        attachment: Attachment,
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener
    ) {
        with(audioPlayer!!) {
            setMessageAndAttachment(item)
            setMediaPlayerManager(mediaPlayerManager)
            isEncryptedFile(BuildConfig.ENCRYPT_API)
            if (BuildConfig.ENCRYPT_API) {
                setEncryptedFileName("${attachment.webId}.${attachment.extension}")
            } else {
                setAudioFileUri(
                    Utils.getFileUri(
                        context = context,
                        fileName = attachment.uri,
                        subFolder = Constants.NapoleonCacheDirectories.AUDIOS.folder
                    )
                )
            }
            setAudioId(item.attachmentList[0].id)
            setListener(object : AudioPlayerCustomView.Listener {
                override fun onErrorPlayingAudio() {
                    clickListener.errorPlayingAudio()
                }

                override fun onPause(messageAndAttachment: MessageAndAttachment?) {
                    clickListener.sendMessageRead(item)
                }

                override fun onComplete(messageAndAttachment: MessageAndAttachment?) {
                    clickListener.sendMessageRead(item)
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
                Timber.d("this.uploadJob: ${this.uploadJob}")
                if (this.uploadJob?.isActive == true) {
                    this.uploadJob?.close()
                } else {
                    attachment.status = Constants.AttachmentStatus.UPLOAD_CANCEL.status
                    clickListener.updateAttachmentState(attachment)
                }
                progressBar?.setProgress(0.0f)
                audioPlayer?.setProgress(0)
                audioPlayer?.hideIndeterminateProgress()
                progressBar?.visibility = View.INVISIBLE
                audioPlayer?.hideProgressBar()
            }
            Constants.AttachmentStatus.UPLOAD_CANCEL.status,
            Constants.AttachmentStatus.ERROR.status -> {
                attachment.status = Constants.AttachmentStatus.SENDING.status
                clickListener.uploadAttachment(attachment, item.message)
            }
            Constants.AttachmentStatus.DOWNLOADING.status -> {
                try {
                    if (this.downloadJob?.isActive == true) {
                        this.downloadJob?.close()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                progressBar?.setProgress(0.0f)
                progressBar?.visibility = View.GONE
                imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                attachment.status = Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                clickListener.updateAttachmentState(attachment)
            }
            Constants.AttachmentStatus.DOWNLOAD_ERROR.status,
            Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
            Constants.AttachmentStatus.ERROR.status -> {
                progressBarIndeterminate?.visibility = View.VISIBLE
                imageButtonState?.visibility = View.GONE
                clickListener.downloadAttachment(item, adapterPosition)
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status,
            Constants.AttachmentStatus.SENT.status -> {
                clickListener.onPreviewClick(item)
            }
        }
    }
}