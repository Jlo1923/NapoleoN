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
import com.naposystems.napoleonchat.webRTC.client.EvenstFromWebRTCClientListener
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ConversationCallActivity :
    AppCompatActivity(), EvenstFromWebRTCClientListener {

    companion object {

        //Llaves Acciones
        const val ACTION_ANSWER_CALL = "answerCall"
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
            webRTCClient.subscribeToPresenceChannel()
        }

        if (NapoleonApplication.callModel?.isVideoCall == true) {

            Timber.d("LLAMADA PASO: ES VIDEOLLAMADA INICIANDO LAS SUPERFICIES DE RENDERIZADO")

            initSurfaceRenders()
        }

        webRTCClient.setTextViewCallDuration(binding.textViewCallDuration)

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
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
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

        if (NapoleonApplication.statusCall.isNoCall()) {

            when (NapoleonApplication.callModel?.typeCall) {
                Constants.TypeCall.IS_INCOMING_CALL -> {
                    if (isAnswerCall.not())
                        webRTCClient.playRingtone()
                }
                Constants.TypeCall.IS_OUTGOING_CALL -> webRTCClient.playRingBackTone()
            }

        } else {

            Timber.d("LLAMADA PASO: : LLAMADA ACTIVA")

            if (NapoleonApplication.callModel?.isVideoCall == true) {
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

        audioManagerCompat.requestCallAudioFocus()

        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)

        val receiver = HeadsetBroadcastReceiver()

        registerReceiver(receiver, intentFilter)

        volumeControlStream = AudioManager.MODE_IN_COMMUNICATION

        super.onCreate(savedInstanceState)

        setUIListeners()

        setViewModelObservers()

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
        webRTCClient.startProximitySensor()
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
                webRTCClient.toggleVideo(checked = true, itsFromBackPressed = true)
            }

            NapoleonApplication.isShowingCallActivity = false

        }
    }

    private fun getExtras() {

        try {

            Timber.d("LLAMADA PASO: INTENTA OBTENER EXTRAS")

            Timber.d("LLAMADA PASO 2: GETEXTRAS CALLMODEL: ${NapoleonApplication.callModel}")

            NapoleonApplication.callModel?.contactId?.let { viewModel.getContact(it) }

            binding.typeCall = NapoleonApplication.callModel?.typeCall?.type

            binding.isVideoCall = NapoleonApplication.callModel?.isVideoCall

            intent.extras?.let { extras ->

                if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {

                    Timber.d("LLAMADA PASO: LLAMADA ENTRANTE SETEANDO OFERTA")

                    webRTCClient.setOffer()

                    if (extras.getBoolean(ACTION_ANSWER_CALL, false)) {
                        isAnswerCall = true
                        Timber.d("LLAMADA PASO: LLAMADA ENTRANTE RESPONDIENDO LLAMADA")
                        answerCall()
                    }
                }

            }

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
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

        webRTCClient.stopRingAndVibrate()

        binding.fabAnswer.visibility = View.GONE

        webRTCClient.createAnswer()

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

        webRTCClient.disposeCall()

    }

    private fun initSurfaceRenders() {

        Timber.d("LLAMADA PASO: INICIANDO LAS SUPERFICIES DE RENDERIZADO")

        runOnUiThread {
            webRTCClient.setLocalVideoView(binding.localSurfaceRender)
            webRTCClient.setRemoteVideoView(binding.remoteSurfaceRender)
            webRTCClient.initSurfaceRenders()

            if (binding.viewSwitcher.nextView.id == binding.containerVideoCall.id)
                binding.viewSwitcher.showNext()

        }
    }

    override fun contactWantChangeToVideoCall() {
        runOnUiThread(Runnable {

            binding.imageButtonChangeToVideo.isEnabled = true

            handlerDialog.alertDialogWithoutNeutralButton(
                R.string.text_contact_want_change_to_video_call,
                false,
                this,
                Constants.LocationAlertDialog.CALL_ACTIVITY.location,
                R.string.text_accept,
                R.string.text_cancel,
                clickPositiveButton = {

                    NapoleonApplication.callModel?.typeCall = Constants.TypeCall.IS_INCOMING_CALL

                    NapoleonApplication.callModel?.isVideoCall = true

                    initSurfaceRenders()

                    webRTCClient.meAcceptChangeToVideoCall()

                    binding.textViewTitle.text =
                        getString(R.string.text_encrypted_video_call)
                }, clickNegativeButton = {
                    webRTCClient.meCancelChangeToVideoCall()
                }
            )
        })
    }

    override fun contactAcceptChangeToVideoCall() {

        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        NapoleonApplication.callModel?.typeCall = Constants.TypeCall.IS_OUTGOING_CALL

        NapoleonApplication.callModel?.isVideoCall = true

        initSurfaceRenders()
    }

    override fun contactCancelChangeToVideoCall() {
        handlerDialog.alertDialogInformative(
            "",
            getString(R.string.text_video_call_rejected),
            true,
            this,
            R.string.text_okay
        ) {}
        binding.imageButtonChangeToVideo.isEnabled = true
    }

    override fun contactCantChangeToVideoCall() {
        binding.imageButtonChangeToVideo.isEnabled = true
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

    override fun showReConnectingTitle() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.VISIBLE
            binding.textViewCallDuration.visibility = View.GONE
            binding.textViewCalling.text = getString(R.string.text_reconnecting_call)
        }
    }

    override fun showFinishingTitle() {
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

    override fun showRemoteVideo() {
        runOnUiThread {
            NapoleonApplication.callModel?.isVideoCall = true
            binding.isVideoCall = true
            if (binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
                binding.viewSwitcher.showNext()
            }

            val constraintSet = ConstraintSet()

            // clonamos el constrainSet del padre del elemento que vamos a modificar
            constraintSet.clone(binding.containerVideoCall)

            // Obtenemos el id del elemento a modificar
            val id = binding.localSurfaceRender.id

            // Cambiamos el margen
            constraintSet.setMargin(id, ConstraintSet.END, Utils.dpToPx(this, 16f))
            constraintSet.setMargin(id, ConstraintSet.BOTTOM, Utils.dpToPx(this, 76f))

            // Cambiamos su tamaño
            constraintSet.constrainPercentWidth(id, 0.3f)
            constraintSet.constrainPercentHeight(id, 0.3f)

            // Quitamos el constraint que tiene
            constraintSet.clear(id, ConstraintSet.TOP)
            constraintSet.clear(id, ConstraintSet.START)

            // Creamos la transición
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(1.0f)
            transition.duration = 1200

            // Aplicamos la transición al padre
            TransitionManager.beginDelayedTransition(binding.containerVideoCall, transition)

            // Aplicamos los constraint al padre
            constraintSet.applyTo(binding.containerVideoCall)
        }
    }

    override fun changeTextviewTitle(stringResourceId: Int) {
        try {
            runOnUiThread {
                binding.textViewTitle.text =
                    getString(
                        stringResourceId,
                        this.getString(R.string.label_nickname, contact?.getNickName())
                    )
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }

    override fun toggleLocalRenderVisibility(visibility: Int) {
        runOnUiThread(Runnable {
            binding.localSurfaceRender.visibility = visibility
        })
    }

    //region Implementation WebRTCClient.WebRTCClientListener
    override fun toggleContactCamera(visibility: Int) {
        runOnUiThread(Runnable {
            binding.cameraOff.containerCameraOff.visibility = visibility
        })
    }

    override fun toggleBluetoothButtonVisibility(isVisible: Boolean) {
        val audioManager: AudioManager = Utils.getAudioManager(this)

        Timber.d("isBluetoothScoOn: ${audioManager.isBluetoothScoOn}")

        binding.imageButtonBluetooth.visibility = if (isVisible) View.VISIBLE else View.GONE

        when {
            audioManager.isBluetoothScoOn -> {
                binding.imageButtonBluetooth.setChecked(checked = true, notifyListener = false)
                binding.imageButtonSpeaker.setChecked(checked = false, notifyListener = false)
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
