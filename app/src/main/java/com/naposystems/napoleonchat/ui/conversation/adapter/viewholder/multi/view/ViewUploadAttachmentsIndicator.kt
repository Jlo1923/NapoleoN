package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.naposystems.napoleonchat.databinding.ViewUploadAttachmentsIndicatorBinding
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.models.UploadAttachmentsIndicatorModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.anims.animHideSlideDown
import com.naposystems.napoleonchat.utility.anims.animShowSlideUp
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
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


    fun bindDataInfo(dataModel: UploadAttachmentsIndicatorModel) {
        handleStatusMessage(dataModel.messageStatus)
        showCountAndQuantity(dataModel.uploadAttachmentsCount, dataModel.attachmentsQuantity)
    }

    @SuppressLint("SetTextI18n")
    private fun showCountAndQuantity(
        uploadAttachmentsCount: Int,
        attachmentsQuantity: Int
    ) {
        viewBinding.apply {
            textCount.text = uploadAttachmentsCount.toString()
            textQuantity.text = "/ $attachmentsQuantity"
        }
    }

    private fun handleStatusMessage(messageStatus: Int) = when (messageStatus) {
        Constants.MessageStatus.ERROR.status -> uiModeNormal()
        else -> uiModeSending()
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