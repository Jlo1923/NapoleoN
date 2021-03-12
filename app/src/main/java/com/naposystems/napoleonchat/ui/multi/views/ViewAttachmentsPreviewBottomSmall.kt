package com.naposystems.napoleonchat.ui.multi.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.naposystems.napoleonchat.databinding.ViewAttachmentPreviewBottomSmallBinding
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item

class ViewAttachmentsPreviewBottomSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val groupieAdapter = GroupieAdapter()

    private val viewBinding: ViewAttachmentPreviewBottomSmallBinding =
        ViewAttachmentPreviewBottomSmallBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
        configureRecycler()
    }

    fun showElements(listElements: List<Item<*>>) {
        groupieAdapter.update(listElements)
        viewBinding.textFilesCount.text = "${listElements.size} de 10"
    }

    fun setOnClickListenerButton(function: () -> Unit) {
        viewBinding.textFilesCount.setOnClickListener {
            function.invoke()
        }
    }

    private fun configureRecycler() {
        val layoutManager = LinearLayoutManager(viewBinding.root.context, HORIZONTAL, false)
        viewBinding.recyclerFiles.apply {
            adapter = groupieAdapter
            setLayoutManager(layoutManager)
        }
    }

}