package com.naposystems.napoleonchat.ui.multi.views.itemview

import android.view.View
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachFileBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.abstracts.GroupieItemViewSelectable
import com.naposystems.napoleonchat.utility.extensions.hide
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
    }

    private fun checkSelected() {
        isSelected = item.isSelected
        selectionResolver()
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

    override fun changeToSelected() = binding.constraintSelected.show()

    override fun changeToUnselected() = binding.constraintSelected.hide()

}