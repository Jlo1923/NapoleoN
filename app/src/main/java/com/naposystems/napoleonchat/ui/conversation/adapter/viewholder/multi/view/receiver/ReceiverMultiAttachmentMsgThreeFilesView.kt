package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.receiver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgThreeFilesBinding
import com.naposystems.napoleonchat.databinding.ViewReceiverMultiAttachmentMsgThreeFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener

class ReceiverMultiAttachmentMsgThreeFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewReceiverMultiAttachmentMsgThreeFilesBinding =
        ViewReceiverMultiAttachmentMsgThreeFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachments(
        listAttachments: List<AttachmentEntity>,
        isStateError: Boolean
    ) = viewBinding.apply {

        viewFileOne.defineListener(this@ReceiverMultiAttachmentMsgThreeFilesView)
        viewFileTwo.defineListener(this@ReceiverMultiAttachmentMsgThreeFilesView)
        viewFileThree.defineListener(this@ReceiverMultiAttachmentMsgThreeFilesView)

        viewFileOne.bindAttachment(listAttachments[0], 0, isStateError)
        viewFileTwo.bindAttachment(listAttachments[1], 1, isStateError)
        viewFileThree.bindAttachment(listAttachments[2], 2, isStateError)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }
}