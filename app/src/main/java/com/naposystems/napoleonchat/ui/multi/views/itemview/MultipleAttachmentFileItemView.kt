package com.naposystems.napoleonchat.ui.multi.views.itemview

import android.graphics.drawable.Drawable
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachFileBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.abstracts.GroupieItemViewSelectable
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.isVideo
import com.naposystems.napoleonchat.utility.extensions.show

class MultipleAttachmentFileItemView(
    val item: MultipleAttachmentFileItem
) : GroupieItemViewSelectable<ItemViewMultipleAttachFileBinding>() {

    private lateinit var binding: ItemViewMultipleAttachFileBinding

    override fun initializeViewBinding(view: View): ItemViewMultipleAttachFileBinding =
        ItemViewMultipleAttachFileBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_multiple_attach_file

    override fun bind(viewBinding: ItemViewMultipleAttachFileBinding, position: Int) {
        binding = viewBinding
        loadImage()
        checkSelected()
        checksVideo()
    }

    private fun checkSelected() {
        isSelected = item.isSelected
        selectionResolver()
    }

    private fun loadImage() {
        try {
            binding.apply {
                Glide.with(root.context)
                    .load(item.contentUri)
//                    .addListener(object : RequestListener<Drawable> {
//                        override fun onLoadFailed(
//                            e: GlideException?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            checksVideo()
//                            return false
//                        }
//
//                        override fun onResourceReady(
//                            resource: Drawable?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            dataSource: DataSource?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            checksVideo()
//                            return false
//                        }
//                    })
                    .into(imageFolderThumbnail)
            }
        } catch (exception: Exception) {

        }
    }

    private fun checksVideo() = binding.layoutVideo.show(item.isVideo())

    override fun changeToSelected() = binding.constraintSelected.show()

    override fun changeToUnselected() = binding.constraintSelected.hide()

}