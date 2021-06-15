package com.naposystems.napoleonchat.ui.multipreview.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.tabs.TabLayout
import com.naposystems.napoleonchat.databinding.ViewAttachmentPreviewBottomTabLayoutBinding
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item

class ViewAttachmentsPreviewBottomTabs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val groupieAdapter = GroupieAdapter()

    private val viewBinding: ViewAttachmentPreviewBottomTabLayoutBinding =
        ViewAttachmentPreviewBottomTabLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun getTabLayout(): TabLayout = viewBinding.tabLayoutFiles

    fun showElements(listElements: List<Item<*>>) {
        groupieAdapter.update(listElements)
    }

    fun setOnClickListenerButton(function: () -> Unit) =
        viewBinding.buttonSend.setOnClickListener {
            function.invoke()
        }

    fun hideTabLayout() = viewBinding.tabLayoutFiles.hide()

    fun getTextInEdit(): String = viewBinding.editTextMessage.text.toString()

    fun selectTab(index: Int) = viewBinding.tabLayoutFiles.apply {
        val tab = getTabAt(index)
        selectTab(tab)
    }

    fun configForCreate() = viewBinding.apply { editTextMessage.show() }

    fun configForViewAttachments() = viewBinding.apply { hideViews(editTextMessage, buttonSend) }

    fun setMessage(message: String) = viewBinding.apply { textMessage.text = message }

    fun showTextByPosition(position: Int) = viewBinding.apply {
        scrollText.show(position == 0)
        if (textMessage.text.toString().isEmpty()) scrollText.hide()
    }
}