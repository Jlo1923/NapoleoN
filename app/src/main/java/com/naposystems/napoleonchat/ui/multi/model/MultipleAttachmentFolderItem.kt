package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri

data class MultipleAttachmentFolderItem(
    val id: Int,
    val folderName: String,
    val quantity: Int = 0,
    val mediaType: Int,
    var contentUri: Uri? = null
)