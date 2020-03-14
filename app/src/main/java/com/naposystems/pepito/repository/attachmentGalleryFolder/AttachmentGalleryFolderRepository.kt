package com.naposystems.pepito.repository.attachmentGalleryFolder

import android.content.ContentUris
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AttachmentGalleryFolderRepository @Inject constructor(private val context: Context) :
    IContractAttachmentGalleryFolders.Repository {

    override suspend fun getFolders(): List<GalleryFolder> {
        val galleryFolders = mutableListOf<GalleryFolder>()

        //SELECT
        val projection = arrayOf(
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            "COUNT(${MediaStore.Files.FileColumns._ID}) AS ${MediaStore.Files.FileColumns._COUNT}",
            MediaStore.Files.FileColumns._ID
        )

        //WHERE
        val selection =
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
                    "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ?"

        //WHERE ARGS
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
        )

        //GROUP
        val bucketGroupBy =
            "$selection) GROUP BY (${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}"

        //SORT
        val bucketSort =
            "${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME} ASC, ${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"

        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection,
            bucketGroupBy,
            selectionArgs,
            bucketSort
        )?.use { cursorFolders ->

            while (cursorFolders.moveToNext()) {

                val folderNameColumnIndex =
                    cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                val idColumnIndex =
                    cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val fileQuantityColumnIndex =
                    cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns._COUNT)
                val mediaTypeColumnIndex =
                    cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                val folderName = cursorFolders.getString(folderNameColumnIndex)
                val fileId = cursorFolders.getInt(idColumnIndex)
                val quantity = cursorFolders.getInt(fileQuantityColumnIndex)
                val mediaType = cursorFolders.getInt(mediaTypeColumnIndex)

                val galleryFolder = GalleryFolder(
                    id = fileId,
                    folderName = folderName,
                    quantity = quantity
                )

                var tableUri: Uri? = null
                var projectionThumbnail: Array<String>? = null
                var selectionThumbnail: String? = null
                var dataColumn = ""

                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    tableUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI
                    projectionThumbnail = arrayOf(MediaStore.Images.Thumbnails.DATA)
                    selectionThumbnail = "${MediaStore.Images.Thumbnails.IMAGE_ID}=$fileId"
                    dataColumn = MediaStore.Images.Thumbnails.DATA

                } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    tableUri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI
                    projectionThumbnail = arrayOf(MediaStore.Video.Thumbnails.DATA)
                    selectionThumbnail = "${MediaStore.Video.Thumbnails.VIDEO_ID}=$fileId"
                    dataColumn = MediaStore.Video.Thumbnails.DATA
                }

                context.contentResolver.query(
                    tableUri!!,
                    projectionThumbnail,
                    selectionThumbnail,
                    null,
                    null
                )?.use {
                    if (it.moveToFirst()) {
                        val dataColumnIndex = it.getColumnIndexOrThrow(dataColumn)
                        galleryFolder.thumbnailUri =
                            Uri.parse(it.getString(dataColumnIndex))
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            fileId.toLong()
                        )
                        galleryFolder.contentUri = contentUri
                    }
                }

                galleryFolders.add(galleryFolder)
            }
        }

        return galleryFolders
    }
}