package com.naposystems.napoleonchat.ui.multipreview.views.items

import android.view.View
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewAttachmentOptionBinding
import com.naposystems.napoleonchat.ui.multipreview.model.AttachmentOptionItem
import com.naposystems.napoleonchat.ui.multipreview.model.AttachmentOptionItem.*
import com.naposystems.napoleonchat.utility.abstracts.GroupieItemViewSelectable

class AttachmentOptionItemView(
    val item: AttachmentOptionItem
) : GroupieItemViewSelectable<ItemViewAttachmentOptionBinding>() {

    private lateinit var binding: ItemViewAttachmentOptionBinding

    override fun initializeViewBinding(view: View): ItemViewAttachmentOptionBinding =
        ItemViewAttachmentOptionBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_attachment_option

    override fun changeToSelected() {
        binding.layout.isSelected = true
    }

    override fun changeToUnselected() {
        binding.layout.isSelected = false
    }

    override fun bind(viewBinding: ItemViewAttachmentOptionBinding, position: Int) {
        binding = viewBinding
        selectionResolver()
        configureOptionType()
    }

    fun changeDrawableIcon(iconSelfDestruction: Int) {
        val drawable = binding.root.context.getDrawable(iconSelfDestruction)
        binding.imageFolderThumbnail.setImageDrawable(drawable)
    }

    private fun configureOptionType() = when (item) {
        AUTO_DESTRUCTION -> modeAutoDestruction()
        CAN_RESEND -> TODO()
        CAN_DOWNLOAD -> TODO()
        DELETE -> modeDelete()
    }

    private fun modeAutoDestruction() = binding.apply {
        layout.isSelected = true
        imageFolderThumbnail.setImageResource(R.drawable.ic_format_time)
    }

    private fun modeDelete() = binding.apply {
        layout.isSelected = false
        imageFolderThumbnail.setImageResource(R.drawable.ic_delete_black)
    }

}