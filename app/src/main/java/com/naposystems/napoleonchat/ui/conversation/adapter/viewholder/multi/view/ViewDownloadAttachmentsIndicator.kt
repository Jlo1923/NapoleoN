package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.naposystems.napoleonchat.databinding.ViewDownloadAttachmentsIndicatorBinding
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.models.DownloadAttachmentsIndicatorModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show

class ViewDownloadAttachmentsIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val viewBinding: ViewDownloadAttachmentsIndicatorBinding =
        ViewDownloadAttachmentsIndicatorBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    fun bindDataInfo(dataModel: DownloadAttachmentsIndicatorModel) {
        handleStatusMessage(dataModel.messageStatus)
        showCountAndQuantity(dataModel.downloadAttachmentsCount, dataModel.attachmentsQuantity)
    }

    private fun showCountAndQuantity(downloadAttachmentsCount: Int, attachmentsQuantity: Int) {
        viewBinding.apply {
            textCount.text = downloadAttachmentsCount.toString()
            textQuantity.text = "/ $attachmentsQuantity"
        }
    }

    private fun handleStatusMessage(messageStatus: Int) = when (messageStatus) {
        Constants.MessageStatus.ERROR.status -> uiModeNormal()
        else -> uiModeSending()
    }

    private fun uiModeSending() = viewBinding.apply {
        lottieUploading.show()
        imageDownload.hide()
    }

    private fun uiModeNormal() = viewBinding.apply {
        lottieUploading.hide()
        imageDownload.show()
    }

}