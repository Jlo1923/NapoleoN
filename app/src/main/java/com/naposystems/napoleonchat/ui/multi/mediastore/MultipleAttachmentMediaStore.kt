package com.naposystems.napoleonchat.ui.multi.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.*
import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.helpers.*
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFolderItem
import com.naposystems.napoleonchat.utility.Constants
import javax.inject.Inject

class MultipleAttachmentMediaStore @Inject constructor(
    private val context: Context
) : IContractMultipleAttachment.MediaStore {

    override fun getFoldersForConversation(): List<MultipleAttachmentFolderItem> {

        val galleryFolders = mutableListOf<MultipleAttachmentFolderItem>()

        val projection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projectionApiLvl29Folders
        } else {
            projectionApiLvl24Folders
        }

        val whereCondition = whereForMediaStore
        val selectionArgs = selectionArgsForMediaStore

        val sorter = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            bucketSortApiLvl29
        } else {
            bucketSortApiLvl24
        }

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, whereCondition, selectionArgs, sorter
        )?.use { cursorFolders ->
            if (cursorFolders.moveToFirst()) {
                //val folderIdColumnIndex = cursorFolders.getColumnIndexOrThrow(BUCKET_ID)
                val folderNameColumnIndex =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        cursorFolders.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)
                    } else {
                        -1
                    }

                val idColumnIndex = cursorFolders.getColumnIndexOrThrow(_ID)
                val mediaTypeColumnIndex = cursorFolders.getColumnIndexOrThrow(MEDIA_TYPE)

                do {

                    val folderName = if (folderNameColumnIndex != -1) {
                        cursorFolders.getString(folderNameColumnIndex)
                    } else {
                        val indexForData = cursorFolders.getColumnIndexOrThrow(DATA)
                        val splitData = cursorFolders.getString(indexForData).split("/")
                        val nameFromData = splitData[splitData.size - 2]
                        nameFromData
                    }

                    val indexForParent = cursorFolders.getColumnIndexOrThrow(PARENT)
                    val parent = cursorFolders.getString(indexForParent)

                    val fileId = cursorFolders.getInt(idColumnIndex)
                    val mediaType = cursorFolders.getInt(mediaTypeColumnIndex)

                    val exist = galleryFolders.any { it.folderName == folderName }
                    if (!exist) {
                        //val quantity = getCount(folderId, selectionArgs, selectionArgs)
                        val quantity = 10
                        galleryFolders.add(
                            MultipleAttachmentFolderItem(
                                fileId,
                                folderName,
                                quantity,
                                mediaType,
                                parent = parent
                            )
                        )
                    }
                } while (cursorFolders.moveToNext())
            }
        }
        return galleryFolders.toList()
    }

    override fun getFilesByFolder(
        folderParent: String,
        mapIds: Map<Int, Int>
    ): List<MultipleAttachmentFileItem> {
        val galleryFiles = mutableListOf<MultipleAttachmentFileItem>()

        val projection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            projectionApiLvl29Files
        } else {
            projectionApiLvl24Files
        }

        val whereCondition = whereForMediaStoreFiles
        val selectionArgs = getSelectionArgsForFilesByFolderName(folderParent)
        val sorter = sortForFiles

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, whereCondition, selectionArgs, sorter
        )?.use { newCursor ->

            if (newCursor.moveToFirst()) {
                val idColumnIndex = newCursor.getColumnIndexOrThrow(_ID)
                val mediaTypeColumnIndex = newCursor.getColumnIndexOrThrow(MEDIA_TYPE)
                val mediaType = newCursor.getInt(mediaTypeColumnIndex)

                do {
                    val fileId = newCursor.getInt(idColumnIndex)
                    val attachmentType = when (mediaType) {
                        MEDIA_TYPE_IMAGE -> Constants.AttachmentType.IMAGE.type
                        MEDIA_TYPE_VIDEO -> Constants.AttachmentType.VIDEO.type
                        else -> ""
                    }

                    val externalUri: Uri = if (mediaType == MEDIA_TYPE_IMAGE) {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }

                    val contentUri = ContentUris.withAppendedId(externalUri, fileId.toLong())

                    val isInMapIds = mapIds.containsKey(fileId)
                    galleryFiles.add(
                        MultipleAttachmentFileItem(fileId, attachmentType, contentUri, isInMapIds)
                    )

                } while (newCursor.moveToNext())
            }
        }
        return galleryFiles.toList()
    }
}