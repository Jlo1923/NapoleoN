package com.naposystems.napoleonchat.ui.multi.views.itemview

import android.view.View
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachFileBinding
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachPreviewSmallFileBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.abstracts.GroupieItemViewSelectable
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show
import com.xwray.groupie.viewbinding.BindableItem

class MultipleAttachmentPreviewSmallFileItemView(
    val item: MultipleAttachmentFileItem
) : BindableItem<ItemViewMultipleAttachPreviewSmallFileBinding>() {

    private lateinit var binding: ItemViewMultipleAttachPreviewSmallFileBinding

    override fun initializeViewBinding(view: View): ItemViewMultipleAttachPreviewSmallFileBinding =
        ItemViewMultipleAttachPreviewSmallFileBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_multiple_attach_preview_small_file

    override fun bind(viewBinding: ItemViewMultipleAttachPreviewSmallFileBinding, position: Int) {
        binding = viewBinding
        loadImage()
    }

    private fun loadImage() {
        try {
            binding.apply {
                Glide.with(root.context).load(item.contentUri)
                    .into(imageFolderThumbnail)
            }
        } catch (exception: Exception) {

        }

    }
}