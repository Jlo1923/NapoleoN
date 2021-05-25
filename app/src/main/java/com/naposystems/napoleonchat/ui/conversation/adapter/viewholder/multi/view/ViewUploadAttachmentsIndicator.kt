package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.naposystems.napoleonchat.databinding.ViewUploadAttachmentsIndicatorBinding
import com.naposystems.napoleonchat.di.module.sources.local.LocalDataSourceModule
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.models.UploadAttachmentsIndicatorModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show

class ViewUploadAttachmentsIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val viewBinding: ViewUploadAttachmentsIndicatorBinding =
        ViewUploadAttachmentsIndicatorBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
    }

    fun bindDataInfo(dataModel: UploadAttachmentsIndicatorModel) {
        handleStatusMessage(dataModel.messageStatus)
        writeQuantity(dataModel.uploadAttachmentsCount, dataModel.attachmentsQuantity)
    }

    private fun writeQuantity(uploadAttachmentsCount: Int, attachmentsQuantity: Int) =
        viewBinding.apply {
            textCount.text = uploadAttachmentsCount.toString()
            textQuantity.text = "/ $attachmentsQuantity"
        }

    private fun handleStatusMessage(messageStatus: Int) = when (messageStatus) {
        Constants.MessageStatus.SENDING.status -> uiModeSending()
        else -> uiModeNormal()
    }

    private fun uiModeSending() = viewBinding.apply {
        lottieUploading.show()
        imageUpload.hide()
    }

    private fun uiModeNormal() = viewBinding.apply {
        lottieUploading.hide()
        imageUpload.show()
    }

}