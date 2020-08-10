package com.naposystems.pepito.repository.attachmentGalleryFolder

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import com.naposystems.pepito.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AttachmentGalleryFolderRepository @Inject constructor(private val context: Context) :
    IContractAttachmentGalleryFolders.Repository {

    override fun getFolders(isConversation: Boolean) = flow {
        withContext(Dispatchers.IO) {
            try {
                emit(GalleryResult.Loading)
                val galleryFolders = mutableListOf<GalleryFolder>()

                //SELECT
                val projection = arrayOf(
                    MediaStore.Files.FileColumns.BUCKET_ID,
                    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.HEIGHT,
                    MediaStore.Files.FileColumns.WIDTH,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE
                )

                //WHERE
                val selection = if (isConversation) {
                    "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
                            "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.SIZE} > 0 " +
                            "AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml' " +
                            "AND ${MediaStore.Files.FileColumns.SIZE} <= ${Constants.MAX_IMAGE_VIDEO_FILE_SIZE}"
                } else {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? " +
                            "AND ${MediaStore.Files.FileColumns.SIZE} > 0 AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml' " +
                            "AND ${MediaStore.Files.FileColumns.SIZE} <= ${Constants.MAX_IMAGE_VIDEO_FILE_SIZE}"
                }

                //WHERE ARGS
                val selectionArgs: Array<String> = if (isConversation) {
                    arrayOf(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
                    )
                } else {
                    arrayOf(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
                    )
                }

                //SORT
                val bucketSort =
                    "${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME} ASC, ${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"

                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    bucketSort
                )?.use { cursorFolders ->

                    if (cursorFolders.moveToFirst()) {
                        val folderIdColumnIndex =
                            cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
                        val folderNameColumnIndex =
                            cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                        val idColumnIndex =
                            cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                        val mediaTypeColumnIndex =
                            cursorFolders.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                        do {
                            val folderId = cursorFolders.getString(folderIdColumnIndex)
                            val folderName = cursorFolders.getString(folderNameColumnIndex)
                            val fileId = cursorFolders.getInt(idColumnIndex)
                            val mediaType = cursorFolders.getInt(mediaTypeColumnIndex)

                            val exist = galleryFolders.any { it.folderName == folderName }

                            if (!exist) {
                                val quantity = getCount(folderId, selection, selectionArgs)
                                val galleryFolder = GalleryFolder(
                                    id = fileId,
                                    folderName = folderName,
                                    quantity = quantity
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val uri =
                                        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                        else
                                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                    val contentUri = ContentUris.withAppendedId(
                                        uri,
                                        fileId.toLong()
                                    )
                                    val bitmapThumbnail = context.contentResolver.loadThumbnail(
                                        contentUri,
                                        Size(640, 480),
                                        null
                                    )
                                    galleryFolder.bitmapThumbnail = bitmapThumbnail
                                } else {
                                    val bitmap =
                                        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                                            MediaStore.Images.Thumbnails.getThumbnail(
                                                context.contentResolver, fileId.toLong(),
                                                MediaStore.Images.Thumbnails.MINI_KIND,
                                                null as BitmapFactory.Options?
                                            )
                                        } else {
                                            MediaStore.Video.Thumbnails.getThumbnail(
                                                context.contentResolver, fileId.toLong(),
                                                MediaStore.Video.Thumbnails.MINI_KIND,
                                                null as BitmapFactory.Options?
                                            )
                                        }

                                    galleryFolder.bitmapThumbnail = bitmap
                                }

                                galleryFolders.add(galleryFolder)
                            }

                        } while (cursorFolders.moveToNext())
                    }
                }

                emit(GalleryResult.Success(galleryFolders))
            } catch (e: Exception) {
                Timber.e(e)
                emit(GalleryResult.Error("Ha ocurrido un error al cargar la galería", e))
            }
        }
    }

    private fun getCount(bucketId: String, selection: String, selectionArgs: Array<String>): Int {
        val projection = arrayOf(
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val newSelection = "$selection AND ${MediaStore.Files.FileColumns.BUCKET_ID} = ?"
        val newSelectionArgs = selectionArgs.plus(bucketId)

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            newSelection,
            newSelectionArgs,
            null
        ).use { cursor ->
            return if (cursor == null || !cursor.moveToFirst()) 0
            else cursor.count
        }
    }
}