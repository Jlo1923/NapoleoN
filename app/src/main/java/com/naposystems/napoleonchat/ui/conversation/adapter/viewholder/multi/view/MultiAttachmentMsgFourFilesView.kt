package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgFourFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import kotlinx.android.synthetic.main.view_multi_attachment_msg_four_files.view.*

class MultiAttachmentMsgFourFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewMultiAttachmentMsgFourFilesBinding =
        ViewMultiAttachmentMsgFourFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
    }

    fun bindAttachments(listAttachments: List<AttachmentEntity>) = viewBinding.apply {
        viewFileOne.defineListener(this@MultiAttachmentMsgFourFilesView)
        viewFileTwo.defineListener(this@MultiAttachmentMsgFourFilesView)
        viewFileThree.defineListener(this@MultiAttachmentMsgFourFilesView)
        viewFileFour.defineListener(this@MultiAttachmentMsgFourFilesView)

        viewFileOne.bindAttachment(listAttachments[0], 0)
        viewFileTwo.bindAttachment(listAttachments[1], 1)
        viewFileThree.bindAttachment(listAttachments[2], 2)
        viewFileFour.bindAttachment(listAttachments[3], 3)
    }


    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }


}