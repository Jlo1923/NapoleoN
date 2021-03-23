package com.naposystems.napoleonchat.ui.previewmulti.views.items

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewAttachmentOptionBinding
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachFileBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.model.AttachmentOptionItem
import com.naposystems.napoleonchat.ui.previewmulti.model.AttachmentOptionItem.*
import com.naposystems.napoleonchat.utility.abstracts.GroupieItemViewSelectable
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show

class AttachmentOptionItemView(
    val item: AttachmentOptionItem
) : GroupieItemViewSelectable<ItemViewAttachmentOptionBinding>() {

    private lateinit var binding: ItemViewAttachmentOptionBinding

    override fun initializeViewBinding(view: View): ItemViewAttachmentOptionBinding =
        ItemViewAttachmentOptionBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_attachment_option

    override fun bind(viewBinding: ItemViewAttachmentOptionBinding, position: Int) {
        binding = viewBinding
        checkSelected()
        configureOptionType()
    }

    private fun configureOptionType() {
        when (item) {
            AUTO_DESTRUCTION -> modeAutoDestruction()
            CAN_RESEND -> TODO()
            CAN_DOWNLOAD -> TODO()
            DELETE -> TODO()
        }
    }

    private fun modeAutoDestruction() {
        binding.apply {
            layout.isSelected = true
            imageFolderThumbnail.setImageResource(R.drawable.ic_format_time)
        }
    }

    private fun checkSelected() {
        //isSelected = item.isSelected
        selectionResolver()
    }

    override fun changeToSelected() {
        binding.layout.isSelected = true
    }

    override fun changeToUnselected() {
        binding.layout.isSelected = false
    }

    fun changeDrawableIcon(iconSelfDestruction: Int) {
        val drawable = binding.root.context.getDrawable(iconSelfDestruction)
        binding.imageFolderThumbnail.setImageDrawable(drawable)
    }

}