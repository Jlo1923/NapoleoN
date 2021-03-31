package com.naposystems.napoleonchat.ui.previewmulti.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.tabs.TabLayout
import com.naposystems.napoleonchat.databinding.ViewAttachmentPreviewBottomTabLayoutBinding
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

    init {

    }

    fun showElements(listElements: List<Item<*>>) {
        groupieAdapter.update(listElements)
    }

    fun setOnClickListenerButton(function: () -> Unit) {
    }


}