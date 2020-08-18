package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomInputPanelWidgetBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.utility.Utils
import java.util.concurrent.TimeUnit

class InputPanelWidget(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    IContractInputPanel {

    private var binding: CustomInputPanelWidgetBinding
    private var showEmojiIcon: Boolean = true
    private var showCameraIcon: Boolean = true
    private var showAttachmentIcon: Boolean = true

    private val objectAnimatorMic = AnimatorInflater.loadAnimator(
        context,
        R.animator.animator_alpha_repeat_infinite
    ) as ObjectAnimator

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

                binding.imageButtonEmoji.visibility = if (showEmojiIcon) View.VISIBLE else View.GONE
                binding.imageButtonAttachment.visibility =
                    if (showAttachmentIcon) View.VISIBLE else View.GONE
                binding.imageButtonCamera.visibility =
                    if (showCameraIcon) View.VISIBLE else View.GONE


            } finally {
                recycle()
            }
        }
    }

    override fun setEditTextWatcher(textWatcher: TextWatcher) {
        binding.textInputEditTextInput.apply {
            addTextChangedListener(textWatcher)
        }
    }

    override fun getEditTex() = binding.textInputEditTextInput

    override fun getImageButtonAttachment() = binding.imageButtonAttachment

    override fun getImageButtonCamera() = binding.imageButtonCamera

    override fun getImageButtonEmoji() = binding.imageButtonEmoji

    override fun getTextCancelAudio() = binding.textViewCancel

    override fun hideImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.GONE
    }

    override fun showImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.VISIBLE
    }

    override fun openQuote(messageAndAttachment: MessageAndAttachment) {
        binding.textInputEditTextInput.requestFocus()
        binding.layoutQuote.setupMessageAndAttachment(messageAndAttachment)
        binding.layoutQuote.visibility = View.VISIBLE
        //Utils.openKeyboard(binding.textInputEditTextInput)
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

    override fun changeViewSwitcherToCancel() {
        if (binding.viewSwitcherText.nextView.id == binding.textViewCancel.id) {
            binding.viewSwitcherText.showNext()
        }
    }

    override fun changeViewSwitcherToSlideToCancel() {
        if (binding.viewSwitcher.nextView.id == binding.containerSlideToCancel.id) {
            binding.viewSwitcher.inAnimation =
                AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
            binding.viewSwitcher.outAnimation =
                AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
            binding.viewSwitcher.showNext()
            objectAnimatorMic.target = binding.imageViewMic
            objectAnimatorMic.start()
        }
    }

    override fun changeViewSwitcherToInputPanel() {
        if (binding.viewSwitcher.nextView.id == binding.containerInputPanel.id) {
            binding.viewSwitcher.inAnimation =
                AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            binding.viewSwitcher.outAnimation =
                AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
            binding.viewSwitcher.showNext()
            objectAnimatorMic.cancel()
        }

        if (binding.viewSwitcherText.nextView.id == binding.containerTextSlide.id) {
            binding.viewSwitcherText.showNext()
        }
    }

    override fun setRecordingTime(time: Long) {
        binding.textViewTime.apply {
            text = Utils.getDuration(time, time >= TimeUnit.HOURS.toMillis(1))
        }
    }

    override fun clearTextEditText() {
        binding.textInputEditTextInput.setText("")
    }

    //endregion
}