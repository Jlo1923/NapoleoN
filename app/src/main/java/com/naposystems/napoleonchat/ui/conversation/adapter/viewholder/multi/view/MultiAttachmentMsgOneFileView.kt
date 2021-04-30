package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgOneFilesBinding
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgTwoFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener

class MultiAttachmentMsgOneFileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewMultiAttachmentMsgOneFilesBinding =
        ViewMultiAttachmentMsgOneFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachments(listAttachments: List<AttachmentEntity>) = viewBinding.apply {
        viewFileOne.defineListener(this@MultiAttachmentMsgOneFileView)
        viewFileOne.bindAttachment(listAttachments[0], 0)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }


}