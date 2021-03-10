package com.naposystems.napoleonchat.webRTC

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
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notification.NotificationService
import com.naposystems.napoleonchat.service.socket.SocketService
import com.naposystems.napoleonchat.service.socket.SocketServiceImp
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.utility.BluetoothStateManager
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.toJSONObject
import com.pusher.client.channel.PresenceChannel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebRTCClient @Inject constructor(
    private val context: Context,
    private val socketService: SocketService
) : IContractWebRTCClient, BluetoothStateManager.BluetoothStateListener {

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
                    mListener?.contactNotAnswer()
                    dispose()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownEndCallBusy: CountDownTimer =
        object : CountDownTimer(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                if (!isActiveCall) {
                    dispose()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownIncomingCall: CountDownTimer =
        object : CountDownTimer(countDownTime, TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                Timber.d("CountDown finish")
                if (!isActiveCall) {
                    mListener?.contactNotAnswer()
                    dispose()
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

    private var videoCapturerAndroid: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var sdpConstraints: MediaConstraints? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localPeer: PeerConnection? = null
    private var localVideoView: SurfaceViewRenderer? = null
    private var remoteVideoView: SurfaceViewRenderer? = null
    private var channel: String = ""
    private var textViewTimer: TextView? = null
    private var mListener: WebRTCClientListener? = null
    private var bluetoothStateManager: BluetoothStateManager? = null

    private var isActiveCall: Boolean = false
    private var callTime: Long = 0
    private var contactId: Int = 0
    private var isVideoCall: Boolean = false
    private var incomingCall: Boolean = false
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

    private var peerIceServer: MutableList<PeerConnection.IceServer> = arrayListOf(
        PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER)
            .createIceServer(),
        PeerConnection.IceServer.builder(BuildConfig.TURN_SERVER)
            .setUsername("wPJlHAYY")
            .setPassword("GrI09zxkwFuOihIf")
            .createIceServer()
    )

    private val eglBase: EglBase by lazy {
        EglBase.create()
    }

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            /* enableIntelVp8Encoder */true,
            /* enableH264HighProfile */true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    private lateinit var localMediaStream: MediaStream
    private lateinit var remoteMediaStream: MediaStream

    private val wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            WebRTCClient::class.simpleName
        )

    interface WebRTCClientListener {
        fun contactWantChangeToVideoCall()
        fun contactCancelledVideoCall()
        fun contactTurnOffCamera()
        fun contactTurnOnCamera()
        fun showRemoteVideo()
        fun callEnded()
        fun changeLocalRenderVisibility(visibility: Int)
        fun changeTextViewTitle(stringResourceId: Int)
        fun changeBluetoothButtonVisibility(isVisible: Boolean)
        fun enableControls()
        fun resetIsOnCallPref()
        fun contactNotAnswer()
        fun showTimer()
        fun showConnectingTitle()
        fun changeCheckedSpeaker(checked: Boolean)
        fun hangupByNotification()
        fun unlockVideoButton()
        fun rejectByNotification()
    }

    init {
        subscribeToRXEvents()
    }

    private fun subscribeToRXEvents() {
        Timber.d("subscribeToRXEvents")
        val disposableContactJoinToCall = RxBus.listen(RxEvent.ContactHasJoinToCall::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("ContactHasJoinToCall, ${it.channel}, ${this.channel}")
                if (it.channel == this.channel) {
                    stopMediaPlayer()
                    createOffer()
                }
            }

        val disposableIceCandidateReceived = RxBus.listen(RxEvent.IceCandidateReceived::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channel == this.channel) {
                    localPeer?.addIceCandidate(it.iceCandidate)
                }
            }

        val disposableOfferReceived = RxBus.listen(RxEvent.OfferReceived::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channel == this.channel) {
                    Timber.d("OfferReceived")
                    localPeer?.setRemoteDescription(
                        CustomSdpObserver("Remote offer"),
                        it.sessionDescription
                    )
                    createAnswer()
                }
            }

        val disposableAnswerReceived = RxBus.listen(RxEvent.AnswerReceived::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channel == this.channel) {
                    Timber.d("AnswerReceived")
                    localPeer?.setRemoteDescription(
                        CustomSdpObserver("Answer"),
                        it.sessionDescription
                    )
                }
            }

        val disposableContactHasHangup = RxBus.listen(RxEvent.ContactHasHangup::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("ContactHasHangup")
                if (it.channel == this.channel && !isIncomingCall()) {
                    Data.isContactReadyForCall = false
                    stopMediaPlayer()
                    unSubscribeCallChannel()
                    localPeer?.dispose()
                }
            }

        val disposableContactWantChangeToVideoCall =
            RxBus.listen(RxEvent.ContactWantChangeToVideoCall::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("isOnCallActivity: $isOnCallActivity")
                    if (it.channel == this.channel && isOnCallActivity) {
                        Timber.d("ContactWantChangeToVideoCall")
                        mListener?.contactWantChangeToVideoCall()
                    } else {
                        socketService.emitToCall(
                            this.channel,
                            SocketServiceImp.CONTACT_CANT_CHANGE_TO_VIDEO
                        )
                    }
                }

        val disposableContactAcceptChangeToVideoCall =
            RxBus.listen(RxEvent.ContactAcceptChangeToVideoCall::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.channel == this.channel && !isVideoCall) {
                        Timber.d("ContactAcceptChangeToVideoCall")
                        isVideoCall = true
                        renegotiateCall = true
                        startCaptureVideo()
                    }
                }

        val disposableContactCancelChangeToVideoCall =
            RxBus.listen(RxEvent.ContactCancelChangeToVideoCall::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.channel == this.channel) {
                        mListener?.contactCancelledVideoCall()
                    }
                }

        val disposableContactTurnOffCamera = RxBus.listen(RxEvent.ContactTurnOffCamera::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channel == this.channel) {
                    contactTurnOffCamera = true
                    mListener?.contactTurnOffCamera()
                }
            }

        val disposableContactTurnOnCamera = RxBus.listen(RxEvent.ContactTurnOnCamera::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.channel == this.channel) {
                    contactTurnOffCamera = false
                    mListener?.contactTurnOnCamera()
                }
            }

        val disposableContactRejectCall = RxBus.listen(RxEvent.ContactRejectCall::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Data.isContactReadyForCall = false
                mListener?.changeTextViewTitle(R.string.text_contact_is_busy)
                countDownEndCallBusy.start()
                playSound(
                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.busy_tone),
                    true
                ) {
                    // Intentionally empty
                }
            }

        val disposableHeadsetState = RxBus.listen(RxEvent.HeadsetState::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it.state) {
                    Constants.HeadsetState.PLUGGED.state -> {
                        Timber.d("Headset plugged")
                        isHeadsetConnected = true
                        if (isVideoCall && !isBluetoothAvailable) {
                            audioManager.isSpeakerphoneOn = false
                        }

                        if (!isVideoCall && audioManager.isSpeakerphoneOn) {
                            audioManager.isSpeakerphoneOn = false
                            mListener?.changeCheckedSpeaker(false)
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
                        }
                    }
                }
            }

        val disposableContactCancelCall = RxBus.listen(RxEvent.ContactCancelCall::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("ContactCancelCall")
                if (it.channel == this.channel) {
                    try {
                        Data.isContactReadyForCall = false
                        audioManager.isSpeakerphoneOn = false
                        audioManager.mode = AudioManager.MODE_NORMAL
                        stopMediaPlayer()
                        unSubscribeCallChannel()
                        localAudioTrack?.setEnabled(false)
                    } catch (e: Exception) {
                        Timber.e("Error manejado, $e")
                    } finally {
                        dispose()
                    }
                }
            }

        val disposableHangupByNotification = RxBus.listen(RxEvent.HangupByNotification::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("HangupByNotification")
                if (it.channel == this.channel) {
                    Data.isContactReadyForCall = false
                    mListener?.hangupByNotification()
                }
            }

        val disposableContactCantChangeToVideoCall =
            RxBus.listen(RxEvent.ContactCantChangeToVideoCall::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("ContactCantChangeToVideoCall")
                    if (it.channel == this.channel) {
                        mListener?.unlockVideoButton()
                    }
                }

        /*val disposableRejectByNotification =
            RxBus.listen(RxEvent.RejectCallByNotification::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("RejectCallByNotification")
                    if (it.channel == this.channel) {
                        mListener?.rejectByNotification()
                    }
                }*/

        disposable.add(disposableContactJoinToCall)
        disposable.add(disposableIceCandidateReceived)
        disposable.add(disposableOfferReceived)
        disposable.add(disposableAnswerReceived)
        disposable.add(disposableContactHasHangup)
        disposable.add(disposableContactWantChangeToVideoCall)
        disposable.add(disposableContactAcceptChangeToVideoCall)
        disposable.add(disposableContactCancelChangeToVideoCall)
        disposable.add(disposableContactTurnOffCamera)
        disposable.add(disposableContactTurnOnCamera)
        disposable.add(disposableContactRejectCall)
        disposable.add(disposableHeadsetState)
        disposable.add(disposableContactCancelCall)
        disposable.add(disposableHangupByNotification)
        disposable.add(disposableContactCantChangeToVideoCall)
    }

    private fun initializeProximitySensor() {
        if (!isVideoCall && !audioManager.isSpeakerphoneOn && !wakeLock.isHeld) {
            wakeLock.acquire(TimeUnit.HOURS.toMillis(99))
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

    /**
     * Creamos la instancia de PeerConnection local
     */
    private fun createPeerConnection() {
        Timber.d("createPeerConnection")
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServer)

        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        //Usamos ECDSA encryption
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA

        localPeer = peerConnectionFactory.createPeerConnection(
            rtcConfig,
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
                            renderRemoteVideo(mediaStreams.first())
                        }
                    }
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                    super.onIceConnectionChange(iceConnectionState)
                    Timber.d("onIceConnectionChange $iceConnectionState")

                    if (iceConnectionState == PeerConnection.IceConnectionState.CHECKING) {
                        mListener?.showConnectingTitle()
                    }

                    if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                        isActiveCall = true
                        countDownEndCall.cancel()
                        countDownIncomingCall.cancel()
                        initializeProximitySensor()
                        mListener?.enableControls()
                        //mListener?.showTimer()
                        mHandler.postDelayed(
                            mCallTimeRunnable,
                            TimeUnit.SECONDS.toMillis(1)
                        )

//                        val notificationService = NotificationService()
                        val notificationService = NotificationService(context.applicationContext)
                        notificationService.updateCallInProgress(channel, contactId, isVideoCall)

                        if (!isVideoCall && incomingCall) {
                            audioManager.isSpeakerphoneOn = false
                            mListener?.changeCheckedSpeaker(false)
                        }
                    }

                    /*if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                        RxBus.publish(RxEvent.CallEnd())
                        val intent = Intent(context, WebRTCCallService::class.java)
                        intent.action = WebRTCCallService.ACTION_CALL_END
                        context.startService(intent)

                        isMicOn = true
                        isVideoMuted = false
                        isBluetoothActive = false
                        isReturnCall = false
                        contactTurnOffCamera = false
                        callTime = 0L
                        isActiveCall = false
                        renegotiateCall = false
                        unSubscribeCallChannel()
                        mListener?.resetIsOnCallPref()

                        playSound(
                            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.end_call_tone),
                            false
                        ) {
                            dispose()
                        }
                    }*/

                    if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED ||
                        iceConnectionState == PeerConnection.IceConnectionState.CLOSED
                    ) {
                        Data.isContactReadyForCall = false
                        RxBus.publish(RxEvent.CallEnd())
                        val intent = Intent(context, WebRTCCallService::class.java)
                        intent.action = WebRTCCallService.ACTION_CALL_END
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
                        mListener?.resetIsOnCallPref()

                        playSound(
                            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.end_call_tone),
                            false
                        ) {
                            dispose()
                        }
                    }
                }
            })

        createLocalAudioTrack()
        addLocalAudioTrackToLocalMediaStream()

        /*if (isVideoCall) {
            createLocalVideoTrack()
            addLocalVideoTrackToLocalMediaStream()
        }*/

        localPeer?.addStream(localMediaStream)
    }

    private fun createLocalVideoTrack() {
        Timber.d("createLocalVideoTrack")

        videoCapturerAndroid = createVideoCapturer()
        sdpConstraints = MediaConstraints()

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

    /**
     * Agregamos el localVideoTrack al localMediaStream
     */
    private fun addLocalVideoTrackToLocalMediaStream() {
        localMediaStream.addTrack(localVideoTrack)
    }

    private fun addLocalAudioTrackToLocalMediaStream() {
        localMediaStream = peerConnectionFactory.createLocalMediaStream("localMediaStream")
        localMediaStream.addTrack(localAudioTrack)
    }

    /**
     * Recibe el ice candidate local y lo envía al peer remoto a través del socket
     */
    private fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        try {
            socketService.emitToCall(channel = channel, jsonObject = iceCandidate.toJSONObject())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun startCallTimer() {
        textViewTimer?.text =
            Utils.getDuration(callTime, callTime >= TimeUnit.HOURS.toMillis(1))
        val oneSecond = TimeUnit.SECONDS.toMillis(1)
        callTime += oneSecond
        mHandler.postDelayed(mCallTimeRunnable, oneSecond)
    }

    /**
     * Aquí creamos la oferta y la enviamos a través del socket
     */
    private fun createOffer() {
        Timber.d("createOffer")
        sdpConstraints = MediaConstraints()
        sdpConstraints?.mandatory?.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        if (isVideoCall) {
            sdpConstraints?.mandatory?.add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo",
                    "true"
                )
            )
        }
        localPeer?.createOffer(object : CustomSdpObserver("Local offer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    (CustomSdpObserver("Local offer")),
                    sessionDescription
                )
                Timber.d("createOffer onCreateSuccess")
                socketService.emitToCall(
                    channel = channel,
                    jsonObject = sessionDescription.toJSONObject()
                )
            }
        }, sdpConstraints)
    }

    /**
     * Aquí creamos la respuesta y la enviamos a través del socket
     */
    private fun createAnswer() {
        localPeer?.createAnswer(object : CustomSdpObserver("Local Answer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("Local Answer"),
                    sessionDescription
                )
                Timber.d("createAnswer onCreateSuccess")
                socketService.emitToCall(
                    channel = channel,
                    jsonObject = sessionDescription.toJSONObject()
                )
            }
        }, MediaConstraints())
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
                mListener?.showRemoteVideo()

                videoTrack.addSink(remoteVideoView)
            } catch (e: Exception) {
                Timber.d("NO Got Remote Stream")
                Timber.e(e)
            }
        }
    }

    //region Implementation IContractWebRTCClient

    override fun setListener(webRTCClientListener: WebRTCClientListener) {
        this.mListener = webRTCClientListener
        bluetoothStateManager = BluetoothStateManager(context, this)
        isOnCallActivity = true
        Timber.d("isOnCallActivity: $isOnCallActivity")

        if (!isActiveCall) {
            createPeerConnection()
        }
    }

    override fun getContactId() = this.contactId

    override fun setContactId(contactId: Int) {
        this.contactId = contactId
    }

    override fun isVideoCall() = this.isVideoCall

    override fun setIsVideoCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
    }

    override fun isIncomingCall(): Boolean = this.incomingCall

    override fun setIncomingCall(incomingCall: Boolean) {
        this.incomingCall = incomingCall
    }

    override fun getChannel() = this.channel

    override fun setChannel(channel: String) {
        this.channel = channel
    }

    override fun subscribeToChannel(isActionAnswer: Boolean) {
        socketService.subscribeToCallChannel(channel, isActionAnswer, isVideoCall)
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
        audioManager.isSpeakerphoneOn = isChecked

        if (isChecked || audioManager.isBluetoothScoOn) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            unregisterProximityListener()
        } else {
            initializeProximitySensor()
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

    override fun emitJoinToCall() {
        socketService.joinToCall(channel)
    }

    override fun stopRingAndVibrate() {
        stopMediaPlayer()
        vibrator?.cancel()
    }

    override fun emitHangUp() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        audioManager.isSpeakerphoneOn = false
        socketService.emitToCall(channel, SocketServiceImp.HANGUP_CALL)
    }

    override fun changeToVideoCall() {
        if (!isVideoCall) {
            socketService.emitToCall(channel, SocketServiceImp.CONTACT_WANT_CHANGE_TO_VIDEO)
        }
    }

    override fun cancelChangeToVideoCall() {
        socketService.emitToCall(channel, SocketServiceImp.CONTACT_CANCEL_CHANGE_TO_VIDEO)
    }

    override fun muteVideo(checked: Boolean, itsFromBackPressed: Boolean) {
        if (isVideoCall && localMediaStream.videoTracks.isNotEmpty()) {
            val videoTrack = localMediaStream.videoTracks.first()
            isVideoMuted = checked

            if (checked) {
                socketService.emitToCall(channel, SocketServiceImp.CONTACT_TURN_OFF_CAMERA)
                mListener?.changeLocalRenderVisibility(View.GONE)
                videoTrack.setEnabled(false)
            } else {
                socketService.emitToCall(channel, SocketServiceImp.CONTACT_TURN_ON_CAMERA)
                mListener?.changeLocalRenderVisibility(View.VISIBLE)
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
        isBluetoothActive = isEnabled

        if (isEnabled) {
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
                }
                else -> {
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

    override fun playCallingTone() {
        audioManager.mode = MODE_IN_COMMUNICATION
        if (isBluetoothAvailable || isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.isSpeakerphoneOn = isVideoCall
        }
        countDownEndCall.start()
        playSound(
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.ringback_tone),
            true
        ) {
            // Intentionally empty
        }
    }

    override fun acceptChangeToVideoCall() {
        isVideoCall = true
        startCaptureVideo()
        socketService.emitToCall(channel, SocketServiceImp.CONTACT_ACCEPT_CHANGE_TO_VIDEO)
        mListener?.changeTextViewTitle(R.string.text_encrypted_video_call)
        //renderRemoteVideo()
    }

    override fun startProximitySensor() {
        initializeProximitySensor()
    }

    override fun stopProximitySensor() {
        unregisterProximityListener()
    }

    override fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
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

    override fun isActiveCall() = isActiveCall

    override fun dispose() {
        Timber.d("Dispose")

        socketService.unSubscribeCallChannel(this.channel)

        Data.isOnCall = false
        Data.isContactReadyForCall = false

        val intent = Intent(context, WebRTCCallService::class.java)
        intent.action = WebRTCCallService.ACTION_CALL_END
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

        videoCapturerAndroid?.dispose()
        localVideoView?.release()
        remoteVideoView?.release()

        mHandler.removeCallbacks(mCallTimeRunnable)

        bluetoothStateManager?.onDestroy()

        mListener?.callEnded()
        if (isActiveCall)
            localPeer?.close()
    }

    override fun unSubscribeCallChannel() {
        socketService.unSubscribeCallChannel(channel)
    }

    override fun subscribeToChannelFromBackground(channel: String) {
        socketService.joinToCall(channel)
    }

    override fun getPusherChannel(channel: String): PresenceChannel? {
        return socketService.getPusherChannel(channel)
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

        //handleBluetooth(isAvailable)
        isBluetoothAvailable = isAvailable

        if (!isFirstTimeBluetoothAvailable && !isHeadsetConnected) {
            Timber.d("isFirstTimeBluetoothAvailable")
            isFirstTimeBluetoothAvailable = true
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
        }

        if (isAvailable && isVideoCall && isBluetoothStopped) {
            audioManager.isSpeakerphoneOn = true
        }

        if (!isAvailable && isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        }

        mListener?.changeBluetoothButtonVisibility(isAvailable)
    }
    //endregion
}