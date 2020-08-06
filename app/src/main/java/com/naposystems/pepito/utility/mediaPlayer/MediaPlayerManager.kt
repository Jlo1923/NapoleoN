package com.naposystems.pepito.utility.mediaPlayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.custom.animatedTwoVectorView.AnimatedTwoVectorView
import com.naposystems.pepito.utility.BluetoothStateManager
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.audioManagerCompat.AudioManagerCompat
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object MediaPlayerManager :
    SensorEventListener, IContractMediaPlayer, BluetoothStateManager.BluetoothStateListener {

    private const val NORMAL_SPEED = 1.0f
    private const val TWO_X_SPEED = 1.5f

    private val loadControl: LoadControl = DefaultLoadControl.Builder().setBufferDurationsMs(
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE
    ).createDefaultLoadControl()

    private var mediaPlayer: SimpleExoPlayer? = null
    private lateinit var context: Context

    private var mStartAudioTime: Long = 0L
    private var mIsEncryptedFile: Boolean = false
    private var mIsBluetoothConnected: Boolean = false
    private var mIsInitialized: Boolean = false
    private var currentAudioId: String = ""
    private var currentAudioUri: Uri? = null
    private var currentAudioFileName: String? = null
    private var mSpeed: Float = 1.0f
    private var mImageButtonPlay: ImageView? = null
    private var mImageButtonSpeed: ImageButton? = null
    private var mPreviousAudioId: String? = null
    private var mPreviousUri: Uri? = null
    private var mPreviousFileName: String? = null
    private var mSeekBar: AppCompatSeekBar? = null
    private var mTextViewDuration: TextView? = null
    private var mListener: Listener? = null
    private var tempFile: File? = null
    private var mDuration: Long = 0L

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
    private val mAudioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var bluetoothStateManager: BluetoothStateManager? = null

    private val audioManagerCompat by lazy {
        AudioManagerCompat.create(context)
    }

    private val soundMediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
    }

    private var isProximitySensorActive: Boolean = false

    interface Listener {
        fun onErrorPlayingAudio()
        fun onPauseAudio(messageWebId: String)
        fun onCompleteAudio(messageWebId: String)
    }

    private fun setSeekbarProgress() {
        mediaPlayer?.let {
            if (it.duration > 0) {
                val progress = ((it.currentPosition * 100) / it.duration)

                Timber.d("Conver setSeekbarProgress: $progress, position: ${it.currentPosition}, duration: ${it.duration}, seekbar: ${mSeekBar == null}")

                mSeekBar?.progress = progress.toInt()
            }
        }
    }

    private fun deleteTempFile() {
        if (mIsEncryptedFile && tempFile?.exists() == true) {
            tempFile?.delete()
        }
    }

    private fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    private fun registerProximityListener() {
        mSensorManager.registerListener(
            this,
            mProximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun enableSpeedControl(isEnabled: Boolean) {
        mImageButtonSpeed?.isEnabled = isEnabled
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        Timber.d("Conver buildMediaSource:$uri")
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun setupVoiceNoteSound(sound: Int) {
        try {
            soundMediaPlayer.apply {
                reset()
                setDataSource(
                    context,
                    Uri.parse("android.resource://" + context.packageName + "/" + sound)
                )
                if (isPlaying) {
                    stop()
                    reset()
                    release()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    //region Implementation SensorEventListener
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_PROXIMITY) return
        if (mediaPlayer == null || mediaPlayer?.playbackState != Player.STATE_READY) return

        Timber.d("Conver onSensorChanged: ${event.sensor.type}")

        mediaPlayer?.let { mediaPlayer ->
            val streamType: Int =
                if (event.values[0] < 5f && event.values[0] != mProximitySensor.maximumRange) {
                    AudioManager.STREAM_VOICE_CALL
                } else {
                    AudioManager.STREAM_MUSIC
                }

            if (streamType == AudioManager.STREAM_VOICE_CALL && !mAudioManager.isWiredHeadsetOn) {
                val progress =
                    ((mediaPlayer.currentPosition * 100) / mediaPlayer.duration)
                Timber.d("Conver progress: $progress")
                wakeLock.acquire()
                try {
                    isProximitySensorActive = true
                    mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
                    mSpeed = NORMAL_SPEED
                    mHandler.removeCallbacks(mRunnable)
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    this.mediaPlayer = null
                    playAudio(progress = progress.toInt(), isEarpiece = true)
                } catch (e: IOException) {
                    Timber.e(e)
                }
            } else if (streamType == AudioManager.STREAM_MUSIC && System.currentTimeMillis() - mStartAudioTime > 500 && !mAudioManager.isWiredHeadsetOn) {
//                unregisterProximityListener()
                isProximitySensorActive = false
                if (wakeLock.isHeld) {
                    wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                }
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.playWhenReady = false
                    changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                    mListener?.onPauseAudio(currentAudioId)
                    mHandler.removeCallbacks(mRunnable)
                }
            }
        }
    }
    //endregion

    //region Implementation

    override fun setContext(context: Context) {
        this.context = context
    }

    override fun initializeBluetoothManager() {
        bluetoothStateManager = BluetoothStateManager(context, this)
    }

    override fun setAudioId(audioId: String) {
        if (mPreviousAudioId != audioId) {
            mSeekBar?.progress = 0
            if (mediaPlayer?.isPlaying == true) {
                changeIconPlayPause(R.drawable.ic_baseline_play_circle)
            }
        }
        this.currentAudioId = audioId
    }

    override fun setAudioUri(uri: Uri?) {
        this.currentAudioUri = uri
    }

    override fun setAudioFileName(fileName: String) {
        this.currentAudioFileName = fileName
    }

    override fun playAudio(progress: Int, isEarpiece: Boolean) {

        try {

            mAudioManager.isSpeakerphoneOn = !isProximitySensorActive

            if (mPreviousAudioId == currentAudioId && progress == 0) {
                if (mediaPlayer != null) {
                    if (mediaPlayer?.isPlaying == true) {
                        changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                        mHandler.removeCallbacks(mRunnable)
                        mListener?.onPauseAudio(currentAudioId)
                    } else {
                        setupVoiceNoteSound(R.raw.tone_audio_message_start)
                        changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                        mRunnable = Runnable {
                            setSeekbarProgress()

                            mHandler.postDelayed(
                                mRunnable,
                                50
                            )
                        }
                        mHandler.postDelayed(mRunnable, 0)
                    }
                    mediaPlayer?.playWhenReady = !mediaPlayer!!.isPlaying
                }
            } else {

                mPreviousAudioId = currentAudioId

                mStartAudioTime = System.currentTimeMillis()

                mediaPlayer?.stop()
                mediaPlayer?.release()

                Timber.d("Conver currentPosition: ${mediaPlayer?.currentPosition}, seekbar.max: ${mSeekBar?.max}, isProximitySensorActive: $isProximitySensorActive")

                mediaPlayer = ExoPlayerFactory.newSimpleInstance(
                    context,
                    DefaultRenderersFactory(context),
                    DefaultTrackSelector(),
                    loadControl
                )

                with(mediaPlayer!!) {

                    if (!mIsBluetoothConnected)
                        registerProximityListener()

                    /*if (mPreviousAudioId != currentAudioId || progress > 0) {

                        *//*stop()
                    mediaPlayer.release()*//*

                        mPreviousAudioId = currentAudioId

                        Timber.d("Conver mIsEncryptedFile: $mIsEncryptedFile")

                    }*/

                    prepare(
                        buildMediaSource(
                            if (mIsEncryptedFile) {
                                tempFile = FileManager.createTempFileFromEncryptedFile(
                                    context,
                                    Constants.AttachmentType.AUDIO.type,
                                    currentAudioFileName!!,
                                    "mp3"
                                )

                                tempFile?.toUri()!!
                            } else {
                                currentAudioUri!!
                            }
                        )
                    )

                    audioAttributes = AudioAttributes.Builder()
                        .setContentType(if (isProximitySensorActive) C.CONTENT_TYPE_SPEECH else C.CONTENT_TYPE_MUSIC)
                        .setUsage(if (isProximitySensorActive) C.USAGE_VOICE_COMMUNICATION else C.USAGE_MEDIA)
                        .build()

                    audioManagerCompat.requestCallAudioFocus()
                    mAudioManager.isSpeakerphoneOn = !isProximitySensorActive

                    playWhenReady = true

                    addListener(object : Player.EventListener {

                        var started = false

                        override fun onPlayerStateChanged(
                            playWhenReady: Boolean,
                            playbackState: Int
                        ) {
                            Timber.d("Conver onPlayerStateChanged: $playWhenReady, $playbackState")

                            when (playbackState) {
                                Player.STATE_READY -> {
                                    Timber.d("Conver onPrepared ${mediaPlayer?.bufferedPercentage} buffered")
                                    if (mediaPlayer == null) return

                                    if (started) {
                                        Timber.d("Conver Already started. Ignoring.")
                                        return
                                    }
                                    started = true

                                    mediaPlayer?.let {
                                        if (progress > 0) {
                                            Timber.d("Conver seekto: ${(it.duration * progress)}")
                                            it.seekTo((it.duration * progress) / 100)
                                        }

                                        /*if (mDuration > 0 && it.duration > 0) {
                                            val currentProgress =
                                                ((it.currentPosition * 100) / it.duration)
                                            mSeekBar?.max = 100
                                            mSeekBar?.progress = currentProgress.toInt()
                                            it.seekTo((duration * currentProgress) / 100)
                                        }*/

                                    }

                                    mSensorManager.registerListener(
                                        this@MediaPlayerManager,
                                        mProximitySensor,
                                        SensorManager.SENSOR_DELAY_NORMAL
                                    )

                                    Timber.d("Conver start audio")
                                    enableSpeedControl(true)

                                    mRunnable = Runnable {
                                        setSeekbarProgress()

                                        mHandler.postDelayed(
                                            mRunnable,
                                            50
                                        )
                                    }
                                    mHandler.postDelayed(mRunnable, 0)
                                    changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                                    setupVoiceNoteSound(R.raw.tone_audio_message_start)
                                }

                                Player.STATE_ENDED -> {
                                    Timber.i("Conver onComplete")
                                    mSeekBar?.progress = 0
                                    mSensorManager.unregisterListener(this@MediaPlayerManager)
                                    if (wakeLock.isHeld) {
                                        wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                                    }
                                    changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                                    mHandler.removeCallbacks(mRunnable)
                                    mListener?.onCompleteAudio(currentAudioId)
                                    setupVoiceNoteSound(R.raw.tone_audio_message_end)
                                }
                            }
                        }

                        override fun onPlayerError(error: ExoPlaybackException?) {
                            Timber.w("Conver MediaPlayer Error: $error")

                            mSensorManager.unregisterListener(this@MediaPlayerManager)
                            if (wakeLock.isHeld) {
                                wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                            }

                            if (::mRunnable.isInitialized) {
                                mHandler.removeCallbacks(mRunnable)
                            }

                            mListener?.onErrorPlayingAudio()
                        }
                    })

                    /*
                if (mPreviousAudioId != currentAudioId || progress > 0) {

                    mediaPlayer.stop()
                    mediaPlayer.release()

                    mPreviousAudioId = currentAudioId

                    Timber.d("Conver mIsEncryptedFile: $mIsEncryptedFile")

                }

                mediaPlayer.setOnPreparedListener {

                    Timber.d("Conver setOnPreparedListener: ${mSeekBar?.max}, duration: $duration, progress: $progress")
                    mSeekBar?.max = 100

                    if (progress > 0) {
                        mediaPlayer.seekTo((it.duration * progress))
                    }
                }

                *//*mediaPlayer.setOnCompletionListener {
                    Timber.d("Conver setOnCompletionListener")
                    deleteTempFile()
//                    resetMediaPlayer()
                    mListener?.onCompleteAudio(currentAudioId)
                    //unregisterProximityListener()
                }*//*

                if (isPlaying) {
                    playWhenReady = false
                    mImageButtonPlay?.reverseAnimation()
                    Timber.d("Conver mediaplayer pause: $currentAudioId")
                    mListener?.onPauseAudio(currentAudioId)
                    enableSpeedControl(false)
                    mHandler.removeCallbacks(mRunnable)
                    setSeekbarProgress()

                } else {
                    Timber.d("Conver start audio")
                    enableSpeedControl(true)
                    audioManagerCompat.requestCallAudioFocus()
                    mAudioManager.isSpeakerphoneOn = !(isProximitySensorActive || isEarpiece)
                    playWhenReady = true

                    mRunnable = Runnable {
                        setSeekbarProgress()

                        mHandler.postDelayed(
                            mRunnable,
                            50
                        )
                    }
                    mHandler.postDelayed(mRunnable, 0)
                    mImageButtonPlay?.playAnimation()
                }*/
                }
            }
        } catch (e: Exception) {
            Timber.e("Conver error: ${e.message}")
            mListener?.onErrorPlayingAudio()
        }
    }

    override fun unregisterProximityListener() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        mSensorManager.unregisterListener(this, mProximitySensor)
    }

    override fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            changeIconPlayPause(R.drawable.ic_baseline_play_circle)
        }
        mediaPlayer?.playWhenReady = false
    }

    override fun isEncryptedFile(isEncryptedFile: Boolean) {
        this.mIsEncryptedFile = isEncryptedFile
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun setImageButtonPlay(imageButtonPlay: ImageView) {
        /*if (this.mImageButtonPlay != imageButtonPlay) {
            this.mImageButtonPlay?.reverseAnimation()
        }*/
        this.mImageButtonPlay = imageButtonPlay
    }

    override fun setImageButtonSpeed(imageButtonSpeed: ImageButton) {
        if (this.mImageButtonSpeed != imageButtonSpeed) {
            this.mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
            mSpeed = NORMAL_SPEED
        }
        this.mImageButtonSpeed = imageButtonSpeed
    }

    override fun setSeekbar(seekBar: AppCompatSeekBar) {
        /*if (this.mSeekBar != null && this.mSeekBar != seekBar) {
            this.mSeekBar?.progress = 0
        }*/
        this.mSeekBar = seekBar

        this.mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mediaPlayer?.let {
                    if (fromUser) {
                        it.seekTo(progress.toLong())
                    }
                    try {
                        Timber.d("Conve onProgressChanged: $progress, duration: ${it.duration}")
                        mTextViewDuration?.text = Utils.getDuration(
                            (it.duration - ((it.duration * progress) / 100)),
                            showHours = false
                        )
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
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
        this.mTextViewDuration = textView
    }

    override fun rewindMilliseconds(audioId: String, millis: Long) {
        mediaPlayer?.let {
            if (mSeekBar != null && audioId == currentAudioId) {
                val minusValue = it.currentPosition - millis

                if (it.currentPosition >= millis) {
                    mSeekBar?.progress = ((minusValue * 100) / it.duration).toInt()
                    it.seekTo(minusValue)
                } else {
                    it.seekTo(0)
                }
            }
        }
    }

    override fun changeSpeed(audioId: String) {
        if (mPreviousAudioId == audioId) {
            mSpeed = if (mSpeed == NORMAL_SPEED) {
                mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_1x_circle_outline)
                TWO_X_SPEED
            } else {
                mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
                NORMAL_SPEED
            }
            val playbackParameters = PlaybackParameters(mSpeed)
            mediaPlayer?.playbackParameters = playbackParameters
        }
    }

    override fun forwardMilliseconds(audioId: String, millis: Long) {
        mediaPlayer?.let {
            if (mSeekBar != null && currentAudioId == audioId) {
                val minorValue = it.duration - (millis + TimeUnit.SECONDS.toMillis(1))

                if (mSeekBar!!.progress <= minorValue) {
                    mSeekBar?.progress = (((it.currentPosition + 5000) * 100) / it.duration).toInt()
                    it.seekTo(it.currentPosition + 5000)
                }
            }
        }
    }

    override fun setDuration(duration: Long) {
        mediaPlayer?.let {
            if (duration > 0 && it.duration > 0) {
                val progress = ((it.currentPosition * 100) / it.duration)
                Timber.d("Conver setDuration: $duration, current: ${getCurrentPosition()}, max: ${getMax()}, audioId: ${getAudioId()}, progress: $progress")
                mSeekBar?.max = 100
                mSeekBar?.progress = progress.toInt()
                this.mDuration = duration
            }
        }
    }

    override fun getCurrentPosition() = mediaPlayer?.currentPosition?.toInt() ?: 0

    override fun getMax() = mSeekBar?.max ?: -1

    override fun getAudioId() = this.currentAudioId

    override fun isPlaying() = mediaPlayer?.isPlaying ?: false

    override fun refreshSeekbarProgress() {
        setSeekbarProgress()
    }

    override fun resetMediaPlayer() {
        Timber.d("Conver resetMediaPlayer")
        deleteTempFile()

        if (::mRunnable.isInitialized) {
            mHandler.removeCallbacks(mRunnable)
        }

        mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
        changeIconPlayPause(R.drawable.ic_baseline_play_circle)
        mImageButtonPlay = null
        mImageButtonSpeed = null
        mPreviousAudioId = null
        mSeekBar?.progress = 0
        mSeekBar = null
        mTextViewDuration = null
        mSpeed = NORMAL_SPEED

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        unregisterProximityListener()

    }

    override fun resetMediaPlayer(messageWebId: String) {
        if (currentAudioId == messageWebId) {
            deleteTempFile()

            if (::mRunnable.isInitialized) {
                mHandler.removeCallbacks(mRunnable)
            }
            mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
            changeIconPlayPause(R.drawable.ic_baseline_play_circle)
            mImageButtonPlay = null
            mImageButtonSpeed = null
            mPreviousAudioId = null
            mSeekBar?.progress = 0
            mSeekBar = null
            mTextViewDuration?.text = ""
            mTextViewDuration = null
            mSpeed = NORMAL_SPEED

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            unregisterProximityListener()

        }
    }
    //endregion

    //region Implementation BluetoothStateManager.BluetoothStateListener
    override fun onBluetoothStateChanged(isAvailable: Boolean) {
        Timber.d("onBluetoothStateChanged: $isAvailable")
        mIsBluetoothConnected = isAvailable
        if (isAvailable) {
            unregisterProximityListener()
        } else {
            registerProximityListener()
        }
    }
    //endregion

    private fun changeIconPlayPause(drawable : Int) {
        mImageButtonPlay?.setImageDrawable(
            context.resources.getDrawable(
                drawable, context.theme
            )
        )
    }
}