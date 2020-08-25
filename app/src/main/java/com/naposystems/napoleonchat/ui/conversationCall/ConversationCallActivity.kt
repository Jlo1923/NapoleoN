package com.naposystems.napoleonchat.ui.conversationCall

import android.content.Intent
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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityConversationCallBinding
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.webRTC.IContractWebRTCClient
import com.naposystems.napoleonchat.webRTC.WebRTCClient
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess


class ConversationCallActivity : AppCompatActivity(), WebRTCClient.WebRTCClientListener {

    companion object {
        const val IS_VIDEO_CALL = "isVideoCall"
        const val CONTACT_ID = "contact"
        const val IS_INCOMING_CALL = "isIncomingCall"
        const val CHANNEL = "channel"
        const val IS_FROM_CLOSED_APP = "isFromClosedApp"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var socketService: IContractSocketService.SocketService

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var webRTCClient: IContractWebRTCClient

    private lateinit var binding: ActivityConversationCallBinding

    private val viewModel: ConversationCallViewModel by viewModels { viewModelFactory }

    private var contact: Contact? = null

    private var isVideoCall: Boolean = false
    private var contactId: Int = 0
    private var isIncomingCall: Boolean = false
    private var channel: String = ""
    private var isFromClosedApp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        Timber.d("onCreate")

        volumeControlStream = AudioManager.STREAM_MUSIC

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        when (sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)) {
            Constants.ThemesApplication.LIGHT_NAPOLEON.theme -> setTheme(R.style.AppTheme)
            Constants.ThemesApplication.DARK_NAPOLEON.theme -> setTheme(R.style.AppThemeDarkNapoleon)
            Constants.ThemesApplication.BLACK_GOLD_ALLOY.theme -> setTheme(R.style.AppThemeBlackGoldAlloy)
            Constants.ThemesApplication.COLD_OCEAN.theme -> setTheme(R.style.AppThemeColdOcean)
            Constants.ThemesApplication.CAMOUFLAGE.theme -> setTheme(R.style.AppThemeCamouflage)
            Constants.ThemesApplication.PURPLE_BLUEBONNETS.theme -> setTheme(R.style.AppThemePurpleBluebonnets)
            Constants.ThemesApplication.PINK_DREAM.theme -> setTheme(R.style.AppThemePinkDream)
            Constants.ThemesApplication.CLEAR_SKY.theme -> setTheme(R.style.AppThemeClearSky)
        }

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation_call)

        webRTCClient = WebRTCClient(this, socketService, sharedPreferencesManager)

        getExtras()

        webRTCClient.setTextViewCallDuration(binding.textViewCalling)

        with(window) {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

            addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        webRTCClient.setListener(this)

        initSurfaceRenders()

        if (isVideoCall && binding.viewSwitcher.nextView.id == binding.containerVideoCall.id) {
            webRTCClient.startCaptureVideo()
            binding.viewSwitcher.showNext()
        }

        if (isIncomingCall) {
            if (Build.VERSION.SDK_INT < 29 || !isFromClosedApp) {
                webRTCClient.playRingtone()
            }
        } else {
            webRTCClient.playCallingTone()
        }

        binding.fabAnswer.setOnClickListener {
            if (isIncomingCall) {
                NotificationUtils.cancelWebRTCCallNotification(this)
                webRTCClient.emitJoinToCall()
                webRTCClient.stopRingAndVibrate()
                binding.fabAnswer.visibility = View.GONE
            }
            if (!isFromClosedApp) {
                val intent = Intent(this, WebRTCCallService::class.java)
                intent.action = WebRTCCallService.ACTION_CALL_CONNECTED
                this.startService(intent)
            }
        }

        binding.fabHangup.setOnClickListener {
            viewModel.resetIsOnCallPref()
            if (!isIncomingCall && !webRTCClient.isActiveCall()) {
                viewModel.sendMissedCall(contactId, isVideoCall)
            }
            if (!isFromClosedApp) {
                val intent = Intent(this, WebRTCCallService::class.java)
                intent.action = WebRTCCallService.ACTION_CALL_END
                val bundle = Bundle()

                bundle.putString(
                    Constants.CallKeys.CHANNEL,
                    channel
                )

                bundle.putInt(
                    Constants.CallKeys.CONTACT_ID,
                    contactId
                )
                intent.putExtras(bundle)
                this.startService(intent)
            }
            webRTCClient.emitHangUp()
            webRTCClient.dispose()
        }

        binding.imageButtonSpeaker.setOnClickListener {
            webRTCClient.setSpeakerOn()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (isIncomingCall) {
            webRTCClient.handleKeyDown(keyCode)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {
        webRTCClient.emitHangUp()
        webRTCClient.dispose()
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

    override fun onStop() {
        viewModel.resetIsOnCallPref()
        super.onStop()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        webRTCClient.unSubscribeCallChannel()
        super.onDestroy()
    }

    private fun getExtras() {
        intent.extras?.let { bundle ->
            Timber.d("getExtras: $bundle")
            if (bundle.containsKey(IS_INCOMING_CALL)) {
                isIncomingCall = bundle.getBoolean(IS_INCOMING_CALL)
                binding.isIncomingCall = isIncomingCall
            }

            if (intent.action == WebRTCCallService.ACTION_ANSWER_CALL) {
                NotificationUtils.cancelWebRTCCallNotification(this)
                if (isIncomingCall) {
                    Timber.d("ACTION_ANSWER_CALL")
                    NotificationUtils.cancelWebRTCCallNotification(this)
                    channel = bundle.getString(CHANNEL, "")

                    webRTCClient.setChannel(channel)

                    if (isIncomingCall) {
                        webRTCClient.subscribeToChannelFromBackground()
                    }

                    webRTCClient.stopRingAndVibrate()
                    binding.fabAnswer.visibility = View.GONE
                }
            } else {
                if (bundle.containsKey(CHANNEL)) {
                    channel = bundle.getString(CHANNEL, "")
                    webRTCClient.setChannel(channel)
                    if (isIncomingCall) {
                        webRTCClient.subscribeToChannel()
                    }
                }
            }

            if (bundle.containsKey(IS_VIDEO_CALL)) {
                isVideoCall = bundle.getBoolean(IS_VIDEO_CALL, false)
                binding.isVideoCall = isVideoCall
                webRTCClient.setIsVideoCall(isVideoCall)
            }

            if (bundle.containsKey(CONTACT_ID)) {
                contactId = bundle.getInt(CONTACT_ID)
                viewModel.getContact(contactId)
            }

            if (bundle.containsKey(IS_FROM_CLOSED_APP)) {
                isFromClosedApp = bundle.getBoolean(IS_FROM_CLOSED_APP, false)
            }
        }
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
        if (isIncomingCall && isFromClosedApp) {
            exitProcess(0)
        }
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

    override fun changeBluetoothButtonVisibility(visibility: Int) {
        val audioManager: AudioManager = Utils.getAudioManager(this)

        binding.imageButtonBluetooth.visibility = visibility

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
            if (isIncomingCall) {
                binding.containerControls.visibility = View.VISIBLE
                binding.fabAnswer.visibility = View.GONE
            }

            if (!isVideoCall) {
                binding.imageButtonSpeaker.setChecked(false, notifyListener = false)
            }
        }
    }

    override fun resetIsOnCallPref() {
        viewModel.resetIsOnCallPref()
    }

    override fun contactNotAnswer() {
        if (!isIncomingCall)
            viewModel.sendMissedCall(contactId, isVideoCall)
    }

    override fun showTimer() {
        runOnUiThread {
            binding.textViewCalling.visibility = View.GONE
            binding.textViewCallDuration.visibility = View.VISIBLE
        }
    }

    //endregion
}
