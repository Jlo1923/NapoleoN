package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.receiver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgTwoFilesBinding
import com.naposystems.napoleonchat.databinding.ViewReceiverMultiAttachmentMsgTwoFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener

class ReceiverMultiAttachmentMsgTwoFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewReceiverMultiAttachmentMsgTwoFilesBinding =
        ViewReceiverMultiAttachmentMsgTwoFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachments(
        listAttachments: List<AttachmentEntity>,
        isMsgStateError: Boolean
    ) = viewBinding.apply {
        viewFileOne.defineListener(this@ReceiverMultiAttachmentMsgTwoFilesView)
        viewFileTwo.defineListener(this@ReceiverMultiAttachmentMsgTwoFilesView)
        viewFileOne.bindAttachment(listAttachments[0], 0, isMsgStateError)
        viewFileTwo.bindAttachment(listAttachments[1], 1, isMsgStateError)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }


}