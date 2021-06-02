package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.receiver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.naposystems.napoleonchat.databinding.ViewAttachmentsStatusReceiverCollageBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extensions.showViews

class ViewAttachmentStatusReceiverCollage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val viewBinding: ViewAttachmentsStatusReceiverCollageBinding =
        ViewAttachmentsStatusReceiverCollageBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindAttachment(attachment: AttachmentEntity, isMsgStateError: Boolean) {
        bindAttachmentType(attachment)
        bindAttachmentStatus(attachment.status, isMsgStateError)
    }

    private fun bindAttachmentType(attachment: AttachmentEntity) = viewBinding.apply {
        imageViewTypeVideo.show(attachment.isTypeVideo())
        imageViewTypeImage.show(attachment.isTypeVideo().not())
    }

    private fun bindAttachmentStatus(attachmentStatus: Int, isMsgStateError: Boolean) {
        when (attachmentStatus) {
            Constants.AttachmentStatus.DOWNLOAD_ERROR.status,
            Constants.AttachmentStatus.DOWNLOAD_CANCEL.status -> uiModeError(isMsgStateError)
            Constants.AttachmentStatus.DOWNLOADING.status -> uiModeDownloading()
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> uiModeDownloadComplete()
            Constants.AttachmentStatus.READED.status -> uiModeRead()
            else -> Unit
        }
    }

    private fun uiModeError(isMsgStateError: Boolean) = viewBinding.apply {
        layoutCenterStatus.show()
        imageViewStatusError.show(isMsgStateError)
        hideViews(progressBar, imageViewStatusRead)
    }

    private fun uiModeDownloading() = viewBinding.apply {
        showViews(layoutCenterStatus, progressBar)
        hideViews(imageViewStatusRead, imageViewStatusError)
    }

    private fun uiModeRead() = viewBinding.apply {
        showViews(layoutCenterStatus, imageViewStatusRead)
        hideViews(imageViewStatusError, progressBar)
    }

    private fun uiModeDownloadComplete() = viewBinding.apply {
        showViews(layoutCenterStatus)
        hideViews(imageViewStatusRead, imageViewStatusError, progressBar)
    }

}