package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.naposystems.napoleonchat.databinding.ViewMultiAttachmentMsgFiveFilesBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.utility.extensions.show

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

    lateinit var listAttachments: List<AttachmentEntity>

    fun bindAttachments(listAttachments: List<AttachmentEntity>, mine: Boolean) {
        this.listAttachments = listAttachments
        viewBinding.apply {
            viewFileOne.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileTwo.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileThree.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileFour.defineListener(this@MultiAttachmentMsgFiveFilesView)
            viewFileFive.defineListener(this@MultiAttachmentMsgFiveFilesView)

            viewFileOne.bindAttachment(listAttachments[0], 0, mine)
            viewFileTwo.bindAttachment(listAttachments[1], 1, mine)
            viewFileThree.bindAttachment(listAttachments[2], 2, mine)
            viewFileFour.bindAttachment(listAttachments[3], 3, mine)
            viewFileFive.bindAttachment(listAttachments[4], 4, mine)
        }
        validateMustShowMoreFiles()
        defineViewListeners()
    }

    private fun validateMustShowMoreFiles() = viewBinding.apply {
        val difference = listAttachments.size - 5
        textMoreFilesQuantity.text = "+$difference"
        textMoreFilesQuantity.show(difference > 0)
    }

    private fun defineViewListeners() {
        viewBinding.apply {
            textMoreFilesQuantity.setOnClickListener {
                listener?.onMsgItemFileAction(
                    MultiAttachmentMsgItemAction.ViewAttachment(5)
                )
            }
        }
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        listener?.onMsgItemFileAction(action)
    }

}