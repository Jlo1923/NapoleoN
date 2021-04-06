package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgFiveFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener

class MultiAttachmentMsgFiveFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewMultiAttachmentMsgFiveFilesBinding =
        ViewMultiAttachmentMsgFiveFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
    }

    fun bindAttachments(listAttachments: List<AttachmentEntity>) {
        viewBinding.apply {
            viewFileOne.bindAttachment(listAttachments[0], 0)
            viewFileTwo.bindAttachment(listAttachments[1], 1)
            viewFileThree.bindAttachment(listAttachments[2], 2)
            viewFileFour.bindAttachment(listAttachments[3], 3)
            viewFileFive.bindAttachment(listAttachments[4], 4)
        }

        defineViewListeners()
    }

    private fun defineViewListeners() {
        viewBinding.apply {
            viewFileOne.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileTwo.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileThree.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileFour.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileFive.defineListener(this@MultiAttachmentMsgFiveFilesView)
        }
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }


}