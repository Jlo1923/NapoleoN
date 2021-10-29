package com.naposystems.napoleonchat.ui.conversation.adapter

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.service.uploadService.UploadService
import com.naposystems.napoleonchat.ui.custom.audioPlayer.AudioPlayerCustomView
import com.naposystems.napoleonchat.ui.custom.circleProgressBar.CircleProgressBar
import com.naposystems.napoleonchat.ui.custom.inputPanel.InputPanelQuote
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class ConversationViewHolder constructor(
    view: View,
    private val context: Context
) : RecyclerView.ViewHolder(view) {

    private var downloadJob: Job? = null
    private var countDownTimer: CountDownTimer? = null

    var parentContainerMessage: View? = null
    var textViewCountDown: TextView? = null
    var quote: InputPanelQuote? = null
    var progressBarIndeterminate: ProgressBar? = null
    var progressBar: CircleProgressBar? = null
    var imageButtonState: AppCompatImageButton? = null
    var audioPlayer: AudioPlayerCustomView? = null
    var imageViewAttachment: ImageView? = null
    var imageButtonSend: AppCompatImageButton? = null
    var textViewMessage: TextView? = null
    var imageButtonPlay: AppCompatImageButton? = null
    var imageButtonShow: AppCompatImageButton? = null

    var progressVisibility = false

    open fun countDown(
        item: MessageAttachmentRelation,
        textView: TextView?,
        itemToEliminate: (MessageAttachmentRelation) -> Unit
    ) {

        countDownTimer?.cancel()
        val endTime = item.messageEntity.totalSelfDestructionAt.toLong()
        if (endTime > 0) {
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let {
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

                        val text = if(item.messageEntity.status != Constants.MessageStatus.ERROR.status){
                            //correcto
                            Utils.getTimeWithDays(
                                millisUntilFinished,
                                showHours = true
                            )
                        } else {
                            Utils.getTimeForMessageError(
                                millisUntilFinished
                            )
                        }
                        textView?.text = text
                    }
                }
                countDownTimer?.start()
            }
        } else {
            showDestructionTime(item)
        }
    }

    fun setProgress(
        progress: Float
    ) {
        try {
            if (this.downloadJob?.isActive == true) {
                Timber.d(" setProgress this.downloadJob: ${this.downloadJob}")

                if (progress > 0) {
                    Timber.d("*Test: Download Progress: $progress")
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.setProgress(progress.toFloat())
                    imageButtonState?.visibility = View.VISIBLE
                    progressBarIndeterminate?.visibility = View.GONE
                }

                if (progress == 100f) {
                    Timber.d("*Test: Download Success: $progress")
                    progressBar?.visibility = View.GONE
                    imageButtonState?.visibility = View.INVISIBLE
                    imageButtonPlay?.visibility = View.VISIBLE
                    imageButtonShow?.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
            progressBarIndeterminate?.visibility = View.GONE
        }
    }

    fun setUploadProgressAndJob(
        progress: Float
    ) {
        Timber.d("*Test: Upload Progress: $progress")
        progressBar?.let { circleProgressBar ->
            when {
                progress < 10f -> {
                    circleProgressBar.visibility = View.VISIBLE
                    circleProgressBar.setProgress(progress)
                    progressVisibility = true
                    progressBarIndeterminate?.visibility = View.GONE

                    imageButtonState?.visibility = View.VISIBLE
                    imageButtonState?.isEnabled = false
                }
                progress in 10f..80f -> {
                    circleProgressBar.setProgress(progress)
                    progressBarIndeterminate?.visibility = View.GONE

                    imageButtonState?.visibility = View.VISIBLE
                    imageButtonState?.isEnabled = true
                }
                progress >= 80f && progressVisibility -> {
                    circleProgressBar.setProgress(100f)
                    circleProgressBar.visibility = View.GONE
                    progressVisibility = false
                    Timber.d("*Test: Full Progress")
                    progressBarIndeterminate?.visibility = View.VISIBLE

                    imageButtonState?.isEnabled = false
                    imageButtonState?.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun setCompressProgressAndJob(
        progress: Float
    ) {
        Timber.d("*Test: Compress Progress: $progress")
    }

    fun setDownloadProgressAndJob(
        progress: Float,
        job: ProducerScope<*>
    ) {
        Timber.d("*Test: Download Progress: $progress, job: $job")
        progressBar?.visibility = View.VISIBLE
        progressBar?.setProgress(progress)

        if (progress > 0) {
            progressBarIndeterminate?.visibility = View.GONE
        }

        if (progress == 100f) {
            Timber.d("*Test: Full Download")
            progressBar?.visibility = View.GONE
//            imageButtonState?.visibility = View.GONE
        }

//        this.downloadJob = job
    }

    fun setUploadComplete(boolean: Boolean) {
        if (boolean) {
            progressBar?.visibility = View.GONE
            imageButtonState?.visibility = View.INVISIBLE
            Timber.d("enablePlayButton setUploadComplete: true")
            audioPlayer?.enablePlayButton(true)
        }
    }

    fun setDownloadStart(job: Job) {
        Timber.d("setDownloadStart: $job")
        this.downloadJob = job
        imageButtonState?.visibility = View.VISIBLE
        progressBar?.setProgress(0f)
        progressBar?.visibility = View.INVISIBLE
        imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
        progressBarIndeterminate?.visibility = View.VISIBLE
    }

    fun setDownloadComplete(isDownloadComplete: Boolean) {
        Timber.d("isDownloadComplete: $isDownloadComplete")
        if (isDownloadComplete) {
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
            progressBarIndeterminate?.visibility = View.GONE
            imageButtonState?.visibility = View.INVISIBLE
            imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
            imageButtonPlay?.visibility = View.VISIBLE
            imageButtonShow?.visibility = View.VISIBLE
        }
    }

    fun setDownloadCancel(isCancel: Boolean) {
        Timber.d("setDownloadCancel: $isCancel")
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
            val colorAnim =
                ObjectAnimator.ofInt(
                    parentContainerMessage,
                    "backgroundColor",
                    Color.parseColor("#99CCCCCC"),
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

    fun playAudio(playAudio: Boolean) {
        if (playAudio) {
            audioPlayer?.apply {
            }
            audioPlayer?.playAudio()
        }
    }

    fun setStateMessage(state: Int) {
        imageButtonSend?.isEnabled =
            !(state == Constants.StateMessage.START.state || state == Constants.StateMessage.SUCCESS.state)
    }

    open fun bind(
        item: MessageAttachmentRelation,
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

        if (item.messageEntity.isSelected) {
            parentContainerMessage?.setBackgroundColor(Color.parseColor("#99CCCCCC"))
        } else {
            parentContainerMessage?.setBackgroundColor(Color.TRANSPARENT)
        }

        parentContainerMessage?.setOnLongClickListener {
            clickListener.onLongClick(item.messageEntity)
            true
        }

        item.quoteEntity?.let {
            quote?.setupMessageAndAttachment(item)
            quote?.visibility = View.VISIBLE
        } ?: run {
            quote?.visibility = View.GONE
        }

        val firstAttachmentEntity = if (item.messageEntity.numberAttachments == 1) {
            item.getFirstAttachment()
        } else {
            null
        }

        firstAttachmentEntity?.let { attachment ->
            Timber.d("message.id: ${item.messageEntity.id}, attachment.id: ${attachment.id}, message.status ${item.messageEntity.status}, attachment.status ${attachment.status}, job: ${this.downloadJob}")
//            Timber.d("hasUploadComplete: $hasUploadComplete")

            if (item.messageEntity.status == Constants.MessageStatus.UNREAD.status &&
                attachment.status == Constants.AttachmentStatus.NOT_DOWNLOADED.status
            ) {
                Timber.d("Attachment status: ${attachment.status}, uri: ${attachment.fileName}")
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
                    progressBarIndeterminate?.isVisible = true
                }

                Constants.AttachmentStatus.SENT.status -> {
//                    imageButtonState?.visibility = View.INVISIBLE
                    progressBarIndeterminate?.visibility = View.GONE
                    progressBar?.visibility = View.INVISIBLE
                    progressBar?.setProgress(0f)
                }

                Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                    progressBar?.setProgress(0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    imageButtonState?.visibility = View.INVISIBLE
                }

                Constants.AttachmentStatus.DOWNLOADING.status -> {
                    imageButtonState?.setImageResource(R.drawable.ic_close_black_24)
                    progressBarIndeterminate?.visibility = View.VISIBLE
                }

                Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                    progressBar?.visibility = View.GONE
                    progressBar?.setProgress(0f)
                    progressBarIndeterminate?.visibility = View.GONE
                    imageButtonState?.visibility = View.INVISIBLE
                    if (audioPlayer != null && mediaPlayerManager != null) {
                        Timber.d("enablePlayButton DOWNLOAD_COMPLETE: true")
                        audioPlayer?.enablePlayButton(true)
                        loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
                        progressBar?.visibility = View.GONE
                        imageButtonState?.visibility = View.INVISIBLE
                    }

                    if (attachment.type == Constants.AttachmentType.GIF_NN.type && item.messageEntity.status == Constants.MessageStatus.UNREAD.status ||
                        attachment.type == Constants.AttachmentType.GIF.type && item.messageEntity.status == Constants.MessageStatus.UNREAD.status
                    ) {
                        clickListener.sendMessageRead(item)
                    }

                    imageButtonPlay?.visibility = View.VISIBLE
                    imageButtonShow?.visibility = View.VISIBLE
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

                    if (item.messageEntity.isMine == Constants.IsMine.YES.value) {
//                        imageButtonState?.setImageResource(R.drawable.ic_file_upload_black)
                    } else {
//                        imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                    }

//                    imageButtonState?.visibility = View.VISIBLE
                }
                else -> {
                    imageViewAttachment?.visibility = View.GONE
                    imageButtonState?.visibility = View.INVISIBLE
                    progressBar?.setProgress(0.0f)
                    progressBar?.visibility = View.GONE
                    progressBarIndeterminate?.visibility = View.GONE
                    imageButtonPlay?.visibility = View.GONE
                    imageButtonShow?.visibility = View.GONE
                }
            }

            imageButtonState?.let {
                RxView.clicks(it)
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        imageButtonStateClickListener(
                            attachment,
                            clickListener,
                            item
                        )
                    }
            }

            imageViewAttachment?.setOnClickListener {
                imageButtonShow?.let { button ->
                    if (attachment.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status ||
                        attachment.status == Constants.AttachmentStatus.SENT.status ||
                        button.isVisible
                    ) {
                        clickListener.onPreviewClick(item)
                    }
                }
            }

            if (item.messageEntity.isMine == Constants.IsMine.YES.value && audioPlayer != null && mediaPlayerManager != null) {
                loadMediaPlayer(mediaPlayerManager, attachment, item, clickListener)
            }
        } ?: run {
            imageViewAttachment?.visibility = View.GONE
            imageButtonState?.visibility = View.INVISIBLE
            progressBar?.setProgress(0.0f)
            progressBar?.visibility = View.GONE
            progressBarIndeterminate?.visibility = View.GONE
            imageButtonPlay?.visibility = View.GONE
            imageButtonShow?.visibility = View.GONE
        }

        imageButtonSend?.setSafeOnClickListener {
            if (item.messageEntity.status == Constants.MessageStatus.ERROR.status) {
                imageButtonSend?.isEnabled = false
                clickListener.reSendMessage(item.messageEntity)
            }
        }
    }

    private fun showDestructionTime(messageAndAttachmentRelation: MessageAttachmentRelation) {
        val message = messageAndAttachmentRelation.messageEntity

        val sharedPreferencesManager: SharedPreferencesManager = SharedPreferencesManager(context)
        val selfDestructionTime = sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME)

        if(message.status != Constants.MessageStatus.SENDING.status){
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
                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time -> R.string.text_every_twenty_four_hours
                else -> -1
            }
            if (stringId != -1) {
                textViewCountDown?.visibility = View.VISIBLE
                textViewCountDown?.text = context.resources.getString(stringId)
            }
        }else{
            textViewCountDown?.visibility = View.INVISIBLE
            textViewCountDown?.text = ""
        }

    }

    private fun loadMediaPlayer(
        mediaPlayerManager: MediaPlayerManager,
        attachmentEntity: AttachmentEntity,
        item: MessageAttachmentRelation,
        clickListener: ConversationAdapter.ClickListener
    ) {
        Timber.d("loadMediaPlayer, current: ${mediaPlayerManager.getCurrentPosition()}, max: ${mediaPlayerManager.getMax()}, audioId: ${mediaPlayerManager.getMessageId()}")
        with(audioPlayer!!) {
            setMessageId(item.messageEntity.id)
            setMediaPlayerManager(mediaPlayerManager)
            setDuration(attachmentEntity.duration)

            if (item.messageEntity.isMine == Constants.IsMine.YES.value) {
                if (attachmentEntity.status == Constants.AttachmentStatus.SENT.status) {
                    isEncryptedFile(BuildConfig.ENCRYPT_API)
                    if (BuildConfig.ENCRYPT_API) {
                        setEncryptedFileName("${attachmentEntity.webId}.${attachmentEntity.extension}")
                    } else {
                        setAudioFileUri(
                            Utils.getFileUri(
                                context = context,
                                fileName = attachmentEntity.fileName,
                                subFolder = Constants.CacheDirectories.AUDIOS.folder
                            )
                        )
                    }
                } else {
                    isEncryptedFile(false)
                    setAudioFileUri(
                        Utils.getFileUri(
                            context = context,
                            fileName = attachmentEntity.fileName,
                            subFolder = Constants.CacheDirectories.AUDIOS.folder
                        )
                    )
                }
            } else {
                isEncryptedFile(BuildConfig.ENCRYPT_API)
                if (BuildConfig.ENCRYPT_API) {
                    setEncryptedFileName("${attachmentEntity.webId}.${attachmentEntity.extension}")
                } else {
                    setAudioFileUri(
                        Utils.getFileUri(
                            context = context,
                            fileName = attachmentEntity.fileName,
                            subFolder = Constants.CacheDirectories.AUDIOS.folder
                        )
                    )
                }
            }
            setListener(object : AudioPlayerCustomView.Listener {
                override fun onErrorPlayingAudio() {
                    clickListener.errorPlayingAudio()
                }

                override fun onPause(messageId: Int, webId: String) {
                    clickListener.sendMessageRead(messageId, webId, false, adapterPosition)
                }

                override fun onComplete(messageId: Int) {
                    clickListener.sendMessageRead(messageId, "", true, adapterPosition)
                }
            })
        }
    }

    private fun imageButtonStateClickListener(
        attachmentEntity: AttachmentEntity,
        clickListener: ConversationAdapter.ClickListener,
        item: MessageAttachmentRelation
    ) {
        when (attachmentEntity.status) {
            Constants.AttachmentStatus.SENDING.status -> {
                attachmentEntity.status = Constants.AttachmentStatus.UPLOAD_CANCEL.status
                clickListener.updateAttachmentState(attachmentEntity)
                val message = item.messageEntity
                message.status = Constants.MessageStatus.ERROR.status
                clickListener.updateMessageState(message)
                progressBar?.setProgress(0.0f)
                progressBar?.visibility = View.INVISIBLE
                progressBarIndeterminate?.isVisible = false
                imageButtonState?.setImageResource(R.drawable.ic_file_upload_black)
                imageButtonState?.visibility = View.VISIBLE

                val intent = Intent(context, UploadService::class.java).apply {
                    action = UploadService.ACTION_CANCEL_UPLOAD
                }

                context.startService(intent)
            }
            Constants.AttachmentStatus.UPLOAD_CANCEL.status,
            Constants.AttachmentStatus.ERROR.status -> {
                attachmentEntity.status = Constants.AttachmentStatus.SENDING.status
                clickListener.uploadAttachment(attachmentEntity, item.messageEntity)
            }
            Constants.AttachmentStatus.DOWNLOADING.status -> {
                try {
                    Timber.d("Cancel job: $downloadJob")
                    if (this.downloadJob?.isActive == true) {
                        this.downloadJob?.cancel()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                progressBar?.setProgress(0.0f)
                progressBar?.visibility = View.GONE
                imageButtonState?.setImageResource(R.drawable.ic_file_download_black)
                attachmentEntity.status = Constants.AttachmentStatus.DOWNLOAD_CANCEL.status
                clickListener.updateAttachmentState(attachmentEntity)
            }
            Constants.AttachmentStatus.DOWNLOAD_ERROR.status,
            Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
            Constants.AttachmentStatus.ERROR.status -> {
                progressBarIndeterminate?.visibility = View.VISIBLE
                imageButtonState?.visibility = View.INVISIBLE
                clickListener.downloadAttachment(item, adapterPosition)
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status,
            Constants.AttachmentStatus.SENT.status -> {
                clickListener.onPreviewClick(item)
            }
        }
    }
}