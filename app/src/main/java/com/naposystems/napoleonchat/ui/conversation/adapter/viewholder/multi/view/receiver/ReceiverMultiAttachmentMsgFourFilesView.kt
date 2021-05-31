package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.receiver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgFourFilesBinding
import com.naposystems.napoleonchat.databinding.ViewReceiverMultiAttachmentMsgFourFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import kotlinx.android.synthetic.main.view_multi_attachment_msg_four_files.view.*

class ReceiverMultiAttachmentMsgFourFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewReceiverMultiAttachmentMsgFourFilesBinding =
        ViewReceiverMultiAttachmentMsgFourFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachments(
        listAttachments: List<AttachmentEntity>,
        isStateError: Boolean
    ) =
        viewBinding.apply {

            viewFileOne.defineListener(this@ReceiverMultiAttachmentMsgFourFilesView)
            viewFileTwo.defineListener(this@ReceiverMultiAttachmentMsgFourFilesView)
            viewFileThree.defineListener(this@ReceiverMultiAttachmentMsgFourFilesView)
            viewFileFour.defineListener(this@ReceiverMultiAttachmentMsgFourFilesView)

            viewFileOne.bindAttachment(listAttachments[0], 0, isStateError)
            viewFileTwo.bindAttachment(listAttachments[1], 1, isStateError)
            viewFileThree.bindAttachment(listAttachments[2], 2, isStateError)
            viewFileFour.bindAttachment(listAttachments[3], 3, isStateError)

        }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }


}