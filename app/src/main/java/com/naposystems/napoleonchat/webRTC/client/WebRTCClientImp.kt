package com.naposystems.napoleonchat.webRTC.client

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.MODE_IN_CALL
import android.media.AudioManager.MODE_IN_COMMUNICATION
import android.media.MediaPlayer
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.socketClient.SocketClientImp
import com.naposystems.napoleonchat.service.socketClient.SocketEventsListener
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
    private val handlerMediaPlayerNotification: HandlerMediaPlayerNotification,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val eglBase: EglBase,
    private val rtcConfiguration: PeerConnection.RTCConfiguration
) : WebRTCClient,
    SocketEventsListener.Call,
    BluetoothStateManager.BluetoothStateListener {

    //region Atributos
    override var callModel = CallModel(
        contactId = 0,
        isVideoCall = false,
        typeCall = Constants.TypeCall.IS_OUTGOING_CALL,
        channelName = "",
        offer = ""
    )

    override var isActiveCall: Boolean = false
    //endregion

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val mCallTimeRunnable: Runnable = Runnable { startCallTimer() }

    //Tiempo de Repique
    private val countDownTime = TimeUnit.MINUTES.toMillis(30)

    private var countDownEndCall: CountDownTimer =
        object : CountDownTimer(countDownTime, TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                Timber.d("CountDown finish")
                if (isActiveCall.not()) {
                    webRTCClientListener?.contactNotAnswer()
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownEndCallBusy: CountDownTimer =
        object : CountDownTimer(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                if (isActiveCall.not()) {
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    private var countDownIncomingCall: CountDownTimer =
        object : CountDownTimer(countDownTime, TimeUnit.SECONDS.toMillis(1)) {
            override fun onFinish() {
                Timber.d("CountDown finish")
                if (isActiveCall.not()) {
                    webRTCClientListener?.contactNotAnswer()
                    disposeCall()
                }
            }

            override fun onTick(millisUntilFinished: Long) = Unit
        }

    //TODO: Revisar el AudioAttribute
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

    private var textViewTimer: TextView? = null

    private val wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            WebRTCClientImp::class.simpleName
        )

    init {
        subscribeToRXEvents()
        socketClient.setSocketCallListener(this)
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
                                webRTCClientListener?.changeCheckedSpeaker(false)
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
                    Timber.d("HangupByNotification")
                    if (it.channel == this.callModel.channelName) {
                        webRTCClientListener?.hangupByNotification()
                    }
                }

        disposable.add(disposableHeadsetState)

        disposable.add(disposableHangupByNotification)
    }

    override fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener) {

        Timber.d("LLAMADA PASO 2: SETEANDO webRTCClientListener isActiveCall: $isActiveCall")

        this.webRTCClientListener = webRTCClientListener

        bluetoothStateManager = BluetoothStateManager(context, this)

        if (isActiveCall.not()) {
            createPeerConnection()
        }

    }

    private fun createPeerConnection() {

        Timber.d("LLAMADA PASO 3: CREANDO PEERCONNECTION")

        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfiguration,
            object : CustomPeerConnectionObserver() {

                override fun onRenegotiationNeeded() {
                    super.onRenegotiationNeeded()
                    Timber.d("LLAMADA PASO: onRenegotiationNeeded renegotiateCall: $renegotiateCall isReturnCall: $isReturnCall")
                    if (renegotiateCall || isReturnCall) {
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

                    Timber.d("LLAMADA PASO: onIceConnectionChange $iceConnectionState")

                    when (iceConnectionState) {

                        PeerConnection.IceConnectionState.CHECKING -> webRTCClientListener?.showConnectingTitle()
                        PeerConnection.IceConnectionState.CONNECTED -> connectCall()
                        PeerConnection.IceConnectionState.CLOSED,
                        PeerConnection.IceConnectionState.DISCONNECTED -> disposeCall()
//                        PeerConnection.IceConnectionState.CLOSED -> disposeCall()
                        else -> Timber.e("IceConnectionState Not Recognized")

                    }
                }
            })

        createLocalAudioTrack()

        addLocalAudioTrackToLocalMediaStream()

        if (callModel.isVideoCall)
            startCaptureVideo()

        peerConnection?.addStream(localMediaStream)
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

    private fun createLocalAudioTrack() {
        val audioConstraints = MediaConstraints()
        val audioSource: AudioSource =
            peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack("localAudioTrack1", audioSource)
        localAudioTrack?.setEnabled(true)
    }

    private fun createLocalVideoTrack() {
        Timber.d("createLocalVideoTrack")

        videoCapturerAndroid = createVideoCapturer()

        mediaConstraints = MediaConstraints()

        videoCapturerAndroid?.let { videoCapturer ->

            val surfaceTextureHelper: SurfaceTextureHelper =
                SurfaceTextureHelper.create(
                    "SurfaceTextureHelper",
                    eglBase.eglBaseContext
                )

            videoSource =
                peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)

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

    private fun addLocalVideoTrackToLocalMediaStream() {
        localMediaStream.addTrack(localVideoTrack)
    }

    private fun addLocalAudioTrackToLocalMediaStream() {
        localMediaStream =
            peerConnectionFactory.createLocalMediaStream("localMediaStream")
        localMediaStream.addTrack(localAudioTrack)
    }

    private fun connectCall() {

        Timber.d("LLAMADA PASO: LLAMADA CONECTADA")

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

        handlerNotification.notificationCallInProgress(callModel)

        stopRingAndVibrate()

        if (callModel.isVideoCall) {
            renderRemoteVideo(remoteMediaStream)
        } else {
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                audioManager.isSpeakerphoneOn = false
                webRTCClientListener?.changeCheckedSpeaker(false)
            }
            if (isBluetoothActive || isHeadsetConnected) {
                stopProximitySensor()
            }
        }
    }

    override fun disposeCall() {

        Timber.d("LLAMADA PASO: DISPOSE CALL")

        RxBus.publish(RxEvent.CallEnd())

        Timber.d("LLAMADA PASO: PIDE ENVIAR A SERVICIO CALL END")

        val intent = Intent(context, WebRTCService::class.java)

        intent.action = WebRTCService.ACTION_CALL_END

        context.startService(intent)


        Timber.d("LLAMADA PASO: END CALL TONE")

        handlerMediaPlayerNotification.stopRingtone()

        handlerMediaPlayerNotification.playEndTone()

        Timber.d("LLAMADA PASO: DESSUSCRIBIR A CANAL")
        socketClient.unSubscribePresenceChannel(this.callModel.channelName)

        audioManager.mode = AudioManager.MODE_NORMAL

        audioManager.stopBluetoothSco()

        audioManager.isBluetoothScoOn = false

        audioManager.isSpeakerphoneOn = false

        unregisterProximityListener()

        countDownEndCallBusy.cancel()

        countDownEndCall.cancel()

        countDownIncomingCall.cancel()

        //disposable.clear()

        //TODO: Revisar posible leak de memoria por apertura de camara

//        if (callModel.isVideoCall) {

//            localVideoView?.release()
//            remoteVideoView?.release()

//            videoCapturerAndroid?.stopCapture()
//        }

        mHandler.removeCallbacks(mCallTimeRunnable)

        bluetoothStateManager?.onDestroy()

        if (isActiveCall) {
            Timber.d("LLAMADA PASO: SI LLAMADA ACTIVA DISPOSE PEERCONECTION")
            isActiveCall = false
            try {
                peerConnection?.close()
                peerConnection?.dispose()
            } catch (e: java.lang.Exception) {
                Timber.e(e.localizedMessage)
            }
        }

        isMicOn = true

        isVideoMuted = false

        isBluetoothActive = false

        isReturnCall = false

        contactTurnOffCamera = false

        callTime = 0L

        renegotiateCall = false

        Timber.d("LLAMADA PASO: CIERRA LA VISTA DE LLAMADA")
        webRTCClientListener?.callEnded()

    }


    private fun initializeProximitySensor() {
        if (callModel.isVideoCall.not() && audioManager.isSpeakerphoneOn.not() && wakeLock.isHeld.not()) {
            wakeLock.acquire()
        }
    }

    private fun unregisterProximityListener() {
        if (wakeLock.isHeld) {
            wakeLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
        }
    }


    private fun startCallTimer() {
        textViewTimer?.text =
            Utils.getDuration(callTime, callTime >= TimeUnit.HOURS.toMillis(1))
        val oneSecond = TimeUnit.SECONDS.toMillis(1)
        callTime += oneSecond
        mHandler.postDelayed(mCallTimeRunnable, oneSecond)
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

    override fun subscribeToPresenceChannel() {
        Timber.d("LLAMADA PASO 4: SUSCRIBIRSE AL CANAL DE LLAMADAS")
        socketClient.subscribeToPresenceChannel(callModel)
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
        isMicOn = isMicOn.not()
        localAudioTrack?.setEnabled(isMicOn)
    }

    override fun setItsReturnCall(itsReturnCall: Boolean) {
        if (callModel.isVideoCall)
            this.isReturnCall = itsReturnCall
    }

    override fun getMicIsOn(): Boolean = this.isMicOn

    override fun isSpeakerOn(): Boolean = audioManager.isSpeakerphoneOn

    override fun isVideoMuted(): Boolean = this.isVideoMuted

    override fun isBluetoothActive(): Boolean = this.isBluetoothActive

    override fun contactTurnOffCamera(): Boolean = this.contactTurnOffCamera

    override fun initSurfaceRenders() {
        localVideoView?.init(eglBase.eglBaseContext, null)
        localVideoView?.setZOrderMediaOverlay(true)

        remoteVideoView?.init(eglBase.eglBaseContext, null)
        remoteVideoView?.setZOrderMediaOverlay(true)
    }

    override fun startCaptureVideo() {
        createLocalVideoTrack()
        addLocalVideoTrackToLocalMediaStream()
        videoCapturerAndroid?.startCapture(640, 480, 30)
        localVideoTrack?.addSink(localVideoView)
    }

    override fun stopRingAndVibrate() {
        handlerMediaPlayerNotification.stopRingtone()
    }

    override fun emitHangUp() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        audioManager.isSpeakerphoneOn = false
        socketClient.emitClientCall(callModel.channelName, SocketClientImp.HANGUP_CALL)
    }

    override fun changeToVideoCall() {
        if (callModel.isVideoCall.not()) {
            socketClient.emitClientCall(
                callModel.channelName,
                SocketClientImp.CONTACT_WANT_CHANGE_TO_VIDEO
            )
        }
    }

    override fun cancelChangeToVideoCall() {
        socketClient.emitClientCall(
            callModel.channelName,
            SocketClientImp.CONTACT_CANCEL_CHANGE_TO_VIDEO
        )
    }

    override fun muteVideo(checked: Boolean, itsFromBackPressed: Boolean) {
        if (callModel.isVideoCall && localMediaStream.videoTracks.isNotEmpty()) {
            val videoTrack = localMediaStream.videoTracks.first()
            isVideoMuted = checked

            if (checked) {
                socketClient.emitClientCall(
                    callModel.channelName,
                    SocketClientImp.CONTACT_TURN_OFF_CAMERA
                )
                webRTCClientListener?.changeLocalRenderVisibility(View.GONE)
                videoTrack.setEnabled(false)
            } else {
                socketClient.emitClientCall(
                    callModel.channelName,
                    SocketClientImp.CONTACT_TURN_ON_CAMERA
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
                    audioManager.mode = MODE_IN_CALL
                    stopProximitySensor()
                }
                else -> {
                    startProximitySensor()
                    audioManager.isSpeakerphoneOn = callModel.isVideoCall
                }
            }
        }
    }

    override fun playRingtone() {

        mediaPlayerHasStopped = false

        countDownIncomingCall.start()

        audioManager.isSpeakerphoneOn = (isBluetoothAvailable || isHeadsetConnected).not()

        Timber.d("*Test: ${audioManager.isSpeakerphoneOn}")

        Timber.d("RINGTONE: PlayRingtone")
        handlerMediaPlayerNotification.playRingtone()

    }

    override fun playRingBackTone() {

        audioManager.mode = MODE_IN_COMMUNICATION

        if (isBluetoothAvailable || isHeadsetConnected) {
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.isSpeakerphoneOn = callModel.isVideoCall
        }

        countDownEndCall.start()

        Timber.d("RINGTONE: playRingBack EN WEBRTCCLIENT")
        handlerMediaPlayerNotification.playRingBack()

    }

    override fun acceptChangeToVideoCall() {
        callModel.isVideoCall = true
        startCaptureVideo()
        socketClient.emitClientCall(
            callModel.channelName,
            SocketClientImp.CONTACT_ACCEPT_CHANGE_TO_VIDEO
        )
        webRTCClientListener?.changeTextViewTitle(R.string.text_encrypted_video_call)

    }

    override fun startProximitySensor() {
        if (audioManager.isSpeakerphoneOn.not() && isHeadsetConnected.not() && isBluetoothActive.not()) {
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

    override fun unSubscribePresenceChannel() {
        socketClient.unSubscribePresenceChannel(callModel.channelName)
    }

    override fun getPusherChannel(channel: String): Boolean {
        return socketClient.getStatusPresenceChannel(channel)
    }

    override fun renderRemoteVideo() {
        if (remoteMediaStream.videoTracks.isNotEmpty()) {
            remoteMediaStream.videoTracks.first()?.addSink(remoteVideoView)
        }
    }

    private fun renderRemoteVideo(mediaStream: MediaStream) {
        Timber.d("firstMediaStream, ${mediaStream.videoTracks.isEmpty()}")
        if (mediaStream.videoTracks.isNotEmpty()) {

            remoteMediaStream = mediaStream
            val videoTrack = mediaStream.videoTracks[0]
            try {
                stopProximitySensor()

                if (isBluetoothAvailable) {
                    audioManager.isSpeakerphoneOn = false
                } else {
                    audioManager.isSpeakerphoneOn = this.isHeadsetConnected.not()
                }
                webRTCClientListener?.showRemoteVideo()

                videoTrack.addSink(remoteVideoView)
            } catch (e: Exception) {
                Timber.d("NO Got Remote Stream")
                Timber.e(e)
            }
        }
    }

    override fun startWebRTCService(callModel: CallModel) {

        Timber.d("LLAMADA PASO: STARTWEBRTCSERVICE")

        callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL

        val service = Intent(context, WebRTCService::class.java).apply {
            putExtras(Bundle().apply {
                putSerializable(Constants.CallKeys.CALL_MODEL, callModel)
            })
        }
        context.startService(service)
    }

    //region Implementation BluetoothStateManager.BluetoothStateListener
    override fun onBluetoothStateChanged(isAvailable: Boolean) {
        Timber.d("onBluetoothStateChanged: $isAvailable")

        isBluetoothAvailable = isAvailable

        if (isFirstTimeBluetoothAvailable.not() && isHeadsetConnected.not()) {
            Timber.d("isFirstTimeBluetoothAvailableeeee")
            isFirstTimeBluetoothAvailable = true
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            audioManager.isSpeakerphoneOn = false
            stopProximitySensor()
        }

        if (isAvailable && callModel.isVideoCall && isBluetoothStopped) {
            Timber.d("onBluetoothStateChanged 2do")
            audioManager.isSpeakerphoneOn = true
        }

        if (isAvailable && callModel.isVideoCall.not()) {
            stopProximitySensor()
        }

        if (isAvailable.not() && isHeadsetConnected) {
            Timber.d("onBluetoothStateChanged 3ero")
            audioManager.isSpeakerphoneOn = false
        }
        webRTCClientListener?.changeBluetoothButtonVisibility(isAvailable)
    }
    //endregion

    //region Implementation SocketEventListener.Call

    //region Conexion
    override fun itsSubscribedToPresenceChannelOutgoingCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO 7: ya Suscrito")

        if (callModel.channelName == this.callModel.channelName) {

            Timber.d("LLAMADA PASO: onSuccessConnectPresenceChannel")

            webRTCClientListener?.onSuccessConnectPresenceChannel()

            Timber.d("LLAMADA PASO: Crea Offer")

            createOffer()

        }
    }

    override fun itsSubscribedToPresenceChannelIncomingCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO: Inicia el servicio WebRTC desde itsSubscribedToPresenceChannelIncomingCall")

        Timber.d("LLAMADA PASO: onSuccessConnectPresenceChannel callModel: $callModel")

        webRTCClientListener?.onSuccessConnectPresenceChannel()

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

            if (callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL && iceCandidatesCaller.isNotEmpty()) {
                iceCandidatesCaller.forEach { iceCandidate ->
                    socketClient.emitClientCall(
                        channel = callModel.channelName,
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
    //endregion

    //region ChangeToVideoCall
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

    override fun contactAcceptChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName && callModel.isVideoCall.not()) {
            webRTCClientListener?.changeTextViewTitle(R.string.text_encrypted_video_call)
            webRTCClientListener?.contactAcceptChangeToVideoCall()
            callModel.isVideoCall = true
            renegotiateCall = true
            startCaptureVideo()
        }
    }

    override fun contactCancelChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName)
            webRTCClientListener?.contactCancelledVideoCall()
    }

    override fun contactCantChangeToVideoCall(channelName: String) {
        if (channelName == this.callModel.channelName)
            webRTCClientListener?.unlockVideoButton()
    }
    //endregion

    //region Handler Camera
    override fun contactTurnOnCamera(channelName: String) {
        if (channelName == this.callModel.channelName) {
            contactTurnOffCamera = false
            webRTCClientListener?.contactTurnOnCamera()
        }
    }

    override fun contactTurnOffCamera(channelName: String) {
        if (channelName == this.callModel.channelName) {
            contactTurnOffCamera = true
            webRTCClientListener?.contactTurnOffCamera()
        }
    }
    //endregion

    //region Hangup
    override fun contactHasHangup(channelName: String) {
        if (channelName == this.callModel.channelName && callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {

            Timber.d("LLAMADA PASO: COLGAR LLAMADA DESDE ESCUCHADOR")
            disposeCall()

        }
    }
    //endregion

//endregion

}