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
import com.naposystems.napoleonchat.databinding.ActivityConversationCallBinding
import com.naposystems.napoleonchat.service.HeadsetBroadcastReceiver
import com.naposystems.napoleonchat.service.notificationMessage.NotificationMessagesService
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.audioManagerCompat.AudioManagerCompat
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import com.naposystems.napoleonchat.webRTC.client.WebRTCClientListener
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ConversationCallActivity : AppCompatActivity(), WebRTCClientListener {

    companion object {
        const val ANSWER_CALL = "answerCall"
        const val IS_VIDEO_CALL = "isVideoCall"
        const val CONTACT_ID = "contact"
        const val TYPE_CALL = "typeCall"
        const val CHANNEL = "channel"
        const val IS_FROM_CLOSED_APP = "isFromClosedApp"
        const val ITS_FROM_RETURN_CALL = "its_from_return_call"
        const val OFFER = "offer"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var webRTCClient: WebRTCClient

    @Inject
    lateinit var notificationMessagesService: NotificationMessagesService

    private lateinit var binding: ActivityConversationCallBinding

    private val viewModel: ConversationCallViewModel by viewModels { viewModelFactory }

    private var contact: ContactEntity? = null

    private var isVideoCall: Boolean = false
    private var contactId: Int = 0
    private var typeCall: Int = Constants.TypeCall.IS_OUTGOING_CALL.type
    private var channel: String = ""
    private var isFromClosedApp: Boolean = false
    private var hangUpPressed: Boolean = false
    private var answerCall: Boolean = false
    private var offer: String = ""

    private val audioManagerCompat by lazy {
        AudioManagerCompat.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)

        Data.isShowingCallActivity = true

        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation_call)

        webRTCClient.setWebRTCClientListener(this)

        getExtras()

        if (typeCall == Constants.TypeCall.IS_OUTGOING_CALL.type)
            webRTCClient.subscribeToCallChannel()

        audioManagerCompat.requestCallAudioFocus()

        Timber.d("onCreate")

        val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)

        val receiver = HeadsetBroadcastReceiver()

        registerReceiver(receiver, intentFilter)

        volumeControlStream = AudioManager.MODE_IN_COMMUNICATION

        super.onCreate(savedInstanceState)

        initSurfaceRenders()

        webRTCClient.setTextViewCallDuration(binding.textViewCalling)

        with(window) {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            if (isVideoCall) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        if (isVideoCall && binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
            webRTCClient.startCaptureVideo()
            binding.viewSwitcher.showNext()
        }

        if (!webRTCClient.isActiveCall) {
            if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                if (Build.VERSION.SDK_INT < 29 || !isFromClosedApp) {
                    Timber.d("*Test: Ring CallActivity")
                    webRTCClient.playRingtone()
                }
            } else {
                notificationMessagesService.startWebRTCCallService(
                    channel, isVideoCall, contactId, false, offer
                )
                webRTCClient.playCallingTone()
            }
        } else {
            if (isVideoCall) {
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
        return if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type && !webRTCClient.isActiveCall) {
            webRTCClient.handleKeyDown(keyCode)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {

        if (webRTCClient.isActiveCall) {

            Timber.d("startCallActivity, onBackPressed")

            if (webRTCClient.isVideoCall) {
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
            typeCall == Constants.TypeCall.IS_INCOMING_CALL.type
        ) {

            binding.fabAnswer.visibility = View.GONE
            webRTCClient.stopRingAndVibrate()

            if (webRTCClient.getPusherChannel(channel) != null) {
                Timber.d("ACTION_ANSWER_CALL if")
                webRTCClient.createAnswer()
            } else {
                Timber.d("ACTION_ANSWER_CALL else")
                //webRTCClient.subscribeToChannelFromBackground(channel)
            }
        }
    }

    private fun getExtras() {
        intent.extras?.let { bundle ->
            channel = bundle.getString(CHANNEL, "")

            if (bundle.containsKey(CONTACT_ID)) {
                contactId = bundle.getInt(CONTACT_ID)
                viewModel.getContact(contactId)
                Data.currentCallContactId = contactId
            }

            webRTCClient.contactId = contactId
            webRTCClient.channel = channel

            Timber.d("getExtras: ${intent.action}, ${bundle.containsKey(ANSWER_CALL)}")
            if (bundle.containsKey(TYPE_CALL) && !webRTCClient.isActiveCall) {
                typeCall = bundle.getInt(TYPE_CALL)
                binding.typeCall = typeCall
                webRTCClient.typeCall = typeCall

                //TODO: Verificar si para la conexion del socket se necesita la llamada activa
            }

            if (bundle.containsKey(IS_VIDEO_CALL)) {
                isVideoCall = bundle.getBoolean(IS_VIDEO_CALL, false)
                binding.isVideoCall = isVideoCall
                webRTCClient.isVideoCall = isVideoCall
            }

            if (bundle.containsKey(IS_FROM_CLOSED_APP)) {
                isFromClosedApp = bundle.getBoolean(IS_FROM_CLOSED_APP, false)
            }

            webRTCClient.setItsReturnCall(bundle.getBoolean(ITS_FROM_RETURN_CALL, false))

            //NotificationUtils.cancelWebRTCCallNotification(this)

            if (bundle.containsKey(OFFER)) {
                offer = bundle.getString(OFFER, "")

                webRTCClient.setOffer(offer)
            }

            if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                Timber.d("ACTION_ANSWER_CALL: ${bundle.getBoolean(ANSWER_CALL, false)}")
                val answerCall = bundle.getBoolean(ANSWER_CALL, false)

                if (answerCall) {
                    Timber.d("webRTCClient.stopRingAndVibrate()")
                    webRTCClient.stopRingAndVibrate()
                    binding.fabAnswer.isVisible = false
                    webRTCClient.createAnswer()
                }

            }

        }
    }

    private fun setUIListeners() {

        binding.fabAnswer.setOnClickListener {
            if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                webRTCClient.createAnswer()
                notificationMessagesService.stopMediaPlayer()
                webRTCClient.stopRingAndVibrate()
                binding.fabAnswer.visibility = View.GONE
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

    private fun hangUp() {
        Timber.d("WebRTCClient hangUp")
        if (!hangUpPressed) {

            Data.isContactReadyForCall = false

            closeNotification()

            hangUpPressed = true
            viewModel.resetIsOnCallPref()
            when {
                typeCall == Constants.TypeCall.IS_OUTGOING_CALL.type && !webRTCClient.isActiveCall -> {
                    viewModel.sendMissedCall(contactId, isVideoCall)
                    Timber.d("CancelCall 1")
                    viewModel.cancelCall(contactId, channel)
                }
                typeCall == Constants.TypeCall.IS_INCOMING_CALL.type && !webRTCClient.isActiveCall -> {
                    Timber.d("CancelCall 2")
                    viewModel.cancelCall(contactId, channel)
                }
                else -> {
                    webRTCClient.emitHangUp()
                }
            }

            webRTCClient.dispose()
            Timber.d("SocketService webRTCClient.dispose()")
        }
    }

    private fun closeNotification() {
        val intent = Intent(this, WebRTCService::class.java)
        intent.action = WebRTCService.ACTION_CALL_END
        val bundle = Bundle()

        bundle.putString(
            Constants.CallKeys.CHANNEL_NAME,
            channel
        )

        bundle.putInt(
            Constants.CallKeys.CONTACT_ID,
            contactId
        )
        intent.putExtras(bundle)
        this.startService(intent)
    }

    private fun initSurfaceRenders() {
        webRTCClient.setLocalVideoView(binding.surfaceRender)
        webRTCClient.setRemoteVideoView(binding.remoteSurfaceRender)
        webRTCClient.initSurfaceRenders()
    }

    //region Implementation WebRTCClient.WebRTCClientListener
    override fun contactWantChangeToVideoCall() {
        binding.imageButtonChangeToVideo.isEnabled = true
        Utils.alertDialogWithoutNeutralButton(
            R.string.text_contact_want_change_to_video_call,
            false,
            this,
            Constants.LocationAlertDialog.CALL_ACTIVITY.location,
            R.string.text_accept,
            R.string.text_cancel,
            clickPositiveButton = {
                webRTCClient.acceptChangeToVideoCall()
                binding.textViewTitle.text =
                    getString(R.string.text_encrypted_video_call)
            }, clickNegativeButton = {
                webRTCClient.cancelChangeToVideoCall()
            }
        )
    }

    override fun contactCancelledVideoCall() {
        Utils.alertDialogInformative(
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
            isVideoCall = true
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
        binding.textViewTitle.text =
            getString(
                stringResourceId,
                this.getString(R.string.label_nickname, contact?.getNickName())
            )
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
            if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                binding.containerControls.visibility = View.VISIBLE
                binding.fabAnswer.visibility = View.GONE
            }
        }
    }

    override fun resetIsOnCallPref() {
        viewModel.resetIsOnCallPref()
    }

    override fun contactNotAnswer() {
        if (typeCall == Constants.TypeCall.IS_OUTGOING_CALL.type) {
            Timber.d("CancelCall")
            viewModel.cancelCall(contactId, channel)
            viewModel.sendMissedCall(contactId, isVideoCall)
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
                getString(if (isVideoCall) R.string.text_encrypting_videocall else R.string.text_encrypting_call)
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

    //endregion
}
