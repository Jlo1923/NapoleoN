package com.naposystems.napoleonchat.ui.multi.views.itemview

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.View
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ItemViewMultipleAttachFolderBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFolderItem
import com.naposystems.napoleonchat.utility.GlideManager
import com.xwray.groupie.viewbinding.BindableItem

class MultipleAttachmentFolderItemView(
    val item: MultipleAttachmentFolderItem
) : BindableItem<ItemViewMultipleAttachFolderBinding>() {

    private lateinit var binding: ItemViewMultipleAttachFolderBinding

    override fun initializeViewBinding(view: View): ItemViewMultipleAttachFolderBinding =
        ItemViewMultipleAttachFolderBinding.bind(view)

    override fun getLayout(): Int = R.layout.item_view_multiple_attach_folder

    override fun bind(viewBinding: ItemViewMultipleAttachFolderBinding, position: Int) {
        binding = viewBinding
        loadInfoFolder()
        loadImageForFolder()
    }

    private fun loadInfoFolder() {
        binding.apply {
            textFolderName.text = item.folderName
        }
    }

    private fun loadImageForFolder() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri =
                    if (item.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    else
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                val contentUri = ContentUris.withAppendedId(uri, item.id.toLong())
                val bitmapThumbnail = binding.root.context.contentResolver.loadThumbnail(
                    contentUri,
                    Size(640, 480),
                    null
                )
                binding.apply {
                    GlideManager.loadBitmap(imageFolderThumbnail, bitmapThumbnail)
                }
            } else {
                val bitmap =
                    MediaStore.Images.Thumbnails.getThumbnail(
                        binding.root.context.contentResolver, item.id.toLong(),
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null as BitmapFactory.Options?
                    )
                binding.apply {
                    GlideManager.loadBitmap(imageFolderThumbnail, bitmap)
                }
            }
        } catch (exception: Exception) {

        }
    }

}