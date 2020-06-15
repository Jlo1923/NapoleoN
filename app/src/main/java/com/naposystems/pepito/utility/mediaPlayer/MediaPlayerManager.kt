package com.naposystems.pepito.utility.mediaPlayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.custom.animatedTwoVectorView.AnimatedTwoVectorView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaPlayerManager(private val context: Context) :
    SensorEventListener, IContractMediaPlayer {

    companion object {
        private const val NORMAL_SPEED = 1.0f
        private const val TWO_X_SPEED = 1.5f
    }

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
    }

    private var mStartAudioTime: Long = 0L
    private var mIsEncryptedFile: Boolean = false
    private var currentAudioId: Int = 0
    private var currentAudioUri: Uri? = null
    private var currentAudioFileName: String? = null
    private var mSpeed: Float = 1.0f
    private var mImageButtonPlay: AnimatedTwoVectorView? = null
    private var mImageButtonSpeed: ImageButton? = null
    private var mPreviousAudioId: Int? = null
    private var mPreviousUri: Uri? = null
    private var mPreviousFileName: String? = null
    private var mSeekBar: AppCompatSeekBar? = null
    private var mTextViewDuration: TextView? = null
    private var mListener: Listener? = null
    private var tempFile: File? = null
    private val mHandler: Handler by lazy {
        Handler()
    }
    private lateinit var mRunnable: Runnable

    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            MediaPlayerManager::class.simpleName
        )
    }
    private val mSensorManager: SensorManager by lazy {
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    }
    private val mProximitySensor: Sensor by lazy {
        mSensorManager.getDefaultSensor(
            Sensor.TYPE_PROXIMITY
        )
    }
    private val mAudioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    interface Listener {
        fun onErrorPlayingAudio()
        fun onPauseAudio()
        fun onCompleteAudio()
    }

    private fun setSeekbarProgress() {
        mSeekBar?.progress = mediaPlayer.currentPosition
    }

    private fun deleteTempFile() {
        if (mIsEncryptedFile && tempFile?.exists() == true) {
            tempFile?.delete()
        }
    }

    private fun stop() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun registerProximityListener() {
        mSensorManager.registerListener(
            this,
            mProximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    //region Implementation SensorEventListener
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        Timber.d("onSensorChanged")
        if (event.sensor.type != Sensor.TYPE_PROXIMITY) return

        mediaPlayer.let { mediaPlayer ->
            val streamType: Int =
                if (event.values[0] < 5f && event.values[0] != mProximitySensor.maximumRange) {
                    AudioManager.STREAM_VOICE_CALL
                } else {
                    AudioManager.STREAM_MUSIC
                }

            if (streamType == AudioManager.STREAM_VOICE_CALL && !mAudioManager.isWiredHeadsetOn
            ) {
                val position = mediaPlayer.currentPosition.toDouble()
                val duration = mediaPlayer.duration.toDouble()
                val progress = position / duration
                wakeLock.acquire()
                try {
                    mImageButtonSpeed?.setImageResource(R.drawable.ic_1x_speed_black)
                    mSpeed = NORMAL_SPEED
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    playAudio(progress = progress, isEarpiece = true)
                } catch (e: IOException) {
                    Timber.e(e)
                }
            } else if (streamType == AudioManager.STREAM_MUSIC && System.currentTimeMillis() - mStartAudioTime > 500) {
                unregisterProximityListener()
                if (wakeLock.isHeld) {
                    wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                }
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    mImageButtonPlay?.reverseAnimation()
                    mListener?.onPauseAudio()
                }
            }
        }
    }
    //endregion

    //region Implementation
    override fun setAudioId(audioId: Int) {
        this.currentAudioId = audioId
    }

    override fun setAudioUri(uri: Uri?) {
        this.currentAudioUri = uri
    }

    override fun setAudioFileName(fileName: String) {
        this.currentAudioFileName = fileName
    }

    override fun playAudio(progress: Double, isEarpiece: Boolean) {
        try {
            mStartAudioTime = System.currentTimeMillis()

            mediaPlayer.apply {

                mSensorManager.registerListener(
                    this@MediaPlayerManager,
                    mProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(if (isEarpiece) AudioAttributes.CONTENT_TYPE_SPEECH else AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(if (isEarpiece) AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (mPreviousAudioId != currentAudioId || progress > 0) {

                    mediaPlayer.stop()
                    mediaPlayer.reset()

                    mPreviousAudioId = currentAudioId

                    if (mIsEncryptedFile) {
                        tempFile = FileManager.createTempFileFromEncryptedFile(
                            context,
                            Constants.AttachmentType.AUDIO.type,
                            currentAudioFileName!!,
                            "mp3"
                        )

                        setDataSource(tempFile?.absolutePath)
                    } else {
                        val stream =
                            context.contentResolver.openFileDescriptor(currentAudioUri!!, "r")
                        setDataSource(stream!!.fileDescriptor)
                    }

                    prepare()
                }

                mediaPlayer.setOnPreparedListener {

                    mSeekBar?.max = duration

                    if (progress > 0) {
                        mediaPlayer.seekTo((it.duration * progress).toInt())
                    }
                }

                mediaPlayer.setOnCompletionListener {
                    deleteTempFile()
                    resetMediaPlayer()
                    mListener?.onCompleteAudio()
                    unregisterProximityListener()
                }

                mRunnable = Runnable {
                    setSeekbarProgress()

                    mHandler.postDelayed(
                        mRunnable,
                        50
                    )
                }

                if (isPlaying) {
                    pause()
                    mImageButtonPlay?.reverseAnimation()
                    mListener?.onPauseAudio()
                } else {
                    mAudioManager.isSpeakerphoneOn = !isEarpiece
                    start()

                    mHandler.postDelayed(mRunnable, 0)
                    mImageButtonPlay?.playAnimation()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            mListener?.onErrorPlayingAudio()
        }
    }

    override fun unregisterProximityListener() {
        if (wakeLock.isHeld) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
        }
        mSensorManager.unregisterListener(this, mProximitySensor)
    }

    override fun pauseAudio() {
        if (mediaPlayer.isPlaying) {
            mImageButtonPlay?.reverseAnimation()
        }
        mediaPlayer.pause()
    }

    override fun isEncryptedFile(isEncryptedFile: Boolean) {
        this.mIsEncryptedFile = isEncryptedFile
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun setImageButtonPlay(imageButtonPlay: AnimatedTwoVectorView) {
        if (this.mImageButtonPlay != imageButtonPlay) {
            this.mImageButtonPlay?.reverseAnimation()
        }
        this.mImageButtonPlay = imageButtonPlay
    }

    override fun setImageButtonSpeed(imageButtonSpeed: ImageButton) {
        if (this.mImageButtonSpeed != imageButtonSpeed) {
            this.mImageButtonSpeed?.setImageResource(R.drawable.ic_1x_speed_black)
            mSpeed = NORMAL_SPEED
        }
        this.mImageButtonSpeed = imageButtonSpeed
    }

    override fun setSeekbar(seekBar: AppCompatSeekBar) {
        if (this.mSeekBar != null && this.mSeekBar != seekBar) {
            this.mSeekBar?.progress = 0
        }
        this.mSeekBar = seekBar

        this.mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
                try {
                    mTextViewDuration?.text = Utils.getDuration(
                        (mediaPlayer.duration - progress).toLong(),
                        showHours = false
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Intentionally empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Intentionally empty
            }
        })
    }

    override fun setTextViewDuration(textView: TextView) {
        if (this.mTextViewDuration != textView) {
            this.mTextViewDuration?.text = ""
        }
        this.mTextViewDuration = textView
    }

    override fun rewindMilliseconds(audioId: Int, millis: Long) {
        if (mSeekBar != null && audioId == mPreviousAudioId) {
            val minusValue = mediaPlayer.currentPosition - millis

            if (mediaPlayer.currentPosition >= millis) {
                mediaPlayer.seekTo(minusValue.toInt())
            } else {
                mediaPlayer.seekTo(0)
            }
        }
    }

    override fun changeSpeed(audioId: Int) {
        if (mPreviousAudioId == audioId) {
            mSpeed = if (mSpeed == NORMAL_SPEED) {
                mImageButtonSpeed?.setImageResource(R.drawable.ic_2x_speed_black)
                TWO_X_SPEED
            } else {
                mImageButtonSpeed?.setImageResource(R.drawable.ic_1x_speed_black)
                NORMAL_SPEED
            }
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(mSpeed)
        }
    }

    override fun forwardMilliseconds(audioId: Int, millis: Long) {
        if (mSeekBar != null && mPreviousAudioId == audioId) {
            val minorValue = mediaPlayer.duration - (millis + TimeUnit.SECONDS.toMillis(1))

            if (mSeekBar!!.progress <= minorValue) {
                mediaPlayer.seekTo(mediaPlayer.currentPosition + 5000)
            }
        }
    }

    override fun resetMediaPlayer() {
        deleteTempFile()

        if (::mRunnable.isInitialized) {
            mHandler.removeCallbacks(mRunnable)
        }

        mImageButtonSpeed?.setImageResource(R.drawable.ic_1x_speed_black)
        mImageButtonPlay?.reverseAnimation()
        mImageButtonPlay = null
        mImageButtonSpeed = null
        mPreviousAudioId = null
        mSeekBar?.progress = 0
        mSeekBar = null
        mTextViewDuration?.text = ""
        mTextViewDuration = null
        mSpeed = NORMAL_SPEED

        mediaPlayer.pause()
        mediaPlayer.reset()
        mSensorManager.unregisterListener(this, mProximitySensor)
        if (wakeLock.isHeld) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
        }
    }
    //endregion
}