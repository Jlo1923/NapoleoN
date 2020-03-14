package com.naposystems.pepito.repository.attachmentGallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.IContractAttachmentGallery
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AttachmentGalleryRepository @Inject constructor(private val context: Context) :
    IContractAttachmentGallery.Repository {

    override suspend fun queryGalleryItemsByFolder(
        page: Int,
        perPage: Int,
        folderName: String
    ): List<GalleryItem> {
        val galleryItems = mutableListOf<GalleryItem>()

        withContext(Dispatchers.IO) {

            val projectionFilesFolder = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
            )

            val selectionFilesFolder =
                "${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}=? " +
                        "AND (${MediaStore.Files.FileColumns.MEDIA_TYPE}=? " +
                        "OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) " +
                        "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ?"

            val selectionArgsFilesFolder = arrayOf(
                folderName,
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
            )

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projectionFilesFolder,
                selectionFilesFolder,
                selectionArgsFilesFolder,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC LIMIT ${page * perPage}, $perPage"
            )?.use { cursorFilesInFolder ->

                while (cursorFilesInFolder.moveToNext()) {

                    val folderNameColumn =
                        cursorFilesInFolder.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                    Timber.d("${folderName}: ${cursorFilesInFolder.getString(folderNameColumn)}")

                    val idColumnIndex =
                        cursorFilesInFolder.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val mediaTypeColumnIndex =
                        cursorFilesInFolder.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                    val fileId = cursorFilesInFolder.getInt(idColumnIndex)
                    val mediaType = cursorFilesInFolder.getInt(mediaTypeColumnIndex)

                    val attachmentType = when (mediaType) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> Constants.AttachmentType.IMAGE.type
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> Constants.AttachmentType.VIDEO.type
                        else -> ""
                    }

                    val galleryItem = GalleryItem(
                        id = fileId,
                        attachmentType = attachmentType
                    )

                    var externalUri: Uri? = null

                    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }

                    val contentUri = ContentUris.withAppendedId(
                        externalUri!!,
                        fileId.toLong()
                    )
                    galleryItem.contentUri = contentUri

                    galleryItems.add(galleryItem)
                }
            }
        }

        return galleryItems
    }
}