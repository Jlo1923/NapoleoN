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
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
import timber.log.Timber
import kotlin.properties.Delegates

class MyMultiAttachmentMsgView @JvmOverloads constructor(
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
        handleAttachmentImage()
        theAttachment?.let {
            viewBinding.viewAttachmentStatus.bindAttachment(it)
        }
    }

    private fun handleAttachmentImage() {
        theAttachment?.let {
            if (it.body.isNotEmpty()) {
                viewBinding.apply {
                    imageViewAttachment.show()
                    imageViewEmpty.hide()
                }
                loadImage()
            } else {
                viewBinding.apply {
                    imageViewAttachment.hide()
                    imageViewEmpty.show()
                }
            }
        }
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