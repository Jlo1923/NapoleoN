package com.naposystems.napoleonchat.ui.conversationCall

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
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.service.HeadsetBroadcastReceiver
import com.naposystems.napoleonchat.service.notificationClient.HandlerNotification
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.audioManagerCompat.AudioManagerCompat
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import com.naposystems.napoleonchat.webRTC.client.WebRTCClientListener
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ConversationCallActivity : AppCompatActivity(), WebRTCClientListener {

    companion object {

        //Llaves Modelo
        const val KEY_CALL_MODEL = "callModel"

        //Llaves Acciones
        const val ACTION_ANSWER_CALL = "answerCall"

        const val ITS_FROM_RETURN_CALL = "its_from_return_call"
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

    private lateinit var binding: ActivityConversationCallBinding

    private val viewModel: ConversationCallViewModel by viewModels { viewModelFactory }

    private var contact: ContactEntity? = null

    private lateinit var callModel: CallModel

    private val audioManagerCompat by lazy {
        AudioManagerCompat.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)

        NapoleonApplication.isShowingCallActivity = true

        NapoleonApplication.isCurrentOnCall = true

        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation_call)

        Timber.d("LLAMADA PASO 1: MOSTRANDO ACTIVIDAD LLAMADA")

        webRTCClient.setWebRTCClientListener(this)

        getExtras()

        if (callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
            Timber.d("LLAMADA PASO 2: LLAMADA SALIENTE")
            webRTCClient.subscribeToCallChannel()
        }

        audioManagerCompat.requestCallAudioFocus()

        Timber.d("onCreate")

        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)

        val receiver = HeadsetBroadcastReceiver()

        registerReceiver(receiver, intentFilter)

        volumeControlStream = AudioManager.MODE_IN_COMMUNICATION

        super.onCreate(savedInstanceState)

        if (callModel.isVideoCall) {
            initSurfaceRenders()
        }

        webRTCClient.setTextViewCallDuration(binding.textViewCalling)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        with(window) {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            if (callModel.isVideoCall) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        if (callModel.isVideoCall && binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
            webRTCClient.startCaptureVideo()
            binding.viewSwitcher.showNext()
        }

        if (!webRTCClient.isActiveCall) {
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                if (Build.VERSION.SDK_INT < 29 || callModel.isFromClosedApp == Constants.FromClosedApp.YES) {
                    Timber.d("*Test: Ring CallActivity")
                    webRTCClient.playRingtone()
                }
            } else {

                webRTCClient.startWebRTCService(
                    callModel
                )
                webRTCClient.playRingBackTone()
            }
        } else {
            if (callModel.isVideoCall) {
                webRTCClient.renderRemoteVideo()
                showRemoteVideo()
                binding.surfaceRender.isVisible = !webRTCClient.isVideoMuted()
                binding.cameraOff.containerCameraOff.isVisible = webRTCClient.contactTurnOffCamera()
            }
            binding.imageButtonMicOff.setChecked(!webRTCClient.getMicIsOn(), false)
            binding.imageButtonSpeaker.setChecked(webRTCClient.isSpeakerOn(), false)
            binding.imageButtonMuteVideo.setChecked(webRTCClient.isVideoMuted(), false)
            binding.imageButtonBluetooth.setChecked(webRTCClient.isBluetoothActive(), false)
        }

        setUIListeners()

        setViewModelObservers()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL && !webRTCClient.isActiveCall) {
            webRTCClient.handleKeyDown(keyCode)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {

        if (webRTCClient.isActiveCall) {

            Timber.d("startCallActivity, onBackPressed")

            if (webRTCClient.callModel.isVideoCall) {
                webRTCClient.muteVideo(checked = true, itsFromBackPressed = true)
            }

            webRTCClient.setIsOnCallActivity(false)

            super.onBackPressed()
        }
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
        super.onPause()
        webRTCClient.stopProximitySensor()
    }

    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)

        Timber.d("onNewIntent ${intent.action}")

        if (intent.action == WebRTCService.ACTION_ANSWER_CALL &&
            !webRTCClient.isActiveCall &&
            callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL
        ) {

            binding.fabAnswer.visibility = View.GONE

            webRTCClient.stopRingAndVibrate()

            if (webRTCClient.getPusherChannel(callModel.channelName)) {
                Timber.d("ACTION_ANSWER_CALL if")
                webRTCClient.createAnswer()
            } else {
                Timber.d("ACTION_ANSWER_CALL else")
                //webRTCClient.subscribeToChannelFromBackground(channel)
            }
        }
    }

    private fun getExtras() {

        try {

            Timber.d("LLAMADA PASO: INTENTA OBTENER EXTRAS}")

            intent.extras?.let { extras ->

                Timber.d("LLAMADA PASO: OBTENIENDO EXTRAS ${extras.keySet()}")

                Timber.d("LLAMADA PASO: OBTENIENDO KEY_CALL_MODEL")
                callModel = extras.getSerializable(KEY_CALL_MODEL) as CallModel

                Timber.d("LLAMADA PASO 2: GETEXTRAS CALLMODEL: $callModel")

                viewModel.getContact(callModel.contactId)

                NapoleonApplication.currentCallContactId = callModel.contactId

                binding.typeCall = callModel.typeCall.type

                binding.isVideoCall = callModel.isVideoCall

                webRTCClient.callModel = callModel

                if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {

                    webRTCClient.setOffer(callModel.offer)

                    if (extras.getBoolean(ACTION_ANSWER_CALL, false))
                        answerCall()
                }

                if (extras.containsKey(ITS_FROM_RETURN_CALL)) {
                    webRTCClient.setItsReturnCall(extras.getBoolean(ITS_FROM_RETURN_CALL, false))
                }
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }

    private fun setUIListeners() {

        binding.fabAnswer.setOnClickListener {
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
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

        binding.imageButtonMuteVideo.setOnCheckedChangeListener { _, isChecked ->
            webRTCClient.muteVideo(isChecked)
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

        Timber.d("HANGUP: PRESIONADO")

        binding.fabHangup.isEnabled = false

        viewModel.resetIsOnCallPref()

        Timber.d("HANGUP: PRESIONADO ${webRTCClient.isActiveCall} TypeCall: ${callModel.typeCall}")

        if (!webRTCClient.isActiveCall)
            when (callModel.typeCall) {
                Constants.TypeCall.IS_OUTGOING_CALL -> {

                    Timber.d("HANGUP: SEND MISSED CALL")
                    Timber.d("HANGUP: CANCELL CALL")

                    viewModel.sendMissedCall(callModel)
                    viewModel.cancelCall(callModel)
                }

                Constants.TypeCall.IS_INCOMING_CALL -> {

                    Timber.d("HANGUP: CANCELL CALL")

                    viewModel.cancelCall(callModel)
                }
            }
        else
            webRTCClient.emitHangUp()

        Timber.d("HANGUP: EMITE COLGAR")

        webRTCClient.disposeCall()

        closeNotification()

        Timber.d("SocketService webRTCClient.dispose()")

    }

    private fun closeNotification() {
        val intent = Intent(this, WebRTCService::class.java)
        intent.action = WebRTCService.ACTION_CALL_END
        val bundle = Bundle().apply {
            putSerializable(Constants.CallKeys.CALL_MODEL, callModel)
        }
        intent.putExtras(bundle)
        this.startService(intent)
    }

    private fun initSurfaceRenders() {
        runOnUiThread {
            webRTCClient.setLocalVideoView(binding.surfaceRender)
            webRTCClient.setRemoteVideoView(binding.remoteSurfaceRender)
            webRTCClient.initSurfaceRenders()
        }
    }

    //region Implementation WebRTCClient.WebRTCClientListener
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
                    initSurfaceRenders()
                    webRTCClient.acceptChangeToVideoCall()
                    binding.textViewTitle.text =
                        getString(R.string.text_encrypted_video_call)
                }, clickNegativeButton = {
                    webRTCClient.cancelChangeToVideoCall()
                }
            )
        })
    }

    override fun contactCancelledVideoCall() {
        handlerDialog.alertDialogInformative(
            "",
            getString(R.string.text_video_call_rejected),
            true,
            this,
            R.string.text_okay
        ) {}
        binding.imageButtonChangeToVideo.isEnabled = true
    }

    override fun contactTurnOffCamera() {
        binding.cameraOff.containerCameraOff.visibility = View.VISIBLE
    }

    override fun contactTurnOnCamera() {
        binding.cameraOff.containerCameraOff.visibility = View.GONE
    }

    override fun showRemoteVideo() {
        runOnUiThread {
            callModel.isVideoCall = true
            binding.isVideoCall = true
            if (binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
                binding.viewSwitcher.showNext()
            }

            val constraintSet = ConstraintSet()

            // clonamos el constrainSet del padre del elemento que vamos a modificar
            constraintSet.clone(binding.containerVideoCall)

            // Obtenemos el id del elemento a modificar
            val id = binding.surfaceRender.id

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

    override fun callEnded() {
        finish()
    }

    override fun changeLocalRenderVisibility(visibility: Int) {
        binding.surfaceRender.visibility = visibility
    }

    override fun changeTextViewTitle(stringResourceId: Int) {
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

    override fun changeBluetoothButtonVisibility(isVisible: Boolean) {
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

    override fun enableControls() {
        runOnUiThread {
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                binding.containerControls.visibility = View.VISIBLE
                binding.fabAnswer.visibility = View.GONE
            }
        }
    }

    override fun resetIsOnCallPref() {
        viewModel.resetIsOnCallPref()
    }

    override fun contactNotAnswer() {
        if (callModel.typeCall == Constants.TypeCall.IS_OUTGOING_CALL) {
            Timber.d("CancelCall")
            viewModel.cancelCall(callModel)
            viewModel.sendMissedCall(callModel)
        }
    }

    override fun showTimer() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.GONE
            binding.textViewCallDuration.visibility = View.VISIBLE
        }
    }

    override fun showConnectingTitle() {
        runOnUiThread {
            binding.textViewCalling.isVisible = true
            binding.textViewCalling.text =
                getString(if (callModel.isVideoCall) R.string.text_encrypting_videocall else R.string.text_encrypting_call)
        }
    }

    override fun changeCheckedSpeaker(checked: Boolean) {
        runOnUiThread {
            binding.imageButtonSpeaker.setChecked(checked = false, notifyListener = false)
        }
    }

    override fun hangupByNotification() {
        hangUp()
    }

    override fun unlockVideoButton() {
        binding.imageButtonChangeToVideo.isEnabled = true
    }

    override fun rejectByNotification() {
        hangUp()
    }

    override fun contactAcceptChangeToVideoCall() {
        initSurfaceRenders()
    }

    //endregion
}
