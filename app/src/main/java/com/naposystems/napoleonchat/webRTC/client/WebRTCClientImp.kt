package com.naposystems.napoleonchat.webRTC.client

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.socketClient.EventsFromSocketClientListener
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.socketClient.SocketClientImp
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.adapters.toJSONObject
import com.naposystems.napoleonchat.utility.adapters.toSessionDescription
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import com.naposystems.napoleonchat.webRTC.CustomPeerConnectionObserver
import com.naposystems.napoleonchat.webRTC.CustomSdpObserver
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    EventsFromSocketClientListener,
    BluetoothStateManager.BluetoothStateListener {

    //region Atributos Sobreescritos
    private var disposingCall = false

    override var renegotiateCall: Boolean = false

    override var isHideVideo: Boolean = false

    override var contactCameraIsVisible: Boolean = false

    override var isMicOn: Boolean = true

    override var isBluetoothActive: Boolean = false
    //endregion

    //region Atributos Privados
    private var callTimerHandler: Handler = Handler(Looper.getMainLooper())

    private var callTimerRunnable = Runnable { startCallTimer() }

    //Tiempo de Repique
    private var countDownRingCall: CountDownTimer = object : CountDownTimer(
        TimeUnit.SECONDS.toMillis(30),
        TimeUnit.SECONDS.toMillis(1)
    ) {
        override fun onFinish() {
            Timber.d("LLAMADA PASO: COUNTDOWN RING")

            if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
                playEndCall(cancelCall = true)
            } else {
                playEndCall()
            }
        }

        override fun onTick(millisUntilFinished: Long) = Unit
    }

    //Tiempo de Sonido de Ocupado
    private var countDownEndCallBusy: CountDownTimer = object : CountDownTimer(
        TimeUnit.SECONDS.toMillis(2),
        TimeUnit.SECONDS.toMillis(1)
    ) {
        override fun onFinish() {
            disposeCall()
        }

        override fun onTick(millisUntilFinished: Long) = Unit
    }

    //Tiempo de Reconexion
    private var countDownReconnecting: CountDownTimer = object : CountDownTimer(
        TimeUnit.SECONDS.toMillis(15),
        TimeUnit.SECONDS.toMillis(1)
    ) {
        override fun onFinish() {
            Timber.d("LLAMADA PASO: COUNTDOWN RECONNECTING FINISH")
            playEndCall()
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

    private var eventFromWebRtcClientListener: EventFromWebRtcClientListener? = null

    private var bluetoothStateManager: BluetoothStateManager? = null

    private var callTime: Long = 0

    val oneSecond = TimeUnit.SECONDS.toMillis(1)

    private var mediaPlayerHasStopped: Boolean = false

    private var isFirstTimeBluetoothAvailable: Boolean = false

    private var isBluetoothAvailable: Boolean = false

    private var isHeadsetConnected: Boolean = false

    private var isBluetoothStopped: Boolean = false

    private var textViewTimer: TextView? = null

    private lateinit var wakeLock: PowerManager.WakeLock

    //endregion

    init {
        Timber.d("LLAMADA PASO: EN WEBRTCCLIENT")
        reInit()
        subscribeToRXEvents()
        socketClient.setEventsFromSocketClientListener(this)
    }

    //region Implementation WebRtcClientListener
    override fun reInit() {

        try {

            NapoleonApplication.statusCall = StatusCallEnum.STATUS_NO_CALL

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

            try {
                iceCandidatesCaller.clear()
            } catch (e: Exception) {
                Timber.e("iceCandidatesCaller")
            }
            callTime = 0
//            isActiveCall = false
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

            textViewTimer = null

            bluetoothStateManager = null

            eventFromWebRtcClientListener = null

            mediaConstraints = null

            remoteSurfaceViewRenderer = null

            try {
                if (::remoteMediaStream.isInitialized)
                    remoteMediaStream.dispose()
            } catch (e: java.lang.Exception) {
                Timber.e("LLAMADA PASO: REINIT remoteMediaStream ${e.localizedMessage}")
            }

            localSurfaceViewRenderer = null

            localAudioTrack = null

            localVideoTrack = null

            localVideoSource = null

            try {
                if (::localMediaStream.isInitialized)
                    localMediaStream.dispose()
            } catch (e: java.lang.Exception) {
                Timber.e("LLAMADA PASO: REINIT localMediaStream ${e.localizedMessage}")
            }

            videoCapturerAndroid?.dispose()

            videoCapturerAndroid = null

            disposingCall = false

            try {
                peerConnection = null
            } catch (e: Exception) {
                Timber.e("LLAMADA PASO: INTENTANDO NULEAR")
            }

            Timber.d("LLAMADA PASO: FINALIZANDO REINIT")

        } catch (e: Exception) {
            Timber.e("LLAMADA PASO: REINIT ${e.localizedMessage}")
        }
    }

    override fun setEventsFromWebRTCClientListener(eventFromWebRtcClientListener: EventFromWebRtcClientListener) {

        Timber.d("LLAMADA PASO 2: SETEANDO webRTCClientListener NapoleonApplication.statusCall.isNoCall(): ${NapoleonApplication.statusCall.isNoCall()}")

        this.eventFromWebRtcClientListener = eventFromWebRtcClientListener

        bluetoothStateManager = BluetoothStateManager(context, this)

        if (NapoleonApplication.statusCall.isNoCall())
            createPeerConnection()

    }

    override fun connectSocket() {

        Timber.d("LLAMADA PASO 3: CONECTAR SOCKET")

        GlobalScope.launch {
            socketClient.connectSocket()
        }
    }

    override suspend fun subscribeToPresenceChannel() {
        NapoleonApplication.callModel?.let {
            if (it.mustSubscribeToPresenceChannel && it.channelName != "" && NapoleonApplication.statusCall.isNoCall()) {
                Timber.d("LLAMADA PASO 4: SUSCRIBIRSE AL CANAL DE LLAMADAS")
                socketClient.subscribeToPresenceChannel()
            }
        }
    }

    override fun setOffer() {

        Timber.d("LLAMADA PASO 4: SETEANDO oferta")

        NapoleonApplication.callModel?.offer?.let {
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

        Timber.d("LLAMADA PASO 5: ${peerConnection?.connectionState()}")

        peerConnection?.createAnswer(
            object : CustomSdpObserver("Local Answer") {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    peerConnection?.setLocalDescription(
                        CustomSdpObserver("Local Answer"),
                        sessionDescription
                    )
                    Timber.d("LLAMADA PASO: Emitiendo respuesta")

                    NapoleonApplication.callModel?.channelName?.let {
                        socketClient.emitClientCall(
                            jsonObject = sessionDescription.toJSONObject()
                        )
                    }
                }
            }, MediaConstraints()
        )
    }

    override fun startWebRTCService() {

        Timber.d("LLAMADA PASO: STARTWEBRTCSERVICE")

        context.startService(Intent(context, WebRTCService::class.java))
    }

    //Change to video Call
    override fun changeToVideoCall() {
        if (NapoleonApplication.callModel?.isVideoCall == false) {
            socketClient.emitClientCall(
                SocketClientImp.CONTACT_WANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun meAcceptChangeToVideoCall() {
        NapoleonApplication.callModel?.typeCall = Constants.TypeCall.IS_INCOMING_CALL
        NapoleonApplication.callModel?.isVideoCall = true
        socketClient.emitClientCall(
            SocketClientImp.CONTACT_ACCEPT_CHANGE_TO_VIDEO
        )
        eventFromWebRtcClientListener?.showTypeCallTitle()
    }

    override fun meCancelChangeToVideoCall() {
        socketClient.emitClientCall(
            SocketClientImp.CONTACT_CANCEL_CHANGE_TO_VIDEO
        )
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

        videoCapturerAndroid?.startCapture(640, 480, 15)

    }

    override fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        localSurfaceViewRenderer = surfaceViewRenderer
    }

    override fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
        remoteSurfaceViewRenderer = surfaceViewRenderer
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

        } catch (e: Exception) {
            Timber.d("NO Got Remote Stream")
            Timber.e(e)
        }
    }

    //Camera
    //previousState es el estado anterior de la vista, tener cuidado con eso
    override fun toggleVideo(previousState: Boolean, itsFromBackPressed: Boolean) {
        if (NapoleonApplication.callModel?.isVideoCall == true) {

            if (previousState) {
                videoCapturerAndroid?.stopCapture()
                NapoleonApplication.callModel?.channelName?.let {
                    socketClient.emitClientCall(
                        SocketClientImp.CONTACT_TURN_OFF_CAMERA
                    )
                }
                eventFromWebRtcClientListener?.toggleLocalRenderVisibility(visibility = true)
            } else {
                NapoleonApplication.callModel?.channelName?.let {
                    socketClient.emitClientCall(
                        SocketClientImp.CONTACT_TURN_ON_CAMERA
                    )
                }
                eventFromWebRtcClientListener?.toggleLocalRenderVisibility(visibility = false)
            }

            if (itsFromBackPressed && previousState) {
                localMediaStream.removeTrack(localVideoTrack)
            }

            isHideVideo = if (itsFromBackPressed) false else previousState

        }
    }

    override fun switchCamera() {
        val cameraVideoCapturer = videoCapturerAndroid as CameraVideoCapturer
        cameraVideoCapturer.switchCamera(null)
    }

    //Ringtone
    override fun playRingTone() {

        mediaPlayerHasStopped = false

        countDownRingCall.start()

        audioManager.isSpeakerphoneOn = (isBluetoothAvailable || isHeadsetConnected).not()

        Timber.d("*Test: ${audioManager.isSpeakerphoneOn}")

        Timber.d("RINGTONE: PlayRingtone")

        handlerMediaPlayerNotification.playRingTone()

    }

    override fun playEndCall(cancelCall: Boolean) {
        try {
            eventFromWebRtcClientListener?.showFinishingCall()
            MediaPlayer().apply {
                setDataSource(
                    context,
                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.end_call_tone_new)
                )
                this.isLooping = false
                setOnCompletionListener { if (cancelCall) cancelCall() else disposeCall() }
                prepare()
                start()
            }

        } catch (ex: Exception) {
            Timber.e(ex.message.toString())
            if (cancelCall)
                cancelCall()
            else
                disposeCall()
        }
    }

    override fun playBackTone() {

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (isBluetoothAvailable || isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.isSpeakerphoneOn = NapoleonApplication.callModel?.isVideoCall == true
        }

        countDownRingCall.start()

        Timber.d("RINGTONE: playRingBack EN WEBRTCCLIENT")
        handlerMediaPlayerNotification.playBackTone()

    }

    override fun stopRingAndVibrate() {
        handlerMediaPlayerNotification.stopTone()
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
        Timber.d("handleBluetooth: $isEnabled, ${NapoleonApplication.callModel?.isVideoCall}")
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
                    audioManager.isSpeakerphoneOn =
                        NapoleonApplication.callModel?.isVideoCall == true
                }
            }
        }
    }

    //Keydown
    override fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (NapoleonApplication.statusCall.isNoCall()) {
                    handlerMediaPlayerNotification.stopTone()
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

    override fun emitHangUp() {
        Timber.d("LLAMADA PASO: emitir hangup")
        socketClient.emitClientCall(
            SocketClientImp.HANGUP_CALL
        )
    }

    override fun hideNotification() {

        val intent = Intent(context, WebRTCService::class.java)

        intent.action = WebRTCService.ACTION_HIDE_NOTIFICATION

        context.startService(intent)
    }

    override fun disposeCall(typeEndCall: TypeEndCallEnum?) {

        Timber.d("LLAMADA PASO: DISPOSE CALL")

        try {

            if (disposingCall.not()) {

                disposingCall = true

                typeEndCall?.let {
                    when (it) {
                        TypeEndCallEnum.TYPE_CANCEL -> {
                            syncManager.cancelCall()
                        }
                        TypeEndCallEnum.TYPE_REJECT -> {
                            syncManager.rejectCall()
                        }
                    }
                }

                NapoleonApplication.callModel?.let { callModel ->
                    if (callModel.isFromClosedApp == Constants.FromClosedApp.YES) {
                        Timber.d("LLAMADA PASO 3: DISCONNECT SOCKET DISPOSE CALL")
                        socketClient.disconnectSocket()
                    } else {
                        Timber.d("LLAMADA PASO 3: unsubscribe presence DISPOSE CALL")
                        callModel.channelName.let {
                            socketClient.unSubscribePresenceChannel()
                        }
                    }

                }

                NapoleonApplication.callModel = null

                NapoleonApplication.statusCall = StatusCallEnum.STATUS_NO_CALL

                processDisposeCall()

            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }


    }

    override fun cancelCall() {
        NapoleonApplication.callModel?.let {
            Timber.e("LLAMADA PASO: CONTACT CANCEL CALL")
            syncManager.cancelCall()
            syncManager.sendMissedCall()
            disposeCall()
        }
    }

    override fun contactRejectCall() {
        NapoleonApplication.callModel?.channelName?.let {
            eventFromWebRtcClientListener?.showOccupiedCall()
            countDownEndCallBusy.start()
            handlerMediaPlayerNotification.playBusyTone()
            syncManager.sendMissedCall()
        }
    }

    override fun contactCancelCall() {
        NapoleonApplication.callModel?.let {
            Timber.e("LLAMADA PASO: CONTACT CANCEL CALL")
            playEndCall()
        }
    }

//endregion

//region Metodos privados

    private fun subscribeToRXEvents() {
        val disposableHeadsetState = RxBus.listen(RxEvent.HeadsetState::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

                when (it.state) {
                    Constants.HeadsetState.PLUGGED.state -> handlerHeadsetPlugged()
                    Constants.HeadsetState.UNPLUGGED.state -> handlerHeadsetUnplugged()
                }
            }

        val disposableHangupByNotification =
            RxBus.listen(RxEvent.HangupByNotification::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("LLAMADA PASO: COLGANDO DESDE NOTIFICACION")
                    NapoleonApplication.callModel?.let {
                        eventFromWebRtcClientListener?.hangUpFromNotification()
                    }
                }

        disposable.add(disposableHeadsetState)

        disposable.add(disposableHangupByNotification)
    }

    private fun handlerHeadsetPlugged() {
        Timber.d("Headset plugged")
        stopProximitySensor()
        isHeadsetConnected = true
        if (NapoleonApplication.callModel?.isVideoCall == true) {
            if (isBluetoothAvailable.not()) {
                audioManager.isSpeakerphoneOn = false
            }
        } else {
            if (audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = false
                eventFromWebRtcClientListener?.toggleCheckedSpeaker(false)
            }
        }
    }

    private fun handlerHeadsetUnplugged() {
        isHeadsetConnected = false
        Timber.d("Headset unplugged")

        if (NapoleonApplication.callModel?.isVideoCall == true) {
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
                        Timber.d("LLAMADA PASO: onRenegotiationNeeded renegotiateCall: $renegotiateCall")
                        if (renegotiateCall && NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_OUTGOING_CALL
                        ) {
                            renegotiateCall = false
                            Timber.d("LLAMADA PASO: onRenegotiationNeeded CREAR OFERTA")
                            createOffer()
                        }
                    }

                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        Timber.d("LLAMADA PASO: GENERACION ICECANDIDATE $iceCandidate")
                        onIceCandidateGenerated(iceCandidate)
                    }

                    override fun onAddTrack(
                        rtpReceiver: RtpReceiver,
                        mediaStreams: Array<MediaStream>
                    ) {
                        super.onAddTrack(rtpReceiver, mediaStreams)

                        Timber.d("LLAMADA PASO: onAddTrack RTPRECEIVERS $rtpReceiver")
                        Timber.d("LLAMADA PASO: onAddTrack MEDIASTREAMS $mediaStreams")

                        if (mediaStreams.isNotEmpty() && NapoleonApplication.callModel?.isVideoCall == true) {

                            Timber.d("LLAMADA PASO: onAddTrack NapoleonApplication.callModel?.isVideoCall")

                            remoteMediaStream = mediaStreams.first()

                            if (mediaStreams.first().videoTracks.isNotEmpty() && NapoleonApplication.statusCall.isConnectedCall()) {

                                remoteMediaStream.videoTracks.first()
                                    ?.addSink(remoteSurfaceViewRenderer)

                                peerConnection?.addStream(mediaStreams.first())

                                renderRemoteVideo()

                            }
                        }
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {

                        super.onIceConnectionChange(iceConnectionState)

                        when (iceConnectionState) {

                            PeerConnection.IceConnectionState.CHECKING -> {
                                eventFromWebRtcClientListener?.showCypheryngCall()
                            }

                            PeerConnection.IceConnectionState.CONNECTED -> {
                                eventFromWebRtcClientListener?.showTimer()
                                connectCall()
                            }

                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                eventFromWebRtcClientListener?.showReConnectingCall()
                                countDownReconnecting.start()
                            }

                            else ->
                                Timber.d("IceConnectionState UNHANDLER $iceConnectionState")
                        }
                    }


                    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                        super.onSignalingChange(signalingState)

                        when (signalingState) {
                            PeerConnection.SignalingState.CLOSED -> {
                                peerConnection = null
                            }
                            else -> {
                                Timber.d("SignalingState UNHANDLER $signalingState")
                            }

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

            if (NapoleonApplication.callModel?.isVideoCall == true)
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
                if (NapoleonApplication.statusCall.isNoCall()) {
                    Timber.d("LLAMADA PASO 10.1: Llamada no activa consume api")

                    NapoleonApplication.callModel?.let {
                        it.offer = sessionDescription.toJSONObject().toString()
                    }

                    syncManager.callContact()

                } else {
                    Timber.d("LLAMADA PASO 10.2: Emite llamar")

                    NapoleonApplication.callModel?.channelName?.let {
                        socketClient.emitClientCall(
                            jsonObject = sessionDescription.toJSONObject()
                        )
                    }
                }
            }

        }, mediaConstraints)
    }

    private fun onIceCandidateGenerated(iceCandidate: IceCandidate) {
        try {
            if (NapoleonApplication.statusCall.isConnectedCall()) {
                NapoleonApplication.callModel?.channelName?.let {
                    socketClient.emitClientCall(
                        jsonObject = iceCandidate.toJSONObject()
                    )
                }
            } else {
                if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                    NapoleonApplication.callModel?.channelName?.let {
                        socketClient.emitClientCall(
                            jsonObject = iceCandidate.toJSONObject()
                        )
                    }
                } else {
                    iceCandidatesCaller.add(iceCandidate)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    //Audio
    private fun createLocalAudioTrack() {

        val audioConstraints = MediaConstraints()

        val audioSource: AudioSource = peerConnectionFactory.createAudioSource(audioConstraints)

        localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack1", audioSource)

        localAudioTrack?.setEnabled(true)

        localMediaStream = peerConnectionFactory.createLocalMediaStream("localMediaStream")

        localMediaStream.addTrack(localAudioTrack)

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

    private fun startCallTimer() {
        textViewTimer?.text =
            Utils.getDuration(callTime, callTime >= TimeUnit.HOURS.toMillis(1))
        callTime += oneSecond
        callTimerHandler.postDelayed(callTimerRunnable, oneSecond)
    }

    private fun connectCall() {

        Timber.d("LLAMADA PASO: LLAMADA CONECTADA")

        NapoleonApplication.statusCall = StatusCallEnum.STATUS_CONNECTED_CALL

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        countDownRingCall.cancel()

        countDownReconnecting.cancel()

        eventFromWebRtcClientListener?.enableControls()

        eventFromWebRtcClientListener?.handlerActiveCall()

        if (callTime == 0L)
            callTimerHandler.postDelayed(
                callTimerRunnable,
                TimeUnit.SECONDS.toMillis(1)
            )

        handlerNotification.notificationCallInProgress()

        stopRingAndVibrate()

        startProximitySensor()

        if (NapoleonApplication.callModel?.isVideoCall == false && NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
            audioManager.isSpeakerphoneOn = false
            eventFromWebRtcClientListener?.toggleCheckedSpeaker(false)
        }

        if (NapoleonApplication.callModel?.isVideoCall == true) {
            renderRemoteVideo()
        }

        if ((NapoleonApplication.callModel?.isVideoCall == false && isBluetoothActive) || isHeadsetConnected) {
            stopProximitySensor()
        }
    }

//endregion

    //region Implementation EventsFromSocketClientListener
    override fun itsSubscribedToPresenceChannelOutgoingCall() {

        Timber.d("LLAMADA PASO 7: ya Suscrito")

        NapoleonApplication.callModel?.channelName.let {

            Timber.d("LLAMADA PASO: Crea Offer")

            createOffer()

            startWebRTCService()
        }
    }

    override fun itsSubscribedToPresenceChannelIncomingCall() {

        Timber.d("LLAMADA PASO: Inicia el servicio WebRTC desde itsSubscribedToPresenceChannelIncomingCall")

        NapoleonApplication.callModel?.let {
            startWebRTCService()
        }
    }

    //Connection
    override fun iceCandidateReceived(iceCandidate: IceCandidate) {
        NapoleonApplication.callModel?.channelName.let {
            Timber.d("LLAMADA PASO: AGREGO ICECANDIDATE $iceCandidate")
            peerConnection?.addIceCandidate(iceCandidate)
        }
    }

    override fun offerReceived(sessionDescription: SessionDescription) {
        NapoleonApplication.callModel?.channelName.let {
            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Remote offer"),
                sessionDescription
            )
            GlobalScope.launch {
                createAnswer()
            }
        }
    }

    override fun answerReceived(sessionDescription: SessionDescription) {
        NapoleonApplication.callModel?.channelName?.let {
            Timber.d("LLAMADA PASO: SETEO RESPUESTA")

            peerConnection?.setRemoteDescription(
                CustomSdpObserver("Answer"),
                sessionDescription
            )

            iceCandidatesCaller.forEach { iceCandidate ->
                Timber.d("LLAMADA PASO: EMITO ICECANDIDATE")
                socketClient.emitClientCall(
                    iceCandidate.toJSONObject()
                )
            }
            Timber.d("LLAMADA PASO: VACIO ICECANDIDATE")
            iceCandidatesCaller.clear()

        }
    }

    //Handler Call
    override fun listenerRejectCall() {
        contactRejectCall()
    }

    override fun listenerCancelCall() {
        contactCancelCall()
    }

    //Videocall
    override fun contactWantChangeToVideoCall() {
        NapoleonApplication.callModel?.let { callModel ->
            if (callModel.channelName != "" &&
                NapoleonApplication.isShowingCallActivity
            )
                eventFromWebRtcClientListener?.contactWantChangeToVideoCall()
            else {
                socketClient.emitClientCall(
                    SocketClientImp.CONTACT_CANT_CHANGE_TO_VIDEO
                )
            }
        }
    }

    override fun contactAcceptChangeToVideoCall() {
        NapoleonApplication.callModel.let { callModel ->
            if (callModel?.channelName != "" && callModel?.isVideoCall == false) {
                NapoleonApplication.callModel?.typeCall = Constants.TypeCall.IS_OUTGOING_CALL
                NapoleonApplication.callModel?.isVideoCall = true
                renegotiateCall = true
                eventFromWebRtcClientListener?.contactAcceptChangeToVideoCall()
            }
        }
    }

    override fun contactCancelChangeToVideoCall() {
        NapoleonApplication.callModel.let { callModel ->
            if (callModel?.channelName != "")
                eventFromWebRtcClientListener?.contactCancelChangeToVideoCall()
        }
    }

    override fun contactCantChangeToVideoCall() {
        NapoleonApplication.callModel.let { callModel ->
            if (callModel?.channelName != "")
                eventFromWebRtcClientListener?.contactCantChangeToVideoCall()

        }
    }

    //Turn ON/OFF Camera
    override fun toggleContactCamera(isVisible: Boolean) {
        NapoleonApplication.callModel.let { callModel ->
            if (callModel?.channelName != "") {
                this.contactCameraIsVisible = isVisible
                if (isVisible.not()) {
                    eventFromWebRtcClientListener?.toggleContactCamera(View.VISIBLE)
                } else {
                    eventFromWebRtcClientListener?.toggleContactCamera(View.INVISIBLE)
                    initSurfaceRenders()
                }
            }
        }
    }

    override fun contactHasHangup() {
        NapoleonApplication.callModel.let {
            Timber.d("LLAMADA PASO: CONTACT HAS HANGUP")
            playEndCall()
        }
    }

    override fun processDisposeCall() {

        try {

            hideNotification()

            RxBus.publish(RxEvent.CallEnd())

            Timber.d("LLAMADA PASO: PROCESS DISPOSE CALL")

            countDownEndCallBusy.cancel()

            countDownRingCall.cancel()

            countDownReconnecting.cancel()

            bluetoothStateManager?.onDestroy()

            stopProximitySensor()

            callTimerHandler.removeCallbacks(callTimerRunnable)

            Timber.d("LLAMADA PASO: CIERRA LA VISTA DE LLAMADA")
            eventFromWebRtcClientListener?.callEnded()

            peerConnection?.close()

        } catch (e: java.lang.Exception) {
            Timber.e(e.localizedMessage)
        } finally {
            Timber.d("LLAMADA PASO: RE INIT DESDE DISPOSECALL")
            reInit()
        }

    }

//endregion

    //region Implementation BluetoothStateManager.BluetoothStateListener
    override fun onBluetoothStateChanged(available: Boolean) {
        Timber.d("onBluetoothStateChanged: $available")

        this.isBluetoothAvailable = available

        if (isFirstTimeBluetoothAvailable.not() && isHeadsetConnected.not()) {
            Timber.d("isFirstTimeBluetoothAvailableeeee")
            isFirstTimeBluetoothAvailable = true
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
        }

        if (available && NapoleonApplication.callModel?.isVideoCall == true && isBluetoothStopped) {
            Timber.d("onBluetoothStateChanged 2do")
            audioManager.isSpeakerphoneOn = true
        }

        if (available && NapoleonApplication.callModel?.isVideoCall == false) {
            stopProximitySensor()
        }

        if (available.not() && isHeadsetConnected) {
            Timber.d("onBluetoothStateChanged 3ero")
            audioManager.isSpeakerphoneOn = false
        }
        eventFromWebRtcClientListener?.toggleBluetoothButtonVisibility(available)
    }
//endregion

}