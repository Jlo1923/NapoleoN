package com.naposystems.pepito.ui.custom.inputPanel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.InputPanelWidgetBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.FabSend
import com.naposystems.pepito.utility.Utils
import java.util.*
import java.util.logging.Handler

class InputPanelWidget(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    IContractInputPanel {

    private var binding: InputPanelWidgetBinding
    private var showEmojiIcon: Boolean = true
    private var showCameraIcon: Boolean = true
    private var showAttachmentIcon: Boolean = true
    private var showOnlySendIcon: Boolean = false

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
                    R.layout.input_panel_widget,
                    this@InputPanelWidget,
                    true
                )

                showEmojiIcon = getBoolean(R.styleable.InputPanelWidget_showEmojiIcon, true)
                showAttachmentIcon =
                    getBoolean(R.styleable.InputPanelWidget_showAttachmentIcon, true)
                showCameraIcon = getBoolean(R.styleable.InputPanelWidget_showCameraIcon, true)
                showOnlySendIcon = getBoolean(R.styleable.InputPanelWidget_showOnlySendIcon, false)

                binding.imageButtonEmoji.visibility = if (showEmojiIcon) View.VISIBLE else View.GONE
                binding.imageButtonAttachment.visibility =
                    if (showAttachmentIcon) View.VISIBLE else View.GONE
                binding.imageButtonCamera.visibility =
                    if (showCameraIcon) View.VISIBLE else View.GONE

                binding.floatingActionButtonSend.setShowOnlySendIcon(showOnlySendIcon)

            } finally {
                recycle()
            }
        }
    }

    override fun setEditTextWatcher(textWatcher: TextWatcher) {
        binding.textInputEditTextInput.addTextChangedListener(textWatcher)
    }

    override fun getFloatingActionButton(): FabSend {
        return binding.floatingActionButtonSend
    }

    override fun morphFloatingActionButtonIcon() {
        binding.floatingActionButtonSend.morph()
    }

    override fun getIsShowingMic() {
        binding.floatingActionButtonSend.isShowingMic()
    }

    override fun getEditTex(): EditText {
        return binding.textInputEditTextInput
    }

    override fun getImageButtonAttachment(): ImageButton {
        return binding.imageButtonAttachment
    }

    override fun getImageButtonCamera(): ImageButton {
        return binding.imageButtonCamera
    }

    override fun getImageButtonEmoji(): ImageButton {
        return binding.imageButtonEmoji
    }

    override fun hideImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.GONE
    }

    override fun showImageButtonCamera() {
        binding.imageButtonCamera.visibility = View.VISIBLE
    }

    override fun openQuote(messageAndAttachment: MessageAndAttachment) {
        binding.textInputEditTextInput.requestFocus()
        binding.layoutQuote.visibility = View.VISIBLE
        binding.layoutQuote.setupMessageAndAttachment(messageAndAttachment)
        Utils.openKeyboard(binding.textInputEditTextInput)
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

    //endregion
}