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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultiAttachmentMsgItemBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.RetryUpload
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.ViewAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.*
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
import timber.log.Timber
import kotlin.math.min
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

    fun bindAttachment(attachmentEntity: AttachmentEntity, index: Int, mine: Boolean) {
        theAttachment = attachmentEntity
        mIndex = index
        loadImage()
        handleAttachmentStatus(mine)
    }

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    private fun handleAttachmentStatus(mine: Boolean) {
        theAttachment?.let {
            when (it.status) {
                SENDING.status, DOWNLOADING.status -> uiModeProcessing()
                SENT.status, NOT_DOWNLOADED.status, DOWNLOAD_ERROR.status -> uiModeDone()
                ERROR.status -> uiModeError()
                RECEIVED.status, DOWNLOAD_COMPLETE.status -> uiReceived()
                READED.status -> uiReaded(mine)
                //NOT_DOWNLOADED.status -> launchDownload()
                else -> Unit
            }
        }
    }

    private fun uiReceived() {
        viewBinding.apply {
            showViews(imageViewAttachment, imageViewIconShow, imageViewStatus)
            hideViews(progressBar, imageRetry)
            imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_unread))
        }
    }

    private fun uiReaded(mine: Boolean) {
        viewBinding.apply {
            showViews(imageViewAttachment, imageViewIconShow, imageViewStatus)
            hideViews(progressBar, imageRetry)
            if (mine) { // is Mine
                imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_baseline_check_circle))
            } else {
                imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_readed))
            }
        }
    }

    private fun uiModeError() = viewBinding.apply {
        showViews(imageRetry)
        hideViews(progressBar, imageViewAttachment, imageViewIconShow, imageViewStatus)
    }

    private fun uiModeDone() = viewBinding.apply {
        showViews(imageViewAttachment, imageViewIconShow, imageViewStatus)
        hideViews(progressBar, imageRetry)
        imageViewStatus.setImageDrawable(root.context.getDrawable(R.drawable.ic_message_sent))
    }

    private fun uiModeProcessing() = viewBinding.apply {
        hideViews(imageViewAttachment, imageViewIconShow, imageRetry, imageViewStatus)
        showViews(progressBar)
    }

    private fun defineViewListeners() = viewBinding.apply {
        imageViewAttachment.setOnClickListener {
            ifNotNull(theAttachment, listener) { _, listener ->
                listener.onMsgItemFileAction(ViewAttachment(mIndex))
            }
        }

        imageRetry.setOnClickListener {
            ifNotNull(theAttachment, listener) { attachment, listener ->
                listener.onMsgItemFileAction(RetryUpload(attachment))
            }
        }
    }

    private fun launchDownload() = ifNotNull(theAttachment, listener) { attachment, listener ->
        listener.onMsgItemFileAction(MultiAttachmentMsgItemAction.RetryDownload(attachment))
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