package com.naposystems.napoleonchat.utility.mediaPlayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.BluetoothStateManager
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.audioManagerCompat.AudioManagerCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MediaPlayerManager @Inject constructor(private val context: Context) :
    SensorEventListener, IContractMediaPlayer, BluetoothStateManager.BluetoothStateListener {

    companion object {
        const val NORMAL_SPEED = 1.0f
        const val TWO_X_SPEED = 1.5f
    }

    private val loadControl: LoadControl = DefaultLoadControl.Builder().setBufferDurationsMs(
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE,
        Int.MAX_VALUE
    ).createDefaultLoadControl()

    private var mediaPlayer: SimpleExoPlayer? = null
    private var mStartAudioTime: Long = 0L
    private var mIsEncryptedFile: Boolean = false
    private var mIsBluetoothConnected: Boolean = false
    private var currentMessageId: Int = -1
    private var currentAudioUri: Uri? = null
    private var currentAudioFileName: String? = null
    private var mSpeed: Float = 1.0f
    private var mImageButtonPlay: ImageView? = null
    private var mImageButtonSpeed: ImageButton? = null
    private var mPreviousMessageId: Int = -1
    private var mSeekBar: AppCompatSeekBar? = null
    private var mTextViewDuration: TextView? = null
    private var mListener: Listener? = null
    private var tempFile: File? = null
    private var mStatusPlayWithSensor : Boolean = false
    private var sensorStatusLast: Int = 3 //Inactive

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

    private var bluetoothStateManager: BluetoothStateManager

    private val audioManagerCompat by lazy {
        AudioManagerCompat.create(context)
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
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
        fun onPauseAudio(messageId: Int, webId: String = "")
        fun onCompleteAudio(messageId: Int)
    }

    init {
        subscribeToRXEvents()
        bluetoothStateManager = BluetoothStateManager(context, this)
    }

    private fun subscribeToRXEvents() {
        val disposableMessagesToEliminate = RxBus.listen(RxEvent.MessagesToEliminate::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { params ->
                params?.id?.forEach { message ->
                    if(currentMessageId == message.messageEntity.id) {
                        mListener?.onPauseAudio(message.messageEntity.id, message.messageEntity.webId)
                    }
                } ?: kotlin.run {
                    mListener?.onPauseAudio(currentMessageId)
                }
                resetMediaPlayer()
            }

        disposable.add(disposableMessagesToEliminate)
    }

    private fun setSeekbarProgress() {
        mediaPlayer?.let {
            if (it.duration > 0 && currentMessageId == mSeekBar?.tag) {
                val progress = ((it.currentPosition.toFloat() * 100) / it.duration.toFloat())

                Timber.d("Conver setSeekbarProgress: $progress, position: ${it.currentPosition}, duration: ${it.duration}, seekbar: ${mSeekBar == null}")
                try {
                    updateDuration((it.duration.toFloat() - ((it.duration.toFloat() * progress) / 100)).toLong())
                } catch (e: Exception) {
                    Timber.e(e)
                }
                mSeekBar?.progress = progress.toInt()
            } else {
                mSeekBar?.progress = 0
            }
        }
    }

    private fun deleteTempFile() {
        if (mIsEncryptedFile && tempFile?.exists() == true) {
            tempFile?.delete()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        Timber.d("Conver buildMediaSource:$uri")
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    public fun setupVoiceNoteSound(sound: Int) {
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val streamType: Int =
            if (event.values[0] < 5f && event.values[0] != mProximitySensor.maximumRange) {
                AudioManager.STREAM_VOICE_CALL
            } else {
                AudioManager.STREAM_MUSIC
            }

        //Optimizaci??n para dispositivos de gama baja, causan multi instancia a el sensor
        if (sensorStatusLast == streamType) return
        else sensorStatusLast = streamType


        if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
            if (wakeLock.isHeld && !mStatusPlayWithSensor) wakeLock.release()
            Handler(Looper.myLooper()!!).postDelayed({
                if (mediaPlayer?.isPlaying == true){
                    playAudio()
                }
            },1000)

            Handler(Looper.myLooper()!!).postDelayed({
                if (sensorStatusLast != 0 && mAudioManager.mode == AudioManager.MODE_IN_COMMUNICATION){
                    if(mediaPlayer?.isPlaying == true){
                        playAudio(0,false)
                    }
                }
            },2700)
            if(sensorStatusLast == 0) wakeLock.acquire()
            return
        }

        if (mIsBluetoothConnected) return

        Timber.d("Conver onSensorChanged: ${event.sensor.type}")


        mediaPlayer?.let { mediaPlayer ->

            val progress = ((mediaPlayer.currentPosition * 100) / mediaPlayer.duration)

            if (streamType == AudioManager.STREAM_VOICE_CALL && !mAudioManager.isWiredHeadsetOn) {

                Timber.d("Conver progress: $progress")
                if (progress > 0) {
                    wakeLock.acquire()
                    try {
                        isProximitySensorActive = true
                        mSpeed = if (mSpeed == NORMAL_SPEED) {
                            mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
                            NORMAL_SPEED
                        } else {
                            mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_1x_circle_outline)
                            TWO_X_SPEED
                        }
                        mHandler.removeCallbacks(mRunnable)
                        mediaPlayer.stop()
                        mediaPlayer.release()
                        this.mediaPlayer = null
                        playAudio(progress = progress.toInt(), isEarpiece = true)
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
            } else if (streamType == AudioManager.STREAM_MUSIC && System.currentTimeMillis() - mStartAudioTime > 500 && !mAudioManager.isWiredHeadsetOn) {
//                unregisterProximityListener()
                isProximitySensorActive = false
                if (wakeLock.isHeld) {
                    wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                }
                if (mediaPlayer.isPlaying && currentMessageId != mPreviousMessageId) {

                    mediaPlayer.playWhenReady = false
                    changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                    mListener?.onPauseAudio(currentMessageId)
                    mHandler.removeCallbacks(mRunnable)
                    Timber.d("*TestProximity: unregisterProximityListener")
                    RxBus.publish(
                        RxEvent.StateFlag(Constants.StateFlag.OFF.state)
                    )
                }

                playAudio()

            }
        }
    }
    //endregion

    //region Implementation

    override fun setMessageId(messageId: Int) {
        if (currentMessageId != -1 && mPreviousMessageId != messageId) {
            mListener?.onPauseAudio(currentMessageId)
            mSeekBar?.progress = 0
            if (mediaPlayer?.isPlaying == true) {
                changeIconPlayPause(R.drawable.ic_baseline_play_circle)
            }
        }
        this.currentMessageId = messageId
    }

    override fun setAudioUri(uri: Uri?) {
        this.currentAudioUri = uri
    }

    override fun setAudioFileName(fileName: String) {
        this.currentAudioFileName = fileName
    }

    override fun playAudio(progress: Int, isEarpiece: Boolean) {
        try {

            //change to sensor
                Timber.d("elian play audio $mediaPlayer")

            if(sensorStatusLast == 0){ //active
                mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                mAudioManager.isSpeakerphoneOn = false
            }else{
                mAudioManager.mode = AudioManager.MODE_NORMAL
                mAudioManager.isSpeakerphoneOn = true
            }

            if (mPreviousMessageId == currentMessageId && progress == 0) {
                if (mediaPlayer != null) {
                    if (mediaPlayer?.isPlaying == true) {
                        changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                        isProximitySensorActive = false
                        mAudioManager.isSpeakerphoneOn = false
                        mHandler.removeCallbacks(mRunnable)
                        mListener?.onPauseAudio(currentMessageId)

                        Timber.e("*TestAudio: pause")
                        RxBus.publish(
                            RxEvent.StateFlag(Constants.StateFlag.OFF.state)
                        )
                        mediaPlayer?.playWhenReady = !mediaPlayer!!.isPlaying
                        unregisterProximityListener()

                    } else {
                        //setupVoiceNoteSound(R.raw.tone_audio_message_start)
                        changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                        Timber.d("*TestAudio : Play")

                        RxBus.publish(
                            RxEvent.StateFlag(Constants.StateFlag.ON.state)
                        )
                        mRunnable = Runnable {
                            setSeekbarProgress()

                            mHandler.postDelayed(
                                mRunnable,
                                20
                            )
                        }
                        mHandler.postDelayed(mRunnable, 0)

                        //reset media player for detect sensor
                        registerProximityListener()

                        if(sensorStatusLast == 0 || mediaPlayer == null){ //active
                            val progress = ((mediaPlayer!!.currentPosition * 100) / mediaPlayer!!.duration)

                            mediaPlayer!!.stop()
                            mediaPlayer!!.release()
                            this.mediaPlayer = null

                            Handler(Looper.myLooper()!!).postDelayed({
                                playAudio(progress = progress.toInt(), isEarpiece = true)
                            },1000)
                        }else{
                            Handler(Looper.myLooper()!!).postDelayed({
                                mediaPlayer!!.playWhenReady = true
                            },1000)

                        }
                    }


                }
            } else {


                mPreviousMessageId = currentMessageId

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

                    val playbackParameters = PlaybackParameters(mSpeed)
                    mediaPlayer?.playbackParameters = playbackParameters

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
                        .setContentType(C.CONTENT_TYPE_MUSIC)
//                        .setContentType(if (isProximitySensorActive) C.CONTENT_TYPE_SPEECH else C.CONTENT_TYPE_MUSIC)
//                        .setUsage(if (isProximitySensorActive) C.USAGE_VOICE_COMMUNICATION else C.USAGE_MEDIA)
                        .setUsage( C.USAGE_MEDIA)
                        .build()

                    audioManagerCompat.requestCallAudioFocus()


                    playWhenReady = true

                    addListener(object : Player.EventListener {

                        var started = false

                        override fun onPlayerStateChanged(
                            playWhenReady: Boolean,
                            playbackState: Int
                        ) {
                            Timber.e("Conver onPlayerStateChanged: $playWhenReady, $playbackState")

                            if(playWhenReady == false){
                                unregisterProximityListener()
                            }

                            when (playbackState) {
                                Player.STATE_READY -> {
                                    Timber.d("Conver onPrepared ${mediaPlayer?.bufferedPercentage} buffered")
                                    if (mediaPlayer == null) return

                                    if (started) {
                                        Timber.d("Conver Already started. Ignoring.")
                                        return
                                    }

                                    mStatusPlayWithSensor = false

                                    started = true

                                    mediaPlayer?.let {
                                        if (progress > 0) {
                                            Timber.d("Conver seekto: ${(it.duration * progress)}")
                                            it.seekTo((it.duration * progress) / 100)
                                        }
                                    }

                                    registerProximityListener()

                                    Timber.d("Conver start audio")
                                    mImageButtonSpeed?.isEnabled = true

                                    mRunnable = Runnable {
                                        setSeekbarProgress()

                                        mHandler.postDelayed(
                                            mRunnable,
                                            20
                                        )
                                    }
                                    mHandler.postDelayed(mRunnable, 0)
                                    changeIconPlayPause(R.drawable.ic_baseline_pause_circle)
                                    Timber.d("*TestAudio: Play Media Player")
                                    RxBus.publish(
                                        RxEvent.StateFlag(Constants.StateFlag.ON.state)
                                    )
                                }

                                Player.STATE_ENDED -> {
                                    mStatusPlayWithSensor = true
                                    Timber.i("Conver onComplete")
                                    mSeekBar?.progress = 0
                                    unregisterProximityListener()
                                    if (wakeLock.isHeld && !isProximitySensorActive) {
                                        wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                                    }
                                    changeIconPlayPause(R.drawable.ic_baseline_play_circle)
                                    mHandler.removeCallbacks(mRunnable)
                                    mListener?.onCompleteAudio(currentMessageId)
                                    setupVoiceNoteSound(R.raw.tone_audio_message_end)
                                    Timber.d("*TestAudio: Pause Media Player")
                                    RxBus.publish(
                                        RxEvent.StateFlag(Constants.StateFlag.OFF.state)
                                    )
                                }
                            }
                        }

                        override fun onPlayerError(error: ExoPlaybackException?) {
                            Timber.w("Conver MediaPlayer Error: $error")

                            mStatusPlayWithSensor = false

                            unregisterProximityListener()
                            if (wakeLock.isHeld) {
                                wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                            }

                            if (::mRunnable.isInitialized) {
                                mHandler.removeCallbacks(mRunnable)
                            }

                            mListener?.onErrorPlayingAudio()
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            super.onIsPlayingChanged(isPlaying)
                            Timber.d("-- OLA $isPlaying")
                        }

                    })
                }
            }
        } catch (e: Exception) {
            Timber.e("Conver error: $e")
            mListener?.onErrorPlayingAudio()
        }
    }

    override fun registerProximityListener(forceEnable: Boolean) {
        if((mediaPlayer?.isPlaying == true && isProximitySensorActive == false) || forceEnable == true){
            mSensorManager.registerListener(
                this,
                mProximitySensor,
                1 * 1000 * 1000
            )
        }
    }

    override fun unregisterProximityListener() {
        mSensorManager.unregisterListener(this, mProximitySensor)
        isProximitySensorActive = false
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
        this.mImageButtonPlay = imageButtonPlay
    }

    override fun setImageButtonSpeed(imageButtonSpeed: ImageButton) {
        if (this.mImageButtonSpeed != imageButtonSpeed) {
            this.mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
            mSpeed = NORMAL_SPEED
        }
        this.mImageButtonSpeed = imageButtonSpeed
    }

    override fun setStateImageButtonSpeed(imageButtonSpeed: ImageButton, messageId: Int) {
        if (currentMessageId == messageId) {
            this.mImageButtonSpeed = imageButtonSpeed
            if (mSpeed == NORMAL_SPEED) {
                this.mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
            } else {
                this.mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_1x_circle_outline)
            }
        } else {
            this.mImageButtonSpeed?.setImageResource(R.drawable.ic_baseline_2x_circle_outline)
        }
    }

    override fun setSeekbar(seekBar: AppCompatSeekBar) {
        this.mSeekBar = seekBar
        this.mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mediaPlayer?.let {
                    if (fromUser) {
                        it.seekTo((it.duration * progress) / 100)
                    }
                    try {
                        Timber.d("Conve onProgressChanged: $progress, duration: ${it.duration}")
                        updateDuration((it.duration.toFloat() - ((it.duration.toFloat() * progress) / 100)).toLong())
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    override fun setTextViewDuration(textView: TextView) {
        this.mTextViewDuration = textView
    }

    override fun rewindMilliseconds(messageId: Int, millis: Long) {
        mediaPlayer?.let {
            if (mSeekBar != null && messageId == currentMessageId) {
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

    override fun changeSpeed(messageId: Int) {
        if (mPreviousMessageId == messageId) {
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

    override fun forwardMilliseconds(messageId: Int, millis: Long) {
        mediaPlayer?.let {
            if (mSeekBar != null && currentMessageId == messageId) {
                val minorValue = it.duration - (millis + TimeUnit.SECONDS.toMillis(1))

                Timber.d("minorValue: $minorValue, seekBarProgress: ${mSeekBar!!.progress}, duration: ${it.duration}, current: ${it.currentPosition}")

                if (it.currentPosition <= minorValue) {
                    mSeekBar?.progress =
                        (((it.currentPosition + millis) * 100) / it.duration).toInt()
                    Timber.d("minorValue ${(((it.currentPosition + millis) * 100) / it.duration).toInt()}")
                    it.seekTo(it.currentPosition + millis)
                }
            }
        }
    }

    override fun setDuration(duration: Long) {
        mediaPlayer?.let {
            if (duration > 0 && it.duration > 0) {
                val progress = ((it.currentPosition * 100) / it.duration)
                Timber.d("Conver setDuration: $duration, current: ${getCurrentPosition()}, max: ${getMax()}, audioId: ${getMessageId()}, progress: $progress")
                mSeekBar?.max = 100
                mSeekBar?.progress = progress.toInt()
            }
        }
    }

    override fun getCurrentPosition() = mediaPlayer?.currentPosition?.toInt() ?: 0

    override fun getMax() = mSeekBar?.max ?: -1

    override fun getMessageId() = this.currentMessageId

    override fun isPlaying() = mediaPlayer?.isPlaying ?: false

    override fun completeAudioPlaying() {
        mListener?.onPauseAudio(currentMessageId)
    }

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
        mPreviousMessageId = -1
        mSeekBar?.progress = 0
        mSeekBar = null
        mTextViewDuration = null
        mSpeed = NORMAL_SPEED

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        unregisterProximityListener()

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

    private fun changeIconPlayPause(drawable: Int) {
        mImageButtonPlay?.setImageDrawable(
            context.resources.getDrawable(
                drawable, context.theme
            )
        )
    }

    private fun updateDuration(duration: Long) {
        try {
            val text = Utils.getDuration(
                duration,
                showHours = false
            )
            Timber.d("Duration Audio: $text")
            mTextViewDuration?.text = text
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}