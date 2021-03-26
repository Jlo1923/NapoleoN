package com.naposystems.napoleonchat.webRTC.client

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.MODE_IN_CALL
import android.media.AudioManager.MODE_IN_COMMUNICATION
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesService
import com.naposystems.napoleonchat.service.socketMessage.SocketEventsListener
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageServiceImp
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.utility.BluetoothStateManager
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.toJSONObject
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.webRTC.CustomPeerConnectionObserver
import com.naposystems.napoleonchat.webRTC.CustomSdpObserver
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import com.pusher.client.channel.PresenceChannel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebRTCClientImp @Inject constructor(
    private val context: Context,
    private val socketMessageService: SocketMessageService,
    private val syncManager: SyncManager,
    private val notificationMessagesService: NotificationMessagesService,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val eglBase: EglBase,
    private val rtcConfiguration: PeerConnection.RTCConfiguration
) : WebRTCClient, SocketEventsListener.Call, BluetoothStateManager.BluetoothStateListener {

    //region Atributos
    override var isActiveCall: Boolean = false
    override var contactId: Int = 0
    override var isVideoCall: Boolean = false
    override var typeCall: Int = 0
    override var channel: String = ""
    //endregion


    private val vibrator: Vibrator? by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val mCallTimeRunnable: Runnable = Runnable { startCallTimer() }

    private val vibratePattern = longArrayOf(0, 400, 1000, 600, 1000, 800, 1000, 1000)

    private val countDownTime = TimeUnit.SECONDS.toMillis(30)

    private var countDownEndCall: CountDownTimer =
        object : CountDownTimer(countDownTime, TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                Timber.d("CountDown finish")
                if (!isActiveCall) {
                    webRTCClientListener?.contactNotAnswer()
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownEndCallBusy: CountDownTimer =
        object : CountDownTimer(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                if (!isActiveCall) {
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownIncomingCall: CountDownTimer =
        object : CountDownTimer(countDownTime, TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                Timber.d("CountDown finish")
                if (!isActiveCall) {
                    webRTCClientListener?.contactNotAnswer()
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private val mediaPlayer: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                .build()
        )
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var peerConnection: PeerConnection? = null
    private var mediaConstraints: MediaConstraints? = null
    private val iceCandidatesCaller: MutableList<IceCandidate> = mutableListOf()

    private var videoCapturerAndroid: VideoCapturer? = null

    private var videoSource: VideoSource? = null

    private var localVideoTrack: VideoTrack? = null
    private var localVideoView: SurfaceViewRenderer? = null

    private var localAudioTrack: AudioTrack? = null

    private var remoteVideoView: SurfaceViewRenderer? = null

    private var webRTCClientListener: WebRTCClientListener? = null

    private var bluetoothStateManager: BluetoothStateManager? = null

    private lateinit var localMediaStream: MediaStream

    private lateinit var remoteMediaStream: MediaStream

    private var callTime: Long = 0
    private var isMicOn: Boolean = true
    private var mediaPlayerHasStopped: Boolean = false
    private var renegotiateCall: Boolean = false
    private var isFirstTimeBluetoothAvailable: Boolean = false
    private var isBluetoothAvailable: Boolean = false
    private var isHeadsetConnected: Boolean = false
    private var isBluetoothStopped: Boolean = false
    private var isReturnCall: Boolean = false
    private var isVideoMuted: Boolean = false
    private var isBluetoothActive: Boolean = false
    private var contactTurnOffCamera: Boolean = false
    private var isOnCallActivity: Boolean = false
    private var stringResource: String = "android.resource://" + context.packageName + "/"

    private var textViewTimer: TextView? = null

    private val wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            WebRTCClientImp::class.simpleName
        )

    init {
        subscribeToRXEvents()
        socketMessageService.setSocketCallListener(this)
    }

    private fun subscribeToRXEvents() {
        Timber.d("subscribeToRXEvents")
        val disposableHeadsetState = RxBus.listen(RxEvent.HeadsetState::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it.state) {
                    Constants.HeadsetState.PLUGGED.state -> {
                        Timber.d("Headset plugged")
                        stopProximitySensor()
                        isHeadsetConnected = true
                        if (isVideoCall && !isBluetoothAvailable) {
                            audioManager.isSpeakerphoneOn = false
                        }

                        if (!isVideoCall && audioManager.isSpeakerphoneOn) {
                            audioManager.isSpeakerphoneOn = false
                            webRTCClientListener?.changeCheckedSpeaker(false)
                        }
                    }
                    Constants.HeadsetState.UNPLUGGED.state -> {
                        isHeadsetConnected = false
                        Timber.d("Headset unplugged")

                        if (isVideoCall && !isBluetoothAvailable) {
                            audioManager.isSpeakerphoneOn = true
                        }

                        if (isVideoCall && isBluetoothAvailable) {
                            audioManager.isSpeakerphoneOn = false
                            startProximitySensor()
                        }

                        if (!isVideoCall && !isSpeakerOn()) {
                            startProximitySensor()
                        }
                    }
                }
            }

        val disposableHangupByNotification = RxBus.listen(RxEvent.HangupByNotification::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("HangupByNotification")
                if (it.channel == this.channel) {
                    webRTCClientListener?.hangupByNotification()
                }
            }

        disposable.add(disposableHeadsetState)
        disposable.add(disposableHangupByNotification)
    }

    /**
     * Creamos la instancia de PeerConnection local
     */
    private fun createPeerConnection() {

        Timber.d("createPeerConnection")

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfiguration,
            object : CustomPeerConnectionObserver() {

                override fun onRenegotiationNeeded() {
                    super.onRenegotiationNeeded()
                    Timber.d("onRenegotiationNeeded, renegotiateCall: $renegotiateCall, isReturnCall: $isReturnCall")
                    if (renegotiateCall || isReturnCall) {
                        isReturnCall = false
                        renegotiateCall = false
                        createOffer()
                    }
                }

                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    Timber.d("onIceCandidate")
                    onIceCandidateReceived(iceCandidate)
                }

                override fun onAddTrack(
                    rtpReceiver: RtpReceiver,
                    mediaStreams: Array<MediaStream>
                ) {
                    super.onAddTrack(rtpReceiver, mediaStreams)

                    Timber.d("onAddTrack")

                    if (mediaStreams.isNotEmpty()) {

                        if (isVideoCall) {

                            Timber.d("onAddTrack isVideoCall")

                            remoteMediaStream = mediaStreams.first()

                            if (mediaStreams.first().videoTracks.isNotEmpty() && isActiveCall) {

                                if (mediaStreams.first().videoTracks.first()
                                        .state() == MediaStreamTrack.State.LIVE
                                ) {

                                    if (remoteMediaStream.videoTracks.isNotEmpty()) {

                                        remoteMediaStream.videoTracks.first()
                                            ?.addSink(remoteVideoView)
                                    }

                                    renderRemoteVideo(remoteMediaStream)
                                }
                            }
                        }
                    }
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {

                    super.onIceConnectionChange(iceConnectionState)

                    Timber.d("onIceConnectionChange $iceConnectionState")

                    if (iceConnectionState == PeerConnection.IceConnectionState.CHECKING) {
                        webRTCClientListener?.showConnectingTitle()
                    }

                    if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {

                        isActiveCall = true

                        audioManager.mode = MODE_IN_COMMUNICATION

                        countDownEndCall.cancel()

                        countDownIncomingCall.cancel()

                        initializeProximitySensor()

                        webRTCClientListener?.enableControls()

                        mHandler.postDelayed(
                            mCallTimeRunnable,
                            TimeUnit.SECONDS.toMillis(1)
                        )

                        notificationMessagesService.updateCallInProgress(
                            channel,
                            contactId,
                            isVideoCall
                        )

                        if (!isVideoCall && typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                            audioManager.isSpeakerphoneOn = false
                            webRTCClientListener?.changeCheckedSpeaker(false)
                        }

                        if (isVideoCall) {
                            renderRemoteVideo(remoteMediaStream)
                        }

                        if ((!isVideoCall && isBluetoothActive) || isHeadsetConnected) {
                            stopProximitySensor()
                        }

                    }

                    if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED ||
                        iceConnectionState == PeerConnection.IceConnectionState.CLOSED
                    ) {
                        NapoleonApplication.isShowingCallActivity = false
                        RxBus.publish(RxEvent.CallEnd())
                        val intent = Intent(context, WebRTCService::class.java)
                        intent.action = WebRTCService.ACTION_CALL_END
                        context.startService(intent)

                        mHandler.removeCallbacks(mCallTimeRunnable)

                        isMicOn = true
                        isVideoMuted = false
                        isBluetoothActive = false
                        isReturnCall = false
                        contactTurnOffCamera = false
                        callTime = 0L
                        isActiveCall = false
                        renegotiateCall = false
                        unSubscribeCallChannel()
                        webRTCClientListener?.resetIsOnCallPref()

                        Timber.d("RINGTONE END CALL TONE ${R.raw.end_call_tone}")

                        playSound(Uri.parse(stringResource + R.raw.end_call_tone), false) {

                            disposeCall()
                        }
                    }
                }
            })

        createLocalAudioTrack()

        addLocalAudioTrackToLocalMediaStream()

        peerConnection?.addStream(localMediaStream)
    }

    /**
     * Aquí creamos la oferta y la enviamos a través del socket
     */
    private fun createOffer() {
        Timber.d("LLAMADA PASO 10 OUTGOING: Creando Oferta")
        mediaConstraints = MediaConstraints()
        mediaConstraints?.mandatory?.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio",
                "true"
            )
        )
        if (isVideoCall) {
            mediaConstraints?.mandatory?.add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo",
                    "true"
                )
            )
        }
        peerConnection?.createOffer(object : CustomSdpObserver("Local offer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection?.setLocalDescription(
                    (CustomSdpObserver("Local offer")),
                    sessionDescription
                )
                Timber.d("createOffer onCreateSuccess")
                if (!isActiveCall) {
                    Timber.d("LLAMADA PASO 10.1 OUTGOING: Llamada no activa consume api")
                    syncManager.callContact(
                        contactId,
                        isVideoCall,
                        sessionDescription.toJSONObject().toString()
                    )
                } else {
                    Timber.d("LLAMADA PASO 10.2 OUTGOING: Emite llamar")
                    socketMessageService.emitToCall(
                        channel = channel,
                        jsonObject = sessionDescription.toJSONObject()
                    )
                }
            }
        }, mediaConstraints)
    }

    /**
     * Aquí creamos la respuesta y la enviamos a través del socket
     */
    override fun createAnswer() {

        peerConnection?.createAnswer(
            object : CustomSdpObserver("Local Answer") {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    peerConnection?.setLocalDescription(
                        CustomSdpObserver("Local Answer"),
                        sessionDescription
                    )
                    Timber.d("createAnswer onCreateSuccess")
                    socketMessageService.emitToCall(
                        channel = channel,
                        jsonObject = sessionDescription.toJSONObject()
                    )
                }
            }, MediaConstraints()
        )
    }

    private fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        try {
            if (isActiveCall) {
                socketMessageService.emitToCall(
                    channel = channel,
                    jsonObject = iceCandidate.toJSONObject()
                )
            } else {
                if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                    socketMessageService.emitToCall(
                        channel = channel,
                        jsonObject = iceCandidate.toJSONObject()
                    )
                } else {
                    iceCandidatesCaller.add(iceCandidate)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun initializeProximitySensor() {
        if (!isVideoCall && !audioManager.isSpeakerphoneOn && !wakeLock.isHeld) {
            wakeLock.acquire()
        }
    }

    private fun unregisterProximityListener() {
        if (wakeLock.isHeld) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
        }
    }

    private fun playSound(uriSound: Uri, isLooping: Boolean, completionCallback: () -> Unit) {
        try {
            mediaPlayer.apply {
                reset()
                if (isPlaying) {
                    stop()
                    reset()
                }
                setDataSource(
                    context,
                    uriSound
                )
                this.isLooping = isLooping
                prepare()
                setOnCompletionListener { completionCallback() }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun stopMediaPlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    private fun createLocalVideoTrack() {
        Timber.d("createLocalVideoTrack")

        videoCapturerAndroid = createVideoCapturer()
        mediaConstraints = MediaConstraints()

        videoCapturerAndroid?.let { videoCapturer ->

            val surfaceTextureHelper: SurfaceTextureHelper =
                SurfaceTextureHelper.create("SurfaceTextureHelper", eglBase.eglBaseContext)

            videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)

            videoCapturer.initialize(
                surfaceTextureHelper,
                context,
                videoSource?.capturerObserver
            )

            localVideoTrack =
                peerConnectionFactory.createVideoTrack("localVideoTrack1", videoSource)
            localVideoView?.setMirror(true)
            remoteVideoView?.setMirror(false)
        }
    }

    private fun createLocalAudioTrack() {
        val audioConstraints = MediaConstraints()
        val audioSource: AudioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack1", audioSource)
        localAudioTrack?.setEnabled(true)
    }

    private fun createVideoCapturer(): VideoCapturer? {
        return createCameraCapturer(Camera2Enumerator(context))
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Timber.d("Creating other camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun addLocalVideoTrackToLocalMediaStream() {
        localMediaStream.addTrack(localVideoTrack)
    }

    private fun addLocalAudioTrackToLocalMediaStream() {
        localMediaStream = peerConnectionFactory.createLocalMediaStream("localMediaStream")
        localMediaStream.addTrack(localAudioTrack)
    }

    private fun startCallTimer() {
        textViewTimer?.text =
            Utils.getDuration(callTime, callTime >= TimeUnit.HOURS.toMillis(1))
        val oneSecond = TimeUnit.SECONDS.toMillis(1)
        callTime += oneSecond
        mHandler.postDelayed(mCallTimeRunnable, oneSecond)
    }

    private fun renderRemoteVideo(firstMediaStream: MediaStream) {
        Timber.d("firstMediaStream, ${firstMediaStream.videoTracks.isEmpty()}")
        if (firstMediaStream.videoTracks.isNotEmpty()) {

            remoteMediaStream = firstMediaStream
            val videoTrack = firstMediaStream.videoTracks[0]
            try {
                stopProximitySensor()

                if (isBluetoothAvailable) {
                    audioManager.isSpeakerphoneOn = false
                } else {
                    audioManager.isSpeakerphoneOn = !this.isHeadsetConnected
                }
                webRTCClientListener?.showRemoteVideo()

                videoTrack.addSink(remoteVideoView)
            } catch (e: Exception) {
                Timber.d("NO Got Remote Stream")
                Timber.e(e)
            }
        }
    }

    //region Implementation IContractWebRTCClient

    override fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener) {

        Timber.d("LLAMADA PASO 2 OUTGOING: SETEANDO webRTCClientListener")

        this.webRTCClientListener = webRTCClientListener

        bluetoothStateManager = BluetoothStateManager(context, this)

        isOnCallActivity = true

        if (!isActiveCall) {
            createPeerConnection()
        }

    }

    override fun setOffer(offer: String?) {
        Timber.d("setOffer")
        offer?.let {
            val jsonData = JSONObject(it)

            val sessionDescription = jsonData.toSessionDescription(
                SessionDescription.Type.OFFER
            )

            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Remote offer"),
                sessionDescription
            )
        }
    }

    override fun subscribeToCallChannel() {
        Timber.d("LLAMADA PASO 4 OUTGOING: SUSCRIBIRSE AL CANAL DE LLAMADAS")
        socketMessageService.subscribeToCallChannel(
            contactId,
            channel,
            isVideoCall
        )
    }

    override fun setTextViewCallDuration(textView: TextView) {
        this.textViewTimer = textView
    }

    override fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        this.localVideoView = surfaceViewRenderer
    }

    override fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        this.remoteVideoView = surfaceViewRenderer
    }

    override fun setSpeakerOn(isChecked: Boolean) {
        Timber.d("setSpeakerOn: $isChecked")
        audioManager.isSpeakerphoneOn = isChecked

        if (isChecked || audioManager.isBluetoothScoOn) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            stopProximitySensor()
        } else {
            startProximitySensor()
        }
    }

    override fun setMicOff() {
        isMicOn = !isMicOn
        localAudioTrack?.setEnabled(isMicOn)
    }

    override fun setItsReturnCall(itsReturnCall: Boolean) {
        if (isVideoCall) {
            this.isReturnCall = itsReturnCall
        }
    }

    override fun getMicIsOn(): Boolean = this.isMicOn

    override fun isSpeakerOn(): Boolean = audioManager.isSpeakerphoneOn

    override fun isVideoMuted(): Boolean = this.isVideoMuted

    override fun isBluetoothActive(): Boolean = this.isBluetoothActive

    override fun contactTurnOffCamera(): Boolean = this.contactTurnOffCamera

    override fun setIsOnCallActivity(isOnCallActivity: Boolean) {
        this.isOnCallActivity = isOnCallActivity
    }

    override fun initSurfaceRenders() {
        localVideoView?.init(eglBase.eglBaseContext, null)
        localVideoView?.setZOrderMediaOverlay(true)

        remoteVideoView?.init(eglBase.eglBaseContext, null)
        remoteVideoView?.setZOrderMediaOverlay(true)
    }

    override fun startCaptureVideo() {
        createLocalVideoTrack()
        addLocalVideoTrackToLocalMediaStream()
        videoCapturerAndroid?.startCapture(1280, 720, 30)
        localVideoTrack?.addSink(localVideoView)
    }

