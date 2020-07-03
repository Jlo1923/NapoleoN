package com.naposystems.pepito.ui.custom.audioPlayer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomViewAudioPlayerBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AudioPlayerCustomView constructor(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet), IContractAudioPlayer, MediaPlayerManager.Listener {

    private var messageAndAttachment: MessageAndAttachment? = null

    private var mTintColor: Int = 0
    private var mIsEncryptedFile: Boolean = false
    private var mAudioFileUri: Uri? = null
    private var mEncryptedFileName: String = ""
    private var mAudioId: String = ""
    private var mSeekbarProgressBackgroundTint: Int = 0
    private var mSeekbarProgressTint: Int = 0
    private var mSeekbarThumbTint: Int = 0

    private var binding: CustomViewAudioPlayerBinding

    private var mediaPlayerManager: MediaPlayerManager? = null
    private var mListener: Listener? = null

    interface Listener {
        fun onErrorPlayingAudio()
        fun onPause(audioId: String)
        fun onComplete(audioId: String)
    }

    init {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.AudioPlayerCustomView,
            0, 0
        ).apply {
            try {
                val infService = Context.LAYOUT_INFLATER_SERVICE
                val layoutInflater =
                    getContext().getSystemService(infService) as LayoutInflater

                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.custom_view_audio_player,
                    this@AudioPlayerCustomView,
                    true
                )

                mTintColor = getResourceId(
                    R.styleable.AudioPlayerCustomView_controlColors,
                    R.color.colorBackground
                )

                mIsEncryptedFile = getBoolean(
                    R.styleable.AudioPlayerCustomView_isEncryptedFile,
                    false
                )

                mAudioId = getString(R.styleable.AudioPlayerCustomView_audioId) ?: ""
                mSeekbarProgressBackgroundTint = getResourceId(
                    R.styleable.AudioPlayerCustomView_seekbarProgressBackgroundTint,
                    R.color.white
                )
                mSeekbarProgressTint = getResourceId(
                    R.styleable.AudioPlayerCustomView_seekbarProgressTint,
                    R.color.white
                )
                mSeekbarThumbTint = getResourceId(
                    R.styleable.AudioPlayerCustomView_seekbarThumbTint,
                    R.color.white
                )

                setTintColor()

            } finally {
                recycle()
            }
        }
    }

    private fun setTintColor() {
        with(ContextCompat.getColorStateList(context, mTintColor)) {
            binding.imageButtonPlay.imageTintList = this
            binding.imageButtonRewind.imageTintList = this
            binding.imageButtonSpeed.imageTintList = this
            binding.imageButtonForward.imageTintList = this
            binding.textViewDuration.setTextColor(this)
        }

        binding.seekbar.progressBackgroundTintList =
            ContextCompat.getColorStateList(context, mSeekbarProgressBackgroundTint)
        binding.seekbar.progressTintList =
            ContextCompat.getColorStateList(context, mSeekbarProgressTint)
        binding.seekbar.thumbTintList = ContextCompat.getColorStateList(context, mTintColor)
    }

    private fun setListeners() {

        mediaPlayerManager?.setListener(this)

        binding.imageButtonPlay.setOnClickListener {

            mediaPlayerManager?.apply {
                setAudioId(mAudioId)
                setImageButtonPlay(binding.imageButtonPlay)
                setSeekbar(binding.seekbar)
                setImageButtonSpeed(binding.imageButtonSpeed)
                setTextViewDuration(binding.textViewDuration)

                if (mIsEncryptedFile) {
                    setAudioFileName(mEncryptedFileName)
                } else {
                    setAudioUri(mAudioFileUri)
                }

                playAudio()
            }
        }

        binding.imageButtonRewind.setOnClickListener {
            mediaPlayerManager?.rewindMilliseconds(mAudioId, TimeUnit.SECONDS.toMillis(5))
        }

        binding.imageButtonForward.setOnClickListener {
            mediaPlayerManager?.forwardMilliseconds(mAudioId, TimeUnit.SECONDS.toMillis(5))
        }

        binding.imageButtonSpeed.setOnClickListener {
            mediaPlayerManager?.changeSpeed(mAudioId)
        }
    }

    //region Implementation IContractAudioPlayer

    override fun enablePlayButton(isEnable: Boolean) {
        Timber.d("Conver enablePlayButton: $isEnable, currentPosition: ${mediaPlayerManager?.getCurrentPosition()}, max: ${mediaPlayerManager?.getMax()}")
        binding.imageButtonPlay.visibility = if (isEnable) View.VISIBLE else View.INVISIBLE
        binding.imageButtonPlay.isEnabled = isEnable
    }

    override fun playAudio() {
        Timber.d("Conver playAudio: $mAudioId")
        binding.imageButtonPlay.performClick()
    }

    override fun isEncryptedFile(isEncryptedFile: Boolean) {
        this.mIsEncryptedFile = isEncryptedFile
        this.mediaPlayerManager?.isEncryptedFile(isEncryptedFile)
    }

    override fun setMessageAndAttachment(messageAndAttachment: MessageAndAttachment) {
        this.messageAndAttachment = messageAndAttachment
    }

    override fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager) {
        this.mediaPlayerManager = mediaPlayerManager
        setListeners()
    }

    override fun setAudioFileUri(uri: Uri) {
        this.mAudioFileUri = uri
    }

    override fun setEncryptedFileName(fileName: String) {
        this.mEncryptedFileName = fileName
    }

    override fun setAudioId(id: String) {
        this.mAudioId = id
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun setDuration(duration: Long) {
        Timber.d("setDuration: $duration")
        binding.textViewDuration.text = Utils.getDuration(duration, false)
        binding.textViewDuration.visibility = if (duration == 0L) View.GONE else View.VISIBLE
        mediaPlayerManager?.setSeekbar(binding.seekbar)
        mediaPlayerManager?.setDuration(duration)
    }

    //endregion

    //region Implementation MediaPlayerManager.Listener
    override fun onErrorPlayingAudio() {
        this@AudioPlayerCustomView.mListener?.onErrorPlayingAudio()
    }

    override fun onPauseAudio(messageWebId: String) {
        Timber.d("Conver onPauseAudio: $messageWebId")
        /*if (messageAndAttachment?.message?.status == Constants.MessageStatus.UNREAD.status) {
            messageAndAttachment?.message?.status = Constants.MessageStatus.READED.status
            Timber.d("Conver audioPlayerCustom pause")
            this.mListener?.onPause(audioId)
        }*/
        this.mListener?.onPause(messageWebId)
    }

    override fun onCompleteAudio(messageWebId: String) {
        Timber.d("Conver onCompleteAudio: $messageWebId")
        /*if (messageAndAttachment?.message?.status == Constants.MessageStatus.UNREAD.status) {
            messageAndAttachment?.message?.status = Constants.MessageStatus.READED.status
            this.mListener?.onComplete(audioId)
        }*/
        this.mListener?.onComplete(messageWebId)
    }
    //endregion
}