package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.sender

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.naposystems.napoleonchat.databinding.ViewAttachmentsStatusSenderCollageBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extensions.showViews

class ViewAttachmentStatusSenderCollage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val viewBinding: ViewAttachmentsStatusSenderCollageBinding =
        ViewAttachmentsStatusSenderCollageBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachment(attachment: AttachmentEntity, isMsgStateError: Boolean) {
        bindAttachmentType(attachment)
        bindAttachmentStatus(attachment.status, isMsgStateError)
    }

    private fun bindAttachmentType(attachment: AttachmentEntity) = viewBinding.apply {
        if (attachment.isTypeVideo()) {
            imageViewTypeVideo.show()
            imageViewTypeImage.hide()
        } else {
            imageViewTypeVideo.hide()
            imageViewTypeImage.show()
        }
    }

    private fun bindAttachmentStatus(attachmentStatus: Int, isMsgStateError: Boolean) {
        when (attachmentStatus) {
            Constants.AttachmentStatus.ERROR.status -> uiModeError(isMsgStateError)
            Constants.AttachmentStatus.SENDING.status -> uiModeSending()
            Constants.AttachmentStatus.SENT.status -> uiModeSent()
            Constants.AttachmentStatus.RECEIVED.status -> uiModeReceived()
            Constants.AttachmentStatus.READED.status -> uiModeRead()
            else -> Unit
        }
    }

    private fun uiModeError(isMsgStateError: Boolean) = viewBinding.apply {
        layoutCenterStatus.show()
        if (isMsgStateError) imageViewStatusError.show() else imageViewStatusError.hide()
        hideViews(progressBar, imageViewStatusReceived, imageViewStatusRead, imageViewStatusSent)
    }

    private fun uiModeSending() = viewBinding.apply {
        showViews(layoutCenterStatus, progressBar)
        hideViews(
            imageViewStatusReceived,
            imageViewStatusRead,
            imageViewStatusSent,
            imageViewStatusError
        )
    }

    private fun uiModeSent() = viewBinding.apply {
        showViews(layoutCenterStatus, imageViewStatusReceived, imageViewStatusSent)
        hideViews(imageViewStatusError, imageViewStatusRead, progressBar)
    }

    private fun uiModeReceived() = viewBinding.apply {
        showViews(layoutCenterStatus, imageViewStatusReceived)
        hideViews(imageViewStatusError, imageViewStatusRead, progressBar, imageViewStatusSent)
    }

    private fun uiModeRead() = viewBinding.apply {
        showViews(layoutCenterStatus, imageViewStatusRead)
        hideViews(imageViewStatusError, imageViewStatusReceived, progressBar, imageViewStatusSent)
    }

}