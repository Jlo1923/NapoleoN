package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomInputPanelWidgetBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.ui.custom.microphoneRecorderView.MicrophoneRecorderView
import com.naposystems.napoleonchat.utility.Utils
import timber.log.Timber
import java.util.concurrent.TimeUnit

class InputPanelWidget(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    IContractInputPanel, MicrophoneRecorderView.Listener {

    private var binding: CustomInputPanelWidgetBinding
    private var showEmojiIcon: Boolean = true
    private var showCameraIcon: Boolean = true
    private var showAttachmentIcon: Boolean = true
    private var showMicrophone: Boolean = true
    private var showFabSend: Boolean = true
    private var mListener: Listener? = null
    private lateinit var animationMove: Animation

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.InputPanelWidget,
            0, 0
        ).apply {
            try {
                val infService = Context.LAYOUT_INFLATER_SERVICE
                val layoutInflater = getContext().getSystemService(infService) as LayoutInflater
                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.custom_input_panel_widget,
                    this@InputPanelWidget,
                    true
                )

                showEmojiIcon = getBoolean(R.styleable.InputPanelWidget_showEmojiIcon, true)
                showAttachmentIcon =
                    getBoolean(R.styleable.InputPanelWidget_showAttachmentIcon, true)
                showCameraIcon = getBoolean(R.styleable.InputPanelWidget_showCameraIcon, true)
                showMicrophone = getBoolean(R.styleable.InputPanelWidget_showMicrophone, true)
                showFabSend = getBoolean(R.styleable.InputPanelWidget_showFabSend, false)

                binding.imageButtonEmoji.visibility = if (showEmojiIcon) View.VISIBLE else View.GONE
                binding.imageButtonAttachment.visibility =
                    if (showAttachmentIcon) View.VISIBLE else View.GONE
                binding.imageButtonCamera.visibility =
                    if (showCameraIcon) View.VISIBLE else View.GONE
                binding.microphoneRecorderView.isVisible = showMicrophone
                binding.imageButtonSend.isVisible = showFabSend

                binding.microphoneRecorderView.setListener(this@InputPanelWidget)

                binding.textViewCancel.setOnClickListener {
                    cancelRecording()
                }

                binding.imageButtonSend.setOnClickListener {
                    mListener?.onSendButtonClicked()
                }

            } finally {
                recycle()
            }
        }
    }

    interface Listener {
        fun checkRecordAudioPermission(successCallback: () -> Unit)
        fun onRecorderStarted()
        fun onRecorderReleased()
        fun onRecorderLocked()
        fun onRecorderCanceled()
        fun onSendButtonClicked()
    }

    //region IContractInputPanel
    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun isRecordingInLockedMode(): Boolean {
        return binding.microphoneRecorderView.isRecordingLocked()
    }

    override fun releaseRecordingLock() {
        if (binding.viewSwitcherText.nextView.id == binding.containerTextSlide.id) {
            binding.viewSwitcherText.showNext()
        }
        binding.microphoneRecorderView.unlockAction()
    }

    override fun setEditTextWatcher(textWatcher: TextWatcher) {
        binding.textInputEditTextInput.apply {
            addTextChangedListener(textWatcher)
        }
    }

    override fun getEditText() = binding.textInputEditTextInput

    override fun getImageButtonAttachment() = binding.imageButtonAttachment

    override fun getImageButtonCamera() = binding.imageButtonCamera

    override fun getImageButtonEmoji() = binding.imageButtonEmoji

    override fun hideImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.GONE
    }

    override fun showImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.VISIBLE
    }

    override fun hideImageButtonSend() {
        /*binding.imageButtonSend.animate()
            .alpha(0f)
            .setDuration(150)
            .start()*/
        binding.imageButtonSend.isVisible = false
    }

    override fun showImageButtonSend() {
        /*binding.imageButtonSend.animate()
            .alpha(1f)
            .setDuration(150)
            .start()*/
        binding.imageButtonSend.isVisible = true
    }

    override fun hideButtonRecord() {
        binding.microphoneRecorderView.isVisible = false
    }

    override fun showButtonRecord() {
        binding.microphoneRecorderView.isVisible = true
    }

    override fun openQuote(messageAndAttachment: MessageAndAttachment) {
        binding.textInputEditTextInput.requestFocus()
        binding.layoutQuote.setupMessageAndAttachment(messageAndAttachment)
        binding.layoutQuote.visibility = View.VISIBLE
        Utils.openKeyboard(binding.textInputEditTextInput)
    }

    override fun containerWrap() {
        val layoutParams = binding.viewSwitcher.layoutParams
        layoutParams.height = WRAP_CONTENT
        binding.viewSwitcher.layoutParams = layoutParams
    }

    override fun containerNoWrap() {
        val layoutParams = binding.viewSwitcher.layoutParams
        layoutParams.height = resources.getDimension(R.dimen.conversation_fab_size).toInt()
        binding.viewSwitcher.layoutParams = layoutParams
    }

    override fun getWebIdQuote() =
        binding.layoutQuote.getMessageAndAttachment()?.message?.webId ?: ""

    override fun closeQuote() {
        binding.layoutQuote.closeQuote()
    }

    override fun resetImage() {
        binding.layoutQuote.resetImage()
    }

    override fun getQuote() = binding.layoutQuote.getMessageAndAttachment()

    override fun setRecordingTime(time: Long) {
        binding.textViewTime.apply {
            text = Utils.getDuration(time, time >= TimeUnit.HOURS.toMillis(1))
        }
    }

    override fun clearTextEditText() {
        binding.textInputEditTextInput.setText("")
    }

    override fun cancelRecording() {
        binding.microphoneRecorderView.cancelAction()
        if (binding.textInputEditTextInput.text.toString().count() <= 0) {
            showButtonRecord()
        } else {
            showImageButtonSend()
        }
    }

    //endregion

    //region MicrophoneRecorderView.Listener
    override fun checkRecordAudioPermission(successCallback: () -> Unit) {
        mListener?.checkRecordAudioPermission(successCallback)
    }

    override fun onRecordPressed() {
        mListener?.onRecorderStarted()
        binding.containerInputPanel.startAnimation(
            AnimationUtils.loadAnimation(
                context, R.anim.slide_out_left
            )
        )
        binding.containerInputPanel.isVisible = false
        /*Utils.fadeIn(binding.containerSlideToCancel, 150)
        Utils.fadeOut(binding.containerInputPanel, 150, View.INVISIBLE)*/
        binding.containerSlideToCancel.startAnimation(
            AnimationUtils.loadAnimation(
                context, R.anim.slide_in_right_input
            )
        )
        binding.imageViewMic.startAnimation(
            AnimationUtils.loadAnimation(
                context, R.anim.intermittent
            )
        )

        binding.containerSlideToCancel.isVisible = true
    }

    override fun onRecordReleased() {
        /*binding.imageButtonSend.animate()
            .alpha(0f)
            .setDuration(150)
            .start()*/
        binding.imageButtonSend.isVisible = false

        binding.containerInputPanel.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.slide_in_left
            )
        )
        binding.containerInputPanel.isVisible = true
        binding.containerSlideToCancel.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.slide_out_right_input
            )
        )
        binding.containerSlideToCancel.isVisible = false

        mListener?.onRecorderReleased()
    }

    override fun onRecordCanceled() {
        /*binding.imageButtonSend.animate()
            .alpha(0f)
            .setDuration(150)
            .start()*/
        binding.imageButtonSend.isVisible = false

        binding.containerInputPanel.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.slide_in_left
            )
        )
        binding.containerInputPanel.isVisible = true
        binding.containerSlideToCancel.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.slide_out_right_input
            )
        )
        binding.containerSlideToCancel.isVisible = false
        if (binding.viewSwitcherText.nextView.id == binding.containerTextSlide.id) {
            binding.viewSwitcherText.showNext()
        }

        mListener?.onRecorderCanceled()

        animationMove.fillAfter = false
    }

    override fun onRecordLocked() {
        mListener?.onRecorderLocked()
        if (binding.viewSwitcherText.nextView.id == binding.textViewCancel.id) {
            binding.viewSwitcherText.showNext()
        }
        binding.microphoneRecorderView.isVisible = false
        /*binding.imageButtonSend.animate()
            .alpha(1f)
            .setDuration(150)
            .start()*/
        binding.imageButtonSend.isVisible = true
        animationMove.fillAfter = false
    }

    override fun onRecordMoved(offsetX: Float, absoluteX: Float) {
        Timber.d("*TestMove: $offsetX, $absoluteX")
        val moveSetX = offsetX - 26f
        /*val animation: Animation = TranslateAnimation(
            Animation.ABSOLUTE, offsetX,
            Animation.ABSOLUTE, offsetX,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f
        )*/

        animationMove = TranslateAnimation(
            Animation.ABSOLUTE, moveSetX,
            Animation.ABSOLUTE, moveSetX,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f
        )

        animationMove.duration = 0
        animationMove.fillAfter = true
//        animationMove.fillBefore = true

        binding.containerTextSlide.startAnimation(animationMove)

        val direction = ViewCompat.getLayoutDirection(this)
        val position: Float = absoluteX / binding.containerSlideToCancel.width

        if (direction == ViewCompat.LAYOUT_DIRECTION_LTR && position <= 0.5 ||
            direction == ViewCompat.LAYOUT_DIRECTION_RTL && position >= 0.6
        ) {
            binding.microphoneRecorderView.cancelAction()
        }
    }

    override fun onRecordPermissionRequired() {

    }
    //endregion
}