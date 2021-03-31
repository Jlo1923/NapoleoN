package com.naposystems.napoleonchat.ui.multi.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.naposystems.napoleonchat.databinding.ViewAttachmentButtonSendBinding
import com.naposystems.napoleonchat.databinding.ViewAttachmentPreviewBottomSmallBinding
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item

class ViewAttachmentsButtonSend @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val viewBinding: ViewAttachmentButtonSendBinding =
        ViewAttachmentButtonSendBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {

    }

    fun setText(string: String) {
        viewBinding.apply { textFilesCount.text = string }
    }

    fun defineListener(function: () -> Unit) {
        viewBinding.cardView.setOnClickListener {
            function.invoke()
        }
    }

}