package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.receiver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.databinding.ItemViewMultiAttachmentMsgItemReceiverBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.ViewAttachment
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.utility.extensions.getBlurTransformation
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
import timber.log.Timber
import kotlin.properties.Delegates

class ReceiverMultiAttachmentMsgView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val viewBinding: ItemViewMultiAttachmentMsgItemReceiverBinding =
        ItemViewMultiAttachmentMsgItemReceiverBinding.inflate(
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

    fun bindAttachment(attachmentEntity: AttachmentEntity, index: Int, isMsgStateError: Boolean) {
        theAttachment = attachmentEntity
        mIndex = index
        loadImageFromData()
        theAttachment?.let {
            viewBinding.viewAttachmentStatus.bindAttachment(it, isMsgStateError)
        }
    }

    private fun loadImageFromData() = theAttachment?.thumbnailUri?.let {
        if (it.isNotEmpty()) {
            loadImage(it)
        } else {
            showFalseView()
        }
    }

    private fun showFalseView() = viewBinding.apply {
        imageViewAttachment.hide()
        viewFalseImage.show()
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

//        imageRetry.setOnClickListener {
//            ifNotNull(theAttachment, listener) { attachment, listener ->
//                listener.onMsgItemFileAction(RetryUpload(attachment))
//            }
//        }
    }

    private fun launchDownload() = ifNotNull(theAttachment, listener) { attachment, listener ->
        listener.onMsgItemFileAction(MultiAttachmentMsgItemAction.RetryDownload(attachment))
    }

    private fun loadImage(uri: String) {
        viewBinding.apply {
            imageViewAttachment.show()
            viewFalseImage.hide()
            Glide.with(root.context)
                .load(uri)
                .transform(*getBlurTransformation(root.context))
                .into(imageViewAttachment)
        }
    }

}