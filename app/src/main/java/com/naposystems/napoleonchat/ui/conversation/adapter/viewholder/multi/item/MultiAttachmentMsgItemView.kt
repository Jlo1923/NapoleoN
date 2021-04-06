package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.item

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultiAttachmentMsgItemBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.xwray.groupie.viewbinding.BindableItem

class MultiAttachmentMsgItemView(
    val item: AttachmentEntity
) : BindableItem<ItemViewMultiAttachmentMsgItemBinding>() {

    private lateinit var binding: ItemViewMultiAttachmentMsgItemBinding

    override fun initializeViewBinding(view: View): ItemViewMultiAttachmentMsgItemBinding =
        ItemViewMultiAttachmentMsgItemBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_multi_attachment_msg_item

    override fun bind(viewBinding: ItemViewMultiAttachmentMsgItemBinding, position: Int) {
        binding = viewBinding
        loadImage()
    }

    private fun loadImage() {
//        try {
//            binding.apply {
//                Glide.with(root.context)
//                    .load(item.thumbnailUri)
//                    .into(imageViewAttachment)
//            }
//        } catch (exception: Exception) {
//
//        }

        val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()
        transformationList.add(CenterCrop())

        when (item.type) {
            Constants.AttachmentType.IMAGE.type,
            Constants.AttachmentType.VIDEO.type -> {
                transformationList.add(BlurTransformation(binding.root.context))
            }
        }

        transformationList.add(RoundedCorners(8))

        Glide.with(binding.imageViewAttachment)
            .load("https://ichef.bbci.co.uk/news/640/cpsprodpb/35F4/production/_116221831_mediaitem116221830.jpg")
            .transform(
                *transformationList.toTypedArray()
            )
            .into(binding.imageViewAttachment)
    }

}