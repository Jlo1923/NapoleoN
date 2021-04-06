package com.naposystems.napoleonchat.ui.custom.audioPlayer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomViewAudioPlayerBinding
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AudioPlayerCustomView constructor(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet), IContractAudioPlayer, MediaPlayerManager.Listener {

    private var mTintColor: Int = 0
    private var mIsEncryptedFile: Boolean = false
    private var mAudioFileUri: Uri? = null
    private var mEncryptedFileName: String = ""
    private var mMessageId: Int = -1
    private var mEnable : Boolean = true
    private var mSeekbarProgressBackgroundTint: Int = 0
    private var mSeekbarProgressTint: Int = 0
    private var mSeekbarThumbTint: Int = 0

    private var binding: CustomViewAudioPlayerBinding

    private var mediaPlayerManager: MediaPlayerManager? = null
    private var mListener: Listener? = null

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    interface Listener {
        fun onErrorPlayingAudio()
        fun onPause(messageId : Int, webId: String)
        fun onComplete(messageId : Int)
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

                binding.textViewDuration.text = "00:00"
                binding.seekbar.progress = 0
                binding.seekbar.max = 100

                setTintColor()

            } finally {
                recycle()
            }
        }

        subscribeToRXEvents()
    }

    private fun subscribeToRXEvents() {
        val disposableEnableButtonPlayAudio = RxBus.listen(RxEvent.EnableButtonPlayAudio::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                binding.imageButtonPlay.isEnabled = event.state
                mEnable = event.state
            }
        disposable.add(disposableEnableButtonPlayAudio)
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
            if (mEnable) {
                mediaPlayerManager?.apply {
                    setMessageId(mMessageId)
                    setImageButtonPlay(binding.imageButtonPlay)
                    setSeekbar(binding.seekbar)
                    setImageButtonSpeed(binding.imageButtonSpeed)
                    setTextViewDuration(binding.textViewDuration)

                    if (mIsEncryptedFile) {
                        isEncryptedFile(BuildConfig.ENCRYPT_API)
                        setAudioFileName(mEncryptedFileName)
                    } else {
                        isEncryptedFile(false)
                        setAudioUri(mAudioFileUri)
                    }

                    playAudio()
                }
            }
        }

        binding.imageButtonRewind.setOnClickListener {
            mediaPlayerManager?.rewindMilliseconds(mMessageId, TimeUnit.SECONDS.toMillis(5))
        }

        binding.imageButtonForward.setOnClickListener {
            mediaPlayerManager?.forwardMilliseconds(mMessageId, TimeUnit.SECONDS.toMillis(5))
        }

        binding.imageButtonSpeed.setOnClickListener {
            mediaPlayerManager?.changeSpeed(mMessageId)
        }
    }

    //region Implementation IContractAudioPlayer

    override fun enablePlayButton(isEnable: Boolean) {
        binding.imageButtonPlay.isEnabled = isEnable
    }

    override fun playAudio() {
        Timber.d("Conver playAudio: $mMessageId")
        binding.imageButtonPlay.performClick()
    }

    override fun isEncryptedFile(isEncryptedFile: Boolean) {
        this.mIsEncryptedFile = isEncryptedFile
        this.mediaPlayerManager?.isEncryptedFile(isEncryptedFile)
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

    override fun setMessageId(id: Int) {
        this.mMessageId = id
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun setDuration(duration: Long) {
        Timber.d("Conver setDuration: $duration, mAudioId: $mMessageId, mediaPlayerAudioId: ${mediaPlayerManager?.getMessageId()}")
        binding.seekbar.max = 100
        mediaPlayerManager?.let {
            binding.seekbar.tag = mMessageId
            if (mMessageId != -1 && it.getCurrentPosition() != 0 && duration > 0 && mMessageId == mediaPlayerManager?.getMessageId()) {
                val progress = ((it.getCurrentPosition() * 100) / duration).toInt()
                binding.textViewDuration.text = Utils.getDuration(
                    (duration - ((duration * progress) / 100)),
                    showHours = false
                )
                binding.seekbar.progress = progress

                if (mediaPlayerManager?.isPlaying() == true) {
                    it.setSeekbar(binding.seekbar)
                    it.setTextViewDuration(binding.textViewDuration)
                    it.setImageButtonPlay(binding.imageButtonPlay)
                    it.setStateImageButtonSpeed(binding.imageButtonSpeed, mMessageId)
                    binding.imageButtonPlay.setImageDrawable(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_pause_circle, context.theme
                        )
                    )
                    Timber.d("*TestAudio: Play")
                } else {
                    it.setStateImageButtonSpeed(binding.imageButtonSpeed, mMessageId)
                    binding.imageButtonPlay.setImageDrawable(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_play_circle, context.theme
                        )
                    )
                    Timber.d("*TestAudio: Pause")
                }
//                mediaPlayerManager?.setDuration(duration)
                /*if (mediaPlayerManager?.isPlaying() == true && !binding.imageButtonPlay.hasBeenInitialized) {
                    binding.imageButtonPlay.playAnimation()
                }*/
            } else {
                binding.textViewDuration.text = Utils.getDuration(duration, false)
                binding.textViewDuration.visibility =
                if (duration == 0L) View.GONE else View.VISIBLE
                binding.seekbar.progress = 0
                it.refreshSeekbarProgress()
                binding.imageButtonSpeed.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
                binding.imageButtonPlay.setImageDrawable(
                    context.resources.getDrawable(
                        R.drawable.ic_baseline_play_circle, context.theme
                    )
                )
//                Timber.d("*TestAudio: Pause")
            }
        } ?: run {
            binding.textViewDuration.text = Utils.getDuration(duration, false)
            binding.textViewDuration.visibility = if (duration == 0L) View.GONE else View.VISIBLE
        }
    }

    //endregion

    //region Implementation MediaPlayerManager.Listener
    override fun onErrorPlayingAudio() {
        this@AudioPlayerCustomView.mListener?.onErrorPlayingAudio()
    }

    override fun onPauseAudio(messageId: Int, webId: String) {
        this.mListener?.onPause(messageId, webId)
    }

    override fun onCompleteAudio(messageId: Int) {
        this.mListener?.onComplete(messageId)
    }
    //endregion
}