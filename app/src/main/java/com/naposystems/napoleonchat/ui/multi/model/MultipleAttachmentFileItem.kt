package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri

data class MultipleAttachmentFileItem(
    val id: Int,
    val attachmentType: String = "",
    var contentUri: Uri? = null
)