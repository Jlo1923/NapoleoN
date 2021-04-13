package com.naposystems.napoleonchat.webRTC.client

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.socketClient.SocketClientImp
import com.naposystems.napoleonchat.service.socketClient.SocketEventListener
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.utility.BluetoothStateManager
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.toJSONObject
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import com.naposystems.napoleonchat.webRTC.CustomPeerConnectionObserver
import com.naposystems.napoleonchat.webRTC.CustomSdpObserver
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//TODO: Refactorizar esta clase e independizar la conexion del WebRTC, con los contadores y los manejadores de notificacion
class WebRTCClientImp
@Inject constructor(
    private val context: Context,
    private val socketClient: SocketClient,
    private val syncManager: SyncManager,
    private val handlerNotification: HandlerNotification,
    private val handlerMediaPlayerNotification: HandlerMediaPlayerNotification
) : WebRTCClient,
    SocketEventListener,
    BluetoothStateManager.BluetoothStateListener {

    //region Atributos
    override var callModel = CallModel(
        contactId = 0,
        isVideoCall = false,
        typeCall = Constants.TypeCall.IS_OUTGOING_CALL,
        channelName = "",
        offer = ""
    )

    override var renegotiateCall: Boolean = false

    override var isActiveCall: Boolean = false

    override var isHideVideo: Boolean = false

    override var contactCameraIsVisible: Boolean = false

    override var isMicOn: Boolean = true

    override var isBluetoothActive: Boolean = false
    //endregion

    private var mHandler: Handler = Handler(Looper.getMainLooper())

    private var mCallTimeRunnable = Runnable { startCallTimer() }

    //Tiempo de Repique
    private var countDownRingCall: CountDownTimer = object : CountDownTimer(
        TimeUnit.SECONDS.toMillis(30),
        TimeUnit.SECONDS.toMillis(1)
    ) {
        override fun onFinish() {
            Timber.d("CountDown finish")
            if (isActiveCall.not()) {
                webRTCClientListener?.onContactNotAnswer()
                disposeCall()
            }
        }

        override fun onTick(millisUntilFinished: Long) = Unit
    }

    private var countDownEndCallBusy: CountDownTimer = object : CountDownTimer(
        TimeUnit.SECONDS.toMillis(3),
        TimeUnit.SECONDS.toMillis(1)
    ) {
        override fun onFinish() {
            if (isActiveCall.not()) {
                disposeCall()
            }
        }

        override fun onTick(millisUntilFinished: Long) = Unit
    }

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


    private var peerIceServer: MutableList<PeerConnection.IceServer> = arrayListOf(
        PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER)
            .createIceServer(),
        PeerConnection.IceServer.builder(BuildConfig.TURN_SERVER)
            .setUsername("wPJlHAYY")
            .setPassword("GrI09zxkwFuOihIf")
            .createIceServer()
    )

    //TODO: Revisar el AudioAttribute
    private val mediaPlayer: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                .build()
        )
    }

    private var disposable: CompositeDisposable = CompositeDisposable()

    private lateinit var audioManager: AudioManager

    private var peerConnection: PeerConnection? = null

    private var mediaConstraints: MediaConstraints? = null

    private var iceCandidatesCaller: MutableList<IceCandidate> = mutableListOf()

    private var videoCapturerAndroid: VideoCapturer? = null

    private lateinit var localMediaStream: MediaStream

    private var localVideoSource: VideoSource? = null

    private var localVideoTrack: VideoTrack? = null

    private var localAudioTrack: AudioTrack? = null

    private var localSurfaceViewRenderer: SurfaceViewRenderer? = null

    private lateinit var remoteMediaStream: MediaStream

    private var remoteSurfaceViewRenderer: SurfaceViewRenderer? = null

    private var webRTCClientListener: WebRTCClientListener? = null

    private var bluetoothStateManager: BluetoothStateManager? = null

    private var callTime: Long = 0

    private var mediaPlayerHasStopped: Boolean = false

    private var isFirstTimeBluetoothAvailable: Boolean = false

    private var isBluetoothAvailable: Boolean = false

    private var isHeadsetConnected: Boolean = false

    private var isBluetoothStopped: Boolean = false

    private var isReturnCall: Boolean = false

    private var textViewTimer: TextView? = null

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun reInit() {

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_NORMAL

        audioManager.stopBluetoothSco()

        audioManager.isBluetoothScoOn = false

        audioManager.isSpeakerphoneOn = false

        wakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                WebRTCClientImp::class.simpleName
            )

        iceCandidatesCaller = mutableListOf()

        callTime = 0

        isActiveCall = false
        isHideVideo = false
        contactCameraIsVisible = false
        isMicOn = true
        isBluetoothActive = false
        mediaPlayerHasStopped = false
        renegotiateCall = false
        isFirstTimeBluetoothAvailable = false
        isBluetoothAvailable = false
        isHeadsetConnected = false
        isBluetoothStopped = false
        isReturnCall = false

        textViewTimer = null

        bluetoothStateManager = null

        webRTCClientListener = null

        mediaConstraints = null

        remoteSurfaceViewRenderer = null

        if (::remoteMediaStream.isInitialized)
            try {
                remoteMediaStream.dispose()
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }

        localSurfaceViewRenderer = null

        localAudioTrack = null

        localVideoTrack = null

        localVideoSource = null

        if (::localMediaStream.isInitialized)
            try {
                localMediaStream.dispose()
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }

        videoCapturerAndroid?.dispose()

        videoCapturerAndroid = null

        peerConnection = null
    }

    init {
        reInit()
        subscribeToRXEvents()
        socketClient.setSocketEventListener(this)
    }

    private fun subscribeToRXEvents() {
        val disposableHeadsetState = RxBus.listen(RxEvent.HeadsetState::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

                when (it.state) {

                    Constants.HeadsetState.PLUGGED.state -> {
                        Timber.d("Headset plugged")
                        stopProximitySensor()
                        isHeadsetConnected = true
                        if (callModel.isVideoCall) {
                            if (isBluetoothAvailable.not()) {
                                audioManager.isSpeakerphoneOn = false
                            }
                        } else {
                            if (audioManager.isSpeakerphoneOn) {
                                audioManager.isSpeakerphoneOn = false
                                webRTCClientListener?.toggleCheckedSpeaker(false)
                            }
                        }
                    }

                    Constants.HeadsetState.UNPLUGGED.state -> {
                        isHeadsetConnected = false
                        Timber.d("Headset unplugged")

                        if (callModel.isVideoCall) {
                            if (isBluetoothAvailable) {
                                audioManager.isSpeakerphoneOn = false
                                startProximitySensor()
                            } else {
                                audioManager.isSpeakerphoneOn = true
                            }

                        } else if (isSpeakerOn().not()) {
                            startProximitySensor()
                        }
                    }

                }
            }

        val disposableHangupByNotification =
            RxBus.listen(RxEvent.HangupByNotification::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("LLAMADA PASO: COLGANDO DESDE NOTIFICACION")
                    if (it.channel == this.callModel.channelName)
                        webRTCClientListener?.hangUpFromNotification()
                }

        disposable.add(disposableHeadsetState)

        disposable.add(disposableHangupByNotification)
    }

    override fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener) {

        Timber.d("LLAMADA PASO 2: SETEANDO webRTCClientListener isActiveCall: $isActiveCall")

        this.webRTCClientListener = webRTCClientListener

        bluetoothStateManager = BluetoothStateManager(context, this)

        if (isActiveCall.not())
            createPeerConnection()

    }

    override fun connectSocket(mustSubscribeToPresenceChannel: Boolean, callModel: CallModel) {
        socketClient.connectSocket(
            mustSubscribeToPresenceChannel = true,
            callModel = callModel
        )
    }

    private fun createPeerConnection() {

        Timber.d("LLAMADA PASO 3: CREANDO PEERCONNECTION")

        try {

            if (peerConnection != null) {
                try {
                    Timber.d("LLAMADA PASO 3: NULLEANDO PEERCONNECTION")
                    peerConnection = null
                } catch (e: java.lang.Exception) {
                    Timber.d("LLAMADA PASO 3: ERROR")
                    Timber.e(e.localizedMessage)
                }
            }

            val rtcConfiguration = PeerConnection.RTCConfiguration(peerIceServer)

            rtcConfiguration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            rtcConfiguration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcConfiguration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            rtcConfiguration.continualGatheringPolicy =
                PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            //Usamos ECDSA encryption
            rtcConfiguration.keyType = PeerConnection.KeyType.ECDSA

            peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfiguration,
                object : CustomPeerConnectionObserver() {

                    override fun onRenegotiationNeeded() {
                        super.onRenegotiationNeeded()
                        Timber.d("LLAMADA PASO: onRenegotiationNeeded renegotiateCall: $renegotiateCall isReturnCall: $isReturnCall")
                        if ((renegotiateCall || isReturnCall) && callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
                            isReturnCall = false
                            renegotiateCall = false
                            Timber.d("LLAMADA PASO: onRenegotiationNeeded CREAR OFERTA")
                            createOffer()
                        }
                    }

                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        Timber.d("LLAMADA PASO: onIceCandidate $iceCandidate")
                        onIceCandidateReceived(iceCandidate)
                    }

                    override fun onAddTrack(
                        rtpReceiver: RtpReceiver,
                        mediaStreams: Array<MediaStream>
                    ) {
                        super.onAddTrack(rtpReceiver, mediaStreams)

                        Timber.d("LLAMADA PASO: onAddTrack RTPRECEIVERS $rtpReceiver")
                        Timber.d("LLAMADA PASO: onAddTrack MEDIASTREAMS $mediaStreams")

                        if (mediaStreams.isNotEmpty()) {

                            if (callModel.isVideoCall) {

                                Timber.d("LLAMADA PASO: onAddTrack callModel.isVideoCall")

                                remoteMediaStream = mediaStreams.first()

                                if (mediaStreams.first().videoTracks.isNotEmpty() && isActiveCall) {

                                    remoteMediaStream.videoTracks.first()
                                        ?.addSink(remoteSurfaceViewRenderer)

                                    peerConnection?.addStream(mediaStreams.first())

                                    renderRemoteVideo()

                                }
                            }
                        }
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {

                        super.onIceConnectionChange(iceConnectionState)

                        Timber.d("LLAMADA PASO: onIceConnectionChange $iceConnectionState")

                        when (iceConnectionState) {

                            PeerConnection.IceConnectionState.CHECKING -> webRTCClientListener?.showConnectingTitle()
                            PeerConnection.IceConnectionState.CONNECTED -> connectCall()
                            PeerConnection.IceConnectionState.FAILED -> {
                                disposeCall()
                            }
                            PeerConnection.IceConnectionState.NEW,
                            PeerConnection.IceConnectionState.COMPLETED,
                            PeerConnection.IceConnectionState.DISCONNECTED,
                            PeerConnection.IceConnectionState.CLOSED ->
                                Timber.d("IceConnectionState UNHANDLER $iceConnectionState")

                            else -> Timber.e("IceConnectionState Not Recognized")

                        }
                    }
                })

            createLocalAudioTrack()

            peerConnection?.addStream(localMediaStream)

        } catch (e: java.lang.Exception) {

            Timber.e("LLAMADA PASO ${e.localizedMessage}")

        }

    }

    private fun createOffer() {

        Timber.d("LLAMADA PASO 10: Creando Oferta")

        mediaConstraints = MediaConstraints()

        mediaConstraints?.mandatory?.apply {
            add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio",
                    "true"
                )
            )

            if (callModel.isVideoCall)
                add(
                    MediaConstraints.KeyValuePair(
                        "OfferToReceiveVideo",
                        "true"
                    )
                )
        }

        peerConnection?.createOffer(object : CustomSdpObserver("Local offer") {

            override fun onCreateSuccess(sessionDescription: SessionDescription) {

                super.onCreateSuccess(sessionDescription)

                Timber.d("LLAMADA PASO: OFERTA CREADA SETEANDOLA")

                peerConnection?.setLocalDescription(
                    (CustomSdpObserver("Local offer")),
                    sessionDescription
                )

                if (isActiveCall.not()) {
                    Timber.d("LLAMADA PASO 10.1: Llamada no activa consume api")
                    syncManager.callContact(
                        callModel.contactId,
                        callModel.isVideoCall,
                        sessionDescription.toJSONObject().toString()
                    )
                } else {
                    Timber.d("LLAMADA PASO 10.2: Emite llamar")
                    socketClient.emitClientCall(
                        channel = callModel.channelName,
                        jsonObject = sessionDescription.toJSONObject()
                    )
                }
            }

        }, mediaConstraints)
    }

    override fun subscribeToPresenceChannel() {
        Timber.d("LLAMADA PASO 4: SUSCRIBIRSE AL CANAL DE LLAMADAS")
        socketClient.subscribeToPresenceChannel(callModel)
    }

    override fun unSubscribePresenceChannel() {
        socketClient.unSubscribePresenceChannel(callModel.channelName)
    }

    override fun setOffer(offer: String?) {

        Timber.d("LLAMADA PASO 4: SETEANDO oferta")

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

    override fun createAnswer() {

        Timber.d("LLAMADA PASO 5: Creando Respuesta")

        peerConnection?.createAnswer(
            object : CustomSdpObserver("Local Answer") {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    peerConnection?.setLocalDescription(
                        CustomSdpObserver("Local Answer"),
                        sessionDescription
                    )
                    Timber.d("LLAMADA PASO: Emitiendo respuesta")
                    socketClient.emitClientCall(
                        channel = callModel.channelName,
                        jsonObject = sessionDescription.toJSONObject()
                    )
                }
            }, MediaConstraints()
        )
    }

    private fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        try {
            if (isActiveCall) {
                socketClient.emitClientCall(
                    channel = callModel.channelName,
                    jsonObject = iceCandidate.toJSONObject()
                )
            } else {
                if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                    socketClient.emitClientCall(
                        channel = callModel.channelName,
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

    override fun startWebRTCService(callModel: CallModel) {

        Timber.d("LLAMADA PASO: STARTWEBRTCSERVICE")

        callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL

        val intent = Intent(context, WebRTCService::class.java).apply {
            putExtras(Bundle().apply {
                putSerializable(Constants.CallKeys.CALL_MODEL, callModel)
            })
        }

        context.startService(intent)
    }

    //Change To VideoCall


    //Audio
    private fun createLocalAudioTrack() {

        val audioConstraints = MediaConstraints()

        val audioSource: AudioSource = peerConnectionFactory.createAudioSource(audioConstraints)

        localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack1", audioSource)

        localAudioTrack?.setEnabled(true)

        localMediaStream = peerConnectionFactory.createLocalMediaStream("localMediaStream")

        localMediaStream.addTrack(localAudioTrack)

    }

    //Video
    override fun initSurfaceRenders() {

        localSurfaceViewRenderer?.init(eglBase.eglBaseContext, null)
        localSurfaceViewRenderer?.setZOrderMediaOverlay(true)

        remoteSurfaceViewRenderer?.init(eglBase.eglBaseContext, null)
        remoteSurfaceViewRenderer?.setZOrderMediaOverlay(true)

        localSurfaceViewRenderer?.setMirror(true)

        remoteSurfaceViewRenderer?.setMirror(false)

        startCaptureVideo()

    }

    override fun startCaptureVideo() {

        Timber.d("createLocalVideoTrack")

        videoCapturerAndroid = createCameraCapturer(Camera2Enumerator(context))

        mediaConstraints = MediaConstraints()

        videoCapturerAndroid?.let { videoCapturer ->

            val surfaceTextureHelper: SurfaceTextureHelper =

                SurfaceTextureHelper.create(
                    "SurfaceTextureHelper",
                    eglBase.eglBaseContext
                )

            localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)

            videoCapturer.initialize(
                surfaceTextureHelper,
                context,
                localVideoSource?.capturerObserver
            )

            localVideoTrack =
                peerConnectionFactory.createVideoTrack("localVideoTrack1", localVideoSource)

            localMediaStream.addTrack(localVideoTrack)

            localVideoTrack?.addSink(localSurfaceViewRenderer)
        }

        videoCapturerAndroid?.startCapture(640, 480, 30)

    }

    override fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        localSurfaceViewRenderer = surfaceViewRenderer
        Timber.d("Aqui hago pausa")
    }

    override fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        remoteSurfaceViewRenderer = surfaceViewRenderer
        Timber.d("Aqui hago pausa")
    }

    override fun renderRemoteVideo() {

        try {

            stopProximitySensor()

            if (isBluetoothAvailable) {
                audioManager.isSpeakerphoneOn = false
            } else {
                audioManager.isSpeakerphoneOn = this.isHeadsetConnected.not()
            }

            remoteMediaStream.videoTracks.first()?.addSink(remoteSurfaceViewRenderer)

            webRTCClientListener?.showRemoteVideo()

        } catch (e: Exception) {
            Timber.d("NO Got Remote Stream")
            Timber.e(e)
        }

    }

    //Camera
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
            if (enumerator.isFrontFacing(deviceName).not()) {
                Timber.d("Creating other camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    override fun toggleVideo(checked: Boolean, itsFromBackPressed: Boolean) {

        if (callModel.isVideoCall) {

            isHideVideo = checked

            if (checked) {

                videoCapturerAndroid?.stopCapture()

                socketClient.emitClientCall(
                    callModel.channelName,
                    SocketClientImp.CONTACT_TURN_OFF_CAMERA
                )

                webRTCClientListener?.toggleLocalRenderVisibility(View.INVISIBLE)

            } else {

                videoCapturerAndroid?.startCapture(640,480,30)

                socketClient.emitClientCall(
                    callModel.channelName,
                    SocketClientImp.CONTACT_TURN_ON_CAMERA
                )

                webRTCClientListener?.toggleLocalRenderVisibility(View.VISIBLE)

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

    //Ringtone
    override fun playRingtone() {

        mediaPlayerHasStopped = false

        countDownRingCall.start()

        audioManager.isSpeakerphoneOn = (isBluetoothAvailable || isHeadsetConnected).not()

        Timber.d("*Test: ${audioManager.isSpeakerphoneOn}")

        Timber.d("RINGTONE: PlayRingtone")

        handlerMediaPlayerNotification.playRingtone()

    }

    override fun playRingBackTone() {

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (isBluetoothAvailable || isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.isSpeakerphoneOn = callModel.isVideoCall
        }

        countDownRingCall.start()

        Timber.d("RINGTONE: playRingBack EN WEBRTCCLIENT")
        handlerMediaPlayerNotification.playRingBack()

    }

    override fun stopRingAndVibrate() {
        handlerMediaPlayerNotification.stopRingtone()
    }

    //Proximity Sensor
    override fun startProximitySensor() {
        if (wakeLock.isHeld.not()) {
            wakeLock.acquire()
        }

    }

    override fun stopProximitySensor() {
        if (wakeLock.isHeld) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
        }
    }

    //Speaker
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

    override fun isSpeakerOn(): Boolean = audioManager.isSpeakerphoneOn

    //Microphone
    override fun setMicOff() {
        isMicOn = isMicOn.not()
        localAudioTrack?.setEnabled(isMicOn)
    }

    //Bluetooth
    override fun handleBluetooth(isEnabled: Boolean) {
        Timber.d("handleBluetooth: $isEnabled, ${callModel.isVideoCall}")
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
                    audioManager.mode = AudioManager.MODE_IN_CALL
                    stopProximitySensor()
                }
                else -> {
                    startProximitySensor()
                    audioManager.isSpeakerphoneOn = callModel.isVideoCall
                }
            }
        }
    }

    //Keydown
    override fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (isActiveCall.not()) {
                    handlerMediaPlayerNotification.stopRingtone()
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    //UI
    override fun setTextViewCallDuration(textView: TextView) {
        this.textViewTimer = textView
    }

    private fun startCallTimer() {
        textViewTimer?.text =
            Utils.getDuration(callTime, callTime >= TimeUnit.HOURS.toMillis(1))
        val oneSecond = TimeUnit.SECONDS.toMillis(1)
        callTime += oneSecond
        mHandler.postDelayed(mCallTimeRunnable, oneSecond)
    }

    override fun setItsReturnCall(itsReturnCall: Boolean) {
        if (callModel.isVideoCall)
            this.isReturnCall = itsReturnCall
    }

    //region Implementation BluetoothStateManager.BluetoothStateListener
    override fun onBluetoothStateChanged(isBluetoothAvailable: Boolean) {
        Timber.d("onBluetoothStateChanged: $isBluetoothAvailable")

        this.isBluetoothAvailable = isBluetoothAvailable

        if (isFirstTimeBluetoothAvailable.not() && isHeadsetConnected.not()) {
            Timber.d("isFirstTimeBluetoothAvailableeeee")
            isFirstTimeBluetoothAvailable = true
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
//            stopProximitySensor()
        }

        if (isBluetoothAvailable && callModel.isVideoCall && isBluetoothStopped) {
            Timber.d("onBluetoothStateChanged 2do")
            audioManager.isSpeakerphoneOn = true
        }

        if (isBluetoothAvailable && callModel.isVideoCall.not()) {
            stopProximitySensor()
        }

        if (isBluetoothAvailable.not() && isHeadsetConnected) {
            Timber.d("onBluetoothStateChanged 3ero")
            audioManager.isSpeakerphoneOn = false
        }
        webRTCClientListener?.toggleBluetoothButtonVisibility(isBluetoothAvailable)
    }
    //endregion

    //region Implementation SocketEventListener
    override fun itsSubscribedToPresenceChannelOutgoingCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO 7: ya Suscrito")

        if (callModel.channelName == this.callModel.channelName) {

            Timber.d("LLAMADA PASO: Crea Offer")

            createOffer()

        }
    }

    override fun itsSubscribedToPresenceChannelIncomingCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO: Inicia el servicio WebRTC desde itsSubscribedToPresenceChannelIncomingCall")

        Timber.d("LLAMADA PASO: onSuccessConnectPresenceChannel callModel: $callModel")

        startWebRTCService(callModel)

    }

    override fun iceCandidateReceived(channelName: String, iceCandidate: IceCandidate) {
        if (channelName == this.callModel.channelName)
            peerConnection?.addIceCandidate(iceCandidate)
    }

    override fun offerReceived(
        channelName: String,
        sessionDescription: SessionDescription
    ) {
        if (channelName == this.callModel.channelName) {
            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Remote offer"),
                sessionDescription
            )
            createAnswer()
        }
    }

    override fun answerReceived(
        channelName: String,
        sessionDescription: SessionDescription
    ) {
        if (channelName == this.callModel.channelName) {
            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Answer"),
                sessionDescription
            )

            iceCandidatesCaller.forEach { iceCandidate ->
                socketClient.emitClientCall(
                    channel = callModel.channelName,
                    jsonObject = iceCandidate.toJSONObject()
                )
            }

            iceCandidatesCaller.clear()

        }
    }

    override fun contactRejectCall(channelName: String) {
        webRTCClientListener?.changeTextviewTitle(R.string.text_contact_is_busy)
        countDownEndCallBusy.start()
        handlerMediaPlayerNotification.playBusyTone()
    }

    override fun contactCancelCall(channelName: String) {
        if (channelName == this.callModel.channelName) {
            try {
                audioManager.isSpeakerphoneOn = false
                audioManager.mode = AudioManager.MODE_NORMAL

                handlerMediaPlayerNotification.stopRingtone()

                unSubscribePresenceChannel()

                localAudioTrack?.setEnabled(false)
            } catch (e: Exception) {
                Timber.e("Error manejado, $e")
            } finally {
                disposeCall()
            }
        }
    }

    override fun contactWantChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName && NapoleonApplication.isShowingCallActivity)
            webRTCClientListener?.contactWantChangeToVideoCall()
        else {
            socketClient.emitClientCall(
                this.callModel.channelName,
                SocketClientImp.CONTACT_CANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun changeToVideoCall() {
        if (callModel.isVideoCall.not()) {
            socketClient.emitClientCall(
                callModel.channelName,
                SocketClientImp.CONTACT_WANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun meAcceptChangeToVideoCall() {

        callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL

        callModel.isVideoCall = true

        socketClient.emitClientCall(
            callModel.channelName,
            SocketClientImp.CONTACT_ACCEPT_CHANGE_TO_VIDEO
        )

        webRTCClientListener?.changeTextviewTitle(R.string.text_encrypted_video_call)

    }

    override fun meCancelChangeToVideoCall() {
        socketClient.emitClientCall(
            callModel.channelName,
            SocketClientImp.CONTACT_CANCEL_CHANGE_TO_VIDEO
        )
    }

    override fun contactAcceptChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName && callModel.isVideoCall.not()) {

            callModel.typeCall = Constants.TypeCall.IS_OUTGOING_CALL

            callModel.isVideoCall = true

            renegotiateCall = true

            webRTCClientListener?.contactAcceptChangeToVideoCall()
        }
    }

    override fun contactCancelChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName)
            webRTCClientListener?.contactCancelChangeToVideoCall()
    }

    override fun contactCantChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName)
            webRTCClientListener?.contactCantChangeToVideoCall()
    }

    override fun toggleContactCamera(channelName: String, contactCameraIsVisible: Boolean) {
        if (channelName == this.callModel.channelName) {

            this.contactCameraIsVisible = contactCameraIsVisible

            webRTCClientListener?.toggleContactCamera(if (contactCameraIsVisible.not()) View.VISIBLE else View.INVISIBLE)

        }
    }

    private fun connectCall() {

        Timber.d("LLAMADA PASO: LLAMADA CONECTADA")

        isActiveCall = true

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        countDownRingCall.cancel()

        webRTCClientListener?.enableControls()

        mHandler.postDelayed(
            mCallTimeRunnable,
            TimeUnit.SECONDS.toMillis(1)
        )

        handlerNotification.notificationCallInProgress(callModel)

        stopRingAndVibrate()

        startProximitySensor()

        if (callModel.isVideoCall.not() && callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
            audioManager.isSpeakerphoneOn = false
            webRTCClientListener?.toggleCheckedSpeaker(false)
        }

        if (callModel.isVideoCall) {
            renderRemoteVideo()
        }

        if ((callModel.isVideoCall.not() && isBluetoothActive) || isHeadsetConnected) {
            stopProximitySensor()
        }
    }

    override fun emitHangUp() {
        Timber.d("LLAMADA PASO: emitir hangup")
        socketClient.emitClientCall(
            callModel.channelName,
            SocketClientImp.HANGUP_CALL
        )
    }

    override fun contactHasHangup(channelName: String) {
        if (channelName == this.callModel.channelName) {
            Timber.d("LLAMADA PASO: COLGAR LLAMADA DESDE ESCUCHADOR")
            disposeCall()
        }
    }

    override fun disposeCall() {

        try {

            Timber.d("LLAMADA PASO: DISPOSE CALL")

            Timber.d("LLAMADA PASO: END CALL TONE")

            handlerMediaPlayerNotification.stopRingtone()

            handlerMediaPlayerNotification.playEndTone()

            countDownEndCallBusy.cancel()

            countDownRingCall.cancel()

            bluetoothStateManager?.onDestroy()

            isActiveCall = false

            RxBus.publish(RxEvent.CallEnd())

            Timber.d("LLAMADA PASO: PIDE ENVIAR A SERVICIO CALL END")

            val intent = Intent(context, WebRTCService::class.java)

            intent.action = WebRTCService.ACTION_CALL_END

            intent.putExtras(Bundle().apply {
                putSerializable(Constants.CallKeys.CALL_MODEL, callModel)
            })

            context.startService(intent)

            Timber.d("LLAMADA PASO: DESSUSCRIBIR A CANAL")
            socketClient.unSubscribePresenceChannel(callModel.channelName)

            stopProximitySensor()

            mHandler.removeCallbacks(mCallTimeRunnable)

            Timber.d("LLAMADA PASO: CIERRA LA VISTA DE LLAMADA")
            webRTCClientListener?.callEnded()

            peerConnection?.close()

        } catch (e: java.lang.Exception) {
            Timber.e(e.localizedMessage)
        } finally {
            reInit()
        }

    }

    //endregion

}