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
        }
    }

    fun loadImage() {
        try {
            viewBinding.apply {
                Glide.with(root.context)
                    .load("https://cms-assets.tutsplus.com/uploads/users/2659/posts/31467/image/gilbert-free-color-fonts.jpg")
                    .into(imageViewAttachment)
            }
        } catch (exception: Exception) {

        }
    }

}