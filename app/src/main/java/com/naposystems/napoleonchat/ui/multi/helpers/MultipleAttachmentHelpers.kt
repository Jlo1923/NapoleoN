package com.naposystems.napoleonchat.ui.multi.helpers

import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.naposystems.napoleonchat.utility.Constants

@RequiresApi(Build.VERSION_CODES.Q)
val projectionApiLvl29Folders = arrayOf(
    MediaStore.Files.FileColumns.BUCKET_ID,
    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
    MediaStore.Files.FileColumns.DATE_MODIFIED,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.HEIGHT,
    MediaStore.Files.FileColumns.WIDTH,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.SIZE,
    MediaStore.Files.FileColumns.PARENT,
    MediaStore.Files.FileColumns.DATA,
)

val projectionApiLvl24Folders = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.DATE_MODIFIED,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.Files.FileColumns.HEIGHT,
    MediaStore.Files.FileColumns.WIDTH,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.SIZE,
    MediaStore.Files.FileColumns.PARENT,
    MediaStore.Files.FileColumns.DATA,
)

@RequiresApi(Build.VERSION_CODES.Q)
val projectionApiLvl29Files = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.Files.FileColumns.DATE_MODIFIED,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.SIZE,
    MediaStore.Files.FileColumns.PARENT,
    MediaStore.Files.FileColumns.DISPLAY_NAME,
    MediaStore.Files.FileColumns.DURATION
)

val projectionApiLvl24Files = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.Files.FileColumns.DATE_MODIFIED,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.SIZE,
    MediaStore.Files.FileColumns.PARENT,
    MediaStore.Files.FileColumns.DISPLAY_NAME,
    MediaStore.Files.FileColumns.DATA
)


//WHERE
const val whereForMediaStore =
    "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
            "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.SIZE} > 0 " +
            "AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml' " +
            "AND ${MediaStore.Files.FileColumns.SIZE} <= ${Constants.MAX_IMAGE_VIDEO_FILE_SIZE}"

//WHERE
@RequiresApi(Build.VERSION_CODES.Q)
val whereForMediaStoreFiles =
    "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
            "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml' " +
            "AND ${MediaStore.Files.FileColumns.PARENT}=? AND ${MediaStore.Files.FileColumns.SIZE} <= ${Constants.MAX_IMAGE_VIDEO_FILE_SIZE}"

//WHERE
@RequiresApi(Build.VERSION_CODES.Q)
val whereForMediaStoreFilesWithName =
    "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
            "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml' " +
            "AND ${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}=? AND ${MediaStore.Files.FileColumns.SIZE} <= ${Constants.MAX_IMAGE_VIDEO_FILE_SIZE}"


//WHERE ARGS
val selectionArgsForMediaStore: Array<String> = arrayOf(
    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
    MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
)

fun getSelectionArgsForFilesByFolderName(folderName: String): Array<String> =
    arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(),
        folderName
    )

fun getSelectionArgsForFilesByFolderParent(folderParent: String): Array<String> =
    arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(),
        folderParent
    )


@RequiresApi(Build.VERSION_CODES.Q)
val bucketSortApiLvl29 =
    "${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME} ASC, ${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"

const val bucketSortApiLvl24 =
    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"

const val sortForFiles =
    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"
