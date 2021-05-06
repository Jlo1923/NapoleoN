package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgThreeFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener

class MultiAttachmentMsgThreeFilesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), MultiAttachmentMsgItemListener {

    private var listener: MultiAttachmentMsgItemListener? = null

    private val viewBinding: ViewMultiAttachmentMsgThreeFilesBinding =
        ViewMultiAttachmentMsgThreeFilesBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {

    }

    fun bindAttachments(listAttachments: List<AttachmentEntity>, mine: Boolean) = viewBinding.apply {
        viewFileOne.defineListener(this@MultiAttachmentMsgThreeFilesView)
        viewFileTwo.defineListener(this@MultiAttachmentMsgThreeFilesView)
        viewFileThree.defineListener(this@MultiAttachmentMsgThreeFilesView)

        viewFileOne.bindAttachment(listAttachments[0], 0, mine)
        viewFileTwo.bindAttachment(listAttachments[1], 1, mine)
        viewFileThree.bindAttachment(listAttachments[2], 2, mine)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }
}