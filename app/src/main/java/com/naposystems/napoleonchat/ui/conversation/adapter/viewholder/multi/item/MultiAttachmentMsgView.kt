package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.databinding.ItemViewMultiAttachmentMsgItemBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.ViewAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.*
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
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
                SENT.status, DOWNLOADING.status -> uiModeSent()
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

    fun defineListener(listener: MultiAttachmentMsgItemListener) {
        this.listener = listener
    }

    private fun defineViewListeners() {
        viewBinding.apply {
            imageViewIconShow.setOnClickListener {
                ifNotNull(theAttachment, listener) { attachment, listener ->
                    listener.onMsgItemFileAction(ViewAttachment(attachment, mIndex))
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
    }

    fun loadImage() {
        try {
            viewBinding.apply {
                Glide.with(root.context)
                    .load(theAttachment?.body)
                    .into(imageViewAttachment)
            }
        } catch (exception: Exception) {

        }
    }

}