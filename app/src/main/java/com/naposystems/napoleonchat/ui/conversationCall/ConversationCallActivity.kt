package com.naposystems.napoleonchat.ui.conversationCall

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.databinding.ActivityConversationCallBinding
import com.naposystems.napoleonchat.service.HeadsetBroadcastReceiver
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.audioManagerCompat.AudioManagerCompat
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.naposystems.napoleonchat.webRTC.client.EventFromWebRtcClientListener
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import dagger.android.AndroidInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConversationCallActivity :
    AppCompatActivity(),
    EventFromWebRtcClientListener {

    companion object {

        //Llaves Acciones
        const val ACTION_ANSWER_CALL = "answerCall"
        const val ACTION_RETURN_CALL = "returnCall"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var webRTCClient: WebRTCClient

    @Inject
    lateinit var notificationClient: NotificationClient

    @Inject
    lateinit var handlerNotification: HandlerNotification

    @Inject
    lateinit var handlerDialog: HandlerDialog

    private var isAnswerCall = false

    private var isReturnCall = false

    private lateinit var binding: ActivityConversationCallBinding

    private val viewModel: ConversationCallViewModel by viewModels { viewModelFactory }

    private var contact: ContactEntity? = null

    private val audioManagerCompat by lazy {
        AudioManagerCompat.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)

        Timber.d("LLAMADA PASO 1: MOSTRANDO ACTIVIDAD LLAMADA")

        NapoleonApplication.isShowingCallActivity = true

        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation_call)

        when (NapoleonApplication.statusCall) {
            StatusCallEnum.STATUS_NO_CALL -> {
                Timber.d("LLAMADA PASO 1: reinicia valores")
                webRTCClient.reInit()
            }
            StatusCallEnum.STATUS_CONNECTED_CALL -> {
                Timber.d("LLAMADA PASO 1: volviendo de una llamada previamente conectada")
                showTimer()
                enableControls()
            }
        }

        webRTCClient.setEventsFromWebRTCClientListener(this)

        getExtras()

        if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
            Timber.d("LLAMADA PASO 2: LLAMADA SALIENTE SUSCRIBIENDOSE AL CANAL DE PRESENCIA")
            GlobalScope.launch {
                webRTCClient.subscribeToPresenceChannel()
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                this.window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex.message.toString())
        }

        with(window) {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            if (NapoleonApplication.callModel?.isVideoCall == true) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        audioManagerCompat.requestCallAudioFocus()

        registerReceiver(HeadsetBroadcastReceiver(), IntentFilter(Intent.ACTION_HEADSET_PLUG))

        volumeControlStream = AudioManager.MODE_IN_COMMUNICATION

        super.onCreate(savedInstanceState)

        setUIListeners()

        setViewModelObservers()

    }

    override fun onStart() {
        Timber.d("LLAMADA PASO: onStart")
        if (NapoleonApplication.callModel?.isVideoCall == true)
            initSurfaceRenders()


        if (NapoleonApplication.statusCall.isNoCall()) {
            when (NapoleonApplication.callModel?.typeCall) {
                Constants.TypeCall.IS_INCOMING_CALL -> {
                    if (isAnswerCall)
                        answerCall()
                    else
                        webRTCClient.playRingTone()
                }
                Constants.TypeCall.IS_OUTGOING_CALL ->
                    webRTCClient.playBackTone()
            }
        } else {
            handlerActiveCall()
            if (NapoleonApplication.callModel?.isVideoCall == true) {

                webRTCClient.toggleVideo(previousState = webRTCClient.isHideVideo, false)
                isReturnCall = false
                try {
                    with(window) {
                        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                } catch (ex: Exception) {
                    Timber.e(ex.message.toString())
                }
            }
        }
        super.onStart()
    }

    //    @Synchronized
    private fun initSurfaceRenders() {
        Timber.d("LLAMADA PASO: INICIANDO LAS SUPERFICIES DE RENDERIZADO")
        runOnUiThread {
            webRTCClient.initSurfaceRenders(
                localSurface = binding.localSurfaceRender,
                remoteSurface = binding.remoteSurfaceRender
            )
            if (binding.viewSwitcher.nextView.id == binding.containerVideoCall.id)
                binding.viewSwitcher.showNext()
        }
    }

    //    @Synchronized
    override fun handlerActiveCall() {
        webRTCClient.setTextViewCallDuration(binding.textViewCallDuration)
        Timber.d("LLAMADA PASO: : LLAMADA ACTIVA")
        if (NapoleonApplication.callModel?.isVideoCall == true) {
            Timber.d("LLAMADA PASO: : handlerActiveCall RENDER REMOTE VIDEO")
            webRTCClient.renderRemoteVideo()
            showRemoteVideo()
            binding.localSurfaceRender.isVisible = webRTCClient.isHideVideo.not()
            binding.cameraOff.containerCameraOff.isVisible =
                webRTCClient.contactCameraIsVisible
        }

        binding.imageButtonMicOff.setChecked(webRTCClient.isMicOn.not(), false)
        binding.imageButtonSpeaker.setChecked(webRTCClient.isSpeakerOn(), false)
        binding.imageButtonToggleVideo.setChecked(webRTCClient.isHideVideo, false)
        binding.imageButtonBluetooth.setChecked(webRTCClient.isBluetoothActive, false)
    }

    override fun showRemoteVideo() {
        runOnUiThread {

            binding.isVideoCall = NapoleonApplication.callModel?.isVideoCall

            if (binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
                binding.viewSwitcher.showNext()
            }

            // Creamos la transición
            val transition = ChangeBounds().apply {
                interpolator = AnticipateOvershootInterpolator(1.0f)
                duration = 1200
            }
            // Aplicamos la transición al padre
            TransitionManager.beginDelayedTransition(binding.containerVideoCall, transition)

            // Obtenemos el id del elemento a modificar
            val id = binding.localSurfaceRender.id

            ConstraintSet().apply {
                // clonamos el constrainSet del padre del elemento que vamos a modificar
                clone(binding.containerVideoCall)
                // Cambiamos el margen
                setMargin(
                    id,
                    ConstraintSet.END,
                    Utils.dpToPx(this@ConversationCallActivity, 16f)
                )
                setMargin(
                    id,
                    ConstraintSet.BOTTOM,
                    Utils.dpToPx(this@ConversationCallActivity, 76f)
                )
                // Cambiamos su tamaño
                constrainPercentWidth(id, 0.3f)
                constrainPercentHeight(id, 0.3f)
                // Quitamos el constraint que tiene
                clear(id, ConstraintSet.TOP)
                clear(id, ConstraintSet.START)
                // Aplicamos los constraint al padre
                applyTo(binding.containerVideoCall)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL && NapoleonApplication.statusCall.isNoCall()) {
            webRTCClient.handleKeyDown(keyCode)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {
        setCallOnBackground()
        super.onBackPressed()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
    }

    override fun onResume() {
        super.onResume()
        if (NapoleonApplication.callModel?.isVideoCall == false){
            webRTCClient.startProximitySensor()
        }
    }

    override fun onPause() {
        setCallOnBackground()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)

        Timber.d("onNewIntent ${intent.action}")

        if (intent.action == WebRTCService.ACTION_ANSWER_CALL &&
            NapoleonApplication.statusCall.isNoCall() &&
            NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL
        ) {
            isAnswerCall = true
            answerCall()
        }
    }

    private fun setCallOnBackground() {
        webRTCClient.stopProximitySensor()
        if (NapoleonApplication.statusCall.isConnectedCall()) {
            Timber.d("startCallActivity, onBackPressed")
            if (NapoleonApplication.callModel?.isVideoCall == true) {
                webRTCClient.toggleVideo(previousState = true, itsFromBackPressed = true)
            }
            NapoleonApplication.isShowingCallActivity = false
        }
    }

    private fun getExtras() {
        try {
            Timber.d("LLAMADA PASO 2: GETEXTRAS CALLMODEL: ${NapoleonApplication.callModel}")
            NapoleonApplication.callModel?.contactId?.let { viewModel.getContact(it) }
            binding.typeCall = NapoleonApplication.callModel?.typeCall?.type
            binding.isVideoCall = NapoleonApplication.callModel?.isVideoCall
            intent.extras?.let { extras ->
                if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                    Timber.d("LLAMADA PASO: LLAMADA ENTRANTE SETEANDO OFERTA")
                    webRTCClient.setOffer()
                    if (extras.getBoolean(ACTION_ANSWER_CALL, false)) {
                        Timber.d("LLAMADA PASO: Seteando isAnswerCall")
                        isAnswerCall = true
                    }
                }
                if (extras.getBoolean(ACTION_RETURN_CALL, true)) {
                    Timber.d("LLAMADA PASO: Seteando isReturnCall")
                    isReturnCall = true
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex.message.toString())
        }
    }

    private fun setUIListeners() {

        if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_OUTGOING_CALL)
            binding.fabAnswer.visibility = View.GONE

        binding.fabAnswer.setOnClickListener {
            if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                answerCall()
            }
        }

        binding.fabHangup.setOnClickListener {
            hangUp()
        }

        binding.imageButtonSpeaker.setOnClickListener {
            webRTCClient.setSpeakerOn(binding.imageButtonSpeaker.isChecked)
        }

        binding.imageButtonMicOff.setOnCheckedChangeListener { _, _ ->
            webRTCClient.setMicOff()
        }

        binding.imageButtonChangeToVideo.setOnClickListener {
            binding.imageButtonChangeToVideo.isEnabled = false
            webRTCClient.changeToVideoCall()
        }

        binding.imageButtonToggleVideo.setOnCheckedChangeListener { _, isChecked ->
            webRTCClient.toggleVideo(isChecked)
        }

        binding.imageButtonSwitchCamera.setOnClickListener {
            webRTCClient.switchCamera()
        }

        binding.imageButtonBluetooth.setOnCheckedChangeListener { _, isChecked ->
            webRTCClient.handleBluetooth(isChecked)
        }
    }

    private fun setViewModelObservers() {
        viewModel.contact.observe(this, Observer { contact ->
            if (contact != null) {
                this.contact = contact
                binding.contact = contact
                binding.executePendingBindings()
            }
        })

        viewModel.userDisplayFormat.observe(this, Observer { format ->
            when (format) {
                Constants.UserDisplayFormat.NAME_AND_NICKNAME.format -> {
                    binding.textViewName.visibility = View.VISIBLE
                    binding.textViewNickname.visibility = View.VISIBLE
                }
                Constants.UserDisplayFormat.ONLY_NICKNAME.format -> {
                    binding.textViewName.visibility = View.GONE
                    binding.textViewNickname.visibility = View.VISIBLE
                }
                else -> {
                    // ONLY NAME
                    binding.textViewName.visibility = View.VISIBLE
                    binding.textViewNickname.visibility = View.GONE
                }
            }
        })
    }

    private fun answerCall() {
        handlerActiveCall()
        webRTCClient.stopRingAndVibrate()
        binding.fabAnswer.visibility = View.GONE
        GlobalScope.launch {
            webRTCClient.createAnswer()
        }
    }

    private fun hangUp() {

        binding.fabHangup.isEnabled = false

        Timber.d("LLAMADA PASO: HANGUP PRESIONADO TypeCall: ${NapoleonApplication.callModel?.typeCall}")

        when (NapoleonApplication.statusCall) {
            StatusCallEnum.STATUS_NO_CALL -> {
                Timber.d("LLAMADA PASO: SI LLAMADA NO ACTIVA CONSUME SENDMISSED Y CANCELCALL")
                when (NapoleonApplication.callModel?.typeCall) {
                    Constants.TypeCall.IS_OUTGOING_CALL -> {
                        Timber.d("LLAMADA PASO: LLAMADA COLGADA SE ENVIA LLAMADA PERDIDA")
                        viewModel.sendMissedCall()
                        Timber.d("LLAMADA PASO: LLAMADA COLGADA SE CONSUME CANCEL CALL")
                        viewModel.cancelCall()
                    }

                    Constants.TypeCall.IS_INCOMING_CALL -> {
                        Timber.d("LLAMADA PASO: LLAMADA COLGADA SE CONSUME CANCEL CALL")
                        viewModel.rejectCall()
                    }
                }
            }
            StatusCallEnum.STATUS_CONNECTED_CALL -> {
                Timber.d("LLAMADA PASO: SI LLAMADA ACTIVA EMITE COLGAR")
                webRTCClient.emitHangUp()
            }
        }
        webRTCClient.playEndCall()
    }

    //region Implementation WebRTCClient.WebRTCClientListener
    override fun toggleContactCamera(visibility: Int) {
        runOnUiThread {
            binding.cameraOff.containerCameraOff.visibility = visibility
        }
    }

    override fun contactWantChangeToVideoCall() {
        runOnUiThread {
            binding.imageButtonChangeToVideo.isEnabled = true
            handlerDialog.alertDialogWithoutNeutralButton(
                R.string.text_contact_want_change_to_video_call,
                false,
                this,
                Constants.LocationAlertDialog.CALL_ACTIVITY.location,
                R.string.text_accept,
                R.string.text_cancel,
                clickPositiveButton = {
                    initSurfaceRenders()
                    webRTCClient.meAcceptChangeToVideoCall()
                    handlerActiveCall()
                }, clickNegativeButton = {
                    webRTCClient.meCancelChangeToVideoCall()
                }
            )
        }
    }

    override fun contactAcceptChangeToVideoCall() {

        initSurfaceRenders()

        handlerActiveCall()

//        try {
//            with(window) {
//                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//            }
//        } catch (ex: Exception) {
//            Timber.e(ex.message.toString())
//        }

    }

    override fun contactCancelChangeToVideoCall() {
        runOnUiThread {
            handlerDialog.alertDialogInformative(
                "",
                getString(R.string.text_video_call_rejected),
                true,
                this,
                R.string.text_okay
            ) {}
            binding.imageButtonChangeToVideo.isEnabled = true
        }
    }

    override fun contactCantChangeToVideoCall() {
        runOnUiThread {
            binding.imageButtonChangeToVideo.isEnabled = true
        }
    }

    override fun showTimer() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.GONE
            binding.textViewCallDuration.visibility = View.VISIBLE
        }
    }

    override fun showCypheryngCall() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.VISIBLE
            binding.textViewCallDuration.visibility = View.GONE
            binding.textViewCalling.text =
                getString(if (NapoleonApplication.callModel?.isVideoCall == true) R.string.text_encrypting_videocall else R.string.text_encrypting_call)
        }
    }

    override fun showReConnectingCall() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.VISIBLE
            binding.textViewCallDuration.visibility = View.GONE
            binding.textViewCalling.text = getString(R.string.text_reconnecting_call)
        }
    }

    override fun showOccupiedCall() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.VISIBLE
            binding.textViewCallDuration.visibility = View.GONE
            binding.textViewCalling.text =
                getString(R.string.text_contact_is_busy, contact?.getNickName())
        }
    }

    override fun showFinishingCall() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.VISIBLE
            binding.textViewCallDuration.visibility = View.GONE
            binding.textViewCalling.text = getString(R.string.text_finishing_call)
        }
    }

    override fun enableControls() {
        runOnUiThread {
            binding.containerControls.visibility = View.VISIBLE
            binding.viewBottomSeparator.visibility = View.VISIBLE
            binding.fabAnswer.visibility = View.GONE
        }
    }

    override fun showTypeCallTitle() {
        try {
            runOnUiThread {
                NapoleonApplication.callModel?.let {
                    if (it.isVideoCall)
                        binding.textViewTitle.text =
                            getString(R.string.text_encrypted_video_call)
                    else
                        binding.textViewTitle.text =
                            getString(R.string.text_encrypted_call)
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex.message.toString())
        }
    }

    override fun toggleLocalRenderVisibility(visibility: Boolean) {
        runOnUiThread {
            binding.localSurfaceRender.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun toggleBluetoothButtonVisibility(isVisible: Boolean) {
        val audioManager: AudioManager = Utils.getAudioManager(this)

        Timber.d("isBluetoothScoOn: ${audioManager.isBluetoothScoOn}")

        binding.imageButtonBluetooth.visibility = if (isVisible) View.VISIBLE else View.GONE

        when {
            audioManager.isBluetoothScoOn -> {
                binding.imageButtonSpeaker.setChecked(checked = false, notifyListener = false)
                binding.imageButtonBluetooth.setChecked(checked = true, notifyListener = false)
            }
            audioManager.isSpeakerphoneOn -> {
                binding.imageButtonSpeaker.setChecked(checked = true, notifyListener = false)
                binding.imageButtonBluetooth.setChecked(checked = false, notifyListener = false)
            }
            else -> {
                binding.imageButtonSpeaker.setChecked(checked = false, notifyListener = false)
                binding.imageButtonBluetooth.setChecked(checked = false, notifyListener = false)
            }
        }
    }

    override fun toggleCheckedSpeaker(checked: Boolean) {
        runOnUiThread {
            binding.imageButtonSpeaker.setChecked(checked = false, notifyListener = false)
        }
    }

    override fun hangUpFromNotification() {
        hangUp()
    }

    override fun onContactNotAnswer() {
        if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
            Timber.d("LLAMADA PASO: CONTACTO NO CONTESTO Y SE CANCELA LLAMADA")
            viewModel.cancelCall()
            viewModel.sendMissedCall()
        }
    }

    override fun callEnded() {
        Timber.d("LLAMADA PASO: SETEA A FALSE LA VISTA DE LLAMADA")
        NapoleonApplication.isShowingCallActivity = false
        NapoleonApplication.callModel = null
        finish()
    }
//endregion
}