//    override fun emitJoinToCall() {
//        socketMessageService.emitToCall(channel, SocketMessageServiceImp.CONTACT_JOIN_TO_CALL)
//    }

    override fun stopRingAndVibrate() {
        stopMediaPlayer()
        vibrator?.cancel()
    }

    override fun emitHangUp() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        audioManager.isSpeakerphoneOn = false
        socketMessageService.emitToCall(channel, SocketMessageServiceImp.HANGUP_CALL)
    }

    override fun changeToVideoCall() {
        if (!isVideoCall) {
            socketMessageService.emitToCall(
                channel,
                SocketMessageServiceImp.CONTACT_WANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun cancelChangeToVideoCall() {
        socketMessageService.emitToCall(
            channel,
            SocketMessageServiceImp.CONTACT_CANCEL_CHANGE_TO_VIDEO
        )
    }

    override fun muteVideo(checked: Boolean, itsFromBackPressed: Boolean) {
        if (isVideoCall && localMediaStream.videoTracks.isNotEmpty()) {
            val videoTrack = localMediaStream.videoTracks.first()
            isVideoMuted = checked

            if (checked) {
                socketMessageService.emitToCall(
                    channel,
                    SocketMessageServiceImp.CONTACT_TURN_OFF_CAMERA
                )
                webRTCClientListener?.changeLocalRenderVisibility(View.GONE)
                videoTrack.setEnabled(false)
            } else {
                socketMessageService.emitToCall(
                    channel,
                    SocketMessageServiceImp.CONTACT_TURN_ON_CAMERA
                )
                webRTCClientListener?.changeLocalRenderVisibility(View.VISIBLE)
                videoTrack.setEnabled(true)
            }
        }

        if (itsFromBackPressed) {
            videoCapturerAndroid?.stopCapture()
            localMediaStream.removeTrack(localVideoTrack)
        }
    }

    override fun switchCamera() {
        val videoCapturer = videoCapturerAndroid as CameraVideoCapturer
        videoCapturer.switchCamera(null)
    }

    override fun handleBluetooth(isEnabled: Boolean) {
        Timber.d("handleBluetooth: $isEnabled, $isVideoCall")
        isBluetoothActive = isEnabled

        if (isEnabled) {
            stopProximitySensor()
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
            isBluetoothStopped = false
        } else {
            isBluetoothStopped = true
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false

            when {
                isHeadsetConnected -> {
                    audioManager.isSpeakerphoneOn = false
                }
                isBluetoothAvailable -> {
                    audioManager.isSpeakerphoneOn = true
                    audioManager.mode = MODE_IN_CALL
                    stopProximitySensor()
                }
                else -> {
                    startProximitySensor()
                    audioManager.isSpeakerphoneOn = isVideoCall
                }
            }
        }
    }

    override fun playRingtone() {
        mediaPlayerHasStopped = false
        countDownIncomingCall.start()
        playSound(Settings.System.DEFAULT_RINGTONE_URI, true) {
            // Intentionally empty
        }
        audioManager.isSpeakerphoneOn = !(isBluetoothAvailable || isHeadsetConnected)
        Timber.d("*Test: ${audioManager.isSpeakerphoneOn}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(vibratePattern, 0)
            vibrator?.vibrate(effect)
        } else {
            vibrator?.vibrate(vibratePattern, 0)
        }
    }

    override fun playRingBackTone() {

        audioManager.mode = MODE_IN_COMMUNICATION

        if (isBluetoothAvailable || isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.isSpeakerphoneOn = isVideoCall
        }

        countDownEndCall.start()
        Timber.d("RINGTONE PLAYING ${R.raw.ringback_tone}")
        playSound(Uri.parse(stringResource + R.raw.ringback_tone), true) {

        }
    }

    override fun acceptChangeToVideoCall() {
        isVideoCall = true
        startCaptureVideo()
        socketMessageService.emitToCall(
            channel,
            SocketMessageServiceImp.CONTACT_ACCEPT_CHANGE_TO_VIDEO
        )
        webRTCClientListener?.changeTextViewTitle(R.string.text_encrypted_video_call)

    }

    override fun startProximitySensor() {
        if (!audioManager.isSpeakerphoneOn && !isHeadsetConnected && !isBluetoothActive) {
            initializeProximitySensor()
        }
    }

    override fun stopProximitySensor() {
        unregisterProximityListener()
    }

    override fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (!isActiveCall) {
                    stopMediaPlayer()
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    override fun disposeCall() {

        NapoleonApplication.isOnCall = false

        socketMessageService.unSubscribeCallChannel(this.channel)

        val intent = Intent(context, WebRTCService::class.java)

        intent.action = WebRTCService.ACTION_CALL_END

        context.startService(intent)

        audioManager.mode = AudioManager.MODE_NORMAL

        audioManager.stopBluetoothSco()

        audioManager.isBluetoothScoOn = false

        audioManager.isSpeakerphoneOn = false

        unregisterProximityListener()

        countDownEndCallBusy.cancel()
        countDownEndCall.cancel()
        countDownIncomingCall.cancel()

        stopRingAndVibrate()

        stopMediaPlayer()

        //disposable.clear()

        if (isVideoCall) {
//            localVideoTrack?.removeSink(localVideoView)
            localVideoView?.release()
            remoteVideoView?.release()
//            videoSource?.dispose()
            videoCapturerAndroid?.dispose()
        }

//        localMediaStream.dispose()
//
//        remoteMediaStream.dispose()
//
//        localAudioTrack?.dispose()

        mHandler.removeCallbacks(mCallTimeRunnable)

        bluetoothStateManager?.onDestroy()

        webRTCClientListener?.callEnded()

        if (isActiveCall) {
            peerConnection?.close()
            peerConnection?.dispose()
        }
    }

    override fun unSubscribeCallChannel() {
        socketMessageService.unSubscribeCallChannel(channel)
    }

    override fun getPusherChannel(channel: String): PresenceChannel? {
        return socketMessageService.getPusherChannel(channel)
    }

    override fun renderRemoteVideo() {
        if (remoteMediaStream.videoTracks.isNotEmpty()) {
            remoteMediaStream.videoTracks.first()?.addSink(remoteVideoView)
        }
    }

//endregion

    //region Implementation BluetoothStateManager.BluetoothStateListener
    override fun onBluetoothStateChanged(isAvailable: Boolean) {
        Timber.d("onBluetoothStateChanged: $isAvailable")

        isBluetoothAvailable = isAvailable

        if (!isFirstTimeBluetoothAvailable && !isHeadsetConnected) {
            Timber.d("isFirstTimeBluetoothAvailableeeee")
            isFirstTimeBluetoothAvailable = true
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
            stopProximitySensor()
        }

        if (isAvailable && isVideoCall && isBluetoothStopped) {
            Timber.d("onBluetoothStateChanged 2do")
            audioManager.isSpeakerphoneOn = true
        }

        if (isAvailable && !isVideoCall) {
            stopProximitySensor()
        }

        if (!isAvailable && isHeadsetConnected) {
            Timber.d("onBluetoothStateChanged 3ero")
            audioManager.isSpeakerphoneOn = false
        }

        webRTCClientListener?.changeBluetoothButtonVisibility(isAvailable)
    }
    //endregion


    //region Implementation SocketEventListener.Call

    //region Conexion
    override fun itsSubscribedToCallChannel(
        contactId: Int,
        channelName: String,
        isVideoCall: Boolean
    ) {
        Timber.d("LLAMADA PASO 7 OUTGOING: ya Suscrito")
        if (channelName == this.channel) {
            stopMediaPlayer()
            if (!isActiveCall) {
                Timber.d("LLAMADA PASO 8 OUTGOING: En llamada activa crea PeerConnection")
                createPeerConnection()
            }
            Timber.d("LLAMADA PASO 9 OUTGOING: Crea Offer")
            createOffer()
        }
    }

    override fun iceCandidateReceived(channelName: String, iceCandidate: IceCandidate) {
        if (channelName == this.channel)
            peerConnection?.addIceCandidate(iceCandidate)
    }

    override fun offerReceived(channelName: String, sessionDescription: SessionDescription) {
        if (channelName == this.channel) {
            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Remote offer"),
                sessionDescription
            )
            createAnswer()
        }
    }

    override fun answerReceived(channelName: String, sessionDescription: SessionDescription) {
        if (channelName == this.channel) {
            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Answer"),
                sessionDescription
            )

            if (typeCall == Constants.TypeCall.IS_OUTGOING_CALL.type && iceCandidatesCaller.isNotEmpty()) {
                iceCandidatesCaller.forEach { iceCandidate ->
                    socketMessageService.emitToCall(
                        channel = channel,
                        jsonObject = iceCandidate.toJSONObject()
                    )
                }
                iceCandidatesCaller.clear()
            }
        }
    }
    //endregion

    //region Handler Call
    override fun contactRejectCall(channelName: String) {
        webRTCClientListener?.changeTextViewTitle(R.string.text_contact_is_busy)
        countDownEndCallBusy.start()
        playSound(Uri.parse(stringResource + R.raw.busy_tone), true) { }
    }

    override fun contactCancelCall(channelName: String) {
        if (channelName == this.channel) {
            try {
                audioManager.isSpeakerphoneOn = false
                audioManager.mode = AudioManager.MODE_NORMAL
                stopMediaPlayer()
                unSubscribeCallChannel()
                localAudioTrack?.setEnabled(false)
            } catch (e: Exception) {
                Timber.e("Error manejado, $e")
            } finally {
                disposeCall()
            }
        }
    }
    //endregion

    //region ChangeToVideoCall
    override fun contactWantChangeToVideoCall(channelName: String) {
        if (channelName == this.channel && isOnCallActivity)
            webRTCClientListener?.contactWantChangeToVideoCall()
        else {
            socketMessageService.emitToCall(
                this.channel,
                SocketMessageServiceImp.CONTACT_CANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun contactAcceptChangeToVideoCall(channelName: String) {
        if (channelName == this.channel && !isVideoCall) {
            webRTCClientListener?.changeTextViewTitle(R.string.text_encrypted_video_call)
            webRTCClientListener?.contactAcceptChangeToVideoCall()
            isVideoCall = true
            renegotiateCall = true
            startCaptureVideo()
        }
    }

    override fun contactCancelChangeToVideoCall(channelName: String) {
        if (channelName == this.channel)
            webRTCClientListener?.contactCancelledVideoCall()
    }

    override fun contactCantChangeToVideoCall(channelName: String) {
        if (channelName == this.channel)
            webRTCClientListener?.unlockVideoButton()
    }
    //endregion

    //region Handler Camera
    override fun contactTurnOnCamera(channelName: String) {
        if (channelName == this.channel) {
            contactTurnOffCamera = false
            webRTCClientListener?.contactTurnOnCamera()
        }
    }

    override fun contactTurnOffCamera(channelName: String) {
        if (channelName == this.channel) {
            contactTurnOffCamera = true
            webRTCClientListener?.contactTurnOffCamera()
        }
    }
    //endregion

    //region Hangup
    override fun contactHasHangup(channelName: String) {
        if (channelName == this.channel && typeCall == Constants.TypeCall.IS_OUTGOING_CALL.type)
            disposeCall()

    }
    //endregion

    //endregion

}