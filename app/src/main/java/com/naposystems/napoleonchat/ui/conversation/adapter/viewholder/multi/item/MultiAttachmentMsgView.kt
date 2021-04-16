package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.item

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.databinding.ItemViewMultiAttachmentMsgItemBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.ViewAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.*
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
import timber.log.Timber
import kotlin.properties.Delegates

class MultiAttachmentMsgView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val viewBinding: ItemViewMultiAttachmentMsgItemBinding =
        ItemViewMultiAttachmentMsgItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    private var listener: MultiAttachmentMsgItemListener? = null
    private var theAttachment: AttachmentEntity? = null
    private var mIndex: Int by Delegates.notNull()

    init {
        defineViewListeners()
    }

    fun bindAttachment(attachmentEntity: AttachmentEntity, index: Int) {
        theAttachment = attachmentEntity
        mIndex = index
        loadImage()
        showUiByStatus()
    }

    private fun showUiByStatus() = viewBinding.apply {
        theAttachment?.let {
            when (it.status) {
                SENDING.status -> uiModeSending()
                SENT.status -> uiModeSent()
                ERROR.status -> uiModeError()
            }
        }
    }

    private fun uiModeError() = viewBinding.apply {
        showViews(imageRetry)
        hideViews(progressBar, imageViewAttachment, imageViewIconShow)
    }

    private fun uiModeSent() = viewBinding.apply {
        showViews(imageViewAttachment, imageViewIconShow)
        hideViews(progressBar, imageRetry)
    }

    private fun uiModeSending() = viewBinding.apply {
        hideViews(imageViewAttachment, imageViewIconShow, imageRetry)
        showViews(progressBar)
    }

    private fun uiModeDefault() = viewBinding.apply {
        hideViews(imageViewAttachment, imageViewIconShow, imageRetry, progressBar)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    private fun defineViewListeners() = viewBinding.apply {
        imageViewAttachment.setOnClickListener {
            ifNotNull(theAttachment, listener) { _, listener ->
                listener.onMsgItemFileAction(ViewAttachment(mIndex))
            }
        }
        imageRetry.setOnClickListener {
            ifNotNull(theAttachment, listener) { attachment, listener ->
                listener.onMsgItemFileAction(
                    MultiAttachmentMsgItemAction.RetryUpload(attachment)
                )
            }
        }
    }

    private fun loadImage() {
        try {
            viewBinding.apply {

                val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()
                transformationList.apply {
                    add(CenterCrop())
                    add(BlurTransformation(root.context))
                    add(RoundedCorners(8))
                }

                Glide.with(root.context)
                    .load(theAttachment?.body)
                    .transform(*transformationList.toTypedArray())
                    .into(imageViewAttachment)

            }
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

}