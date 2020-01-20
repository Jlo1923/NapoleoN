package com.naposystems.pepito.ui.custom.inputPanel

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.custom.FabSend

class InputPanelWidget(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs),
    IContractInputPanel {

    private var showEmojiIcon: Boolean = true
    private var showCameraIcon: Boolean = true
    private var showAttachmentIcon: Boolean = true
    private var showOnlySendIcon: Boolean = true

    private val normalMargin by lazy {
        resources.getDimension(R.dimen.normal_margin).toInt()
    }

    private val smallMargin by lazy {
        resources.getDimension(R.dimen.small_margin).toInt()
    }

    private val linearLayoutContainer: LinearLayout by lazy {
        LinearLayout(context)
    }

    private val linearLayoutInputPanel: LinearLayout by lazy {
        LinearLayout(context)
    }

    private val floatingButton: FabSend by lazy {
        FabSend(context)
    }

    private val editText: EditText by lazy {
        EditText(ContextThemeWrapper(context, R.style.EditTextInputPanel))
    }

    private lateinit var imageButtonEmoji: ImageButton
    private lateinit var imageButtonAttachment: ImageButton
    private lateinit var imageButtonCamera: ImageButton

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.InputPanelWidget,
            0, 0
        ).apply {
            try {
                showEmojiIcon = getBoolean(R.styleable.InputPanelWidget_showEmojiIcon, true)
                showAttachmentIcon =
                    getBoolean(R.styleable.InputPanelWidget_showAttachmentIcon, true)
                showCameraIcon = getBoolean(R.styleable.InputPanelWidget_showCameraIcon, true)
                showOnlySendIcon = getBoolean(R.styleable.InputPanelWidget_showOnlySendIcon, false)

                linearLayoutContainer.apply {
                    orientation = HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                addView(linearLayoutContainer)

                with(linearLayoutContainer) {
                    val linearLayoutParams = LayoutParams(linearLayoutContainer.layoutParams)

                    linearLayoutParams.apply {
                        addRule(ALIGN_PARENT_START)
                        addRule(ALIGN_PARENT_END)
                    }

                    layoutParams = linearLayoutParams
                }

                createLinearInputPanel(context)

                createFloatingButton()

                imageButtonEmoji.visibility = if (showEmojiIcon) View.VISIBLE else View.GONE
                imageButtonAttachment.visibility =
                    if (showAttachmentIcon) View.VISIBLE else View.GONE
                imageButtonCamera.visibility = if (showCameraIcon) View.VISIBLE else View.GONE

            } finally {
                recycle()
            }
        }
    }

    private fun createLinearInputPanel(context: Context) {

        linearLayoutInputPanel.apply {
            id = View.generateViewId()
            background = resources.getDrawable(R.drawable.bg_input_panel, context.theme)
            orientation = HORIZONTAL
        }

        linearLayoutContainer.addView(linearLayoutInputPanel)

        with(linearLayoutInputPanel) {
            val linearLayoutParams = LinearLayout.LayoutParams(this.layoutParams)

            linearLayoutParams.apply {
                width = 0
                weight = 1f
                setMargins(normalMargin, smallMargin, normalMargin, smallMargin)
            }

            layoutParams = linearLayoutParams
        }

        imageButtonEmoji =
            addImageButton(context, smallMargin, 0, R.drawable.ic_insert_emoticon_black)

        addEditText(context)

        imageButtonAttachment =
            addImageButton(context, 0, normalMargin, R.drawable.ic_attachment_black)

        imageButtonCamera =
            addImageButton(context, smallMargin, normalMargin, R.drawable.ic_camera_primary)
    }

    private fun createFloatingButton() {

        floatingButton.apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        floatingButton.setShowOnlySendIcon(showOnlySendIcon)

        linearLayoutContainer.addView(floatingButton)

        with(floatingButton) {
            val fabLinearParams = LinearLayout.LayoutParams(floatingButton.layoutParams)

            fabLinearParams.apply {
                marginEnd = smallMargin
                setVerticalGravity(Gravity.CENTER)
            }

            layoutParams = fabLinearParams
        }
    }

    private fun addEditText(context: Context) {

        editText.apply {
            background = resources.getDrawable(android.R.color.transparent, context.theme)
            hint = resources.getString(R.string.text_write_message)
            maxLines = 4
        }

        linearLayoutInputPanel.addView(editText)

        with(editText) {
            val editTextLayoutParams = LinearLayout.LayoutParams(layoutParams)

            editTextLayoutParams.apply {
                width = 0
                weight = 1f
                setMargins(smallMargin, 0, 0, 0)
            }

            layoutParams = editTextLayoutParams
        }
    }

    private fun addImageButton(
        context: Context,
        leftMargin: Int,
        rightMargin: Int,
        drawable: Int
    ): ImageButton {
        val imageButton = ImageButton(context)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true)

        imageButton.apply {
            setImageResource(drawable)
            setBackgroundResource(outValue.resourceId)
            setColorFilter(
                ContextCompat.getColor(context, R.color.secondaryTextColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        linearLayoutInputPanel.addView(imageButton)

        with(imageButton) {
            val imgButtonLayoutParams = LinearLayout.LayoutParams(layoutParams)

            imgButtonLayoutParams.apply {
                setMargins(leftMargin, 0, rightMargin, 0)
            }

            layoutParams = imgButtonLayoutParams
        }

        return imageButton
    }

    //region Implementation IContractInputPanel
    override fun setEditTextWatcher(textWatcher: TextWatcher) {
        editText.addTextChangedListener(textWatcher)
    }

    override fun morphFloatingActionButtonIcon() = floatingButton.morph()

    override fun getEditTex() = editText

    override fun getFloatingActionButton() = floatingButton

    override fun getImageButtonEmoji() = imageButtonEmoji

    override fun getImageButtonAttachment() = imageButtonAttachment

    override fun getImageButtonCamera() = imageButtonCamera

    override fun hideImageButtonCamera() {
        imageButtonCamera.visibility = View.GONE
    }

    override fun showImageButtonCamera() {
        imageButtonCamera.visibility = View.VISIBLE
    }

    //endregion
}